package main;

import java.util.List;

abstract class CompoundExpr implements CompoundExpression{
	protected List<Expression> _children;
	protected CompoundExpression _parent;
	protected String _name;
	
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flatten() {
		final List<Expression> currentChildren = _children;
		for(Expression x : currentChildren) {
			if(!isExpressionLiteral(x)) {
				final CompoundExpr current = (CompoundExpr)x;
				if(current.getName().equals(getName())) {
					current.removeParentFromChildren(this);
					_children.addAll(current._children);
					_children.remove(current);
				}
			}
		}
		for(Expression x: _children) {
			x.flatten();
		}
	}

	@Override
	public String convertToString(int indentLevel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSubexpression(Expression subexpression) {
		_children.add(subexpression);
	}
	
	protected List<Expression> getChildren() {
		return _children;
	}
	
	protected String getName() {
		return _name;
	}
	
	protected boolean isExpressionLiteral(Expression x) {
		return x.getClass().getSimpleName().equals("LiteralExpression");
	}

	protected void removeParentFromChildren(CompoundExpression newParent) {
		for(int i = 0; i < _children.size(); i++) {
			_children.get(i).setParent(newParent);
		}
	}
}
