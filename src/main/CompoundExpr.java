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
		CompoundExpression end = new CompoundExpr(_name, new ArrayList<Expression>());
		if(_children.size() == 0) return end;
		for(int i = 0; i < _children.size(); i++) {
			Expression child = _children.get(i).deepCopy();
			end.addSubexpression(child);
		}
		return end;
	}

	@Override
	public void flatten() {
		final List<Expression> currentChildren = clone(_children);
		for(int i = 0; i < currentChildren.size(); i++) {
			final CompoundExpr current = (CompoundExpr)currentChildren.get(i);
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

	@Override
	public String convertToString(int indentLevel) {
		StringBuffer sb = new StringBuffer();
		Expression.indent(sb, indentLevel);
		sb.append(getName() + '\n');
		for(int i = 0; i < _children.size(); i++) {
			sb.append(_children.get(i).convertToString(indentLevel+1));
		}
		return sb.toString();
	}

	@Override
	public void addSubexpression(Expression subexpression) {
		_children.add(subexpression);
		subexpression.setParent(this);
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
	
	private static List<Expression> deepClone(List<Expression> a) {
		List<Expression> x = new ArrayList<Expression>();
		for(Expression toClone : a) {
			x.add(toClone.deepCopy());
		}
		return x;
	}
	
	private static List<Expression> clone(List<Expression> a) {
		List<Expression> x = new ArrayList<Expression>();
		for(Expression toClone : a) {
			x.add(toClone);
		}
		return x;
	}
}
