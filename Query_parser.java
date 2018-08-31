package OPE_DB;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Query_parser {
	
	KeyStructure dataBaseKeys;
	static final char[] characters= {'<', '>', '='};
	static final List<String> comparisons = Arrays.asList("<",">","<>","<=",">=", "=");
	public Query_parser() {}
	
	public Query_object parseQuery(String query) {
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
		Query_object qObj = new Query_object();
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
				if(words[i].equals(","))
					continue;
				qObj.returnAttributes.add(words[i].replaceAll(",*$", ""));
			}
			int whereIndex = Arrays.asList(words).indexOf("WHERE");
			if (whereIndex == -1) 
			{// query has no conditions, no need to translate query;
				qObj.translatedQuery = query;
				return qObj;
			}
			// else, from "where" to iterate all the conditions
			for (int i= whereIndex; i < words.length; i++) {
				if(comparisons.contains(words[i])) {
					
				}
			}
		}

		
		return qObj;
	}
	
	public void SetDatabaseKeys(KeyStructure keys) {
		this.dataBaseKeys = keys;
	}
	
	class Query_object{
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
		String attribute;
		BigInteger lower;
		BigInteger upper;
		String comparision;
		// if one bounday value is passed, which means it is a single select
		public AttributeRange(String attribute, BigInteger value, String comparision) {
			this.attribute = attribute;
			this.lower = this.upper = value;
			this.comparision = comparision; 
		}
		
		// for range query
		public AttributeRange(String attribute, BigInteger lower, BigInteger upper, String comparision) {
			this.attribute = attribute;
			this.lower = lower;
			this.upper = upper;
			this.comparision = comparision;
		}
	}
}
