package lu.uni.snt.droidra;
import edu.psu.cse.siis.coal.DefaultResultProcessor;
import edu.psu.cse.siis.coal.Result;
import edu.psu.cse.siis.coal.Results;


public class DroidRAResultProcessor extends DefaultResultProcessor
{
	public void processResult() 
	{
		for (Result result : Results.getResults()) 
		{
			DroidRAResult.stmtKeyValues.putAll(DroidRAResult.toStmtKeyValues(result));
			
			GlobalRef.uniqStmtKeyValues = DroidRAResult.toUniqStmtKeyValues(DroidRAResult.stmtKeyValues);
		}
	}
	
}
