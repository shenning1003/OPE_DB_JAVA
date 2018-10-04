package OPE_DB;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Query_parser {
	
	
	static final char[] characters= {'<', '>', '='};
	static final List<String> comparator = Arrays.asList("<",">","<>","<=",">=", "=");
	static final List<String> joinKeywords = Arrays.asList("LEFT", "INNER", "RIGHT", "OUTER", "JOIN");
	KeyStructure dataBaseKeys;
	OPE ope;
	public Query_parser(KeyStructure keys, OPE ope) {
		this.dataBaseKeys = keys;
		this.ope = ope;
	}
	
	public Query_object parseQuery(String query) {
		Query_object qObj = new Query_object();
		// adjust space and formalize the query.
		query = query.toUpperCase();  
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
			else {
				sb.append(query.charAt(i));
			}
		}
		// reset query string, so we can split by space
		query = sb.toString();
		sb = new StringBuffer();  // re-init here to build translated query
		qObj.setOriginalQuery(query);
		qObj.setValidationQuery(query);
		String[] words = query.trim().split(" ");
		switch(words[0]) {
		case "UPDATE":
			break;
		case "INSERT":
			break;
		case "DELETE":
			break;
		case "SELECT":
			sb.append("SELECT");
			int fromIndex = Arrays.asList(words).indexOf("FROM");
			int whereIndex = Arrays.asList(words).indexOf("WHERE");
			if(fromIndex == -1)
				return null;
			// find all the returning attributes
			for (int i = 1; i < fromIndex; i ++) {
				sb.append(words[i] + " "); // building query here
				if(words[i].equals(","))
					continue;
				qObj.returnAttributes.add(words[i].replaceAll(",*$", "")); // "tableName.attribute"  format
			}
			
			// find all the tables and their alias from keyword " FROM "
			int current = fromIndex+1;
			sb.append("FROM "); 
			for (int i = fromIndex+1; i <= whereIndex; i++) {
				sb.append(words[i]+ " ");
				if (joinKeywords.contains(words[i])|| words[i].endsWith(",") || words[i].equals("WHERE")) {
					if (i - current == 1) {
						if (!words[current].contains(".")) {
							qObj.tableAlias.put(words[current], words[current]);
						}
						else {
							String text = words[current];
							String[] split2 = text.split("\\.");
							qObj.tableAlias.put(split2[1], split2[1]);
						}
					}
					else if(i - current == 2) {
						if(!words[current].contains(".")) {
							qObj.tableAlias.put(words[current+1], words[current]);
						}
						else {
							String[] split2 = words[current].split("\\.");
							qObj.tableAlias.put(words[current]+1, split2[1]);
						}
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
			// from "where" to iterate all the conditions
			for (int i= whereIndex; i < words.length; i++) {
				if (words[i].equals("AND") || words[i].equals("OR") || words[i].equals("NOT")) {
					sb.append(words[i] + " ");
					continue;
				}
				if(comparator.contains(words[i])) {
					
					String tableName="", columnName="";
					if(!words[i-1].contains(".")) {
						Map.Entry<String, String> entry = qObj.tableAlias.entrySet().iterator().next();
						tableName = entry.getValue();
						columnName = words[i-1];
					}
					else if(words[i].contains(".")){
						// if specify table name, the must multiple table involved in this query
						String[] split = words[i-1].split(".");
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
					BigInteger value = null;
					if (words[i+1].startsWith("\"") && words[i+1].endsWith("\"")) {
						String str = words[i+1].replaceAll("^\"|\"$", "");
						value = HelperFunctions.StringToNumber(str);
					}
					else if(words[i+1].matches("'([0-9]{4})-([0-9]{2})-([0-9]{2})'")){
						//String str = words[i+1].replaceAll("[\\-]", "");
						String pattern = "yyyy-MM-dd";
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
						Date date = new Date();
						try {
							date = simpleDateFormat.parse(words[i+1].replaceAll("\'", ""));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						value = HelperFunctions.DateToNumber(date);
					}
					else{
						value = BigInteger.valueOf(Long.parseLong(words[i+1]));
					}
					
					BigInteger cipherValue = null;
					if (words[i].equals("<") || words[i].equals("<=")) {
						cipherValue = ope.simple_OPE_encrypt(value, key, domainBit, rangeBit);
						AttributeRange ar = new AttributeRange(tableName, columnName, cipherValue, words[i]);
						qObj.addRangeColumn(ar);
						sb.append(words[i-1] + " < "  + cipherValue.toString() + " ");
					}
					else if (words[i].equals(">") || words[i].equals(">=")) {
						cipherValue = ope.simple_OPE_encrypt(value.add(BigInteger.ONE)
								, key, domainBit, rangeBit);
						AttributeRange ar = new AttributeRange(tableName, columnName, cipherValue, words[i]);
						qObj.addRangeColumn(ar);
						sb.append(words[i-1] + " > " + cipherValue.toString() + " ");
					}
					else if(words[i].equals("=")) {
						BigInteger upper = ope.simple_OPE_encrypt(value.add(BigInteger.ONE)
								, key, domainBit, rangeBit);
						BigInteger lower = ope.simple_OPE_encrypt(value, 
								key, domainBit, rangeBit);
						// a > low && a < high
						AttributeRange ar1 = new AttributeRange(tableName, columnName, lower, ">");
						AttributeRange ar2 = new AttributeRange(tableName, columnName, upper, "<");
						qObj.addRangeColumn(ar1);
						qObj.addRangeColumn(ar2);
						sb.append("(" + words[i-1] + " < " + upper + " AND " + words[i-1] + " > " + lower + ") " );
					}
					else{ // not equal
						BigInteger upper = ope.simple_OPE_encrypt(value.add(BigInteger.ONE)
								, key, domainBit, rangeBit);
						BigInteger lower = ope.simple_OPE_encrypt(value, 
								key, domainBit, rangeBit);
						AttributeRange ar1 = new AttributeRange(tableName, columnName, lower, ">");
						AttributeRange ar2 = new AttributeRange(tableName, columnName, upper, "<");
						qObj.addRangeColumn(ar1);
						qObj.addRangeColumn(ar2);
						sb.append("(" + words[i-1] + " < " + lower + " OR " + words[i-1] + " > " + upper +") ");
					}
				}
			}
			qObj.translatedQuery = sb.toString();
			
		}

		
		return qObj;
	}
	
	public void SetDatabaseKeys(KeyStructure keys) {
		this.dataBaseKeys = keys;
	}
	
	public static void main(String args[]) {
		OPE ope = new OPE();
		KeyStructure keys = KeyReader.readKey();
		Query_parser parser = new Query_parser(keys, ope);
		Query_object qo = parser.parseQuery("SELECT * FROM OPE_EMPLOYEE WHERE emp_no = 10 and first_name > \"ABC\"");
		System.out.println(qo.translatedQuery);
	}
	
}

class Query_object{
	// alias to real table name
	HashMap<String, String> tableAlias = new HashMap<String, String>(); 
	String originalQuery;
	String translatedQuery;
	String validationQuery;
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
	
	public void setValidationQuery(String query){
		this.validationQuery = query;
	}
	
	public String getValidationQuery(){
		return this.validationQuery;
	}
	
}


/*
 */
class AttributeRange{
	String table;
	String attribute;
	BigInteger boundary;
	String symbol;
	// if one bounday value is passed, which means it is a single select
	public AttributeRange(String table, String attribute, BigInteger value, String comparision) {
		this.table = table;
		this.attribute = attribute;
		this.boundary = value;
		this.symbol = comparision; 
	}
	
}
