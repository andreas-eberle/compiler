package compiler.firm.optimization.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import firm.nodes.Add;
import firm.nodes.Block;
import firm.nodes.Conv;
import firm.nodes.Minus;
import firm.nodes.Mul;
import firm.nodes.Node;
import firm.nodes.Not;
import firm.nodes.Shl;
import firm.nodes.Shr;
import firm.nodes.Shrs;
import firm.nodes.Start;
import firm.nodes.Sub;

public class LoopInvariantVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new LoopInvariantVisitor();
		}
	};

	private HashMap<Node, Node> backedges = new HashMap<>();
	private HashMap<Block, Set<Block>> dominators = new HashMap<>();

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return nodeReplacements;
	}

	private Node getInnerMostLoopHeader(Block block) {
		Set<Block> dominatorBlocks = dominators.get(block);
		Set<Block> loops = new HashSet<>();
		for (Block dominatorBlock : dominatorBlocks) {
			// find loop header that dominates 'block'
			if (!dominatorBlock.equals(block) && backedges.containsValue(dominatorBlock)) {
				loops.add(dominatorBlock);
			}
		}

		ArrayList<Block> sameLevelLoops = new ArrayList<>();
		L1: for (Block b : loops) {
			if (dominators.containsKey(b) && dominators.get(b).containsAll(loops)) {
				for (Map.Entry<Node, Node> entry : backedges.entrySet()) {
					if (entry.getValue().equals(b)) {
						if (dominators.containsKey((Block) entry.getKey()) && !dominators.get((Block) entry.getKey()).contains(block)
								&& !dominatorBlocks.contains(entry.getKey())) {
							// b and the looṕ header are on the same 'level'
							sameLevelLoops.add(b);
							continue L1;
						}
					}
				}
			}
		}
		for (Block b : sameLevelLoops) {
			loops.remove(b);
		}
		for (Block b : loops) {
			if (dominators.containsKey(b) && dominators.get(b).containsAll(loops)) {
				return b;
			}
		}
		return null;
	}

	@Override
	public void visit(Add add) {
		Add node = (Add) (nodeReplacements.containsKey(add) ? nodeReplacements.get(add) : add);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				Node preLoopBlock = pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newAdd(preLoopBlock, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Sub sub) {
		Sub node = (Sub) (nodeReplacements.containsKey(sub) ? nodeReplacements.get(sub) : sub);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				Node preLoopBlock = pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newSub(preLoopBlock, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Conv conv) {
		Conv node = (Conv) (nodeReplacements.containsKey(conv) ? nodeReplacements.get(conv) : conv);
		Node operand = nodeReplacements.containsKey(node.getOp()) ? nodeReplacements.get(node.getOp()).getBlock() : node.getOp().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(operand)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				Node preLoopBlock = pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.contains(operand)) {
					Node copy = node.getGraph().newConv(preLoopBlock, node.getOp(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Shl shl) {
		Shl node = (Shl) (nodeReplacements.containsKey(shl) ? nodeReplacements.get(shl) : shl);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				Node preLoopBlock = pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newShl(preLoopBlock, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Shr shr) {
		Shr node = (Shr) (nodeReplacements.containsKey(shr) ? nodeReplacements.get(shr) : shr);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				Node preLoopBlock = pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newShr(preLoopBlock, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Shrs shrs) {
		Shrs node = (Shrs) (nodeReplacements.containsKey(shrs) ? nodeReplacements.get(shrs) : shrs);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				Node preLoopBlock = pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newShrs(preLoopBlock, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Mul mul) {
		Mul node = (Mul) (nodeReplacements.containsKey(mul) ? nodeReplacements.get(mul) : mul);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				Node preLoopBlock = pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newMul(preLoopBlock, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Minus minus) {
		Minus node = (Minus) (nodeReplacements.containsKey(minus) ? nodeReplacements.get(minus) : minus);
		Node operand = nodeReplacements.containsKey(node.getOp()) ? nodeReplacements.get(node.getOp()).getBlock() : node.getOp().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(operand)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				Node preLoopBlock = pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.contains(operand)) {
					Node copy = node.getGraph().newMinus(preLoopBlock, node.getOp(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Not not) {
		Not node = (Not) (nodeReplacements.containsKey(not) ? nodeReplacements.get(not) : not);
		Node operand = nodeReplacements.containsKey(node.getOp()) ? nodeReplacements.get(node.getOp()).getBlock() : node.getOp().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(operand)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				Node preLoopBlock = pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.contains(operand)) {
					Node copy = node.getGraph().newNot(preLoopBlock, node.getOp(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Start start) {
		FirmUtils utils = new FirmUtils(start.getGraph());
		dominators = utils.getDominators();
		backedges = utils.getBackEdges();
	}
}
