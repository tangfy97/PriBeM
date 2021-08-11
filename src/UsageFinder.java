import soot.*;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.options.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class UsageFinder {
    public static String javapath = System.getProperty("java.class.path");
	public static String jredir = System.getProperty("java.home")+"/lib/rt.jar";
	public static String path = javapath+File.pathSeparator+jredir;
    public static String sourceDirectory = System.getProperty("user.dir");
    public static String clsName = "UsageExample";


    public static void setupSoot() {
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(path);
        SootClass sc = Scene.v().loadClassAndSupport(clsName);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();
    }

    public static boolean doesInvokeTheMethod(Unit u, String methodSubsignature, String classSignature) {
        AtomicBoolean result = new AtomicBoolean(false);
        u.apply(new AbstractStmtSwitch() {
            @Override
            public void caseInvokeStmt(InvokeStmt invokeStmt) {
                String invokedSubsignature = invokeStmt.getInvokeExpr().getMethod().getSubSignature();
                String invokedClassSignature = invokeStmt.getInvokeExpr().getMethod().getDeclaringClass().getName();
                Type returnType = invokeStmt.getInvokeExpr().getMethod().getReturnType();
                List<Type> parameterTypes = new ArrayList<Type>();
                parameterTypes = invokeStmt.getInvokeExpr().getMethod().getParameterTypes();
                if (invokedSubsignature.equals(methodSubsignature)) {
                    if (classSignature == null || invokedClassSignature.equals(classSignature)) {
                        result.set(true);
                    }
                }

            }
        });
        return result.get();
    }
    
    
    /*
    protected List<Type> getParameterTypes(final Method method) {
        // retrieve all parameter types
        List<Type> parameterTypes = new ArrayList<Type>();
        if (method.getParameters() != null) {
          List<? extends CharSequence> parameters = method.getParameterTypes();

          for (CharSequence t : parameters) {
            Type type = DexType.toSoot(t.toString());
            parameterTypes.add(type);
          }
        }
        return parameterTypes;
      }
     */
    public static void main(String[] args) {
        //if (args.length == 0) {
        //    System.err.println("Please provide a method subsignature to search for its usages.");
        //    return;
        //}
        setupSoot();
        //String usageMethodSubsignature = args[0];
        String usageMethodSubsignature = "void println(java.lang.String)";
        String usageClassSignature = "java.io.PrintStream";
        String classMessage = " of the class " + usageClassSignature;
        //String usageClassSignature = null;
        //String classMessage = "";
        //if (args.length > 1) {
        //    usageClassSignature = args[1];
        //    classMessage = " of the class " + usageClassSignature;
        //}
        System.out.println("Searching the usages of method " + usageMethodSubsignature + classMessage + "...");
        SootClass mainClass = Scene.v().getSootClass(clsName);
        for (SootMethod sm : mainClass.getMethods()) {
            JimpleBody body = (JimpleBody) sm.retrieveActiveBody();


            List<Unit> usageFound = new ArrayList<>();
            for (Iterator<Unit> it = body.getUnits().snapshotIterator(); it.hasNext(); ) {
                Unit u = it.next();
                if (doesInvokeTheMethod(u, usageMethodSubsignature, usageClassSignature)&& ((sm.getReturnType().toString()=="void")||(sm.getReturnType().toString()=="boolean")))
                	System.out.println("Found one safe invocation");
                    usageFound.add(u);
            }
            if (usageFound.size() > 0) {
                System.out.println(usageFound.size() + " Usage(s) found in the method " + sm.getSignature() + " with return type: " + sm.getReturnType() + " and parameter types: " + sm.getParameterTypes());
                for (Unit u : usageFound) {
                    System.out.println("   " + u.toString());
                }
            }
            if (usageFound.size() == 0) {
            	System.out.println("No result");
            }

        }
    }
}
