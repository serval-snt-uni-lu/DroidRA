package lu.uni.snt.droidra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtType;
import lu.uni.snt.droidra.model.StmtValue;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class HeuristicUnknownValueInfer 
{
	private static HeuristicUnknownValueInfer instance;
	
	public static HeuristicUnknownValueInfer getInstance()
	{
		if (null == instance)
		{
			instance = new HeuristicUnknownValueInfer();
		}
		return instance;
	}
	
	public Map<StmtKey, StmtValue> infer(Map<StmtKey, StmtValue> stmtKeyValues)
	{
		Map<StmtKey, StmtValue> newStmtKeyValues = inferThroughKnownClassNames(stmtKeyValues);
		
		return inferThroughAllClasses(newStmtKeyValues);
	}
	
	/**
	 * Since we may get a list of class names (through Class.forName), 
	 * this method traverse these methods to guess an unknown class name, giving a field/method name.
	 * 
	 * Problem: If the class is not in the current classpath, then there is no way to infer through this way.
	 * For example, reflection calls are used because of hiden class/methods.
	 * 
	 * @param stmtKeyValues
	 * @return
	 */
	public Map<StmtKey, StmtValue> inferThroughKnownClassNames(Map<StmtKey, StmtValue> stmtKeyValues)
	{
		Map<StmtKey, StmtValue> newStmtKeyValues = new HashMap<StmtKey, StmtValue>();
		
		Set<String> possibleClsNames = new HashSet<String>();
		
		for (Map.Entry<StmtKey, StmtValue> entry : stmtKeyValues.entrySet())
		{
			StmtKey key = entry.getKey();
			StmtValue value = entry.getValue();
			
			if (value.getType() == StmtType.SIMPLE_STRING)
			{
				if (key.getStmt().toString().contains("java.lang.Class forName"))
				{
					for (String str : value.getParam0Set())
					{
						if (str.contains(","))
						{
							String[] strs = str.split(",");
							for (String s : strs)
							{
								possibleClsNames.add(s.trim());
							}
						}
						else
						{
							possibleClsNames.add(str);
						}
					}
					
					
				}
			}
		}
		
		for (Map.Entry<StmtKey, StmtValue> entry : stmtKeyValues.entrySet())
		{
			StmtKey key = entry.getKey();
			StmtValue value = entry.getValue();

			Set<ClassDescription> oldSet = new HashSet<ClassDescription>();
			Set<ClassDescription> newSet = new HashSet<ClassDescription>();
			
			for (ClassDescription clsDesc : value.getClsSet())
			{
				String clsName = clsDesc.cls;
				
				if (null != clsName && clsName.equals("(.*)"))
				{
					//There is also no way to infer className for CLASS_NEW_INSTANCE/CONSTRUCTOR_CALL
					switch (value.getType())
					{
					case FIELD_CALL:
						String fieldName = clsDesc.name;

						for (String possibleClsName : possibleClsNames)
						{
							try
							{
								SootClass sc = Scene.v().getSootClass(possibleClsName);
								if (null == sc)
								{
									continue;
								}
								
								try
								{
									SootField sf = sc.getFieldByNameUnsafe(fieldName);
									if (null == sf)
									{
										continue;
									}
									
									//Class ``possibleClsName" contains a field called ``fieldName"
									oldSet.add(clsDesc);
									
									ClassDescription cd = new ClassDescription();
									cd.cls = possibleClsName;
									cd.name = fieldName;
									newSet.add(cd);
								}
								catch (Exception ex) { }
							}
							catch (Exception ex)
							{
								//There is no that clss in the Scene
							}
							
						}
							
						
						break;
					case METHOD_CALL:
						String methodName = clsDesc.name;

						for (String possibleClsName : possibleClsNames)
						{
							try
							{
								SootClass sc = Scene.v().getSootClass(possibleClsName);
								if (null == sc)
								{
									continue;
								}
								
								try
								{
									SootMethod sm = sc.getMethodByNameUnsafe(methodName);
									if (null == sm)
									{
										continue;
									}
									
									//Class ``possibleClsName" contains a field called ``fieldName"
									oldSet.add(clsDesc);
									
									ClassDescription cd = new ClassDescription();
									cd.cls = possibleClsName;
									cd.name = methodName;
									newSet.add(cd);
								}
								catch (Exception ex) { }
							}
							catch (Exception ex)
							{
								//There is no that clss in the Scene
							}
							
						}
						
						break;
					default:    //SIMPLE_STRING
						break;
					}
				}
			}
			
			value.getClsSet().removeAll(oldSet);
			value.getClsSet().addAll(newSet);
			
			newStmtKeyValues.put(key, value);
		}
		
		return newStmtKeyValues;
	}
	
	
	/**
	 * Only infer the className through its possible field/method name.
	 * This method traverses all classes in the current classpath.
	 * 
	 * Again, if the target class is not in the current classpath, this method will fail to infer the class name.
	 * 
	 * @param uniqStmtKeyValues
	 * @return
	 */
	public Map<StmtKey, StmtValue> inferThroughAllClasses(Map<StmtKey, StmtValue> stmtKeyValues)
	{
		Map<StmtKey, StmtValue> newStmtKeyValues = new HashMap<StmtKey, StmtValue>();
		
		Map<String, Set<String>> nameToClasses = new HashMap<String, Set<String>>();
		
		//(1) Fill nameToClasses mapping
		for (Map.Entry<StmtKey, StmtValue> entry : stmtKeyValues.entrySet())
		{
			StmtValue value = entry.getValue();

			for (ClassDescription clsDesc : value.getClsSet())
			{
				String clsName = clsDesc.cls;
				
				if (null != clsName && clsName.equals("(.*)"))
				{
					//There is also no way to infer className for CLASS_NEW_INSTANCE/CONSTRUCTOR_CALL
					switch (value.getType())
					{
					case FIELD_CALL:
						String fieldName = clsDesc.name;

						for (Iterator<SootClass> iter = Scene.v().getClasses().snapshotIterator(); iter.hasNext(); )
						{
							SootClass sc = iter.next();
							
							try
							{
								SootField sf = sc.getFieldByNameUnsafe(fieldName);
								if (null == sf)
								{
									continue;
								}
							}
							catch (Exception ex) { }
							
							//To differentiate field name and method name
							fieldName = "F:" + fieldName;
							
							Set<String> clses = null;
							if (nameToClasses.containsKey(fieldName))
							{
								clses = nameToClasses.get(fieldName);
							}
							else
							{
								clses = new HashSet<String>();
							}
							
							clses.add(sc.getName());
							
							nameToClasses.put(fieldName, clses);
						}
						
						break;
					case METHOD_CALL:
						String methodName = clsDesc.name;

						for (Iterator<SootClass> iter = Scene.v().getClasses().snapshotIterator(); iter.hasNext(); )
						{
							SootClass sc = iter.next();
							
							try
							{
								SootMethod sm = sc.getMethodByNameUnsafe(methodName);
								if (null == sm)
								{
									continue;
								}
							}
							catch (Exception ex) { }
							
							methodName = "M:" + methodName;
							
							Set<String> clses = null;
							if (nameToClasses.containsKey(methodName))
							{
								clses = nameToClasses.get(methodName);
							}
							else
							{
								clses = new HashSet<String>();
							}
							
							clses.add(sc.getName());
							
							nameToClasses.put(methodName, clses);
						}
						
						break;
					default:    //SIMPLE_STRING
						break;
					}
				}
			}
		}
		
		
		//(2) infer value
		Map<String, String> nameToClassMap = new HashMap<String, String>();
		while (nameToClasses.size() > 0)
		{
			Map<String, String> tmpMap = valueInfer(nameToClasses);
			nameToClassMap.putAll(tmpMap);
			for (String key : tmpMap.keySet())
			{
				nameToClasses.remove(key);
			}
		}
		
		
		//(3) write inferred value back
		for (Map.Entry<StmtKey, StmtValue> entry : stmtKeyValues.entrySet())
		{
			StmtKey key = entry.getKey();
			StmtValue value = entry.getValue();

			Set<ClassDescription> unknownClsDescSet = new HashSet<ClassDescription>();
			Set<ClassDescription> clsDescSet = new HashSet<ClassDescription>();
			
			for (ClassDescription clsDesc : value.getClsSet())
			{
				String clsName = clsDesc.cls;
				
				if (null != clsName && clsName.equals("(.*)"))
				{
					//There is also no way to infer className for CLASS_NEW_INSTANCE/CONSTRUCTOR_CALL
					switch (value.getType())
					{
					case FIELD_CALL:
						String fieldName = clsDesc.name;
						fieldName = "F:" + fieldName;
						
						if (nameToClassMap.containsKey(fieldName))
						{
							ClassDescription cd = new ClassDescription();
							cd.cls = nameToClassMap.get(fieldName);
							cd.name = fieldName.replace("F:", "");

							clsDescSet.add(cd);
						}
						
						break;
					case METHOD_CALL:
						String methodName = clsDesc.name;
						methodName = "M:" + methodName;
						
						if (nameToClassMap.containsKey(methodName))
						{
							unknownClsDescSet.add(clsDesc);
							
							ClassDescription cd = new ClassDescription();
							cd.cls = nameToClassMap.get(methodName);
							cd.name = methodName.replace("M:", "");
							clsDescSet.add(cd);
						}
						
						break;
					default:    //SIMPLE_STRING
						break;
					}
				}
			}
			
			value.getClsSet().removeAll(unknownClsDescSet);
			value.getClsSet().addAll(clsDescSet);
			
			newStmtKeyValues.put(key, value);
		}

		return newStmtKeyValues;
	}
	
	
	public Map<String, String> valueInfer(Map<String, Set<String>> nameToClasses)
	{
		Set<String> potentialClses = new HashSet<String>();
		for (Set<String> set : nameToClasses.values())
		{
			potentialClses.addAll(set);
		}
		
		String[] allClses = potentialClses.toArray(new String[] {});
		
		int[] value = new int[allClses.length];
		
		for (String key : nameToClasses.keySet())
		{
			int[] tmp = new int[allClses.length];
			
			for (int i = 0; i < allClses.length; i++)
			{
				if (nameToClasses.get(key).contains(allClses[i]))
				{
					tmp[i] = 1;
				}
			}
			
			for (int i = 0; i < value.length; i++)
			{
				value[i] = value[i] + tmp[i];
			}
		}
		
		int maxValue = -1;
		int maxSeq = 0;
		for (int i = 0; i < value.length; i++)
		{
			if (maxValue < value[i])
			{
				maxValue = value[i];
				maxSeq = i;
			}	
		}
		
		//System.out.println("The max value is " + maxValue);
		//System.out.println("The max seq is " + maxSeq);
		
		Map<String, String> nameToClassMap = new HashMap<String, String>();
		for (String key : nameToClasses.keySet())
		{
			if (nameToClasses.get(key).contains(allClses[maxSeq]))
			{
				nameToClassMap.put(key, allClses[maxSeq]);
			}
		}
		
		return nameToClassMap;
	}
	
	
	
}
