package net.geertvos.theater.api.hashing;

public interface ConsistentHashFunction {

	int hash(String input);
	
}
