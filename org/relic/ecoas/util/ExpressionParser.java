/*
 * Expression parser
 *
 * 2010 Boisy G. Pitre
 */
package org.relic.ecoas.util;

import org.relic.ecoas.util.*;

enum ExpressionType
{    
	Constant,
	Variable,
	Function
};

class ExpressionNode
{
    ExpressionNode left, right, parent;
	ExpressionType type;
	String name;
    int value;

	ExpressionNode()
	{
		left = null;
		right = null;
		parent = null;
	}
	
	void visit()
	{
	}
	
	void makeRightParentOf(ExpressionNode node)
	{
		this.left = node;
		this.parent = node.parent;
		if (node.parent != null) node.parent = this;
		if (this.parent != null) this.parent.right = this;
	}
	
	void makeLeftChildOf(ExpressionNode node)
	{
		this.left = node.left;
		if (node.left != null) node.left = this;
		this.parent = node;
		this.left.parent = this;
	}
	
	void makeRightChildOf(ExpressionNode node)
	{
		this.left = node.right;
		this.parent = node;
		if (node.right != null && node.right.parent != null) node.right.parent = this;
		node.right = this;
	}
}

class SymbolNode extends ExpressionNode
{
	SymbolTable table;
	
	SymbolNode(SymbolTable value)
	{
		table = value;
	}
	
	void visit()
	{
		Symbol s = table.symbolForName(name);
		if (s == null)
		{
			value = 0;
		}
		else
		{
			value = (s.value);
		}
	}
}

class EqualFunctionNode extends ExpressionNode
{
	EqualFunctionNode()
	{
		name = "=";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			if (left.value == right.value)
			{
				value = 1;
			}
			else
			{
				value = 0;
			}
		}
	}
}

class NotEqualFunctionNode extends ExpressionNode
{
	NotEqualFunctionNode()
	{
		name = "<>";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			if (left.value != right.value)
			{
				value = 1;
			}
			else
			{
				value = 0;
			}
		}
	}
}

class GreaterThanFunctionNode extends ExpressionNode
{
	GreaterThanFunctionNode()
	{
		name = ">";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			if (left.value > right.value)
			{
				value = 1;
			}
			else
			{
				value = 0;
			}
		}
	}
}

class GreaterThanOrEqualFunctionNode extends ExpressionNode
{
	GreaterThanOrEqualFunctionNode()
	{
		name = ">=";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			if (left.value >= right.value)
			{
				value = 1;
			}
			else
			{
				value = 0;
			}
		}
	}
}

class LessThanFunctionNode extends ExpressionNode
{
	LessThanFunctionNode()
	{
		name = "<";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			if (left.value < right.value)
			{
				value = 1;
			}
			else
			{
				value = 0;
			}
		}
	}
}

class LessThanOrEqualFunctionNode extends ExpressionNode
{
	LessThanOrEqualFunctionNode()
	{
		name = "<=";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			if (left.value <= right.value)
			{
				value = 1;
			}
			else
			{
				value = 0;
			}
		}
	}
}

class AdditionFunctionNode extends ExpressionNode
{
	AdditionFunctionNode()
	{
		name = "+";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			value = left.value + right.value;
		}
	}
}

class LogicalAndFunctionNode extends ExpressionNode
{
	LogicalAndFunctionNode()
	{
		name = "&";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			value = left.value & right.value;
		}
	}
}

class LogicalOrFunctionNode extends ExpressionNode
{
	LogicalOrFunctionNode()
	{
		name = "&";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			value = left.value | right.value;
		}
	}
}

class LogicalExclusiveOrFunctionNode extends ExpressionNode
{
	LogicalExclusiveOrFunctionNode()
	{
		name = "^";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			value = left.value ^ right.value;
		}
	}
}

class LogicalNotFunctionNode extends ExpressionNode
{
	LogicalNotFunctionNode()
	{
		name = "!";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null)
		{
			if (left.value == 0)
			{
				value = 1;
			}
			else
			{
				value = 0;
			}
		}
	}
}

class SubtractionFunctionNode extends ExpressionNode
{
	SubtractionFunctionNode()
	{
		name = "-";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			value = left.value - right.value;
		}
	}
}

class MultiplicationFunctionNode extends ExpressionNode
{
	MultiplicationFunctionNode()
	{
		name = "*";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			value = left.value * right.value;
		}
	}
}

class DivisionFunctionNode extends ExpressionNode
{
	DivisionFunctionNode()
	{
		name = "/";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			if (right.value == 0)
			{
				System.out.println("**** Error: division by zero attempted");
				return;
			}
			value = left.value / right.value;
		}
	}
}

class ModulusFunctionNode extends ExpressionNode
{
	ModulusFunctionNode()
	{
		name = "%";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null && right != null)
		{
			value = left.value % right.value;
		}
	}
}

class ComplementFunctionNode extends ExpressionNode
{
	ComplementFunctionNode()
	{
		name = "~";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null)
		{
			value = ~left.value;
		}
	}
}

class NegateFunctionNode extends ExpressionNode
{
	NegateFunctionNode()
	{
		name = "-";
		type = ExpressionType.Function;
	}
	
	void visit()
	{
		if (left != null)
		{
			value = -left.value;
		}
	}
}

public class ExpressionParser
{
	String expression;
	SymbolTable table;
	ExpressionNode rootNode;
	ReferenceTable references;
	
	public ExpressionParser(SymbolTable table)
	{
		this.table = table;
	}
	
	public ExpressionParser()
	{
	}
	
	ExpressionNode handleTerm(StringBuffer expression) throws ExpressionException
	{
		int val = 0;
		ExpressionNode n = null;
		
		if (expression.length() == 0)
		{
			// unbalanced expression
			throw new ExpressionException("unbalanced expression");
		}
		else if (expression.charAt(0) == ')')
		{
			return n;
		}
		// a leading ~ or ^ is complement
		else if (expression.charAt(0) == '~' || expression.charAt(0) == '^')
		{
			ComplementFunctionNode n1 = new ComplementFunctionNode();
			expression = expression.deleteCharAt(0);
			n1.left = handleTerm(expression);
			
			return n1;
		}
		// a leading plus sign is a positive
		// a leading minus sign is a negation
		else if (expression.charAt(0) == '-')
		{
			NegateFunctionNode n1 = new NegateFunctionNode();
			expression = expression.deleteCharAt(0);
			n1.left = handleTerm(expression);
			
			return n1;
		}
		// a leading plus sign is a positive
		else if (expression.charAt(0) == '+')
		{
			
			expression = expression.deleteCharAt(0);
			return handleTerm(expression);
		}

		// open parenthesis?
		while (expression.charAt(0) == '(')
		{
			expression = expression.deleteCharAt(0);
			
			ExpressionNode n1 = handleExpression(expression);
			
			if (expression.length() == 0 || expression.charAt(0) != ')')
			{
				throw new ExpressionException("too many open parentheses");
			}
			
			expression = expression.deleteCharAt(0);
					
			return n1;
		}

		if (expression.charAt(0) == '*')
		{
			// special meaning to assembler -- current PC offset
			Symbol s = table.symbolForName("*");
			expression = expression.deleteCharAt(0);
			
			if (s != null)
			{
				n = new SymbolNode(table);
				n.name = "*";
				return n;
			}
		}		
		else if (expression.charAt(0) == '.')
		{
			// special meaning to assembler -- current DATA offset
			// but could also be EDTASM .not.!
			
			int nextDot = expression.substring(1).indexOf('.');
			if (nextDot != -1 && expression.substring(1, nextDot + 1).compareToIgnoreCase("not") == 0)
			{
				LogicalNotFunctionNode n1 = new LogicalNotFunctionNode();
				expression = expression.delete(0, nextDot + 2);
				n1.left = handleTerm(expression);
				
				return n1;
			}
			else
			{
				// special meaning to assembler -- current DATA offset
				Symbol s = table.symbolForName(".");
				
				if (s != null)
				{
					n = new SymbolNode(table);
					n.name = ".";
					expression = expression.deleteCharAt(0);
					return n;
				}
			}
		}		
		else if (expression.charAt(0) == '\'')
		{
			// special meaning to assembler -- next character should be converted to ASCII
			expression = expression.deleteCharAt(0);
			if (expression.length() == 0)
			{
				throw new ExpressionException("missing character after '");
			}

			val = expression.charAt(0);
			expression = expression.deleteCharAt(0);
		}		
		else if (expression.charAt(0) == '%')
		{
			expression = expression.deleteCharAt(0);
			
			while (expression.length() > 0 &&
			      (expression.charAt(0) == '0' ||
				   expression.charAt(0) == '1')
			)
			{
				int v = 0;
				
				switch (expression.charAt(0))
				{
					case '0':
						v = 0;
						break;
					case '1':
						v = 1;
						break;
				}
					
				val = (val * 2) + v;

				expression = expression.deleteCharAt(0);
			}
		}
		// octal constant?
		else if (expression.charAt(0) == '@')
		{
			expression = expression.deleteCharAt(0);
			
			while (expression.length() > 0 &&
			      (expression.charAt(0) == '0' ||
				   expression.charAt(0) == '1' ||
				   expression.charAt(0) == '2' ||
				   expression.charAt(0) == '3' ||
				   expression.charAt(0) == '4' ||
				   expression.charAt(0) == '5' ||
				   expression.charAt(0) == '6' ||
				   expression.charAt(0) == '7')
			)
			{
				int v = 0;
				
				switch (expression.charAt(0))
				{
					case '0':
						v = 0;
						break;
					case '1':
						v = 1;
						break;
					case '2':
						v = 2;
						break;
					case '3':
						v = 3;
						break;
					case '4':
						v = 4;
						break;
					case '5':
						v = 5;
						break;
					case '6':
						v = 6;
						break;
					case '7':
						v = 7;
						break;
				}
					
				val = (val * 8) + v;

				expression = expression.deleteCharAt(0);
			}
		}
		// hexadecimal constant?
		else if (expression.charAt(0) == '$')
		{
			expression = expression.deleteCharAt(0);
			
			while (expression.length() > 0 &&
			      (expression.charAt(0) == '0' ||
				   expression.charAt(0) == '1' ||
				   expression.charAt(0) == '2' ||
				   expression.charAt(0) == '3' ||
				   expression.charAt(0) == '4' ||
				   expression.charAt(0) == '5' ||
				   expression.charAt(0) == '6' ||
				   expression.charAt(0) == '7' ||
				   expression.charAt(0) == '8' ||
				   expression.charAt(0) == '9' ||
				   expression.charAt(0) == 'a' ||
				   expression.charAt(0) == 'b' ||
				   expression.charAt(0) == 'c' ||
				   expression.charAt(0) == 'd' ||
				   expression.charAt(0) == 'e' ||
				   expression.charAt(0) == 'f' ||
				   expression.charAt(0) == 'A' ||
				   expression.charAt(0) == 'B' ||
				   expression.charAt(0) == 'C' ||
				   expression.charAt(0) == 'D' ||
				   expression.charAt(0) == 'E' ||
				   expression.charAt(0) == 'F')
			)
			{
				int v = 0;
				
				switch (expression.charAt(0))
				{
					case '0':
						v = 0;
						break;
					case '1':
						v = 1;
						break;
					case '2':
						v = 2;
						break;
					case '3':
						v = 3;
						break;
					case '4':
						v = 4;
						break;
					case '5':
						v = 5;
						break;
					case '6':
						v = 6;
						break;
					case '7':
						v = 7;
						break;
					case '8':
						v = 8;
						break;
					case '9':
						v = 9;
						break;
					case 'a':
					case 'A':
						v = 10;
						break;
					case 'b':
					case 'B':
						v = 11;
						break;
					case 'c':
					case 'C':
						v = 12;
						break;
					case 'd':
					case 'D':
						v = 13;
						break;
					case 'e':
					case 'E':
						v = 14;
						break;
					case 'f':
					case 'F':
						v = 15;
						break;
						
				}
					
				val = (val * 16) + v;

				expression = expression.deleteCharAt(0);
			}
		}
		// variable?
		else if (Character.isLetter(expression.charAt(0)) == true || expression.charAt(0) == '_')
		{
			// let's collect the variable name
			String tmp = "";
			
			while (expression.length() > 0 && (Character.isLetterOrDigit(expression.charAt(0)) == true
				|| expression.charAt(0) == '$'
				|| expression.charAt(0) == '_'
				|| expression.charAt(0) == '@'
				|| expression.charAt(0) == '.'
			))
			{
				tmp = tmp + expression.charAt(0);
				expression = expression.deleteCharAt(0);
			}		
			
			// check if this is a variable
			if (table == null)
			{
				throw new ExpressionException("no symbol table");
			}
			
			Symbol s = table.symbolForName(tmp);
			
			n = new SymbolNode(table);
			n.name = tmp;
			if (references != null)
			{
				references.add(new Reference(tmp, 2, "code"));
			}
			if (s == null)
			{
				throw new ExpressionException("undefined symbol");
			}

			return n;
		}
		// decimal constant?
		else
		{
			if (Character.isDigit(expression.charAt(0)) == false || expression.charAt(0) == '.')
			{
				throw new ExpressionException("unbalanced expression");
			}
			
			String number = "";
			while (expression.length() > 0 && Character.isDigit(expression.charAt(0)))
			{
				number = number + expression.charAt(0);
				expression.deleteCharAt(0);
			}
			
			n = new ExpressionNode();
			n.value = Integer.parseInt(number);
			n.type = ExpressionType.Constant;
		}
		
		if (n == null)
		{
			n = new ExpressionNode();
			n.value = val;
			n.type = ExpressionType.Constant;
		}
		
		return n;
	}
	
	ExpressionNode handleExpression(StringBuffer expression) throws ExpressionException
	{
		Boolean parenthesesUsed = false;
		ExpressionNode l = null;
		
		if (expression.length() > 0 && expression.charAt(0) == '(')
		{
			// concern ourselves with closing parentheses
			parenthesesUsed = true;
		}
	
		// pickup term part of expression
		l = handleTerm(expression);

		if (l == null) // || error.length() > 0 && error.equals("undefined symbol") == false)
		{
			return null;
		}

		do
		{
			ExpressionNode op = getop(expression);

			if (op != null)
			{
				if (expression.length() == 0)
				{
					throw new ExpressionException("unbalanced expression");
				}
				
				if (parenthesesUsed == false && l.name != null && higherPrecedence(op, l) == true)
				{
					// make the operator the right child of l
					op.makeRightChildOf(l);
				}
				else
				{
					// make the operator the right parent
					op.makeRightParentOf(l);
				}
				
				ExpressionNode r = handleTerm(expression);

				if (r == null)
				{
					return op;
				}
				
				op.right = r;
				r.parent = op;
				l = op;
			}
		} while (expression.length() > 0 && expression.charAt(0) !=')');

		// return top-most node
		while (l.parent != null)
		{
			l = l.parent;
		}
			
		return l;
	}
	
	ExpressionNode buildExpressionTree() throws ExpressionException
	{
		if (expression == null || expression.length() == 0)
		{
			throw new ExpressionException("empty expression");
		}
		
		StringBuffer expressionCopy = new StringBuffer(expression);
		
		ExpressionNode n = handleExpression(expressionCopy);

		if (expressionCopy.length() > 0 && expressionCopy.charAt(0) == ')')
		{
			// too many )'s
			throw new ExpressionException("too many closed parentheses");
		}
		
		return n;
	}
	
	public void setExpression(String value) throws ExpressionException
	{
		expression = value;
	
		rootNode = buildExpressionTree();
	}
	
	public void setSymbolTable(SymbolTable value)
	{
		table = value;
	}
	
	public int evaluate() throws ExpressionException
	{
		int result = 0;
		
		if (rootNode != null)
		{
			result = walkAndCompute(rootNode);
		}
		
		return result;
	}

	public int evaluate(String value, ReferenceTable references) throws ExpressionException
	{
		int result = 0;
		
		this.references = references;
		
		setExpression(value);
		result = evaluate();
		
		return result;
	}
	
	int walkAndCompute(ExpressionNode node)
	{
		int leftVal, rightVal;
		
		if (node.left != null)
		{
			leftVal = walkAndCompute(node.left);
		}
		if (node.right != null)
		{
			rightVal = walkAndCompute(node.right);
		}
		
		node.visit();
		
		if (node.type == ExpressionType.Function && node.left.type == ExpressionType.Constant && (node.right == null || node.right.type == ExpressionType.Constant))
		{
			// both left and right values are constants -- collapse into parent
			node.left = null;
			node.right = null;
			node.name = null;
			node.type = ExpressionType.Constant;
		}
		
		return node.value;
	}
	
	ExpressionNode getop(StringBuffer expression) throws ExpressionException
	{
		if (expression.length() == 0)
		{
			return null;
		}
		
		char op = expression.charAt(0);
		expression = expression.deleteCharAt(0);

		// handle EDTASM operators that begin and end with '.'
		if (op == '.')
		{
			int nextDot = expression.indexOf(".");
			if (nextDot == -1)
			{
				throw new ExpressionException("malformed operator");
			}
			
			String edtasmOperator = expression.substring(0, nextDot);
			
			// skip over dot
			expression = expression.delete(0, nextDot + 1);
			
			if (edtasmOperator.compareToIgnoreCase("and") == 0)
			{
				return new LogicalAndFunctionNode();
			}
			else
			if (edtasmOperator.compareToIgnoreCase("or") == 0)
			{
				return new LogicalOrFunctionNode();
			}
			else
			if (edtasmOperator.compareToIgnoreCase("xor") == 0)
			{
				return new LogicalExclusiveOrFunctionNode();
			}
			else
			if (edtasmOperator.compareToIgnoreCase("div") == 0)
			{
				return new DivisionFunctionNode();
			}
			else
			if (edtasmOperator.compareToIgnoreCase("mod") == 0)
			{
				return new ModulusFunctionNode();
			}
			else
			if (edtasmOperator.compareToIgnoreCase("equ") == 0)
			{
				return new EqualFunctionNode();
			}
			else
			if (edtasmOperator.compareToIgnoreCase("neq") == 0)
			{
				return new NotEqualFunctionNode();
			}
			else
			{
				throw new ExpressionException("malformed operator");
			}
		}
		
		switch (op)
		{
			case '&':
				return new LogicalAndFunctionNode();
			case '|':
			case '!':
				return new LogicalOrFunctionNode();
			case '^':
				return new LogicalOrFunctionNode();
			case '=':
				return new EqualFunctionNode();
			case '+':
				return new AdditionFunctionNode();
			case '-':
				return new SubtractionFunctionNode();
			case '*':
				return new MultiplicationFunctionNode();
			case '/':
				return new DivisionFunctionNode();
			case '%':
				return new ModulusFunctionNode();
			case '>':
				if (expression.charAt(0) == '=')
				{
					expression = expression.deleteCharAt(0);
					return new GreaterThanOrEqualFunctionNode();
				}
				else
				{
					return new GreaterThanFunctionNode();
				}
			case '<':
				if (expression.charAt(0) == '>')
				{
					expression = expression.deleteCharAt(0);
					return new NotEqualFunctionNode();
				}
				else
				{
					if (expression.charAt(0) == '=')
					{
						expression = expression.deleteCharAt(0);
						return new LessThanOrEqualFunctionNode();
					}
					else
					{
						return new LessThanFunctionNode();
					}
				}
				
			default:
				// we don't recognize... back up and return null
				expression.insert(0, op);

				if (op != ')')
				{
					throw new ExpressionException("unknown operator " + op);
				}				
				break;
		}
		
		return null;
	}
	
	int operatorPriority(ExpressionNode op)
	{
		if (op instanceof NegateFunctionNode
			||
			op instanceof ComplementFunctionNode
			)
		{
			return 3;
		}
		
		if (op instanceof MultiplicationFunctionNode
			||
			op instanceof DivisionFunctionNode
			||
			op instanceof ModulusFunctionNode
			)
		{
			return 2;
		}
		
		if (op instanceof AdditionFunctionNode
			||
			op instanceof SubtractionFunctionNode
			)
		{
			return 1;
		}
		
		if (op instanceof GreaterThanFunctionNode
			||
			op instanceof LessThanFunctionNode
			||
			op instanceof GreaterThanOrEqualFunctionNode
			||
			op instanceof LessThanOrEqualFunctionNode
			||
			op instanceof NotEqualFunctionNode
			||
			op instanceof LogicalAndFunctionNode
			||
			op instanceof LogicalOrFunctionNode
			||
			op instanceof LogicalExclusiveOrFunctionNode
			)
		{
			return 0;
		}
		
		// not an operator, return large number
		return 9999;
	}

	Boolean higherPrecedence(ExpressionNode op1, ExpressionNode op2)
	{
		int p1v = operatorPriority(op1);
		int p2v = operatorPriority(op2);
		
		return p1v > p2v;
	}
	
}
