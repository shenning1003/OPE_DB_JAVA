package OPE_DB;

import java.io.IOException;

public class Run {
	
	public static void main(String args[]){
		KeyStructure keys = KeyReader.readKey();
		DB_connection db = new DB_connection();;
		OPE_DB opeDB  = new OPE_DB();
		
		opeDB.createOPE_DB();
		try {
			opeDB.InsertFakeTuple();
			for(int i = 0; i < 10; i ++){
				opeDB.randomDelete(500);
				opeDB.querySalary("SELECT * FROM ope_salary WHERE ");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
