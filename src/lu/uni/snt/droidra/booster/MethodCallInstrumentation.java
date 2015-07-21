package lu.uni.snt.droidra.booster;

import java.util.ArrayList;
import java.util.List;

import lu.uni.snt.droidra.ClassDescription;
import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtType;
import lu.uni.snt.droidra.model.StmtValue;
import lu.uni.snt.droidra.model.UniqStmt;
import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;

public class MethodCallInstrumentation extends DefaultInstrumentation
{
	public MethodCallInstrumentation(StmtKey stmtKey, StmtValue stmtValue, UniqStmt uniqStmt) {
		super(stmtKey, stmtValue, uniqStmt);
	}

	@Override
	public void instrument() 
	{
		SootMethod sootMethod = stmtKey.getMethod();
		Stmt stmt = stmtKey.getStmt();
		
		Body body = sootMethod.retrieveActiveBody();
		Stmt nextStmt = getNextStmt(body, stmt);
		LocalGenerator localGenerator = new LocalGenerator(body);
		
		if (StmtType.METHOD_CALL != stmtValue.getType())
		{
			return;
		}
		
		List<Unit> injectedUnits = new ArrayList<Unit>();
		
		for (ClassDescription clsDesc : stmtValue.getClsSet())
		{
			if (Scene.v().containsClass(clsDesc.cls))
			{
				boolean isStaticMethod = InstrumentationUtils.isStaticReflectiveInvocation(stmt);
				SootClass sc = Scene.v().getSootClass(clsDesc.cls);
				SootMethod calleeMethod = getSootMethod(sc, sootMethod, clsDesc.name, stmt);
				
				List<Value> args = new ArrayList<Value>();
				
				if (null == calleeMethod)
				{
					String methodSubSignature = "java.lang.Object " + clsDesc.name + "(java.lang.Object[])";
					calleeMethod = Mocker.mockSootMethod(clsDesc.cls, methodSubSignature, isStaticMethod);
					
					Local local = localGenerator.generateLocal(ArrayType.v(RefType.v("java.lang.Object"), 1));
					Unit assignU = Jimple.v().newAssignStmt(local, stmt.getInvokeExpr().getArg(1));
					injectedUnits.add(assignU);
					
					args.add(local);
				}
				else
				{
					List<Type> paramTypes = calleeMethod.getParameterTypes();

					Value arg1 = stmt.getInvokeExpr().getArg(1);
					
					if (null == arg1 || "null".equals(arg1.toString()))
					{
						args.add(NullConstant.v());
					}
					else
					{
						for (int i = 0; i < paramTypes.size(); i++)
						{
							Type type = paramTypes.get(i);
							
							System.out.println("DEBUG: " + stmt);
							System.out.println("DEBUG: " + arg1);
							
							Local local = localGenerator.generateLocal(RefType.v("java.lang.Object"));
							Unit assignU = Jimple.v().newAssignStmt(local, Jimple.v().newArrayRef(arg1, IntConstant.v(i)));
							injectedUnits.add(assignU);
							
							type = InstrumentationUtils.toPrimitiveWrapperType(type);
							
							Local local2 = localGenerator.generateLocal(type);
							Unit assignU2 = Jimple.v().newAssignStmt(local2, Jimple.v().newCastExpr(local, type));
							injectedUnits.add(assignU2);
							
							args.add(local2);
						}
					}
				}
				
				InvokeExpr invokeExpr = null;
				if (isStaticMethod || calleeMethod.isStatic())
				{
					invokeExpr = Jimple.v().newStaticInvokeExpr(calleeMethod.makeRef(), args);
				}
				else
				{
					Value arg0 = stmt.getInvokeExpr().getArg(0);
					Local arg0Local = localGenerator.generateLocal(arg0.getType());
					
					Unit assignU = Jimple.v().newAssignStmt(arg0Local, arg0);
					//To avoid the problem that the return varilale is as same as a parameter value (symbol)
					body.getUnits().insertBefore(assignU, stmt);
					//injectedUnits.add(assignU);
					
					if (isInterfaceType(arg0Local))
					{
						invokeExpr = Jimple.v().newInterfaceInvokeExpr(arg0Local, calleeMethod.makeRef(), args);
					}
					else
					{
						System.out.println(calleeMethod.isStatic());
						invokeExpr = Jimple.v().newVirtualInvokeExpr(arg0Local, calleeMethod.makeRef(), args);
					}
				}
				
				if (stmt instanceof AssignStmt)
				{
					Value leftOp = ((AssignStmt) stmt).getLeftOp();
					
					Unit invokeU = Jimple.v().newAssignStmt(leftOp, invokeExpr);
					injectedUnits.add(invokeU);
				}
				else
				{
					Unit invokeU = Jimple.v().newInvokeStmt(invokeExpr);
					injectedUnits.add(invokeU);
				}
			}
		}
		
		for (int i = injectedUnits.size()-1; i >= 0; i--)
		{
			body.getUnits().insertAfter(injectedUnits.get(i), stmt);
		}
		
		injectedStmtWrapper(body, localGenerator, stmt, nextStmt);
		
		System.out.println(body);
		body.validate();
		
	}
	
	public boolean isInterfaceType(Value value)
	{
		try
		{
			String type = value.getType().toString();
			SootClass sc = Scene.v().getSootClass(type);
			
			if (sc.isInterface())
			{
				return true;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return false;
	}
	
	/*
	 * 1) If there is only one method, then use its information to guide args construction. 
	 * 2) If not, use the array parameter to infer the method.
	 * 		through the number of parameters
	 * 		If there are several methods that have same number parameters, randomly select one
	 * 
	 * @param sc
	 * @param sm
	 * @param methodName
	 * @param callerStmt
	 * @return 
	 * 	null, the SootClass doesn't contain method sm
	 */
	/*
	public SootMethod getSootMethod(SootClass callerClass, SootMethod callerMethod, String methodName, Stmt callerStmt)
	{
		SootMethod targetMethod = null;
		
		List<SootMethod> sootMethods = callerClass.getMethods();
		
		List<SootMethod> targetMethodList = new ArrayList<SootMethod>();
		
		for (SootMethod sootMethod : sootMethods)
		{
			if (sootMethod.getName().equals(methodName))
			{
				targetMethodList.add(sootMethod);
			}
		}
		
		if (1 == targetMethodList.size())
		{
			targetMethod = targetMethodList.get(0);
		}
		else if (1 < targetMethodList.size())
		{
			ArrayVar arrayVar = getNearestArrayVar(callerMethod, callerStmt);
			
			if (null != arrayVar)
			{
				for (SootMethod sootMethod : targetMethodList)
				{
					if (sootMethod.getParameterCount() == arrayVar.length)
					{
						targetMethod = sootMethod;
						break;
					}
				}
				
				//At least the targetMethod exists
				if (null == targetMethod)
				{
					targetMethod = targetMethodList.get(0);
				}
			}
		}
		
		return targetMethod;
	}
	
	private ArrayVar getNearestArrayVar(SootMethod callerMethod, Stmt callerStmt)
	{
		ArrayVar arrayVar = null;
		
		Value arg1 = callerStmt.getInvokeExpr().getArg(1);
		
		List<ArrayVar> arrayVars = DroidRAUtils.method2arrayVars.get(callerMethod.getSignature());
		
		for (ArrayVar av : arrayVars)
		{
			if (av.stmt.toString().equals(callerStmt.toString()))
			{
				break;
			}
			else
			{
				if (arrayVar.varStr.equals(arg1.toString()))
				{
					arrayVar = av;
				}
			}
		}
		
		return arrayVar;
	}*/
}