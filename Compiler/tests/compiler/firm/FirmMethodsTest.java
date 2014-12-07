package compiler.firm;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compiler.utils.TestUtils;

public class FirmMethodsTest {

	@Before
	public void setUp() {
		FirmUtils.initFirm();
	}

	@After
	public void tearDown() {
		FirmUtils.finishFirm();
	}

	@Test
	public void testjFirmInit() throws Exception {

		compiler.ast.Program ast = TestUtils.getAstForFile("firmdata/methodsTest.java");
		assertEquals(1, ast.getClasses().size());

		// SemanticCheckResults semanticResult = SemanticChecker.checkSemantic(ast);

		// final FirmHierarchy hierarchy = new FirmHierarchy();
		// hierarchy.initialize(semanticResult.getClassScopes());

		// FirmGenerationVisitor firmGen = new FirmGenerationVisitor(hierarchy);
		// for (ClassMember classMember : ast.getClasses().get(0).getMembers()) {
		// classMember.accept(firmGen);
		// }

		FirmTestUtils.assertExportEquals("firmdata/testjFirmInit");
	}
}