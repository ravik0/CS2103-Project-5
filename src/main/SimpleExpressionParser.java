package main;

import java.util.ArrayList;

/**
 * Starter code to implement an ExpressionParser. Your code should parse a context-free grammar
 * for mathematical expressions for addition, multiplication, and parentheses over single-letter
 * and integer operands. Suggested (though not required) grammar:
 * E := A | X
 * A := A+M | M
 * M := M*M | X
 * X := (E) | L
 * L := [0-9]+ | [a-z]
 */
public class SimpleExpressionParser implements ExpressionParser {
	/**
	 * Attempts to create an expression tree -- flattened as much as possible -- from the specified String.
         * Throws a ExpressionParseException if the specified string cannot be parsed.
	 * @param str the string to parse into an expression tree
	 * @param withJavaFXControls you can just ignore this variable for R1
	 * @return the Expression object representing the parsed expression tree
	 */
	public Expression parse (String str, boolean withJavaFXControls) throws ExpressionParseException {
		// Remove spaces -- this simplifies the parsing logic
		str = str.replaceAll(" ", "");
		Expression expression = parseExpression(str);
		if (expression == null) {
			// If we couldn't parse the string, then raise an error
			throw new ExpressionParseException("Cannot parse expression: " + str);
		}
		// Flatten the expression before returning
		expression.flatten();
		return expression;
	}
	
	/**
	 * Parses a possible mathematical expression into a valid expression tree
	 * @param str the string to parse
	 * @return the head of the parse tree if the string is valid, null otherwise
	 */
	private Expression parseExpression (String str) {
		if(!verifyExpression(str)) return null;
		if(str.length() == 1 || ParsedExpression.isNumber(str)) { //if the string is a literal, return it
			return new ParsedExpression(str, new ArrayList<Expression>());
		}
		final ParsedExpression top;
		int x = str.charAt(0) == '(' ? findCloseParen(str) : findStart(str, 0); //if the first character is an open paren, find the close paren index. otherwise find the split point
		if(x == str.length()-1 && str.charAt(0) == '(') { //if the first character is an open paren and the close paren index is the end of the string
			top = new ParsedExpression("()", new ArrayList<Expression>());
			top.addSubexpression(parseExpression(str.substring(1, str.length()-1))); //parse the interior expression
			return top;
		}
		else if(str.charAt(x) == ')'){ //otherwise if first character is an open paren but the close paren isn't at the end of the string
			top = new ParsedExpression(str.substring(x+1,x+2), new ArrayList<Expression>()); //create a new expression with the modifier next to the close paren
			top.addSubexpression(parseExpression(str.substring(0,x+1))); //parse the parenthetical expression
			top.addSubexpression(parseExpression(str.substring(x+2))); //parse the rest of the expression
			return top;
		}
		top = new ParsedExpression(str.substring(x,x+1), new ArrayList<Expression>()); //otherwise x is just the position of a modifier
		top.addSubexpression(parseExpression(str.substring(0,x)));
		top.addSubexpression(parseExpression(str.substring(x+1)));
		top.flatten(); //flatten everything before returning it
		return top;
	}
	
	/**
	 * Verifies that a string is a valid mathematical expression
	 * @param x the string to verify
	 * @return true if it is a valid expression, false otherwise
	 */
	private boolean verifyExpression(String x) {
		final int len = x.length();
		if((len == 1 && Character.isLetter(x.charAt(0)) || ParsedExpression.isNumber(x))) return true; //L -> [a-z] | [0-9]+
		else if (len >= 2 && x.charAt(0) == '(' && verifyExpression(x.substring(1, len-1)) && x.charAt(len-1) == ')') return true; //X -> (E)
		if(verifyMultOrAddExpr(x, '*') || verifyMultOrAddExpr(x, '+')) return true; // M -> M*M || A -> A+M
		return false;
	}
	
	/**
	 * Verifies that a string x passed the grammar cases M -> M*M || A -> A+M
	 * @param x the string to verify
	 * @param modifier the modifier to check
	 * @return true if it is valid, false otherwise
	 */
	private boolean verifyMultOrAddExpr(String x, Character modifier) {
		final int len = x.length();
		for(int i = 1; i < len-1; i++) {
			if(!isNotModifier(x.charAt(i)) && isNotModifier(x.charAt(i-1)) && isNotModifier(x.charAt(i+1))
					&& verifyExpression(x.substring(0,i)) && verifyExpression(x.substring(i+1))) return true;
		}
		return false;
	}
	
	/**
	 * Checks to see if a character is not a modifier
	 * @param x the character to check
	 * @return true if it not a modifier, false otherwise
	 */
	private boolean isNotModifier(Character x) {
		return !(x == '*' || x == '+');
	}

	/**
	 * Finds where to split the parsing tree off, prioritizing + over *
	 * @param x the string to look through
	 * @param start the index to start at
	 * @return the index of + or *
	 */
	private int findStart(String x, int start) {
		final int plus = find(x, start, '+');
		return plus == -1 ? find(x, start, '*') : plus; //if there is no +, find the *.
	}
	
	/**
	 * Finds the closing parenthesis of a string with a '(' at index 0
	 * @param x the string to look through
	 * @return index of closing parenthesis
	 */
	private int findCloseParen(String x) {
		return find(x, 1, ')');
	}
	
	/**
	 * Helper function to look through a mathematical string looking for a certain character
	 * @param x the string to look through
	 * @param start the start position in the string
	 * @param lookFor the character to look for
	 * @return the index of the character, -1 if it is not present
	 */
	private int find(String x, int start, Character lookFor) {
		int passedParen = 0;
		for(int i = start; i < x.length(); i++) {
			if(x.charAt(i) == lookFor && passedParen <= 0) return i;
			else if (x.charAt(i) == ')') passedParen--;
			else if (x.charAt(i) == '(') passedParen++;
		}
		return -1;
	}
}
