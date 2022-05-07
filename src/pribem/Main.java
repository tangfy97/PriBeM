package pribem;

import java.io.IOException;
import java.util.*;

import pribem.Info.Method;

public class Main {
	public static String sourceDirectory = System.getProperty("user.dir") + "/output";
	public static String jarDirectory = System.getProperty("user.dir") + "/data";
	public Set<Method> methods = new HashSet<Method>();
	private Core classReader;
	public String testCp;

	public static void main(String[] args) throws Exception {

		try {
			String sourceDir = jarDirectory;
			String outputDir = sourceDirectory;

			Main main = new Main();
			main.run(sourceDir, outputDir);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void run(String sourceDir, String outputDir) throws Exception {
		internalRun(sourceDir, outputDir);
	}

	private void internalRun(String sourceDir, String outputDir) throws Exception {
		Set<String> testClasses = Core.getAllClassesFromDirectory(jarDirectory);
		String testCp = Core.buildCP(jarDirectory);

		// Cache the methods from the set.
		System.out.println("***** Loading java classes ***** \n");
		classReader = new Core(testCp);
		classReader.loadTestSet(testClasses);
		System.out.println("All finished.");

	}

}
