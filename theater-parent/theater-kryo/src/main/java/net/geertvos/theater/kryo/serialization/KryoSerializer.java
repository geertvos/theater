package net.geertvos.theater.kryo.serialization;

import net.geertvos.theater.api.serialization.Deserializer;
import net.geertvos.theater.api.serialization.Serializer;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.log4j.Logger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoSerializer implements Serializer, Deserializer {

	private final static Logger log = Logger.getLogger(KryoSerializer.class);
	private StackObjectPool<Kryo> kryoPool;

	public KryoSerializer() {
		PoolableObjectFactory<Kryo> realFactory = new BasePoolableObjectFactory<Kryo>() {

			@Override
			public Kryo makeObject() throws Exception {
				Kryo kryo = new Kryo();
				return kryo;
			}
		};
		kryoPool = new StackObjectPool<Kryo>(realFactory);
	}
	
	public byte[] serialize(Object object) {
		Kryo kryo;
		try {
			kryo = kryoPool.borrowObject();
			Output output = new Output(1, Integer.MAX_VALUE);
			kryo.writeClassAndObject(output, object);
			kryoPool.returnObject(kryo);
			return output.toBytes();
		} catch (Exception e) {
			log.error("Unable to serialize object.", e);
		}
		return null;
	}


	public Object deserialize(byte[] bytes) {
		if(bytes==null) {
			return null;
		}
		Kryo kryo;
		try {
			kryo = kryoPool.borrowObject();
			Input input = new Input(bytes);
			Object object = kryo.readClassAndObject(input);
			kryoPool.returnObject(kryo);
			return object;
		} catch (Exception e) {
			log.error("Unable to deserialize object.", e);
		}
		return null;
	}

}
