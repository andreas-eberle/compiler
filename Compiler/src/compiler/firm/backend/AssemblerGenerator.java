package compiler.firm.backend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import compiler.ast.declaration.MainMethodDeclaration;
import compiler.firm.backend.FirmGraphTraverser.BlockInfo;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.FunctionSpecificationOperation;
import compiler.firm.backend.operations.P2AlignOperation;
import compiler.firm.backend.operations.TextOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.registerallocation.InterferenceGraph;
import compiler.firm.backend.registerallocation.LinearScanRegisterAllocation;

import firm.BackEdges;
import firm.BlockWalker;
import firm.Graph;
import firm.Program;
import firm.nodes.Block;

public final class AssemblerGenerator {

	private AssemblerGenerator() {
	}

	public static void createAssemblerX8664(Path outputFile, final CallingConvention callingConvention, boolean doPeephole, boolean noRegisters,
			boolean debugRegisterAllocation) throws IOException {
		InterferenceGraph.setDebuggingMode(debugRegisterAllocation);

		final ArrayList<AssemblerOperation> assembler = new ArrayList<>();

		assembler.add(new TextOperation());
		assembler.add(new P2AlignOperation());

		for (Graph graph : Program.getGraphs()) {
			BackEdges.enable(graph);
			graph.walk(new InsertBlockAfterConditionVisitor());
			BackEdges.disable(graph);
			assembler.add(new FunctionSpecificationOperation(graph.getEntity().getLdName()));
		}

		for (Graph graph : Program.getGraphs()) {
			if (debugRegisterAllocation)
				System.out.println(graph.getEntity().getLdName());

			BlockNodesCollectingVisitor collectorVisitor = new BlockNodesCollectingVisitor();
			graph.walkTopological(collectorVisitor);

			// final NodeNumberPrintingVisitor printer = new NodeNumberPrintingVisitor();

			HashMap<Block, BlockNodes> nodesPerBlockMap = collectorVisitor.getNodesPerBlockMap();
			X8664AssemblerGenerationVisitor visitor = new X8664AssemblerGenerationVisitor(callingConvention);

			BackEdges.enable(graph);
			HashMap<Block, BlockInfo> blockInfos = FirmGraphTraverser.calculateBlockInfos(graph);
			FirmGraphTraverser.walkBlocksAllocationFriendly(graph, blockInfos, new BlockNodesWalker(visitor, nodesPerBlockMap));
			BackEdges.disable(graph);

			visitor.finishOperationsList();
			ArrayList<AssemblerOperation> operationsBlocksPostOrder = visitor.getAllOperations();
			final HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks = visitor.getOperationsOfBlocks();

			calculateLiveness(graph, operationsOfBlocks);

			if (debugRegisterAllocation)
				generatePlainAssemblerFile(Paths.get(graph.getEntity().getLdName() + ".plain"), operationsBlocksPostOrder);

			allocateRegisters(graph, operationsBlocksPostOrder, noRegisters, debugRegisterAllocation);

			operationsBlocksPostOrder.clear(); // free some memory

			ArrayList<AssemblerOperation> operationsList = generateOperationsList(graph, blockInfos, operationsOfBlocks);

			if (doPeephole) {
				PeepholeOptimizer peepholeOptimizer = new PeepholeOptimizer(operationsList, assembler);
				peepholeOptimizer.optimize();
			} else {
				assembler.addAll(operationsList);
			}
		}

		generateAssemblerFile(outputFile, assembler);
	}

	private static void allocateRegisters(Graph graph, ArrayList<AssemblerOperation> operationsBlocksPostOrder, boolean noRegisters,
			boolean debugRegisterAllocation) {
		boolean isMain = MainMethodDeclaration.MAIN_METHOD_NAME.equals(graph.getEntity().getLdName());

		new LinearScanRegisterAllocation(isMain, operationsBlocksPostOrder).allocateRegisters(debugRegisterAllocation, noRegisters);
	}

	private static ArrayList<AssemblerOperation> generateOperationsList(Graph graph, HashMap<Block, BlockInfo> blockInfos,
			final HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
		final ArrayList<AssemblerOperation> operationsList = new ArrayList<>();

		FirmGraphTraverser.walkLoopOptimizedPostorder(graph, blockInfos, new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				operationsList.addAll(operationsOfBlocks.get(block));
			}
		});

		return operationsList;
	}

	private static void generateAssemblerFile(Path outputFile, List<AssemblerOperation> assembler) throws IOException {
		BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.US_ASCII);

		for (AssemblerOperation operation : assembler) {
			for (String operationString : operation.toStringWithSpillcode()) {
				writer.write(operationString);
				writer.newLine();
			}
		}
		writer.close();
	}

	private static void generatePlainAssemblerFile(Path outputFile, List<AssemblerOperation> operations) {
		try {
			BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.US_ASCII);
			for (AssemblerOperation operation : operations) {
				writer.write(operation.toString() + " # r:" + operation.getReadRegisters() + "; w:" + operation.getWriteRegisters());
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class BlockNodesWalker implements BlockWalker {
		private final BulkPhiNodeVisitor visitor;
		private final HashMap<Block, BlockNodes> nodesPerBlockMap;

		public BlockNodesWalker(BulkPhiNodeVisitor visitor, HashMap<Block, BlockNodes> nodesPerBlockMap) {
			this.visitor = visitor;
			this.nodesPerBlockMap = nodesPerBlockMap;
		}

		@Override
		public void visitBlock(Block block) {
			nodesPerBlockMap.get(block).visitNodes(visitor, nodesPerBlockMap);
		}
	}

	private static void calculateLiveness(Graph graph, HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
		final HashMap<Block, AssemblerOperationsBlock> operationsBlocks = new HashMap<>();
		for (Entry<Block, ArrayList<AssemblerOperation>> entry : operationsOfBlocks.entrySet()) {
			operationsBlocks.put(entry.getKey(), new AssemblerOperationsBlock(entry.getKey(), entry.getValue()));

		}

		final LinkedList<AssemblerOperationsBlock> workList = new LinkedList<>();

		graph.walkBlocks(new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				AssemblerOperationsBlock operationsBlock = operationsBlocks.get(block);
				if (operationsBlock != null) {
					operationsBlock.calculateTree(operationsBlocks);
					operationsBlock.calculateUsesAndKills();
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

		for (Entry<Block, AssemblerOperationsBlock> entry : operationsBlocks.entrySet()) {
			System.out.println(entry.getValue());
		}
	}
}
