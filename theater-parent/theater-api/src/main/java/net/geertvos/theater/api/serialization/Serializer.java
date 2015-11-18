package net.geertvos.theater.api.serialization;

/**
 * Interface for serializers used by the Theater framework.
 */
public interface Serializer {

	/**
	 * Serialize an object to byte array. Needs to be thread safe. 
	 * @param o Any object
	 * @return The byte array as output
	 */
	byte[] serialize(Object o);
	
}
