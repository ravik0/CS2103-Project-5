package tests;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import main.CompoundExpr;
import main.CompoundExpression;
import main.Expression;
import main.ExpressionParseException;
import main.ExpressionParser;
import main.SimpleExpressionParser;

import java.util.*;
import java.io.*;

/**
 * Code to test Project 5; you should definitely add more tests!
 */
public class ExpressionParserPartialTester {
	private ExpressionParser _parser;

	@Before
	/**
	 * Instantiates the actors and movies graphs
	 */
	public void setUp () throws IOException {
		_parser = new SimpleExpressionParser();
	}

	@Test
	/**
	 * Just verifies that the SimpleExpressionParser could be instantiated without crashing.
	 */
	public void finishedLoading () {
		assertTrue(true);
		// Yay! We didn't crash
	}

	//@Test
	/**
	 * Verifies that a specific expression is parsed into the correct parse tree.
	 */
	public void testExpression1 () throws ExpressionParseException {
		final String expressionStr = "a+b";
		final String parseTreeStr = "+\n\ta\n\tb\n";
		assertEquals(parseTreeStr, _parser.parse(expressionStr, false).convertToString(0));
	}


	public void test() {
		CompoundExpression one = new CompoundExpr("*", new ArrayList<Expression>());
		CompoundExpression two = new CompoundExpr("3", new ArrayList<Expression>());
		CompoundExpression three = new CompoundExpr("4", new ArrayList<Expression>());
		CompoundExpression four = new CompoundExpr("+", new ArrayList<Expression>());
		CompoundExpression five = new CompoundExpr("()", new ArrayList<Expression>());
		CompoundExpression seven = new CompoundExpr("()", new ArrayList<Expression>());
		CompoundExpression six = new CompoundExpr("3", new ArrayList<Expression>());
		one.addSubexpression(five);
		one.addSubexpression(two);
		five.addSubexpression(seven);
		seven.addSubexpression(four);
		four.addSubexpression(three);
		four.addSubexpression(six);
		System.out.println(one.convertToString(0));
		one.flatten();
		System.out.println(one.convertToString(0));
		
		Expression lol = one.deepCopy();
		System.out.println(one.convertToString(0));
		System.out.println(one.equals(lol));
	}
	
	//@Test
	/**
	 * Verifies that a specific expression is parsed into the correct parse tree.
	 */
	public void testExpression2 () throws ExpressionParseException {
		final String expressionStr = "13*x";
		final String parseTreeStr = "*\n\t13\n\tx\n";
		assertEquals(parseTreeStr, _parser.parse(expressionStr, false).convertToString(0));
	}

	@Test
	/**
	 * Verifies that a specific expression is parsed into the correct parse tree.
	 */
	public void testExpression3 () throws ExpressionParseException {
		final String expressionStr = "4*(z+5*x)";
		final String parseTreeStr = "*\n\t4\n\t()\n\t\t+\n\t\t\tz\n\t\t\t*\n\t\t\t\t5\n\t\t\t\tx\n";
		assertEquals(parseTreeStr, _parser.parse(expressionStr, false).convertToString(0));
	}

	//@Test
	/**
	 * Verifies that a specific expression is parsed into the correct parse tree.
	 */
	public void testExpressionAndFlatten1 () throws ExpressionParseException {
		final String expressionStr = "1+2+3";
		final String parseTreeStr = "+\n\t1\n\t2\n\t3\n";
		assertEquals(parseTreeStr, _parser.parse(expressionStr, false).convertToString(0));
	}

	//@Test
	/**
	 * Verifies that a specific expression is parsed into the correct parse tree.
	 */
	public void testExpressionAndFlatten2 () throws ExpressionParseException {
		final String expressionStr = "(x+(x)+(x+x)+x)";
		final String parseTreeStr = "()\n\t+\n\t\tx\n\t\t()\n\t\t\tx\n\t\t()\n\t\t\t+\n\t\t\t\tx\n\t\t\t\tx\n\t\tx\n";
		assertEquals(parseTreeStr, _parser.parse(expressionStr, false).convertToString(0));
	}

	@Test(expected = ExpressionParseException.class) 
	/**
	 * Verifies that a specific expression is parsed into the correct parse tree.
	 */
	public void testException1 () throws ExpressionParseException {
		final String expressionStr = "1+2+";
		_parser.parse(expressionStr, false);
	}

	@Test(expected = ExpressionParseException.class) 
	/**
	 * Verifies that a specific expression is parsed into the correct parse tree.
	 */
	public void testException2 () throws ExpressionParseException {
		final String expressionStr = "((()))";
		_parser.parse(expressionStr, false);
	}

	@Test(expected = ExpressionParseException.class) 
	/**
	 * Verifies that a specific expression is parsed into the correct parse tree.
	 */
	public void testException3 () throws ExpressionParseException {
		final String expressionStr = "()()";
		_parser.parse(expressionStr, false);
	}
}
