package lu.uni.snt.reflection11;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;

/**
 * @testcase_name Reflection11
 * @version 0.1
 * @author SnT, University of Luxembourg 
 * @author_mail li.li@uni.lu
 * 
 * @description A device id is extracted by the main activity and 
 *  then is sent to another activity through ICC.
 *  In the target component, InFlowActivity, a class instance is created using reflection. 
 *  Sensitive data (device id) is stored using a setter in this class, 
 *  read back using a getter and then leaked. No type information
 *  on the target class is used.
 * @dataflow onCreate: source -> Intent -> o.setImei() -> o.getImei() -> sink
 * @number_of_leaks 1
 * @challenges The analysis must be able to reflective invocations of methods without
 * 	type information on the target class.
 * 	And the analysis have to be aware of inter-component communication (ICC) of Android apps.
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId(); //source

		Intent i = new Intent(this, InFlowActivity.class);
		i.putExtra("DroidBench", imei);
		this.startActivity(i);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
