package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JgOperation extends JumpOperation {

	public JgOperation(LabelOperation label) {
		super(label);
	}

	@Override
	protected String getOperationString() {
		return "\tjg " + getLabel();
	}

}
