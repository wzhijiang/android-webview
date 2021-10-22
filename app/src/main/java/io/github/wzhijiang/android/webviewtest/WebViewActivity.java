package io.github.wzhijiang.android.webviewtest;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import io.github.wzhijiang.android.webview.GHWebView;

public class WebViewActivity extends Activity {

    private GHWebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        FrameLayout rootLayout = findViewById(R.id.root_layout);
        mWebView = GHWebView.create(this, rootLayout, displayMetrics.widthPixels,
                displayMetrics.heightPixels, 0);
        mWebView.loadUrl("https://www.bilibili.com/");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mWebView != null) {
            return mWebView.dispatchTouchEvent((int) ev.getX(), (int) ev.getY(), ev.getAction());
        }
        return super.dispatchTouchEvent(ev);
    }
}
