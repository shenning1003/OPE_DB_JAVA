package OPE_DB;

import java.io.BufferedInputStream;
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
		Process p = r.exec(location + " " + String.join(" ", params)); // process to run OPE.c
		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
		BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = inBr.readLine()) != null) {
			result += line;
		}
		//System.out.println(result);
		return new BigInteger(result);
	}
	
	/*
	 * Call OPE to encypte a number 
	 */
	public BigInteger simple_OPE_encrypt(BigInteger plaintext, int key, int domainBit, int rangeBit) {
		try {
			return OPE_call('e', plaintext, key, domainBit, rangeBit);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	public BigInteger simple_OPE_decrypt(BigInteger ciphertext, int key, int domainBit, int rangeBit) {
		try {
			return  OPE_call('d', ciphertext, key, domainBit, rangeBit);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * Non-deterministic OPE
	 */
	public BigInteger OPE_encrypt(BigInteger plaintext, int key, int domainBit, int rangeBit) {
		try {
			BigInteger lower = OPE_call('e', plaintext, key, domainBit, rangeBit);
			BigInteger upper = OPE_call('e', plaintext.add(BigInteger.ONE), key, domainBit, rangeBit );
			// not totally random because hard to generate a random number in BigInteger.
			// So only get randomize in the range of Long type
			// if (upper - lower) > long.MAX_VALUE
			BigInteger distance = upper.subtract(lower);
			if (distance.compareTo(BigInteger.ZERO) < 0) {
				System.out.println("ERROR");
				System.out.println(plaintext + "   " +  domainBit + "   "+  rangeBit);
			}
			if(distance.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0 ) {
				return lower.add(BigInteger.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)));
			}else {
				return lower.add(BigInteger.valueOf(ThreadLocalRandom.current().nextLong( distance.longValue() )));
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

	/*
	 * Test cases for external OPE program, looks good
	 */
	public static void main(String args[]){
		OPE ope = new OPE();
		try {
			System.out.println(ope.OPE_call('e', BigInteger.valueOf(1), 654321, 50, 60));
			System.out.println(ope.OPE_call('e', BigInteger.valueOf(20), 654321, 5, 60));
			System.out.println(ope.OPE_call('e', BigInteger.valueOf(21), 654321, 5, 60));
			System.out.println(ope.OPE_call('e', BigInteger.valueOf(22), 654321, 5, 60));
			System.out.println(ope.OPE_call('e', BigInteger.valueOf(23), 654321, 5, 60));
			System.out.println(ope.OPE_call('e', BigInteger.valueOf(24), 654321, 5, 60));
//			System.out.println(ope.OPE_call('e', BigInteger.valueOf(11), 123455, 16, 64));
//			System.out.println(ope.OPE_encrypt(BigInteger.valueOf(10), 123455, 16, 64));
//			System.out.println(ope.OPE_decrypt(new BigInteger("3121788886969026"), 123455, 16, 64));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
