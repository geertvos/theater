package net.geertvos.theater.cassandra.actorstore;

import java.util.UUID;

import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Keyspace;
import net.geertvos.theater.api.actors.Actor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CassandraActorDao {

	private static final int CLASS_IDENTIFIER = 100;
	
	private final ColumnFamilyTemplate<Integer, UUID> template;
	private final Kryo kryo;
	private final Output out = new Output(1,Integer.MAX_VALUE);
	private final Class clazz;
	
	public CassandraActorDao(Keyspace ksp, String columnFamily, Class clazz) {
		this.clazz = clazz;
		kryo = new Kryo();
		kryo.addDefaultSerializer(UUID.class, new net.geertvos.theater.core.serialization.UUIDSerializer());
		template = new ThriftColumnFamilyTemplate<Integer, UUID>(ksp, columnFamily, IntegerSerializer.get(), UUIDSerializer.get());
	}

	public void write(int partition, Actor actor) {
		ColumnFamilyUpdater<Integer, UUID> updater = template.createUpdater(partition);
		updater.setByteArray(actor.getId().getId(), serialize(actor));
		template.update(updater);
	}
	
	public Actor read(int partition, UUID id) {
		 ColumnFamilyResult<Integer, UUID> res = template.queryColumns(partition);
		 if(res.hasResults()) {
			 byte[] data = res.getByteArray(id);
			 if(data != null) {
				 return deserialize(res.getByteArray(id));
			 }
		 }
		 return null;
	}
	
	public void delete(int partition, UUID id) {
		template.deleteColumn(partition, id);
	}
	
	private byte[] serialize(Actor m) {
		out.clear();
		kryo.writeObject(out, m);
		return out.getBuffer();
	}
	
	private Actor deserialize(byte[] data) {
		Input in = new Input(data);
		return kryo.readObject(in, clazz);
	}

}
