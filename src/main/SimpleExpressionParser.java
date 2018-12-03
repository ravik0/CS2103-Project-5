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
			return makeExpr(str,0,str.length());
		}
		final ParsedExpression top;
		final boolean stringStartWithParen = str.charAt(0) == '(';
		final int cutPoint = stringStartWithParen ? findCloseParen(str) : findCutPoint(str); 
		if(cutPoint == str.length()-1 && stringStartWithParen) { 
			top = makeExpr("()",0,2);
			addParsedExpr(top,str,1,str.length()-1); //parse the interior expression
			return top;
		}
		else if(str.charAt(cutPoint) == ')'){ //otherwise if first character is an open paren but the close paren isn't at the end of the string
			top = makeExpr(str,cutPoint+1,cutPoint+2); //create a new expression of the modifier next to the close paren
			addParsedExpr(top,str,0,cutPoint+1); //parse the parenthetical expression
			addParsedExpr(top,str,cutPoint+2,str.length()); //parse the rest of the expression
			return top;
		}
		top = makeExpr(str,cutPoint,cutPoint+1); //otherwise x is just the position of a modifier
		addParsedExpr(top,str,0,cutPoint);
		addParsedExpr(top,str,cutPoint+1,str.length());
		top.flatten(); //flatten everything before returning it
		return top;
	}
	
	/**
	 * Helper function to add a non-parsed subexpression to an already parsed expression
	 * @param top the parsed expression
	 * @param x the string containing the non-parsed subexpression
	 * @param start the position to start the parsing of x
	 * @param end the position to end the parsing of x
	 */
	private void addParsedExpr(ParsedExpression top, String x, int start, int end) {
		top.addSubexpression(parseExpression(x.substring(start, end)));
	}
	
	/**
	 * Helper function to make a new ParsedExpression
	 * @param x the string to make it out of
	 * @param start the start position of the name
	 * @param end the end position of the name
	 * @return a new parsed expression
	 */
	private ParsedExpression makeExpr(String x, int start, int end) {
		return new ParsedExpression(x.substring(start,end), new ArrayList<Expression>());
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
		if(verifyModifier(x)) return true; // M -> M*M || A -> A+M
		return false;
	}
	
	/**
	 * Verifies that a string x passes the grammar cases M -> M*M || A -> A+M
	 * @param x the string to verify
	 * @return true if it is valid, false otherwise
	 */
	private boolean verifyModifier(String x) {
		final int len = x.length();
		for(int i = 1; i < len-1; i++) {
			if(!isNotModifier(x.charAt(i)) && isNotModifier(x.charAt(i-1)) && isNotModifier(x.charAt(i+1))
					&& verifyExpression(x.substring(0,i)) && verifyExpression(x.substring(i+1))) return true;
			//checks if the character at position i is a modifier, and the two characters around it aren't
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
	 * @return the index of + or *
	 */
	private int findCutPoint(String x) {
		final int plus = find(x, 0, '+');
		return plus == -1 ? find(x, 0, '*') : plus; //if there is no +, find the *.
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
		int passedParen = 0; //makes sure that this character is not inside of a parenthesis block
		for(int i = start; i < x.length(); i++) {
			if(x.charAt(i) == lookFor && passedParen <= 0) return i;
			else if (x.charAt(i) == ')') passedParen--;
			else if (x.charAt(i) == '(') passedParen++;
		}
		return -1;
	}
}
