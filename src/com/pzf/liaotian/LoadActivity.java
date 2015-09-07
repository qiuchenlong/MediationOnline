package com.pzf.liaotian;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Window;
import android.view.WindowManager;

public class LoadActivity extends Activity {
    
    private static final int LOAD_DISPLAY_TIME = 1500;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.load);
        
        SharedPreferences settings = getSharedPreferences("global_id", 0);
        String id = settings.getString("device_id","");
        if (id.equals("")) {    	
        	
        	final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        	 
            final String tmDevice, tmSerial, tmPhone, androidId;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
         
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
            String uniqueId = deviceUuid.toString();
            Pattern p = Pattern.compile("-");  
            Matcher m = p.matcher(uniqueId);  
            String finalID = m.replaceAll("");  
            
        	SharedPreferences.Editor editor = settings.edit();
            editor.putString("device_id",finalID);
            editor.commit();
		}

        new Handler().postDelayed(new Runnable() {
            public void run() {
                /* Create an Intent that will start the Main WordPress Activity. */
                Intent mainIntent = new Intent(LoadActivity.this, MainWebViewActivity.class);
                LoadActivity.this.startActivity(mainIntent);
                LoadActivity.this.finish();
            }
        }, LOAD_DISPLAY_TIME); //1500 for release

    }
}