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

		if (args.length ==1 && args[0].equals("-i")) {
			ope_db.createOPE_DB();
			ope_db.EncryptDB();
			ope_db.InsertFakeTuple();

		}
		Scanner scan = new Scanner(System.in);
  		System.out.println("Please type the command: ...");
		String query = scan.nextLine();
		while(!query.toUpperCase().equals("EXIT")) {
			Query_object qObj = sqlParser.parseQuery("SELECT * FROM OPE_EMPLOYEE_DB.OPE_SALARY WHERE SALARY > 70000");
			//Query_object qObj = sqlParser.parseQuery("SELECT EMP_NO FROM OPE_EMPLOYEE_DB.OPE_SALARY WHERE SALARY >70000 AND FROM_DATE <= '1992-06-24'");
			long start = System.currentTimeMillis();
			ArrayList<ArrayList<BigInteger>>  queryResults = ope_db.queryOPE_DB(qObj);
			long end = System.currentTimeMillis();
			System.out.println("Query Took : " + ((end-start)/1000));
			start = System.currentTimeMillis();
			ArrayList<ArrayList<String>> decryptedValues = ope_db.decryptOPE_DB(queryResults, qObj);
			end = System.currentTimeMillis();
			System.out.println("Translate took : " + ((end-start)/1000));
			start = System.currentTimeMillis();
			ArrayList<ArrayList<BigInteger>> fakeTuples = cv.getAllExpectedFakeTuples(qObj, queryResults);
			ArrayList<ArrayList<BigInteger>> missingTuples = cv.compareDifferences(queryResults, fakeTuples);
			end = System.currentTimeMillis();
			System.out.println("Validation took: " + ((end-start)/1000));
			
			
			System.out.println(String.format("--------------------------------------------\n"
					+ "There are %d rows returned....Printing the first 10 rows", queryResults.size()));
			for (int i = 0 ; i <queryResults.size(); i ++) {
				for (BigInteger value : queryResults.get(i)) {
					System.out.print(value  + ",  ");
				}
				System.out.println();
			}
			
			
			System.out.println(String.format("--------------------------------------------\n"
					+ "Printing the first 10 decrypted results..."));
			for (int i = 0; i < 10; i ++) {
				for(String value : decryptedValues.get(i)) {
					System.out.print(value + ", ");
				}
				System.out.println();
			}
			
			System.out.println(String.format("--------------------------------------------\n"
					+ "There except %d fake tuples in query result...Printing the first 10 tuples", fakeTuples.size()));
			for (int i = 0 ; i <10; i ++) {
				for (BigInteger value : fakeTuples.get(i)) {
					System.out.print(value.toString() + ",  ");
				}
				System.out.println();
			}
			System.out.println(String.format("--------------------------------------------\n"
					+ "There are %d fake tuples missing from query result", missingTuples.size()));
			if(missingTuples.size() != 0) {
				System.out.println("------The missing tuples are:-------------- ");
				for(ArrayList<BigInteger> tuple : missingTuples) {
					for(BigInteger value : tuple) {
						System.out.print(value.toString() + ",  ");
					}
					System.out.println();
				}
			}
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
