package lu.uni.snt.droidra;
import java.util.List;

import edu.psu.cse.siis.coal.DefaultResultProcessor;
import edu.psu.cse.siis.coal.Result;
import edu.psu.cse.siis.coal.Results;


public class DroidRAResultProcessor extends DefaultResultProcessor
{
	public static List<Result> results = null;
	
	public void processResult() 
	{
		results = Results.getResults();
		
		for (Result result : Results.getResults()) 
		{
			DroidRAResult.stmtKeyValues.putAll(DroidRAResult.toStmtKeyValues(result));
			
			GlobalRef.uniqStmtKeyValues = DroidRAResult.toUniqStmtKeyValues(DroidRAResult.stmtKeyValues);
		}
	}
	
}
