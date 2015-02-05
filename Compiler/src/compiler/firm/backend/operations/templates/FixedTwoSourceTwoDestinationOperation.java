package compiler.firm.backend.operations.templates;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.CallOperation;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.PopOperation;
import compiler.firm.backend.operations.PushOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public abstract class FixedTwoSourceTwoDestinationOperation extends AssemblerOperation implements CurrentlyAliveRegistersNeeding {

	private final RegisterBased source1;
	private final RegisterBased source2;
	private final RegisterBased destination1;
	private final RegisterBased destination2;
	private final Set<RegisterBundle> aliveRegisters = new HashSet<>();

	public FixedTwoSourceTwoDestinationOperation(String comment, RegisterBased source1, RegisterBased source2, RegisterBased destination1,
			RegisterBased destination2) {
		super(comment);

		this.source1 = source1;
		if (source1 instanceof VirtualRegister) {
			((VirtualRegister) source1).addPreferedRegister(new VirtualRegister(SingleRegister.RAX));
		}

		this.source2 = source2;

		this.destination1 = destination1;
		if (destination1 != null && destination1 instanceof VirtualRegister) {
			((VirtualRegister) destination1).addPreferedRegister(new VirtualRegister(SingleRegister.RAX));
		}

		this.destination2 = destination2;
		if (destination2 != null && destination2 instanceof VirtualRegister) {
			((VirtualRegister) destination2).addPreferedRegister(new VirtualRegister(SingleRegister.RDX));
		}
	}

	@Override
	public final String getOperationString() {
		return null;
	}

	@Override
	public String[] toStringWithSpillcode() {
		if (destination1 != null && !destination1.isSpilled())
			aliveRegisters.remove(destination1.getRegisterBundle());
		if (destination2 != null && !destination2.isSpilled())
			aliveRegisters.remove(destination2.getRegisterBundle());

		List<String> commandList = new LinkedList<String>();

		int temporaryStackOffset = 0;
		if (aliveRegisters.contains(RegisterBundle._AX)) {
			commandList.add(new PushOperation(SingleRegister.RAX).toString());
			temporaryStackOffset += CallOperation.STACK_ITEM_SIZE;
		}
		boolean source2OnRdx = !source2.isSpilled() && source2.getRegisterBundle() == RegisterBundle._DX;
		Storage usedSource2 = source2;
		if (aliveRegisters.contains(RegisterBundle._DX) || source2OnRdx) {
			commandList.add(new PushOperation(SingleRegister.RDX).toString());
			temporaryStackOffset += CallOperation.STACK_ITEM_SIZE;
			if (source2OnRdx) {
				usedSource2 = new MemoryPointer(0, SingleRegister.RSP);
			}
		}

		source1.setTemporaryStackOffset(temporaryStackOffset);
		commandList.add(new MovOperation(source1, SingleRegister.EAX).toString());
		source1.setTemporaryStackOffset(0);
		source2.setTemporaryStackOffset(temporaryStackOffset);
		commandList.addAll(getOperationString(source2.getMode(), usedSource2));
		source2.setTemporaryStackOffset(0);

		if (source2OnRdx) {
			commandList.add(new AddOperation(new Constant(CallOperation.STACK_ITEM_SIZE), SingleRegister.RSP, SingleRegister.RSP).toString());
		}
		if (destination1 != null) {
			destination1.setTemporaryStackOffset(temporaryStackOffset);
			commandList.addAll(Arrays.asList(new MovOperation(SingleRegister.EAX, destination1).toString()));
			destination1.setTemporaryStackOffset(0);
		}
		if (destination2 != null) {
			destination2.setTemporaryStackOffset(temporaryStackOffset);
			commandList.addAll(Arrays.asList(new MovOperation(SingleRegister.EDX, destination2).toString()));
			destination2.setTemporaryStackOffset(0);
		}

		if (aliveRegisters.contains(RegisterBundle._DX))
			commandList.add(new PopOperation(SingleRegister.RDX).toString());
		if (aliveRegisters.contains(RegisterBundle._AX))
			commandList.add(new PopOperation(SingleRegister.RAX).toString());

		return commandList.toArray(new String[0]);
	}

	public abstract List<String> getOperationString(Bit mode, Storage source2);

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(source1, source2);
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		if (destination1 == null && destination2 == null) {
			return Collections.emptySet();
		} else if (destination1 == null) {
			return Utils.unionSet(destination2);
		} else if (destination2 == null) {
			return Utils.unionSet(destination1);
		} else {
			return Utils.unionSet(destination2, destination1);
		}
	}

	@Override
	public void setAliveRegisters(Set<RegisterBundle> registers) {
		this.aliveRegisters.clear();
		this.aliveRegisters.addAll(registers);
	}
}
