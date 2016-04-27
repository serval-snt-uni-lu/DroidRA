package lu.uni.snt.droidra.booster;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.IntType;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;

public class Alteration 
{
	public static final String CLASS_ALTERATION = "Alteration";
	public static final String METHOD_TRY = "check";
	
	private static Alteration alteration = new Alteration();
	
	public static Alteration v()
	{
		return alteration;
	}
	
	public void init()
	{
		if (Scene.v().containsClass(CLASS_ALTERATION))
		{
			return;
		}
		
		SootClass sootClass = new SootClass(CLASS_ALTERATION);
    	sootClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
    	sootClass.setPhantom(false);
    	sootClass.setApplicationClass();
    	sootClass.setInScene(true);
    	
    	//sootClass.addMethod(mainMethod);
		
	    List<Type> parameters = new ArrayList<Type>();
	    parameters.add(IntType.v());
	    Type returnType = IntType.v();
	    int modifiers = Modifier.PUBLIC | Modifier.STATIC;

	    SootMethod tryMethod = new SootMethod(METHOD_TRY, parameters, returnType, modifiers);
	    sootClass.addMethod(tryMethod);
	    
	    {
	    	Body b = Jimple.v().newBody(tryMethod);
	    	tryMethod.setActiveBody(b);
	    	
	    	LocalGenerator lg = new LocalGenerator(b);
	    	
	    	Local paramLocal = lg.generateLocal(IntType.v());
	        Unit paramLocalU = Jimple.v().newIdentityStmt(
	        		paramLocal, 
	        		Jimple.v().newParameterRef(IntType.v(), 0));
	    	
	        Local returnLocal = lg.generateLocal(IntType.v());
	        
	        Unit callCheckU = Jimple.v().newAssignStmt(returnLocal, Jimple.v().newStaticInvokeExpr(tryMethod.makeRef(), IntConstant.v(1)));
	        Unit assignU = Jimple.v().newAssignStmt(returnLocal, IntConstant.v(0));
	        Unit returnU = Jimple.v().newReturnStmt(returnLocal);
	        
	        Unit ifU = Jimple.v().newIfStmt(Jimple.v().newEqExpr(IntConstant.v(0), paramLocal), callCheckU);
	        Unit jimpleU = Jimple.v().newGotoStmt(returnU);
	        
	        b.getUnits().add(paramLocalU);
	        b.getUnits().add(ifU);
	        b.getUnits().add(assignU);
	        b.getUnits().add(jimpleU);
	        b.getUnits().add(callCheckU);
	        b.getUnits().add(returnU);
	        
	        System.out.println(b);
	        b.validate();
	    }
	}
	
	public SootMethod getTryMethod()
	{
		return Scene.v().getSootClass(CLASS_ALTERATION).getMethodByName(METHOD_TRY);
	}
}
