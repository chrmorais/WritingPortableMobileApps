package br.com.epx.photor;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;

public class HiddenModel extends Model {
    public final String TAG = "photor.hidden";
    private boolean started = false;
    private WebView wv;

    @SuppressLint("SetJavaScriptEnabled")
    public HiddenModel(final photorActivity observer) {
        super(observer);
        wv = new WebView(observer);
        wv.getSettings().setJavaScriptEnabled(true);
        // Insert this object in Javascript scope under name 'gateway'
        wv.addJavascriptInterface(this, "gateway");
        // Make sure this invisible component can't get focus
        wv.setFocusable(false);
        // Divert Javascript console messages
        wv.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(TAG, cm.message() +
                        " line " + cm.lineNumber() + " " + cm.sourceId() );
                return true;
            }
        });
        // Start Model when Javascript code has been loaded
        wv.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "hidden webview loaded");
                observer.model_ready_cb();
            }
        });
        wv.loadUrl("file:///android_asset/index.html");

        started = false;
    }

    public void reset() {
        if (! started) {
            started = true;
            start();
        }
    }

    public void start() {
        exec("model_init()"); // no params
    }

    private void exec(String f) {
        // Can't return except by call back
        wv.loadUrl("javascript:" + f + ";");
    }

    public String retrieveMemory() {
        return observer.getPrefs();
    }

    public void saveMemory(String s) {
        observer.savePrefs(s);
    }

    public void result_cb(final String type, final String text, final int index)
    {
        observer.runOnUiThread(new Runnable() {
            public void run() {
                observer.result_cb(type, text, index);
            }
        });
    }

    public void model_input(String name, int pos)
    {
        exec("model_input(\"" + name + "\", " + pos + ")");
    }

    public void model_sample_picture(String iso, String aperture, String shutter)
    {
        exec("model_sample_picture(\"" + iso + "\", \"" + aperture + "\", \"" + shutter + "\")");
    }

    public void model_list(String name) {
        exec("model_list(\"" + name +"\")");
    }

    public void model_list_cb(final String name, final String slist)
    {
        Object[] ret = new Object[3];
        ret[0] = new String[0];
        ret[1] = new Double[0];
        ret[2] = Integer.valueOf(0);

        if (slist == null) {
            Log.w(TAG, "List JSON null");
            return;
        }

        try {
            JSONArray jlist = new JSONArray(slist);
            JSONArray strings = jlist.getJSONArray(1);
            JSONArray values = jlist.getJSONArray(0);

            String[] xcoded_strings;
            Double[] xcoded_values;

            ret[1] = xcoded_strings = new String[(int) strings.length()];
            ret[0] = xcoded_values = new Double[(int) values.length()];

            for (int i = 0; i < strings.length(); ++i) {
                xcoded_strings[i] = strings.getString(i);
            }
            for (int i = 0; i < values.length(); ++i) {
                xcoded_values[i] = values.getDouble(i);
            }

            ret[2] = jlist.getInt(2);
        } catch (JSONException e) {
            Log.w(TAG, "List JSON invalid " + slist);
            return;
        }

        final Object[] fret = ret;
        
        observer.runOnUiThread(new Runnable() {
            public void run() {
                observer.model_list_cb(name, fret);
            }
        });
    }
}
