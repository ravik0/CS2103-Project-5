package main;

import java.util.ArrayList;

public class ParentheticalExpression extends CompoundExpr {
	public ParentheticalExpression(String name, CompoundExpression parent) {
		_name = name;
		setParent(parent);
		_children = new ArrayList<Expression>();
	}
}
