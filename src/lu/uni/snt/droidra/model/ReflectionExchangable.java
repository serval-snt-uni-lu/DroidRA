package lu.uni.snt.droidra.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.droidra.ClassDescription;

public class ReflectionExchangable 
{
	Set<Item> items = new HashSet<Item>();
	
	public void set(Map<UniqStmt, StmtValue> uniqStmtKeyValues)
	{
		for (Map.Entry<UniqStmt, StmtValue> entry : uniqStmtKeyValues.entrySet())
		{
			Item item = new Item();
			UniqStmt key = entry.getKey();
			StmtValue value = entry.getValue();
			
			item.className = key.className;
			item.methodSignature = key.methodSignature;
			item.stmt = key.stmt;
			item.stmtSeq = key.stmtSeq;
			
			item.type = value.getType();
			item.clsSet = value.getClsSet();
			item.param0Set = value.getParam0Set();
			
			this.items.add(item);
		}
	}
	
	public Map<UniqStmt, StmtValue> get()
	{
		Map<UniqStmt, StmtValue> uniqStmtKeyValues = new HashMap<UniqStmt, StmtValue>();
		
		for (Item item : items)
		{
			UniqStmt key = new UniqStmt();
			StmtValue value = new StmtValue();
			
			key.className = item.className;
			key.methodSignature = item.methodSignature;
			key.stmt = item.stmt;
			key.stmtSeq = item.stmtSeq;
			
			value.setType(item.type);
			value.setClsSet(item.clsSet);
			value.setParam0Set(item.param0Set);
			
			uniqStmtKeyValues.put(key, value);
		}
		
		return uniqStmtKeyValues;
	}
	
	public class Item
	{
		public String className;
		public String methodSignature;
		public String stmt;
		public int stmtSeq;
		
		private StmtType type = StmtType.SIMPLE_STRING;
		private Set<ClassDescription> clsSet = null;
		private Set<String> param0Set = null;
	}
}
