package compiler.firm.optimization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import compiler.firm.optimization.visitor.ArithmeticVisitor;
import compiler.firm.optimization.visitor.ControlFlowVisitor;
import compiler.firm.optimization.visitor.OptimizationVisitor;
import compiler.firm.optimization.visitor.OptimizationVisitorFactory;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.GraphBase;
import firm.Program;
import firm.bindings.binding_irgopt;
import firm.bindings.binding_irgraph;
import firm.nodes.Block;
import firm.nodes.Node;
import firm.nodes.Phi;

public final class FirmOptimizer {
	private FirmOptimizer() {
	}

	public static void optimize() {
		boolean finished = true;
		do {
			finished = true;
			// finished &= optimize(ConstantFoldingVisitor.getFactory());
			finished &= optimize(ArithmeticVisitor.getFactory());
			finished &= optimize(ControlFlowVisitor.getFactory());
		} while (!finished);
	}

	public static boolean optimize(OptimizationVisitorFactory visitorFactory) {
		boolean finished = true;
		for (Graph graph : Program.getGraphs()) {
			LinkedList<Node> workList = new LinkedList<>();

			OptimizationVisitor visitor = visitorFactory.create();

			BackEdges.enable(graph);
			walkTopological(graph, workList, visitor);
			// graph.walkTopological(visitor);
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

	private static void walkTopological(Graph graph, LinkedList<Node> workList, OptimizationVisitor visitor) {
		binding_irgraph.inc_irg_visited(graph.ptr);
		walkTopological(graph.getEnd(), workList, visitor);
	}

	/**
	 * Algorithm taken from {@link GraphBase}.walkTopological() and adapted by @author Andreas Eberle
	 * 
	 * @param node
	 * @param visitor
	 */
	private static void walkTopological(Node node, LinkedList<Node> workList, OptimizationVisitor visitor) {
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

	private static void visitNode(Node node, LinkedList<Node> workList, OptimizationVisitor visitor) {
		HashMap<Node, Node> targetValues = visitor.getNodeReplacements();
		Node oldTarget = targetValues.get(node);
		node.accept(visitor);
		Node newTarget = targetValues.get(node);

		if (oldTarget == null || !oldTarget.equals(newTarget)) {
			for (Edge e : BackEdges.getOuts(node)) {
				workList.push(e.node);
			}
		}
	}

	private static void workList(LinkedList<Node> workList, OptimizationVisitor visitor) {
		while (!workList.isEmpty()) {
			Node node = workList.pop();
			node.accept(visitor);
		}
	}

	private static void replaceNodesWithTargets(Graph graph, HashMap<Node, Node> targetValuesMap) {
		for (Entry<Node, Node> targetEntry : targetValuesMap.entrySet()) {
			Node node = targetEntry.getKey();

			if (node.getPredCount() > 0) {
				Graph.exchange(node, targetEntry.getValue());
			}
		}
	}
}
