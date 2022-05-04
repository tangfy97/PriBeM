package pribm;
import java.io.IOException;
import java.util.*;

import pribm.Info.Method;


public class Main {
	public static String sourceDirectory = System.getProperty("user.dir")+"/output";
    public static String jarDirectory = System.getProperty("user.dir")+"/data";
    public Set<Method> methods = new HashSet<Method>();
    private ReadClasses classReader;
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
            throws Exception {
		Set<String> testClasses = ReadClasses.getAllClassesFromDirectory(jarDirectory);
		String testCp = ReadClasses.buildCP(jarDirectory);
		
		// Cache the methods from the set.
        System.out.println("***** Loading java classes ***** \n");
		classReader = new ReadClasses(testCp);
		classReader.loadTestSet(testClasses);
		System.out.println("All finished.");
		
	}
	
	
}
