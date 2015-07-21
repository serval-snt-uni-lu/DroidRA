package lu.uni.snt.droidra;
import edu.psu.cse.siis.coal.CommandLineArguments;
import edu.psu.cse.siis.coal.DefaultAnalysis;


public class DroidRAAnalysis<A  extends CommandLineArguments> extends DefaultAnalysis<A>
{
	@Override
	  protected void processResults(A commandLineArguments) {
		DroidRAResultProcessor processor = new DroidRAResultProcessor();
	    processor.processResult();
	  }
}
