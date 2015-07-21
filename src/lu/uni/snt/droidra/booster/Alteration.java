package lu.uni.snt.droidra.booster;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.IntType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
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
	    Type returnType = IntType.v();
	    int modifiers = Modifier.PUBLIC | Modifier.STATIC;

	    SootMethod tryMethod = new SootMethod(METHOD_TRY, parameters, returnType, modifiers);
	    sootClass.addMethod(tryMethod);
	    
	    {
	    	Body b = Jimple.v().newBody(tryMethod);
	    	tryMethod.setActiveBody(b);
	    	
	        Unit returnU = Jimple.v().newReturnStmt(IntConstant.v(0));
	        b.getUnits().add(returnU);
	    }
	}
	
	public SootMethod getTryMethod()
	{
		return Scene.v().getSootClass(CLASS_ALTERATION).getMethodByName(METHOD_TRY);
	}
}
