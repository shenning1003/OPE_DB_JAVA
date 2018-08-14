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
 * ......
 */
public class KeyReader {
	public static KeyStructure readKey() {
		KeyStructure keys = new KeyStructure();
		try {
			File file = new File("Keys");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			TableKeys tableKeys = null;
			while((line = bufferedReader.readLine().trim().toUpperCase()) != null) {
				String[] words = line.split(" ");
				if(words[0].equals("TABLE") && words.length == 3) {
					if(tableKeys != null)
						keys.addTableKey(tableKeys);
					
					tableKeys = new TableKeys(Integer.parseInt(words[2]));
					tableKeys.setTableName(words[1]);
					
				}else if(words[0].equals("COLUMN") && words.length == 6) {
					ColumnKeys column = new ColumnKeys();
					column.setColumnName(words[1]);
					column.setDomain(Integer.parseInt(words[2]));
					column.setRange(Integer.parseInt(words[3]));
					column.setDataKey(Integer.parseInt(words[4]));
					column.setFakeKey(Integer.parseInt(words[5]));
					tableKeys.addColumn(column);
				}else {
					System.out.println("Warning");
					break;
				}
			}
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		return keys;
	}
	
}

class KeyStructure{
	ArrayList<TableKeys> tablesKey = new ArrayList<TableKeys>(); 
	public KeyStructure() {
		this.tablesKey = new ArrayList<TableKeys>();
	}
	public void addTableKey(TableKeys tkeys) {
		tablesKey.add(tkeys);
	}
	public ArrayList<TableKeys> getAllKeys(){
		return this.tablesKey;
	}
}

class TableKeys{
	private int fkNum;
	private String tableName;
	private ArrayList<ColumnKeys> columnsKey; 
	
	public TableKeys(int fkNum) {
		this.fkNum = fkNum;
		columnsKey = new ArrayList<ColumnKeys>();
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
	
	public ArrayList<ColumnKeys> getColumnsKey(){
		return this.columnsKey;
	}
	
	public void addColumn(ColumnKeys key) {
		this.columnsKey.add(key);
	}
}

class ColumnKeys{
	private String columnName;
	private int domain;
	private int range;
	private int dataKey;
	private int fakeKey;
	
	public String getColumnName() {
		return this.columnName;
	}
	public void setColumnName(String name) {
		this.columnName = name;
	}
	public int getDomain(){
		return this.domain;
	}
	public void setDomain(int domain) {
		this.domain = domain;
	}
	public int getRange() {
		return this.range;
	}
	public void setRange(int range) {
		this.range = range;
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