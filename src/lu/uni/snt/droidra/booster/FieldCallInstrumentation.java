package lu.uni.snt.droidra.booster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lu.uni.snt.droidra.ClassDescription;
import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtType;
import lu.uni.snt.droidra.model.StmtValue;
import lu.uni.snt.droidra.model.UniqStmt;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;

public class FieldCallInstrumentation extends DefaultInstrumentation
{
	private Set<String> fieldCallSet = null;
	private String fieldCallsConfigPath = "res/FieldCalls.txt";
	
	public FieldCallInstrumentation(StmtKey stmtKey, StmtValue stmtValue,
			UniqStmt uniqStmt) {
		super(stmtKey, stmtValue, uniqStmt);
		
		fieldCallSet = new HashSet<String>();
		init();
	}

	private void init()
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(fieldCallsConfigPath));
			String line = br.readLine();
			while ((line = br.readLine()) != null)
			{
				fieldCallSet.add(line);
			}
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	public void instrument() 
	{
		SootMethod sootMethod = stmtKey.getMethod();
		Stmt stmt = stmtKey.getStmt();
		
		Body body = sootMethod.retrieveActiveBody();
		Stmt nextStmt = getNextStmt(body, stmt);
		LocalGenerator localGenerator = new LocalGenerator(body);
		
		if (StmtType.FIELD_CALL != stmtValue.getType())
		{
			return;
		}
		
		//To store the units that will be injected later
		List<Unit> injectedUnits = new ArrayList<Unit>();
		
		for (ClassDescription clsDesc : stmtValue.getClsSet())
		{
			if (Scene.v().containsClass(clsDesc.cls))
			{
				SootClass sc = Scene.v().getSootClass(clsDesc.cls);
				SootField sf = getSootField(sc, clsDesc.name);
				
				boolean isStaticField = InstrumentationUtils.isStaticReflectiveInvocation(stmt);
				
				if (null == sf)
				{
					
					sf = Mocker.mockSootField(clsDesc.cls, clsDesc.name, isStaticField);
				}
				
				isStaticField = isStaticField || sf.isStatic();
				
				SootMethod calleeMethod = stmt.getInvokeExpr().getMethod();
				
				if (calleeMethod.getName().startsWith("get"))
				{
					if (isStaticField)
					{
						if (stmt instanceof AssignStmt)
						{
							AssignStmt assignStmt = (AssignStmt) stmt;
							Value leftOp = assignStmt.getLeftOp();
							
							Unit callU = Jimple.v().newAssignStmt(leftOp, Jimple.v().newStaticFieldRef(sf.makeRef()));
							injectedUnits.add(callU);
						}
						
						//It's not make sense that just get a value from an object without signing it to other variables.
					}
					else
					{
						Value arg0 = stmt.getInvokeExpr().getArg(0);
						Local arg0Local = localGenerator.generateLocal(arg0.getType());
						
						Unit assignU = Jimple.v().newAssignStmt(arg0Local, arg0);
						injectedUnits.add(assignU);
						
						if (stmt instanceof AssignStmt)
						{
							AssignStmt assignStmt = (AssignStmt) stmt;
							Value leftOp = assignStmt.getLeftOp();
							
							Unit callU = Jimple.v().newAssignStmt(leftOp, Jimple.v().newInstanceFieldRef(arg0Local, sf.makeRef()));
							injectedUnits.add(callU);
						}
					}
				}
				else if (calleeMethod.getName().startsWith("set"))   //otherwise start with set
				{
					if (isStaticField)
					{
						Value arg1 = stmt.getInvokeExpr().getArg(1);
						Unit callU = Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(sf.makeRef()), arg1);
						injectedUnits.add(callU);
					}
					else
					{
						Value arg0 = stmt.getInvokeExpr().getArg(0);
						//Local arg0Local = localGenerator.generateLocal(arg0.getType());
						
						Value arg1 = stmt.getInvokeExpr().getArg(1);
						Unit callU = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(arg0, sf.makeRef()), arg1);
						injectedUnits.add(callU);
					}
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

	public void setFieldCallsConfigPath(String fieldCallsConfigPath)
	{
		this.fieldCallsConfigPath = fieldCallsConfigPath;
	}
	
	public SootField getSootField(SootClass callerClass, String fieldName)
	{
		try
		{
			return callerClass.getFieldByName(fieldName);
		}
		catch (Exception ex)
		{
			//TODO: Probably the field does not exist in the class
			
		}
		
		return null;
	}
}
