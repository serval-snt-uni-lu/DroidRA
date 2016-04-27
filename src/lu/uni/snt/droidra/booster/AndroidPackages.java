package lu.uni.snt.droidra.booster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class AndroidPackages 
{
	public static String androidPackagesConfig = "res/android-packages.txt";
	public static Set<String> androidPackages = new HashSet<String>();
	
	static
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(androidPackagesConfig));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				androidPackages.add(line);
			}
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static boolean belongTo(String clsName)
	{
		boolean belongTo = false;
		
		for (String pkg : androidPackages)
		{
			if (clsName.startsWith(pkg))
			{
				belongTo = true;
				break;
			}
		}
		
		return belongTo;
	}
	
}
