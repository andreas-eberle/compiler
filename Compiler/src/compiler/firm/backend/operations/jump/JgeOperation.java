package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JgeOperation extends JumpOperation {

	public JgeOperation(LabelOperation label) {
		super(label);
	}

	@Override
	public String getOperationString() {
		return "\tjge " + getLabelName();
	}

	@Override
	public JumpOperation invert(LabelOperation label) {
		return new JlOperation(label);
	}

}
