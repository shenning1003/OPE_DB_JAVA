package OPE_DB;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class CompletenessValidator {
	OPE ope;
	KeyStructure keys;
	BigInteger expectedTulpe;

	public CompletenessValidator(KeyStructure keys) {
		expectedTulpe = BigInteger.ZERO;
		ope = new OPE();
		this.keys = keys;
	}

	/*
	 * generate all the fake values on a column
	 */
	public ArrayList<BigInteger> getFakeValueForColumn(Query_object qObj, String columnName,
			ArrayList<Integer> indexes) {
		ArrayList<BigInteger> result = new ArrayList<BigInteger>();
		String tableName = "";
		String attributeName = "";
		int key;
		int domainBit;
		int rangeBit;
		if (columnName.contains(".")) {
			String[] words = columnName.split(".");
			tableName = words[0];
			attributeName = words[1];
		} else {
			Map.Entry<String, String> entry = qObj.tableAlias.entrySet().iterator().next();
			tableName = entry.getValue();
			attributeName = columnName;
		}
		key = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getFakeKey();
		domainBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getDomainBit();
		rangeBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getRangeBit();
		for (int i : indexes) {
			BigInteger fakeValue = ope.OPE_encrypt(i, key, domainBit, rangeBit);
			result.add(fakeValue);
		}
		return result;
	}

	private ArrayList<Integer> getFakeTupleIndexes(Query_object qObj) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		String validationQuery = qObj.getTranslatedQuery();
		int maxIndex = 0;
		// create a new query where the conditions are about indexes
		for (AttributeRange ar : qObj.getRangeColumn()) {
			String tableName = ar.table;
			String attributeName = ar.attribute;
			int numOfFake = keys.getSingleTableKeys(tableName).get_fkNum();
			maxIndex = maxIndex < numOfFake ? maxIndex : numOfFake;
			int key = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getFakeKey();
			int domainBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getDomainBit();
			int rangeBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getRangeBit();

			BigInteger indexValue = getCorrectIndex(ar.boundary, key, domainBit, rangeBit, ar.symbol, tableName, attributeName);
			validationQuery.replaceAll(attributeName + " " + ar.symbol + " " + ar.boundary.toString(),
					attributeName + " " + ar.symbol + " " + indexValue.toString());

		}
		// find all the qualified indexes
		ExpressionTree t = new ExpressionTree();
		t.insert(validationQuery);
		for (int i = 1; i < maxIndex; i++) {
			if (t.validate(i, t.getRoot())) {
				indexes.add(i);
			}
		}
		return indexes;
	}

	/*
	 * returns the missing fake tuples.
	 */
	public ArrayList<ArrayList<BigInteger>> checkCompleteness(Query_object qObj,
			ArrayList<ArrayList<BigInteger>> queryResult) {
		ArrayList<ArrayList<BigInteger>> fakeTuples = new ArrayList<ArrayList<BigInteger>>();
		ArrayList<ArrayList<BigInteger>> missingTuples = new ArrayList<ArrayList<BigInteger>>();
		// special handling for "select * ", currently join queyr not supported
		if (qObj.returnAttributes.contains("*")) {
			qObj.returnAttributes.remove("*");
			for (Map.Entry<String, String> entry : qObj.tableAlias.entrySet()) {
				switch (entry.getValue()) {
				case "OPE_salary":
					qObj.returnAttributes.add("EMP_NO");
					qObj.returnAttributes.add("SALARY");
					qObj.returnAttributes.add("FROM_DATE");
					qObj.returnAttributes.add("TO_DATE");
					break;
				case "OPE_EMPLOYEE":
					break;
				default:
					break;

				}
			}
		}
		// get all the indexes that should be returned. 
		ArrayList<Integer> indexes = getFakeTupleIndexes(qObj);
		for (int i = 0; i < qObj.returnAttributes.size(); i++) {
			ArrayList<BigInteger> singleColumn = getFakeValueForColumn(qObj, qObj.returnAttributes.get(i), indexes);
			for (int j = 0; j < singleColumn.size(); j++) {
				if (fakeTuples.size() <= j) { // not inited
					ArrayList<BigInteger> tuple = new ArrayList<BigInteger>();
					tuple.add(singleColumn.get(j));
					fakeTuples.add(tuple);
				} else {
					fakeTuples.get(j).add(singleColumn.get(j));
				}
			}
		}

		for (ArrayList<BigInteger> fakeTuple : fakeTuples) {
			boolean found = false;
			for (ArrayList<BigInteger> tuple : queryResult) {
				boolean allSame = true;
				for (int k = 0; k < tuple.size(); k++) {
					if (!fakeTuple.get(k).equals(tuple.get(k))) {
						allSame = false;
					}
				}
				if (allSame)
					found = true;
			}

			if (!found)
				missingTuples.add(fakeTuple);
		}

		return missingTuples;
	}

	private BigInteger getCorrectIndex(BigInteger cipher, int key, int domainBit, int rangeBit, 
			String comparator, String tableName, String columnName) {
		int startIndex = 0;
		TableKey tableKey = null;
		switch(tableName.toUpperCase()){
			case "OPE_SALARY":
				tableKey = keys.getSingleTableKeys("OPE_SALARY");
				break;
			case "OPE_EMPLOYEE":
				tableKey = keys.getSingleTableKeys("OPE_EMPLOYEE");
				break;
			case "":
				break;
			default:
				break;
		}
		
		
		// BigInteger result = BigInteger.ZERO;
		BigInteger decryptedValue = ope.simple_OPE_decrypt(cipher, key, domainBit, rangeBit);
		BigInteger reEncryptedValue = ope.simple_OPE_encrypt(decryptedValue, key, domainBit, rangeBit);
		if (reEncryptedValue.compareTo(cipher) == 1) {
			if (comparator.equals(">") || comparator.equals(">=")) {
				return decryptedValue;
			} else if (comparator.equals("<") || comparator.equals("<=")) {
				return decryptedValue.add(BigInteger.ONE);
			}
		} else if (reEncryptedValue.compareTo(cipher) == -1) {
			if (comparator.equals(">") || comparator.equals(">=")) {
				return decryptedValue.subtract(BigInteger.ONE);
			} else if (comparator.equals("<") || comparator.equals("<=")) {
				return decryptedValue;
			}
		}
		return decryptedValue;
	}

	public static void main(String args[]) {
		String input = "SELECT * FROM TABLE WHERE A > 20 OR ( C < 10 AND B >= 6 )";
		String test2 = "A > 20 OR ( C < 10 AND B >= 6 )";
		Conversion c = new Conversion(test2.split(" "));
		System.out.println(c.inToPost());
		ExpressionTree t = new ExpressionTree();
		t.insert(input);
		t.postOrder(t.getRoot());
		for (int i = 0; i < 50; i++) {
			if (t.validate(i, t.getRoot()))
				System.out.println(i);
		}
	}

}



