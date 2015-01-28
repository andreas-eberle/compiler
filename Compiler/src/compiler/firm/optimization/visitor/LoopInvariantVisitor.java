package compiler.firm.optimization.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import compiler.firm.optimization.GraphDetails;

import firm.Graph;
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

	public static final OptimizationVisitorFactory<Node> FACTORY(final HashMap<Graph, GraphDetails> graphDetails) {
		return new OptimizationVisitorFactory<Node>() {
			@Override
			public OptimizationVisitor<Node> create() {
				return new LoopInvariantVisitor(graphDetails);
			}
		};
	}

	private final HashMap<Graph, GraphDetails> graphDetails;

	private HashMap<Node, Node> backedges = new HashMap<>();
	private HashMap<Block, Set<Block>> dominators = new HashMap<>();

	public LoopInvariantVisitor(HashMap<Graph, GraphDetails> graphDetails) {
		this.graphDetails = graphDetails;
	}

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
						if (dominators.containsKey(entry.getKey()) && !dominators.get(entry.getKey()).contains(block)
								&& !dominatorBlocks.contains(entry.getKey())) {
							// b and the looṕ header are on the same 'level'
							sameLevelLoops.add(b);
							continue L1;
						}
					}
				}
			}
		}
		for (Block b : loops) {
			if (!sameLevelLoops.contains(b) && dominators.containsKey(b) && dominators.get(b).containsAll(loops)) {
				return b;
			}
		}
		return null;
	}

	@Override
	public void visit(Add add) {
		final Add addNode = getNodeOrReplacement(add);
		Block leftBlock = (Block) getNodeOrReplacement(addNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(addNode.getRight()).getBlock();

		checkNode(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return addNode.getGraph().newAdd(newBlock, addNode.getLeft(), addNode.getRight(), addNode.getMode());
			}

		}, addNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Sub sub) {
		final Sub subNode = getNodeOrReplacement(sub);
		Block leftBlock = (Block) getNodeOrReplacement(subNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(subNode.getRight()).getBlock();

		checkNode(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return subNode.getGraph().newSub(newBlock, subNode.getLeft(), subNode.getRight(), subNode.getMode());
			}

		}, subNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Shl shl) {
		final Shl shlNode = getNodeOrReplacement(shl);
		Block leftBlock = (Block) getNodeOrReplacement(shlNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(shlNode.getRight()).getBlock();

		checkNode(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return shlNode.getGraph().newShl(newBlock, shlNode.getLeft(), shlNode.getRight(), shlNode.getMode());
			}

		}, shlNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Shr shr) {
		final Shr shrNode = getNodeOrReplacement(shr);
		Block leftBlock = (Block) getNodeOrReplacement(shrNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(shrNode.getRight()).getBlock();

		checkNode(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return shrNode.getGraph().newShr(newBlock, shrNode.getLeft(), shrNode.getRight(), shrNode.getMode());
			}

		}, shrNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Shrs shrs) {
		final Shrs shrsNode = getNodeOrReplacement(shrs);
		Block leftBlock = (Block) getNodeOrReplacement(shrsNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(shrsNode.getRight()).getBlock();

		checkNode(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return shrsNode.getGraph().newShrs(newBlock, shrsNode.getLeft(), shrsNode.getRight(), shrsNode.getMode());
			}

		}, shrsNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Mul mul) {
		final Mul mulNode = getNodeOrReplacement(mul);
		Block leftBlock = (Block) getNodeOrReplacement(mulNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(mulNode.getRight()).getBlock();

		checkNode(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return mulNode.getGraph().newMul(newBlock, mulNode.getLeft(), mulNode.getRight(), mulNode.getMode());
			}

		}, mulNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Minus minus) {
		final Minus minusNode = getNodeOrReplacement(minus);
		Block operandBlock = (Block) getNodeOrReplacement(minusNode.getOp()).getBlock();

		checkNode(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return minusNode.getGraph().newMinus(newBlock, minusNode.getOp(), minusNode.getMode());
			}

		}, minusNode, operandBlock);
	}

	@Override
	public void visit(Not not) {
		final Not notNode = getNodeOrReplacement(not);
		Block operandBlock = (Block) getNodeOrReplacement(notNode.getOp()).getBlock();

		checkNode(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return notNode.getGraph().newNot(newBlock, notNode.getOp(), notNode.getMode());
			}

		}, notNode, operandBlock);
	}

	@Override
	public void visit(Conv conv) {
		final Conv convNode = getNodeOrReplacement(conv);
		Block operandBlock = (Block) getNodeOrReplacement(convNode.getOp()).getBlock();

		checkNode(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return convNode.getGraph().newConv(newBlock, convNode.getOp(), convNode.getMode());
			}

		}, convNode, operandBlock);
	}

	private void checkNode(NodeFactory factory, Node node, Block... operandBlocks) {
		List<Block> operandBlocksList = Arrays.asList(operandBlocks);

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.containsAll(operandBlocksList)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				Block preLoopBlock = (Block) pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.containsAll(operandBlocksList)) {
					Node copy = factory.copyNode(preLoopBlock);
					addReplacement(node, copy);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getNodeOrReplacement(T node) {
		return (T) (nodeReplacements.containsKey(node) ? nodeReplacements.get(node) : node);
	}

	@Override
	public void visit(Start start) {
		FirmUtils utils = new FirmUtils(start.getGraph());
		dominators = utils.getDominators();
		backedges = utils.getBackEdges();
	}

	private static interface NodeFactory {
		Node copyNode(Block newBlock);
	}
}
