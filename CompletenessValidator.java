package OPE_DB;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class CompletenessValidator {
	OPE ope;
	KeyStructure keys;
	BigInteger expectedTulpe;
	public CompletenessValidator(KeyStructure keys) {
		expectedTulpe = BigInteger.ZERO;
		ope = new OPE();
		this.keys = keys;
	}
	
	public ArrayList<BigInteger> checkCompleteness(Query_object qObj) {
		ArrayList<BigInteger> result = new ArrayList<BigInteger>();
		for( AttributeRange ar : qObj.getRangeColumn()){
			String tableName = ar.table;
			String attributeName = ar.attribute;
			int numOfFake = keys.getSingleTableKeys(tableName).get_fkNum();
			int key = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getFakeKey();
			int domainBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getDomainBit();
			int rangeBit = keys.getSingleTableKeys(tableName).getSingleColumn(attributeName).getRangeBit();
			if (ar.lower.equals(ar.upper)){
				getCorrectIndex(ar.lower, key, domainBit, rangeBit, ar.symbol);
			}
			else{
				
			}
			
 		}
		return result;
	}
	
	private BigInteger getCorrectIndex(BigInteger cipher, int key, int domainBit, int rangeBit, String comparator) {
		//BigInteger result = BigInteger.ZERO;
		BigInteger decryptedValue = ope.simple_OPE_decrypt(cipher, key, domainBit, rangeBit);
		BigInteger reEncryptedValue = ope.simple_OPE_encrypt(decryptedValue, key, domainBit, rangeBit);
		if (reEncryptedValue.compareTo(cipher) == 1) {
			if(comparator.equals(">")|| comparator.equals(">=")) {
				return decryptedValue;
			}
			else if(comparator.equals("<")|| comparator.equals("<=")) {
				return decryptedValue.add(BigInteger.ONE);
			}
		}
		else if(reEncryptedValue.compareTo(cipher) == -1) {
			if (comparator.equals(">")|| comparator.equals(">=")) {
				return decryptedValue.subtract(BigInteger.ONE);
			}
			else if (comparator.equals("<")|| comparator.equals("<=")) {
				return decryptedValue;
			}
		}
		return decryptedValue;
	}
	
	public static void main(String args[]) {
		String input = "SELECT * FROM TABLE WHERE A > 20 OR ( C < 10 AND B >= 6 )";
		String test2 = "A > 20 OR ( C < 10 AND B >= 6 )";
		Conversion c = new Conversion(test2.split(" "));
		System.out.println(c.inToPost());
		Tree t = new Tree();
		t.insert(input);
		t.postOrder(t.getRoot());
	}

}

class Conversion
{
    private Stack s;
    private String input[];
    private String output = "";
 
    public Conversion(String str[])
    {
        input = str;
        s = new Stack(str.length);
    }
 
    public String inToPost()
    {
        for (int i = 0; i < input.length; i++)
        {
            String token = input[i];
            switch (token)
            {
                case "AND":
                case "OR":
                    gotOperator(token, 1);
                    break;
                case "<":
                case "<=":
                case ">":
                case ">=":
                case "=":
                case "<>":
                	gotOperator(token, 2);
                	break;
                case "(":
                    s.push(token);
                    break;
                case ")":
                    gotParenthesis();
                    break;
                default:
                    output = output + " " + token;
            }
        }
        while (!s.isEmpty())
            output = output + " " + s.pop();
        return output;
    }
 
    private void gotOperator(String token, int prec1)
    {
        while (!s.isEmpty())
        {
            String opTop = s.pop();
            if (opTop.equals("("))
            {
                s.push(opTop);
                break;
            } else
            {
                int prec2;
                if (opTop.equals("AND") || opTop.equals("OR"))
                    prec2 = 1;
                else
                    prec2 = 2;
                if (prec2 < prec1)
                {
                    s.push(opTop);
                    break;
                } else {
                    output = output + " " + opTop;
                }

            }
        }
        s.push(token);
    }
 
    private void gotParenthesis()
    {
        while (!s.isEmpty())
        {
            String ch = s.pop();
            if (ch.equals("("))
                break;
            else
                output = output + " " + ch;
        }
    }
}

class Node
{
	public String data;
	public Node leftNode;
	public Node rightNode;
	
	public Node(String data) {
		this.data = data;
	}
	
	public void showNode() {
		System.out.print(this.data);
	}
}

class Stack
{
	private String[] s;
	private int top, max;
	
	public Stack(int max) {
		this.max = max;
		this.s = new String[max];
		this.top = 0;
	}
	
	public void push(String data) {
		s[++top] = data;
	}
	
	public String pop() {
		return s[top--];
	}
	
	public boolean isEmpty() {
		return top == 0;
	}
}

class NodeStack{
	private Node[] s;
	private int top, max;
	
	public NodeStack(int max) {
		this.max = max;
		this.s = new Node[max];
		this.top = 0;
	}
	
	public void push(Node node) {
		s[++top] = node;
	}
	
	public Node pop() {
		return s[top--];
	}
	
	public boolean isEmpty() {
		return top == 0;
	}
}

class Tree
{
	private Node root;
	
	public Tree() {
		root = null;
	}
	
	public Node getRoot(){
		return this.root;
	}
	
	public void postOrder(Node currentRoot) {
		if(currentRoot != null) {
			postOrder(currentRoot.leftNode);
			postOrder(currentRoot.rightNode);
			currentRoot.showNode();
		}
	}
	
	public void insert(String input) {
		Node newNode;
		NodeStack stack = new NodeStack(input.length());
		int whereIndex = input.indexOf("WHERE");
		String subString = input.substring(whereIndex+5);
		String[] split = subString.split(" ");
		Conversion c = new Conversion(split);
		String postOrder = c.inToPost();
		//System.out.println(postOrder); // test here
		split = postOrder.split(" ");
		int index = 0;
		String symbol = split[0];
		while (index < split.length) {
			symbol = split[index];
			if(symbol.equals("<")||symbol.equals("<=")||symbol.equals("=")||symbol.equals(">")||symbol.equals(">=")
					|| symbol.equals("<>") || symbol.equals("AND") || symbol.equals("OR")){
				Node right = stack.pop();
				Node left = stack.pop();
				newNode = new Node(symbol);
				newNode.leftNode = left;
				newNode.rightNode = right;
				stack.push(newNode);
			}
			else{
				newNode = new Node(symbol);
				stack.push(newNode);
			}
			index++;
		}
		this.root = stack.pop();
	}
}
