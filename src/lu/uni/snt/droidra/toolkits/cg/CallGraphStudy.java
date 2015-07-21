package lu.uni.snt.droidra.toolkits.cg;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lu.uni.snt.droidra.retarget.DummyMainGenerator;
import soot.Body;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.Chain;

public class CallGraphStudy extends SceneTransformer
{	
	public static Result result = new Result();
	
	public static void main(String[] args) 
	{
		///   
		//String apkPath = args[0];
		//String androidJars = args[1];
		
		String apkPath = "/Users/li.li/Testspace/apps-study/MonkeyJump2_36.apk";
		String androidJars = "/Users/li.li/Project/github/android-platforms";
		
		CallGraphStudy cgStudy = new CallGraphStudy();
		cgStudy.analyze(apkPath, androidJars);
		
		//cgStudy.result.dump();
	}

	public void analyze(String apkPath, String androidJars)
	{
		G.reset();
		
		CallGraphStudy.result = new Result();
		
        Options.v().set_process_dir(Collections.singletonList(apkPath));
        Options.v().set_android_jars(androidJars);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().setPhaseOption("cg.spark", "on");
        
		Scene.v().loadNecessaryClasses();
		
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.CallGraphStudy", new CallGraphStudy()));
		
        SootMethod sm = DummyMainGenerator.generateDummyMain(apkPath);
        Options.v().set_main_class(sm.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(sm));
        
        PackManager.v().runPacks();
        
        result.components = DummyMainGenerator.getComponents(apkPath);
        result.compNumber = result.components.size();
        
        String apkName = apkPath;
        if (apkName.endsWith(".apk"))
        {
        	apkName = apkName.replace(".apk", "");
        }
        
        if (apkName.contains("/"))
        {
        	apkName = apkName.substring(apkName.lastIndexOf('/')+1);
        }
        result.apkName = apkName;
	}
	
	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1)
	{
		CallGraph cg = Scene.v().getCallGraph();
		
		
		for (Iterator<Edge> iter = cg.iterator(); iter.hasNext(); )
		{
			Edge e = (Edge) iter.next();
			
			UniqEdge ue = new UniqEdge();
			ue.srcMethodSig = e.src().getSignature();
			ue.tgtMethodSig = e.tgt().getSignature();
			
			if (UniqEdge.getClass(ue.srcMethodSig).equals("DummyMainClass") ||
				UniqEdge.getMethodSubSig(ue.srcMethodSig).equals("dummyMainMethod"))
			{
				continue;
			}
			
			result.edges.add(ue);
		}
		
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		for (Iterator<SootClass> iter = sootClasses.iterator(); iter.hasNext();)
		{
			SootClass sc = iter.next();
			result.classes.add(sc.getName());
			result.clsNumber++;
			
			List<SootMethod> sootMethods = sc.getMethods();
			for (SootMethod sm : sootMethods)
			{
				result.methodNumber++;
				
				try
				{
					Body body = sm.retrieveActiveBody();
					result.stmtNumber += body.getUnits().size();
				}
				catch (Exception ex)
				{
					//EXPECTED
				}
			}
		}
		
		//System.out.println(cg.size());
		
		/*
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
		
		if (e.src().toString().contains("init") || e.tgt().toString().contains("init"))
			{
				continue;
			}
			
			List<SootClass> superClasses = hierarchy.getSuperclassesOf(e.src().getDeclaringClass());
			
			boolean isSuperClass = false;
			for (SootClass sc : superClasses)
			{
				if (sc.getName().equals(e.tgt().getDeclaringClass().getName()))
				{
					isSuperClass = true;
					break;
				}
			}
			
			if (isSuperClass)
			{
				continue;
			}
			
			System.out.println(e.src() + "-->" + e.tgt());
		*/
		
		//System.exit(0);
	}
}
