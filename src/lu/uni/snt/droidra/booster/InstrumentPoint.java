package lu.uni.snt.droidra.booster;

import java.util.Iterator;

import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import lu.uni.snt.droidra.model.SimpleStmtValue;
import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.UniqStmt;

public class InstrumentPoint 
{
	public UniqStmt uniqStmt;
	public StmtKey stmtKey;
	public SimpleStmtValue simpleStmtValue;
	
	public void updateStmtKey()
	{
		stmtKey = new StmtKey();
		
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
	}
	
	public void updateUniqStmt()
	{
		uniqStmt = new UniqStmt();
		//TODO:
	}

	@Override
	public String toString() {
		return "InstrumentPoint [uniqStmt=" + uniqStmt + ", stmtKey=" + stmtKey
				+ ", simpleStmtValue=" + simpleStmtValue + "]";
	}
}
