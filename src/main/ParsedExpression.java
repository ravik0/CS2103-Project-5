package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * Class to hold data from a parsed mathematical expression.
 * @author Ravi
 *
 */
public class ParsedExpression implements CompoundExpression{
	private List<Expression> _children;
	private CompoundExpression _parent;
	final private String _name;
	private HBox _node;
	final private List<Label> _labelList;
	private boolean isFocused;
	private int identifier;
	
	public ParsedExpression(String name) {
		_parent = null;
		_children = new ArrayList<Expression>();
		_name = name;
		_node = null;
		_labelList = new ArrayList<Label>();
		identifier = (int)(Math.random()*Integer.MAX_VALUE)  +1;
	}

	public CompoundExpression getParent() {
		return _parent;
	}

	public void setParent(CompoundExpression parent) {
		_parent = parent;
	}

	public Expression deepCopy() {
		final CompoundExpression end = new ParsedExpression(new String(_name));
		((ParsedExpression)end).setIdentifier(this.identifier);
		if(_children.size() == 0) return end;
		for(int i = 0; i < _children.size(); i++) {
			final Expression child = ((ParsedExpression) _children.get(i)).deepCopy();//recursively copy down the tree
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
	public String getName() {
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

	public Node getNode() {
		if(_node == null) formNode();
		return _node;
	}
	
	/**
	 * Helper function to create the node
	 */
	private void formNode() {
		if(isLiteral()) {
			_labelList.add(new Label(getName())); //add to a list of labels for this expression
			_node = new HBox(_labelList.get(0)); //creates node
		}
		else {
			_node = new HBox();
			if(!getName().equals("()")) {
				Label name = new Label(getName());
				for(int i = 0; i < _children.size(); i++) {
					_node.getChildren().add(_children.get(i).getNode());
					if(i != _children.size()-1) _node.getChildren().add(name);
					_labelList.add(name);
					name = new Label(getName()); //avoids the error of adding the same label to the HBox's children by just making a new Label with same string contents.
				}
			}
			else {
				final Label openParen = new Label("(");
				_labelList.add(openParen);
				_node.getChildren().add(openParen);
				for(int i = 0; i < _children.size(); i++) {
					_node.getChildren().add(_children.get(i).getNode());
				}
				final Label closedParen = new Label(")");
				_labelList.add(closedParen);
				_node.getChildren().add(closedParen);
			}
		} 
	}
	
	/**
	 * Sets the identifier of an expression to a certain integer. This is used
	 * when deep copying, so the deep copy has the same identifier as the
	 * normal one.
	 * @param x the new value of the identifier.
	 */
	private void setIdentifier(int x) {
		identifier = x;
	}
	
	/**
	 * Function to reform the node if the underlying expression is changed
	 */
	private void reformNode() {
		_node = null;
		_labelList.clear();
		formNode();
	}
	
	/**
	 * Function to check if this expression is literal
	 * @return true if literal, false otherwise
	 */
	public boolean isLiteral() {
		return (getName().length() == 1 && Character.isLetter(getName().charAt(0)) || ParsedExpression.isNumber(getName()));
	}
	
	/**
	 * Function to check if this expression has a parent
	 * @return true if has parent, false otherwise
	 */
	public boolean hasParent() {
		return _parent != null;
	}

	/**
	 * Function to set the node of this expression to a certain color
	 * @param color the color to set to
	 */
	public void setExpressionColor(Paint color) {
		for(int i = 0; i < _labelList.size(); i++) {
			_labelList.get(i).setTextFill(color);
		}
		if(isFocused) getNode().setStyle("-fx-border-color: red;");
		else getNode().setStyle("");
		for(int i = 0; i < _children.size(); i++) {
			((ParsedExpression)_children.get(i)).setExpressionColor(color);
		}
	}
	
	/**
	 * Function to get all the possible configurations that this expression could be in
	 * @return a map containing the index of this expression and the overall expression
	 */
	public Map<Integer, Expression> getOtherPossibleConfigurations() {
		final List<Expression> otherChildren = ((ParsedExpression)_parent).getChildren();

		final List<List<Expression>> newConfigs = new ArrayList<List<Expression>>();
		
		newConfigs.add(listCopy(otherChildren)); 
		
		final int index = otherChildren.indexOf(this);
		
		//moves this expression around to find all other configurations
		List<Expression> toSwap = newConfigs.get(0);
		for(int i = index; i < otherChildren.size()-1; i++) {
			newConfigs.add(swapRight(toSwap, i));
			toSwap = newConfigs.get(newConfigs.size()-1);
		}
		toSwap = newConfigs.get(0);
		for(int i = index; i > 0; i--) {
			newConfigs.add(swapLeft(toSwap, i));
			toSwap = newConfigs.get(newConfigs.size()-1);
		}
		
		//ties the list of expressions to a parent
		final List<Expression> parentConfigs = new ArrayList<Expression>();
		for(int i = 0; i < newConfigs.size(); i++) {
			parentConfigs.add(addParent(newConfigs.get(i)));
		}
		
		final Map<Integer, Expression> ret = new HashMap<Integer,Expression>();
		for(int i = 0; i < parentConfigs.size(); i++) {
			ret.put(newConfigs.get(i).indexOf(this), parentConfigs.get(i));
		}
		return ret;
	}
	
	/**
	 * Helper function to copy a list of expressions into a new list, avoiding pointer issues
	 * @param toCopy the list to copy
	 * @return a list with the same contents but different pointer
	 */
	private List<Expression> listCopy(List<Expression> toCopy) {
		final List<Expression> ret = new ArrayList<Expression>();
		for(int i = 0; i < toCopy.size(); i++) {
			ret.add(toCopy.get(i));
		}
		return ret;
	}
	
	/**
	 * Swaps the expression at index in toSwap to the right by 1
	 * @param toSwap the list containing the expression to swap
	 * @param index the index to swap at
	 * @return a new list that has the index swapped right
	 */
	private List<Expression> swapRight(List<Expression> toSwap, int index) {
		final List<Expression> ret = new ArrayList<Expression>();
		for(int i = 0; i < index; i++) {
			ret.add(toSwap.get(i));
		}
		ret.add(toSwap.get(index+1));
		ret.add(toSwap.get(index));
		for(int i = index+2; i < toSwap.size(); i++) {
			ret.add(toSwap.get(i));
		}
		return ret;
	}
	
	/**
	 * Swaps the expression at index in toSwap to the left by 1
	 * @param toSwap the list containing the expression to swap
	 * @param index the index to swap at
	 * @return a new list that has the index swapped left
	 */
	private List<Expression> swapLeft(List<Expression> toSwap, int index) {
		final List<Expression> ret = new ArrayList<Expression>();
		for(int i = 0; i < index-1; i++) {
			ret.add(toSwap.get(i));
		}
		ret.add(toSwap.get(index));
		ret.add(toSwap.get(index-1));
		for(int i = index+1; i < toSwap.size(); i++) {
			ret.add(toSwap.get(i));
		}
		return ret;
	}
	
	/**
	 * Helper function to add a list of expressions to the parent of this.
	 * @param toAdd the list of children to add
	 * @return a new expression, with the expression being the parent (deepcopied) and the children being toAdd
	 */
	private Expression addParent(List<Expression> toAdd) {
		final ParsedExpression parent = (ParsedExpression) getParent().deepCopy();
		parent.getChildren().clear();
		for(int i = 0; i < toAdd.size(); i++) {
			parent.addSubexpression(toAdd.get(i).deepCopy());
		}
		return parent;
	}
	
	/**
	 * Function that will go from any point in an expression and return the top of the tree
	 * @param x the expression to go from
	 * @return the full expression where x is a subexpression of the returned expression
	 */
	public static ParsedExpression getOriginal(Expression x) {
		ParsedExpression ret = (ParsedExpression) x;
		while(ret.hasParent()) {
			ret = (ParsedExpression) ret.getParent();
		}
		return ret;
	}
	
	/**
	 * A function that will take a ParsedExpression which has the same children as this but in a different order, and convert
	 * this expression's children to be in the same order while maintaining the same pointers rather than changing them to 
	 * new objects.
	 * @param x the expression to convert to
	 */
	public void convertTo(ParsedExpression x) {
		final List<String> thisList = makeListString(this);
		final List<String> xList = makeListString(x);
		final Map<String, Integer> newIndices = new HashMap<String,Integer>();
		for(int i = 0; i < thisList.size(); i++) {
			newIndices.put(thisList.get(i), xList.indexOf(thisList.get(i))); //the new indices of each child expression is stored in the map, tied to the name of the child expression
		}
		final List<Expression> newChildren = new ArrayList<Expression>();
		for(int i = 0; i < thisList.size(); i++) newChildren.add(null); //set up the list so we can just use .set instead of add(index, element), negligable loss of performance
		for(int i = 0; i < thisList.size(); i++) {
			newChildren.set(newIndices.get(thisList.get(i)), _children.get(i)); //set the new indicies.
		}
		_children = newChildren; //set current children to new children, different order but the same children and same pointers
		for(int i = 0; i < _children.size(); i++) {
			((ParsedExpression) _children.get(i)).convertTo((ParsedExpression) x.getChildren().get(i));
			//recursively convert in case we need to do that. 
			//better safe than sorry
			//efficiency doesn't matter much here so yeah
		}
		reformNode();
	}
	
	/**
	 * A helper function that takes a ParsedExpression and converts its children to a list of strings containing their names
	 * @param x the expression to convert
	 * @return a list of strings - the strings are the names of the children and the ordering is the exact same as the children.
	 */
	private List<String> makeListString(ParsedExpression x) {
		final List<String> ret = new ArrayList<String>();
		for(int i = 0; i < x.getChildren().size(); i++) {
			final ParsedExpression child = ((ParsedExpression) x.getChildren().get(i));
			ret.add(child.getName() + child.identifier);
			//what this does is that it adds a unique identifier so that the program knows
			//which is what and how to match them. like 2+x+x, it can identify the two x's 
			//it is very, very unlikely two different expressions have the same identifier. 
		}
		return ret;
	}
	
	/**
	 * Set whether this node is focused or not
	 * @param x true if focused, false otherwise
	 */
	public void setFocused(boolean x) {
		isFocused = x;
		if(!x) setExpressionColor(Paint.valueOf("black"));
	}
	

}
