package OPE_DB;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/*
 * Should include the functions of: Enc/Dec database, query encrypted DB and verify completeness; inserting/removing 
 * fake tuples from the encrypted database
 */
public class OPE_DB {
	static int localPort = 3367;
	static final String JDBC_DRIVER = "com.mysql.jdbc.Drive";
	static final String OPE_DB_URL = "jdbc:mysql://localhost:";

	static final String OPE_user = "msandbox";
	static final String OPE_password = "mysql";

	OPE ope;
	KeyStructure keyFile;
	CompletenessValidator cv;
	DB_connection DB_conn;
	private Connection OPE_conn;
	private Statement OPE_stmt;

	public OPE_DB(DB_connection db_conn, KeyStructure keyFile) {
		this.keyFile = keyFile;
		ope = new OPE();
		this.DB_conn = db_conn;
		cv = new CompletenessValidator(keyFile);
			
		try {
			// register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			OPE_conn = DriverManager.getConnection(OPE_DB_URL+localPort, OPE_user, OPE_password);
			OPE_stmt = OPE_conn.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create the ope encrypted database by creating each table
	 */
	public void EncryptDB() {
		try {
			EncryptSalaryTable();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<ArrayList<BigInteger>> queryOPE_DB(Query_object qObj) {
		String translatedSql = qObj.getTranslatedQuery();
		int columnNum = qObj.returnAttributes.size();
		ArrayList<ArrayList<BigInteger>> scList= new ArrayList<ArrayList<BigInteger>>();
		try {
			OPE_stmt = OPE_conn.createStatement();
			ResultSet rs = OPE_stmt.executeQuery(translatedSql);
			while(rs.next()) {
				ArrayList<BigInteger> encryptedTuple = new ArrayList<BigInteger>();
				for(int i = 1; i <=columnNum; i++) {
					encryptedTuple.add(new BigInteger(rs.getString(i)));
				}
				scList.add(encryptedTuple);
//				BigInteger emp_no = new BigInteger(rs.getString("emp_id"));
//				BigInteger salary = new BigInteger(rs.getString("salary"));
//				BigInteger from_date = new BigInteger(rs.getString("from_date"));
//				BigInteger to_date = new BigInteger(rs.getString("to_date"));
//				ArrayList<BigInteger> encryptedSalary = new ArrayList<BigInteger>();
//				encryptedSalary.add(emp_no);
//				encryptedSalary.add(salary);
//				encryptedSalary.add(from_date);
//				encryptedSalary.add(to_date);
//				scList.add(encryptedSalary);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return scList;
	}
	
	public ArrayList<Salary> decryptOPE_DB(ArrayList<ArrayList<BigInteger>> scList, Query_object qObj){
//		ArrayList<ArrayList<String>> decryptedList = new ArrayList<ArrayList<String>>();
//		for (int i = 0; i < qObj.returnAttributes.size(); i++){
//			String returnColumn = qObj.returnAttributes.get(i);
//			String tableName;
//			String columnName;
//			if(returnColumn.contains(".")){
//				String[] split = returnColumn.split(".");
//				tableName = split[0];
//				columnName = split[1];
//			}
//			else{
//				Map.Entry<String, String> entry = qObj.tableAlias.entrySet().iterator().next();
//				tableName = entry.getValue();
//				columnName = returnColumn;
//			}
//			TableKey tableKey = keyFile.getSingleTableKeys(tableName);
//			ColumnKey columnKey = tableKey.getSingleColumn(columnName);
//			int dataKey = columnKey.getDataKey();
//			int domainBit = columnKey.getDomainBit();
//			int rangeBit = columnKey.getRangeBit();
//			for(ArrayList<BigInteger> tuple : scList){
//				String plaintext = ope.OPE_decrypt(tuple.get(i), dataKey, domainBit, rangeBit).toString();
//			}
//			
//		}
		ArrayList<Salary> salaries = new ArrayList<Salary>();
		TableKey salaryTableKey = keyFile.getSingleTableKeys("ope_salary");
		for(ArrayList<BigInteger> encSalary : scList) {
			Salary s = new Salary();
			for(int i = 0; i < qObj.returnAttributes.size(); i++) {
				String returnAttribute = qObj.returnAttributes.get(i);
				String attribute = returnAttribute;
				String table = "";
				if (returnAttribute.contains("\\.")) {
					table = returnAttribute.split("\\.")[0];
					attribute = returnAttribute.split("\\.")[1];
				}
				switch(attribute) {
					case "EMP_NO":
						int emp_no = decryptEmpId(salaryTableKey, encSalary.get(i));
						s.setEmp_no(emp_no);
						break;
					case "SALARY":
						int salary = decryptSalary(salaryTableKey, encSalary.get(i));
						s.setSalary(salary);
						break;
					case "FROM_DATE":
						Date fromDate = decryptFromDate(salaryTableKey, encSalary.get(i));
						s.setFromDate(fromDate);
						break;
					case "TO_DATE":
						Date toDate = decryptToDate(salaryTableKey, encSalary.get(i));
						s.setToDate(toDate);
						break;
					default:
						break;
				}					
			}
			
			salaries.add(s);
		}
		return salaries;   
	}
	
	private int decryptEmpId(TableKey salaryTableKey, BigInteger cipher) {
		ColumnKey empIdKey = salaryTableKey.getSingleColumn("emp_no");
		int emp_no = ope.OPE_decrypt(cipher, empIdKey.getDataKey(),
				empIdKey.getDomainBit(), empIdKey.getRangeBit()).intValue();
		return emp_no;
	}
	
	private int decryptSalary(TableKey salaryTableKey, BigInteger cipher) {
		ColumnKey salaryKey = salaryTableKey.getSingleColumn("salary");
		int salary = ope.OPE_decrypt(cipher, salaryKey.getDataKey(),
				salaryKey.getDomainBit(), salaryKey.getRangeBit()).intValue();
		return salary;
	}
	
	private Date decryptFromDate(TableKey salaryTableKey, BigInteger cipher) {
		ColumnKey fromDateKey = salaryTableKey.getSingleColumn("from_date");
		Date fromDate = HelperFunctions.NumberToDate(ope.OPE_decrypt(cipher,
				fromDateKey.getDataKey(), fromDateKey.getDomainBit(), fromDateKey.getRangeBit()).longValue());
		return fromDate;
	}
	private Date decryptToDate(TableKey salaryTableKey, BigInteger cipher) {
		ColumnKey toDateKey = salaryTableKey.getSingleColumn("to_date");
		Date toDate = HelperFunctions.NumberToDate(ope.OPE_decrypt(cipher,
				toDateKey.getDataKey(), toDateKey.getDomainBit(), toDateKey.getRangeBit()).longValue());
		return toDate;
	}
	
	
	/*
	 * create OPE version of database/tables
	 * returns 0 is all good, negative number for errors
	 */
	
	public int createOPE_DB(){
		int result = 0;
		String sql = "CREATE DATABASE IF NOT EXISTS OPE_EMPLOYEE_DB;";
		String empTable = "CREATE TABLE IF NOT EXISTS OPE_EMPLOYEE (emp_no bigint NOT NULL, "
				+ "birth_date bigint, "
				+ "first_name bigint, "
				+ "last_name bigint, "
				+ "gender bigint, "
				+ "hire_date bigint,"
				+ "is_real boolean default 1);";
		String salaryTable = "CREATE TABLE IF NOT EXISTS OPE_SALARY (emp_no bigint NOT NULL, "
				+ "salary bigint, "
				+ "from_date bigint, "
				+ "to_date bigint,"
				+ "is_real boolean default 1);";
		try {
			Statement stmt = OPE_conn.createStatement();
			result += stmt.executeUpdate(sql);
			stmt.execute("USE OPE_EMPLOYEE_DB;");
			result += stmt.executeUpdate(empTable);
			//stmt.executeUpdate(salaryTable);
			result += stmt.executeUpdate(salaryTable);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
	
	public void randomDelete(int number){
		String deletion = "DELETE FROM ope_salary ORDER BY RAND() LIMIT ?";
		try {
			PreparedStatement stmt = OPE_conn.prepareStatement(deletion);
			stmt.setInt(1, number);
			stmt.executeUpdate();
			OPE_conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private int EncryptEmployeeTable() throws SQLException{
		PreparedStatement insertStatement = null;
		TableKEy 
	}

	/*
	 * Return status code, if success return 1, else return 0;
	 */
	private int EncryptSalaryTable() throws SQLException {
		PreparedStatement insertStatement = null;
		// first if we can find key file;
		TableKey salaryTableKey = keyFile.getSingleTableKeys("ope_salary");
		if (salaryTableKey == null)
			return 0;
		ArrayList<Salary> salaries = new ArrayList<Salary>();
		try {
			PreparedStatement stm = DB_conn.getConnection().prepareStatement("SELECT * FROM employees.salaries LIMIT 2000");
			salaries = DB_conn.QuerySalary(stm);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (salaries.size() == 0) {
			return 0;
		} else {
			String sql = "INSERT INTO OPE_EMPLOYEE_DB.OPE_SALARY VALUES (?, ?, ?, ?, ?)";
			try {
				insertStatement = this.OPE_conn.prepareStatement(sql);
				for (Salary s : salaries) {
					// emp_no column
					ColumnKey empIdKey = salaryTableKey.getSingleColumn("emp_no");
					BigInteger ope_emp_no = ope.OPE_encrypt(BigInteger.valueOf(s.getEmp_no()), empIdKey.getDataKey(),
							empIdKey.getDomainBit(), empIdKey.getRangeBit());
					// salary column
					ColumnKey salaryKey = salaryTableKey.getSingleColumn("salary");
					BigInteger ope_salary = ope.OPE_encrypt(BigInteger.valueOf(s.getSalary()), salaryKey.getDataKey(),
							salaryKey.getDomainBit(), salaryKey.getRangeBit());
					// from_date column
					ColumnKey fromDateKey = salaryTableKey.getSingleColumn("from_date");
					BigInteger ope_fromDate = ope.OPE_encrypt(HelperFunctions.DateToNumber(s.getFromDate()),
							fromDateKey.getDataKey(), fromDateKey.getDomainBit(), fromDateKey.getRangeBit());
					// to_date column
					ColumnKey toDateKey = salaryTableKey.getSingleColumn("to_date");
					BigInteger ope_toDate = ope.OPE_encrypt(HelperFunctions.DateToNumber(s.getToDate()),
							toDateKey.getDataKey(), toDateKey.getDomainBit(), toDateKey.getRangeBit());
					// create query
					
					insertStatement.setString(1, ope_emp_no.toString());
					insertStatement.setString(2, ope_salary.toString());
					insertStatement.setString(3, ope_fromDate.toString());
					insertStatement.setString(4, ope_toDate.toString());
					insertStatement.setBoolean(5, true);
					insertStatement.executeUpdate();
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (OPE_conn!= null){					
					try {
						System.err.println("Transaction is being rolled back");
						OPE_conn.rollback();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}finally {
				  if(insertStatement != null) {
					  insertStatement.close();
				  }
				  OPE_conn.setAutoCommit(true);
			}
			return 1;
		}

	}

	public void InsertFakeTuple(){
		if (keyFile == null)
			return;
		if (keyFile.tablesKey.size() == 0)
			return;
		try {
			InsertSalaryTable();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	private void InsertSalaryTable() throws SQLException {
		PreparedStatement insertStatement = null;
		String sql = "INSERT INTO OPE_EMPLOYEE_DB.OPE_SALARY VALUES (?,?,?,?,?)";
		TableKey salaryTableKey = keyFile.getSingleTableKeys("ope_salary");
		int num = salaryTableKey.get_fkNum();
		//int fakeBit = salaryTableKey.getTableDomainBit();
		int emp_No_start = salaryTableKey.getSingleColumn("emp_no").getFakeStartIndex();
		int salary_start = salaryTableKey.getSingleColumn("salary").getFakeStartIndex();
		int from_start = salaryTableKey.getSingleColumn("from_date").getFakeStartIndex();
		int to_start = salaryTableKey.getSingleColumn("to_date").getFakeStartIndex();
		
		int emp_fakeKey = salaryTableKey.getSingleColumn("emp_no").getFakeKey();
		int salary_fakeKey = salaryTableKey.getSingleColumn("salary").getFakeKey();
		int from_fakeKey = salaryTableKey.getSingleColumn("from_date").getFakeKey();
		int to_fakeKey = salaryTableKey.getSingleColumn("to_date").getFakeKey();
		
		int emp_domainBit = salaryTableKey.getSingleColumn("emp_no").getFakeDomainBit();
		int salary_domainBit = salaryTableKey.getSingleColumn("salary").getFakeDomainBit();
		int from_domainBit = salaryTableKey.getSingleColumn("from_date").getFakeDomainBit();
		int to_domainBit = salaryTableKey.getSingleColumn("to_date").getFakeDomainBit();
		
		int emp_rangeBit = salaryTableKey.getSingleColumn("emp_no").getRangeBit();
		int salary_rangeBit = salaryTableKey.getSingleColumn("salary").getRangeBit();
		int from_rangeBit = salaryTableKey.getSingleColumn("from_date").getRangeBit();
		int to_rangeBit = salaryTableKey.getSingleColumn("to_date").getRangeBit();
		for (int i = 1; i <= num; i ++) {
			try {
				insertStatement = OPE_conn.prepareStatement(sql);
				
				BigInteger emp_no = ope.simple_OPE_encrypt(BigInteger.valueOf(emp_No_start + i), emp_fakeKey, emp_domainBit, emp_rangeBit);
				BigInteger salary = ope.simple_OPE_encrypt(BigInteger.valueOf(salary_start + i), salary_fakeKey, salary_domainBit, salary_rangeBit);
				BigInteger from_date = ope.simple_OPE_encrypt(BigInteger.valueOf(from_start + i), from_fakeKey, from_domainBit, from_rangeBit);
				BigInteger to_date = ope.simple_OPE_encrypt(BigInteger.valueOf(to_start + i), to_fakeKey, to_domainBit, to_rangeBit);
				insertStatement.setString(1, emp_no.toString());
				insertStatement.setString(2, salary.toString());
				insertStatement.setString(3, from_date.toString());
				insertStatement.setString(4, to_date.toString());
				insertStatement.setBoolean(5, false);
				insertStatement.executeUpdate();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				  if(insertStatement != null) {
					  insertStatement.close();
				  }
				  OPE_conn.setAutoCommit(true);
			}


			
			
		}
		
	}
}
