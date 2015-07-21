package lu.uni.snt.droidra.toolkits;
import java.io.File;


public class StudyWrapper
{	
	public static void main(String[] args) 
	{
		String androidJars = "/Users/li.li/Project/github/android-platforms";
		String dirPath = "/Users/li.li/Project/gitbitbucket/IccLeaks/all";

		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		
		for (File file : files)
		{
			ClassLoaderStudy.main(new String[] {
				file.getAbsolutePath(),
				androidJars
			});
		}
	}
}
