package br.com.epx.photor;

import org.mozilla.javascript.*;

import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class RhinoModel extends Model {
    Context rhino;
    Scriptable scope;
    Script main_engine;
    Script engine_patch;
    String src;
    String src_patch;
    String src_prepatch;
    Handler to;
    boolean started = false;
    
    public final String TAG = "photor";

    SparseIntArray timeout_handles;

    public RhinoModel(photorActivity observer) {
        super(observer);
        rhino = Context.enter();
        rhino.setOptimizationLevel(-1);
        to = new Handler();
        try {
            src = getResource(R.raw.photo_model);
            src_patch = getResource(R.raw.patch);
            src_prepatch = getResource(R.raw.patch_pre);
        } catch (IOException e) {
            Log.e(TAG, "Could not read Javascript engine source");
        }

        timeout_handles = new SparseIntArray();
        scope = rhino.initStandardObjects();
        Object this_js = Context.javaToJS(this, scope);
        ScriptableObject.putProperty(scope, "gateway", this_js);

        rhino.evaluateString(scope, src_prepatch, "prepatch", 1, null);
        rhino.evaluateString(scope, src, "engine", 1, null);
        rhino.evaluateString(scope, src_patch, "patch", 1, null);

        started = false;
        
        observer.model_ready_cb();
    }

    public void reset() {
        if (! started) {
            started = true;
            start();
        }
    }

    public void start() {
        Function f = (Function) scope.get("model_init", scope);
        exec(f, new Object[0]); // no params
    }
    
    private Object exec(Function f, Object [] params) {
        Object ret = null;
        
        try {
            ret = f.call(rhino, scope, scope, params);
        } catch (RhinoException e) {
            Log.e(TAG, "Exception in engine");
            Log.e(TAG, e.getMessage());
            Log.e(TAG, e.getScriptStackTrace());
        }
        
        return ret;
    }

    private void callLater(final int handle, final int ms)
    {
        final Runnable r = new Runnable() {
            public void run() {
                timeoutAlarm(handle);
            }
        };
        to.postDelayed(r, ms);        
    }

    private void timeoutAlarm(final int handle) {
        if (-2 == timeout_handles.get(handle, -2)) {
            Log.w(TAG, "Handle " + handle + " not assoc with timeout");
            return;
        }

        // test for cancellation
        int ms = timeout_handles.get(handle);
        if (ms < 0) {
            // Log.d(TAG, "Handle " + handle + " had been cancelled");
            timeout_handles.delete(handle);
            return;
        }

        // Log.d(TAG, "Handle " + handle + " cb");

        Function f = (Function) scope.get("timeoutCallback", scope);
        Object [] params = new Object [] {handle};
        exec(f, params);

        // reread to verify if callback cleared the interval
        ms = timeout_handles.get(handle);

        if (ms > 0) {
            // Log.d(TAG, "Rescheduling " + handle);
            callLater(handle, ms);
        } else {
            timeout_handles.delete(handle);
        }
    }

    public void JS_setTimeout(final int milisseconds, final int handle) {
        // Log.d(TAG, "setTimeout " + milisseconds + " " + handle);
        if (-2 != timeout_handles.get(handle, -2)) {
            Log.w(TAG, "timeout with handle " + handle + " already exists");
        }
        timeout_handles.put(handle, 0);
        callLater(handle, milisseconds);
    }

    public void JS_clearTimeout(int handle) {
        // Log.d(TAG, "clearTimeout " + handle);
        if (-2 != timeout_handles.get(handle, -2)) {
            timeout_handles.put(handle, -1);
        } else {
            Log.w(TAG, "Trying to clear a timeout that does not exist " + handle);
        }
    }

    public void JS_setInterval(final int milisseconds, final int handle) {
        // Log.d(TAG, "setInterval " + milisseconds + " " + handle);
        if (-2 != timeout_handles.get(handle, -2)) {
            Log.w(TAG, "timeout with handle " + handle + " already exists");
        }
        timeout_handles.put(handle, milisseconds);
        callLater(handle, milisseconds);
    }

    public void JS_clearInterval(int handle) {
        // Log.d(TAG, "clearInterval " + handle);
        if (-2 != timeout_handles.get(handle, -2)) {
            timeout_handles.put(handle, -1);
        } else {
            Log.w(TAG, "Trying to clear an interval that does not exist " + handle);
        }
    }

    public void JS_log(String txt) {
        Log.w(TAG, "log " + txt);
    }

    public void JS_printf(String txt) {
        Log.w(TAG, "print " + txt);
    }

    public void JS_alert(String text) {
        Toast.makeText(observer, "Alert JS:" + text, Toast.LENGTH_SHORT).show();
        Log.w(TAG, "Alert: " + text);
    }

    public String retrieveMemory() {
        return observer.getPrefs();
    }

    public void saveMemory(String s) {
        observer.savePrefs(s);
    }
    
    public void result_cb(String type, String text, int index)
    {
        observer.result_cb(type, text, index);
    }
    
    public void model_input(String name, int pos)
    {
        Function f = (Function) scope.get("model_input", scope);
        exec(f, new Object[] { name, "" + pos });
    }
    
    public void model_sample_picture(String iso, String aperture, String shutter)
    {
        Function f = (Function) scope.get("model_sample_picture", scope);
        exec(f, new Object[] { iso, aperture, shutter });
    }

    public void model_list(String name) {
        Function f = (Function) scope.get("model_list", scope);
        NativeArray r = (NativeArray) exec(f, new Object[] { name });

        NativeArray strings = (NativeArray) r.get(1, scope);
        NativeArray values = (NativeArray) r.get(0, scope);

        String[] xcoded_strings;
        Double[] xcoded_values;
        
        Object[] ret = new Object[3];
        ret[1] = xcoded_strings = new String[(int) strings.getLength()];
        ret[0] = xcoded_values = new Double[(int) values.getLength()];
        
        for (Object i: strings.getIds()) {
            int ii = (Integer) i;
            xcoded_strings[ii] = (String) ((NativeJavaObject) strings.get(ii, scope)).unwrap();
        }

        for (Object i: values.getIds()) {
            int ii = (Integer) i;
            xcoded_values[ii] = (Double) ((NativeJavaObject) values.get(ii, scope)).unwrap();
        }
        
        ret[2] = (Integer) ((NativeJavaObject) r.get(2, scope)).unwrap();
        
        observer.model_list_cb(name, ret);
    }

    private String getResource(int id) throws IOException {
        InputStream is = observer.getResources().openRawResource(id);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        byte[] readBuffer = new byte[1024];

        try {
            int read;
            do {
                read = is.read(readBuffer, 0, readBuffer.length);
                if(read == -1) {
                    break;
                }
                bout.write(readBuffer, 0, read);
            } while(true);

            return new String(bout.toByteArray(), "UTF-8");
        } finally {
            is.close();
        }
    }
}
