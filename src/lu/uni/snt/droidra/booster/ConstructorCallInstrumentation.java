package lu.uni.snt.droidra.booster;

import java.util.ArrayList;
import java.util.List;

import lu.uni.snt.droidra.ClassDescription;
import lu.uni.snt.droidra.model.SimpleStmtValue;
import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtType;
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
import soot.jimple.Stmt;

public class ConstructorCallInstrumentation extends DefaultInstrumentation
{

	public ConstructorCallInstrumentation(StmtKey stmtKey, SimpleStmtValue stmtValue,
			UniqStmt uniqStmt) {
		super(stmtKey, stmtValue, uniqStmt);
		// TODO Auto-generated constructor stub
	}

	/**
	 * First <cinit> and then <init>
	 * The constructor cannot be static
	 */
	@Override
	public void instrument() 
	{
		SootMethod sootMethod = stmtKey.getMethod();
		Stmt stmt = stmtKey.getStmt();
		
		if (! (stmt instanceof AssignStmt) || ! stmt.containsInvokeExpr())
		{
			return;
		}
		
		Body body = sootMethod.retrieveActiveBody();
		Stmt nextStmt = getNextStmt(body, stmt);
		
		LocalGenerator localGenerator = new LocalGenerator(body);
		
		if (StmtType.CONSTRUCTOR_CALL != stmtValue.getType())
		{
			return;
		}
		
		List<Unit> injectedUnits = new ArrayList<Unit>();
		
		//for (ClassDescription clsDesc : stmtValue.getClsSet())
		//{
		ClassDescription clsDesc = stmtValue.getClsDesc();
			SootClass sc = Scene.v().getSootClass(clsDesc.cls);
			

			Local local = localGenerator.generateLocal(RefType.v(clsDesc.cls));
			
			Unit newU = Jimple.v().newAssignStmt(local, Jimple.v().newNewExpr(sc.getType()));
			injectedUnits.add(newU);
			
			List<SootMethod> cinitList = InstrumentationUtils.getMethodByName(sc, "<clinit>");
			if (cinitList.size() > 0)
			{
				SootMethod cinit = cinitList.get(0);

				Unit cinitCallU = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(cinit.makeRef()));
				injectedUnits.add(cinitCallU);
			}
			
			
			SootMethod init = getSootMethod(sc, sootMethod, "<init>", stmt);
			
			List<Value> args = new ArrayList<Value>();
			
			if (null == init)
			{
				String methodSubSignature = "void <init>(java.lang.Object[])";
				init = Mocker.mockSootMethod(clsDesc.cls, methodSubSignature, false);
				
				System.out.println(stmt);
				
				Local param0Loc = localGenerator.generateLocal(ArrayType.v(RefType.v("java.lang.Object"), 1));
				//whye 1? Unit assignU = Jimple.v().newAssignStmt(param0Loc, stmt.getInvokeExpr().getArg(0));
				Unit assignU = Jimple.v().newAssignStmt(param0Loc, stmt.getInvokeExpr().getArg(0));
				injectedUnits.add(assignU);
				
				args.add(local);
			}
			else
			{
				List<Type> paramTypes = init.getParameterTypes();
				
				Value arg0 = stmt.getInvokeExpr().getArg(0);
				for (int i = 0; i < paramTypes.size(); i++)
				{
					Type type = paramTypes.get(i);
					
					Local local1 = localGenerator.generateLocal(RefType.v("java.lang.Object"));
					Unit assignU = Jimple.v().newAssignStmt(local1, Jimple.v().newArrayRef(arg0, IntConstant.v(i)));
					injectedUnits.add(assignU);
					
					Local local2 = localGenerator.generateLocal(type);
					Unit assignU2 = Jimple.v().newAssignStmt(local2, Jimple.v().newCastExpr(local1, type));
					injectedUnits.add(assignU2);
					
					args.add(local2);
				}
			}
			
			InvokeExpr invokeExpr = Jimple.v().newVirtualInvokeExpr(local, init.makeRef(), args);
			Unit initU = Jimple.v().newInvokeStmt(invokeExpr);
			injectedUnits.add(initU);
			
			AssignStmt assignStmt = (AssignStmt) stmt;
			Unit invokeU = Jimple.v().newAssignStmt(assignStmt.getLeftOp(), local);
			injectedUnits.add(invokeU);
		//}
		
		for (int i = injectedUnits.size()-1; i >= 0; i--)
		{
			body.getUnits().insertAfter(injectedUnits.get(i), stmt);
		}
		
		injectedStmtWrapper(body, localGenerator, stmt, nextStmt);
		
		System.out.println(body);
		body.validate();
	}
}