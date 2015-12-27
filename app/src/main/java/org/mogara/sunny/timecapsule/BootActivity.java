package org.mogara.sunny.timecapsule;

import org.mogara.sunny.timecapsule.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

public class BootActivity extends Activity {
	/** Called when the APP starts. */

	private Handler activityHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		activityHandler.postDelayed(new Runnable(){

			@Override
			public void run() {
				PackageManager manager = getPackageManager();
				PackageInfo info = null;
				try {
					info = manager.getPackageInfo(getPackageName(), 0);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
					return;
				}
				
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(BootActivity.this);
				Intent intent = null;
				
				/*long lastUpdateTime = 0;
				try{
					lastUpdateTime = settings.getLong("lastUpdateTime", 0);
				}catch(ClassCastException e){
					e.printStackTrace();
				}
				
				if(lastUpdateTime != info.lastUpdateTime){
					intent = new Intent(BootActivity.this, GuideActivity.class);
					Editor editor = settings.edit();
					editor.putLong("lastUpdateTime", info.lastUpdateTime);
					editor.commit();
				}else{
					intent = new Intent(BootActivity.this, MainActivity.class);
				}*/
                intent = new Intent(BootActivity.this, PostListActivity.class);
				
				startActivity(intent);
				finish();
			}
			
		}, 500);
	}

}
