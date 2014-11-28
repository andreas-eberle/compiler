package compiler.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.ClassMember;
import compiler.ast.FieldDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.Program;
import compiler.ast.StaticMethodDeclaration;
import compiler.ast.statement.ArrayAccessExpression;
import compiler.ast.statement.BooleanConstantExpression;
import compiler.ast.statement.Expression;
import compiler.ast.statement.IfStatement;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.LocalVariableDeclaration;
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
import compiler.ast.statement.unary.UnaryExpression;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.semantic.exceptions.IllegalAccessToNonStaticMemberException;
import compiler.semantic.exceptions.InvalidMethodCallException;
import compiler.semantic.exceptions.MissingReturnStatementOnAPathException;
import compiler.semantic.exceptions.NoSuchMemberException;
import compiler.semantic.exceptions.NotAnExpressionStatementException;
import compiler.semantic.exceptions.RedefinitionErrorException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.semantic.exceptions.TypeErrorException;
import compiler.semantic.exceptions.UndefinedSymbolException;
import compiler.semantic.symbolTable.Definition;
import compiler.semantic.symbolTable.MethodDefinition;
import compiler.semantic.symbolTable.SymbolTable;

public class DeepCheckingVisitor implements AstVisitor {

	private final List<SemanticAnalysisException> exceptions = new ArrayList<>();
	private final HashMap<Symbol, ClassScope> classScopes;

	private SymbolTable symbolTable = null;
	private ClassDeclaration currentClassDeclaration;
	private ClassScope currentClassScope = null;
	private MethodDefinition currentMethodDefinition = null;

	private boolean isStaticMethod;
	private boolean returnOnAllPaths;
	private boolean isExpressionStatement;

	public DeepCheckingVisitor(HashMap<Symbol, ClassScope> classScopes) {
		this.classScopes = classScopes;
	}

	public List<SemanticAnalysisException> getExceptions() {
		return exceptions;
	}

	private void throwTypeError(AstNode astNode) {
		throwTypeError(astNode, null);
	}

	private void throwTypeError(AstNode astNode, String message) {
		exceptions.add(new TypeErrorException(astNode, message));
	}

	private void throwRedefinitionError(Symbol symbol, Position redefinition) {
		exceptions.add(new RedefinitionErrorException(symbol, redefinition));
	}

	private void throwUndefinedSymbolError(Symbol symbol, Position position) {
		exceptions.add(new UndefinedSymbolException(symbol, position));
	}

	private void throwNoSuchMemberError(Symbol object, Position objPos, Symbol member, Position memberPos) {
		exceptions.add(new NoSuchMemberException(object, objPos, member, memberPos));
	}

	private void throwIllegalAccessToNonStaticMemberError(Position objPos) {
		exceptions.add(new IllegalAccessToNonStaticMemberException(objPos));
	}

	private boolean hasType(Type type, AstNode astNode) {
		return (astNode.getType() == null
				|| (type.getBasicType() != null && type.getBasicType() != BasicType.INT && type.getBasicType() != BasicType.BOOLEAN
				&& astNode.getType().getBasicType() == BasicType.NULL)
				|| astNode.getType().equals(type));
	}

	private boolean hasType(BasicType type, AstNode astNode) {
		return (astNode.getType() == null || astNode.getType().getBasicType() == type);
	}

	private void expectType(Type type, AstNode astNode) {
		if (!hasType(type, astNode)) {
			throwTypeError(astNode);
		}
	}

	private boolean expectType(BasicType type, AstNode astNode) {
		boolean result = true;
		if (!hasType(type, astNode)) {
			throwTypeError(astNode);
			result = false;
		}
		return result;
	}

	private void setType(Type type, AstNode astNode) {
		astNode.setType(type);
	}

	private void setType(BasicType basicType, AstNode astNode) {
		setType(new Type(astNode.getPosition(), basicType), astNode);
	}

	private void checkBinaryOperandEquality(BinaryExpression binaryExpression) {
		AstNode left = binaryExpression.getOperand1();
		AstNode right = binaryExpression.getOperand2();
		left.accept(this);
		right.accept(this);
		if (left.getType() != null && right.getType() != null && !left.getType().equals(right.getType())) {
			throwTypeError(binaryExpression);
		}
	}

	private void checkBinaryOperandEqualityOrNull(BinaryExpression binaryExpression) {
		AstNode left = binaryExpression.getOperand1();
		AstNode right = binaryExpression.getOperand2();
		left.accept(this);
		right.accept(this);
		if (left.getType() != null
				&& right.getType() != null
				&& !(left.getType().equals(right.getType()) || left.getType().getBasicType() == BasicType.NULL || right.getType().getBasicType() == BasicType.NULL)) {
			throwTypeError(binaryExpression);
		}
	}

	private void checkExpression(BinaryExpression binaryExpression, BasicType expected, BasicType result) {
		checkBinaryOperandEquality(binaryExpression);
		expectType(expected, binaryExpression.getOperand1());
		setType(result, binaryExpression);
	}

	private void checkExpression(UnaryExpression unaryExpression, BasicType expected, BasicType result) {
		AstNode operand = unaryExpression.getOperand();
		operand.accept(this);
		expectType(expected, operand);
		setType(result, unaryExpression);
	}

	@Override
	public void visit(AdditionExpression additionExpression) {
		checkExpression(additionExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		this.isExpressionStatement = true;

		checkBinaryOperandEqualityOrNull(assignmentExpression);

		Expression operand1 = assignmentExpression.getOperand1();
		Expression operand2 = assignmentExpression.getOperand2();
		if (!(operand1 instanceof VariableAccessExpression || operand1 instanceof ArrayAccessExpression)) {
			throwTypeError(operand1, "Left side of assignment expression should variable or array.");
		} else if (operand1.getType() != null && operand2.getType() != null &&
				(hasType(BasicType.INT, operand1) || hasType(BasicType.BOOLEAN, operand1)) && hasType(BasicType.NULL, operand2)) {
			throwTypeError(operand2);
		}

		setType(operand1.getType(), assignmentExpression);
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		checkExpression(divisionExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
		checkBinaryOperandEqualityOrNull(equalityExpression);
		setType(BasicType.BOOLEAN, equalityExpression);
	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
		checkExpression(greaterThanEqualExpression, BasicType.INT, BasicType.BOOLEAN);
	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
		checkExpression(greaterThanExpression, BasicType.INT, BasicType.BOOLEAN);
	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
		checkExpression(lessThanEqualExpression, BasicType.INT, BasicType.BOOLEAN);
	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
		checkExpression(lessThanExpression, BasicType.INT, BasicType.BOOLEAN);
	}

	@Override
	public void visit(LogicalAndExpression logicalAndExpression) {
		checkExpression(logicalAndExpression, BasicType.BOOLEAN, BasicType.BOOLEAN);
	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
		checkExpression(logicalOrExpression, BasicType.BOOLEAN, BasicType.BOOLEAN);
	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
		checkExpression(moduloExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
		checkExpression(multiplicationExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
		checkBinaryOperandEqualityOrNull(nonEqualityExpression);
		setType(BasicType.BOOLEAN, nonEqualityExpression);
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
		checkExpression(substractionExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
		setType(BasicType.BOOLEAN, booleanConstantExpression);
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		setType(BasicType.INT, integerConstantExpression);
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		newArrayExpression.getType().accept(this);
		newArrayExpression.getFirstDimension().accept(this);

		expectType(BasicType.INT, newArrayExpression.getFirstDimension());
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		ClassType type = new ClassType(newObjectExpression.getPosition(), newObjectExpression.getIdentifier());
		visit(type);
		newObjectExpression.setType(type);
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		// is inner expression
		if (methodInvocationExpression.getMethodExpression() == null) {
			if (isStaticMethod) {
				// there are no static method
				throwIllegalAccessToNonStaticMemberError(methodInvocationExpression.getPosition());
				return;
			}
			MethodDefinition methodDefinition = currentClassScope.getMethodDefinition(methodInvocationExpression.getMethodIdent());
			if (methodDefinition != null) {
				checkParameterDefinitionAndSetReturnType(methodInvocationExpression, methodDefinition);
			} else {
				throwNoSuchMemberError(currentClassDeclaration.getIdentifier(), currentClassDeclaration.getPosition(),
						methodInvocationExpression.getMethodIdent(), methodInvocationExpression.getPosition());
				return;
			}
		} else {
			// first step in outer left expression
			methodInvocationExpression.getMethodExpression().accept(this);

			Expression leftExpression = methodInvocationExpression.getMethodExpression();

			Type leftExpressionType = leftExpression.getType();

			if (leftExpressionType == null) {
				return; // left expressions failed...
			}

			// if left expression type is != class (e.g. int, boolean, void) then throw error
			if (leftExpressionType.getBasicType() != BasicType.CLASS) {
				throwNoSuchMemberError(leftExpressionType.getIdentifier(), leftExpressionType.getPosition(),
						methodInvocationExpression.getMethodIdent(), methodInvocationExpression.getPosition());
				return;
			}

			// get class scope
			ClassScope classScope = classScopes.get(leftExpressionType.getIdentifier());

			MethodDefinition methodDefinition = null;
			if (classScope != null) {
				methodDefinition = classScope.getMethodDefinition(methodInvocationExpression.getMethodIdent());
			}
			// is there the specified method?
			if (methodDefinition == null) {
				throwNoSuchMemberError(leftExpressionType.getIdentifier(), leftExpressionType.getPosition(),
						methodInvocationExpression.getMethodIdent(), methodInvocationExpression.getPosition());
				return;
			}
			checkParameterDefinitionAndSetReturnType(methodInvocationExpression, methodDefinition);
		}
	}

	private void checkParameterDefinitionAndSetReturnType(MethodInvocationExpression methodInvocationExpression, MethodDefinition methodDefinition) {
		// now check params
		if (methodDefinition.getParameters().length != methodInvocationExpression.getParameters().length) {
			exceptions.add(new InvalidMethodCallException(methodInvocationExpression.getMethodIdent(), methodInvocationExpression.getPosition()));
			return;
		}

		for (int i = 0; i < methodDefinition.getParameters().length; i++) {
			Definition parameterDefinition = methodDefinition.getParameters()[i];
			Expression expression = methodInvocationExpression.getParameters()[i];
			expression.accept(this);

			expectType(parameterDefinition.getType(), expression);
		}

		methodInvocationExpression.setType(methodDefinition.getType());

	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		// first step in outer left expression
		if (variableAccessExpression.getExpression() != null) {
			variableAccessExpression.getExpression().accept(this);
		}

		// is inner expression (no left expression)
		if (variableAccessExpression.getExpression() == null) {
			if (variableAccessExpression.getFieldIdentifier().isDefined()) {
				if (isStaticMethod) {
					if (variableAccessExpression.getFieldIdentifier().getDefinitionScope().getParentScope() == null) {
						// class field has no parent scope since there are no inner classes
						// there are no static fields
						throwIllegalAccessToNonStaticMemberError(variableAccessExpression.getPosition());
						// continue
					}
				}
				variableAccessExpression.setType(variableAccessExpression.getFieldIdentifier().getDefinition().getType());
				variableAccessExpression.setDefinition(variableAccessExpression.getFieldIdentifier().getDefinition());
			} else if (currentClassScope.getFieldDefinition(variableAccessExpression.getFieldIdentifier()) != null) {
				// no static field defined in class scope possible
				if (isStaticMethod) {
					// there are no static fields
					throwIllegalAccessToNonStaticMemberError(variableAccessExpression.getPosition());
					// continue
				}
				variableAccessExpression.setType(currentClassScope.getFieldDefinition(variableAccessExpression.getFieldIdentifier()).getType());
				variableAccessExpression.setDefinition(currentClassScope.getFieldDefinition(variableAccessExpression.getFieldIdentifier()));
				// special case is System
			} else if (variableAccessExpression.getFieldIdentifier().getValue().equals("System")) {
				variableAccessExpression.setType(new ClassType(null, new Symbol("System")));
				variableAccessExpression.setDefinition(null); // TODO: fix me
			} else {
				throwUndefinedSymbolError(variableAccessExpression.getFieldIdentifier(), variableAccessExpression.getPosition());
				return;
			}
		} else {
			Expression leftExpression = variableAccessExpression.getExpression();

			if (leftExpression instanceof ThisExpression && isStaticMethod) {
				return;
			}

			Type leftExpressionType = leftExpression.getType();

			if (leftExpressionType == null) {
				return; // left expressions failed...
			}

			// if left expression type is != class (e.g. int, boolean, void) then throw error
			if (leftExpressionType.getBasicType() != BasicType.CLASS) {
				throwNoSuchMemberError(leftExpressionType.getIdentifier(), leftExpressionType.getPosition(),
						variableAccessExpression.getFieldIdentifier(),
						variableAccessExpression.getPosition());
				return;
			}
			// check if class exists
			ClassScope classScope = classScopes.get(leftExpressionType.getIdentifier());
			if (classScope == null) {
				throwTypeError(variableAccessExpression);
				return;
			}
			// check if member exists in this class
			Definition fieldDef = classScope.getFieldDefinition(variableAccessExpression.getFieldIdentifier());
			if (fieldDef == null) {
				throwNoSuchMemberError(leftExpressionType.getIdentifier(), leftExpressionType.getPosition(),
						variableAccessExpression.getFieldIdentifier(),
						variableAccessExpression.getPosition());
				return;
			}

			variableAccessExpression.setType(fieldDef.getType());
		}
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		Expression arrayExpression = arrayAccessExpression.getArrayExpression();
		Expression indexExpression = arrayAccessExpression.getIndexExpression();
		arrayExpression.accept(this);
		indexExpression.accept(this);

		if (arrayExpression.getType() == null) {
			return; // Already an error thrown in an upper left array part.
		}

		if (arrayExpression.getType().getBasicType() == BasicType.ARRAY) {
			if (arrayExpression.getType().getSubType().getBasicType() == BasicType.STRING_ARGS) {
				throwTypeError(arrayAccessExpression, "Access on main method parameter forbidden.");
			}
			arrayAccessExpression.setType(arrayExpression.getType().getSubType());
		} else {
			throwTypeError(arrayAccessExpression, "Array access on a non array.");
		}
		expectType(BasicType.INT, indexExpression);
	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
		checkExpression(logicalNotExpression, BasicType.BOOLEAN, BasicType.BOOLEAN);
	}

	@Override
	public void visit(NegateExpression negateExpression) {
		checkExpression(negateExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		isExpressionStatement = true;
		boolean isVoidMethod = currentMethodDefinition.getType().getBasicType() == BasicType.VOID;

		if (returnStatement.getOperand() != null) {
			if (isVoidMethod) {
				throwTypeError(returnStatement, "Expected return statement without type.");
				return;
			}
			returnStatement.getOperand().accept(this);
			expectType(currentMethodDefinition.getType(), returnStatement.getOperand());
			returnOnAllPaths = true;
		} else if (!isVoidMethod) {
			throwTypeError(returnStatement);
		}
	}

	@Override
	public void visit(ThisExpression thisExpression) {
		if (isStaticMethod) {
			throwTypeError(thisExpression, "'this' is not allowed in static methods.");
		}
		thisExpression.setType(new ClassType(null, currentClassDeclaration.getIdentifier()));
	}

	@Override
	public void visit(NullExpression nullExpression) {
		setType(BasicType.NULL, nullExpression);
	}

	@Override
	public void visit(Type type) {
		type.setType(type);

		if (type.getBasicType() == BasicType.ARRAY && searchForVoidSubtype(type)) {
			throwTypeError(type);
		}
	}

	private boolean searchForVoidSubtype(Type type) {
		while (type != null && type.getBasicType() == BasicType.ARRAY) {
			type = type.getSubType();
		}
		return type == null || type.getBasicType() == BasicType.VOID;
	}

	@Override
	public void visit(ClassType classType) {
		if (classScopes.containsKey(classType.getIdentifier()) == false) {
			throwTypeError(classType);
			return;
		}
	}

	@Override
	public void visit(Block block) {
		this.symbolTable.enterScope();
		boolean returnInBlockFound = returnOnAllPaths;

		for (Statement statement : block.getStatements()) {
			returnOnAllPaths = false;
			acceptStatement(statement);
			returnInBlockFound |= returnOnAllPaths;
		}

		returnOnAllPaths = returnInBlockFound;
		this.symbolTable.leaveScope();
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		currentClassDeclaration = classDeclaration;
		currentClassScope = classScopes.get(classDeclaration.getIdentifier());

		for (ClassMember classMember : classDeclaration.getMembers()) {
			classMember.accept(this);
		}
		currentClassDeclaration = null;
	}

	@Override
	public void visit(IfStatement ifStatement) {
		AstNode condition = ifStatement.getCondition();
		condition.accept(this);
		expectType(BasicType.BOOLEAN, condition);

		boolean oldReturnOnAllPaths = returnOnAllPaths;
		boolean returnInTrueCase = false;
		boolean returnInFalseCase = false;

		Statement trueCase = ifStatement.getTrueCase();

		returnOnAllPaths = false;
		acceptStatement(trueCase);
		returnInTrueCase = returnOnAllPaths;

		Statement falseCase = ifStatement.getFalseCase();
		if (falseCase != null) {
			returnOnAllPaths = false;
			acceptStatement(falseCase);
			returnInFalseCase = returnOnAllPaths;
		}

		returnOnAllPaths = oldReturnOnAllPaths | (returnInTrueCase & returnInFalseCase);
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		AstNode condition = whileStatement.getCondition();
		condition.accept(this);
		expectType(BasicType.BOOLEAN, condition);

		acceptStatement(whileStatement.getBody());
		returnOnAllPaths = false;
	}

	private void acceptStatement(Statement statement) {
		isExpressionStatement = false;

		statement.accept(this);

		if (statement instanceof Expression && !isExpressionStatement
				&& !(statement instanceof MethodInvocationExpression || statement instanceof NewObjectExpression)) {
			exceptions.add(new NotAnExpressionStatementException(statement.getPosition()));
		}
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
		localVariableDeclaration.getType().accept(this);

		if (hasType(BasicType.VOID, localVariableDeclaration)) {
			throwTypeError(localVariableDeclaration);
		}

		if (localVariableDeclaration.getIdentifier().isDefined()) {
			throwRedefinitionError(localVariableDeclaration.getIdentifier(), localVariableDeclaration.getPosition());
			return;
		}

		symbolTable.insert(localVariableDeclaration.getIdentifier(), new Definition(localVariableDeclaration.getIdentifier(),
				localVariableDeclaration.getType(), localVariableDeclaration));

		Expression expression = localVariableDeclaration.getExpression();
		if (expression != null) {
			expression.accept(this);
			expectType(localVariableDeclaration.getType(), expression);
		}
	}

	@Override
	public void visit(ParameterDefinition parameterDefinition) {
		parameterDefinition.getType().accept(this);

		if (hasType(BasicType.VOID, parameterDefinition)) {
			throwTypeError(parameterDefinition);
		}

		// check if parameter already defined
		if (symbolTable.isDefinedInCurrentScope(parameterDefinition.getIdentifier())) {
			throwRedefinitionError(parameterDefinition.getIdentifier(), parameterDefinition.getPosition());
			return;
		}
		symbolTable.insert(parameterDefinition.getIdentifier(), new Definition(parameterDefinition.getIdentifier(), parameterDefinition.getType(),
				parameterDefinition));
	}

	@Override
	public void visit(Program program) {
		for (ClassDeclaration classDeclaration : program.getClasses()) {
			classDeclaration.accept(this);
		}
	}

	@Override
	public void visit(MethodDeclaration methodDeclaration) {
		isStaticMethod = false;
		visitMethodDeclaration(methodDeclaration);
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		fieldDeclaration.getType().accept(this);
		if (hasType(BasicType.VOID, fieldDeclaration)) {
			throwTypeError(fieldDeclaration);
		}
	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		isStaticMethod = true;
		visitMethodDeclaration(staticMethodDeclaration);
		isStaticMethod = false;
	}

	private void visitMethodDeclaration(MethodDeclaration methodDeclaration) {
		methodDeclaration.getType().accept(this);

		symbolTable = new SymbolTable();
		symbolTable.enterScope();

		for (ParameterDefinition parameterDefinition : methodDeclaration.getParameters()) {
			parameterDefinition.accept(this);
		}

		// visit body
		currentMethodDefinition = currentClassScope.getMethodDefinition(methodDeclaration.getIdentifier());
		if (currentMethodDefinition != null) {
			returnOnAllPaths = false;
			methodDeclaration.getBlock().accept(this);

			// if method has return type, check if all paths have a return statement
			if (currentMethodDefinition.getType().getBasicType() != BasicType.VOID) {
				if (!returnOnAllPaths) {
					exceptions.add(new MissingReturnStatementOnAPathException(methodDeclaration.getPosition(), methodDeclaration.getIdentifier()));
				}
			}
		}

		// leave method scope.
		currentMethodDefinition = null;
		symbolTable.leaveAllScopes();

		methodDeclaration.setNumberOfRequiredLocals(symbolTable.getRequiredLocalVariables());
		symbolTable = null;
	}
}
