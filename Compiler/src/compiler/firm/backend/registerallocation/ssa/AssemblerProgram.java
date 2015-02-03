package compiler.firm.backend.registerallocation.ssa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.backend.operations.dummy.MethodStartEndOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.registerallocation.RegisterAllocationPolicy;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;

import firm.BlockWalker;
import firm.Graph;
import firm.nodes.Block;

public class AssemblerProgram {
	private final HashMap<Block, AssemblerOperationsBlock> operationsBlocks;
	private final Set<VirtualRegister> preAllocatedRegisters = new HashSet<>();

	public AssemblerProgram(Graph graph, HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
		this.operationsBlocks = createOperationsBlocks(operationsOfBlocks);
		calculateLiveInAndLiveOut(graph);
	}

	private void calculateLiveInAndLiveOut(Graph graph) {
		final LinkedList<AssemblerOperationsBlock> workList = new LinkedList<>();

		graph.walkBlocks(new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				AssemblerOperationsBlock operationsBlock = operationsBlocks.get(block);
				if (operationsBlock != null) {
					operationsBlock.calculateTree(operationsBlocks);
					operationsBlock.calculateUsesAndKills();
					preAllocatedRegisters.addAll(operationsBlock.calculatePreallocatedRegisters());
					workList.add(operationsBlock);
				}
			}
		});

		while (!workList.isEmpty()) {
			AssemblerOperationsBlock operationsBlock = workList.removeLast();
			if (operationsBlock.calculateLiveInAndOut()) {
				workList.addAll(operationsBlock.getPredecessors());
			}
		}

		if (SsaRegisterAllocator.DEBUG) {
			for (Entry<Block, AssemblerOperationsBlock> entry : operationsBlocks.entrySet()) {
				System.out.println(entry.getValue());
			}
		}
	}

	private static HashMap<Block, AssemblerOperationsBlock> createOperationsBlocks(HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
		HashMap<Block, AssemblerOperationsBlock> operationsBlocks = new HashMap<>();

		for (Entry<Block, ArrayList<AssemblerOperation>> entry : operationsOfBlocks.entrySet()) {
			operationsBlocks.put(entry.getKey(), new AssemblerOperationsBlock(entry.getKey(), entry.getValue()));
		}

		return operationsBlocks;
	}

	public AssemblerOperationsBlock getOperationsBlock(Block block) {
		return operationsBlocks.get(block);
	}

	public void setDummyOperationsInformation(Set<RegisterBundle> usedRegisters, int stackSize, boolean isMainMethod,
			RegisterAllocationPolicy policy) {
		if (stackSize > 0) {
			stackSize += 0x10;
			stackSize &= -0x10; // Align to 8-byte.
		} else {
			stackSize = 0;
		}

		for (Entry<Block, AssemblerOperationsBlock> curr : operationsBlocks.entrySet()) {
			ArrayList<AssemblerOperation> operations = curr.getValue().getOperations();
			for (AssemblerOperation operation : operations) {
				if (operation instanceof MethodStartEndOperation) {
					MethodStartEndOperation methodStartEndOperation = (MethodStartEndOperation) operation;
					methodStartEndOperation.setStackOperationSize(stackSize);

					if (isMainMethod) { // if it is the main, no registers need to be saved
						methodStartEndOperation.setUsedRegisters(new HashSet<RegisterBundle>());
					} else {
						methodStartEndOperation.setUsedRegisters(usedRegisters);
					}
				}
			}
		}
	}

	public Set<RegisterBundle> getInterferringPreallocatedBundles(VirtualRegister virtualRegister) {
		Set<RegisterBundle> interferringBundles = new HashSet<>();

		for (VirtualRegister preallocated : preAllocatedRegisters) {
			if (doVariablesInterfere(virtualRegister, preallocated)) {
				interferringBundles.add(preallocated.getRegisterBundle());
			}
		}

		return interferringBundles;
	}

	/**
	 * @see Algorithm 4.6 (page 69) of thesis on SSA Register Allocation.
	 * 
	 * @param register1
	 * @param register2
	 * @return
	 */
	private static boolean doVariablesInterfere(VirtualRegister register1, VirtualRegister register2) {
		AssemblerOperation definition1 = register1.getDefinition();
		AssemblerOperation definition2 = register2.getDefinition();

		VirtualRegister dominating;
		VirtualRegister dominated;

		if (dominates(definition1, definition2)) {
			dominating = register1;
			dominated = register2;
		} else if (dominates(definition2, definition1)) {
			dominating = register2;
			dominated = register1;
		} else {
			return false;
		}

		if (dominated.getDefinition().getOperationsBlock().getLiveOut().contains(dominating))
			return true;

		for (AssemblerOperation usage : dominating.getUsages()) {
			if (dominates(dominated.getDefinition(), usage)) {
				return true; // if the definition of the dominated dominates a usage of the dominating, they interfere
			}
		}

		return false;
	}

	private static boolean dominates(AssemblerOperation operation1, AssemblerOperation operation2) {
		AssemblerOperationsBlock operationsBlock1 = operation1.getOperationsBlock();
		AssemblerOperationsBlock operationsBlock2 = operation2.getOperationsBlock();

		if (operationsBlock1 == operationsBlock2) {
			return operationsBlock1.strictlyDominates(operation1, operation2);
		} else {
			return operationsBlock1.dominates(operationsBlock2);
		}
	}

}
