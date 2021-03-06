package compiler.firm.backend.operations.templates;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import compiler.firm.backend.registerallocation.ssa.AssemblerOperationsBlock;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;

public abstract class AssemblerOperation {

	private static final RegisterBundle[] ACCUMULATOR_REGISTERS = { RegisterBundle._9D, RegisterBundle._10D, RegisterBundle._11D };

	private final String comment;
	private int accumulatorRegister = 0;

	private AssemblerOperationsBlock operationsBlock;

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

	public Set<RegisterBased> getReadRegisters() {
		return Collections.emptySet();
	}

	public Set<VirtualRegister> getVirtualReadRegisters() {
		Set<RegisterBased> readRegisters = getReadRegisters();
		Set<VirtualRegister> virtualRegisters = new HashSet<>();
		for (RegisterBased register : readRegisters) {
			if (register instanceof VirtualRegister) {
				virtualRegisters.add((VirtualRegister) register);
			}
		}
		return virtualRegisters;
	}

	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}

	public Set<VirtualRegister> getVirtualWriteRegisters() {
		Set<RegisterBased> writeRegsiterBased = getWriteRegisters();
		Set<VirtualRegister> result = new HashSet<>();
		for (RegisterBased curr : writeRegsiterBased) {
			result.add((VirtualRegister) curr);
		}
		return result;
	}

	public boolean hasSpilledRegisters() {
		for (RegisterBased register : getReadRegisters()) {
			if (register.isSpilled()) {
				return true;
			}
		}
		for (RegisterBased register : getWriteRegisters()) {
			if (register.isSpilled()) {
				return true;
			}
		}
		return false;
	}

	protected RegisterBundle getSpillRegister() {
		if (accumulatorRegister >= ACCUMULATOR_REGISTERS.length) {
			throw new RuntimeException("Running out of spill registers");
		}
		return ACCUMULATOR_REGISTERS[accumulatorRegister++];
	}

	public void setOperationsBlock(AssemblerOperationsBlock operationsBlock) {
		this.operationsBlock = operationsBlock;
	}

	public AssemblerOperationsBlock getOperationsBlock() {
		return operationsBlock;
	}
}
