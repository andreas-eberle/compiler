package compiler.parser;

import java.io.IOException;

import org.junit.Test;

import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class ParseBlockStatementTest {
	private final PrivateMethodCaller caller = new PrivateMethodCaller(Parser.class);;

	@Test
	public void testIdentIdentEqIdent() throws IOException {
		Parser parser = TestUtils.initParser("test test = test;");
		caller.call("parseBlockStatement", parser);
	}

	@Test
	public void testNewArray() throws IOException {
		Parser parser = TestUtils.initParser("A[] a = new A[3];");
		caller.call("parseBlockStatement", parser);
	}

	@Test
	public void testArrayAssignment() throws IOException {
		Parser parser = TestUtils.initParser("b[c] = 3;");
		caller.call("parseBlockStatement", parser);
	}
}