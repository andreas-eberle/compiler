package compiler.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;

import org.junit.Test;

import compiler.Symbol;
import compiler.ast.declaration.FieldDeclaration;
import compiler.ast.declaration.MethodMemberDeclaration;

public class ClassScopeTest {

	@Test
	public void testEqualsAndHashCode0() {
		ClassScope s1 = new ClassScope(null, null);

		assertEquals(s1, s1);
		assertEquals(s1.hashCode(), s1.hashCode());
	}

	@Test
	public void testEqualsAndHashCode1() {
		ClassScope s1 = new ClassScope(null, null);
		ClassScope s2 = new ClassScope(null, null);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testEqualsAndHashCode2() {
		ClassScope s1 = new ClassScope(new HashMap<Symbol, FieldDeclaration>(), null);
		ClassScope s2 = new ClassScope(null, null);

		assertNotEquals(s1, s2);
		assertNotEquals(s2, s1);
	}

	@Test
	public void testEqualsAndHashCode3() {
		ClassScope s1 = new ClassScope(new HashMap<Symbol, FieldDeclaration>(), null);
		ClassScope s2 = new ClassScope(new HashMap<Symbol, FieldDeclaration>(), null);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testEqualsAndHashCode4() {
		ClassScope s1 = new ClassScope(new HashMap<Symbol, FieldDeclaration>(), new HashMap<Symbol, MethodMemberDeclaration>());
		ClassScope s2 = new ClassScope(new HashMap<Symbol, FieldDeclaration>(), null);

		assertNotEquals(s1, s2);
		assertNotEquals(s2, s1);
	}

	@Test
	public void testEqualsAndHashCode5() {
		ClassScope s1 = new ClassScope(new HashMap<Symbol, FieldDeclaration>(), new HashMap<Symbol, MethodMemberDeclaration>());
		ClassScope s2 = new ClassScope(new HashMap<Symbol, FieldDeclaration>(), new HashMap<Symbol, MethodMemberDeclaration>());

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testEqualsAndHashCode6() {
		ClassScope s1 = new ClassScope(null, null);

		assertFalse(s1.equals(null));
	}

	@Test
	public void testEqualsAndHashCode7() {
		ClassScope s1 = new ClassScope(null, null);

		assertFalse(s1.equals(""));
	}
}
