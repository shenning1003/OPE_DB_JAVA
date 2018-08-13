package OPE_DB;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
public class DB_connection {
	static final String JDBC_DRIVE = "com.mysql.jdbc.Drive";
	static final String DB_URL = "jdbc:mysql://localhost/"; // DB name here
	
	static final String user = "";
	static final String password = "";
	
	private Connection conn;
	private Statement stmt;
	public DB_connection(){
		try {
			// register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			
			conn = DriverManager.getConnection(DB_URL, user, password);
			
			stmt = conn.createStatement();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ResultSet execQuery(String query) {
		ArrayList<DB_object> result = new ArrayList<DB_object>();
		try{
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				
				DB_object object= new DB_object(); 
				result.add(object);
			}
			rs.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
}

/*
 * Future changes here. Now this model suppose that only one attribute every query return 
 */
class DB_object{
	private BigInteger ID;
	public DB_object () {
		
	}
	public DB_object(BigInteger ID) {
		this.ID = ID;
	}
	
	public BigInteger getID() {
		return ID;
	}
	
	public void setID(BigInteger ID) {
		this.ID = ID;
	}
	
}
