package lu.uni.snt.droidra.toolkits;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class CommonUtils 
{
	public static void writeResultToFile(String path, String content)
	{
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
		    out.print(content);
		    out.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public static Map<Integer, String> sort(Map<String, Integer> map)
	{
		TreeMap<Integer, String> sortedMap = new TreeMap<Integer, String>();
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
		    sortedMap.put(entry.getValue(), entry.getKey());
		}

		return sortedMap;
	}
	
	static class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }

	    public int compare(String a, String b) {
	    	
	    	if (a.equals(b))
	    	{
	    		return 0;
	    	}
	    	
	    	System.out.println(a + "," + b);
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } 
	    }
	}
}
