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
	
	public boolean checkCompleteness(ArrayList<DB_object> dataset) {
		Collections.sort(dataset, new Comparator<DB_object>() {
			@Override
			public int compare(DB_object o1, DB_object o2) {
				// TODO Auto-generated method stub
				return o2.getID().compareTo(o1.getID());
			}	
		});
		BigInteger upperBound = dataset.get(dataset.size()-1).getID();
		BigInteger lowerBound = dataset.get(0).getID();
				
		try {
			// decrypt bound
			BigInteger upperBound_plaintext = ope.OPE_decrypt(ciphertext, key, domain, range);
			BigInteger lowerBound_plaintext = ope.OPE_decrypt(lowerBound);
			expectedTulpe = upperBound_plaintext.subtract(lowerBound_plaintext).add(BigInteger.ONE);
			
			
			// re-encrypt bound
			BigInteger upperBound_reencrypt = ope.OPE_encryption(upperBound_plaintext);
			BigInteger lowerBound_reencrypt = ope.OPE_encryption(lowerBound_plaintext);
			
			if (upperBound_reencrypt.compareTo(upperBound) < 0 ) {
				expectedTulpe.subtract(BigInteger.ONE);
			}
			if (lowerBound_reencrypt.compareTo(val))
				
			
		}catch(Exception e) {
			
		}
		
		
		// re-encrypt bound
		
		
		
		
		return false;
	}

}
