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
		try{
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {		
				DB_object object= new DB_object(); 
			}
			rs.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
}

/*
 * Define some database objects.
 */
class Employees{
	private int emp_no;
	private Date birth_date;
	private String first_name;
	private String last_name;
	private char gender;
	private Date hire_date;
	
}

class Salaries{
	private int emp_no;
	private int salary;
	private Date from_date;
	private Date to_date;
}

class Title{
	private int emp_no;
	private String title;
	private Date from_date;
	private Date to_date;
}

class Department{
	private int dept_no;
	private String dept_name;
}

class Dept_manager{
	private int emp_no;
	private String dept_no;
	private Date from_date;
	private Date to_date;
}

class Dept_emp{
	private int emp_no;
	private String dept_no;
	private Date from_date;
	private Date to_date;
}