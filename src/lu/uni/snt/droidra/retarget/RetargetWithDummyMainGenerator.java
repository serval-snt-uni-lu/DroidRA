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
		retargetWithDummyMainGeneration(apkPath, androidJar, outputDir, null);
	}
	
	public static void retargetWithDummyMainGeneration(String apkPath, String androidJar, String outputDir, String[] additionalDexes) 
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
            "-force-android-jar", androidJar,
            "-process-dir", apkPath,
            "-d", outputDir,
            "-ire",
			"-pp",
			"-keep-line-number",
			"-allow-phantom-refs",
			"-w",
			"-p", "cg", "enabled:true",
			"-p", "wjtp.rdc", "enabled:true",
			"-src-prec", "apk"
        };
			
		Options.v().set_output_format(Options.output_format_class);
		
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
