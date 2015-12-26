package org.mogara.timecapsule;

import org.mogara.timecapsule.model.Capsule;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Thread thread = new Thread(){
			public void run(){
				Capsule capsule = new Capsule("1");
				capsule.getHotspotKDFeaturePoint();
			}
		};
		thread.start();
	}
}
