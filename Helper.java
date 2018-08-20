package OPE_DB;

import java.util.Date;

public class Helper {
	public static long StringToNumber(String input){
		StringBuffer sb = new StringBuffer();
		for(char c : input.toCharArray()){
			if (c < 'A'){
				sb.append(c);// not letter, some characters
				continue;
			}
			//otherwise english letters	
			int diff = c - 'A';
			if(diff < 10)
				sb.append("0" + diff);
			else
				sb.append(diff);
		}
		return Long.parseLong(sb.toString());
	}
	
	public static String NumberToString(long input){
		StringBuffer sb = new StringBuffer();
		String inputString = String.valueOf(input);
		// TO DO
		return sb.toString();
	}
	
	public static Date NumberToDate(){
		
	}
	
	
	public static long DateToNumber(){
		
	}
}
