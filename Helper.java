package OPE_DB;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Helper {
	public static BigInteger StringToNumber(String input){
		/*
		 * suppose the max string length is 15 chars
		 * starting from space, ascii code 32;
		 */
		BigInteger result = BigInteger.ZERO;
		for (int i =0; i < 15; i++){
			if(i >= input.length()){
				result = result.multiply(BigInteger.valueOf(100));
			}else{
				result = result.multiply(BigInteger.valueOf(100)).add(BigInteger.valueOf((char)(input.charAt(i)-31)));
			}
			//System.out.println("result: " + result);
		}
		return result;
	}

	public static String NumberToString(BigInteger input){
		StringBuffer sb = new StringBuffer();
		while (input.compareTo(BigInteger.valueOf(100)) == 1){
			if (input.mod(BigInteger.valueOf(100)) == BigInteger.ZERO) {
				input = input.divide(BigInteger.valueOf(100));
				continue;
			}
			sb.insert(0,(char)(input.mod(BigInteger.valueOf(100)).add(BigInteger.valueOf(31)).intValue()));
			input = input.divide(BigInteger.valueOf(100));
		}
		sb.insert(0,  (char) input.add(BigInteger.valueOf(31)).intValue());
		return sb.toString();
	}

	
	public static Date NumberToDate(long input){
		String pattern = "yyyyMMdd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		Date date = new Date();
		try {
			date = simpleDateFormat.parse(Long.toString(input));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	
	public static long DateToNumber(Date input){
		String pattern = "yyyyMMdd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		
		return Long.parseLong(simpleDateFormat.format(input));
	}
	
//	public static void main(String args[]) {
//		BigInteger test = StringToNumber("HELLO");
//		System.out.println(test);
//		String hello = NumberToString(test);
//		System.out.println(hello);
//		
//		Date date = NumberToDate(19990404);
//		System.out.println(DateToNumber(date));
//		
//		
//	}
}
