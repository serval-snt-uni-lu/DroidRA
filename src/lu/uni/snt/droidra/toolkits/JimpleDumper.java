package lu.uni.snt.droidra.toolkits;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;
import soot.util.Chain;

public class JimpleDumper extends SceneTransformer
{	
	public static void main(String[] args) 
	{
		String apkPath = args[0];
		String androidJars = args[1];
		
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
		
		if (apkPath.endsWith(".apk"))
		{
			Options.v().set_src_prec(Options.src_prec_apk);
		}
		else
		{
			Options.v().set_src_prec(Options.src_prec_class);
		}
		
		Options.v().set_output_format(Options.output_format_jimple);
			
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.JimpleDumper", new ReflectionCallUsageStudy()));
		
        soot.Main.main(args2);

	}

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) 
	{
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		
		for (Iterator<SootClass> iter = sootClasses.iterator(); iter.hasNext();)
		{
			SootClass sc = iter.next();
			
			if (sc.getName().startsWith("android"))
			{
				continue;
			}
			
			for (SootMethod sm : sc.getMethods())
			{
				try
				{
					Body body = sm.retrieveActiveBody();
					System.out.println(body);
				}
				catch (Exception ex)
				{
					System.out.println("No body for method " + sm.getSignature());
				}
			}
		}
		
		System.exit(0);
	}
}
