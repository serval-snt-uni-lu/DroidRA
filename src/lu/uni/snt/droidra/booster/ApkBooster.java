package lu.uni.snt.droidra.booster;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.droidra.ClassDescription;
import lu.uni.snt.droidra.DroidRAResult;
import lu.uni.snt.droidra.GlobalRef;
import lu.uni.snt.droidra.model.ReflectionProfile;
import lu.uni.snt.droidra.model.ReflectionProfile.RClass;
import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtValue;
import lu.uni.snt.droidra.model.UniqStmt;

import org.apache.commons.io.FileUtils;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.Transform;
import soot.options.Options;

public class ApkBooster extends SceneTransformer
{
	public static void apkBooster(String input, String clsPath, String outputDir) 
	{
		try 
		{
			FileUtils.cleanDirectory(new File(outputDir));
		} 
		catch (IOException e) 
		{	
			e.printStackTrace();
		}
		
		G.reset();
		
		String[] args2 =
        {
            "-process-dir", input,
            "-d", outputDir,
            "-ire",
			"-pp",
			"-cp", clsPath,
			"-keep-line-number",
			"-allow-phantom-refs",
			"-w",
			"-p", "cg", "enabled:true"
        };
			
		if (input.endsWith(".apk"))
		{
			Options.v().set_force_android_jar(clsPath);
			Options.v().set_src_prec(Options.src_prec_apk);
			Options.v().set_output_format(Options.output_format_dex);
		}
		else
		{
			Options.v().set_src_prec(Options.src_prec_class);
			Options.v().set_output_format(Options.output_format_jimple);
		}
		
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.ApkBooster", new ApkBooster()));
		soot.Main.main(args2);
		
		G.reset();
	}
	
	private void sanitize()
	{
		Map<String, RClass> tmpRClasses = new HashMap<String, RClass>();
		
		for (Map.Entry<String, RClass> entry : ReflectionProfile.rClasses.entrySet())
		{
			String clsName = entry.getKey();
			RClass rClass = entry.getValue();
			
			if (! "(.*)".equals(clsName))
			{
				tmpRClasses.put(clsName, rClass);
			}
		}
		
		ReflectionProfile.rClasses = tmpRClasses;
		
		Map<UniqStmt, StmtValue> tmpUniqStmtKeyValues = new HashMap<UniqStmt, StmtValue>();
		
		for (Map.Entry<UniqStmt, StmtValue> entry : GlobalRef.uniqStmtKeyValues.entrySet())
		{
			UniqStmt uniqStmt = entry.getKey();
			StmtValue stmtValue = entry.getValue();
			
			Set<ClassDescription> tmpClsSet = new HashSet<ClassDescription>();
			for (ClassDescription clsDesc : stmtValue.getClsSet())
			{
				if (clsDesc.cls != null && !"(.*)".equals(clsDesc.cls))
				{
					if (clsDesc.name != null && ! "(.*)".equals(clsDesc.name))
					{
						tmpClsSet.add(clsDesc);
					}
				}
			}
			
			stmtValue.setClsSet(tmpClsSet);
			
			if (stmtValue.getClsSet().size() != 0)
				tmpUniqStmtKeyValues.put(uniqStmt, stmtValue);
		}
		
		GlobalRef.uniqStmtKeyValues = tmpUniqStmtKeyValues;
		
		for (Map.Entry<UniqStmt, StmtValue> entry : GlobalRef.uniqStmtKeyValues.entrySet())
		{
			UniqStmt uniqStmt = entry.getKey();
			StmtValue stmtValue = entry.getValue();
		
			System.out.println(uniqStmt.stmt);
			for (ClassDescription desc : stmtValue.getClsSet())
			{
				System.out.println("---->" + desc.cls + "," + desc.name);
			}
		}
	}
	
	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) 
	{
		//Remove (.*) class before bossting the application
		sanitize();
		
		Alteration.v().init();
		
		InstrumentationUtils.mockSootClasses(ReflectionProfile.rClasses);
		
		Map<UniqStmt, StmtKey> keyPairs = DroidRAResult.toStmtKeys(GlobalRef.uniqStmtKeyValues);
		GlobalRef.keyPairs = keyPairs;
		
		for (Map.Entry<UniqStmt, StmtValue> entry : GlobalRef.uniqStmtKeyValues.entrySet())
		{
			UniqStmt uniqStmt = entry.getKey();
			StmtValue stmtValue = entry.getValue();			
			
			StmtKey stmtKey = keyPairs.get(uniqStmt);
			
			IInstrumentation instr = null;
			switch (stmtValue.getType())
			{
			case FIELD_CALL:
				instr = new FieldCallInstrumentation(stmtKey, stmtValue, uniqStmt);
				break;
			case METHOD_CALL:
				instr = new MethodCallInstrumentation(stmtKey, stmtValue, uniqStmt);
				break;
			case CONSTRUCTOR_CALL:
				instr = new ConstructorCallInstrumentation(stmtKey, stmtValue, uniqStmt);
				break;
			case CLASS_NEW_INSTANCE:
				instr = new ClassNewInstanceCallInstrumentation(stmtKey, stmtValue, uniqStmt);
				break;
			default:
				instr = null;
			}
			
			if (null != instr)
			{
				instr.instrument();
			}
		}
		
		System.out.println("==>CG_SIZE: " + Scene.v().getCallGraph().size());
	}

}
