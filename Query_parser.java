package OPE_DB;

import java.util.ArrayList;

public class Query_parser {
	
	KeyStructure dataBaseKeys;
	
	public Query_parser() {}
	
	public Query_object parse(String query) {
		query = query.toUpperCase();  // uppercase
		Query_object qObj = new Query_object();
		qObj.query = query;
		// has where condition
		int range_index =Math.max(query.indexOf("<"), query.indexOf(">")) ;
		if (range_index > 0) {
			qObj.isRangeQuery = true;
			String[] words = query.trim().split(" ");
			for (int i = 0; i < words.length-1; i++) {
				if (words[i+1].startsWith("<") || words[i+1].startsWith(">")) {
					qObj.addRangeColumn(words[i]);  // column name or table.column
				}
			}
		}
		
		return qObj;
	}
	
	public void SetDatabaseKeys(KeyStructure keys) {
		this.dataBaseKeys = keys;
	}
	
	class Query_object{
		String query;
		boolean isRangeQuery;
		boolean isJoinQuery;
		ArrayList<String> rangeQueryNames = new ArrayList<String>();
		
		
		public String getQuery() {
			return query;
		}
		public void setQuery(String query) {
			this.query = query;
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
		
		public void addRangeColumn(String columnName) {
			this.rangeQueryNames.add(columnName);
		}
		
		public ArrayList<String> getRangeColumn() {
			return this.rangeQueryNames;
		}
	}
}
