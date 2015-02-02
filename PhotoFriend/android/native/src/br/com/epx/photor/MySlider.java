package br.com.epx.photor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class MySlider extends View {
    private static final String TAG = "MySlider";
    private Paint mFontPaint;
    private Paint mLinePaint;
    private Paint mBgPaint, mBgPaint2;
    Rect mLiner;
    private GestureDetector mDetector;
    ValueAnimator animation;
    Rect itemBounds[];

    private String [] items = {"x"};
    int pos = 0;
    double gpos = 0;
    double gstep;
    photorActivity observer;
    String observer_name;

    private int w = 320;
    private int h = 40;

    public void setup()
    {
        update();

        class mListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }
        }
        mDetector = new GestureDetector(MySlider.this.getContext(), new mListener());        
    }

    public MySlider(Context context) {
        super(context);
        setup();
    }

    /**
     * @param context
     * @param attrs
     */
    public MySlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public void set_observer(photorActivity obs, String name)
    {
        this.observer = obs;
        this.observer_name = name;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mDetector.onTouchEvent(event);
        if (result) {
            double screenX = event.getX();
            double screenY = event.getY();
            double viewX = screenX - getLeft();
            double viewY = screenY - getTop();
            if (viewX < (w / 2)) {
                pos = Math.max(0, pos - 1);
            } else {
                pos = Math.min(pos + 1, items.length - 1);
            }
            if (observer != null) {
                observer.control_set(observer_name, pos);
            }
            do_animate();
            performClick();
        }
        return true;
    }

    private void do_animate()
    {
        if (animation != null) {
            animation.removeAllListeners();
            animation.removeAllUpdateListeners();
            animation = null;
        }
        
        if (gpos == pos) {
            return;
        }
        
        final int final_pos = pos;
        animation = ValueAnimator.ofFloat((float) gpos, (float) pos);
        animation.setDuration(500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                gpos = (Float) animation.getAnimatedValue();
                invalidate();
            }       
        });
        animation.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {      
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                gpos = final_pos;
                invalidate();
            }
            
            @Override
            public void onAnimationRepeat(Animator animation) {   
            }
            
            public void onAnimationStart(Animator animation) {
            }
        });
        animation.start();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.w = w;
        this.h = h;
        update();
    }
    
    public void set_list(String[] items, int idx)
    {
        this.items = items;
        this.pos = idx;
        update();
    }
    
    public void set_pos(int pos)
    {
        if (pos == -2) {
            // Model convention
            pos = this.items.length;
        }
        if (pos == -1 && gpos == pos) {
            gpos -= 0.25;
        }
        if (pos == this.items.length && gpos == pos) {
            gpos += 0.25;
        }
        this.pos = pos;
        do_animate();
    }

    private void update()
    {
        float fontSize = w / 15;
        mFontPaint = new Paint();
        mFontPaint.setTextSize(fontSize);
        mFontPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        mFontPaint.setStrokeWidth(2f);
        mFontPaint.setColor(Color.BLACK);
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.DKGRAY);
        mLinePaint.setStrokeWidth(2f);
        mLinePaint.setStrokeWidth(0);

        mBgPaint = new Paint();
        mBgPaint.setStrokeWidth(2f);
        mBgPaint.setShader(new LinearGradient(0, 0, w/2, 0, Color.BLACK,
                Color.WHITE, Shader.TileMode.MIRROR));
        mBgPaint2 = new Paint();
        mBgPaint2.setStrokeWidth(2f);
        mBgPaint2.setShader(new LinearGradient(w/2, 0, w, 0, Color.WHITE,
                Color.BLACK, Shader.TileMode.MIRROR));
        mLiner = new Rect(0, h - 10, w, h);
        
        itemBounds = new Rect[items.length];
        for (int i = 0; i < items.length; ++i) {
            Rect bounds = new Rect();
            mFontPaint.getTextBounds(items[i], 0, items[i].length(), bounds);
            itemBounds[i] = bounds;
        }
        
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPaint(mBgPaint);
        canvas.drawPaint(mBgPaint2);
        canvas.drawRect(mLiner, mLinePaint);
        for (int i = 0; i < items.length; ++i) {
            double wtgt = w * 0.5f;
            double woff = w * 0.25f;
            Rect bounds = itemBounds[i];
            int width = bounds.width() / 2;
            int height = bounds.height() / 2;
            double wpos = wtgt + woff * (i - gpos) - width;
            canvas.drawText(items[i], (float) wpos,
                    (float) h * 0.5f + height, mFontPaint);
        }
    }
}
