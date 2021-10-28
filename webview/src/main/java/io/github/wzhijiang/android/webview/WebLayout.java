package io.github.wzhijiang.android.webview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.wzhijiang.android.surface.MotionEventWrapper;

public class WebLayout extends FrameLayout {

    private static final boolean LOG_DRAW = false;
    private static final boolean LOG_TOUCH = true;

    private Surface mSurface;

    private MotionEventWrapper mEventWrapper;

    private FrameLayout mCustomViewContainer;

    public WebLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public WebLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WebLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WebLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        setWillNotDraw(false);

        mEventWrapper = new MotionEventWrapper();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mCustomViewContainer = findViewById(R.id.custom_view_container);
    }

    public FrameLayout getCustomViewContainer() {
        return mCustomViewContainer;
    }

    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mSurface == null) {
            super.draw(canvas);
            return;
        }

        Canvas glAttachedCanvas = mSurface.lockHardwareCanvas();
        if (glAttachedCanvas != null) {
            if (LOG_DRAW) {
                Log.v(BuildConfig.LOG_TAG, "weblayout draw");
            }

            super.draw(glAttachedCanvas);
        } else {
            super.draw(canvas);
            return;
        }

        mSurface.unlockCanvasAndPost(glAttachedCanvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    public boolean dispatchTouchEvent(int x, int y, int action) {
        MotionEvent ev = mEventWrapper.genTouchEvent(x, y, action);

        if (LOG_TOUCH) {
            Log.d(BuildConfig.LOG_TAG, "WebLayout's touch event: " + ev.toString());
        }

        return super.dispatchTouchEvent(ev);
    }

    public void resize(int width, int height) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
        params.height = height;
        params.width = width;
        this.setLayoutParams(params);
    }
}
