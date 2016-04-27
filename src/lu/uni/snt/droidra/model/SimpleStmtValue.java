package lu.uni.snt.droidra.model;

import lu.uni.snt.droidra.ClassDescription;

public class SimpleStmtValue 
{
	private StmtType type = null;
	private ClassDescription clsDesc = null;
	
	public SimpleStmtValue(StmtType type, ClassDescription clsDesc)
	{
		this.type = type;
		this.clsDesc = clsDesc;
	}

	public StmtType getType() {
		return type;
	}

	public void setType(StmtType type) {
		this.type = type;
	}

	public ClassDescription getClsDesc() {
		return clsDesc;
	}

	public void setClsDesc(ClassDescription clsDesc) {
		this.clsDesc = clsDesc;
	}
}
