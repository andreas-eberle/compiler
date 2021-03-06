package compiler.ast.statement;

import compiler.ast.statement.unary.PostfixExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ArrayAccessExpression extends PostfixExpression {
	private final Expression arrayExpression;
	private final Expression indexExpression;

	public ArrayAccessExpression(Position position, Expression arrayExpression, Expression indexExpression) {
		super(position, arrayExpression);
		this.arrayExpression = arrayExpression;
		this.indexExpression = indexExpression;
	}

	public Expression getArrayExpression() {
		return arrayExpression;
	}

	public Expression getIndexExpression() {
		return indexExpression;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

}
