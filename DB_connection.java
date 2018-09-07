package OPE_DB;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;

/*
 * Connect with the original DB; 
 * Most likely only used when initialize the encrypted DB
 */
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
	
	public ArrayList<Employee> QueryEmployee(PreparedStatement stm) {
		ArrayList<Employee> employees = new ArrayList<Employee>();
		try{
			ResultSet rs = stm.executeQuery();
			while(rs.next()) {				
				int empId = rs.getInt("emp_no");
				Date bd = rs.getDate("birth_date");
				String firstName = rs.getString("first_name");
				String lastName = rs.getString("last_name");
				char gender = rs.getString("gender").charAt(0);
				Date hireDate = rs.getDate("hire_date");
				Employee emp = new Employee(empId, bd, firstName, lastName, gender, hireDate);
				employees.add(emp);
			}
			rs.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return employees;
	}
	
	public ArrayList<Salary> QuerySalary(PreparedStatement stm){
		ArrayList<Salary> salaries = new ArrayList<Salary>();
		try {
			ResultSet rs = stm.executeQuery();
			while(rs.next()) {
				int empId = rs.getInt("emp_no");
				int salary = rs.getInt("salary");
				Date from = rs.getDate("from_date");
				Date to = rs.getDate("to_date");
				Salary s = new Salary(empId, salary, from, to);
				salaries.add(s);
			}
			rs.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return salaries;
	}
	
	public Connection getConnection(){
		return this.conn;
	}
}