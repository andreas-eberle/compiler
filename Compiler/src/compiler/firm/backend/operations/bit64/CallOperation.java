package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class CallOperation extends AssemblerOperation {

	private String name;

	public CallOperation(String name) {
		this.name = name;
	}

	@Override
	public String getOperationString() {
		return String.format("\tcall %s", name);
	}

}
