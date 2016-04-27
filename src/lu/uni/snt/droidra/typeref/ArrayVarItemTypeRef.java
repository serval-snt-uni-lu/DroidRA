package lu.uni.snt.droidra.typeref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lu.uni.snt.droidra.model.UniqStmt;
import soot.ArrayType;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.Stmt;
import soot.options.Options;


/**
 * For each invoke statement, if it contains an array argument.
 * This class statically refers the element type of a given item of each array argument.
 * 
 * For example, in the following example, we would like to now that in line 4, the objs contains two elements 
 * and the two elements' type are Integer and String, respectively. 
 * 
 * 1: Object[] objs = new Object[2];
 * 2: objs[0] = 5;
 * 3: objs[1] = "Hello World";
 * 4: invoke(objs);
 * 
 * @author li.li
 *
 */
public class ArrayVarItemTypeRef 
{
	public static Map<UniqStmt, ArrayVarValue[]> arrayTypeRef = new HashMap<UniqStmt, ArrayVarValue[]>();
	
	public static void main(String[] args)
	{
		ArrayVarItemTypeRef.setup("/Users/li.li/Project/workspace_for_coal/ReflectionTest/bin", ".");
		
		ArrayVarItemTypeRef.dump(arrayTypeRef);
	}
	
	public static void dump(Map<UniqStmt, ArrayVarValue[]> arrayTypeRef)
	{
		for (Map.Entry<UniqStmt, ArrayVarValue[]> entry : arrayTypeRef.entrySet())
		{
			UniqStmt uniqStmt = entry.getKey();
			ArrayVarValue[] avValues = entry.getValue();
			
			System.out.println("--------------");
			
			System.out.println(uniqStmt.className + ": " + uniqStmt.methodSignature + "\n" +  
					"            " + uniqStmt.stmt);
			
			for (ArrayVarValue avValue : avValues)
			{
				int length = avValue.length == -1 ? 20 : avValue.length;
				System.out.println("Array Length: " + avValue.length);
				for (int i = 0; i < length; i++)
				{
					System.out.println(avValue.types[i] + "->" + avValue.values[i]);
				}
			}
		}
	}
	
	public static void setup(String input, String clsPath)
	{
		String[] args =
        {
            "-process-dir", input,
            "-ire",
            "-pp",
            "-allow-phantom-refs",
            "-w",
            "-cp", clsPath,
			"-p", "cg", "enabled:false",
			"-p", "jop.cpf", "enabled:true"
        };
			
		G.reset();
		
		if (input.endsWith(".apk"))
		{
			Options.v().set_src_prec(Options.src_prec_apk);
			Options.v().set_force_android_jar(clsPath);
		}
		else
		{
			Options.v().set_src_prec(Options.src_prec_class);
		}
			
		Options.v().set_output_format(Options.output_format_none);
		//Options.v().set_output_format(Options.output_format_class);
		
		
        PackManager.v().getPack("jtp").add(new Transform("jtp.ArgumentTypeRef", new BodyTransformer() {

			@Override
			protected void internalTransform(Body b, String phaseName, Map<String, String> options) 
			{
				int count = 0;

				//System.out.println(b);
				
				Map<String, ArrayVarKey> arrayKeyMap = new HashMap<String, ArrayVarKey>();
				Map<ArrayVarKey, ArrayVarValue> avMap = new HashMap<ArrayVarKey, ArrayVarValue>();
				
				for (Iterator<Local> localIter = b.getLocals().snapshotIterator(); localIter.hasNext(); )
				{
					Local local = localIter.next();
					//System.out.println(local.getType());
					
					Type t = local.getType();
					if (t instanceof ArrayType)
					{
						ArrayType at = (ArrayType) t;
						
						ArrayVarKey avKey = new ArrayVarKey();
						avKey.key = local.toString();
						avKey.baseType = at.getElementType().toString();
						
						ArrayVarValue avValue = new ArrayVarValue();
						
						arrayKeyMap.put(avKey.key, avKey);
						avMap.put(avKey, avValue);
					}	
				}
				
				PatchingChain<Unit> units = b.getUnits();
				for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
				{
					Stmt stmt = (Stmt) iterU.next();
					count++;
					
					if (stmt instanceof AssignStmt)
					{
						AssignStmt assignStmt = (AssignStmt) stmt;
						
						Value leftV = assignStmt.getLeftOp();
						Value rightV = assignStmt.getRightOp();
						
						if (rightV instanceof NewArrayExpr)
						{
							NewArrayExpr naExpr = (NewArrayExpr) rightV;
							int size = -1;
							try
							{
								size = Integer.parseInt(naExpr.getSize().toString());
							}
							catch (Exception ex)
							{
								//The size is unknown, try 10 here
								size = 10;
							}
							
							String key = leftV.toString();
							
							ArrayVarKey avKey = arrayKeyMap.get(key);
							ArrayVarValue avValue = avMap.get(avKey);
							avKey.length = avValue.length = size;
							
							if (size > avValue.types.length)
							{
								avValue.types = new String[size+10];
								avValue.values = new String[size+10];
							}
							
							arrayKeyMap.put(avKey.key, avKey);
			        		avMap.put(avKey, avValue);
						}
						
						
						if (leftV instanceof ArrayRef)
						{
							ArrayRef ar = (ArrayRef) leftV;
							
							int index = -1;
							try
							{
								index = Integer.parseInt(ar.getIndex().toString());
							}
							catch (Exception ex)
							{
								//The index is unknown, cannot be extracted through simple static analysis
								index = -1;
							}
							
							if (-1 != index)
							{
								String key = ar.getBase().toString();
				        		
				        		ArrayVarKey avKey = arrayKeyMap.get(key);
				        		ArrayVarValue avValue = avMap.get(avKey);
				        		
				        		
				        		
				        		//System.out.println("DEBUG:"+ stmt + "," + rightV + "," + index + "," + avValue.values.length);
				        		
				        		avValue.values[index] = rightV.toString();
				        		avValue.types[index] = rightV.getType().toString();
				        		
				        		arrayKeyMap.put(avKey.key, avKey);
				        		avMap.put(avKey, avValue);
							}
						}
					}
					
					if (stmt.containsInvokeExpr())
					{
						List<Value> args = stmt.getInvokeExpr().getArgs();
						
						List<ArrayVarValue> avValues = new ArrayList<ArrayVarValue>();
						
						boolean existArrayRef = false;
						
						for (int i = 0; i < args.size(); i++)
						{
							Value arg = args.get(i);

							ArrayVarKey avKey = arrayKeyMap.get(arg.toString());
							
							//The variable is an array ref
							if (null != avKey)
							{
								existArrayRef = true;
								
								ArrayVarValue avValue = avMap.get(avKey);
								avValues.add(avValue);
							}
						}
						
						if (existArrayRef)
						{
							UniqStmt uniqStmt = new UniqStmt();
							uniqStmt.className = b.getMethod().getDeclaringClass().getName();
							uniqStmt.methodSignature = b.getMethod().getSignature();
							uniqStmt.stmt = stmt.toString();
							uniqStmt.stmtSeq = count;
							
							arrayTypeRef.put(uniqStmt, avValues.toArray(new ArrayVarValue[] {}));
						}
					}
				}

			}
        	
        }));
		
        soot.Main.main(args);
        
        G.reset();
	}
}
