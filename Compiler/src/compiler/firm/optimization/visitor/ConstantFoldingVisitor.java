package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import compiler.firm.optimization.FirmOptimizer;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.TargetValue;
import firm.nodes.Add;
import firm.nodes.And;
import firm.nodes.Const;
import firm.nodes.Conv;
import firm.nodes.Div;
import firm.nodes.Minus;
import firm.nodes.Mod;
import firm.nodes.Mul;
import firm.nodes.Node;
import firm.nodes.Not;
import firm.nodes.Or;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Shl;
import firm.nodes.Shr;
import firm.nodes.Shrs;
import firm.nodes.Sub;

public class ConstantFoldingVisitor extends OptimizationVisitor<TargetValue> {

	protected boolean fixPoint = true;

	public static final OptimizationVisitorFactory<TargetValue> FACTORY = new OptimizationVisitorFactory<TargetValue>() {
		@Override
		public OptimizationVisitor<TargetValue> create() {
			return new ConstantFoldingVisitor();
		}
	};

	protected HashMap<Node, TargetValue> targets = new HashMap<>();

	@Override
	public HashMap<Node, TargetValue> getLatticeValues() {
		return targets;
	}

	@Override
	public HashMap<Node, Node> getNodeReplacements() {
		HashMap<Node, Node> replacements = new HashMap<>();
		LinkedList<Node> workList = new LinkedList<>();
		ConstantFoldingFixpointVisitor visitor = new ConstantFoldingFixpointVisitor(targets, workList);
		Graph currentGraph = null;

		if (!targets.isEmpty()) {
			currentGraph = targets.keySet().iterator().next().getGraph();

			BackEdges.enable(currentGraph);
			do {
				visitor.fixPoint = true;
				FirmOptimizer.walkTopological(currentGraph, workList, visitor);
				FirmOptimizer.workList(workList, visitor);
			} while (!visitor.fixPoint);
			BackEdges.disable(currentGraph);

			for (Entry<Node, TargetValue> targetEntry : targets.entrySet()) {
				Node node = targetEntry.getKey();
				TargetValue target = targetEntry.getValue();
				if (target.isConstant() && !(node instanceof Const) && !(node instanceof Conv)) {
					replacements.put(node, currentGraph.newConst(target));
				}
			}
		}
		return replacements;
	}

	private void setTargetValue(Node node, TargetValue targetValue) {
		if (!targets.containsKey(node) || !targets.get(node).equals(targetValue)) {
			fixPoint = false;
		}
		targets.put(node, targetValue);
	}

	private TargetValue getTargetValue(Node node) {
		return targets.get(node) == null ? TargetValue.getUnknown() : targets.get(node);
	}

	private void binaryTransferFunction(Node node, TargetValue leftTarget, TargetValue rightTarget, TargetValue newTargetValue) {
		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(node, newTargetValue);
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(node, TargetValue.getBad());
		} else {
			setTargetValue(node, TargetValue.getUnknown());
		}
	}

	private void unaryTransferFunction(Node node, TargetValue target, TargetValue newTargetValue) {
		if (target.isConstant()) {
			setTargetValue(node, newTargetValue);
		} else if (target.equals(TargetValue.getBad())) {
			setTargetValue(node, TargetValue.getBad());
		} else {
			setTargetValue(node, TargetValue.getUnknown());
		}
	}

	private void divTransferFunction(Node node, TargetValue leftTarget, TargetValue rightTarget, TargetValue newTargetValue) {
		if (leftTarget.isNull()) {
			for (Edge suc : BackEdges.getOuts(node)) {
				setTargetValue(suc.node, leftTarget);
			}
		} else if (leftTarget.isConstant() && rightTarget.isConstant()) {
			for (Edge suc : BackEdges.getOuts(node)) {
				setTargetValue(suc.node, newTargetValue);
			}
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			for (Edge suc : BackEdges.getOuts(node)) {
				setTargetValue(suc.node, TargetValue.getBad());
			}
		} else {
			for (Edge suc : BackEdges.getOuts(node)) {
				setTargetValue(suc.node, TargetValue.getUnknown());
			}
		}
	}

	private void modTransferFunction(Node node, TargetValue leftTarget, TargetValue rightTarget, TargetValue newTargetValue) {
		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			for (Edge suc : BackEdges.getOuts(node)) {
				setTargetValue(suc.node, newTargetValue);
			}
		} else if (leftTarget.isNull()) {
			for (Edge suc : BackEdges.getOuts(node)) {
				setTargetValue(suc.node, new TargetValue(0, leftTarget.getMode()));
			}
		} else if (leftTarget.isOne()) {
			for (Edge suc : BackEdges.getOuts(node)) {
				setTargetValue(suc.node, new TargetValue(0, leftTarget.getMode()));
			}
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			for (Edge suc : BackEdges.getOuts(node)) {
				setTargetValue(suc.node, TargetValue.getBad());
			}
		} else {
			for (Edge suc : BackEdges.getOuts(node)) {
				setTargetValue(suc.node, TargetValue.getUnknown());
			}
		}
	}

	private void mulTransferFunction(Node node, TargetValue leftTarget, TargetValue rightTarget, TargetValue newTargetValue) {
		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(node, newTargetValue);
		} else if (leftTarget.isNull()) {
			setTargetValue(node, new TargetValue(0, node.getMode()));
		} else if (rightTarget.isNull()) {
			setTargetValue(node, new TargetValue(0, node.getMode()));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(node, TargetValue.getBad());
		} else {
			setTargetValue(node, TargetValue.getUnknown());
		}
	}

	@Override
	public void visit(Add add) {
		TargetValue leftTarget = getTargetValue(add.getLeft());
		TargetValue rightTarget = getTargetValue(add.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.add(rightTarget) : TargetValue.getUnknown();

		binaryTransferFunction(add, leftTarget, rightTarget, newTargetValue);
	}

	@Override
	public void visit(And and) {
		TargetValue leftTarget = getTargetValue(and.getLeft());
		TargetValue rightTarget = getTargetValue(and.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.and(rightTarget) : TargetValue.getUnknown();

		binaryTransferFunction(and, leftTarget, rightTarget, newTargetValue);
	}

	@Override
	public void visit(Const constant) {
		setTargetValue(constant, constant.getTarval());
	}

	@Override
	public void visit(Conv conversion) {
		TargetValue target = getTargetValue(conversion.getOp());
		TargetValue newTargetValue;

		if (target.isLong() && target.isNegative()) { // Do not touch negative conversions
			newTargetValue = TargetValue.getUnknown();
		} else {
			newTargetValue = target.convertTo(conversion.getMode());
		}

		unaryTransferFunction(conversion, target, newTargetValue);
	}

	@Override
	public void visit(Div division) {
		// // firm automatically skips exchanging the node if right target is null
		TargetValue leftTargetValue = getTargetValue(division.getLeft()) == null ? TargetValue.getUnknown() : getTargetValue(division.getLeft());
		TargetValue rightTargetValue = getTargetValue(division.getRight());
		TargetValue newTargetValue = (leftTargetValue.isConstant() && rightTargetValue.isConstant()) ? leftTargetValue.div(rightTargetValue)
				: TargetValue.getUnknown();

		divTransferFunction(division, leftTargetValue, rightTargetValue, newTargetValue);
	}

	@Override
	public void visit(Minus minus) {
		TargetValue target = getTargetValue(minus.getOp());
		TargetValue newTargetValue = (!target.isConstant()) ? TargetValue.getUnknown() : target.neg();

		unaryTransferFunction(minus, target, newTargetValue);
	}

	@Override
	public void visit(Mod mod) {
		// // firm automatically skips exchanging the node if right target is null
		TargetValue leftTargetValue = getTargetValue(mod.getLeft()) == null ? TargetValue.getUnknown() : getTargetValue(mod.getLeft());
		TargetValue rightTargetValue = getTargetValue(mod.getRight());
		TargetValue newTargetValue = (leftTargetValue.isConstant() && rightTargetValue.isConstant()) ? leftTargetValue.mod(rightTargetValue)
				: TargetValue.getUnknown();

		modTransferFunction(mod, leftTargetValue, rightTargetValue, newTargetValue);
	}

	@Override
	public void visit(Mul multiplication) {
		TargetValue leftTarget = getTargetValue(multiplication.getLeft());
		TargetValue rightTarget = getTargetValue(multiplication.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.mul(rightTarget) : TargetValue.getUnknown();

		mulTransferFunction(multiplication, leftTarget, rightTarget, newTargetValue);
	}

	@Override
	public void visit(Not not) {
		TargetValue target = getTargetValue(not.getOp());
		TargetValue newTargetValue = (!target.isConstant()) ? TargetValue.getUnknown() : target.not();

		unaryTransferFunction(not, target, newTargetValue);
	}

	@Override
	public void visit(Or or) {
		TargetValue leftTarget = getTargetValue(or.getLeft());
		TargetValue rightTarget = getTargetValue(or.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.or(rightTarget) : TargetValue.getUnknown();

		binaryTransferFunction(or, leftTarget, rightTarget, newTargetValue);
	}

	@Override
	public void visit(Phi phi) {
		if (phi.getPredCount() < 1) {
			// skip memory phi's that connect to an already removed node
			return;
		}
		TargetValue predTargetValue = getTargetValue(phi.getPred(0)) == null ? TargetValue.getUnknown() : getTargetValue(phi.getPred(0));

		for (int i = 1; i < phi.getPredCount(); i++) {
			TargetValue tmpTargetValue = getTargetValue(phi.getPred(i)) == null ? TargetValue.getUnknown() : getTargetValue(phi.getPred(i));
			if (predTargetValue.isConstant() && tmpTargetValue.isConstant() && predTargetValue.equals(tmpTargetValue)) {
				// same constants as predecessors
				setTargetValue(phi, predTargetValue);
			} else if (predTargetValue.equals(TargetValue.getBad()) || tmpTargetValue.equals(TargetValue.getBad())
					|| (predTargetValue.isConstant() && tmpTargetValue.isConstant() && !predTargetValue.equals(tmpTargetValue))) {
				// node already bad, no chance to recover
				setTargetValue(phi, TargetValue.getBad());
				break;
			} else if (tmpTargetValue.equals(TargetValue.getUnknown())) {
				// predTargetValue is unknown or constant
				setTargetValue(phi, predTargetValue);
			} else {
				// predTargetValue is unknown and tmpTargetValue is a constant
				// save it
				setTargetValue(phi, tmpTargetValue);
				predTargetValue = tmpTargetValue;
			}
		}
	}

	@Override
	public void visit(Proj proj) {
		TargetValue target = getTargetValue(proj);
		if (target == null) {
			setTargetValue(proj, TargetValue.getUnknown());
		}
	}

	@Override
	public void visit(Shl shl) {
		TargetValue leftTarget = getTargetValue(shl.getLeft());
		TargetValue rightTarget = getTargetValue(shl.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.shl(rightTarget) : TargetValue.getUnknown();

		binaryTransferFunction(shl, leftTarget, rightTarget, newTargetValue);
	}

	@Override
	public void visit(Shr shr) {
		TargetValue leftTarget = getTargetValue(shr.getLeft());
		TargetValue rightTarget = getTargetValue(shr.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.shr(rightTarget) : TargetValue.getUnknown();

		binaryTransferFunction(shr, leftTarget, rightTarget, newTargetValue);
	}

	@Override
	public void visit(Shrs shrs) {
		TargetValue leftTarget = getTargetValue(shrs.getLeft());
		TargetValue rightTarget = getTargetValue(shrs.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.shrs(rightTarget) : TargetValue.getUnknown();

		binaryTransferFunction(shrs, leftTarget, rightTarget, newTargetValue);
	}

	@Override
	public void visit(Sub sub) {
		TargetValue leftTarget = getTargetValue(sub.getLeft());
		TargetValue rightTarget = getTargetValue(sub.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.sub(rightTarget, sub.getMode()) : TargetValue
				.getUnknown();

		binaryTransferFunction(sub, leftTarget, rightTarget, newTargetValue);
	}

	private class ConstantFoldingFixpointVisitor extends ConstantFoldingVisitor {

		private ConstantFoldingFixpointVisitor(HashMap<Node, TargetValue> targets, LinkedList<Node> workList) {
			super();
			this.targets = targets;
		}

		@Override
		public void visit(Phi phi) {
			if (phi.getPredCount() < 1) {
				// skip memory phi's that connect to an already removed node
				return;
			}
			TargetValue predTargetValue = getTargetValue(phi.getPred(0)) == null ? TargetValue.getUnknown() : getTargetValue(phi.getPred(0));

			for (int i = 1; i < phi.getPredCount(); i++) {
				TargetValue tmpTargetValue = getTargetValue(phi.getPred(i)) == null ? TargetValue.getUnknown() : getTargetValue(phi.getPred(i));
				if (predTargetValue.isConstant() && tmpTargetValue.isConstant() && predTargetValue.equals(tmpTargetValue)) {
					// both are a constant
					setTargetValue(phi, predTargetValue);
				} else if (predTargetValue.equals(TargetValue.getBad()) || tmpTargetValue.equals(TargetValue.getBad())
						|| (predTargetValue.isConstant() && tmpTargetValue.isConstant() && !predTargetValue.equals(tmpTargetValue))) {
					// at least one is no constant
					setTargetValue(phi, TargetValue.getBad());
					break;
				} else {
					// at least one is unknown
					setTargetValue(phi, TargetValue.getUnknown());
					break;
				}
			}
		}
	}

}
