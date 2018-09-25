package OPE_DB;

import java.math.BigInteger;
import java.util.Date;

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
	
	public Employee(int emp_no, Date birth_date, String first_name, String last_name, char gender, Date hire_date) {
		this.emp_no = emp_no;
		this.birth_date = birth_date;
		this.first_name = first_name;
		this.last_name = last_name;
		this.gender = gender;
		this.hire_date = hire_date;
	}
	
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

class SalaryCipher{
	private BigInteger emp_no;
	private BigInteger salary;
	private BigInteger from_date;
	private BigInteger to_date;
	
	public SalaryCipher(BigInteger empno, BigInteger salary, BigInteger from_date, BigInteger to_date) {
		this.emp_no = empno;
		this.salary = salary;
		this.from_date = from_date;
		this.to_date = to_date;
	}
	public BigInteger getEmp_no() {
		return this.emp_no;
	}
	public BigInteger getSalary() {
		return this.salary;
	}
	public BigInteger getFrom_date() {
		return this.from_date;
	}
	public BigInteger getTo_date() {
		return this.to_date;
	}
}

class Salary{
	private int emp_no;
	private int salary;
	private Date from_date;
	private Date to_date;

	public Salary(int emp_no, int salary, Date fromDate, Date toDate){
		this.emp_no = emp_no;
		this.salary = salary;
		this.from_date = fromDate;
		this.to_date = toDate;
	}

	public int getEmp_no(){
		return this.emp_no;
	}
	public int getSalary(){
		return this.salary;
	}
	public Date getFromDate(){
		return this.from_date;
	}
	public Date getToDate(){
		return this.to_date;
	}
	
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