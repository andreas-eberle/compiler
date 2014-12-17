package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.storage.Register;

public class AddlOperation extends TwoRegOperandsOperation {

	public AddlOperation() {
	}

	public AddlOperation(Register input, Register destinationRegister) {
		super(input, destinationRegister);
	}

	@Override
	public String toString() {
		return String.format("\taddl %s, %s\n", getInputRegister(), getDestinationRegister());
	}

	public static TwoRegOperandsOperation generate(Register input, Register destination) {
		return new AddlOperation(input, destination);
	}
}
