package compiler.firm.backend.operations;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;

public class PopOperation extends AssemblerBitOperation {
	private final RegisterBased register;

	public PopOperation(RegisterBased register) {
		this(null, register);
	}

	public PopOperation(String comment, RegisterBased register) {
		super(comment);
		this.register = register;
	}

	@Override
	public String getOperationString() {
		return String.format("\tpop %s", register.toString());
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Collections.emptySet();
	}
}
