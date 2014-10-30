package compiler.lexer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import compiler.StringTable;

/**
 * Test for correct "lexing" of integer literals
 *
 * @author effenok
 */
public class LexerIntegersTest {

	private StringTable stringTable;

	@Before
	public void setUp() throws Exception {
		stringTable = new StringTable();
	}

	@Test
	public void testDigits() throws Exception {
		String[] literals = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		testIntegerLiterals(literals);
	}

	@Test
	public void testIntegers() throws Exception {
		String[] literals = { "10", "17", "22", "3082034", "4222224", "5642572", "645", "72222", "84", "95711360398520" };
		testIntegerLiterals(literals);
	}

	@Test
	public void testNoNulls() throws Exception {
		String literal = "01234";
		Lexer lexer = initLexer(literal);
		Token tok1 = lexer.getNextToken();
		Token tok2 = lexer.getNextToken();
		Assert.assertEquals(TokenType.INTEGER, tok1.getType());
		Assert.assertEquals("0", tok1.getValue().getValue());
		Assert.assertEquals(TokenType.INTEGER, tok2.getType());
		Assert.assertEquals("1234", tok2.getValue().getValue());
	}

	@Test
	public void testNegativeNumbers() throws Exception {
		String[] literals = { "-0", "-13", "-200", "-3639293", "-478787", "-533335" };
		testNegativeIntegerLiterals(literals);
	}

	private void testIntegerLiterals(String[] literals) throws IOException {
		for (String literal : literals) {
			Lexer lexer = initLexer(literal);
			Token literalToken = lexer.getNextToken();
			Token eof = lexer.getNextToken();

			Assert.assertEquals(TokenType.INTEGER, literalToken.getType());
			Assert.assertEquals(literal, literalToken.getValue().getValue());

			Assert.assertEquals(TokenType.EOF, eof.getType());
		}
	}

	private void testNegativeIntegerLiterals(String[] literals) throws IOException {
		for (String literal : literals) {
			Lexer lexer = initLexer(literal);
			Token minusToken = lexer.getNextToken();
			Token literalToken = lexer.getNextToken();
			Token eof = lexer.getNextToken();

			Assert.assertEquals(TokenType.SUBTRACT, minusToken.getType());
			Assert.assertEquals(TokenType.INTEGER, literalToken.getType());
			// TODO check javadoc for substring
			Assert.assertEquals(literal.substring(1, literal.length()), literalToken.getValue().getValue());
			Assert.assertEquals(TokenType.EOF, eof.getType());
		}
	}

	private Lexer initLexer(String program) throws IOException {
		BufferedInputStream is =
				new BufferedInputStream(new ByteArrayInputStream(program.getBytes(StandardCharsets.US_ASCII)));
		return new Lexer(is, stringTable);
	}
}