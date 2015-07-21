package lu.uni.snt.droidra.model;

public class UniqStmt 
{
	public String className;
	public String methodSignature;
	public String stmt;
	public int stmtSeq;
	
	@Override
	public String toString() {
		return "UniqStmt [className=" + className + ", methodSignature="
				+ methodSignature + ", stmt=" + stmt + ", stmtSeq=" + stmtSeq
				+ "]";
	}
}
