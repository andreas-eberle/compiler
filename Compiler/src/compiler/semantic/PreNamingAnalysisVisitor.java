package compiler.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.Block;
import compiler.ast.Program;
import compiler.ast.declaration.ClassDeclaration;
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
import compiler.ast.statement.IfStatement;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.MethodInvocationExpression;
import compiler.ast.statement.NewArrayExpression;
import compiler.ast.statement.NewObjectExpression;
import compiler.ast.statement.NullExpression;
import compiler.ast.statement.ThisExpression;
import compiler.ast.statement.VariableAccessExpression;
import compiler.ast.statement.WhileStatement;
import compiler.ast.statement.binary.AdditionExpression;
import compiler.ast.statement.binary.AssignmentExpression;
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
import compiler.lexer.Position;
import compiler.semantic.exceptions.MultipleStaticMethodsException;
import compiler.semantic.exceptions.NoMainFoundException;
import compiler.semantic.exceptions.ReDeclarationErrorException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.semantic.exceptions.TypeErrorException;

public class PreNamingAnalysisVisitor implements AstVisitor {

	private final HashMap<Symbol, ClassScope> classScopes = new HashMap<>();

	private HashMap<Symbol, FieldDeclaration> currentFieldsMap = null;
	private HashMap<Symbol, MethodMemberDeclaration> currentMethodsMap = null;

	private boolean mainFound = false;
	private final List<SemanticAnalysisException> exceptions = new ArrayList<>();

	public HashMap<Symbol, ClassScope> getClassScopes() {
		return classScopes;
	}

	public List<SemanticAnalysisException> getExceptions() {
		return exceptions;
	}

	public boolean hasMain() {
		return mainFound;
	}

	@Override
	public void visit(Program program) {
		for (ClassDeclaration curr : program.getClasses()) {
			curr.accept(this);
		}

		if (!mainFound) {
			exceptions.add(new NoMainFoundException());
		}
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		currentFieldsMap = new HashMap<>();
		currentMethodsMap = new HashMap<>();

		for (MemberDeclaration curr : classDeclaration.getMembers()) {
			curr.accept(this);
		}

		Symbol identifier = classDeclaration.getIdentifier();
		if (classScopes.containsKey(identifier)) {
			throwReDeclarationError(identifier, classDeclaration.getPosition());
		} else {
			getClassScopes().put(identifier, new ClassScope(classDeclaration, currentFieldsMap, currentMethodsMap));
			currentFieldsMap = null;
			currentMethodsMap = null;
		}
	}

	@Override
	public void visit(MethodDeclaration methodDeclaration) {
		checkAndInsertDeclaration(methodDeclaration);
	}

	@Override
	public void visit(NativeMethodDeclaration nativeMethodDeclaration) {
		checkAndInsertDeclaration(nativeMethodDeclaration);
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		checkAndInsertDeclaration(fieldDeclaration);
	}

	@Override
	public void visit(MainMethodDeclaration mainMethodDeclaration) {
		if (mainFound) {
			throwMultipleStaticMethodsError(mainMethodDeclaration.getPosition());
			return;
		}

		Type returnType = mainMethodDeclaration.getType();
		if (returnType.getBasicType() != BasicType.VOID) {
			throwTypeError(mainMethodDeclaration, "Invalid return type for main method.");
			return;
		}

		Symbol identifier = mainMethodDeclaration.getIdentifier();
		if (!"main".equals(identifier.getValue())) {
			throwTypeError(mainMethodDeclaration, "'public static void' method must be called 'main'.");
			return;
		}

		if (mainMethodDeclaration.getParameters().size() != 1) {
			throwTypeError(mainMethodDeclaration, "'public static void main' method must have a single argument of type String[].");
			return;
		}

		ParameterDeclaration parameter = mainMethodDeclaration.getParameters().get(0);
		Type parameterType = parameter.getType();
		if (parameterType.getBasicType() != BasicType.ARRAY || !"String".equals(parameterType.getSubType().getIdentifier().getValue())) {
			throwTypeError(mainMethodDeclaration, "'public static void main' method must have a single argument of type String[].");
			return;
		}

		mainFound = true;
		mainMethodDeclaration.getParameters().get(0)
				.setType(new Type(parameter.getPosition(), BasicType.STRING_ARGS));
		checkAndInsertDeclaration(mainMethodDeclaration);
	}

	private void checkAndInsertDeclaration(MethodMemberDeclaration declaration) {
		if (currentMethodsMap.containsKey(declaration.getIdentifier())) {
			throwReDeclarationError(declaration.getIdentifier(), declaration.getPosition());
			return;
		}

		currentMethodsMap.put(declaration.getIdentifier(), declaration);
	}

	private void checkAndInsertDeclaration(FieldDeclaration declaration) {
		if (currentFieldsMap.containsKey(declaration.getIdentifier())) {
			throwReDeclarationError(declaration.getIdentifier(), declaration.getPosition());
			return;
		}

		currentFieldsMap.put(declaration.getIdentifier(), declaration);
	}

	private void throwTypeError(AstNode astNode, String message) {
		exceptions.add(new TypeErrorException(astNode, message));
	}

	private void throwReDeclarationError(Symbol identifier, Position reDeclaration) {
		exceptions.add(new ReDeclarationErrorException(identifier, reDeclaration));
	}

	private void throwMultipleStaticMethodsError(Position declarationPosition) {
		exceptions.add(new MultipleStaticMethodsException(declarationPosition));
	}

	/*
	 * not needed visitor methods follow below
	 */

	@Override
	public void visit(AdditionExpression additionExpression) {
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
	}

	@Override
	public void visit(LogicalAndExpression logicalAndExpression) {
	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
	}

	@Override
	public void visit(NegateExpression negateExpression) {
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
	}

	@Override
	public void visit(ThisExpression thisExpression) {
	}

	@Override
	public void visit(NullExpression nullExpression) {
	}

	@Override
	public void visit(Type type) {
	}

	@Override
	public void visit(ClassType classType) {
	}

	@Override
	public void visit(ArrayType arrayType) {
	}

	@Override
	public void visit(Block block) {
	}

	@Override
	public void visit(IfStatement ifStatement) {
	}

	@Override
	public void visit(WhileStatement whileStatement) {
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
	}

	@Override
	public void visit(ParameterDeclaration parameterDeclaration) {
	}

}
