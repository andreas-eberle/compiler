package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;

public abstract class TwoRegOperandsOperation extends AssemblerOperation {

	private Register input;
	private Register destination;

	public TwoRegOperandsOperation(String comment) {
		super(comment);
	}

	public TwoRegOperandsOperation(String comment, Register input, Register destination) {
		super(comment);
		initialize(input, destination);
	}

	public Register getInputRegister() {
		return input;
	}

	public Register getDestinationRegister() {
		return destination;
	}

	public void initialize(Register input, Register destination) {
		this.input = input;
		this.destination = destination;
	}
}
