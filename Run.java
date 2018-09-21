package OPE_DB;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import com.jcraft.jsch.*;

public class Run {
	static String sshUser = "ningshen";
	static String sshPwd = "Sqwaxqq88331!bsu2";
	static String sshHost = "onyx.boisestate.edu";
	static int sshPort = 22;
	static int nRemotePort = 10122;
	static String remoteHost = "localhost";
	static int nLocalPort = 3367;
	
	
	public static void main(String args[]){
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
		
		String testSQL = "SELECT * FROM employees.employees limit 10";
		PreparedStatement stm;
		ArrayList<Employee> employees = new ArrayList<Employee>();
		try {
			stm = db.getConnection().prepareStatement(testSQL);	
			employees = db.QueryEmployee(stm);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		OPE_DB opeDB  = new OPE_DB();
//		
//		opeDB.createOPE_DB();
//		try {
//			opeDB.InsertFakeTuple();
//			for(int i = 0; i < 10; i ++){
//				opeDB.randomDelete(500);
//				opeDB.querySalary("SELECT * FROM ope_salary WHERE ");
//			}
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		session.disconnect();
	}
}
