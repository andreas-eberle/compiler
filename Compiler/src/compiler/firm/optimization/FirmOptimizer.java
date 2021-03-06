package compiler.firm.optimization;

import java.util.HashMap;
import java.util.LinkedList;

import compiler.firm.FirmUtils;
import compiler.firm.optimization.evaluation.ProgramDetails;
import compiler.firm.optimization.visitor.CommonSubexpressionEliminationVisitor;
import compiler.firm.optimization.visitor.ConstantFoldingVisitor;
import compiler.firm.optimization.visitor.ControlFlowVisitor;
import compiler.firm.optimization.visitor.LoadStoreOptimiziationVisitor;
import compiler.firm.optimization.visitor.LocalOptimizationVisitor;
import compiler.firm.optimization.visitor.LoopFusionVisitor;
import compiler.firm.optimization.visitor.LoopInvariantVisitor;
import compiler.firm.optimization.visitor.LoopUnrolling;
import compiler.firm.optimization.visitor.NormalizationVisitor;
import compiler.firm.optimization.visitor.OptimizationVisitor;
import compiler.firm.optimization.visitor.OptimizationVisitorFactory;
import compiler.firm.optimization.visitor.StrengthReductionVisitor;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.GraphBase;
import firm.Program;
import firm.bindings.binding_irgraph;
import firm.nodes.Block;
import firm.nodes.Div;
import firm.nodes.Mod;
import firm.nodes.Node;
import firm.nodes.Phi;

public final class FirmOptimizer {
	private FirmOptimizer() {
	}

	public static void optimize() {
		boolean finished;
		do {
			// all optimizations should run on the current graph
			// do not optimize evaluateGraphs out!
			finished = true;
			LoopUnrolling.unrollLoops(evaluateGraphs());
			finished &= ObsoleteNodesEliminator.eliminateObsoleteGraphs(evaluateGraphs());
			finished &= optimize(NormalizationVisitor.FACTORY);
			finished &= optimize(ConstantFoldingVisitor.FACTORY);
			finished &= optimize(LocalOptimizationVisitor.FACTORY);
			finished &= optimize(ControlFlowVisitor.FACTORY);
			finished &= optimize(CommonSubexpressionEliminationVisitor.FACTORY);
			finished &= optimize(LoopInvariantVisitor.FACTORY(evaluateGraphs()));
			finished &= optimize(StrengthReductionVisitor.FACTORY);
			finished &= optimize(LoadStoreOptimiziationVisitor.FACTORY(evaluateGraphs()));
			finished &= ObsoleteNodesEliminator.eliminateObsoleteParameters(evaluateGraphs());
			finished &= ObsoleteNodesEliminator.eliminateObsoleteNodes(evaluateGraphs());
			finished &= MethodInliner.inlineCalls(evaluateGraphs());
			finished &= optimize(LoopFusionVisitor.FACTORY(evaluateGraphs()));
		} while (!finished);
	}

	private static ProgramDetails evaluateGraphs() {
		ProgramDetails programDetails = new ProgramDetails();
		programDetails.updateGraphs(Program.getGraphs());
		return programDetails;
	}

	public static <T> boolean optimize(OptimizationVisitorFactory<T> visitorFactory) {
		boolean finished = true;
		for (Graph graph : Program.getGraphs()) {
			LinkedList<Node> workList = new LinkedList<>();

			OptimizationVisitor<T> visitor = visitorFactory.create();
			BackEdges.enable(graph);
			visitor.init(graph);
			walkTopological(graph, workList, visitor);
			workList(workList, visitor);
			BackEdges.disable(graph);

			HashMap<Node, Node> targetValues = visitor.getNodeReplacements();

			finished &= targetValues.isEmpty();

			compiler.firm.FirmUtils.replaceNodes(targetValues);
			compiler.firm.FirmUtils.removeBadsAndUnreachable(graph);

		}
		return finished;
	}

	public static <T> void walkTopological(Graph graph, LinkedList<Node> workList, OptimizationVisitor<T> visitor) {
		binding_irgraph.inc_irg_visited(graph.ptr);
		walkTopological(graph.getEnd(), workList, visitor);
	}

	/**
	 * Algorithm taken from {@link GraphBase}.walkTopological() and adapted by @author Andreas Eberle
	 * 
	 * @param node
	 * @param visitor
	 */
	private static <T> void walkTopological(Node node, LinkedList<Node> workList, OptimizationVisitor<T> visitor) {
		if (node.visited())
			return;

		/* only break loops at phi/block nodes */
		boolean isLoopBreaker = node.getClass() == Phi.class || node.getClass() == Block.class;
		if (isLoopBreaker) {
			node.markVisited();
		}

		if (node.getBlock() != null) {
			walkTopological(node.getBlock(), workList, visitor);
		}
		for (Node pred : node.getPreds()) {
			walkTopological(pred, workList, visitor);
		}

		if (isLoopBreaker || !node.visited()) {
			visitNode(node, workList, visitor);
		}
		node.markVisited();
	}

	private static <T> void visitNode(Node node, LinkedList<Node> workList, OptimizationVisitor<T> visitor) {
		HashMap<Node, T> targetValues = visitor.getLatticeValues();
		T oldTarget = targetValues.get(node);
		node.accept(visitor);
		T newTarget = targetValues.get(node);

		if (oldTarget == null || !oldTarget.equals(newTarget)) {
			for (Edge e : BackEdges.getOuts(node)) {
				workList.push(e.node);
			}
		}
	}

	public static <T> void workList(LinkedList<Node> workList, OptimizationVisitor<T> visitor) {
		while (!workList.isEmpty()) {
			Node node = workList.pop();
			T oldTarget = getTarget(visitor, node);
			node.accept(visitor);
			T newTarget = getTarget(visitor, node);

			if (oldTarget != null && !oldTarget.equals(newTarget)) {
				for (Edge e : BackEdges.getOuts(node)) {
					workList.push(e.node);
				}
			}
		}
	}

	private static <T> T getTarget(OptimizationVisitor<T> visitor, Node node) {
		HashMap<Node, T> targetValues = visitor.getLatticeValues();
		if (node instanceof Div || node instanceof Mod) {
			// if div/mod changed _all_ successors changed
			// it is enough to check the first one
			return targetValues.get(FirmUtils.getFirstSuccessor(node));
		} else {
			return targetValues.get(node);
		}
	}

}
