package io.github.wzhijiang.android.webview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.wzhijiang.android.surface.MotionEventWrapper;

public class GHWebView extends WebView {

    private static final boolean DEBUG = true;

    private WebLayout mWebLayout;
    private MotionEventWrapper mEventWrapper;

    public GHWebView(@NonNull Context context) {
        super(context);
    }

    public GHWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GHWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GHWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void setWebLayout(WebLayout layout) {
        mWebLayout = layout;
    }

    public WebLayout getWebLayout() {
        return mWebLayout;
    }

    public void init() {
        mEventWrapper = new MotionEventWrapper();

        // Enable hardware acceleration so that the webview could play video
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            Context context = getContext();
            if (context instanceof Activity) {
                Window window = ((Activity) context).getWindow();
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        initSettings();

        setWebViewClient(mWebViewClient);

        Log.i(BuildConfig.LOG_TAG, "webview inited");
    }

    private void initSettings() {
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAppCacheEnabled(true);

        // LOAD_CACHE_ELSE_NETWORK
        //   - When we enable this setting, the website acquires cached IP Address even we switch
        //     the network to vpn.
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        settings.setDefaultTextEncodingName("UTF-8");

        // 自适应屏幕
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // 自动缩放
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
    }

    public void setSurface(Surface surface) {
        mWebLayout.setSurface(surface);
    }

    @Override
    public void invalidate() {
        // Notify the WebLayout to draw surface
        mWebLayout.invalidate();

        super.invalidate();

        if (DEBUG) {
            Log.v(BuildConfig.LOG_TAG, "webview invalidate");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    public boolean dispatchTouchEvent(int x, int y, int action) {
        MotionEvent ev = mEventWrapper.genTouchEvent(x, y, action);

        if (DEBUG) {
            Log.d(BuildConfig.LOG_TAG, "touched: " + ev.toString());
        }

        return super.dispatchTouchEvent(ev);
    }

    public void resize(int width, int height) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mWebLayout.getLayoutParams();
        params.height = height;
        params.width = width;
        mWebLayout.setLayoutParams(params);
    }

    public WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final String urlString = request.getUrl().toString();
            if (urlString == null || urlString.startsWith("http://") || urlString.startsWith("https://")) {
                return false;
            }

            return true;
        }
    };

    public static GHWebView create(Context context, int width, int height) {
        LayoutInflater inflater = LayoutInflater.from(context);
        WebLayout layout = (WebLayout)inflater.inflate(R.layout.web_layout, null, false);

        GHWebView webView = layout.findViewById(R.id.web_view);
        webView.setWebLayout(layout);
        webView.init();

        return webView;
    }

    public static GHWebView create(Context context, ViewGroup parent, int width, int height, int zorder) {
        if (parent == null) {
            throw new AssertionError("parent cannot be null");
        }

        GHWebView webView = create(context, width, height);
        parent.addView(webView.getWebLayout(), zorder);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) webView.getWebLayout().getLayoutParams();
        params.height = height;
        params.width = width;
        webView.getWebLayout().setLayoutParams(params);

        return webView;
    }
}
