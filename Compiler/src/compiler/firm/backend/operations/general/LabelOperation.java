package compiler.firm.backend.operations.general;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class LabelOperation extends AssemblerOperation {

	private String name;

	public LabelOperation(String name) {
		this.name = name;
	}

	@Override
	public String getOperationString() {
		return String.format("%s:", name);
	}

}
