package lu.uni.snt.droidra.toolkits.cg;

import java.util.HashSet;
import java.util.Set;

public class Result 
{
	public String apkName;
	public int clsNumber;
	public int compNumber;
	public int methodNumber;
	public int stmtNumber;
	public Set<String> classes = new HashSet<String>();
	public Set<String> components = new HashSet<String>();
	public Set<UniqEdge> edges = new HashSet<UniqEdge>();
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(apkName + ",");
		sb.append(clsNumber + ",");
		sb.append(compNumber + ",");
		sb.append(methodNumber + ",");
		sb.append(stmtNumber + ",");
		sb.append(classes + ",");
		sb.append(components + ",");
		sb.append(edges);
		
		return sb.toString();
	}
	
	public void dump()
	{
		System.out.println("apkName: " + apkName);
		System.out.println("clsNumber: " + clsNumber);
		System.out.println("compNumber: " + compNumber);
		System.out.println("methodNumber: " + methodNumber);
		System.out.println("stmtNumber: " + stmtNumber);
		System.out.println("classes: " + classes);
		System.out.println("components: " + components);
		System.out.println("edges: " + edges);
	}
}
