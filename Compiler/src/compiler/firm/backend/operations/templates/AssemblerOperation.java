package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.RegisterBased;

public abstract class AssemblerOperation {

	private final String comment;

	public AssemblerOperation() {
		this.comment = null;
	}

	public AssemblerOperation(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public final String toString() {
		String operationString = getOperationString();
		return getComment() == null ? operationString : operationString + "\t# " + getComment();
	}

	public String[] toStringWithSpillcode() {
		return new String[] { toString() };
	}

	public abstract String getOperationString();

	public RegisterBased[] getReadRegisters() {
		return new RegisterBased[] {};
	}

	public RegisterBased[] getUsedRegisters() {
		return new RegisterBased[] {};
	}

	public boolean hasSpilledRegisters() {
		for (RegisterBased register : getReadRegisters()) {
			if (register.isSpilled()) {
				return true;
			}
		}
		for (RegisterBased register : getUsedRegisters()) {
			if (register.isSpilled()) {
				return true;
			}
		}
		return false;
	}
}
