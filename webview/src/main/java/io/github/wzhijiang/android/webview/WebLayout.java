package io.github.wzhijiang.android.webview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

public class WebLayout extends FrameLayout {

    private static final boolean DEBUG_DRAW = true;

    private Surface mSurface;

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
            if (DEBUG_DRAW) {
                Log.v(BuildConfig.LOG_TAG, "weblayout draw");
            }

            super.draw(glAttachedCanvas);
        } else {
            super.draw(canvas);
            return;
        }

        mSurface.unlockCanvasAndPost(glAttachedCanvas);
    }
}
