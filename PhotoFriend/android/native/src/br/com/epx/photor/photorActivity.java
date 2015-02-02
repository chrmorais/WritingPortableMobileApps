/* Copyright (c) 2015 Elvis Pfutzenreuter */

package br.com.epx.photor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.pm.PackageManager.NameNotFoundException;

public class photorActivity extends Activity {
    public final String TAG = "photor";

    Map<String,MySlider> rollers;
    TextView dof, evdesc;
    String near, far;
    Model model;
    Handler timer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        String app_ver;
        try {
            app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            app_ver = "1";
        }

        String msgb = getString(R.string.app_about);
        msgb = msgb.replace("@@", app_ver);

        timer = new Handler();
        final photorActivity self = this;
        
        rollers = new HashMap<String, MySlider>();

        rollers.put("aperture", (MySlider) findViewById(R.id.aperture));
        rollers.put("shutter", (MySlider) findViewById(R.id.shutter));
        rollers.put("iso", (MySlider) findViewById(R.id.iso));
        rollers.put("distance", (MySlider) findViewById(R.id.distance));
        rollers.put("ev", (MySlider) findViewById(R.id.ev));
        rollers.put("focallength", (MySlider) findViewById(R.id.focallength));
        evdesc = (TextView) findViewById(R.id.evdesc);
        dof = (TextView) findViewById(R.id.dof);
        near = far = "";

        for (String name: rollers.keySet()) {
            rollers.get(name).set_observer(this, name);
        }

        ImageButton b = (ImageButton) findViewById(R.id.shoot);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                init_photo();
            }
        });

        timer.postDelayed(new Runnable() {
            public void run() {
                // model = new RhinoModel(self);
                model = new HiddenModel(self);
            }
        }, 0);
    }

    public void model_ready_cb()
    {
        timer.postDelayed(new Runnable() {
            public void run() {
                model.reset();
                fill_lists();
            }
        }, 0);
    }

    private void fill_lists() {
        timer.postDelayed(new Runnable() {
            public void run() {
                for (String name: rollers.keySet()) {
                    model.model_list(name);
                }
            }
        }, 0);
    }
    
    public void model_list_cb(String name, Object[] items) {
        String [] labels = (String []) items[1];
        if (name.equals("shutter")) {
            labels = filter_shutter(labels);
        } else if (name.equals("iso")) {
            labels = filter_iso(labels);
        }
        rollers.get(name).set_list(labels, (Integer) items[2]);
    }

    private String [] filter_shutter(String [] orig) {
        String[] res = new String[orig.length];

        for (int i = 0; i < orig.length; ++i) {
            String s = orig[i];
            if (s.indexOf("/") >= 0) {
                s = s.substring(0, s.length() - 1);
            }
            res[i] = s;
        }

        return res;
    }

    private String [] filter_iso(String [] orig) {
        String[] res = new String[orig.length];

        for (int i = 0; i < orig.length; ++i) {
            String s = orig[i];
            if (s.indexOf("ISO ") >= 0) {
                s = s.substring(s.indexOf("ISO ") + 4);
            }
            res[i] = s;
        }

        return res;
    }

    public void control_set(String name, int pos)
    {
        model.model_input(name, pos);
    }

    public void init_photo() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("android.intent.extras.FLASH_MODE_OFF", 1);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = new File(getExternalFilesDir(null), "photor_sample.jpg");
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            File sample = new File(getExternalFilesDir(null), "photor_sample.jpg");
            ExifInterface exif;
            try {
                exif = new ExifInterface(sample.getAbsolutePath());
            } catch (IOException e) {
                return;
            }
            // Warning: at least one phone I tested reported wrong ISO 
            String got_iso = exif.getAttribute(ExifInterface.TAG_ISO);
            String got_aperture = exif.getAttribute(ExifInterface.TAG_APERTURE);
            String got_shutter = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            if (got_iso != null && got_aperture != null && got_shutter != null) {
                Log.d(TAG, "sample picture " + got_iso + " " + got_aperture + " " + got_shutter);
                model.model_sample_picture(got_iso, got_aperture, got_shutter);
            }


        }
    }

    /* Methods called by Javascript/HTML5 level */

    public String getPrefs() {
        SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
        String cookie = sp.getString("c1", "");
        // Log.d(TAG, "Got prefs " + cookie);
        return cookie;
    }

    public void savePrefs(String c) {
        // Log.d(TAG, "Saving prefs " + c);
        SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("c1", c);
        ed.commit();
    }

    public void result_cb(String type, String text, int index)
    {
        // Model convention: -2 means at the other extreme (length + 1)
        if (rollers.containsKey(type)) {
            rollers.get(type).set_pos(index);
        } else if (type.equals("near")) {
            near = text;
            dof.setText(near + " to " + far);
        } else if (type.equals("far")) {
            far = text;
            dof.setText(near + " to " + far);
        } else if (type.equals("evdesc")) {
            evdesc.setText(text);
        } else {
            Log.w(TAG, "Item " + type + " not found");
        }
    }
}
