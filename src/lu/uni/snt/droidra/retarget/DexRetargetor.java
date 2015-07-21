package lu.uni.snt.droidra.retarget;

import soot.G;
import soot.options.Options;

public class DexRetargetor {

	public static void main(String[] args) {
		
		retargetDex("/Users/li.li/Project/workspace_for_coal/DroidDclA/dclStudy/target/4ADA1FE509CBEA0631E0C999E87AD595650CC7116DD61BCBE5DCEEE1F2B8DE84.apk/workspace_BKit_out/classes.dex", "res/framework-android-17.jar", "workspace");
	}

	public static void retargetDex(String dexPath, String androidJar, String outputDir) 
	{
		G.reset();
		
		String[] args2 =
        {
            "-force-android-jar", androidJar,
            "-process-dir", dexPath,
            "-d", outputDir,
            "-ire",
			"-pp",
			"-keep-line-number",
			"-allow-phantom-refs",
			"-w",
			"-p", "cg", "enabled:false",
			"-p", "wjtp.rdc", "enabled:true",
			"-src-prec", "apk"
        };
			
		Options.v().set_output_format(Options.output_format_class);
		soot.Main.main(args2);
		
		G.reset();
	}
}
