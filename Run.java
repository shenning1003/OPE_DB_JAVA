package OPE_DB;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import com.jcraft.jsch.*;

public class Run {
	static String sshUser = "ningshen";
	static String sshPwd = "Sqwaxqq88331!bsu2";
	static String sshHost = "onyx.boisestate.edu";
	static int sshPort = 22;
	static int nRemotePort = 10122;
	static String remoteHost = "localhost";
	static int nLocalPort = 3367;

	public static void main(String args[]) {
		Session session = null;
		final JSch jsch = new JSch();
		java.util.Properties configuration = new java.util.Properties();
		configuration.put("StrictHostKeyChecking", "no");

		try {
			session = jsch.getSession(sshUser, sshHost, 22);
			session.setPassword(sshPwd);
			session.setConfig(configuration);
			session.connect();
			session.setPortForwardingL(nLocalPort, remoteHost, nRemotePort);

		} catch (JSchException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		KeyStructure keys = KeyReader.readKey();
		DB_connection db = new DB_connection();
		OPE_DB ope_db = new OPE_DB(db, keys);

		String testSQL = "SELECT * FROM employees.employees limit 10";
		PreparedStatement stm;
		ArrayList<Employee> employees = new ArrayList<Employee>();
		try {
			// test SSL JDBC connection
			stm = db.getConnection().prepareStatement(testSQL);
			employees = db.QueryEmployee(stm);
			// test create OPE_db
			ope_db.createOPE_DB();
			ope_db.EncryptDB();
			ope_db.InsertFakeTuple();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Scanner scan = new Scanner(System.in);
		String query = scan.nextLine();
		while(!query.toUpperCase().equals("EXIT")) {
			ArrayList<SalaryCipher>  scList = ope_db.querySalary(query);
			ArrayList<Salary> salaries = ope_db.decryptSalary(scList); 
			
		}
		
		
		session.disconnect();
	}
}
