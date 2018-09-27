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
	
	public ArrayList<ArrayList<BigInteger>> querySalary(Query_object qObj) {
		String translatedSql = qObj.getTranslatedQuery();
		ArrayList<ArrayList<BigInteger>> scList= new ArrayList<ArrayList<BigInteger>>();
		try {
			OPE_stmt = OPE_conn.createStatement();
			ResultSet rs = OPE_stmt.executeQuery(translatedSql);
			while(rs.next()) {
				BigInteger emp_no = new BigInteger(rs.getString("emp_no"));
				BigInteger salary = new BigInteger(rs.getString("salary"));
				BigInteger from_date = new BigInteger(rs.getString("from_date"));
				BigInteger to_date = new BigInteger(rs.getString("to_date"));
				ArrayList<BigInteger> encryptedSalary = new ArrayList<BigInteger>();
				encryptedSalary.add(emp_no);
				encryptedSalary.add(salary);
				encryptedSalary.add(from_date);
				encryptedSalary.add(to_date);
				scList.add(encryptedSalary);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		ArrayList<BigInteger> fakeValues = cv.getExceptedFakeTuples(qObj, qObj.returnAttributes.get(0));
//		ArrayList<BigInteger> targetSet = new ArrayList<BigInteger>();
//		if (qObj.returnAttributes.get(0).equals("EMP_NO")){
//			for(SalaryCipher sc : scList){
//				targetSet.add(sc.getEmp_no());
//			}
//		}
//		else if (qObj.returnAttributes.get(0).equals("SALARY")){
//			for (SalaryCipher sc : scList){
//				targetSet.add(sc.getSalary());
//			}
//		}
//		else if (qObj.returnAttributes.get(0).equals("FROM_DATE")){
//			for (SalaryCipher sc : scList){
//				targetSet.add(sc.getFrom_date());
//			}
//		}
//		else{
//			for (SalaryCipher sc :scList){
//				targetSet.add(sc.getTo_date());
//			}
//		}
		return scList;
		//return cv.checkCompleteness(fakeValues, targetSet);
	}
	
	public ArrayList<Salary> decryptSalary(ArrayList<ArrayList<BigInteger>> scList, Query_object qObj){
		ArrayList<Salary> salaries = new ArrayList<Salary>();
		TableKey salaryTableKey = keyFile.getSingleTableKeys("ope_salary");
		for(ArrayList<BigInteger> encSalary : scList) {
			Salary s = new Salary();
			for(int i = 0; i < qObj.returnAttributes.size(); i++) {
				String returnAttribute = qObj.returnAttributes.get(i);
				String attribute = returnAttribute;
				String table = "";
				if (returnAttribute.contains(".")) {
					table = returnAttribute.split(".")[0];
					attribute = returnAttribute.split(".")[1];
				}
				switch(attribute) {
					case "EMP_NO":
						ColumnKey empIdKey = salaryTableKey.getSingleColumn("emp_no");
						int emp_no = ope.OPE_decrypt(encSalary.get(i), empIdKey.getDataKey(),
								empIdKey.getDomainBit(), empIdKey.getRangeBit()).intValue();
						s.setEmp_no(emp_no);
						break;
					case "SALARY":
						ColumnKey salaryKey = salaryTableKey.getSingleColumn("salary");
						int salary = ope.OPE_decrypt(encSalary.get(i), salaryKey.getDataKey(),
								salaryKey.getDomainBit(), salaryKey.getRangeBit()).intValue();
						s.setSalary(salary);
						break;
					case "FROM_DATE":
						ColumnKey fromDateKey = salaryTableKey.getSingleColumn("from_date");
						Date fromDate = HelperFunctions.NumberToDate(ope.OPE_decrypt(encSalary.get(i),
								fromDateKey.getDataKey(), fromDateKey.getDomainBit(), fromDateKey.getRangeBit()).longValue());
						s.setFromDate(fromDate);
						break;
					case "TO_DATE":
						ColumnKey toDateKey = salaryTableKey.getSingleColumn("to_date");
						Date toDate = HelperFunctions.NumberToDate(ope.OPE_decrypt(encSalary.get(i),
								toDateKey.getDataKey(), toDateKey.getDomainBit(), toDateKey.getRangeBit()).longValue());
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
	
	/*
	 * create OPE version of database/tables
	 * returns 0 is all good, negative number for errors
	 */
	
	public int createOPE_DB(){
		int result = 0;
		String sql = "CREATE DATABASE IF NOT EXISTS OPE_EMPLOYEE_DB;";
		String empTable = "CREATE TABLE IF NOT EXISTS OPE_EMPLOYEE (emp_id VARCHAR(255) NOT NULL, "
				+ "birth_date VARCHAR(255), "
				+ "first_name VARCHAR(255), "
				+ "last_name VARCHAR(255), "
				+ "gender VARCHAR(255), "
				+ "hire_date VARCHAR(255));";
		String salaryTable = "CREATE TABLE IF NOT EXISTS OPE_SALARY (emp_id VARCHAR(255) NOT NULL, "
				+ "salary VARCHAR(255), "
				+ "from_date VARCHAR(255), "
				+ "to_date VARCHAR(255));";
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
			PreparedStatement stm = DB_conn.getConnection().prepareStatement("SELECT * FROM employees.salaries LIMIT 10");
			salaries = DB_conn.QuerySalary(stm);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (salaries.size() == 0) {
			return 0;
		} else {
			String sql = "INSERT INTO OPE_EMPLOYEE_DB.OPE_SALARY VALUES (?, ?, ?, ?)";
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
		String sql = "INSERT INTO OPE_EMPLOYEE_DB.OPE_SALARY VALUES (?,?,?,?)";
		TableKey salaryTableKey = keyFile.getSingleTableKeys("ope_salary");
		int num = salaryTableKey.get_fkNum();
		int fakeBit = salaryTableKey.getTableDomainBit();
		while (num > 0) {
			try {
				insertStatement = OPE_conn.prepareStatement(sql);
				BigInteger emp_no = ope.OPE_encrypt(num, salaryTableKey.getSingleColumn("emp_no").getFakeKey(),
						fakeBit, 
						salaryTableKey.getSingleColumn("emp_no").getRangeBit());
				BigInteger salary = ope.OPE_encrypt(num, salaryTableKey.getSingleColumn("salary").getFakeKey(),
						fakeBit, 
						salaryTableKey.getSingleColumn("salary").getRangeBit());
				BigInteger from_date = ope.OPE_encrypt(num, salaryTableKey.getSingleColumn("from_date").getFakeKey(),
						fakeBit, 
						salaryTableKey.getSingleColumn("from_date").getRangeBit());
				BigInteger to_date = ope.OPE_encrypt(num, salaryTableKey.getSingleColumn("to_date").getFakeKey(),
						fakeBit, 
						salaryTableKey.getSingleColumn("to_date").getRangeBit());
				insertStatement.setString(1, emp_no.toString());
				insertStatement.setString(2, salary.toString());
				insertStatement.setString(3, from_date.toString());
				insertStatement.setString(4, to_date.toString());
				insertStatement.executeUpdate();
				num --;
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
