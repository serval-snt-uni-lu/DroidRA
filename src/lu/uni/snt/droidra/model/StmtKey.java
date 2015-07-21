package lu.uni.snt.droidra.model;

import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * This class should be only used inside the process of COAL analysis.
 * In case of sharing this info between processes, using UniqStmt instead.
 * 
 * @author li.li
 *
 */
public class StmtKey 
{
	private SootMethod method;
	private Stmt stmt;
	//private int stmtSeq; //the first stmt is one
	
	public StmtKey() {}
	
	public StmtKey(SootMethod method, Stmt unit)
	{
		this.method = method;
		this.stmt = unit;
		
		//stmtSeq = getStmtSeq(getMethod(), this.stmt);
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append(method.getDeclaringClass().getName() + "/");
		sb.append(method.getSignature() + ":");
		sb.append(stmt.toString());
		
		return sb.toString();
	}


	@Override
	public int hashCode() 
	{
		return toString().hashCode();
	}
	
	
	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		StmtKey other = (StmtKey) obj;
		if (hashCode() != other.hashCode())
			return false;
		
		return true;
	}


	//In case this class has been used in another Scene, e.g., a new type of transformation
	public SootMethod getMethod() 
	{
		//return Scene.v().getMethod(method.getSignature());
		return this.method;
	}

	public Stmt getStmt() 
	{
		/*
		SootMethod method = getMethod();
		
		if (null != method)
		{
			int count = 0;
			
			for (Iterator<Unit> iter = method.retrieveActiveBody().getUnits().snapshotIterator(); iter.hasNext(); )
			{
				Stmt tmpStmt = (Stmt) iter.next();
				count++;
				
				if (stmtSeq == count)
				{
					return tmpStmt;
				}
			}
		}*/
		
		return stmt;
	}

	public void setMethod(SootMethod method) {
		this.method = method;
		
		//if (this.stmt != null)
		//{
			//stmtSeq = getStmtSeq(getMethod(), this.stmt);
			
			//JimpleIndexNumberTag indexTag = new JimpleIndexNumberTag(index);
			//stmt.addTag(indexTag);
		//}
	}


	public void setStmt(Stmt stmt) {
		this.stmt = stmt;
		
		//if (this.method != null)
		//{
			//stmtSeq = getStmtSeq(getMethod(), stmt);
			
			//JimpleIndexNumberTag indexTag = new JimpleIndexNumberTag(index);
			//stmt.addTag(indexTag);
		//}
	}
	
	/*
	public static int getStmtSeq(SootMethod sootMethod, Stmt stmt)
	{
		int count = 0;
		
		for (Iterator<Unit> iter = sootMethod.retrieveActiveBody().getUnits().snapshotIterator(); iter.hasNext(); )
		{
			Stmt tmpStmt = (Stmt) iter.next();
			count++;
			
			if (tmpStmt.toString().equals(stmt.toString()))
			{
				return count;
			}
		}
		
		throw new RuntimeException("No such stmt exist in method " + sootMethod);
	}*/
}
