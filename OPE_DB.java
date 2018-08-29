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
	
	private void EncryptSalaryTable(){
		DB_connection db = new DB_connection();
		ArrayList<Salary> salaries = new ArrayList<Salary>();
		try {
			PreparedStatement stm = db.getConnection().prepareStatement("SELECT * FROM SALARY");
			salaries = db.QuerySalary(stm);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(salaries.size() == 0)
			return;
		else {
			for (Salary s : salaries) {
				try {
					PreparedStatement stmt = this.OPE_conn.prepareStatement("");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	public void InsertFakeTuple() throws IOException {
		if (keyFile == null)
			return;
		if (keyFile.tablesKey.size() == 0)
			return;
		for (TableKeys tk : keyFile.tablesKey) {
			StringBuffer sb = new StringBuffer();
			for (int index =0; index < tk.get_fkNum(); index++) {
				sb.append("INSERT INTO " + tk.getTableName()+ " (");
				for (ColumnKeys ck : tk.getAllColumnsKey()) {
					sb.append(ck.getColumnName() + " ");
				}
				sb.append(") VALUES (");
				for (ColumnKeys ck : tk.getAllColumnsKey()) {
					BigInteger cipher = ope.OPE_encrypt(index, ck.getFakeKey(), ck.getDomain(), ck.getRange());
					sb.append(cipher + " ");
				}
				sb.append(")");
			}
		}
	}
}
