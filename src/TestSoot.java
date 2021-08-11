import java.io.File;
import java.util.Iterator;
import java.util.Map;

import soot.*;
import soot.jimple.Stmt;
import soot.options.Options;

public class TestSoot extends BodyTransformer {
	public static void main(String[] args)	{

		String mainclass = "HelloThread";

		//set classpath
	    String javapath = System.getProperty("java.class.path");
	    String jredir = System.getProperty("java.home")+"/lib/rt.jar";
	    String path = javapath+File.pathSeparator+jredir;
	    Scene.v().setSootClassPath(path);

            //add an intra-procedural analysis phase to Soot
	    TestSoot analysis = new TestSoot();
	    PackManager.v().getPack("jtp").add(new Transform("jtp.TestSoot", analysis));

            //load and set main class
	    Options.v().set_app(true);
	    SootClass appclass = Scene.v().loadClassAndSupport(mainclass);
	    Scene.v().setMainClass(appclass);
	    Scene.v().loadNecessaryClasses();

            //start working
	    PackManager.v().runPacks();
	}

	@Override
	protected void internalTransform(Body b, String phaseName,
		Map<String, String> options) {

		Iterator<Unit> it = b.getUnits().snapshotIterator();
	    while(it.hasNext()){
	    	Stmt stmt = (Stmt)it.next();

	    	System.out.println(stmt);
	    }
	}
}
