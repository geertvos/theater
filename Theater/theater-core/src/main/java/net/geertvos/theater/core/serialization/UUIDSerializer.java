package net.geertvos.theater.core.serialization;

import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class UUIDSerializer extends Serializer<UUID> {

	@Override
	public void write(Kryo kryo, Output output, UUID object) {
		output.writeLong(object.getMostSignificantBits());
		output.writeLong(object.getLeastSignificantBits());
	}

	@Override
	public UUID read(Kryo kryo, Input input, Class<UUID> type) {
		long least = input.readLong();
		long most = input.readLong();
		return new UUID(most,least);
	}

}
