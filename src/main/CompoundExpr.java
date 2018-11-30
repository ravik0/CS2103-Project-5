package main;

import java.util.ArrayList;
import java.util.List;

public class CompoundExpr implements CompoundExpression{
	protected List<Expression> _children;
	protected CompoundExpression _parent;
	protected String _name;
	
	public CompoundExpr(String name, List<Expression> children) {
		_parent = null;
		_children = children;
		_name = name;
	}
	@Override
	public CompoundExpression getParent() {
		return _parent;
	}

	@Override
	public void setParent(CompoundExpression parent) {
		_parent = parent;
	}

	@Override
	public Expression deepCopy() {
		if(_children.equals(new ArrayList<Expression>())) return new CompoundExpr(_name, new ArrayList<Expression>());
		List<Expression> ret = new ArrayList<Expression>();
		CompoundExpression end = new CompoundExpr(_name, ret);
		for(int i = 0; i < _children.size(); i++) {
			Expression child = _children.get(i).deepCopy();
			child.setParent(end);
			ret.add(child);
		}
		return end;
	}

	@Override
	public void flatten() {
		final List<Expression> currentChildren = _children;
		for(Expression x : currentChildren) {
			final CompoundExpr current = (CompoundExpr)x;
			if(current.getName().equals(getName())) {
				current.removeParentFromChildren(this);
				_children.addAll(current._children);
				_children.remove(current);
			}
		}
		for(Expression x: _children) {
			x.flatten();
		}
	}

	@Override
	public String convertToString(int indentLevel) {
		StringBuffer sb = new StringBuffer();
		Expression.indent(sb, indentLevel);
		sb.append(getName());
		for(int i = 0; i < _children.size(); i++) {
			sb.append('\n' + _children.get(i).convertToString(indentLevel+1));
		}
		return sb.toString();
	}

	@Override
	public void addSubexpression(Expression subexpression) {
		if(!isNumber(getName()) && !Character.isLetter(getName().charAt(0))) _children.add(subexpression);
		else throw new IllegalArgumentException("This is a literal!");
	}
	
	protected List<Expression> getChildren() {
		return _children;
	}
	
	protected String getName() {
		return _name;
	}

	protected void removeParentFromChildren(CompoundExpression newParent) {
		for(int i = 0; i < _children.size(); i++) {
			_children.get(i).setParent(newParent);
		}
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
}
