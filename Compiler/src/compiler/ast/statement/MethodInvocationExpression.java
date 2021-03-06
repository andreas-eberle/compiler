package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.declaration.MethodMemberDeclaration;
import compiler.ast.statement.unary.PostfixExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class MethodInvocationExpression extends PostfixExpression {

	private final Symbol methodIdent;
	private final Expression expression;
	private final Expression[] parameters;
	private MethodMemberDeclaration methodDeclaration;

	public MethodInvocationExpression(Position position, Expression leftExpression, Symbol methodIdent, Expression[] parameters) {
		super(position, leftExpression);
		this.methodIdent = methodIdent;
		this.parameters = parameters;
		this.expression = leftExpression;
	}

	public Symbol getMethodIdentifier() {
		return methodIdent;
	}

	public Expression getMethodExpression() {
		return expression;
	}

	public Expression[] getParameters() {
		return parameters;
	}

	public boolean isLocalMethod() {
		return expression == null ? true : false;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	public MethodMemberDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	public void setMethodDeclaration(MethodMemberDeclaration methodDeclaration) {
		this.methodDeclaration = methodDeclaration;
	}
}
