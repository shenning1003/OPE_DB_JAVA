package OPE_DB;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CompletenessValidator {
	OPE ope;
	KeyStructure keys;
	BigInteger expectedTulpe;
	public CompletenessValidator(KeyStructure keys) {
		expectedTulpe = BigInteger.ZERO;
		ope = new OPE();
		this.keys = keys;
	}
	
	public ArrayList<BigInteger> checkCompleteness(Query_object qObj) {
		ArrayList<BigInteger> result = new ArrayList<BigInteger>();
		for( AttributeRange ar : qObj.getRangeColumn()){
			String tableName = ar.table;
			String attributeName = ar.attribute;
			int numOfFake = keys.getSingleTableKeys(tableName).get_fkNum();
			int key = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getFakeKey();
			int domainBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getDomainBit();
			int rangeBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getRangeBit();
			if (ar.lower.equals(ar.upper)){
				getCorrectIndex(ar.lower, key, domainBit, rangeBit, ar.symbol);
			}
			else{
				
			}
			
 		}
		return result;
	}
	
	private BigInteger getCorrectIndex(BigInteger cipher, int key, int domainBit, int rangeBit, String comparator) {
		//BigInteger result = BigInteger.ZERO;
		BigInteger decryptedValue = ope.simple_OPE_decrypt(cipher, key, domainBit, rangeBit);
		BigInteger reEncryptedValue = ope.simple_OPE_encrypt(decryptedValue, key, domainBit, rangeBit);
		if (reEncryptedValue.compareTo(cipher) == 1) {
			if(comparator.equals(">")|| comparator.equals(">=")) {
				return decryptedValue;
			}
			else if(comparator.equals("<")|| comparator.equals("<=")) {
				return decryptedValue.add(BigInteger.ONE);
			}
		}
		else if(reEncryptedValue.compareTo(cipher) == -1) {
			if (comparator.equals(">")|| comparator.equals(">=")) {
				return decryptedValue.subtract(BigInteger.ONE);
			}
			else if (comparator.equals("<")|| comparator.equals("<=")) {
				return decryptedValue;
			}
		}
		return decryptedValue;
	}

}
