package OPE_DB;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class OPE_DB {

	KeyStructure keyFile;
	OPE ope;
	DB_connection db;
	public OPE_DB() {
		keyFile = KeyReader.readKey();
		ope = new OPE();
		db = new DB_connection(keyFile, ope);
	}
	
	public void EncryptDB() { //
		
	}
	
	private void EncryptSalaryTable(){
		ArrayList<Salary> salaries = new ArrayList<Salary>();
		try {
			PreparedStatement stm = db.getConnection().prepareStatement("SELECT * FROM SALARY");
			salaries = db.QuerySalary(stm);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public void InsertFakeTuple() throws IOException {
		if (keyFile == null)
			return;
		if (keyFile.tablesKey.size() == 0)
			return;
		for (TableKeys tk : keyFile.tablesKey) {
			StringBuffer sb = new StringBuffer();
			for (int index =0; index < tk.get_fkNum(); index++) {
				sb.append("INSERT INTO " + tk.getTableName()+ " (");
				for (ColumnKeys ck : tk.getAllColumnsKey()) {
					sb.append(ck.getColumnName() + " ");
				}
				sb.append(") VALUES (");
				for (ColumnKeys ck : tk.getAllColumnsKey()) {
					BigInteger cipher = ope.OPE_encrypt(index, ck.getFakeKey(), ck.getDomain(), ck.getRange());
					sb.append(cipher + " ");
				}
				sb.append(")");
			}
		}
	}
}
