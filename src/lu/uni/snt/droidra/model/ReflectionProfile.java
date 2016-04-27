package lu.uni.snt.droidra.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.droidra.ClassDescription;
import lu.uni.snt.droidra.booster.InstrumentationUtils;
import soot.Scene;
import soot.SootClass;
import soot.jimple.Stmt;

public class ReflectionProfile 
{
	public static Map<String, RClass> rClasses = new HashMap<String, RClass>();
	public static Map<String, String> simpleStrs = new HashMap<String, String>();
	
	public static void fillReflectionProfile(Map<StmtKey, StmtValue> stmtKeyValues)
	{
		for (Map.Entry<StmtKey, StmtValue> entry : stmtKeyValues.entrySet())
		{
			StmtKey key = entry.getKey();
			StmtValue value = entry.getValue();
			
			for (ClassDescription clsDesc : value.getClsSet())
			{
				switch (value.getType())
				{
				case CLASS_NEW_INSTANCE:
					String clsName = clsDesc.name;
					RClass rClass = getRClass(ReflectionProfile.rClasses, clsName);
					
					if (exist(clsDesc.name, null, 1))
					{
						rClass.exist = true;
					}
					
					RConstructor rConstructor = new RConstructor();
					if (exist(clsDesc.name, "<init>", 2))
					{
						rConstructor.exist = true;
					}
					
					rClass.constructors.add(rConstructor);
					
					ReflectionProfile.rClasses.put(clsName, rClass);
					break;
				case CONSTRUCTOR_CALL:
					clsName = clsDesc.cls;
					rClass = getRClass(ReflectionProfile.rClasses, clsName);
					
					if (exist(clsDesc.cls, null, 1))
					{
						rClass.exist = true;
					}
					
					rConstructor = new RConstructor();
					if (exist(clsDesc.name, "<init>", 2))
					{
						rConstructor.exist = true;
					}
					
					rClass.constructors.add(rConstructor);
					ReflectionProfile.rClasses.put(clsName, rClass);
					break;
				case FIELD_CALL:
					clsName = clsDesc.cls;
					String fieldName = clsDesc.name;
					rClass = getRClass(ReflectionProfile.rClasses, clsName);
					
					RField rField = new RField();
					rField.name = fieldName;
					rField.isStatic = InstrumentationUtils.isStaticReflectiveInvocation(key.getStmt());
					rField.probeAndFillType(key.getStmt());
					if (exist(clsDesc.cls, null, 1))
					{
						rClass.exist = true;
					}
					if (exist(clsDesc.cls, fieldName, 3))
					{
						rField.exist = true;
					}
					
					rClass.fields.add(rField);
					ReflectionProfile.rClasses.put(clsName, rClass);
					break;
				case METHOD_CALL:
					clsName = clsDesc.cls;
					String methodName = clsDesc.name;
					rClass = getRClass(ReflectionProfile.rClasses, clsName);
					
					RMethod rMethod = new RMethod();
					rMethod.name = methodName;
					rMethod.isStatic = InstrumentationUtils.isStaticReflectiveInvocation(key.getStmt());
					if (exist(clsDesc.cls, null, 1))
					{
						rClass.exist = true;
					}
					if (exist(clsDesc.cls, methodName, 2))
					{
						rMethod.exist = true;
					}
					
					//At the moment, let the returnType and the paramTypes as empty
					//TODO: Come out a heuristic approach to infer the parameter types (do such for fieldCall as well).
					
					rClass.methods.add(rMethod);
					ReflectionProfile.rClasses.put(clsName, rClass);
					break;
				default:    //SIMPLE_STRING
					break;
				}
			}
			
			for (String str : value.getParam0Set())
			{
				simpleStrs.put(key.getMethod().getSignature() + "\n      " + key.getStmt().toString(), str);
			}
		}
	}
	
	/**
	 * type = 1 for class
	 * 		= 2 for method
	 * 		= 3 for field
	 * 
	 * @param clsName
	 * @param fieldOrMethodName
	 * @param type
	 * @return
	 */
	private static boolean exist(String clsName, String fieldOrMethodName, int type)
	{
		
		if (! Scene.v().containsClass(clsName))
		{
			return false;
		}
		
		if (1 != type)
		{
			SootClass sc = Scene.v().getSootClass(clsName);
			
			try
			{
				Object obj = null;
				
				if (3 == type)
				{
					obj = sc.getFieldByName(fieldOrMethodName);
				}
				else if (2 == type)   //Method
				{
					obj = sc.getMethodByName(fieldOrMethodName);
				}
				
				if (null == obj)
				{
					return false;
				}
			}
			catch (Exception ex)
			{
				return false;
			}
		}
		
		return true;
	}
	
	private static RClass getRClass(Map<String, RClass> rClasses, String clsName)
	{
		RClass rClass = null;
		
		if (! rClasses.containsKey(clsName))
		{
			rClass = new RClass();
			rClass.name = clsName;
		}
		else
		{
			rClass = rClasses.get(clsName);
		}
		
		return rClass;
	}
	
	public static void dump(String prefix)
	{
		for (Map.Entry<String, RClass> entry : rClasses.entrySet())
		{
			String clsName = entry.getKey();
			RClass rClass = entry.getValue();
			
			System.out.println("----------------------------------");
			
			System.out.println(prefix + clsName);
			System.out.println(prefix + "Exists: " + rClass.exist);
			
			if (rClass.fields.size() > 0)
				System.out.println(prefix + "Fields: ");
			for (RField rField : rClass.fields)
			{
				System.out.println(prefix + "    " + rField);
			}
			
			if (rClass.constructors.size() > 0)
				System.out.println(prefix + "Constructors: ");
			for (RConstructor rConstructor : rClass.constructors)
			{
				System.out.println(prefix + "    " + rConstructor);
			}
			
			if (rClass.methods.size() > 0)
				System.out.println(prefix + "Methods: ");
			for (RMethod rMethod : rClass.methods)
			{
				System.out.println(prefix + "    " + rMethod);
			}
		}
		
		if (simpleStrs.size() > 0)
			System.out.println(prefix + "Others: ");	
		
		for (String key : simpleStrs.keySet())
		{
			System.out.println(prefix + "    " + key + "\n" + prefix + "      " + simpleStrs.get(key));
		}
	}
	
	public static void dump()
	{
		dump("");
	}
	
	public static class RClass
	{
		public String name;
		public Set<RField> fields = new HashSet<RField>();
		public Set<RConstructor> constructors = new HashSet<RConstructor>();
		public Set<RMethod> methods = new HashSet<RMethod>();
		public boolean exist = false;
		
		
		@Override
		public String toString() 
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append(name + ",");
			sb.append(constructors + ",");
			sb.append(fields + ",");
			sb.append(methods);

			return sb.toString();
		}
	}
	
	public static class RField
	{
		public String name;
		public String type;
		public boolean isStatic = false;
		public boolean exist = false;
		
		@Override
		public String toString() 
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append(name + ",");
			sb.append(type + ",");
			sb.append(isStatic + ",");
			sb.append(exist + ",");
			
			return sb.toString();
		}
		
		@Override
		public int hashCode() 
		{
			return toString().hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			RField other = (RField) obj;
			
			if (! name.equals(other.name))
			{
				return false;
			}
			else if (! type.equals(other.type))
			{
				return false;
			}
			else if (isStatic != other.isStatic)
			{
				return false;
			}
			
			return true;
		}
		
		/**
		 * This is based on the method itself.
		 * 
		 * @param fieldCallStmt
		 */
		public void probeAndFillType(Stmt fieldCallStmt)
		{
			String fieldCallName = fieldCallStmt.getInvokeExpr().getMethod().getName();
			if (fieldCallName.contains("Boolean"))
			{
				type = "boolean";
			}
			else if (fieldCallName.contains("Byte"))
			{
				type = "byte";
			}
			else if (fieldCallName.contains("Char"))
			{
				type = "char";
			}
			else if (fieldCallName.contains("Short"))
			{
				type = "short";
			}
			else if (fieldCallName.contains("Int"))
			{
				type = "int";
			}
			else if (fieldCallName.contains("Long"))
			{
				type = "long";
			}
			else if (fieldCallName.contains("Float"))
			{
				type = "float";
			}
			else if (fieldCallName.contains("Double"))
			{
				type = "double";
			}
			else 
			{
				type = "java.lang.Object";
			}
		}
	}
	
	public static class RMethod 
	{
		public String name;
		public String returnType;
		public List<String> paramTypes = new ArrayList<String>();
		public boolean exist = false;
		public boolean isStatic = false;
		
		@Override
		public String toString() 
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append(name + ",");
			sb.append(returnType + ",");
			
			for (String paramType : paramTypes)
			{
				sb.append(paramType + ",");
			}
			
			String rtStr = sb.toString();
			
			if (rtStr.endsWith(","))
			{
				rtStr = rtStr.substring(0, rtStr.length()-1);
			}
			
			sb.append("," + exist);
			
			return rtStr;
		}
		
		@Override
		public int hashCode() 
		{
			return toString().hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			RMethod other = (RMethod) obj;
			
			if (paramTypes.size() != other.paramTypes.size())
			{
				return false;
			}
			else
			{
				for (int i = 0; i < paramTypes.size(); i++)
				{
					if (! paramTypes.get(i).equals(other.paramTypes.get(i)))
					{
						return false;
					}
				}
			}
			
			return true;
		}
	}
	
	public static class RConstructor
	{
		public List<String> paramTypes = new ArrayList<String>();
		public boolean exist = false;
		
		@Override
		public String toString() 
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append("<init>");
			
			for (String paramType : paramTypes)
			{
				sb.append(paramType + ",");
			}
			
			String rtStr = sb.toString();
			
			if (rtStr.endsWith(","))
			{
				rtStr = rtStr.substring(0, rtStr.length()-1);
			}
			
			sb.append("," + exist);
			
			return rtStr;
		}
		
		@Override
		public int hashCode() 
		{
			return toString().hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			RConstructor other = (RConstructor) obj;
			
			if (paramTypes.size() != other.paramTypes.size())
			{
				return false;
			}
			else
			{
				for (int i = 0; i < paramTypes.size(); i++)
				{
					if (! paramTypes.get(i).equals(other.paramTypes.get(i)))
					{
						return false;
					}
				}
			}
			
			return true;
		}
	}
	
	public static void main(String[] args)
	{
		RConstructor rc0 = new RConstructor();
		rc0.paramTypes.add("xxx");
		RConstructor rc1 = new RConstructor();
		rc1.paramTypes.add("xxx");
		RConstructor rc2 = new RConstructor();
		rc2.paramTypes.add("xxx");
		
		Set<RConstructor> rcs = new HashSet<RConstructor>();
		rcs.add(rc0);
		rcs.add(rc1);
		rcs.add(rc2);
		
		System.out.println(rcs.size());
	}
}
