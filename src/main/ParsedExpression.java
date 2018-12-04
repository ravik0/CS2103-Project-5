package main;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Class to hold data from a parsed mathematical expression.
 * @author Ravi
 *
 */
public class ParsedExpression implements CompoundExpression{
	private List<Expression> _children;
	private CompoundExpression _parent;
	private String _name;
	private HBox _node;
	
	public ParsedExpression(String name) {
		_parent = null;
		_children = new ArrayList<Expression>();
		_name = name;
		_node = null;
	}

	public CompoundExpression getParent() {
		return _parent;
	}

	public void setParent(CompoundExpression parent) {
		_parent = parent;
	}

	public Expression deepCopy() {
		final CompoundExpression end = new ParsedExpression(new String(_name));
		if(_children.size() == 0) return end;
		for(int i = 0; i < _children.size(); i++) {
			final Expression child = _children.get(i).deepCopy(); //recursively copy down the tree
			end.addSubexpression(child);
		}
		return end;
	}

	public void flatten() {
		for(int i = 0; i < _children.size(); i++) {
			final ParsedExpression current = (ParsedExpression)_children.get(i);
			if(current.getName().equals(getName())) { //if you have two of the same expressions
				final List<Expression> currChild = current.getChildren();
				for(int a = 0; a < currChild.size(); a++) {
					this.addSubexpression(currChild.get(a));
				}
				_children.remove(current);
				i--;
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
		if(isLiteral()) throw new IllegalArgumentException("Subexpression is literal!");
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

	@Override
	public Node getNode() {
		if(_node == null) formNode();
		return _node;
	}
	
	private void formNode() {
		if(isLiteral()) {
			_node = new HBox(new Label(getName()));
		}
		else {
			_node = new HBox();
			if(!getName().equals("()")) {
				for(int i = 0; i < _children.size(); i++) {
					_node.getChildren().add(_children.get(i).getNode());
					if(i != _children.size()-1) _node.getChildren().add(new Label(this.getName()));
				}
			}
			else {
				_node.getChildren().add(new Label("("));
				for(int i = 0; i < _children.size(); i++) {
					_node.getChildren().add(_children.get(i).getNode());
				}
				_node.getChildren().add(new Label(")"));
			}
		}
	}
	
	public boolean isLiteral() {
		return (getName().length() == 1 && Character.isLetter(getName().charAt(0)) || ParsedExpression.isNumber(getName()));
	}

}
