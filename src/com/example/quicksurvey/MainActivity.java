package com.example.quicksurvey;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import net.sourceforge.zbar.Symbol;
import com.dm.zbar.android.scanner.*;

public class MainActivity extends Activity {
	static final String EXTRA_TOKEN = "token";
    private static final int ZBAR_SCANNER_REQUEST = 0;
    private static final int NUM_PARTS_IN_URL = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    public void useCamera(View view) {
        if (isCameraAvailable()) {
            Intent intent = new Intent(this, ZBarScannerActivity.class);
            intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
            startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
        } else {
            Toast.makeText(this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
        }
    }	
    
    public void getSurvey(View view) {
    	Intent intent = new Intent(this, SurveyActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_survey_id);
        String token = editText.getText().toString();
        intent.putExtra(EXTRA_TOKEN, token);
        startActivity(intent);
    }

    public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ZBAR_SCANNER_REQUEST && resultCode == Activity.RESULT_OK) {
            String url = data.getStringExtra(ZBarConstants.SCAN_RESULT);
            String[] parts = url.split("/");
            if (parts.length < NUM_PARTS_IN_URL || parts[2].compareTo("quicksurvey.herokuapp.com") != 0) {
                Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
            } else {
                String token = parts[4]; //http://quicksurvey.herokuapp.com/ask/<token>
                //Toast.makeText(this, "token = " + token, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, SurveyActivity.class);
                intent.putExtra(EXTRA_TOKEN, token);
                startActivity(intent);
            }
        }
    }

}
