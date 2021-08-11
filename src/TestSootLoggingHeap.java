import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.options.Options;

public class TestSootLoggingHeap extends BodyTransformer {

    private static SootMethodRef logFieldAccMethod;

    public static void main(String[] args)	{

        String mainclass = "HelloThread";

        //output Jimple
        //Options.v().set_output_format(1);

//		//set classpath
        String javapath = System.getProperty("java.class.path");
        String jredir = System.getProperty("java.home")+"/lib/rt.jar";
        String path = javapath+File.pathSeparator+jredir;
        Scene.v().setSootClassPath(path);

        //add an intra-procedural analysis phase to Soot
        TestSootLoggingHeap analysis = new TestSootLoggingHeap();
        PackManager.v().getPack("jtp").add(new Transform("jtp.TestSootLoggingHeap", analysis));

        //load and set main class
        Options.v().set_app(true);
        SootClass appclass = Scene.v().loadClassAndSupport(mainclass);
        Scene.v().setMainClass(appclass);
        SootClass logClass = Scene.v().loadClassAndSupport("Log");
        logFieldAccMethod = logClass.getMethod("void logFieldAcc(java.lang.Object,java.lang.String,boolean,boolean)").makeRef();
        Scene.v().loadNecessaryClasses();

        //start working
        PackManager.v().runPacks();

        PackManager.v().writeOutput();
    }

    @Override
    protected void internalTransform(Body b, String phaseName,
                                     Map<String, String> options) {

        //we don't instrument Log class
        if(!b.getMethod().getDeclaringClass().getName().equals("Log"))
        {
            Iterator<Unit> it = b.getUnits().snapshotIterator();
            while(it.hasNext()){
                Stmt stmt = (Stmt)it.next();
                if (stmt.containsFieldRef()) {
                    SootField field = stmt.getFieldRef().getField();
                    boolean isStatic = field.isStatic();
                    String name = field.getSignature();
                    boolean isWrite = true;
                    Thread o = Thread.currentThread();
                    /*
                        This if/else statement determines if the action is read or write
                        The motivation for this was that for every statement, I printed
                        the return value of stmt.getFieldRefBox() and manually looked at each
                        one. The return type of this method is a ValueBox type. I found that
                        write statements returned LinkedVariableBox types and read statements
                        returned LinkedRValueBox types.
                     */
                    if(stmt.getFieldRefBox().toString().contains("LinkedVariableBox"))
                        isWrite = true;
                    else
                        isWrite = false;
                    /*
                        The given output matches that of the instructions, however the thread names are different.
                        Thread "main" is displayed as "Thread-13" and "Thread-0" is displayed as "Thread-14"
                     */
                    Log.logFieldAcc(o, name, isStatic, isWrite);
                }
            }
        }
    }
}