package com.blogspot.russelldavis.sideload_enabler;

import java.io.DataOutputStream;
import java.io.IOException;

import com.blogspot.russelldavis.sideload_enabler.R;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.widget.Toast;

public class SideloadEnabler extends PreferenceActivity implements OnPreferenceChangeListener {
    
    private final static String package_name = "com.blogspot.russelldavis.sideload_enabler";
    private final static String build_apk_name = package_name + ".apk";
    private final static String activity_name = "SideloadEnabler";
    
    private String getScript() {
    	
    	String apk_path = (Integer.parseInt(Build.VERSION.SDK) >= 8) ?
    	  this.getPackageCodePath() : "/data/app/" + build_apk_name;
    	
        return
          "echo Hi" + "\n" + // arbitrary output to signal we've started
	      "ls " + apk_path + " || exit" + "\n" +
	      "mount -o rw,remount -t yaffs2 /dev/block/mtdblock3 /system" + "\n" +
	      "mkdir /system/tmp" + "\n" +
	      "cat " + apk_path + " >/system/tmp/" + build_apk_name + "\n" +
	      "pm uninstall " + package_name + "\n" +
	      "mv /system/tmp/" + build_apk_name + " /system/app/" + "\n" +
	      "rmdir /system/tmp" + "\n" +
	      "mount -o ro,remount -t yaffs2 /dev/block/mtdblock3 /system" + "\n" +
	      "launch=\"" + "am start -a android.intent.action.MAIN -n " + package_name + "/." + activity_name + "\"" + "\n" +
	      "$launch || (sleep 1; $launch) || (sleep 2; $launch) || (sleep 2; $launch)" + "\n";
    }
    
    private static Process execWithInput(String cmd, String input) throws IOException, InterruptedException {
        Process proc = Runtime.getRuntime().exec(cmd);
        DataOutputStream os = new DataOutputStream(proc.getOutputStream());
        os.writeBytes(input);
        os.close();
        return proc;
    }
    
    private void installToSystem() {
        try {
            Process proc = execWithInput("su", getScript());
            // Block until the script starts output
            if (proc.getInputStream().read() == -1) {
                Toast.makeText(this.getApplicationContext(), "Can't run without root access.", Toast.LENGTH_LONG).show();
                this.finish();
            } else {
                Toast.makeText(this.getApplicationContext(), "Restarting app, please wait...", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
        }
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        int perm = checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS);
        if (perm != PackageManager.PERMISSION_GRANTED) {
            installToSystem();
            return;
        }
        
        try {
            this.addPreferencesFromResource(R.xml.prefs);
            CheckBoxPreference pref = (CheckBoxPreference) this.findPreference("sideload");
            pref.setChecked(Settings.Secure.getInt(getContentResolver(), "install_non_market_apps") != 0);
            pref.setOnPreferenceChangeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Settings.Secure.putInt(getContentResolver(), "install_non_market_apps", (Boolean) newValue ? 1 : 0);
        return true;
    }
}