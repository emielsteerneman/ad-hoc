package diffiehellman;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DiffieHellmanProtocol {

	private long a = 6; // secret integer host A, kan random gemaakt
								// worden.
	private long b = 12; // secret integer host B, wordt door host B
								// random gegenereerd.
	private long s; // secret
	private long p; // prime
	private long g = 2; // grondgetal
	private long A; // secret host A
	private long B; // secret host B
	
	public DiffieHellmanProtocol(){
		generateOwnKey();
		generateOtherKey();
		generateKey();
	}

	public long generateOwnKey() {
		setP( PrimeGenerator.generatePrime());
		System.out.println("Starting to calculate my own key..");
		System.out.println("-------------------");
		A = (long) (Math.pow(g, a) % p);
		System.out.println("Key calculated!: " + A);
		System.out.println("-------------------");
		return A;
	}
	
	private void setP(long a){
		
		p = a;
	}
	
	private void setG(long b){
		g = b;
	}

	public long generateOtherKey() {
		System.out.println("Starting to calculate the other host's key...");
		System.out.println("-------------------");
		B = (long) (Math.pow(g, b) % p);
		System.out.println("Key calculated!:" + B);
		System.out.println("-------------------");
		return B;
	}

	public long generateKey() {
		System.out.println("Starting to calculate the secret key...");
		System.out.println("-------------------");
		s = (long) (Math.pow(B, a) % p);
		System.out.println("Key calculated!: " + s);
		System.out.println("-------------------");

		return s;
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

	public String decrypt(byte[] encryptedMessage) {
		byte[] secretKey = getByteValue(s);
		System.out.println(secretKey.toString());
		byte[] message = XOR(encryptedMessage, secretKey);
		String finalMessage = new String(message);
		System.out.println("Decrypting message ...");
		System.out.println("Decrypted message: " + finalMessage);
		return finalMessage;

	}

	public byte[] XOR(byte[] message, byte[] secretKey) {
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

	public byte[] getByteValue(long x) {
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
