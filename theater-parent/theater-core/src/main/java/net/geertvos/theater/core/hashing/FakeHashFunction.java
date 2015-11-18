package net.geertvos.theater.core.hashing;

import java.math.BigInteger;

import net.geertvos.theater.api.hashing.ConsistentHashFunction;

public class FakeHashFunction implements ConsistentHashFunction{

	
	
	public int hash(String input) {
		byte[] bytes = input.getBytes();
		BigInteger bigInt = new BigInteger(1, bytes);
		return bigInt.intValue();
	}

}