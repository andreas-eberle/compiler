package compiler.firm.backend;

import java.util.HashMap;
import java.util.List;

import compiler.firm.FirmUtils;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Mode;
import firm.nodes.Const;
import firm.nodes.Node;

public class StorageManagement {

	private final List<AssemblerOperation> operations;
	private final HashMap<Node, Storage> nodeStorages = new HashMap<>();

	public StorageManagement(List<AssemblerOperation> operations) {
		this.operations = operations;
	}

	private void addOperation(AssemblerOperation assemblerOption) {
		operations.add(assemblerOption);
	}

	private void addStorage(Node node, Storage storage) {
		nodeStorages.put(node, storage);
	}

	public void addConstant(Const node) {
		addStorage(node, new Constant(node));
	}

	public Storage getStorage(Node node) {
		Storage storage = nodeStorages.get(node);

		if (storage == null) {
			storage = new VirtualRegister(getMode(node), node.toString());
			addStorage(node, storage);
		}
		return storage;
	}

	public RegisterBased getValue(Node node) {
		RegisterBased result;
		Storage storage = getStorage(node);
		if (storage instanceof RegisterBased) {
			result = (RegisterBased) storage;
		} else {
			result = new VirtualRegister(getMode(node), node.toString());
			addOperation(new MovOperation(node.toString(), storage, result));
			if (!(storage instanceof Constant)) {
				addStorage(node, result);
			}
		}
		return result;
	}

	public void storeValue(Node node, Storage storage) {
		Storage destination = nodeStorages.get(node);
		if (destination == null) {
			destination = storage;
		}
		addStorage(node, storage);
		storeValue(node, storage, destination);
	}

	private void storeValue(Node node, Storage storage, Storage destination) {
		if (storage != destination) {
			throw new RuntimeException("storeValue source != destination");
		}
	}

	public static Bit getMode(Node node) {
		return getMode(node.getMode());
	}

	public static Bit getMode(Mode mode) {
		if (mode.equals(FirmUtils.getModeReference()) || mode.equals(Mode.getLu())) {
			return Bit.BIT64;
		} else if (mode.equals(FirmUtils.getModeBoolean())) {
			return Bit.BIT8;
		} else {
			return Bit.BIT32;
		}
	}

	public void storeToBackEdges(Node node, RegisterBased register) {
		for (Edge edge : BackEdges.getOuts(node)) {
			Node edgeNode = edge.node;
			if (!edgeNode.getMode().equals(Mode.getM())) {
				storeValue(edgeNode, register);
			}
		}
	}
}
