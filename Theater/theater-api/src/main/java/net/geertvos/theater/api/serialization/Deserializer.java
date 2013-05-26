package net.geertvos.theater.api.serialization;

/**
 * Deserializer for the Theater framework
 */
public interface Deserializer {

	/**
	 * Deserialize bytes into an object. Needs to be thread safe.
	 * @param bytes The inpout
	 * @return Object the output
	 */
	Object deserialize(byte[] bytes);
	
}
