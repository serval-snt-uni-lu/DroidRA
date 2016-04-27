package lu.uni.snt.droidra;

import java.util.Map;
import java.util.Set;

import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtValue;
import lu.uni.snt.droidra.model.UniqStmt;
import lu.uni.snt.droidra.model.ReflectionProfile.RClass;
import lu.uni.snt.droidra.typeref.ArrayVarValue;

public class GlobalRef 
{
	public static String apkPath;
	public static String pkgName;
	public static String apkVersionName;
	public static int apkVersionCode = -1;
	public static int apkMinSdkVersion;
	public static Set<String> apkPermissions;
	
	public static String clsPath;
	
	//Configuration files
	public static final String WORKSPACE = "workspace";
	public static String fieldCallsConfigPath = "res/FieldCalls.txt";
	//public static String coalModelPath = "res/reflection.model";
	public static String coalModelPath = "res/reflection_simple.model";
	public static String rfModelPath = "res/reflection.model";
	public static String dclModelPath = "res/dynamic_code_loading.model";
	
	public static Map<UniqStmt, StmtValue> uniqStmtKeyValues;
	public static Map<String, RClass> rClasses;
	public static Map<UniqStmt, ArrayVarValue[]> arrayTypeRef;
	public static Map<UniqStmt, StmtKey> keyPairs;
	
	
	public static final String jsonFile = "refl.json";
}
