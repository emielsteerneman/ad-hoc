package diffiehellman;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DiffieHellmanProtocol {

	private static long randomSecretInt = (long)(Math.random() * 1000);
	private long s; // secret
	private long prime; // prime
	private static long ground = 2; // grondgetal
	
	public DiffieHellmanProtocol(){
	
	}

	public static long generatePartialKey(long prime, int secretInt) {
		//System.out.println("Starting to calculate my own key..");
		//System.out.println("-------------------");
		
		long key = (long) (Math.pow(ground, secretInt) % prime);
		
		//System.out.println("Key calculated!: " + key);
		//System.out.println("-------------------");
		
		return key;
	}

	public static long calculateSecretKey(long key, int secretInt, long prime) {
		//System.out.println("Starting to calculate the secret key...");
		//System.out.println("-------------------");
		
		return (long) (Math.pow(key, randomSecretInt) % prime);
		
		//System.out.println("Key calculated!: " + s);
		//System.out.println("-------------------");

	}

	public byte[] encrypt(String message) {
		// encrypted message is sent to the used ip address
		System.out.println("Encrypting" + message + "...");
		System.out.println("-------------------");
		System.out.println(message);
		byte[] secretKey = getByteValue(s);
		System.out.println("secretKey: " + Arrays.toString(secretKey));
		byte[] inputBytes = message.getBytes();
		System.out.println("inputBytes: " + Arrays.toString(inputBytes));
		byte[] finalMessage = XOR(inputBytes, secretKey);
		System.out.println("finalMessage: " + Arrays.toString(finalMessage));
		System.out.println("-------------------");
		System.out.println("Message encrypted!");
		return finalMessage;
	}

	public static String decrypt(byte[] encryptedMessage, long key) {
		byte[] secretKey = getByteValue(key);
		System.out.println(secretKey.toString());
		byte[] message = XOR(encryptedMessage, secretKey);
		String finalMessage = new String(message);
		System.out.println("Decrypting message ...");
		System.out.println("Decrypted message: " + finalMessage);
		return finalMessage;
	}

	public static byte[] XOR(byte[] message, byte[] secretKey) {
		System.out.println("XOR Test...");
		System.out.println(Arrays.toString(message));
		System.out.println(Arrays.toString(secretKey));
		byte[] finalMessage = new byte[message.length];
		int i = 0;
		for (byte b : message) {
			finalMessage[i] = (byte) (((b) ^ (secretKey[i++ % secretKey.length])));
		}
		System.out.println(Arrays.toString(finalMessage));
		return finalMessage;
	}

	public static byte[] getByteValue(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(x);
		return buffer.array();
	}

	// @TODO: implement een manier om als een connector een p en g af te spreken
	// met de geconnecteerde.

	public static void main(String[] argv) {
		new DiffieHellmanProtocol();
	
	}

}
