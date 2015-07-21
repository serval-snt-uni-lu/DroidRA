package lu.uni.snt.droidra.toolkits.cg;


public class Main {

	public static void main(String[] args) 
	{
		String app1 = args[0];
		String app2 = args[1];
		String androidJars = args[2];

		PairResult pResult = new PairResult();
		
		CallGraphStudy cgStudy4App1 = new CallGraphStudy();
		cgStudy4App1.analyze(app1, androidJars);
		Result result1 = CallGraphStudy.result;
		pResult.result1 = result1;
		
		CallGraphStudy cgStudy4App2 = new CallGraphStudy();
		cgStudy4App2.analyze(app2, androidJars);
		Result result2 = CallGraphStudy.result;
		pResult.result2 = result2;
		
		for (String cls : result1.classes)
		{
			pResult.clsOnlyApp1.add(cls);
		}
		pResult.clsOnlyApp1.removeAll(result2.classes);
		
		for (String cls : result2.classes)
		{
			pResult.clsOnlyApp2.add(cls);
		}
		pResult.clsOnlyApp2.removeAll(result1.classes);
		
		
		for (UniqEdge ue : result2.edges)
		{
			String srcClass = UniqEdge.getClass(ue.srcMethodSig);
			String tgtClass = UniqEdge.getClass(ue.tgtMethodSig);
			
			if (pResult.clsOnlyApp1.contains(srcClass) && pResult.clsOnlyApp1.contains(tgtClass))
			{
				pResult.cgOnlyApp1.add(ue);
			}
			else if (pResult.clsOnlyApp2.contains(srcClass) && pResult.clsOnlyApp2.contains(tgtClass))
			{
				pResult.cgOnlyApp2.add(ue);
			}
			else if (pResult.clsOnlyApp1.contains(srcClass) && pResult.clsOnlyApp2.contains(tgtClass))
			{
				pResult.cgFromApp1ToApp2.add(ue);
			}
		}
		
		System.out.println("====>" + pResult);
		pResult.toFile("pair-result.txt");
	}

}
