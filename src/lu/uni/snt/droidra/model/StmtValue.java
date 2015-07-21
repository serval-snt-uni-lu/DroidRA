package lu.uni.snt.droidra.model;

import java.util.HashSet;
import java.util.Set;

import lu.uni.snt.droidra.ClassDescription;


public class StmtValue 
{
	private StmtType type = StmtType.SIMPLE_STRING;
	
	//For class-based string analysis
	private Set<ClassDescription> clsSet = null;
	
	//For simple string analysis, e.g., SIMPLE_STRING stmt type
	private Set<String> param0Set = null;

	public StmtValue(StmtType type)
	{
		this();
		this.type = type;
	}
	
	public StmtValue()
	{
		type = StmtType.SIMPLE_STRING;
		clsSet = new HashSet<ClassDescription>();
		param0Set = new HashSet<String>();
	}

	
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append(StmtType.toStringType(type) + ":");
		if (type != StmtType.SIMPLE_STRING)
		{
			sb.append(clsSet);
		}
		else
		{
			sb.append(param0Set);
		}
		
		return sb.toString();
	}

	public StmtType getType() {
		return type;
	}

	public void setType(StmtType type) {
		this.type = type;
	}

	public Set<ClassDescription> getClsSet() {
		return clsSet;
	}

	public void setClsSet(Set<ClassDescription> clsSet) {
		this.clsSet = clsSet;
	}

	public Set<String> getParam0Set() {
		return param0Set;
	}

	public void setParam0Set(Set<String> param0Set) {
		this.param0Set = param0Set;
	}
}
