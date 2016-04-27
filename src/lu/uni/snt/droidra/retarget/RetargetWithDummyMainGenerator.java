package lu.uni.snt.droidra.retarget;

import java.io.File;
import java.io.IOException;


import org.apache.commons.io.FileUtils;

import soot.G;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class RetargetWithDummyMainGenerator 
{
	public static void retargetWithDummyMainGeneration(String apkPath, String androidJar, String outputDir) 
	{
		retargetWithDummyMainGeneration(apkPath, androidJar, outputDir, null, false);
	}
	
	public static void retargetWithDummyMainGeneration(String apkPath, String androidJar, String outputDir, boolean outjar) 
	{
		retargetWithDummyMainGeneration(apkPath, androidJar, outputDir, null, true);
	}
	
	public static void retargetWithDummyMainGeneration(String apkPath, String androidJar, String outputDir, String[] additionalDexes) 
	{
		retargetWithDummyMainGeneration(apkPath, androidJar, outputDir, additionalDexes, false);
	}
	
	public static void retargetWithDummyMainGeneration(String apkPath, String androidJar, String outputDir, String[] additionalDexes, boolean outjar) 
	{
		try 
		{
			FileUtils.cleanDirectory(new File(outputDir));
		} 
		catch (IOException e) 
		{	
			e.printStackTrace();
		}
		
		String appName = apkPath;
		if (appName.contains("/"))
		{
			appName = appName.substring(appName.lastIndexOf('/')+1);
		}
		
		G.reset();
		
		String[] args2 =
        {
            "-force-android-jar", androidJar,
            "-process-dir", apkPath,
            "-ire",
			"-pp",
			"-keep-line-number",
			"-allow-phantom-refs",
			"-w",
			"-p", "cg", "enabled:true",
			"-p", "wjtp.rdc", "enabled:true",
			"-src-prec", "apk"
        };

		if (outjar)
		{
			Options.v().set_output_jar(true);
			Options.v().set_output_dir(appName + ".jar");
		}
		else
		{
			Options.v().set_output_format(Options.output_format_class);
			Options.v().set_output_dir(outputDir);
		}
		
		DummyMainGenerator dmGenerator = new DummyMainGenerator(apkPath);
		//dmGenerator.setFullMethodCover(true);
		
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.DummyMainGenerator", dmGenerator));
		soot.Main.main(args2);
		
		G.reset();
		
		if (null != additionalDexes && 0 < additionalDexes.length)
		{
			for (String dexPath : additionalDexes)
			{
				DexRetargetor.retargetDex(dexPath, androidJar, outputDir);
			}
		}
	}
}
