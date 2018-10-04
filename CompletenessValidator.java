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
		int fakeDomainBit;
		int rangeBit;
		int startIndex;
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
		fakeDomainBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getFakeDomainBit();
		rangeBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getRangeBit();
		startIndex = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getFakeStartIndex();
		for (int i : indexes) {
			BigInteger fakeValue = ope.simple_OPE_encrypt(BigInteger.valueOf(i+startIndex), key, fakeDomainBit, rangeBit);
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
			maxIndex = maxIndex < numOfFake ? numOfFake : maxIndex;
			int key = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getFakeKey();
			int domainBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getFakeDomainBit();
			int rangeBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getRangeBit();

			BigInteger indexValue = getCorrectIndex(ar.boundary, key, domainBit, rangeBit, ar.symbol, tableName, attributeName);
			validationQuery = validationQuery.replaceAll(attributeName + " " + ar.symbol + " " + ar.boundary.toString(),
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
				case "OPE_SALARY":
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
				startIndex = getFakeIndexFromSalaryTable(tableKey, columnName);
				break;
			case "OPE_EMPLOYEE":
				tableKey = keys.getSingleTableKeys("OPE_EMPLOYEE");
				startIndex = getFakeIndexFromEmployeeTable(tableKey, columnName);
				break;
			case "":
				break;
			default:
				startIndex = -1;
				break;
		}
		if (startIndex == -1) {
			System.out.println("---------------------- \n Error getting Fake Start Index for Table "+tableName + " column " + columnName);
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
		return decryptedValue.subtract(BigInteger.valueOf(startIndex));
	}
	
	private int getFakeIndexFromSalaryTable(TableKey tKey, String column) {
		switch(column.toUpperCase()) {
			case "EMP_NO":
				return tKey.getSingleColumn("emp_no").getFakeStartIndex();
			case "SALARY":
				return tKey.getSingleColumn("salary").getFakeStartIndex();
			case "FROM_DATE":
				return tKey.getSingleColumn("from_date").getFakeStartIndex();
			case "TO_DATE":
				return tKey.getSingleColumn("to_date").getFakeStartIndex();
			default:
				return -1;
		}
	}
	
	private int getFakeIndexFromEmployeeTable(TableKey tKey, String column) {
		return -1;
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



