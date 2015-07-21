package lu.uni.snt.droidra.toolkits;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class StringReflectionMethodsModelGenerator
{	
	public static void main(String[] args) throws Exception 
	{
		init();
		toStringModel(methodsWithStrParameters);
	}

	public static Set<String> methodsWithStrParameters = new HashSet<String>();
	
	public static void init()
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader("res/reflection-methods.txt"));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				String[] strs = line.split("->");
				
				if (strs[1].trim().length() > 2)
				{
					methodsWithStrParameters.add(strs[0].trim());
				}
			}
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void toStringModel(Set<String> methodsWithStrParameters)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("class dummy {" + "\n");

		for (String method : methodsWithStrParameters)
		{
			sb.append("  query " + method + " {" + "\n");
			sb.append("    0: type String;" + "\n");
			sb.append("  }" + "\n\n");
		}
		
		sb.append("}");

		System.out.println(sb.toString());
	}
}
