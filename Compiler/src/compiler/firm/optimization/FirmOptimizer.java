package compiler.firm.optimization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import compiler.firm.optimization.visitor.CommonSubexpressionEliminationVisitor;
import compiler.firm.optimization.visitor.ConstantFoldingVisitor;
import compiler.firm.optimization.visitor.ControlFlowVisitor;
import compiler.firm.optimization.visitor.FunctionInliningVisitor;
import compiler.firm.optimization.visitor.LocalOptimizationVisitor;
import compiler.firm.optimization.visitor.LoopInvariantVisitor;
import compiler.firm.optimization.visitor.NormalizationVisitor;
import compiler.firm.optimization.visitor.OptimizationVisitor;
import compiler.firm.optimization.visitor.OptimizationVisitorFactory;
import compiler.firm.optimization.visitor.StrengthReductionVisitor;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.GraphBase;
import firm.Mode;
import firm.Program;
import firm.bindings.binding_irgopt;
import firm.bindings.binding_irgraph;
import firm.nodes.Block;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Return;

public final class FirmOptimizer {
	private FirmOptimizer() {
	}

	public static void optimize() {
		boolean finished;
		do {
			HashMap<Graph, GraphDetails> graphDetails = evaluateGraphs();
			finished = true;
			finished &= optimize(NormalizationVisitor.FACTORY);
			finished &= optimize(ConstantFoldingVisitor.FACTORY);
			finished &= optimize(LocalOptimizationVisitor.FACTORY);
			finished &= optimize(ControlFlowVisitor.FACTORY);
			finished &= optimize(CommonSubexpressionEliminationVisitor.FACTORY);
			finished &= optimize(LoopInvariantVisitor.FACTORY(graphDetails));
			finished &= optimize(StrengthReductionVisitor.FACTORY);
			finished &= optimize(FunctionInliningVisitor.FACTORY);
		} while (!finished);
	}

	private static HashMap<Graph, GraphDetails> evaluateGraphs() {
		HashMap<Graph, GraphDetails> result = new HashMap<>();

		for (Graph graph : Program.getGraphs()) {
			BackEdges.enable(graph);
			result.put(graph, new GraphDetails(hasSideEffects(graph)));
			BackEdges.disable(graph);
		}

		return result;
	}

	private static boolean hasSideEffects(Graph graph) {
		for (Edge startFollower : BackEdges.getOuts(graph.getStart())) {
			if (startFollower.node.getMode().equals(Mode.getM())) {
				Proj projM = (Proj) startFollower.node;

				if (BackEdges.getNOuts(projM) == 1) {
					Edge projMFollower = BackEdges.getOuts(projM).iterator().next();
					if (projMFollower.node instanceof Return) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public static <T> boolean optimize(OptimizationVisitorFactory<T> visitorFactory) {
		boolean finished = true;
		for (Graph graph : Program.getGraphs()) {
			LinkedList<Node> workList = new LinkedList<>();

			OptimizationVisitor<T> visitor = visitorFactory.create();

			BackEdges.enable(graph);
			walkTopological(graph, workList, visitor);
			workList(workList, visitor);
			BackEdges.disable(graph);

			HashMap<Node, Node> targetValues = visitor.getNodeReplacements();

			finished &= targetValues.isEmpty();

			replaceNodesWithTargets(graph, targetValues);

			binding_irgopt.remove_unreachable_code(graph.ptr);
			binding_irgopt.remove_bads(graph.ptr);
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
			node.accept(visitor);
		}
	}

	private static void replaceNodesWithTargets(Graph graph, HashMap<Node, Node> targetValuesMap) {
		for (Entry<Node, Node> targetEntry : targetValuesMap.entrySet()) {
			Node node = targetEntry.getKey();
			Node replacement = targetEntry.getValue();

			if (node.getPredCount() > 0 && !node.equals(replacement)) {
				Graph.exchange(node, replacement);
			}
		}
	}
}
