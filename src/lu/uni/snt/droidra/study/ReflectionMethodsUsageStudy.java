package lu.uni.snt.droidra.study;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.droidra.GlobalRef;
import lu.uni.snt.droidra.retarget.RetargetWithDummyMainGenerator;
import lu.uni.snt.droidra.toolkits.CommonUtils;
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
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import soot.util.Chain;
import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Result;
import edu.psu.cse.siis.coal.Results;
import edu.psu.cse.siis.coal.values.PathValue;
import edu.psu.cse.siis.coal.values.PropagationValue;

public class ReflectionMethodsUsageStudy extends SceneTransformer
{	
	public static void main(String[] args) 
	{
		init();
				
		String appPath = args[0];
		String forceAndroidJar = args[1];
		String outputPath = args[2];
		
		try
		{
			if (appPath.contains("/"))
			{
				currentAppName = appPath.substring(appPath.lastIndexOf('/')+1);
			}

			studyOneApp(appPath, forceAndroidJar);
			
			OneApp oa = appStudyResults.get(currentAppName);
			System.out.println(oa);
			CommonUtils.writeResultToFile(outputPath, oa + "\n");
		}
		catch (Exception ex)
		{
			CommonUtils.writeResultToFile(outputPath, currentAppName + ", exceptions" + "\n");
		}
	}
	
	
	public static String currentAppName = "";
	static Set<String> reflectionMethods = new HashSet<String>();
	static Map<String, OneApp> appStudyResults = new HashMap<String, OneApp>();
	
	static Set<String> appClasses = new HashSet<String>();
	
	public static void init()
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader("res/reflection-methods-rq1-study.txt"));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				String[] strs = line.split("->");
				reflectionMethods.add(strs[0].trim());
			}
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	

	public static void runReflectionAnalysisStudy(String androidJar)
	{
		G.reset();
        
        String[] args3 = {
			"-cp", androidJar,
			"-model", GlobalRef.coalModelPath,
			"-input", GlobalRef.WORKSPACE
		};
		
        edu.psu.cse.siis.coal.Main.main(args3);
		
		JimpleBasedInterproceduralCFG icfg = AnalysisParameters.v().getIcfg();
		
		for (Result result : Results.getResults()) 
		{
			for (Map.Entry<Unit, Map<Integer, Object>> entry : result.getResults().entrySet()) 
			{
				Stmt key = (Stmt) entry.getKey();
				Map<Integer, Object> value = entry.getValue();

				try
				{
					//This method may throws a NullPointerException
					String belongingClsName = icfg.getMethodOf(key).getDeclaringClass().getName();
					
					if (appClasses.contains(belongingClsName))
					{
						OneApp oa = appStudyResults.get(currentAppName);
						oa.droidRAIdentifiedNum++;
						
						//Normally, the size of entry.getValue() should equal to one
					   	for (Map.Entry<Integer, Object> entry2 : value.entrySet()) 
					   	{
					   		Object obj = entry2.getValue();
					   		
					   		if (obj instanceof PropagationValue)
					   		{
					   			PropagationValue pv = (PropagationValue) obj;
				   				Set<PathValue> pathValues = pv.getPathValues();
				   				
				   				for (PathValue pathValue : pathValues)
				   				{
				   					String v = pathValue.toString();
				   					if (!v.contains("(.*)") && 
						   				!v.equals("top") &&
						   				!v.equals("null") &&
						   				!v.equals("NULL-CONSTANT")
						   				)
						   			{
						   				oa.droidRAInspectedNum++;
						   				break;
						   			}
				   				}
					   		}
					   		else
					   		{
					   			String v = obj.toString();
					   			
					   			if (!v.contains("(.*)") && 
					   				!v.equals("top") &&
					   				!v.equals("null") && 
					   				!v.equals("NULL-CONSTANT")
						   			)
					   			{
					   				oa.droidRAInspectedNum++;
					   			}
					   		}
					   		
					   		//if contain at least one str, we take it as inspected reflection method
					   	}
					   	
					   	appStudyResults.put(currentAppName, oa);
					}
				}
				catch (Exception ex)
				{
					
				}
			}
		}
	}
	
	public static void studyOneApp(String apkPath, String androidJar)
	{
		G.reset();
		
		RetargetWithDummyMainGenerator.retargetWithDummyMainGeneration(apkPath, androidJar, GlobalRef.WORKSPACE);
		
		String[] args2 =
        {
            "-process-dir", GlobalRef.WORKSPACE,
            "-force-android-jar", androidJar,
            "-ire",
            "-pp",
            "-allow-phantom-refs",
            "-w",
			//"-p", "cg", "enabled:true",
			"-p", "cg.spark", "enabled:true",
			"-p", "jop.cpf", "enabled:true"
        };
		
		G.reset();

		Options.v().set_src_prec(Options.src_prec_class);
		Options.v().set_output_format(Options.output_format_none);
			
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.ReflectionMethodUsageStudy", new ReflectionMethodsUsageStudy()));
		
        soot.Main.main(args2);
        
        runReflectionAnalysisStudy(androidJar);
	}
	
	
	
	/**
	 * Considering only Android application code, but without considering the android.support.* library.
	 */
	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) 
	{
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		
		for (Iterator<SootClass> iter = sootClasses.iterator(); iter.hasNext();)
		{
			SootClass sc = iter.next();
			
			if (sc.getName().startsWith("android.support"))
			{
				continue;
			}
			
			appClasses.add(sc.getName());
			
			for (SootMethod sm : sc.getMethods())
			{
				try
				{
					Body body = sm.retrieveActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					
					for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
					{
						Stmt stmt = (Stmt) iterU.next();
						
						if (stmt.containsInvokeExpr())
						{
							String invokeMethodSignature = stmt.getInvokeExpr().getMethod().getSignature();
							
							if (reflectionMethods.contains(invokeMethodSignature))
							{
								OneApp oa = appStudyResults.get(currentAppName);
								if (null == oa)
								{
									oa = new OneApp();
									oa.appName = currentAppName;
								}
								
								oa.totalNum++;
								appStudyResults.put(currentAppName, oa);
								
								System.out.println("---->" + sm.getSignature() + "/" + invokeMethodSignature);
							}
						}
					}
				}
				catch (Exception ex)
				{
					//System.out.println("No body for method " + sm.getSignature());
				}
			}
		}
		
		Set<String> analyzedMethods = new HashSet<String>();
		
		CallGraph cg = Scene.v().getCallGraph();
		
		OneApp oa = appStudyResults.get(currentAppName);
		if (null == oa)
		{
			oa = new OneApp();
			oa.appName = currentAppName;
		}
		
		oa.cgSize = cg.size();
		appStudyResults.put(currentAppName, oa);
		
		for (Iterator<Edge> iter = cg.iterator(); iter.hasNext(); )
		{
			Edge e = (Edge) iter.next();
			
			SootMethod[] smArray = new SootMethod[] {e.src(), e.tgt()};
			
			for (SootMethod sm : smArray)
				if (appClasses.contains(sm.getDeclaringClass().getName()))
				{
					if (sm.getDeclaringClass().getName().startsWith("android.support"))
					{
						continue;
					}
					
					if (! analyzedMethods.contains(sm.getSignature()))
					{
						analyzedMethods.add(sm.getSignature());
						
						try
						{
							Body body = sm.retrieveActiveBody();
							PatchingChain<Unit> units = body.getUnits();
							
							for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
							{
								Stmt stmt = (Stmt) iterU.next();
								
								if (stmt.containsInvokeExpr())
								{
									String invokeMethodSignature = stmt.getInvokeExpr().getMethod().getSignature();
									
									if (reflectionMethods.contains(invokeMethodSignature))
									{
										oa = appStudyResults.get(currentAppName);
										oa.dummyMainReachableNum++;
										appStudyResults.put(currentAppName, oa);
										
										System.out.println("****>" + sm.getSignature() + "/" + invokeMethodSignature);
									}
								}
							}
						}
						catch (Exception ex)
						{
							//System.out.println("No body for method " + sm.getSignature());
						}
					}
				}
			
			//Methods belong to application class but excluding one that starts with "android.support"
			//Each Method need to be considered only once.
		}
		
		//System.exit(0);
	}
	
	static class OneApp
	{
		public String appName;
		public int totalNum;
		public int dummyMainReachableNum;
		public int droidRAIdentifiedNum;
		public int droidRAInspectedNum; //only considering itmes that the string are successfully extracted.
		public int cgSize = 0;
		
		@Override
		public String toString() {
			return appName + "," + totalNum + "," + dummyMainReachableNum + "," + droidRAIdentifiedNum + "," + droidRAInspectedNum + "," + cgSize;
			
			/*
			return "[appName=" + appName + ", totalNum=" + totalNum
					+ ", dummyMainReachableNum=" + dummyMainReachableNum
					+ ", droidRAIdentifiedNum=" + droidRAIdentifiedNum
					+ ", droidRAInspectedNum=" + droidRAInspectedNum
					+ ", cgSize=" + cgSize + "]";
					*/
		}
	}
}
