package lu.uni.snt.droidra.toolkits.cg;

import java.util.HashSet;
import java.util.Set;

import lu.uni.snt.droidra.toolkits.CommonUtils;

public class PairResult 
{
	public Result result1;
	public Result result2;
	
	public Set<String> clsOnlyApp1 = new HashSet<String>();
	public Set<String> clsOnlyApp2 = new HashSet<String>();
	
	public Set<UniqEdge> cgOnlyApp1 = new HashSet<UniqEdge>();
	public Set<UniqEdge> cgOnlyApp2 = new HashSet<UniqEdge>();
	public Set<UniqEdge> cgFromApp1ToApp2 = new HashSet<UniqEdge>();
	
	public void toFile(String filePath)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(result1.toString());
		sb.append(result2.toString());
		sb.append(clsOnlyApp1 + ",");
		sb.append(clsOnlyApp2 + ",");
		sb.append(cgOnlyApp1 + ",");
		sb.append(cgOnlyApp2 + ",");
		sb.append(cgFromApp1ToApp2);
		
		String content = sb.toString();
		
		CommonUtils.writeResultToFile(filePath, content + "\n");
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(result1.apkName + ",");
		sb.append(result2.apkName + ",");
		sb.append(result1.clsNumber + ",");
		sb.append(result2.clsNumber + ",");
		sb.append(clsOnlyApp1.size() + ",");
		sb.append(clsOnlyApp2.size() + ",");
		sb.append(cgOnlyApp1.size() + ",");
		sb.append(cgOnlyApp2.size() + ",");
		sb.append(cgFromApp1ToApp2.size());
		
		return sb.toString();
	}
}
