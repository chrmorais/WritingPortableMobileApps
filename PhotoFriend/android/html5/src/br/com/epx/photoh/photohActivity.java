/* Copyright (c) 2015 Elvis Pfutzenreuter */

package br.com.epx.photoh;

import android.app.Activity;
import android.content.DialogInterface;
import android.webkit.*;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.pm.PackageManager.NameNotFoundException;

public class photohActivity extends Activity {
    boolean fullscreen_enabled;
    boolean wakelock_enabled;
    AlertDialog.Builder alt_bld;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void wakelock(boolean new_value)
    {
        wakelock_enabled = new_value;
        findViewById(R.id.webview).setKeepScreenOn(wakelock_enabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.fullscreen0:
            fullscreen_enabled = false;
            update_fullscreen_win();
            update_fullscreen_html((WebView) findViewById(R.id.webview));
            break;
        case R.id.fullscreen1:
            fullscreen_enabled = true;
            update_fullscreen_win();
            update_fullscreen_html((WebView) findViewById(R.id.webview));
            break;
        case R.id.sleep1:
            wakelock(true);
            break;
        case R.id.sleep0:
            wakelock(false);
            break;
        case R.id.about:
            AlertDialog d = alt_bld.create();
            d.show();
            break;
        }
        SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("fullscreen", fullscreen_enabled);
        ed.putBoolean("wakelock_enabled", wakelock_enabled);
        ed.commit();
        return true;
    }

    public void update_fullscreen_win()
    {
        if (fullscreen_enabled) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(0,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.fullscreen0).setVisible(fullscreen_enabled);
        menu.findItem(R.id.fullscreen1).setVisible(!fullscreen_enabled);
        menu.findItem(R.id.sleep1).setVisible(!wakelock_enabled);
        menu.findItem(R.id.sleep0).setVisible(wakelock_enabled);
        return true;
    }

    public void update_fullscreen_html(WebView webview)
    {
        webview.loadUrl("file:///android_asset/indexa.html");
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
        fullscreen_enabled = sp.getBoolean("fullscreen", false);
        wakelock_enabled = sp.getBoolean("wakelock_enabled", false);

        update_fullscreen_win();

        setContentView(R.layout.main);

        String app_ver;
        try {
            app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            app_ver = "1";
        }

        String msgb = getString(R.string.app_about);
        msgb = msgb.replace("@@", app_ver);

        alt_bld = new AlertDialog.Builder(this);
        alt_bld.setTitle("About");
        alt_bld.setMessage(msgb);
        alt_bld.setNeutralButton(R.string.back, 
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }
                );

        WebView webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setSupportZoom(true);
        if (android.os.Build.VERSION.SDK_INT >= 18) { // Jelly Bean MR2
            webview.getSettings().setLoadWithOverviewMode(true);
        }

        // we use the Activity itself as the gateway object,
        // that Javascript can see and call methods
        webview.addJavascriptInterface(this, "Gateway");

        webview.setFocusable(false);
        webview.setFocusableInTouchMode(false);
        webview.setVerticalScrollBarEnabled(false);
        webview.setHorizontalScrollBarEnabled(false);
        webview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        this.update_fullscreen_html(webview);
        wakelock(wakelock_enabled);
    }

    /* Methods called by Javascript/HTML5 level */

    public String getPrefs() {
        SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
        String cookie = sp.getString("c1", "");
        return cookie;
    }

    public void alertJS(String text) {
        Toast.makeText(this, "Alert JS:" + text, Toast.LENGTH_SHORT).show();
    }

    public void savePrefs(String c) {
        SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("c1", c);
        ed.commit();
    }

    public void callHTML5()
    {
        /* How to call a Javascript-level function */
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.loadUrl("javascript:Gateway.alertJS('bla');");
    }
}
