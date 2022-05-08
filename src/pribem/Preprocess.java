package pribem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import soot.jimple.spark.SparkTransformer;

public class Preprocess {

	public static String bomPath = System.getProperty("user.dir") + "/basic/BOM.txt";
	public static String bimPath = System.getProperty("user.dir") + "/basic/BIM.txt";
	public static String eomPath = System.getProperty("user.dir") + "/basic/EOM.txt";
	public static String eimPath = System.getProperty("user.dir") + "/basic/EIM.txt";

	public static Set<String> loadBOM() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(bomPath));
		Set<String> BOM = new HashSet<String>();
		String line = bufReader.readLine();
		while (line != null) {
			BOM.add(line);
			line = bufReader.readLine();
		}
		bufReader.close();
		System.out.println("BOMS are loaded with " + BOM.size() + " methods.");
		return BOM;
	}

	public static Set<String> loadBIM() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(bimPath));
		Set<String> BIM = new HashSet<String>();
		String line = bufReader.readLine();
		while (line != null) {
			BIM.add(line);
			line = bufReader.readLine();
		}
		bufReader.close();
		System.out.println("BIMs are loaded with " + BIM.size() + " methods.");
		return BIM;
	}

	public static Set<String> loadEOM() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(eomPath));
		Set<String> EOM = new HashSet<String>();
		String line = bufReader.readLine();
		while (line != null) {
			EOM.add(line);
			line = bufReader.readLine();
		}
		bufReader.close();
		System.out.println("EOMs are loaded with " + EOM.size() + " methods.");
		return EOM;
	}

	public static Set<String> loadEIM() throws Exception {
		BufferedReader bufReader = new BufferedReader(new FileReader(eimPath));
		Set<String> EIM = new HashSet<String>();
		String line = bufReader.readLine();
		while (line != null) {
			EIM.add(line);
			line = bufReader.readLine();
		}
		bufReader.close();
		System.out.println("EIMs are loaded with " + EIM.size() + " methods.");
		return EIM;
	}

	static void setSparkPointsToAnalysis() {
		System.out.println("[SPARK] Starting analysis ...");

		HashMap<String, String> opt = new HashMap<String, String>();

		opt.put("enabled", "true");
		opt.put("verbose", "true");
		opt.put("ignore-types", "false");
		opt.put("force-gc", "false");
		opt.put("pre-jimplify", "false");
		opt.put("vta", "false");
		opt.put("rta", "false");
		opt.put("field-based", "true");
		opt.put("types-for-sites", "false");
		opt.put("merge-stringbuffer", "true");
		opt.put("string-constants", "true");
		opt.put("simulate-natives", "true");
		opt.put("simple-edges-bidirectional", "false");
		opt.put("on-fly-cg", "true");
		opt.put("simplify-offline", "false");
		opt.put("simplify-sccs", "false");
		opt.put("ignore-types-for-sccs", "false");
		opt.put("propagator", "worklist");
		opt.put("set-impl", "double");
		opt.put("double-set-old", "hybrid");
		opt.put("double-set-new", "hybrid");
		opt.put("dump-html", "false");
		opt.put("dump-pag", "false");
		opt.put("dump-solution", "false");
		opt.put("topo-sort", "false");
		opt.put("dump-types", "true");
		opt.put("class-method-var", "true");
		opt.put("dump-answer", "false");
		opt.put("add-tags", "false");
		opt.put("set-mass", "false");

		SparkTransformer.v().transform("", opt);

		System.out.println("[SPARK] Done! \n\n");
	}
}
