package net.geertvos.theater.cassandra.actorstore;

import java.util.UUID;

import org.testng.log4testng.Logger;

import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Keyspace;
import net.geertvos.theater.api.actors.ActorHandle;
import net.geertvos.theater.api.hashing.HashFunction;
import net.geertvos.theater.core.hashing.Md5HashFunction;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CassandraActorDao {

	private static final Logger LOG = Logger.getLogger(CassandraActorDao.class);
	private final ColumnFamilyTemplate<Integer, UUID> template;
	private final Kryo kryo;
	private final Output out = new Output(1,Integer.MAX_VALUE);
	private final int segmentCount;
	private HashFunction hashFunction =  new Md5HashFunction();

	public CassandraActorDao(Keyspace ksp, String columnFamily, int segmentCount) {
		this.segmentCount = segmentCount;
		this.kryo = new Kryo();
		kryo.addDefaultSerializer(UUID.class, new net.geertvos.theater.core.serialization.UUIDSerializer());
		this.template = new ThriftColumnFamilyTemplate<Integer, UUID>(ksp, columnFamily, IntegerSerializer.get(), UUIDSerializer.get());
	}

	public void write(ActorHandle handle, Object state) {
		int hash = hash(handle.getId());
		int partition = hash % segmentCount;
		ColumnFamilyUpdater<Integer, UUID> updater = template.createUpdater(partition);
		updater.setByteArray(handle.getId(), serialize(state));
		template.update(updater);
	}
	
	private int hash(UUID id) {
		return hashFunction.hash(id.toString());
	}
	
	public Object read(UUID id) {
		int hash = hash(id);
		int partition = hash % segmentCount;

		 ColumnFamilyResult<Integer, UUID> res = template.queryColumns(partition);
		 if(res.hasResults()) {
			 byte[] data = res.getByteArray(id);
			 if(data != null) {
				 try {
					 return deserialize(res.getByteArray(id)); 
				 } catch(Exception e) {
					 LOG.error("Unable to deserialize actor "+id, e);
					 return null;
				 }
			 }
		 }
		 return null;
	}
	
	public void delete(int partition, UUID id) {
		template.deleteColumn(partition, id);
	}
	
	private byte[] serialize(Object m) {
		out.clear();
		kryo.writeClassAndObject(out, m);
		return out.getBuffer();
	}
	
	private Object deserialize(byte[] data) {
		Input in = new Input(data);
		return kryo.readClassAndObject(in);
	}

}
