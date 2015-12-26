package org.mogara.timecapsule;

import java.io.File;

import org.mogara.timecapsule.model.Capsule;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final int ADD_CAPSULE_CODE = 1;
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
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_CAPSULE_CODE && resultCode == Activity.RESULT_OK) {
			Thread thread = new Thread() {
				public void run() {
					if (capsule != null)
						capsule.getKDFeaturePoint();
				}
			};
			thread.start();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
