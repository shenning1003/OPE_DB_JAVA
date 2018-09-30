package OPE_DB;

public class ExpressionTree {
	private Node root;

	public ExpressionTree() {
		root = null;
	}

	public Node getRoot() {
		return this.root;
	}

	public void postOrder(Node currentRoot) {
		if (currentRoot != null) {
			postOrder(currentRoot.leftNode);
			postOrder(currentRoot.rightNode);
			currentRoot.showNode();
		}
	}

	public void insert(String input) {
		Node newNode;
		NodeStack stack = new NodeStack(input.length());
		int whereIndex = input.indexOf("WHERE");
		String subString = input.substring(whereIndex + 5);
		String[] split = subString.split(" ");
		Conversion c = new Conversion(split);
		String postOrder = c.inToPost();
		// System.out.println(postOrder); // test here
		split = postOrder.split(" ");
		int index = 0;
		String symbol = split[0];
		while (index < split.length) {
			symbol = split[index];
			if (symbol.equals("<") || symbol.equals("<=") || symbol.equals("=") || symbol.equals(">")
					|| symbol.equals(">=") || symbol.equals("<>") || symbol.equals("AND") || symbol.equals("OR")) {
				Node right = stack.pop();
				Node left = stack.pop();
				newNode = new Node(symbol);
				newNode.leftNode = left;
				newNode.rightNode = right;
				stack.push(newNode);
			} else {
				newNode = new Node(symbol);
				stack.push(newNode);
			}
			index++;
		}
		this.root = stack.pop();
	}

	public boolean validate(int num, Node currentNode) {
		if (currentNode != null) {
			boolean leftAnswer = validate(num, currentNode.leftNode);
			boolean rightAnswer = validate(num, currentNode.rightNode);
			switch (currentNode.data) {
			case "<":
				if (num < Integer.parseInt(currentNode.rightNode.data)) {
					return true;
				} else {
					return false;
				}
			case "<=":
				if (num <= Integer.parseInt(currentNode.rightNode.data)) {
					return true;
				} else {
					return false;
				}
			case ">":
				if (num > Integer.parseInt(currentNode.rightNode.data)) {
					return true;
				} else {
					return false;
				}
			case ">=":
				if (num >= Integer.parseInt(currentNode.rightNode.data)) {
					return true;
				} else {
					return false;
				}
			case "=":
				if (num == Integer.parseInt(currentNode.rightNode.data)) {
					return true;
				} else {
					return false;
				}
			case "AND":
				return leftAnswer && rightAnswer;
			case "OR":
				return leftAnswer || rightAnswer;
			default:
				return false;
			}
		} else {
			// System.out.println("Error evaluate expression tree");
			return false;
		}

	}
}


class Conversion {
	private Stack s;
	private String input[];
	private String output = "";

	public Conversion(String str[]) {
		input = str;
		s = new Stack(str.length);
	}

	public String inToPost() {
		for (int i = 0; i < input.length; i++) {
			String token = input[i];
			switch (token) {
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

	private void gotOperator(String token, int prec1) {
		while (!s.isEmpty()) {
			String opTop = s.pop();
			if (opTop.equals("(")) {
				s.push(opTop);
				break;
			} else {
				int prec2;
				if (opTop.equals("AND") || opTop.equals("OR"))
					prec2 = 1;
				else
					prec2 = 2;
				if (prec2 < prec1) {
					s.push(opTop);
					break;
				} else {
					output = output + " " + opTop;
				}

			}
		}
		s.push(token);
	}

	private void gotParenthesis() {
		while (!s.isEmpty()) {
			String ch = s.pop();
			if (ch.equals("("))
				break;
			else
				output = output + " " + ch;
		}
	}
}

class Node {
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

class Stack {
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

class NodeStack {
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
