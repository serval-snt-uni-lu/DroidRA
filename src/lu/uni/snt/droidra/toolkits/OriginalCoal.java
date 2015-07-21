package lu.uni.snt.droidra.toolkits;

import lu.uni.snt.droidra.DroidRAUtils;
import lu.uni.snt.droidra.GlobalRef;
import edu.psu.cse.siis.coal.Main;

public class OriginalCoal {

	public static void main(String[] args) {
		//String apkPath = "/Users/li.li/Project/gitbitbucket/IccLeaks/all/00621E015191863041E78726B863B7E1374B17FDA690367878D1272B0E44B232.apk";
		String apkPath = "testapps/Reflection10.apk";
		//String apkPath = "/Users/li.li/Project/apktool/apks/mobi.infolife.iShopping.apk";
		String androidJars = "/Users/li.li/Project/github/android-platforms";
		
		DroidRAUtils.extractApkInfo(apkPath);
		String forceAndroidJar = androidJars + "/android-18/android.jar";
		GlobalRef.clsPath = forceAndroidJar;
		
		//Step (1): Retarget
		//RetargetWithDummyMainGenerator.retargetWithDummyMainGeneration(apkPath, forceAndroidJar, GlobalRef.WORKSPACE);
		
		
		String[] args2 = {
			"-cp", forceAndroidJar,
			"-model", GlobalRef.coalModelPath,
			"-input", GlobalRef.WORKSPACE
		};

		/*
		String[] args2 = {
				"-cp", ".",
				"-model", "res/full-test.model",
				"-input", "/Users/li.li/Project/workspace_for_coal/ReflectionTest/bin"
			};
		GlobalRef.apkPath = "/Users/li.li/Project/workspace_for_coal/ReflectionTest/bin";
		GlobalRef.clsPath = ".";	
			
		*/
		args = args2;
		
		Main.main(args2);
	}

	
}
