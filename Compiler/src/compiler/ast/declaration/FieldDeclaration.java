package compiler.ast.declaration;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class FieldDeclaration extends MemberDeclaration {
	private final Type type;

	public FieldDeclaration(Position position, boolean isStatic, Type type, Symbol identifier) {
		super(position, isStatic, identifier);
		this.type = type;
	}

	public FieldDeclaration(Type type, Symbol identifier) {
		this(null, false, type, identifier);
	}

	public FieldDeclaration(Position position, Type type, Symbol identifier) {
		this(position, false, type, identifier);
	}

	public FieldDeclaration(boolean isStatic, Type type, Symbol identifier) {
		this(null, isStatic, type, identifier);
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected int getSortPriority() {
		return 1;
	}

	@Override
	protected String getAssemblerNamePrefix() {
		return "f$";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldDeclaration other = (FieldDeclaration) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
