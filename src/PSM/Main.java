package PSM;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import PSM.Info.Method;

import java.*;
import soot.*;


public class Main {
	public static String sourceDirectory = System.getProperty("user.dir")+"/output";
    public static String jarDirectory = System.getProperty("user.dir")+"/examples";
    public Set<Method> methods = new HashSet<Method>();
    //private FeatureDetector featureDetector;
    private ReadClasses classReader;
    public String testCp;
	
	public static void main(String[] args) throws IOException,InterruptedException {
		
		try {/*
            if (args.length != 2) {
                System.err.println("");
                System.err.println(
                        "Usage: java de.fraunhofer.iem.swan.Main <source-dir> <train-sourcecode> <train-json> <output-dir>\n");
                System.err.println("<source-dir>:\tDirectory with all JAR files or source code of the Test Data.");
                System.err.println("\t\tThis is the actual user library being evaluated.\n");
                System.err.println("<output-dir>:\tDirectory where the output should be written.\n");
                return;
            }*/

            // Get configuration options from command line arguments.
            String sourceDir = jarDirectory;
            //String trainSourceCode = args[1].equals("internal") ? null : args[1];
            //String trainJson = args[2].equals("internal") ? null : args[2];
            String outputDir = sourceDirectory;

            Main main = new Main();
            main.run(sourceDir, outputDir);
            // System.out.println("Done.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		
	}
	
	public void run(String sourceDir, String outputDir) throws IOException, InterruptedException {
		// This helper object keeps track of created temporary directories and files to
        // to be deleted before exiting the
        // application.
        FileUtility fileUtility = new FileUtility();

        try {

            internalRun(sourceDir, outputDir);

        } finally {

            // Delete temporary files and folders that have been created.
            fileUtility.dispose();
        }
    }
	
	private void internalRun(String sourceDir, String outputDir)
            throws IOException, InterruptedException {
		Set<String> testClasses = ReadClasses.getAllClassesFromDirectory(jarDirectory);
		String testCp = ReadClasses.buildCP(jarDirectory);
		//System.out.print(jarDirectory + "\n");
		
		/*
		//Cache the features
		System.out.println("***** Loading features *****");
		featureDetector = new FeatureDetector(testCp);
		featureDetector.initializeFeatures(0); // use 0 for all feature instances
		*/
		
		// Cache the methods from the set.
        System.out.println("***** Loading java classes ***** \n");
		classReader = new ReadClasses(testCp);
		classReader.loadTestSet(testClasses);
		classReader.featureChecker();
		
	}
	
	
}
