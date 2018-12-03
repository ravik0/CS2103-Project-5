package main;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold data from a parsed mathematical expression.
 * @author Ravi
 *
 */
public class ParsedExpression implements CompoundExpression{
	private List<Expression> _children;
	private CompoundExpression _parent;
	private String _name;
	
	public ParsedExpression(String name, List<Expression> children) {
		_parent = null;
		_children = children;
		_name = name;
	}

	public CompoundExpression getParent() {
		return _parent;
	}

	public void setParent(CompoundExpression parent) {
		_parent = parent;
	}

	public Expression deepCopy() {
		final CompoundExpression end = new ParsedExpression(_name, new ArrayList<Expression>());
		if(_children.size() == 0) return end;
		for(int i = 0; i < _children.size(); i++) {
			final Expression child = _children.get(i).deepCopy(); //recursively copy down the tree
			end.addSubexpression(child);
		}
		return end;
	}

	public void flatten() {
		final List<Expression> currentChildren = clone(_children); //create a clone of the list, same objects but new list pointer
		for(int i = 0; i < currentChildren.size(); i++) {
			final ParsedExpression current = (ParsedExpression)currentChildren.get(i);
			if(current.getName().equals(getName())) { //if you have two of the same expressions
				final List<Expression> currChild = current.getChildren();
				for(int a = 0; a < currChild.size(); a++) {
					this.addSubexpression(currChild.get(a));
				}
				_children.remove(current);
			}
		}
		for(Expression x: _children) {
			x.flatten(); //recursively flatten down the list
		}
		//base case is when something has no children, as then the for loops don't run.
	}

	public String convertToString(int indentLevel) {
		final StringBuffer sb = new StringBuffer();
		Expression.indent(sb, indentLevel);
		sb.append(getName() + '\n');
		for(int i = 0; i < _children.size(); i++) {
			sb.append(_children.get(i).convertToString(indentLevel+1)); //depth first recursion
		}
		return sb.toString();
	}

	public void addSubexpression(Expression subexpression) {
		if((getName().length() == 1 && Character.isLetter(getName().charAt(0)) || ParsedExpression.isNumber(getName()))) throw new IllegalArgumentException("Subexpression is literal!");
		//if the length is 1 and it's a letter (L -> [a-z]) or it's a number (L -> [0-9]+) then you can't add to this expression, it's terminal
		_children.add(subexpression);
		subexpression.setParent(this); //set the parent here to lower complexity in the user functions
	}
	
	/**
	 * Return the children nodes of this expression
	 * @return the list of children nodes
	 */
	public List<Expression> getChildren() {
		return _children;
	}
	
	/**
	 * Returns the name of this expression, so either a literal or mathematical term
	 * @return the name of the expression
	 */
	private String getName() {
		return _name;
	}
	
	/**
	 * Checks to see if the string is a number
	 * @param x the string to check
	 * @return true if x is a number, false otherwise
	 */
	public static boolean isNumber(String x) {
		try {
			Double.parseDouble(x); //tries to parse the string into a double
		}
		catch (NumberFormatException e) {
			return false; //if it throws a NumberFormatException (not a number) then return false
		}
		return true;
	}
	
	/**
	 * Returns a clone of the list, with the list being new and the object pointers being the same. 
	 * @param a the list to copy
	 * @return a copied list
	 */
	private List<Expression> clone(List<Expression> a) {
		final List<Expression> x = new ArrayList<Expression>();
		for(Expression toClone : a) {
			x.add(toClone);
		}
		return x;
	}
}
