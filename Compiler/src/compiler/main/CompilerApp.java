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
import compiler.firm.FirmUtils;
import compiler.firm.generation.FirmGraphGenerator;
import compiler.firm.optimization.FirmOptimizer;
import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.parser.printer.PrettyPrinter;
import compiler.semantic.SemanticCheckResults;
import compiler.semantic.SemanticChecker;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.utils.Utils;

public final class CompilerApp {

	private static final String LEXTEST = "lextest";
	private static final String PRETTY_PRINT_AST = "print-ast";
	private static final String CHECK = "check";
	private static final String DEBUG = "debug";
	private static final String GRAPH_FIRM = "graph-firm";
	private static final String OUTPUT_ASSEMBLER = "assembler";
	private static final String COMPILE_FIRM = "compile-firm";
	private static final String NO_OPT = "no-opt";
	private static final String C_INCLUDE = "c-include";

	/**
	 * Private constructor, as no objects of this class shall be created.
	 */
	private CompilerApp() {
	}

	public static void main(final String args[]) {
		Utils.getThreadFactory(Utils.DEFAULT_STACK_SIZE_MB).newThread(new Runnable() {
			@Override
			public void run() {
				int exitCode = execute(args);
				System.exit(exitCode);
			}
		}).start();
	}

	private static int execute(String[] args) {
		boolean debug = false;

		Options options = new Options();
		options.addOption("h", "help", false, "print this message");
		options.addOption(null, LEXTEST, false, "print tokens generated by lexer");
		options.addOption(null, PRETTY_PRINT_AST, false, "print ast generated by parser with pretty-printer as code");
		options.addOption(null, CHECK, false, "checks if the given source file is valid code.");

		options.addOption(null, DEBUG, false, "prints more detailed error messages (only useful in case of a crash)");
		options.addOption(null, GRAPH_FIRM, false, "dump a firm graph to the current directory.");
		options.addOption("s", null, true, "Used to define the suffix of the dumped firm graph. (Only to be used with --"
				+ GRAPH_FIRM + ")");
		options.addOption(null, OUTPUT_ASSEMBLER, false, "outputs the generated assembler into file assembler.s. (Only to be used with --"
				+ COMPILE_FIRM + ")");
		options.addOption(null, COMPILE_FIRM, false, "use the firm backend to produce amd64 code.");
		options.addOption("o", null, true, "Used to define the filename/path of the generated executable. (Only to be used with --"
				+ COMPILE_FIRM + ")");
		options.addOption(null, NO_OPT, false, "deactivate optimizations");
		options.addOption(null, C_INCLUDE, true, "Compile the given file and use it for the mapping of native methods.");

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
					StringTable stringTable = new StringTable();
					Lexer lexer = new Lexer(Files.newBufferedReader(file, StandardCharsets.US_ASCII), stringTable);

					if (cmd.hasOption(LEXTEST)) {
						return executeLextest(lexer);
					}

					// Execute Parser
					Parser parser = new Parser(lexer);

					AstNode ast;
					try {
						ast = parser.parse();
					} catch (ParsingFailedException e) {
						e.printParserExceptions();
						return 1;
					}

					if (cmd.hasOption(PRETTY_PRINT_AST)) {
						System.out.print(PrettyPrinter.prettyPrint(ast));
						return 0;
					}

					SemanticCheckResults semanticResult = SemanticChecker.checkSemantic(ast, stringTable);
					if (semanticResult.hasErrors()) {
						for (SemanticAnalysisException curr : semanticResult.getExceptions()) {
							System.err.println(curr.getMessage());
						}
						return 1;
					}

					if (cmd.hasOption(CHECK)) {
						return 0; // Abort execution, if only check is required
					}

					FirmUtils.initFirm();
					FirmGraphGenerator.transformToFirm(ast, semanticResult.getClassScopes());

					FirmUtils.highToLowLevel();

					if (!cmd.hasOption(NO_OPT)) {
						FirmOptimizer.optimize();
					}

					if (cmd.hasOption(GRAPH_FIRM)) {
						String suffix = "";
						if (cmd.hasOption('s')) {
							suffix = cmd.getOptionValue('s');
						}
						FirmUtils.createFirmGraph(suffix);
					}

					int result = 0;
					if (cmd.hasOption(COMPILE_FIRM)) {
						String outputFile;
						if (cmd.hasOption('o')) {
							outputFile = cmd.getOptionValue('o');
						} else {
							outputFile = Utils.getBinaryFileName("a");
						}

						result = FirmUtils.createBinary(outputFile, cmd.hasOption(OUTPUT_ASSEMBLER), cmd.getOptionValue(C_INCLUDE));
					}

					if (!cmd.hasOption(COMPILE_FIRM) && cmd.hasOption(OUTPUT_ASSEMBLER)) {
						String outputFile;
						if (cmd.hasOption('o')) {
							outputFile = cmd.getOptionValue('o');
						} else {
							outputFile = Utils.getBinaryFileName("assembler");
						}

						FirmUtils.createAssembler(outputFile);
					}

					FirmUtils.finishFirm();

					return result;
				} catch (IOException e) {
					System.err.println("Error accessing file " + file + ": " + e.getMessage());
				}
			}
		} catch (ParseException e) {
			System.err.println("Wrong Command Line Parameters: " + e.getMessage());
		} catch (Throwable t) {
			System.err.println("Unexpected exception occured: " + t.getMessage());
			t.printStackTrace();
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