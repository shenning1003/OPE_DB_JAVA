package OPE_DB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/*
 * The keys are stored in such a structure.
 * TABLE  table_name   num_of_fake_tuple
 * COLUMN column_name  domainBit,   rangeBit,  data_key,  fake_tuple_key,  fake_tuple_start_index,  fake_tuple_domainBit
 * COLUMN column_name  domainBit,   rangeBit,  data_key,  fake_tuple_key,  fake_tuple_start_index,  fake_tuple_domainBit
 */
public class KeyReader {
	public static KeyStructure readKey() {
		KeyStructure keys = new KeyStructure();
		try {
			File file = new File("./keys");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			TableKey tableKeys = null;
			while((line = bufferedReader.readLine()) != null) {
				String[] words = line.toUpperCase().split(" ");
				if(words[0].equals("TABLE") && words.length == 3) {
					if(tableKeys != null)
						keys.addTableKey(tableKeys);
					
					tableKeys = new TableKey(words[1], Integer.parseInt(words[2]));
					tableKeys.setTableName(words[1]);
					
				}else if(words[0].equals("COLUMN") && words.length == 8) {
					ColumnKey column = new ColumnKey();
					column.setColumnName(words[1]);
					column.setDomainBit(Integer.parseInt(words[2]));
					column.setRangeBit(Integer.parseInt(words[3]));
					column.setDataKey(Integer.parseInt(words[4]));
					column.setFakeKey(Integer.parseInt(words[5]));
					column.setFakeStartIndex(Integer.parseInt(words[6]));
					column.setFakeDomainBit(Integer.parseInt(words[7]));
					tableKeys.addColumn(column);
				}else {
					//System.out.println("Warning");
					continue;
				}
			}
			keys.addTableKey(tableKeys);
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		return keys;
	}
	
}

class KeyStructure{
	ArrayList<TableKey> tablesKey = new ArrayList<TableKey>(); 
	public KeyStructure() {
		this.tablesKey = new ArrayList<TableKey>();
	}
	public void addTableKey(TableKey tkeys) {
		tablesKey.add(tkeys);
	}
	public ArrayList<TableKey> getAllKeys(){
		return this.tablesKey;
	}
	public TableKey getSingleTableKeys(String tableName) {
		for(TableKey tkey : tablesKey) {
			if (tkey.getTableName().equals(tableName.toUpperCase()))
				return tkey;
		}
		return null;
	}
}

class TableKey{
	private int fkNum;
	private String tableName;
//	private int tableDomainBit;
	private ArrayList<ColumnKey> columnsKey; 
	
	public TableKey(String tableName, int fkNum) {
		this.fkNum = fkNum;
		columnsKey = new ArrayList<ColumnKey>();
	}
	
//	public void setTableDomainBit(int bit) {	
//		this.tableDomainBit = bit;
//	}
//	public int getTableDomainBit() {
//		return this.tableDomainBit;
//	}
	
	public String getTableName() {
		return this.tableName;
	}
	public void setTableName(String name) {
		this.tableName = name;
	}
	
	public int get_fkNum() {
		return this.fkNum;
	}
	
	public ArrayList<ColumnKey> getAllColumnsKey(){
		return this.columnsKey;
	}
	
	public ColumnKey getSingleColumn(String columnName) {
		for(ColumnKey c : columnsKey) {
			if(c.getColumnName().equals(columnName.toUpperCase()))
				return c;
		}
		return null;
	}
	
	public void addColumn(ColumnKey key) {
		this.columnsKey.add(key);
	}
}

class ColumnKey{
	private String columnName;
	private int domainBit;
	private int rangeBit;
	private int dataKey;
	private int fakeKey;
	private int fakeStartIndex;
	private int fakeRangeBit;
	
	public String getColumnName() {
		return this.columnName;
	}
	public void setColumnName(String name) {
		this.columnName = name;
	}
	public int getDomainBit(){
		return this.domainBit;
	}
	public void setDomainBit(int domain) {
		this.domainBit = domain;
	}
	public int getRangeBit() {
		return this.rangeBit;
	}
	public void setRangeBit(int range) {
		this.rangeBit = range;
	}
	public int getDataKey() {
		return this.dataKey;
	}
	public void setDataKey(int dataKey) {
		this.dataKey = dataKey;
	}
	public int getFakeKey() {
		return this.fakeKey;
	}
	public void setFakeKey(int fakeKey) {
		this.fakeKey = fakeKey;
	}
	public void setFakeStartIndex(int index) {
		this.fakeStartIndex = index;
	}
	public void setFakeDomainBit(int bit) {
		this.fakeRangeBit = bit;
	}
	public int getFakeStartIndex() {
		return this.fakeStartIndex;
	}
	public int getFakeDomainBit() {
		return this.fakeRangeBit;
	}
	
}