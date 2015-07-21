package lu.uni.snt.droidra.booster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lu.uni.snt.droidra.GlobalRef;
import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtValue;
import lu.uni.snt.droidra.model.UniqStmt;
import lu.uni.snt.droidra.typeref.ArrayVarValue;
import soot.Body;
import soot.IntType;
import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;

public abstract class DefaultInstrumentation implements IInstrumentation
{
	StmtKey stmtKey = null;
	StmtValue stmtValue = null;
	UniqStmt uniqStmt = null;
	
	public DefaultInstrumentation(StmtKey stmtKey, StmtValue stmtValue, UniqStmt uniqStmt)
	{
		this.stmtKey = stmtKey;
		this.stmtValue = stmtValue;
		this.uniqStmt = uniqStmt;
		
		Alteration.v().init();
	}
	
	/*
	public DefaultInstrumentation(UniqStmt uniqStmt, StmtValue stmtValue)
	{
		StmtKey skey = new StmtKey();
		SootMethod sm = Scene.v().getMethod(uniqStmt.methodSignature);
		skey.setMethod(sm);
		
		Body body = sm.retrieveActiveBody();
		int count = 0;
		for (Iterator<Unit> iter = body.getUnits().snapshotIterator(); iter.hasNext(); )
		{
			Stmt stmt = (Stmt) iter.next();
			count++;
			
			if (count == uniqStmt.stmtSeq)
			{
				skey.setStmt(stmt);
				break;
			}
		}
		
		this.uniqStmt = uniqStmt;
		this.stmtKey = skey;
		this.stmtValue = stmtValue;
	}*/
	
	/**
	 * CallerStmt example: invoke(obj, array[]).
	 * 
	 * @param callerClass
	 * @param callerMethod
	 * @param methodName
	 * @param callerStmt
	 * @return
	 */
	public SootMethod getSootMethod(SootClass callerClass, SootMethod callerMethod, String methodName, Stmt callerStmt)
	{
		SootMethod targetMethod = null;
		
		List<SootMethod> targetMethodList = InstrumentationUtils.getMethodByName(callerClass, methodName);
		
		if (1 == targetMethodList.size())
		{
			targetMethod = targetMethodList.get(0);
		}
		else if (1 < targetMethodList.size())
		{
			//Get all its possible arrays
			ArrayVarValue[] arrayVarValues = GlobalRef.arrayTypeRef.get(uniqStmt);
			
			if (null == arrayVarValues)
			{
				//TODO: Come back later...
				targetMethod = null;
			}
			//In our example, we know that for each statement, there is only one array
			else if (1 != arrayVarValues.length)
			{
				//Mock
				targetMethod = null;
				System.out.println("Strange: More than one array parameters in " + callerStmt);
			}
			else
			{
				ArrayVarValue avValue = arrayVarValues[0];
				
				if (null != avValue)
				{
					//First check the length of parameters
					List<SootMethod> methodsWithSameParamLength = new ArrayList<SootMethod>();
					for (SootMethod sootMethod : targetMethodList)
					{
						if (sootMethod.getParameterCount() == avValue.length)
						{
							methodsWithSameParamLength.add(sootMethod);
						}
					}
					
					if (1 == methodsWithSameParamLength.size())
					{
						targetMethod = methodsWithSameParamLength.get(0); 
					}
					if (1 < methodsWithSameParamLength.size())
					{
						//more than one methods, perform more fine-grained analysis
						int matchScore = 0;
						SootMethod matchMethod = null;
						
						for (SootMethod sm : methodsWithSameParamLength)
						{
							List<Type> types = sm.getParameterTypes();
							for (int i = 0; i < types.size(); i++)
							{
								if (! types.get(i).toString().equals(avValue.types[i]))
								{
									if (matchScore <= i)
									{
										matchScore = i;
										matchMethod = sm;
										break;
									}
									
								}
							}
						}
						
						targetMethod = matchMethod;
					}
					else
					{
						//Mock
						targetMethod = null;
					}
				}
			}
		}
		else
		{
			//There is no same named method found.
			//It's safe to return null here, later we based on this null to generate a mock method.
			targetMethod = null;
		}
		
		return targetMethod;
	}
	
	
	public Stmt getNextStmt(Body body, Stmt stmt)
	{
		PatchingChain<Unit> units = body.getUnits();
		
		for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
		{
			Stmt s = (Stmt) iterU.next();
			
			if (s.equals(stmt))
			{
				if (iterU.hasNext())
					return (Stmt) iterU.next();
			}
			
		}
		
		return null;
	}
	
	public void injectedStmtWrapper(Body body, LocalGenerator localGenerator, Stmt stmt, Stmt nextStmt)
	{
		Local opaqueLocal = localGenerator.generateLocal(IntType.v());
		Unit assignU = Jimple.v().newAssignStmt(opaqueLocal, Jimple.v().newStaticInvokeExpr(Alteration.v().getTryMethod().makeRef()));
		Unit ifU = Jimple.v().newIfStmt(Jimple.v().newEqExpr(IntConstant.v(1), opaqueLocal), nextStmt);

		body.getUnits().insertAfter(ifU, stmt);
		body.getUnits().insertAfter(assignU, stmt);
	}
}
