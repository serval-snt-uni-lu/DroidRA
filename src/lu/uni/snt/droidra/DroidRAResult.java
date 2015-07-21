package lu.uni.snt.droidra;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtType;
import lu.uni.snt.droidra.model.StmtValue;
import lu.uni.snt.droidra.model.UniqStmt;
import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Result;
import edu.psu.cse.siis.coal.values.PathValue;
import edu.psu.cse.siis.coal.values.PropagationValue;



public class DroidRAResult
{
	public static Map<StmtKey, StmtValue> stmtKeyValues = new HashMap<StmtKey, StmtValue>();
	public static Map<UniqStmt, StmtValue> uniqStmtKeyValues = new HashMap<UniqStmt, StmtValue>();
	
	public static Map<StmtKey, StmtValue> toStmtKeyValues(Result result)
	{
		Map<StmtKey, StmtValue> stmtKeyValues = new HashMap<StmtKey, StmtValue>();
		
		for (Map.Entry<Unit, Map<Integer, Object>> entry : result.getResults().entrySet()) 
		{
			StmtKey stmtKey = new StmtKey();
			
			Stmt stmt = (Stmt) entry.getKey();
		   	SootMethod method = AnalysisParameters.v().getIcfg().getMethodOf(stmt);
		   	
		   	stmtKey.setMethod(method);
		   	stmtKey.setStmt(stmt);

		   	//Normally, the size of entry.getValue() should equal to one
		   	for (Map.Entry<Integer, Object> entry2 : entry.getValue().entrySet()) 
		   	{
		   		StmtValue stmtValue = new StmtValue(StmtType.getType(stmt));
		   		
		   		int seq = entry2.getKey();
		   		Object obj = entry2.getValue();
		   		
		   		
		   		//only considering -1 and 0 at the momment
		   		//The seq value is depending on the model
		   		if (-1 == seq)    //The caller ref
		   		{
		   			if (obj instanceof PropagationValue)
		   			{
		   				PropagationValue pv = (PropagationValue) obj;
		   				Set<PathValue> pathValues = pv.getPathValues();
		   				
		   				for (PathValue pathValue : pathValues)
		   				{
		   					if (null == pathValue || pathValue.toString().equals("null"))
		   					{
		   						continue;
		   					}
		   					
		   					ClassDescription clsDesc = new ClassDescription();
		   					if (stmtValue.getType() == StmtType.FIELD_CALL)
		   					{
		   						clsDesc.cls = pathValue.getFieldValue("declaringClass_field").toString();
		   						clsDesc.name = pathValue.getFieldValue("name_field").toString();
		   					}
		   					else if (stmtValue.getType() == StmtType.METHOD_CALL)
		   					{
		   						clsDesc.cls = pathValue.getFieldValue("declaringClass_method").toString();
		   						clsDesc.name = pathValue.getFieldValue("name_method").toString();
		   					}
		   					else if (stmtValue.getType() == StmtType.CLASS_NEW_INSTANCE)
		   					{
		   						clsDesc.name = pathValue.getFieldValue("name_class").toString();
		   						clsDesc.cls = clsDesc.name;
		   					}
		   					else if (stmtValue.getType() == StmtType.CONSTRUCTOR_CALL)
		   					{
		   						clsDesc.cls = pathValue.getFieldValue("declaringClass_constructor").toString();
		   					}
		   					else if (stmtValue.getType() == StmtType.CLASS_CALL)
		   					{
		   						clsDesc.cls = pathValue.getFieldValue("name_class").toString();
		   					}
		   					
		   					if (null != clsDesc.cls && clsDesc.equals("(.*)"))
		   					{
		   						continue;
		   					}
		   					
		   					if (null != clsDesc.cls && clsDesc.cls.contains("/"))
		   					{
		   						clsDesc.cls = clsDesc.cls.replace("/", ".");
		   					}

		   					stmtValue.getClsSet().add(clsDesc);
		   				}
		   			}
		   			else
		   			{
		   				//TODO: implementing later for directly parsing the str
		   			}
		   		}
		   		else if (0 == seq)   //parameter
		   		{
		   			String str = obj.toString();
		   			str = str.replace("[", "").replace("]", "");
		   			
		   			stmtValue.getParam0Set().add(str);
		   		}

		   		if (stmtValue.getClsSet().size() != 0 || stmtValue.getParam0Set().size() != 0)
		   			stmtKeyValues.put(stmtKey, stmtValue);
		    }
		}
		
		return stmtKeyValues;
	}

	public static Map<UniqStmt, StmtValue> toUniqStmtKeyValues(Map<StmtKey, StmtValue> stmtKeyValues)
	{
		uniqStmtKeyValues = new HashMap<UniqStmt, StmtValue>();
		
		for (Map.Entry<StmtKey, StmtValue> entry : stmtKeyValues.entrySet())
		{
			StmtKey stmtKey = entry.getKey();
			StmtValue stmtValue = entry.getValue();
			
			UniqStmt uniqStmt = new UniqStmt();
			
			uniqStmt.className = stmtKey.getMethod().getDeclaringClass().getName();
			uniqStmt.methodSignature = stmtKey.getMethod().getSignature();
			uniqStmt.stmt = stmtKey.getStmt().toString();
			
			Body body = stmtKey.getMethod().retrieveActiveBody();
			int count = 0;
			for (Iterator<Unit> iter = body.getUnits().snapshotIterator(); iter.hasNext(); )
			{
				Stmt stmt = (Stmt) iter.next();
				count++;
				
				if (stmt.toString().equals(stmtKey.getStmt().toString()))
				{
					uniqStmt.stmtSeq = count;
					break;
				}
			}
			
			uniqStmtKeyValues.put(uniqStmt, stmtValue);
		}
		
		return uniqStmtKeyValues;
	}
	
	public static Map<StmtKey, StmtValue> toStmtKeyValues(Map<UniqStmt, StmtValue> uniqStmtKeyValues)
	{
		stmtKeyValues = new HashMap<StmtKey, StmtValue>();
		
		for (Map.Entry<UniqStmt, StmtValue> entry : uniqStmtKeyValues.entrySet())
		{
			UniqStmt uniqStmt = entry.getKey();
			StmtValue stmtValue = entry.getValue();
			
			StmtKey stmtKey = new StmtKey();
			SootMethod sm = Scene.v().getMethod(uniqStmt.methodSignature);
			stmtKey.setMethod(sm);
			
			Body body = sm.retrieveActiveBody();
			int count = 0;
			for (Iterator<Unit> iter = body.getUnits().snapshotIterator(); iter.hasNext(); )
			{
				Stmt stmt = (Stmt) iter.next();
				count++;
				
				if (count == uniqStmt.stmtSeq)
				{
					stmtKey.setStmt(stmt);
					break;
				}
			}
			
			stmtKeyValues.put(stmtKey, stmtValue);
		}
		
		return stmtKeyValues;
	}
	
	public static Map<UniqStmt, StmtKey> toStmtKeys(Map<UniqStmt, StmtValue> uniqStmtKeyValues)
	{
		Map<UniqStmt, StmtKey> keyPairs = new HashMap<UniqStmt, StmtKey>();
		
		for (Map.Entry<UniqStmt, StmtValue> entry : uniqStmtKeyValues.entrySet())
		{
			UniqStmt uniqStmt = entry.getKey();
			
			StmtKey stmtKey = new StmtKey();
			SootMethod sm = Scene.v().getMethod(uniqStmt.methodSignature);
			stmtKey.setMethod(sm);
			
			Body body = sm.retrieveActiveBody();
			int count = 0;
			for (Iterator<Unit> iter = body.getUnits().snapshotIterator(); iter.hasNext(); )
			{
				Stmt stmt = (Stmt) iter.next();
				count++;
				
				if (count == uniqStmt.stmtSeq)
				{
					stmtKey.setStmt(stmt);
					break;
				}
			}
			
			keyPairs.put(uniqStmt, stmtKey);
		}
		
		return keyPairs;
	}
}
