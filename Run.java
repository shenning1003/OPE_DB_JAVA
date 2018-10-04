package OPE_DB;

import java.io.IOException;
import java.math.BigInteger;
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
		OPE ope = new OPE();
		Query_parser sqlParser = new Query_parser(keys, ope);
		DB_connection db = new DB_connection();
		OPE_DB ope_db = new OPE_DB(db, keys);
		CompletenessValidator cv = new CompletenessValidator(keys);

		String testSQL = "SELECT * FROM employees.employees limit 10";
		PreparedStatement stm;
		//ArrayList<Employee> employees = new ArrayList<Employee>();
		if (args.length ==1 && args[0].equals("-i")) {
			// test SSL JDBC connection
			//stm = db.getConnection().prepareStatement(testSQL);
			//employees = db.QueryEmployee(stm);
			// test create OPE_db
			ope_db.createOPE_DB();
			ope_db.EncryptDB();
			ope_db.InsertFakeTuple();

		}
		Scanner scan = new Scanner(System.in);
  		System.out.println("Please type the command: ...");
		String query = scan.nextLine();
		while(!query.toUpperCase().equals("EXIT")) {
			Query_object qObj = sqlParser.parseQuery("SELECT * FROM OPE_EMPLOYEE_DB.OPE_SALARY WHERE SALARY > 70000");
			//Query_object qObj = sqlParser.parseQuery("SELECT * FROM OPE_EMPLOYEE_DB.OPE_SALARY WHERE SALARY > 70000 and FROM_DATE > '1992-06-24'");
			ArrayList<ArrayList<BigInteger>>  queryResults = ope_db.querySalary(qObj);
			ArrayList<Salary> salaries = ope_db.decryptSalary(queryResults, qObj);
			ArrayList<ArrayList<BigInteger>> missingTuples = cv.checkCompleteness(qObj, queryResults);
			if(missingTuples.size() == 0) {
				System.out.println("The returning results may be complete");
			}
			else {
				System.out.println("The returning results are not complete");
			}
			query = scan.nextLine();
		}
		
		
		session.disconnect();
	}
}
