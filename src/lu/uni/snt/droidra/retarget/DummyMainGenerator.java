package lu.uni.snt.droidra.retarget;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.droidra.booster.InstrumentationUtils;
import soot.ArrayType;
import soot.Body;
import soot.IntType;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.iccta.ICCDummyMainCreator;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.Chain;


public class DummyMainGenerator extends SceneTransformer{

	private String apkFileLocation = null;
	private boolean fullMethodCover = false;
	
	public static final String DUMMY_CLASS_NAME = "DummyMainClass";
	public static final String DUMMY_METHOD_NAME = "main";
	
	public static Set<String> addtionalDexFiles = new HashSet<String>();
	
	public DummyMainGenerator(String apkFileLocation)
	{
		this.apkFileLocation = apkFileLocation;
	}
	
	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) 
	{
		try
		{	
			ProcessManifest processMan = new ProcessManifest(apkFileLocation);
			Set<String> entrypoints = processMan.getEntryPointClasses();
			
			SootMethod mainMethod = generateMain(entrypoints);
			
			System.out.println(mainMethod.retrieveActiveBody());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static Set<String> getComponents(String apkFileLocation)
	{
		Set<String> entrypoints = null;
		try
		{	
			ProcessManifest processMan = new ProcessManifest(apkFileLocation);
			entrypoints = processMan.getEntryPointClasses();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return entrypoints;
	}
	
	public static SootMethod generateDummyMain(String apkFileLocation)
	{
		SootMethod mainMethod = null;
		
		try
		{	
			ProcessManifest processMan = new ProcessManifest(apkFileLocation);
			Set<String> entrypoints = processMan.getEntryPointClasses();
			
			DummyMainGenerator dmGenerator = new DummyMainGenerator(apkFileLocation);
			
			mainMethod = dmGenerator.generateMain(entrypoints);
			
			System.out.println(mainMethod.retrieveActiveBody());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return mainMethod;
	}
	
	public SootMethod generateMain(Set<String> components)
	{
		SootMethod mainMethod = new SootMethod(DUMMY_METHOD_NAME, 
				Arrays.asList(new Type[] {ArrayType.v(RefType.v("java.lang.String"), 1)}), 
    			VoidType.v(), 
    			Modifier.PUBLIC | Modifier.STATIC);
    	JimpleBody body = Jimple.v().newBody(mainMethod);
    	mainMethod.setActiveBody(body);
    	
    	SootClass sootClass = new SootClass(DUMMY_CLASS_NAME);
    	sootClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
    	sootClass.setPhantom(false);
    	sootClass.setApplicationClass();
    	sootClass.setInScene(true);
    	
    	sootClass.addMethod(mainMethod);
		
    	LocalGenerator generator = new LocalGenerator(body);
		
		for (String str : components)
		{
			SootClass sc = Scene.v().getSootClass(str);
			if (sc.isPhantom())
			{
				continue;
			}
			
			SootMethod method = ICCDummyMainCreator.v().generateDummyMainMethod(str);
			instrumentDummyMainMethod(method);
			
			SootClass cls = method.getDeclaringClass();
			SootMethod sootMethod = cls.getMethod("<init>", new ArrayList<Type>());
			
			if (null == sootMethod)
			{
				throw new RuntimeException("No default constructor for comp " + cls.getName());
			}
			
			Local al = generator.generateLocal(cls.getType());
			Unit newU = (Unit) Jimple.v().newAssignStmt(al, Jimple.v().newNewExpr(cls.getType()));
			
			Unit initU = (Unit) Jimple.v().newInvokeStmt(
					Jimple.v().newSpecialInvokeExpr(al, sootMethod.makeRef()));
			
			Unit callU = (Unit) Jimple.v().newInvokeStmt(
					Jimple.v().newSpecialInvokeExpr(al, method.makeRef()));
			
			body.getUnits().add(newU);
			body.getUnits().add(initU);
			body.getUnits().add(callU);
		}
		
		body.getUnits().add(Jimple.v().newReturnVoidStmt());
		
		if (fullMethodCover)
		{
			mainMethod = appendNonComponents(mainMethod);
		}
		
		body.validate();
		
		return mainMethod;
	}
	
	
	public SootMethod appendNonComponents(SootMethod mainMethod)
	{
		Set<String> coveredMethods = new HashSet<String>();
		
		CallGraph cg = Scene.v().getCallGraph();
		
		for (Iterator<Edge> iter = cg.iterator(); iter.hasNext(); )
		{
			Edge edge = iter.next();
			
			coveredMethods.add(edge.src().getSignature());
			coveredMethods.add(edge.tgt().getSignature());
		}
		
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		
		for (Iterator<SootClass> iter = sootClasses.iterator(); iter.hasNext();)
		{
			SootClass sc = iter.next();
			
			List<SootMethod> methodList = sc.getMethods();
			for (SootMethod sm : methodList)
			{
				if (sm.getDeclaringClass().getName().startsWith("android.support"))
				{
					continue;
				}
				
				if (sc.isPhantom() || ! sm.isConcrete())
				{
					continue;
				}
				
				if (sm.getName().equals("<init>") || sm.getName().equals("<clinit>"))
				{
					continue;
				}

				
				
				if (coveredMethods.contains(sm.getSignature()))
				{
					//Already covered.
					continue;
				}
				
				mainMethod = addMethod(mainMethod, sm.getSignature());
			}
		}
		
		return mainMethod;
	}
	
	public SootMethod addMethod(SootMethod mainMethod, String methodSignature)
	{
		Body body = mainMethod.getActiveBody();
    	
		Stmt returnStmt = null;
		
    	PatchingChain<Unit> units = body.getUnits();
    	for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext(); )
    	{
    		Stmt stmt = (Stmt) iter.next();
    		
    		if (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt)
    		{
    			returnStmt = stmt;
    		}
    	}
    	
    	SootMethod sm = Scene.v().getMethod(methodSignature);
    	
    	List<Type> paramTypes = sm.getParameterTypes();
    	List<Value> paramValues = new ArrayList<Value>();
    	for (int i = 0; i < paramTypes.size(); i++)
    	{
    		paramValues.add(InstrumentationUtils.toDefaultSootTypeValue(paramTypes.get(i)));
    	}
    	
    	
    	if (sm.isStatic())    //No need to construct its obj ref
    	{
    		InvokeExpr expr = Jimple.v().newStaticInvokeExpr(sm.makeRef(), paramValues);
    		Unit callU = Jimple.v().newInvokeStmt(expr);
    		units.insertBefore(callU, returnStmt);
    	}
    	else
    	{
    		//new obj first and then call the method
    		
    		SootClass sc = sm.getDeclaringClass();
    		List<SootMethod> methods = sc.getMethods();
    		
    		SootMethod init = null;
    		SootMethod clinit = null;
    		
    		for (SootMethod method : methods)
    		{
    			if (method.getName().equals("<clinit>"))
    			{
    				clinit = method;
    			}
    			
    			if (method.getName().equals("<init>"))
    			{
    				init = method;
    			}
    		}
    		
    		LocalGenerator localGenerator = new LocalGenerator(body);
    		
    		Local obj = localGenerator.generateLocal(sc.getType());
    		
    		Unit newU = Jimple.v().newAssignStmt(obj, Jimple.v().newNewExpr(sc.getType())); 
    		units.insertBefore(newU, returnStmt);
    		
    		if (null != clinit)
    		{
    			Unit clinitCallU = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(clinit.makeRef()));
    			units.insertBefore(clinitCallU, returnStmt);
    		}
    		
    		if (null != init)
    		{
    			List<Type> initParamTypes = init.getParameterTypes();
    	    	List<Value> initParamValues = new ArrayList<Value>();
    	    	for (int i = 0; i < initParamTypes.size(); i++)
    	    	{
    	    		initParamValues.add(InstrumentationUtils.toDefaultSootTypeValue(initParamTypes.get(i)));
    	    	}
    	    	
    	    	Unit initCallU = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(obj, init.makeRef(), initParamValues));
    	    	units.insertBefore(initCallU, returnStmt);
    		}
    		else
    		{
    			throw new RuntimeException("Is it possible that a class does not contain an <init> method?");
    		}
    	}
    	
    	System.out.println(body);
    	body.validate();
    	
    	return mainMethod;
	}
	
	
	public void instrumentDummyMainMethod(SootMethod mainMethod)
	{
		Body body = mainMethod.getActiveBody();
    	
    	PatchingChain<Unit> units = body.getUnits();
    	for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext(); )
    	{
    		Stmt stmt = (Stmt) iter.next();
    		
    		if (stmt instanceof IdentityStmt)
    		{
    			continue;
    		}
    		   	
    		//For the purpose of confusion dex optimization (because of the strategy of generating dummyMain method)
			AssignStmt aStmt = (AssignStmt) stmt;
			SootMethod fuzzyMe = generateFuzzyMethod(mainMethod.getDeclaringClass());
			InvokeExpr invokeExpr = Jimple.v().newVirtualInvokeExpr(body.getThisLocal(), fuzzyMe.makeRef());
			Unit assignU = Jimple.v().newAssignStmt(aStmt.getLeftOp(), invokeExpr);
			units.insertAfter(assignU, aStmt);
			
			break;
    	}
	}
    
	public SootMethod generateFuzzyMethod(SootClass sootClass)
	{
    	String name = "fuzzyMe";
	    List<Type> parameters = new ArrayList<Type>();
	    Type returnType = IntType.v();
	    int modifiers = Modifier.PUBLIC;
	    SootMethod fuzzyMeMethod = new SootMethod(name, parameters, returnType, modifiers);
	    sootClass.addMethod(fuzzyMeMethod);
	    
	    {
	    	Body b = Jimple.v().newBody(fuzzyMeMethod);
	    	fuzzyMeMethod.setActiveBody(b);
	    	LocalGenerator lg = new LocalGenerator(b);
	        Local thisLocal = lg.generateLocal(sootClass.getType());
	        Unit thisU = Jimple.v().newIdentityStmt(thisLocal, 
	                Jimple.v().newThisRef(sootClass.getType()));
	        Unit returnU = Jimple.v().newReturnStmt(IntConstant.v(1));
	        b.getUnits().add(thisU);
	        b.getUnits().add(returnU);
	    }
	        
	    return fuzzyMeMethod;
	}

	public void setFullMethodCover(boolean fullMethodCover) {
		this.fullMethodCover = fullMethodCover;
	}	
}
