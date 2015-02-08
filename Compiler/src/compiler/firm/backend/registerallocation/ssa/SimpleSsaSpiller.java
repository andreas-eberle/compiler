package compiler.firm.backend.registerallocation.ssa;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.backend.X8664AssemblerGenerationVisitor;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

import firm.BlockWalker;
import firm.nodes.Block;

public class SimpleSsaSpiller {

	private static final boolean DEBUG_SPILLED_REGISTERS = false;

	private final AssemblerProgram program;

	private int currentStackOffset = 0;

	public SimpleSsaSpiller(AssemblerProgram program) {
		this.program = program;
	}

	public void reduceRegisterPressure(final int availableRegisters, final boolean allowSpilling) {
		currentStackOffset = 0; // reset state

		FirmUtils.walkDominanceTree(program.getStartBlock(), new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				reduceRegisterPressure(block, availableRegisters, allowSpilling);
			}
		});
	}

	private void reduceRegisterPressure(Block block, int availableRegisters, boolean allowSpilling) {
		AssemblerOperationsBlock operationsBlock = program.getOperationsBlock(block);
		if (operationsBlock == null) {
			return;
		}

		Set<VirtualRegister> aliveRegisters = new HashSet<>(operationsBlock.getLiveIn());
		for (Iterator<VirtualRegister> iterator = aliveRegisters.iterator(); iterator.hasNext();) {
			if (iterator.next().isSpilled()) {
				iterator.remove();
			}
		}

		for (AssemblerOperation operation : operationsBlock.getOperations()) {
			for (VirtualRegister readRegister : operation.getVirtualReadRegisters()) {
				if (operationsBlock.isLastUsage(readRegister, operation)) {
					aliveRegisters.remove(readRegister);
				}
			}

			for (RegisterBased writeRegisterBased : operation.getWriteRegisters()) {
				VirtualRegister writeRegister = (VirtualRegister) writeRegisterBased;
				if (writeRegister.getRegister() == null && !writeRegister.isSpilled()) {
					aliveRegisters.add(writeRegister);

					if (aliveRegisters.size() > availableRegisters) {
						if (!allowSpilling) {
							throw new MustSpillException();
						}
						spillRegisterOf(aliveRegisters);
					}
				}
			}
		}
	}

	private void spillRegisterOf(Set<VirtualRegister> aliveRegisters) {
		VirtualRegister toBeSpilled = aliveRegisters.iterator().next();
		for (VirtualRegister curr : aliveRegisters) {
			if (toBeSpilled.getUsages().size() < curr.getUsages().size()) {
				toBeSpilled = curr;
			}
		}

		Utils.debugln(DEBUG_SPILLED_REGISTERS, "spilling register: VR_" + toBeSpilled.getNum());
		aliveRegisters.remove(toBeSpilled);
		spillRegister(toBeSpilled);
	}

	private void spillRegister(VirtualRegister spilledRegister) {
		spilledRegister.setSpilled(true);
		currentStackOffset += X8664AssemblerGenerationVisitor.STACK_ITEM_SIZE;
		spilledRegister.setStorage(new MemoryPointer(currentStackOffset, SingleRegister.RSP));
	}

	public int getCurrentStackOffset() {
		return currentStackOffset;
	}
}
