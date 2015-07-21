package lu.uni.snt.droidra.toolkits;

import java.util.HashSet;
import java.util.Set;

import soot.SootClass;

public class SootUtils 
{
	public static Set<String> getAllSuperClasses(SootClass sootClass)
	{
		Set<String> clsSet = new HashSet<String>();
		
		clsSet.add(sootClass.getName());
		
		while (sootClass.hasSuperclass())
		{
			sootClass = sootClass.getSuperclass();
			
			String clsName = sootClass.getName();
			clsSet.add(clsName);
		}
		
		return clsSet;
	}
}
