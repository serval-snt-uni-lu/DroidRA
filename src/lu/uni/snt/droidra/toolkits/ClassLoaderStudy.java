package lu.uni.snt.droidra.toolkits;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import soot.Body;
import soot.G;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.util.Chain;

public class ClassLoaderStudy extends SceneTransformer
{	
	public static void main(String[] args) 
	{
		String apkPath = args[0];
		String androidJars = args[1];
		
		//String apkPath = "/Users/li.li/Project/workspace_for_gittqtuuuu/soot-infoflow-android-iccta/error/gameloft/com.gameloft.android.ANMP.GloftMTHM.apk";
		//String androidJars = "/Users/li.li/Project/github/android-platforms";
		
		//CommonUtils.writeResultToFile("test/classLoaderStudy.txt", apkPath + "\n");
		
		String[] args2 =
        {
            "-process-dir", apkPath,
            "-android-jars", androidJars,
            "-ire",
            "-pp",
            "-allow-phantom-refs",
            "-w",
			"-p", "cg", "enabled:false",
			"-p", "jop.cpf", "enabled:true"
        };
		
		G.reset();
		
		Options.v().set_src_prec(Options.src_prec_class);
		Options.v().set_output_format(Options.output_format_none);
			
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.ClassLoaderStudy", new ClassLoaderStudy()));
		
        soot.Main.main(args2);

	}

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) 
	{
		Chain<SootClass> sootClasses = Scene.v().getClasses();
		
		for (Iterator<SootClass> iter = sootClasses.iterator(); iter.hasNext();)
		{
			SootClass sc = iter.next();
			
			List<SootMethod> methodList = sc.getMethods();
			
			for (SootMethod sm : methodList)
			{
				try
				{
					Body body = sm.retrieveActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					
					for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
					{
						Stmt stmt = (Stmt) iterU.next();
						
						if (stmt.toString().contains("ClassLoader"))
						{
							System.out.println(stmt);
							CommonUtils.writeResultToFile("test/classLoaderStudy.txt", stmt.toString() + "\n");
						}
						
					}
					
				}
				catch (Exception ex)
				{
					System.out.println("No body for method " + sm.getSignature());
				}
			}
		}
		
		//This will stop the wrap (main) thread as well.
		//System.exit(0);
	}
}
