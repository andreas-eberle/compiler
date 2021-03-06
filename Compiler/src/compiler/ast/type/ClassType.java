package compiler.ast.type;

import compiler.Symbol;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ClassType extends Type {

	private final Symbol identifier;
	private firm.ClassType firmClassType;

	public ClassType(Symbol identifier) {
		this(null, identifier);
	}

	public ClassType(Position position, Symbol identifier) {
		super(position, BasicType.CLASS);
		this.identifier = identifier;
	}

	@Override
	public Symbol getIdentifier() {
		return identifier;
	}

	public firm.ClassType getFirmClassType() {
		if (firmClassType == null) {
			firmClassType = new firm.ClassType(getIdentifier().getValue());
		}
		return firmClassType;
	}

	@Override
	protected firm.Type generateFirmType() {
		return new firm.PointerType(getFirmClassType());
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Type))
			return false;
		Type otherT = (Type) obj;
		if (getBasicType() == BasicType.NULL || otherT.getBasicType() == BasicType.NULL)
			return true;
		if (getClass() != obj.getClass())
			return false;
		ClassType other = (ClassType) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}
}
