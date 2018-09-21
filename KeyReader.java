package OPE_DB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/*
 * The keys are stored in such a structure.
 * TABLE XXX Num_of_fake_tuples
 * COLUMN XXX DOMAIN RANGE KEY1 KEY2 
 * (numbers like birthday, not starts from 0. So for encryption, 
 * we encrypt (VALUE - DOMAIN). Same changes in decryption )
 * ......
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
					
				}else if(words[0].equals("COLUMN") && words.length == 6) {
					ColumnKey column = new ColumnKey();
					column.setColumnName(words[1]);
					column.setDomainBit(Integer.parseInt(words[2]));
					column.setRangeBit(Integer.parseInt(words[3]));
					column.setDataKey(Integer.parseInt(words[4]));
					column.setFakeKey(Integer.parseInt(words[5]));
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
			if (tkey.getTableName().equals(tableName))
				return tkey;
		}
		return null;
	}
}

class TableKey{
	private int fkNum;
	private String tableName;
	private ArrayList<ColumnKey> columnsKey; 
	
	public TableKey(String tableName, int fkNum) {
		this.fkNum = fkNum;
		columnsKey = new ArrayList<ColumnKey>();
	}
	
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
			if(c.getColumnName().equals(columnName))
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
}