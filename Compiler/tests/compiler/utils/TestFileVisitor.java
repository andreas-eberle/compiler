package compiler.utils;

import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;

/**
 * common class to test that expected output matches compiler's output
 */
@Ignore
public class TestFileVisitor extends SimpleFileVisitor<Path> {

	public interface FileTester {
		void testSourceFile(Path sourceFilePath, Path expectedResultFilePath) throws Exception;
	}

	private static final String JAVA_EXTENSION = ".java";

	private final String expectedResultFileExtension;
	private final PathMatcher matcher;
	private final FileTester fileTester;
	private final List<Path> failedTestsList = new ArrayList<>();

	public TestFileVisitor(String expectedResultFileExtension, FileTester fileTester) {
		this.expectedResultFileExtension = expectedResultFileExtension;
		matcher = FileSystems.getDefault().getPathMatcher("glob:*" + expectedResultFileExtension);
		this.fileTester = fileTester;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			String parseFilename = name.toString();
			String sourceFilename = parseFilename.replace(expectedResultFileExtension, JAVA_EXTENSION);

			Path sourceFilePath = file.getParent().resolve(sourceFilename);

			try {
				if (!Files.exists(sourceFilePath)) {
					Assert.fail("cannot find program to output " + sourceFilePath);
				}

				fileTester.testSourceFile(sourceFilePath, file);
			} catch (Throwable e) {
				System.err.println("Test for file = " + file + " failed");
				e.printStackTrace();
				failedTestsList.add(file);
			}

		}
		return FileVisitResult.CONTINUE;
	}

	public void checkForFailedTests() {
		if (failedTestsList.size() != 0) {
			Assert.fail("Tests for " + failedTestsList.size() + " test(s) failed");
		}
	}
}
