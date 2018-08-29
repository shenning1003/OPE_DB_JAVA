package OPE_DB;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/*
 * Should include the functions of: Enc/Dec database, query encrypted DB and verify completeness; inserting/removing 
 * fake tuples from the encrypted database
 */
public class OPE_DB {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Drive";
	static final String OPE_DB_URL = "";

	static final String OPE_user = "";
	static final String OPE_password = "";
	
	Query_parser sqlParser;
	OPE ope;
	KeyStructure keyFile;
	private Connection OPE_conn;
	private Statement OPE_stmt;
	public OPE_DB(DB_connection conn) {
		this.sqlParser = new Query_parser();
		keyFile = KeyReader.readKey();
		ope = new OPE();
		try {
			// register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			OPE_conn = DriverManager.getConnection(OPE_DB_URL, OPE_user, OPE_password);
			OPE_stmt = OPE_conn.createStatement();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Create the ope encrypted database by creating each table
	 */
	public void EncryptDB() {
		EncryptSalaryTable();
	}
	
	/*
	 * Return status code, if success return 1, else return 0;
	 */
	private int EncryptSalaryTable(){
		//first if we can find key file;
		TableKey salaryTableKey = keyFile.getSingleTableKeys("ope_salary");
		if(salaryTableKey == null)
			return 0;
		DB_connection db = new DB_connection();
		ArrayList<Salary> salaries = new ArrayList<Salary>();
		try {
			PreparedStatement stm = db.getConnection().prepareStatement("SELECT * FROM SALARY");
			salaries = db.QuerySalary(stm);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(salaries.size() == 0){
			return 0;
		}
		else {
			String sql = "INSERT INTO ope_salary VALUES (?, ?, ?, ?)";
			for (Salary s : salaries) {
				try {
					PreparedStatement insertStatement = this.OPE_conn.prepareStatement("");
					ColumnKey empIdKey = salaryTableKey.getSingleColumn("emp_no");
					BigInteger ope_emp_no = ope.OPE_encrypt(BigInteger.valueOf(s.getEmp_no()), empIdKey.getDataKey(), 
							empIdKey.getDomainBit(), empIdKey.getRangeBit());
					ColumnKey salaryKey = salaryTableKey.getSingleColumn("salary");
					BigInteger ope_salary = ope.OPE_encrypt(plaintext, key, domainBit, rangeBit)
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
			}
			return 1;
		}
		
	}
	
	
	public void InsertFakeTuple() throws IOException {
		if (keyFile == null)
			return;
		if (keyFile.tablesKey.size() == 0)
			return;
		for (TableKey tk : keyFile.tablesKey) {
			StringBuffer sb = new StringBuffer();
			for (int index =0; index < tk.get_fkNum(); index++) {
				sb.append("INSERT INTO " + tk.getTableName()+ " (");
				for (ColumnKey ck : tk.getAllColumnsKey()) {
					sb.append(ck.getColumnName() + " ");
				}
				sb.append(") VALUES (");
				for (ColumnKey ck : tk.getAllColumnsKey()) {
					BigInteger cipher = ope.OPE_encrypt(index, ck.getFakeKey(), ck.getDomainBit(), ck.getRangeBit());
					sb.append(cipher + " ");
				}
				sb.append(")");
			}
		}
	}
}
