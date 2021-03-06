package compiler.firm.generation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import compiler.Symbol;
import compiler.ast.Block;
import compiler.ast.declaration.ClassDeclaration;
import compiler.ast.declaration.Declaration;
import compiler.ast.declaration.FieldDeclaration;
import compiler.ast.declaration.LocalVariableDeclaration;
import compiler.ast.declaration.MainMethodDeclaration;
import compiler.ast.declaration.MemberDeclaration;
import compiler.ast.declaration.MethodDeclaration;
import compiler.ast.declaration.MethodMemberDeclaration;
import compiler.ast.declaration.NativeMethodDeclaration;
import compiler.ast.declaration.ParameterDeclaration;
import compiler.ast.statement.ArrayAccessExpression;
import compiler.ast.statement.BooleanConstantExpression;
import compiler.ast.statement.Expression;
import compiler.ast.statement.IfStatement;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.MethodInvocationExpression;
import compiler.ast.statement.NewArrayExpression;
import compiler.ast.statement.NewObjectExpression;
import compiler.ast.statement.NullExpression;
import compiler.ast.statement.Statement;
import compiler.ast.statement.ThisExpression;
import compiler.ast.statement.VariableAccessExpression;
import compiler.ast.statement.WhileStatement;
import compiler.ast.statement.binary.AdditionExpression;
import compiler.ast.statement.binary.AssignmentExpression;
import compiler.ast.statement.binary.BinaryExpression;
import compiler.ast.statement.binary.DivisionExpression;
import compiler.ast.statement.binary.EqualityExpression;
import compiler.ast.statement.binary.GreaterThanEqualExpression;
import compiler.ast.statement.binary.GreaterThanExpression;
import compiler.ast.statement.binary.LessThanEqualExpression;
import compiler.ast.statement.binary.LessThanExpression;
import compiler.ast.statement.binary.LogicalAndExpression;
import compiler.ast.statement.binary.LogicalOrExpression;
import compiler.ast.statement.binary.ModuloExpression;
import compiler.ast.statement.binary.MuliplicationExpression;
import compiler.ast.statement.binary.NonEqualityExpression;
import compiler.ast.statement.binary.SubtractionExpression;
import compiler.ast.statement.unary.LogicalNotExpression;
import compiler.ast.statement.unary.NegateExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.ast.type.ArrayType;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.firm.FirmUtils;
import compiler.semantic.ClassScope;
import firm.Construction;
import firm.Entity;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.PrimitiveType;
import firm.Relation;
import firm.bindings.binding_ircons.op_pin_state;
import firm.nodes.Call;
import firm.nodes.Load;
import firm.nodes.Node;
import firm.nodes.Store;

public class FirmGenerationVisitor implements AstVisitor {

	private final Entity calloc;

	// current definitions
	private Construction methodConstruction = null;
	private final Map<firm.nodes.Block, Node> methodReturns = new LinkedHashMap<firm.nodes.Block, Node>();

	// create new map for param <-> variable number
	private Node activePhiNode;

	private Expression assignmentRightSide = null;

	private firm.nodes.Block trueDestination;
	private firm.nodes.Block falseDestination;

	public FirmGenerationVisitor() {
		Mode.setDefaultModeP(FirmUtils.getModeReference());

		// void* calloc_proxy (size_t num, size_t size);
		MethodType callocType = new MethodType(
				new firm.Type[] { new PrimitiveType(FirmUtils.getModeInteger()), new PrimitiveType(FirmUtils.getModeInteger()) },
				new firm.Type[] { new PrimitiveType(FirmUtils.getModeReference()) });
		calloc = new Entity(firm.Program.getGlobalType(), FirmUtils.CALLOC_PROXY, callocType);
	}

	public void initialize(HashMap<Symbol, ClassScope> classScopes) {
		// first iterate over all classes -- so that forward references to classes
		// in method parameters and return types work
		for (Entry<Symbol, ClassScope> currentEntry : classScopes.entrySet()) {
			ClassDeclaration classDeclaration = currentEntry.getValue().getClassDeclaration();

			// Create class nodes
			classDeclaration.getType().getFirmClassType();
			classDeclaration.getType().getFirmType();
		}

		// iterate over all fields and methods and create firm entities
		for (Entry<Symbol, ClassScope> currentEntry : classScopes.entrySet()) {
			ClassScope scope = currentEntry.getValue();
			ClassDeclaration classDeclaration = scope.getClassDeclaration();
			firm.ClassType firmClassType = classDeclaration.getType().getFirmClassType();

			// Create field declarations
			for (Declaration currentField : scope.getFieldDeclarations()) {
				new Entity(firmClassType, currentField.getAssemblerName(), currentField.getType().getFirmType());
			}
			for (MethodMemberDeclaration currentMethod : scope.getMethodDeclarations()) {
				List<ParameterDeclaration> parameterDeclarations = currentMethod.getValidParameters();

				// types of parameters
				// first parameter is "this" with type referenceToClass
				int thisOffset = currentMethod.isStatic() ? 0 : 1;
				firm.Type[] parameterTypes = new firm.Type[parameterDeclarations.size() + thisOffset];
				if (!currentMethod.isStatic())
					parameterTypes[0] = classDeclaration.getType().getFirmType();

				for (int paramIdx = 0; paramIdx < parameterDeclarations.size(); paramIdx++) {
					parameterTypes[paramIdx + thisOffset] = parameterDeclarations.get(paramIdx).getType().getFirmType();
				}

				// return type
				firm.Type[] returnType = {};
				if (!currentMethod.getType().is(BasicType.VOID)) {
					returnType = new firm.Type[] { currentMethod.getType().getFirmType() };
				}

				// create methodType and methodEntity
				MethodType methodType = new MethodType(parameterTypes, returnType);

				// create new entity and attach to currentClass
				new Entity(firmClassType, currentMethod.getAssemblerName(), methodType);
			}

			firm.ClassType classType = classDeclaration.getType().getFirmClassType();
			classType.layoutFields();
			classType.finishLayout();
		}
	}

	private Node getThisPointer() {
		return methodConstruction.getVariable(0, FirmUtils.getModeReference());
	}

	private static interface CreateBinaryFirmNode {
		public Node createNode(Node operand1, Node operand2, Mode mode);
	}

	private Node getNodeForExpression(Expression expression) {
		Node operand;
		if (expression.getType().is(BasicType.BOOLEAN)) {
			// maybe additional blocks need to be created
			operand = createBooleanNodeFromBinaryExpression(expression);
		} else {
			expression.accept(this);
			operand = expression.getFirmNode();
		}
		return operand;
	}

	private void createFirmForBinaryOperation(BinaryExpression binaryExpression, CreateBinaryFirmNode firmNodeCreator) {
		// get firmNode for operands
		Node operand1 = getNodeForExpression(binaryExpression.getOperand1());
		Node operand2 = getNodeForExpression(binaryExpression.getOperand2());

		Node exprNode = firmNodeCreator.createNode(operand1, operand2, binaryExpression.getType().getMode());
		binaryExpression.setFirmNode(exprNode);
	}

	private void createFirmForComparisonOperation(BinaryExpression binaryExpression, final Relation comparison) {
		createFirmForBinaryOperation(binaryExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				Node cmp = methodConstruction.newCmp(operand1, operand2, comparison);
				return methodConstruction.newCond(cmp);
			}
		});
	}

	@Override
	public void visit(AdditionExpression additionExpression) {
		createFirmForBinaryOperation(additionExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				return methodConstruction.newAdd(operand1, operand2, mode);
			}
		});
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		// first evaluate rhsExpression, than let lhsExpression decide what to do with rhsExpression;

		Expression leftExpression = assignmentExpression.getOperand1();
		Expression rightExpression = assignmentExpression.getOperand2();
		assert rightExpression != null;
		assignmentRightSide = rightExpression;
		leftExpression.accept(this);
		assignmentRightSide = null;
		assignmentExpression.setFirmNode(rightExpression.getFirmNode());
	}

	private Node getRightSideOfAssignment(Expression rightExpression) {
		if (rightExpression.getFirmNode() != null) {
			return rightExpression.getFirmNode();
		}

		return getNodeForExpression(rightExpression);
	}

	private Node createBooleanNodeFromBinaryExpression(Expression expression) {
		if (expression instanceof BooleanConstantExpression
				|| expression instanceof VariableAccessExpression
				|| expression instanceof MethodInvocationExpression
				|| expression instanceof LogicalNotExpression) {
			expression.accept(this);
			return expression.getFirmNode();
		} else {
			firm.nodes.Block trueBlock = methodConstruction.newBlock();
			firm.nodes.Block falseBlock = methodConstruction.newBlock();
			firm.nodes.Block afterBlock = methodConstruction.newBlock();

			evaluateBooleanExpression(expression, trueBlock, falseBlock);
			// create true block assigning true to temp variable
			trueBlock.mature();
			methodConstruction.setCurrentBlock(trueBlock);
			Node trueConst = createBooleanConstantNode(true);
			Node trueJump = methodConstruction.newJmp();
			afterBlock.addPred(trueJump);

			// create false block assigning false to temp variable
			falseBlock.mature();
			methodConstruction.setCurrentBlock(falseBlock);
			Node falseConst = createBooleanConstantNode(false);
			Node falseJump = methodConstruction.newJmp();
			afterBlock.addPred(falseJump);

			afterBlock.mature();
			methodConstruction.setCurrentBlock(afterBlock);
			Node phi = methodConstruction.newPhi(new Node[] { trueConst, falseConst }, FirmUtils.getModeBoolean());
			activePhiNode = phi;
			return phi;
		}
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		createFirmForBinaryOperation(divisionExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				Node tupleNode = methodConstruction.newDiv(methodConstruction.getCurrentMem(), operand1, operand2, mode,
						op_pin_state.op_pin_state_pinned);
				return methodConstruction.newProj(tupleNode, mode, Call.pnTResult);
			}
		});
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
		createFirmForComparisonOperation(equalityExpression, Relation.Equal);
	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
		createFirmForComparisonOperation(greaterThanEqualExpression, Relation.GreaterEqual);
	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
		createFirmForComparisonOperation(greaterThanExpression, Relation.Greater);
	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
		createFirmForComparisonOperation(lessThanEqualExpression, Relation.LessEqual);
	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
		createFirmForComparisonOperation(lessThanExpression, Relation.Less);
	}

	@Override
	public void visit(LogicalAndExpression logicalAndExpression) {
		firm.nodes.Block leftTrueBlock = methodConstruction.newBlock();
		evaluateBooleanExpression(logicalAndExpression.getOperand1(), leftTrueBlock, falseDestination);

		shortEvaluationRightOperand(logicalAndExpression, leftTrueBlock);
	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
		firm.nodes.Block leftFalseBlock = methodConstruction.newBlock();
		evaluateBooleanExpression(logicalOrExpression.getOperand1(), trueDestination, leftFalseBlock);

		shortEvaluationRightOperand(logicalOrExpression, leftFalseBlock);
	}

	private void shortEvaluationRightOperand(BinaryExpression binaryExpression, firm.nodes.Block block) {
		firm.nodes.Block currentBlock = methodConstruction.getCurrentBlock();
		methodConstruction.setCurrentBlock(block);
		evaluateBooleanExpression(binaryExpression.getOperand2(), trueDestination, falseDestination);
		methodConstruction.setCurrentBlock(currentBlock);
	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
		createFirmForBinaryOperation(moduloExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				Node tupleNode = methodConstruction.newMod(methodConstruction.getCurrentMem(), operand1, operand2, mode,
						op_pin_state.op_pin_state_pinned);
				return methodConstruction.newProj(tupleNode, mode, Call.pnTResult);
			}
		});
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
		createFirmForBinaryOperation(multiplicationExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				return methodConstruction.newMul(operand1, operand2, mode);
			}
		});
	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
		createFirmForComparisonOperation(nonEqualityExpression, Relation.LessGreater);
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
		createFirmForBinaryOperation(substractionExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				return methodConstruction.newSub(operand1, operand2, mode);
			}
		});

	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
		Node constant = createBooleanConstantNode(booleanConstantExpression.isValue());
		booleanConstantExpression.setFirmNode(constant);
	}

	private Node createBooleanConstantNode(boolean boolValue) {
		int boolIntValue = boolValue ? 0xFF : 0;
		return methodConstruction.newConst(boolIntValue, FirmUtils.getModeBoolean());
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		// assume that Integer.parseInt doesn't fail (must be checked in semantic analysis)
		String intValue = integerConstantExpression.getIntegerLiteral();
		int val = Integer.parseInt(intValue);

		Node constant = methodConstruction.newConst(val, FirmUtils.getModeInteger());
		integerConstantExpression.setFirmNode(constant);
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		Node[] parameterNodes;
		Entity method;

		Node methodObject = null;
		int thisOffset = 1;

		// Get base object
		MethodMemberDeclaration methodDeclaration = methodInvocationExpression.getMethodDeclaration();
		Expression methodExpression = methodInvocationExpression.getMethodExpression();

		if (methodDeclaration.isStatic()) {
			thisOffset = 0;
		} else if (methodExpression != null) {
			methodExpression.accept(FirmGenerationVisitor.this);
			methodObject = methodExpression.getFirmNode();
		} else {
			methodObject = getThisPointer();
		}
		method = getEntity(methodDeclaration);

		// Generate parameter list
		Expression[] parameters = methodInvocationExpression.getParameters();
		parameterNodes = new Node[parameters.length + thisOffset];

		if (thisOffset == 1) {
			parameterNodes[0] = methodObject;
		}

		for (int j = 0; j < parameters.length; j++) {
			parameterNodes[j + thisOffset] = getNodeForExpression(parameters[j]);
		}

		Node addressOfMethod = methodConstruction.newAddress(method);
		MethodType firmMethodType = (MethodType) method.getType();
		methodInvocationExpression.setFirmNode(callMethod(addressOfMethod, parameterNodes, firmMethodType));
	}

	private Node callMethod(Node addressOfMethod, Node[] parameterNodes, MethodType firmMethodType) {
		Node methodCall = methodConstruction.newCall(methodConstruction.getCurrentMem(), addressOfMethod, parameterNodes,
				firmMethodType);
		Node memoryAfterCall = methodConstruction.newProj(methodCall, Mode.getM(), Call.pnM);
		methodConstruction.setCurrentMem(memoryAfterCall);

		// get result
		Node resultValue = null;
		if (firmMethodType.getNRess() != 0) {
			Node methodResult = methodConstruction.newProj(methodCall, Mode.getT(), Call.pnTResult);
			resultValue = methodConstruction.newProj(methodResult, firmMethodType.getResType(0).getMode(), 0);
		}
		return resultValue;
	}

	private Node callCalloc(Node numberOfElements, Node sizeofClass) {
		return callMethod(methodConstruction.newAddress(calloc),
				new Node[] { numberOfElements, sizeofClass },
				(MethodType) calloc.getType());
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		int elementsSize = newArrayExpression.getType().getSubType().getFirmType().getSizeBytes();

		Expression elementsCount = newArrayExpression.getFirstDimension();
		elementsCount.accept(this);

		Node referenceToObject = callCalloc(elementsCount.getFirmNode(), intToNode(elementsSize));
		newArrayExpression.setFirmNode(referenceToObject);
	}

	private Node intToNode(int i) {
		return methodConstruction.newConst(i, FirmUtils.getModeInteger());
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		firm.ClassType classType = newObjectExpression.getObjectClass().getType().getFirmClassType();
		Node referenceToObject = callCalloc(intToNode(1), intToNode(classType.getSizeBytes()));
		newObjectExpression.setFirmNode(referenceToObject);
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		Expression assignmentRightSide = this.assignmentRightSide;
		this.assignmentRightSide = null;

		Expression objectNameForFieldAccess = variableAccessExpression.getExpression();
		if (objectNameForFieldAccess == null) {
			Declaration declaration = variableAccessExpression.getDeclaration();
			if (declaration instanceof LocalVariableDeclaration) {
				variableAccess(variableAccessExpression, assignmentRightSide);
			} else {
				memberAccess(variableAccessExpression, getThisPointer(), assignmentRightSide);
			}
		} else {
			objectNameForFieldAccess.accept(this);
			memberAccess(variableAccessExpression, objectNameForFieldAccess.getFirmNode(),
					assignmentRightSide);
		}
	}

	private void variableAccess(VariableAccessExpression variableAccessExpression, Expression assignmentRightSide) {
		LocalVariableDeclaration declaration = (LocalVariableDeclaration) variableAccessExpression.getDeclaration();
		int variableNumber = declaration.getVariableNumber();

		if (assignmentRightSide != null) {
			Node rightSideNode = getRightSideOfAssignment(assignmentRightSide);
			// this is variable set expression:
			methodConstruction.setVariable(variableNumber, rightSideNode);
			variableAccessExpression.setFirmNode(rightSideNode);
		} else {
			Type astType = declaration.getType();
			Mode accessMode = astType.getMode();
			Node node = methodConstruction.getVariable(variableNumber, accessMode);
			variableAccessExpression.setFirmNode(node);
		}
	}

	private void memberAccess(VariableAccessExpression variableAccessExpression, Node object, Expression assignmentRightSide) {
		Entity field = getEntity(variableAccessExpression.getDeclaration());

		Node addressOfField = methodConstruction.newMember(object, field);

		if (assignmentRightSide != null) {
			memberAssign(addressOfField, assignmentRightSide);
			variableAccessExpression.setFirmNode(assignmentRightSide.getFirmNode());
		} else {
			firm.Mode fieldAccessMode = field.getType().getMode();
			// primitive types have modes, Pointer types don't and there is no method to set it
			// for minijava it is safe to set all pointers to reference
			if (fieldAccessMode == null)
				fieldAccessMode = FirmUtils.getModeReference();
			Node member = memberGet(addressOfField, fieldAccessMode);
			variableAccessExpression.setFirmNode(member);
		}
	}

	private Node memberAssign(Node addressOfField, Expression content) {
		Node contentNode = getRightSideOfAssignment(content);
		Node storeValue = methodConstruction.newStore(methodConstruction.getCurrentMem(), addressOfField, contentNode);
		Node memoryAfterStore = methodConstruction.newProj(storeValue, Mode.getM(), Store.pnM);
		methodConstruction.setCurrentMem(memoryAfterStore);
		return memoryAfterStore;
	}

	private Node memberGet(Node addressOfField, Mode fieldAccessMode) {
		Node loadValue = methodConstruction.newLoad(methodConstruction.getCurrentMem(), addressOfField, fieldAccessMode);
		Node loadMememory = methodConstruction.newProj(loadValue, Mode.getM(), Load.pnM);
		methodConstruction.setCurrentMem(loadMememory);
		return methodConstruction.newProj(loadValue, fieldAccessMode, Load.pnRes);
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		// save rvalue so that variable access expression doesn't think it is a store
		Expression assignmentRightSide = this.assignmentRightSide;
		this.assignmentRightSide = null;

		Expression arrayExpression = arrayAccessExpression.getArrayExpression();
		Expression indexExpression = arrayAccessExpression.getIndexExpression();

		arrayExpression.accept(this);
		indexExpression.accept(this);

		// calculate index offset
		Node arrayIndex = methodConstruction
				.newSel(arrayExpression.getFirmNode(), indexExpression.getFirmNode(), ((ArrayType) arrayExpression.getType()).getFirmArrayType());

		Node result = null;
		if (assignmentRightSide != null) {
			result = memberAssign(arrayIndex, assignmentRightSide);
		} else {
			result = memberGet(arrayIndex, arrayExpression.getType().getSubType().getMode());
		}
		arrayAccessExpression.setFirmNode(result);
	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
		if (trueDestination == null || falseDestination == null) {
			Expression operand = logicalNotExpression.getOperand();
			operand.accept(this);
			Node firmNode = operand.getFirmNode();
			logicalNotExpression.setFirmNode(methodConstruction.newNot(firmNode, firmNode.getMode()));
		} else {
			evaluateBooleanExpression(logicalNotExpression.getOperand(), falseDestination, trueDestination);
		}
	}

	@Override
	public void visit(NegateExpression negateExpression) {
		// get firmNode for the operand
		Expression operand = negateExpression.getOperand();
		operand.accept(this);
		Node operandNode = operand.getFirmNode();

		Node exprNode = methodConstruction.newMinus(operandNode, negateExpression.getType().getMode());
		negateExpression.setFirmNode(exprNode);
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		boolean hasOperand = returnStatement.getOperand() != null;

		Node returnNode;
		if (hasOperand) {
			Node exprNode;
			if (returnStatement.getOperand().getType().is(BasicType.BOOLEAN)) {
				exprNode = createBooleanNodeFromBinaryExpression(returnStatement.getOperand());
			} else {
				returnStatement.getOperand().accept(this);
				exprNode = returnStatement.getOperand().getFirmNode();
			}
			returnNode = methodConstruction.newReturn(methodConstruction.getCurrentMem(), new Node[] { exprNode });
		} else {
			// return void
			returnNode = methodConstruction.newReturn(methodConstruction.getCurrentMem(), new Node[] {});
		}

		methodReturns.put(methodConstruction.getCurrentBlock(), returnNode);
	}

	@Override
	public void visit(ThisExpression thisExpression) {
		thisExpression.setFirmNode(getThisPointer());
	}

	@Override
	public void visit(NullExpression nullExpression) {
		Node cNull = methodConstruction.newConst(0, FirmUtils.getModeReference());
		nullExpression.setFirmNode(cNull);

	}

	@Override
	public void visit(Block block) {
		if (!block.isEmpty()) {
			for (Statement statement : block.getStatements()) {
				statement.accept(this);

				if (methodReturns.containsKey(methodConstruction.getCurrentBlock())) {
					break; // stop visiting the rest, we already have a return
				}
			}

			Node returnNode = methodReturns.get(methodConstruction.getCurrentBlock());
			if (returnNode != null) {
				block.setFirmNode(returnNode);
			} else {
				// get last statement and set block firmNode to this statement
				Statement lastStatement = block.getStatements().get(block.getNumberOfStatements() - 1);
				block.setFirmNode(lastStatement.getFirmNode());
			}
		}
	}

	private Node getConditionNode(Expression expression) {
		Node conditionNode;
		if (expression.getFirmNode() != null && !expression.getFirmNode().getMode().equals(Mode.getT())) {
			// booleans and boolean constants
			Node trueConst = createBooleanConstantNode(true);
			Node cmp = methodConstruction.newCmp(expression.getFirmNode(), trueConst, Relation.Equal);
			conditionNode = methodConstruction.newCond(cmp);
		} else {
			// the result is already the right tuple
			conditionNode = expression.getFirmNode();
		}
		return conditionNode;
	}

	private void evaluateBooleanExpression(Expression condition, firm.nodes.Block trueBlock, firm.nodes.Block falseBlock) {
		firm.nodes.Block oldTrueDestination = this.trueDestination;
		firm.nodes.Block oldFalseDestination = this.falseDestination;
		this.trueDestination = trueBlock;
		this.falseDestination = falseBlock;

		condition.accept(this);

		this.trueDestination = oldTrueDestination;
		this.falseDestination = oldFalseDestination;

		Node conditionNode = getConditionNode(condition);
		Mode mode = Mode.getX();

		if (conditionNode != null && !conditionNode.getBlock().equals(methodConstruction.getCurrentBlock())) {
			// TODO: find a more elegant way to do this!
			// we reference a condition node that is not in the current block, get the last set phi node instead
			// create a new condition node for that phi node
			Node trueConst = createBooleanConstantNode(true);
			Node cmp = methodConstruction.newCmp(activePhiNode, trueConst, Relation.Equal);
			conditionNode = methodConstruction.newCond(cmp);
			Node condTrue = methodConstruction.newProj(conditionNode, mode, 1);
			trueBlock.addPred(condTrue);
			Node condFalse = methodConstruction.newProj(conditionNode, mode, 0);
			falseBlock.addPred(condFalse);
			activePhiNode = null;
		} else if (conditionNode != null) { // For logical and and logical or this is already done, ignore it.
			Node condTrue = methodConstruction.newProj(conditionNode, mode, 1);
			trueBlock.addPred(condTrue);
			Node condFalse = methodConstruction.newProj(conditionNode, mode, 0);
			falseBlock.addPred(condFalse);
		}
	}

	@Override
	public void visit(IfStatement ifStatement) {
		boolean hasElseBlock = ifStatement.getFalseCase() != null;

		firm.nodes.Block trueBlock = methodConstruction.newBlock();
		firm.nodes.Block falseBlock;
		firm.nodes.Block endifBlock = methodConstruction.newBlock();

		if (hasElseBlock) {
			// only create new block if necessary
			falseBlock = methodConstruction.newBlock();
		} else {
			falseBlock = endifBlock;
		}

		evaluateBooleanExpression(ifStatement.getCondition(), trueBlock, falseBlock);
		trueBlock.mature();

		// set new block as active and visit true case
		methodConstruction.setCurrentBlock(trueBlock);
		ifStatement.getTrueCase().accept(this);
		// prevent a second control flow instruction from being set for this block
		if (!methodReturns.containsKey(methodConstruction.getCurrentBlock())) {
			Node trueJmp = methodConstruction.newJmp();
			endifBlock.addPred(trueJmp);
		}
		if (hasElseBlock) {
			// only mature if else block is present
			falseBlock.mature();

			// set new block as active and visit false case
			methodConstruction.setCurrentBlock(falseBlock);
			ifStatement.getFalseCase().accept(this);
			// prevent a second control flow instruction from being set for this block
			if (!methodReturns.containsKey(methodConstruction.getCurrentBlock())) {
				Node falseJmp = methodConstruction.newJmp();
				endifBlock.addPred(falseJmp);
			}
		}

		endifBlock.mature();
		methodConstruction.setCurrentBlock(endifBlock);
		ifStatement.setFirmNode(null);
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		Node startJump = methodConstruction.newJmp();

		firm.nodes.Block loopBlock = methodConstruction.newBlock();
		firm.nodes.Block endBlock = methodConstruction.newBlock();
		firm.nodes.Block startBlock = methodConstruction.newBlock();
		startBlock.addPred(startJump);

		methodConstruction.setCurrentBlock(startBlock);
		evaluateBooleanExpression(whileStatement.getCondition(), loopBlock, endBlock);
		loopBlock.mature();
		endBlock.mature();

		methodConstruction.setCurrentBlock(loopBlock);
		methodConstruction.getCurrentMem();

		whileStatement.getBody().accept(this);
		if (!methodReturns.containsKey(methodConstruction.getCurrentBlock())) {
			Node loopJump = methodConstruction.newJmp();
			startBlock.addPred(loopJump);
		}

		startBlock.mature();
		methodConstruction.setCurrentBlock(endBlock);
		methodConstruction.getGraph().keepAlive(startBlock);
		whileStatement.setFirmNode(null);
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
		Expression expression = localVariableDeclaration.getExpression();
		if (expression != null) {
			VariableAccessExpression variableAccess = new VariableAccessExpression(null, null, localVariableDeclaration.getIdentifier());
			variableAccess.setDeclaration(localVariableDeclaration);
			AssignmentExpression assignment = new AssignmentExpression(null, variableAccess, expression);
			assignment.accept(this);
		} else {
			assignDefaultValue(localVariableDeclaration);
		}
	}

	private void assignDefaultValue(Declaration declaration) {
		Expression expression;
		switch (declaration.getType().getBasicType()) {
		case INT:
			expression = new IntegerConstantExpression(null, "0");
			break;
		case BOOLEAN:
			expression = new BooleanConstantExpression(null, false);
			break;
		case ARRAY:
		case CLASS:
			expression = new NullExpression(null);
			break;
		default:
			throw new RuntimeException("Internal Compiler Error: This should never happen!");
		}

		expression.setType(declaration.getType());
		VariableAccessExpression variableAccess = new VariableAccessExpression(null, null, declaration.getIdentifier());
		variableAccess.setDeclaration(declaration);
		AssignmentExpression assignment = new AssignmentExpression(null, variableAccess, expression);
		assignment.accept(this);
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		for (MemberDeclaration curr : classDeclaration.getMembers()) {
			curr.accept(this);
		}
	}

	@Override
	public void visit(compiler.ast.Program program) {
		for (ClassDeclaration curr : program.getClasses()) {
			curr.accept(this);
		}
	}

	@Override
	public void visit(MethodDeclaration methodDeclaration) {
		visitMethodDeclaration(methodDeclaration);
	}

	@Override
	public void visit(MainMethodDeclaration staticMethodDeclaration) {
		visitMethodDeclaration(staticMethodDeclaration);
	}

	public void visitMethodDeclaration(MethodDeclaration methodDeclaration) {
		clearState();

		int numberLocalVariables = methodDeclaration.getNumberOfLocalVariables();
		int variablesCount = 1 /* this */+ methodDeclaration.getValidParameters().size() + numberLocalVariables /* boolean assignments */+ 1;
		Graph graph = new Graph(getEntity(methodDeclaration), variablesCount);
		methodConstruction = new Construction(graph);

		Node args = graph.getArgs();
		if (!methodDeclaration.isStatic()) {
			// set this parameter
			createThisParameter(args);
		}
		// create parameters variables
		for (ParameterDeclaration param : methodDeclaration.getValidParameters()) {
			param.accept(this);
		}

		methodDeclaration.getBlock().accept(this);

		// add all collected return nodes
		for (Node returnNode : methodReturns.values()) {
			graph.getEndBlock().addPred(returnNode);
		}
		if (methodDeclaration.getType().is(BasicType.VOID)
				&& !methodReturns.containsKey(methodConstruction.getCurrentBlock())) {
			// return void if no return was specified yet
			Node returnNode = methodConstruction.newReturn(methodConstruction.getCurrentMem(), new Node[] {});
			graph.getEndBlock().addPred(returnNode);
		}

		methodConstruction.finish();
	}

	@Override
	public void visit(NativeMethodDeclaration nativeMethodDeclaration) {
		// nothing to do
	}

	private void createThisParameter(Node args) {
		Node projThis = methodConstruction.newProj(args, FirmUtils.getModeReference(), 0);
		methodConstruction.setVariable(0, projThis);
	}

	@Override
	public void visit(ParameterDeclaration parameterDeclaration) {
		Type type = parameterDeclaration.getType();
		Node reference = methodConstruction.getGraph().getArgs();
		Node parameterProj = methodConstruction.newProj(reference, type.getMode(), parameterDeclaration.getVariableNumber());
		methodConstruction.setVariable(parameterDeclaration.getVariableNumber(), parameterProj);
		parameterDeclaration.setFirmNode(parameterProj);
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
	}

	private void clearState() {
		this.assignmentRightSide = null;
		this.methodConstruction = null;
		this.methodReturns.clear();
		this.activePhiNode = null;
	}

	@Override
	public void visit(Type type) {
		// Type will never be visited
	}

	@Override
	public void visit(ClassType classType) {
		// ClassType will never be visited
	}

	@Override
	public void visit(ArrayType arrayType) {
		// ArrayType will never be visited
	}

	private Entity getEntity(Declaration declaration) {
		return declaration.getClassDeclaration().getType().getFirmClassType().getMemberByName(declaration.getAssemblerName());
	}

}
