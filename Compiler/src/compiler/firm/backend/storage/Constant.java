package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

import firm.nodes.Const;

public class Constant extends Storage {
	private final int constant;

	public Constant(int constant) {
		this.constant = constant;
	}

	public Constant(Const constNode) {
		this.constant = constNode.getTarval().asInt();
	}

	@Override
	public String toString() {
		String result;
		if (constant < 0) {
			result = String.format("$-0x%x", -constant);
		} else {
			result = String.format("$0x%x", constant);
		}
		return result;
	}

	@Override
	public RegisterBased[] getReadOnRightSideRegister() {
		return null;
	}

	@Override
	public RegisterBased[] getUsedRegister() {
		return null;
	}

	@Override
	public boolean isSpilled() {
		return false;
	}

	public int getConstant() {
		return constant;
	}

	@Override
	public Bit getMode() {
		return null; // TODO implement this correctly
	}

	@Override
	public SingleRegister getSingleRegister() {
		return null;
	}

	@Override
	public RegisterBundle getRegisterBundle() {
		return null;
	}
}