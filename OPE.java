package OPE_DB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/*
 *This class calls the external OPE program and
 *do the encryption and decryption  
 *
 */
public class OPE {
	private static String location = "";
	
	/*
	 * operation: -d: decryption, -e: encryption
	 */
	private BigInteger OPE_call(char operation, BigInteger input, int key, int domain, int range) throws IOException {
		String[] params;
		String result = "";
		Runtime r = Runtime.getRuntime();
		if(operation == 'd') {
			params = new String[] {"-d", input.toString(), Integer.toString(key), Integer.toString(domain),
					Integer.toString(range)};
		}
		else {
			params = new String[] {"-e", input.toString(), Integer.toString(key), Integer.toString(domain),
					Integer.toString(range)};
		}
		Process p = r.exec(location + " " + params); // process to run OPE.c
		BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = is.readLine()) != null)
			result += line;
		try {
			p.waitFor();
		}catch(InterruptedException e) {
			System.err.println(e);
			return null;
		}
		return new BigInteger(result);
	}
	
	/*
	 * Call OPE to encypte a number 
	 */
	public BigInteger OPE_encrypt(BigInteger plaintext, int key, int domain, int range) {
		Random random = new Random();
		try {
			BigInteger lower = OPE_call('e', plaintext, key, domain, range);
			BigInteger upper = OPE_call('e', plaintext.add(BigInteger.ONE), key, domain, range );
			// not totally random because hard to generate a random number in BigInteger.
			// So only get randomize in the range of Long type
			// if (upper - lower) > long.MAX_VALUE
			if(upper.subtract(lower).compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1) {
				return lower.add(BigInteger.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)));
			}else {
				return lower.add(BigInteger.valueOf(ThreadLocalRandom.current().nextLong( upper.subtract(lower).longValue() )));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public BigInteger OPE_encrypt(long plaintext, int key, int domain, int range) {
		return OPE_encrypt(BigInteger.valueOf(plaintext), key, domain, range);
	}
	/*
	 * call OPE to decrypte a number
	 */
	
	public BigInteger OPE_decrypt(BigInteger ciphertext, int key, int domain, int range) {
		try {
			return OPE_call('d', ciphertext, key, domain, range);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public BigInteger OPE_decrypt(long ciphertext, int key, int domain, int range) throws IOException {
		return OPE_decrypt(BigInteger.valueOf(ciphertext), key, domain, range);
	}

}
