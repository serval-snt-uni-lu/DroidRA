package lu.uni.snt.droidra.booster;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;

public class Mocker {

	public static SootClass mockSootClass(String clsName)
	{
		SootClass sc = null;
		
		if (Scene.v().containsClass(clsName))
		{
			sc = Scene.v().getSootClass(clsName);
			
			if (sc.isPhantom())
			{
				//sc.setPhantom(false);
				sc.setApplicationClass();
				sc.setInScene(true);
				
				try {
					for (Field field : sc.getClass().getFields())
					{
						if (field.getName().equals("isPhantom"))
						{
							field.setAccessible(true);
							field.setBoolean(sc, false);
						}
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			sc = new SootClass(clsName);
			sc.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
			
			sc.setPhantom(false);
			sc.setApplicationClass();
			sc.setInScene(true);
		}
		
		mockConstructor(sc);

		return sc;
	}
	
	public static SootField mockSootField(String clsName, String fieldName, boolean isStatic)
	{
		SootClass sc = mockSootClass(clsName);
		
		SootField sf = null;
		
		try
		{
			sf = sc.getFieldByName(fieldName);
		}
		catch (Exception ex)
		{
			sf = null;
		}
		
		if (null == sf)
		{
			int m = Modifier.PUBLIC;
			if (isStatic)
			{
				m = m | Modifier.STATIC;
			}
			
			sf = new SootField(fieldName, InstrumentationUtils.toSootTypeByName(fieldName), m);
			sc.addField(sf);
		}
		
		
		return sf;
	}
	
	public static SootMethod mockSootMethod(String clsName, String methodSubSignature, boolean isStatic)
	{
		SootClass sc = mockSootClass(clsName);
		
		SootMethod sm = null;

		try
		{
			sm = sc.getMethod(methodSubSignature);
		}
		catch (Exception ex)
		{
			sm = null;
		}

		if (null == sm)
		{
			int m = Modifier.PUBLIC;
			if (isStatic)
			{
				m = m | Modifier.STATIC;
			}
			
			List<Type> paramTypes = new ArrayList<Type>();
			paramTypes.add(ArrayType.v(RefType.v("java.lang.Object"), 1));
			
			String[] strs = methodSubSignature.split(" ");
			String methodName = strs[1].trim().substring(0, strs[1].trim().indexOf("("));
			
			if (null == methodName || methodName.isEmpty())
			{
				return null;
			}
			
			sm = new SootMethod(methodName, paramTypes, RefType.v("java.lang.Object"), m);
			sc.addMethod(sm);
			
			//Add body of sm
			JimpleBody b = Jimple.v().newBody(sm);
	        sm.setActiveBody(b);
	        //LocalGenerator lg = new LocalGenerator(b);
			{
				b.insertIdentityStmts();
				
				
				
				//Local rtLoc = lg.generateLocal(RefType.v("java.lang.Object"));
				
				//Local param0 = lg.generateLocal(ArrayType.v(RefType.v("java.lang.Object"), 1));
				//Unit param0U = Jimple.v().newIdentityStmt(rtLoc, Jimple.v().newParameterRef(ArrayType.v(RefType.v("java.lang.Object"), 1), 0));
				
				
				//Unit rtLocAssignU = Jimple.v().newAssignStmt(rtLoc, param0);
				
				Unit returnU = Jimple.v().newReturnStmt(b.getParameterLocal(0));
				
				//b.getUnits().add(param0U);
				b.getUnits().add(returnU);
			}
			
			System.out.println("validation:" + b);
			b.validate();
		}	
		
		return sm;
	}
	
	public static void mockConstructor(SootClass sc)
	{
		SootMethod sm = null;
		
		//Without parameters
		String methodSubSignature = "void <init>()";
		
		try
		{
			sm = sc.getMethod(methodSubSignature);
		}
		catch (Exception ex)
		{
			sm = null;
		}
		
		int m = Modifier.PUBLIC;
		
		if (null == sm)
		{
			sm = new SootMethod("<init>", new ArrayList<Type>(), VoidType.v(), m);
			sc.addMethod(sm);
			
			//Add body
			JimpleBody b = Jimple.v().newBody(sm);
	        sm.setActiveBody(b);
			{
				b.insertIdentityStmts();
				b.getUnits().add(Jimple.v().newReturnVoidStmt());
			}
			
			System.out.println("validation:" + b);
			b.validate();
		}
		
		//Static init
		/*
		methodSubSignature = "void <clinit>()";
		
		try
		{
			sm = sc.getMethod(methodSubSignature);
		}
		catch (Exception ex)
		{
			sm = null;
		}
		
		if (null == sm)
		{
			sm = new SootMethod("<clinit>", new ArrayList<Type>(), VoidType.v(), m | Modifier.STATIC);
			sc.addMethod(sm);
			
			//Add body
			JimpleBody b = Jimple.v().newBody(sm);
	        sm.setActiveBody(b);
			{
				b.getUnits().add(Jimple.v().newReturnVoidStmt());
			}
			
			b.validate();
			System.out.println("validation:" + b);
		}
		*/
		
		//With parameter
		methodSubSignature = "void <init>(java.lang.Object[])";
		
		try
		{
			sm = sc.getMethod(methodSubSignature);
		}
		catch (Exception ex)
		{
			sm = null;
		}
		
		
		if (null == sm)
		{
			List<Type> paramTypes = new ArrayList<Type>();
			paramTypes.add(ArrayType.v(RefType.v("java.lang.Object"), 1));
			
			sm = new SootMethod("<init>", paramTypes, VoidType.v(), m);
			sc.addMethod(sm);
			
			//Add body
			JimpleBody b = Jimple.v().newBody(sm);
	        sm.setActiveBody(b);
	        //LocalGenerator lg = new LocalGenerator(b);
			{
				b.insertIdentityStmts();
				
				//Local rtLoc = lg.generateLocal(RefType.v("java.lang.Object"));
				//Local param0Loc = lg.generateLocal(ArrayType.v(RefType.v("java.lang.Object"), 1));
				//Unit param0U = Jimple.v().newIdentityStmt(rtLoc, Jimple.v().newParameterRef(ArrayType.v(RefType.v("java.lang.Object"), 1), 0));
				//Unit assignU = Jimple.v().newAssignStmt(rtLoc, Jimple.v().newCastExpr(param0Loc, RefType.v("java.lang.Object")));
				//b.getUnits().add(param0U);
				//b.getUnits().add(assignU);
				b.getUnits().add(Jimple.v().newReturnVoidStmt());
			}
			
			System.out.println("validation:" + b);
			b.validate();
		}
	}
}
