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
	
	protected Expression parseExpression (String str) {
		//System.out.println(str);
		if(!verifyExpression(str)) return null;
		if(str.length() == 1 || CompoundExpr.isNumber(str)) {
			return new CompoundExpr(str, new ArrayList<Expression>());
		}
		CompoundExpr top;
		int x = str.charAt(0) == '(' ? findCloseParen(str) : findStart(str, 0);
		int startPoint = 0;
		int endPoint = str.length();
		if(x == str.length()-1 && str.charAt(0) == '(') {
			top = new CompoundExpr("()", new ArrayList<Expression>());
			top.addSubexpression(parseExpression(str.substring(1, str.length()-1)));
			return top;
		}
		else if(str.charAt(x) == ')'){
			top = new CompoundExpr(str.substring(x+1,x+2), new ArrayList<Expression>());
			top.addSubexpression(parseExpression(str.substring(0,x+1)));
			top.addSubexpression(parseExpression(str.substring(x+2)));
			return top;
		}
		else {
			top = new CompoundExpr(str.substring(x,x+1), new ArrayList<Expression>());
		}
		top.addSubexpression(parseExpression(str.substring(startPoint,x)));
		top.addSubexpression(parseExpression(str.substring(x+1, endPoint)));
		top.flatten();
		return top;
	}
	
	private static boolean verifyExpression(String x) {
		int len = x.length();
		if((len == 1 && Character.isLetter(x.charAt(0)) || CompoundExpr.isNumber(x))) return true; //L -> [a-z] | [0-9]+
		else if (len >= 2 && x.charAt(0) == '(' && verifyExpression(x.substring(1, len-1)) && x.charAt(len-1) == ')') return true; //X -> (E)
		if(verifyMultOrAddExpr(x, '*') || verifyMultOrAddExpr(x, '+')) return true; // M -> M*M || A -> A+M
		return false;
	}
	
	private static boolean verifyMultOrAddExpr(String x, Character modifier) {
		int len = x.length();
		for(int i = 1; i < len-1; i++) {
			if(!isNotModifier(x.charAt(i)) && isNotModifier(x.charAt(i-1)) && isNotModifier(x.charAt(i+1))
					&& verifyExpression(x.substring(0,i)) && verifyExpression(x.substring(i+1))) return true;
		}
		return false;
	}
	
	private static boolean isNotModifier(Character x) {
		return !(x == '*' || x == '+');
	}

	private int findStart(String x, int start) {
		int passedParen = 0;
		for(int i = start; i < x.length(); i++) {
			if(x.charAt(i) == '(') passedParen++;
			if(x.charAt(i) == ')') passedParen--;
			if((x.charAt(i) == '+') && passedParen == 0) return i;
		}
		return findMult(x, start);
	}
	
	private int findMult(String x, int start) {
		int passedParen = 0;
		for(int i = start; i < x.length(); i++) {
			if(x.charAt(i) == '(') passedParen++;
			if(x.charAt(i) == ')') passedParen--;
			if((x.charAt(i) == '*') && passedParen == 0) return i;
		}
		return -1;
	}
	
	private int findCloseParen(String x) {
		int passedParen = 0;
		for(int i = 1; i < x.length(); i++) {
			if(x.charAt(i) == ')' && passedParen <= 0) return i;
			else if (x.charAt(i) == ')') passedParen--;
			else if (x.charAt(i) == '(') passedParen++;
		}
		return -1;
	}
}
