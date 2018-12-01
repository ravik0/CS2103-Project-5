package main;

import java.util.ArrayList;
import java.util.List;

public class ParsedExpression implements CompoundExpression{
	protected List<Expression> _children;
	protected CompoundExpression _parent;
	protected String _name;
	
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
		CompoundExpression end = new ParsedExpression(_name, new ArrayList<Expression>());
		if(_children.size() == 0) return end;
		for(int i = 0; i < _children.size(); i++) {
			Expression child = _children.get(i).deepCopy();
			end.addSubexpression(child);
		}
		return end;
	}

	public void flatten() {
		final List<Expression> currentChildren = clone(_children);
		for(int i = 0; i < currentChildren.size(); i++) {
			final ParsedExpression current = (ParsedExpression)currentChildren.get(i);
			if(current.getName().equals(getName())) {
				for(int a = 0; a < current.getChildren().size(); a++) {
					this.addSubexpression(current.getChildren().get(a));
				}
				_children.remove(current);
			}
		}
		for(Expression x: _children) {
			x.flatten();
		}
	}

	public String convertToString(int indentLevel) {
		StringBuffer sb = new StringBuffer();
		Expression.indent(sb, indentLevel);
		sb.append(getName() + '\n');
		for(int i = 0; i < _children.size(); i++) {
			sb.append(_children.get(i).convertToString(indentLevel+1));
		}
		return sb.toString();
	}

	public void addSubexpression(Expression subexpression) {
		_children.add(subexpression);
		subexpression.setParent(this);
	}
	
	private List<Expression> getChildren() {
		return _children;
	}
	
	private String getName() {
		return _name;
	}
	
	public static boolean isNumber(String x) {
		try {
			Double.parseDouble(x);
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private List<Expression> clone(List<Expression> a) {
		List<Expression> x = new ArrayList<Expression>();
		for(Expression toClone : a) {
			x.add(toClone);
		}
		return x;
	}
}
