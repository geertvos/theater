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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Lists;

public class CassandraMessageLogDao<T> {

	private static final int CLASS_IDENTIFIER = 100;
	
	private final ColumnFamilyTemplate<Integer, UUID> template;
	private final Kryo kryo;
	private final Output out = new Output(1,Integer.MAX_VALUE);
	private final Class<T> messageClass;
	
	public CassandraMessageLogDao(Keyspace ksp, String columnFamily, Class<T> messageClass) {
		this.messageClass = messageClass;
		kryo = new Kryo();
		kryo.register(messageClass, CLASS_IDENTIFIER);
		kryo.addDefaultSerializer(UUID.class, new net.geertvos.theater.core.serialization.UUIDSerializer());
		template = new ThriftColumnFamilyTemplate<Integer, UUID>(ksp, columnFamily, IntegerSerializer.get(), UUIDSerializer.get());
	}

	public void write(int segment, Message message) {
		ColumnFamilyUpdater<Integer, UUID> updater = template.createUpdater(segment);
		updater.setByteArray(message.getMessageId(), serialize(message));
		template.update(updater);
	}
	
	public T read(int segment, UUID id) {
		 ColumnFamilyResult<Integer, UUID> res = template.queryColumns(segment);
		 return deserialize(res.getByteArray(id));
	}
	
	public void delete(int segment, UUID id) {
		template.deleteColumn(segment, id);
	}
	
	private byte[] serialize(Message m) {
		out.clear();
		kryo.writeObject(out, m);
		return out.getBuffer();
	}
	
	private T deserialize(byte[] data) {
		Input in = new Input(data);
		return kryo.readObject(in, messageClass);
	}

	public List<T> getSegment(int segment) {
		HSlicePredicate<UUID> predicate = new HSlicePredicate<UUID>(UUIDSerializer.get());
		predicate.setRange(null, null, false, Integer.MAX_VALUE);
		ColumnFamilyResult<Integer, UUID> res = template.queryColumns(segment,predicate);
		ArrayList<T> messages = new ArrayList<T>();
		for(UUID column : res.getColumnNames()) {
			T m = deserialize(res.getByteArray(column));
			messages.add(m);
		}
		return Lists.reverse(messages);
	}
}
