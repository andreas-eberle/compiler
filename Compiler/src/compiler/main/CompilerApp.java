package compiler.main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import compiler.StringTable;
import compiler.ast.AstNode;
import compiler.firm.FirmGraphGenerator;
import compiler.firm.FirmUtils;
import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.parser.printer.PrettyPrinter;
import compiler.semantic.SemanticCheckResults;
import compiler.semantic.SemanticChecker;
import compiler.semantic.exceptions.SemanticAnalysisException;

public final class CompilerApp {

	private static final String LEXTEST = "lextest";
	private static final String PRETTY_PRINT_AST = "print-ast";
	private static final String CHECK = "check";
	private static final String DEBUG = "debug";
	private static final String COMPILE_FIRM = "compile-firm";
	private static final String GRAPH_FIRM = "graph-firm";

	/**
	 * Private constructor, as no objects of this class shall be created.
	 */
	private CompilerApp() {
	}

	public static void main(String args[]) {
		int exitCode = execute(args);
		System.exit(exitCode);
	}

	private static int execute(String[] args) {
		boolean debug = false;

		Options options = new Options();
		options.addOption("h", "help", false, "print this message");
		options.addOption(null, LEXTEST, false, "print tokens generated by lexer");
		options.addOption(null, PRETTY_PRINT_AST, false, "print ast generated by parser with pretty-printer as code");
		// This option (CHECK) is necessary to be compatible with the commands from the advisors.
		options.addOption(null, CHECK, false, "checks if the given source file is valid code.");
		options.addOption(null, DEBUG, false, "prints more detailed error messages (only useful in case of a crash)");
		options.addOption(null, COMPILE_FIRM, false, "use the firm backend to produce amd64 code.");
		options.addOption(null, GRAPH_FIRM, false, "dump a firm graph to the current directory.");

		CommandLineParser commandLineParser = new BasicParser();
		try {
			CommandLine cmd = commandLineParser.parse(options, args);
			debug = cmd.hasOption(DEBUG);

			if (cmd.hasOption("help"))
			{
				printHelp(options);
				return 0;
			}

			String[] remainingArgs = cmd.getArgs();
			if (remainingArgs.length == 1) {
				Path file = Paths.get(remainingArgs[0]);

				try {
					// Execute Lexer
					Lexer lexer = new Lexer(Files.newBufferedReader(file, StandardCharsets.US_ASCII), new StringTable());

					if (cmd.hasOption(LEXTEST)) {
						return executeLextest(lexer);
					}

					// Execute Parser
					Parser parser = new Parser(lexer);

					AstNode ast;
					try {
						ast = parser.parse();
					} catch (ParsingFailedException e) {
						System.err.println(e.toString());
						return 1;
					}

					if (cmd.hasOption(PRETTY_PRINT_AST)) {
						System.out.print(PrettyPrinter.prettyPrint(ast));
					}

					SemanticCheckResults semanticResult = SemanticChecker.checkSemantic(ast);
					if (semanticResult.hasErrors()) {
						for (SemanticAnalysisException curr : semanticResult.getExceptions()) {
							System.out.println(curr.getMessage());
						}
						return 1;
					}

					FirmUtils.initFirm();
					FirmGraphGenerator.transformToFirm(ast, semanticResult.getClassScopes());

					if (cmd.hasOption(COMPILE_FIRM)) {
						FirmUtils.createBinary("a");
					}

					if (cmd.hasOption(GRAPH_FIRM)) {
						FirmUtils.createFirmGraph();
					}
					FirmUtils.finishFirm();

					return 0;
				} catch (IOException e) {
					System.err.println("Error accessing file " + file + ": " + e.getMessage());
				}
			}
		} catch (ParseException e) {
			System.err.println("Wrong Command Line Parameters: " + e.getMessage());
		} catch (Throwable t) {
			System.err.println("Unexpected exception occured: " + t.getMessage());
			if (debug) {
				t.printStackTrace();
			}
		}
		printHelp(options);
		return 1;
	}

	private static int executeLextest(Lexer lexer) throws IOException {
		Token token;
		do {
			token = lexer.getNextToken();
			System.out.println(token.getTokenString());
		} while (token.getType() != TokenType.EOF);
		return 0;
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("compiler [options] [file]", options);
	}
}