package main;
/**
 * Exception thrown when a ExpressionParser fails to parse a specified string.
 */
public class ExpressionParseException extends Exception {
	public ExpressionParseException (String message) {
		super(message);
	}
}
