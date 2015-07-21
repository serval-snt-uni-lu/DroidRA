package lu.uni.snt.droidra.toolkits.cg;

import java.util.HashSet;
import java.util.Set;

public class UniqEdge 
{
	public String srcMethodSig;
	public String tgtMethodSig;
	
	public static String getClass(String methodSig)
	{
		int endPos = methodSig.indexOf(":");
		
		return methodSig.substring(1, endPos);
	}
	
	public static String getMethodSubSig(String methodSig)
	{
		String tmpStr = methodSig.split(":")[1].trim();
		
		return tmpStr.substring(0, tmpStr.length()-1);
	}
	
	public static void main(String[] args)
	{
		String methodSig = "<com.admob.android.ads.AdManager: void clientError(java.lang.String)>";
		
		System.out.println(getClass(methodSig));
		System.out.println(getMethodSubSig(methodSig));

		UniqEdge e1 = new UniqEdge();
		e1.srcMethodSig = "<com.admob.android.ads.AdManager: void src(java.lang.String)>";
		e1.tgtMethodSig = "<com.admob.android.ads.AdManager: void clientError(java.lang.String)>";
		
		UniqEdge e2 = new UniqEdge();
		e2.srcMethodSig = "<com.admob.android.ads.AdManager: void src(java.lang.String)>";
		e2.tgtMethodSig = "<com.admob.android.ads.AdManager: void clientError(java.lang.String)>";
		
		if (e1.equals(e2))
		{
			System.out.println("e1 == e2");
		}
		
		Set<UniqEdge> edges = new HashSet<UniqEdge>();
		edges.add(e1);
		edges.add(e2);
		
		System.out.println(edges.size());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UniqEdge other = (UniqEdge) obj;
		if (srcMethodSig == null) {
			if (other.srcMethodSig != null)
				return false;
		} else if (!srcMethodSig.equals(other.srcMethodSig))
			return false;
		if (tgtMethodSig == null) {
			if (other.tgtMethodSig != null)
				return false;
		} else if (!tgtMethodSig.equals(other.tgtMethodSig))
			return false;
		return true;
	}

	@Override
	public String toString() 
	{
		return srcMethodSig + "->" + tgtMethodSig;
	}
	
	@Override
	public int hashCode() 
	{
		return toString().hashCode();
	}
}
