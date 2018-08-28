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
	private static String location = "/home/nshen/cs546_2/OPE/cryptdb/crypto/a.out";
	
	/*
	 * operation: -d: decryption, -e: encryption
	 */
	private BigInteger OPE_call(char operation, BigInteger input, int key, int domainBit, int rangeBit) throws IOException {
		String[] params;
		String result = "";
		Runtime r = Runtime.getRuntime();
		if(operation == 'd') {
			params = new String[] {"-d", input.toString(), Integer.toString(key), Integer.toString(domainBit),
					Integer.toString(rangeBit)};
		}
		else {
			params = new String[] {"-e", input.toString(), Integer.toString(key), Integer.toString(domainBit),
					Integer.toString(rangeBit)};
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
		System.out.println(result);
		return new BigInteger(result);
	}
	
	/*
	 * Call OPE to encypte a number 
	 */
	public BigInteger OPE_encrypt(BigInteger plaintext, int key, int domainBit, int rangeBit) {
		try {
			BigInteger lower = OPE_call('e', plaintext, key, domainBit, rangeBit);
			BigInteger upper = OPE_call('e', plaintext.add(BigInteger.ONE), key, domainBit, rangeBit );
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
	public BigInteger OPE_encrypt(long plaintext, int key, int domainBit, int rangeBit) {
		return OPE_encrypt(BigInteger.valueOf(plaintext), key, domainBit, rangeBit);
	}
	/*
	 * call OPE to decrypte a number
	 */
	
	public BigInteger OPE_decrypt(BigInteger ciphertext, int key, int domainBit, int rangeBit) {
		try {
			BigInteger likelyValue =  OPE_call('d', ciphertext, key, domainBit, rangeBit);
			BigInteger reEnc = OPE_call('e', likelyValue, key, domainBit, rangeBit);
			return (ciphertext.compareTo(reEnc)== 1) ? likelyValue: likelyValue.subtract(BigInteger.ONE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public BigInteger OPE_decrypt(long ciphertext, int key, int domainBit, int rangeBit) throws IOException {
		return OPE_decrypt(BigInteger.valueOf(ciphertext), key, domainBit, rangeBit);
	}

	public static void main(String args[]){
		OPE ope = new OPE();
		try {
			ope.OPE_call('e', BigInteger.valueOf(111333), 123455, 16, 64);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
