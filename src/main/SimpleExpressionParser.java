package main;
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
		Expression expression;
		
		// TODO implement me
		return null;
	}
	
	public static boolean verifyExpression(String x) {
		x = removeAllWhiteSpace(x);
		int len = x.length();
		if((len == 1 && Character.isLetter(x.charAt(0)) || isNumber(x))) return true; //L -> [a-z] | [0-9]+
		else if (len >= 2 && x.charAt(0) == '(' && verifyExpression(x.substring(1, len-1)) && x.charAt(len-1) == ')') return true; //X -> (E)
		if(verifyMultOrAddExpr(x, '*') || verifyMultOrAddExpr(x, '+')) return true; // M -> M*M || A -> A+M
		return false;
	}
	
	private static boolean verifyMultOrAddExpr(String x, Character modifier) {
		int len = x.length();
		for(int i = 1; i < len-1; i++) {
			if(x.charAt(i) == modifier && isNotModifier(x.charAt(i-1)) && isNotModifier(x.charAt(i+1))
					&& verifyExpression(x.substring(0,i)) && verifyExpression(x.substring(i+1))) return true;
		}
		return false;
	}
	
	private static boolean isNumber(String x) {
		try {
			Double.parseDouble(x);
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private static String removeAllWhiteSpace(String x) {
		StringBuilder bob = new StringBuilder();
		for(int i = 0; i < x.length(); i++) {
			String character = x.substring(i, i+1);
			if(!character.equals(" ")) {
				bob.append(character);
			}
		}
		return bob.toString();
	}
	
	private static boolean isNotModifier(Character x) {
		return !(x == '*' || x == '+');
	}
}
