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
				BigInteger decryptedValue = ope.simple_OPE_decrypt(ar.lower, key, domainBit, rangeBit);
				BigInteger reEncryptedValue = ope.simple_OPE_encrypt(decryptedValue, key, domainBit, rangeBit);
				if ()
			}
			else{
				if 
			}
			
 		}
		return result;
	}

}
