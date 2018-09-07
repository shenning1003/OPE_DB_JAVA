package OPE_DB;

import java.sql.Date;

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
	private String emp_no;
	private String salary;
	private String from_date;
	private String to_date;
	
	public SalaryCipher(String empno, String salary, String from_date, String to_date) {
		this.emp_no = empno;
		this.salary = salary;
		this.from_date = from_date;
		this.to_date = to_date;
	}
	public String getEmp_no() {
		return this.emp_no;
	}
	public String getSalary() {
		return this.salary;
	}
	public String getFrom_date() {
		return this.from_date;
	}
	public String getTo_date() {
		return this.to_date;
	}
}

class Salary{
	private int emp_no;
	private int salary;
	private Date from_date;
	private Date to_date;

	public Salary(int emp_no, int salary, Date from, Date to){
		this.emp_no = emp_no;
		this.salary = salary;
		this.from_date = from;
		this.to_date = to;
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