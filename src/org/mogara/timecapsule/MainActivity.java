package org.mogara.timecapsule;

import java.io.File;

import org.mogara.timecapsule.core.Config;
import org.mogara.timecapsule.model.Capsule;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final int ADD_CAPSULE_CODE = 1;
	private static final int TAKE_OUT_CODE = 2;
	private Capsule capsule = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button addCapsuleButton = (Button) findViewById(R.id.add_capsule_button);
		addCapsuleButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				capsule = new Capsule();
				capsule.mkdirs();
				Uri fileUri = Uri.fromFile(new File(capsule.getRawImage()));
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				startActivityForResult(intent, ADD_CAPSULE_CODE);
			}
		});

		Button takeOutButton = (Button) findViewById(R.id.take_out_button);
		takeOutButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Uri fileUri = Uri.fromFile(new File(Config.STORAGE_PATH + "/snapshot"));
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				startActivityForResult(intent, TAKE_OUT_CODE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

		if (requestCode == ADD_CAPSULE_CODE) {
			Thread thread = new Thread() {
				public void run() {
					if (capsule != null)
						capsule.getKDFeaturePoint();
				}
			};
			thread.start();
		} else if (requestCode == TAKE_OUT_CODE) {
			Thread thread = new Thread() {
				public void run() {
					File file = new File(Config.STORAGE_PATH + "/snapshot");
					if (file.exists()) {
						Bitmap bitmap = BitmapFactory.decodeFile(Config.STORAGE_PATH + "/snapshot");
						String capsuleId = Capsule.find(bitmap);
						if (capsuleId != null) {
							Log.i("result", capsuleId);
						} else {
							Log.i("result", "none");
						}
					}
				}
			};
			thread.start();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}
