package lu.uni.snt.droidra.model;

import soot.jimple.Stmt;

public enum StmtType 
{
	SIMPLE_STRING, FIELD_CALL, METHOD_CALL, CONSTRUCTOR_CALL, CLASS_CALL, CLASS_NEW_INSTANCE;
	
	public static StmtType getType(Stmt stmt)
	{
		if (! stmt.containsInvokeExpr())
		{
			throw new RuntimeException(stmt + " must contain an invoke expression.");
		}
		
		String cls = stmt.getInvokeExpr().getMethod().getDeclaringClass().getName();
		if ("java.lang.reflect.Field".equals(cls))
		{
			return FIELD_CALL;
		}
		else if ("java.lang.reflect.Method".equals(cls))
		{
			return METHOD_CALL;
		}
		else if ("java.lang.reflect.Constructor".equals(cls))
		{
			return CONSTRUCTOR_CALL;
		}
		else if ("java.lang.Class".equals(cls) && "newInstance".equals(stmt.getInvokeExpr().getMethod().getName()))
		{
			return CLASS_NEW_INSTANCE;
		}
		else if ("java.lang.Class".equals(cls))
		{
			return CLASS_CALL;
		}
		else
		{
			return SIMPLE_STRING;
		}
	}
	
	public static String toStringType(StmtType type)
	{
		switch (type)
		{
		case FIELD_CALL:
			return "FIELD_CALL";
		case METHOD_CALL:
			return "METHOD_CALL";
		case CONSTRUCTOR_CALL:
			return "CONSTRUCTOR_CALL";
		case CLASS_NEW_INSTANCE:
			return "CLASS_NEW_INSTANCE";
		case CLASS_CALL:
			return "CLASS_CALL";
		default:
			return "SIMPLE_STRING";
		}
	}
}
