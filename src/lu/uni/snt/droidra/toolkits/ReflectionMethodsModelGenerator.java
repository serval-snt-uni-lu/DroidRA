package lu.uni.snt.droidra.toolkits;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReflectionMethodsModelGenerator
{	
	public static Set<String> methodsWithStrParameters = new HashSet<String>();
	
	public static void main(String[] args) throws Exception 
	{
		
		Set<String> sigs = dump("java.lang.Class");
		toClassModel("java.lang.Class", sigs, "class");
		
		sigs = dump("java.lang.reflect.Constructor");
		toClassModel("java.lang.reflect.Constructor", sigs, "constructor");
		
		sigs = dump("java.lang.reflect.Method");
		toClassModel("java.lang.reflect.Method", sigs, "method");
		
		sigs = dump("java.lang.reflect.Field");
		toClassModel("java.lang.reflect.Field", sigs, "field");
		
		toStringModel(methodsWithStrParameters);
	}
	
	public static Set<String> dump(String clsName) throws Exception
	{
		Set<String> sigs = new HashSet<String>();
		
		Class<?> cls = Class.forName(clsName);
		
		Method[] methods = cls.getMethods();
		for (Method method : methods)
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append("<" + clsName + ": ");
			
			sb.append(method.getReturnType().getCanonicalName() + " ");
			
			sb.append(method.getName() + "(");
			
			List<Integer> strParams = new ArrayList<Integer>();
			
			for (int i = 0; i < method.getParameterTypes().length; i++)
			{
				if (i > 0)
				{
					sb.append(",");
				}
				
				String ptName = method.getParameterTypes()[i].getCanonicalName();
				
				sb.append(ptName);
				
				if ("java.lang.String".equals(ptName))
				{
					strParams.add(i);
					
					if (i != 0)
					{
						System.out.println("***************DEBUG**********************");
					}
				}
			}
			
			sb.append(")>");
			
			if (! strParams.isEmpty())
			{
				methodsWithStrParameters.add(sb.toString());
			}
			
			sigs.add(sb.toString());
			
			sb.append(" -> {");
			
			for (int i = 0; i < strParams.size(); i++)
			{
				if (i > 0)
				{
					sb.append(",");
				}
				
				sb.append(strParams.get(i));
			}
			
			sb.append("}");
			
			//System.out.println(sb.toString());
		}
		
		return sigs;
	}
	
	public static void toClassModel(String clsName, Set<String> methods, String suffix)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("class " + clsName + " {");
		
		sb.append("Class declaringClass_" + suffix + ";");
		sb.append("String name_" + suffix + ";");
		
		
		
		if ("java.lang.Class".equals(clsName))
		{
			for (String method : methods)
			{
				if (method.contains("forName"))
				{
					sb.append("mod gen " + method + " {");
					sb.append("0: replace name_"  + suffix + ";");
					sb.append("}");
				}
				else
				{
					sb.append("query " + method + " {");
					sb.append("-1: type " + clsName + ";");
					sb.append("}");
				}
			}
			
			sb.append("}");
		}
		else
		{
			Set<String> modGenMethods = new HashSet<String>();
			
			if ("java.lang.reflect.Field".equals(clsName))
			{
				modGenMethods.add("<java.lang.Class: java.lang.reflect.Field getDeclaredField(java.lang.String)>");
				modGenMethods.add("<java.lang.Class: java.lang.reflect.Field getField(java.lang.String)>");
			}
			else if ("java.lang.reflect.Method".equals(clsName))
			{
				modGenMethods.add("<java.lang.Class: java.lang.reflect.Method getDeclaredMethod(java.lang.String,java.lang.Class[])>");
				modGenMethods.add("<java.lang.Class: java.lang.reflect.Method getMethod(java.lang.String,java.lang.Class[])>");
			}
			else if ("java.lang.reflect.Constructor".equals(clsName))
			{
				modGenMethods.add("<java.lang.Class: java.lang.reflect.Constructor getConstructor(java.lang.Class[])>");
				modGenMethods.add("<java.lang.Class: java.lang.reflect.Constructor[] getConstructors()>");
			}
			
			for (String modGenMethod : modGenMethods)
			{
				sb.append("mod gen " + modGenMethod + " {");
				sb.append("-1: replace declaringClass_" + suffix + ";");
				sb.append("0: replace name_" + suffix + ";");
				sb.append("}");
			}
			
			for (String method : methods)
			{
				sb.append("query " + method + " {");
				sb.append("-1: type " + clsName + ";");
				sb.append("}");
			}
			
			sb.append("}");
		}
			
		System.out.println(sb.toString());
	}
	
	public static void toStringModel(Set<String> methodsWithStrParameters)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("class dummy {");

		for (String method : methodsWithStrParameters)
		{
			sb.append("query " + method + " {");
			sb.append("0: type String;");
			sb.append("}");
		}
		
		sb.append("}");

		System.out.println(sb.toString());
	}
}
