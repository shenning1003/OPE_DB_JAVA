package OPE_DB;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Query_parser {
	
	
	static final char[] characters= {'<', '>', '='};
	static final List<String> symbol = Arrays.asList("<",">","<>","<=",">=", "=");
	static final List<String> joinKeywords = Arrays.asList("LEFT", "INNER", "RIGHT", "OUTER", "JOIN");
	KeyStructure dataBaseKeys;
	OPE ope;
	public Query_parser(KeyStructure keys, OPE ope) {
		this.dataBaseKeys = keys;
		this.ope = ope;
	}
	
	public Query_object parseQuery(String query) {
		Query_object qObj = new Query_object();
		
		query = query.toUpperCase();  // all to uppercase
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < query.length(); i++) {
			if(HelperFunctions.charArrayContains(query.charAt(i), characters)) {
				// if current char is a special char in sql
				if(query.charAt(i-1) != ' ' && !HelperFunctions.charArrayContains(query.charAt(i-1), characters)) {
					// if previous char is not special char and not space, add a space before;
					sb.append(" ");
				}
				sb.append(query.charAt(i));
				
				if(query.charAt(i+1)!=' ' && !HelperFunctions.charArrayContains(query.charAt(i+1), characters)) {
					// if next char is not special char and not space, add a space after
					sb.append(" ");
				}
			}
		}
		// reset query string, so we can split by space
		query = sb.toString();
		sb = new StringBuffer();  // re-init here to build translated query
		qObj.setOriginalQuery(query);
		String[] words = query.trim().split(" ");
		switch(words[0]) {
		case "UPDATE":
			break;
		case "INSERT":
			break;
		case "DELETE":
			break;
		case "SELECT":
			int fromIndex = Arrays.asList(words).indexOf("FROM");
			if(fromIndex == -1)
				return null;
			// find all the returning attributes
			for (int i = 1; i < fromIndex; i ++) {
				sb.append(words[i] + " "); // building query here
				if(words[i].equals(","))
					continue;
				qObj.returnAttributes.add(words[i].replaceAll(",*$", ""));
			}
			int whereIndex = Arrays.asList(words).indexOf("WHERE");
			// find all the tables and their alias
			int current = fromIndex +1;
			sb.append("FROM "); // building here
			for (int i = fromIndex+1; i < whereIndex; i++) {
				sb.append(words[i]+ " ");
				if (joinKeywords.contains(words[i])|| words[i].endsWith(",")) {
					if (i-current <1)
						continue;
					if (i - current == 1) {
						qObj.tableAlias.put(words[current], words[current]);
					}
					else if(i - current == 2) {
						qObj.tableAlias.put(words[current+1], words[current]);
					}
					else {
						System.out.println("Error: parsing Join");
					}
					if (i+2 < whereIndex && words[i+1].equals("JOIN")) {
						current = i +2;
					}
					else {
						if (i +1 < whereIndex) {
							current = i+1;
						}
					}
				}
			}
			if (whereIndex == -1) 
			{// query has no conditions, no need to translate query;
				qObj.translatedQuery = query;
				return qObj;
			}
			sb.append("WHERE ");
			// else, from "where" to iterate all the conditions
			for (int i= whereIndex; i < words.length; i++) {
				if(symbol.contains(words[i])) {
					String[] split = words[i-1].split(".");
					String tableName, columnName;
					if(split.length == 1) {
						Map.Entry<String, String> entry = qObj.tableAlias.entrySet().iterator().next();
						tableName = entry.getValue();
						columnName = words[i-1];
					}
					else if(split.length == 2){
						tableName = qObj.tableAlias.get(split[0]);
						columnName = split[1];
					}
					else {
						System.out.println("Error: parsing table and column name");
					}
					int key = this.dataBaseKeys.getSingleTableKeys(tableName).getSingleColumn(columnName).getDataKey();
					int domainBit = this.dataBaseKeys.getSingleTableKeys(tableName).getSingleColumn(columnName)
							.getDomainBit();
					int rangeBit = this.dataBaseKeys.getSingleTableKeys(tableName).getSingleColumn(columnName)
							.getRangeBit();
					if (words[i+1].startsWith("\"") && words[i+1].endsWith("\"")) { // string
						String str = words[i+1].replaceAll("^\"|\"$", "");
						BigInteger strValue = null;
						if (words[i].equals("<") || words[i].equals("<=")) {
							strValue = ope.simple_OPE_encrypt(HelperFunctions.StringToNumber(str), key, domainBit, rangeBit);
							AttributeRange ar = new AttributeRange(tableName, columnName, strValue, words[i]);
							qObj.addRangeColumn(ar);
							sb.append(words[i-1] + " < "  + strValue.toString() + " ");
						}
						else if (words[i].equals(">") || words[i].equals(">=")) {
							strValue = ope.simple_OPE_encrypt(HelperFunctions.StringToNumber(str).add(BigInteger.ONE)
									, key, domainBit, rangeBit);
							AttributeRange ar = new AttributeRange(tableName, columnName, strValue, words[i]);
							qObj.addRangeColumn(ar);
							sb.append(words[i-1] + " > " + strValue.toString() + " ");
						}
						else if(words[i].equals("=")) {
							BigInteger upper = ope.simple_OPE_encrypt(HelperFunctions.StringToNumber(str).add(BigInteger.ONE)
									, key, domainBit, rangeBit);
							BigInteger lower = ope.simple_OPE_encrypt(HelperFunctions.StringToNumber(str), 
									key, domainBit, rangeBit);
							// a > low && a < high
							AttributeRange ar1 = new AttributeRange(tableName, columnName, lower, ">");
							AttributeRange ar2 = new AttributeRange(tableName, columnName, upper, "<");
							sb.append("(" + words[i-1] + " < " + ar2.toString() + " AND " + words[i-1] + " > " + ar1.toString() );
						}
					}
					else if(words[i+1].matches("\\d{4}-\\d{2}-\\d{2}")) {
						BigInteger dateValue = ope.simple_OPE_decrypt(ciphertext, key, domainBit, rangeBit)
					}
				}
			}
		}

		
		return qObj;
	}
	
	public void SetDatabaseKeys(KeyStructure keys) {
		this.dataBaseKeys = keys;
	}
	
	class Query_object{
		// alias to real table name
		HashMap<String, String> tableAlias = new HashMap<String, String>(); 
		String originalQuery;
		String translatedQuery;
		boolean isRangeQuery;
		boolean isJoinQuery;
		ArrayList<AttributeRange> rangeQueryAttributes = new ArrayList<AttributeRange>();
		ArrayList<String> returnAttributes = new ArrayList<String>();
		
		
		public String getOriginalQuery() {
			return originalQuery;
		}
		public void setOriginalQuery(String query) {
			this.originalQuery = query;
		}
		public String getTranslatedQuery() {
			return translatedQuery;
		}
		public void setTranslatedQuery(String query) {
			this.translatedQuery = query;
		}
		
		public boolean getIsRangeQuery() {
			return isRangeQuery;
		}
		public void setIsRangeQuery(boolean isRangeQuery) {
			this.isRangeQuery = isRangeQuery;
		}
		
		public boolean getIsJoinQuery() {
			return isJoinQuery;
		}
		
		public void setIsJoinQuery(boolean isJoinQuery) {
			this.isJoinQuery = isJoinQuery;
		}
		
		public void addRangeColumn(AttributeRange column) {
			this.rangeQueryAttributes.add(column);
		}
		public ArrayList<AttributeRange> getRangeColumn() {
			return this.rangeQueryAttributes;
		}
		
		
	}
	/*
	 */
	class AttributeRange{
		String table;
		String attribute;
		BigInteger lower;
		BigInteger upper;
		String symbol;
		// if one bounday value is passed, which means it is a single select
		public AttributeRange(String table, String attribute, BigInteger value, String comparision) {
			this.table = table;
			this.attribute = attribute;
			this.lower = this.upper = value;
			this.symbol = comparision; 
		}
		
		// for range query
		public AttributeRange(String table, String attribute, BigInteger lower, BigInteger upper, String comparision) {
			this.table = table;
			this.attribute = attribute;
			this.lower = lower;
			this.upper = upper;
			this.symbol = comparision;
		}
	}
}
