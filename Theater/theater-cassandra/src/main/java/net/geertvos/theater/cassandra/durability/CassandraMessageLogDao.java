package net.geertvos.theater.cassandra.durability;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.prettyprint.cassandra.model.HSlicePredicate;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Keyspace;
import net.geertvos.theater.api.messaging.Message;
import net.geertvos.theater.core.actor.ActorIdImpl;
import net.geertvos.theater.core.networking.SegmentMessage;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.testng.log4testng.Logger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CassandraMessageLogDao {

	private Logger log = Logger.getLogger(CassandraMessageLogDao.class);
	private final ColumnFamilyTemplate<Integer, UUID> template;

	private StackObjectPool<Kryo> kryoPool;
	
	public CassandraMessageLogDao(Keyspace ksp, String columnFamily) {
		template = new ThriftColumnFamilyTemplate<Integer, UUID>(ksp, columnFamily, IntegerSerializer.get(), UUIDSerializer.get());
		
		PoolableObjectFactory<Kryo> realFactory = new BasePoolableObjectFactory<Kryo>() {

			@Override
			public Kryo makeObject() throws Exception {
				Kryo kryo = new Kryo();
				kryo.register(SegmentMessage.class);
				kryo.register(ActorIdImpl.class);
				kryo.addDefaultSerializer(UUID.class, new net.geertvos.theater.core.serialization.UUIDSerializer());
				return kryo;
			}
		};
		kryoPool = new StackObjectPool<Kryo>(realFactory);

	}

	public void write(int segment, Message message) {
		ColumnFamilyUpdater<Integer, UUID> updater = template.createUpdater(segment);
		updater.setByteArray(message.getMessageId(), serialize(message));
		template.update(updater);
	}
	
	public Message read(int segment, UUID id) {
		 ColumnFamilyResult<Integer, UUID> res = template.queryColumns(segment);
		 try {
			 return deserialize(res.getByteArray(id));
		 } catch(KryoException e) {
			 log.error("Deserialization of message failed, deleting.",e);
			 template.deleteColumn(segment, id);
			 return null;
		 }
	}
	
	public void delete(int segment, UUID id) {
		template.deleteColumn(segment, id);
	}
	
	private byte[] serialize(Message m) {
		Output out = new Output(1,Integer.MAX_VALUE);
		Kryo kryo;
		try {
			kryo = kryoPool.borrowObject();
			kryo.writeClassAndObject(out, m);
			kryoPool.returnObject(kryo);
			return out.getBuffer();
		} catch(Exception e) {
			log.error("Unable to serialize message.", e);
		}
		return null;
	}
	
	private Message deserialize(byte[] data) {
		Input in = new Input(data);
		try {
			Kryo kryo = kryoPool.borrowObject();
			Message m = (Message) kryo.readClassAndObject(in);
			kryoPool.returnObject(kryo);
			return m;
		} catch (Exception e) {
			log.error("Unable to deserialize message.", e);
		}
		return null;
	}

	public List<Message> getSegment(int segment) {
		HSlicePredicate<UUID> predicate = new HSlicePredicate<UUID>(UUIDSerializer.get());
		predicate.setRange(null, null, false, Integer.MAX_VALUE);
		ColumnFamilyResult<Integer, UUID> res = template.queryColumns(segment,predicate);
		ArrayList<Message> messages = new ArrayList<Message>();
		for(UUID column : res.getColumnNames()) {
			try {
				Message m = deserialize(res.getByteArray(column));
				messages.add(m);
			} catch(KryoException e) {
				log.error("Deserialization of message failed, deleting.",e);
				template.deleteColumn(segment, column);
			}
		}
		return messages;
	}
}
