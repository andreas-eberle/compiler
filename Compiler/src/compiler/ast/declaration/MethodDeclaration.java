package compiler.ast.declaration;

import java.util.List;

import compiler.Symbol;
import compiler.ast.Block;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class MethodDeclaration extends MethodMemberDeclaration {
	private final Block block;
	private int numberOfLocalVariables;

	public MethodDeclaration(Position position, boolean isStatic, Symbol identifier, List<ParameterDeclaration> parameters, Type returnType,
			Block body) {
		super(position, isStatic, identifier, parameters, returnType);
		this.block = body;
	}

	public Block getBlock() {
		return block;
	}

	public void setNumberOfRequiredLocals(int numberOfLocals) {
		this.setNumberOfLocalVariables(numberOfLocals);
	}

	public int getNumberOfLocalVariables() {
		return numberOfLocalVariables;
	}

	public void setNumberOfLocalVariables(int numberOfLocalVariables) {
		this.numberOfLocalVariables = numberOfLocalVariables;
	}

	@Override
	protected String getAssemblerNamePrefix() {
		return "m$";
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isNative() {
		return false;
	}
}
