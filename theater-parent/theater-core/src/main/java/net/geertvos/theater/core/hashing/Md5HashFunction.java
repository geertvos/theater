package net.geertvos.theater.core.hashing;

import java.math.BigInteger;
import java.security.MessageDigest;

import net.geertvos.theater.api.hashing.ConsistentHashFunction;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;


public class Md5HashFunction implements ConsistentHashFunction {

	private StackObjectPool<MessageDigest> md5Pool;

	public Md5HashFunction() {
		PoolableObjectFactory<MessageDigest> realFactory = new BasePoolableObjectFactory<MessageDigest>() {

			@Override
			public MessageDigest makeObject() throws Exception {
				return DigestUtils.getMd5Digest();
			}
		};
		md5Pool = new StackObjectPool<MessageDigest>(realFactory);
	}

	public int hash(String input) {
		try {
			MessageDigest digest = md5Pool.borrowObject();
			byte[] digested = digest.digest(input.getBytes(Charsets.UTF_8));
			BigInteger integer = new BigInteger(digested);
			md5Pool.returnObject(digest);
			return Math.abs(integer.intValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
}
