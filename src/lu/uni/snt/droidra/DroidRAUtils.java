package lu.uni.snt.droidra;

import lu.uni.snt.droidra.retarget.RetargetWithDummyMainGenerator;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class DroidRAUtils 
{
	public static void main(String[] args)
	{
		RetargetWithDummyMainGenerator.retargetWithDummyMainGeneration("/Users/li.li/Project/apktool/apks/com.for2w.appshare.apk", 
				"/Users/li.li/Project/github/android-platforms/android-18/android.jar", 
				"workspace");
		
		//DroidRAUtils.initArrayVarMap("workspace", "/Users/li.li/Project/github/android-platforms/android-18/android.jar");
	}
	
	public static void extractApkInfo(String apkPath)
	{
		GlobalRef.apkPath = apkPath;
		
		try 
		{
			ProcessManifest manifest = new ProcessManifest(apkPath);
			
			GlobalRef.pkgName = manifest.getPackageName();
			GlobalRef.apkVersionCode = manifest.getVersionCode();
			GlobalRef.apkVersionName = manifest.getVersionName();
			GlobalRef.apkMinSdkVersion = manifest.getMinSdkVersion();
			GlobalRef.apkPermissions = manifest.getPermissions();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	
	
	//public static Map<String, List<ArrayVar>> method2arrayVars = new HashMap<String, List<ArrayVar>>();
	/*
	public static void initArrayVarMap(String clsDir, String clsPath)
	{
		String[] args =
        {
            "-process-dir", clsDir,
            "-ire",
            "-pp",
            "-allow-phantom-refs",
            "-w",
            "-cp", clsPath,
			"-p", "cg", "enabled:false",
			"-p", "jop.cpf", "enabled:true"
        };
		
		G.reset();
		
		Options.v().set_src_prec(Options.src_prec_class);
		Options.v().set_output_format(Options.output_format_none);
			
        PackManager.v().getPack("jtp").add(new Transform("jtp.initArrayVarMap", new BodyTransformer() {

			@Override
			protected void internalTransform(Body b, String phaseName, Map<String, String> options) 
			{
				String methodSignature = b.getMethod().getSignature();
				
				List<ArrayVar> arrayVarSet = new ArrayList<ArrayVar>();
				
				PatchingChain<Unit> units = b.getUnits();
				for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
				{
					Stmt stmt = (Stmt) iterU.next();
					
					if (stmt instanceof AssignStmt)
					{
						AssignStmt assignStmt = (AssignStmt) stmt;
						Value rightOp = assignStmt.getRightOp();
						
						if (rightOp instanceof NewArrayExpr)
						{
							NewArrayExpr newArrayExpr = (NewArrayExpr) rightOp;
							
							ArrayVar arrayVar = new ArrayVar();
							arrayVar.stmt = stmt;
							arrayVar.varStr = assignStmt.getLeftOp().toString();
							arrayVar.baseType = newArrayExpr.getBaseType();
							
							try
							{
								arrayVar.length = Integer.parseInt(newArrayExpr.getSize().toString());
							}
							catch (NumberFormatException ex)
							{
								//Cannot simply obtain the length of array, which is probably transferred from other methods.
								arrayVar.length = 0;
							}
							
							
							
							arrayVarSet.add(arrayVar);
						}
					}
				}
				
				method2arrayVars.put(methodSignature, arrayVarSet);
			}
        	
        }));
		
        soot.Main.main(args);
        
        G.reset();
	}
*/
	
}
