package OPE_DB;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;

import OPE_DB.Query_parser.Query_object;
public class DB_connection {
	static final String JDBC_DRIVE = "com.mysql.jdbc.Drive";
	static final String DB_URL = "jdbc:mysql://localhost/"; // DB name here
	
	static final String user = "";
	static final String password = "";
	
	Query_parser sqlParser;
	KeyStructure keys;
	OPE ope;
	private Connection conn;
	private Statement stmt;
	
	public DB_connection(KeyStructure key, OPE ope){
		this.sqlParser = new Query_parser();
		this.keys = key;
		this.ope = ope;
		try {
			// register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			
			conn = DriverManager.getConnection(DB_URL, user, password);
			
			stmt = conn.createStatement();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Employee> QueryEmployee(String query) {
		ArrayList<Employee> employees = new ArrayList<Employee>();
		Query_object sqlObject = sqlParser.parse(query);
		TableKeys tkeys = keys.getSingleTableKeys("EMPLOYEE");
		try{
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {		
				Employee emp = new Employee();
				long encrypted_emp_no = rs.getLong("emp_no");
				emp.setEmp_no(ope.OPE_decrypt(encrypted_emp_no, key, domain, range));
				long encrypted_Birth_data = rs.getLong("birth_data");
				long encrypted_FirstName = rs.getLong("first_name");
				employees.add(emp);
			}
			rs.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return employees;
	}
	
	public ArrayList<Salary> QuerySalary(String query){
		ArrayList<Salary> salaries = new ArrayList<Salary>();
		try {
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				
			}
			rs.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return salaries;
	}
	
	
}

/*
 * Define some database objects.
 */
class Employee{
	private int emp_no;
	private Date birth_date;
	private String first_name;
	private String last_name;
	private char gender;
	private Date hire_date;
	
	public void setEmp_no(int emp_no) {
		this.emp_no = emp_no;
	}
	public void setBirth_date(Date bdate) {
		this.birth_date = bdate;
	}
	public void setFirstName(String fname) {
		this.first_name = fname;
	}
	public void setLastName(String lname) {
		this.last_name=lname;
	}
	public void SetGender(char gender) {
		this.gender = gender;
	}
	public void SetHireDate(Date hdate) {
		this.hire_date = hdate;
	}
	
}

class Salary{
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