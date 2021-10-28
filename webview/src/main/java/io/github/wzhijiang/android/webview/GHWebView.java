package io.github.wzhijiang.android.webview;

import android.app.Activity;
import android.content.Context;
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
import android.webkit.WebChromeClient;
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

    private static final boolean LOG_INVALIDATE = false;

    private WebLayout mWebLayout;

    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

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
        setWebChromeClient(mWebChromeClient);

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

    @Override
    public void invalidate() {
        // Notify the WebLayout to draw surface
        mWebLayout.invalidate();

        super.invalidate();

        if (LOG_INVALIDATE) {
            Log.v(BuildConfig.LOG_TAG, "webview invalidate");
        }
    }

    public void setSurface(Surface surface) {
        mWebLayout.setSurface(surface);
    }

    public boolean dispatchTouchEvent(int x, int y, int action) {
        return mWebLayout.dispatchTouchEvent(x, y, action);
    }

    public void resize(int width, int height) {
        mWebLayout.resize(width, height);
    }

    @Override
    public boolean canGoBack() {
        FrameLayout customViewContainer = mWebLayout.getCustomViewContainer();
        if (customViewContainer != null && customViewContainer.getVisibility() == View.VISIBLE) {
            return true;
        }

        return super.canGoBack();
    }

    @Override
    public void goBack() {
        Log.e(BuildConfig.LOG_TAG, "goBack");

        FrameLayout customViewContainer = mWebLayout.getCustomViewContainer();

        // If customViewContainer is visible (fullscreen)
        if (customViewContainer != null && customViewContainer.getVisibility() == View.VISIBLE) {
            mWebChromeClient.onHideCustomView();
            return;
        }

        super.goBack();
    }

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final String urlString = request.getUrl().toString();
            if (urlString == null || urlString.startsWith("http://") || urlString.startsWith("https://")) {
                return false;
            }

            return true;
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);

            if (DEBUG) {
                Log.d(BuildConfig.LOG_TAG, "WebChromeClient onShowCustomView");
            }

            // If a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mCustomView = view;
            GHWebView.this.setVisibility(View.GONE);
            mWebLayout.getCustomViewContainer().setVisibility(View.VISIBLE);
            mWebLayout.getCustomViewContainer().addView(mCustomView);
            mCustomViewCallback = callback;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();

            if (DEBUG) {
                Log.d(BuildConfig.LOG_TAG, "WebChromeClient onHideCustomView");
            }

            if (mCustomView == null) {
                return;
            }

            GHWebView.this.setVisibility(View.VISIBLE);
            mWebLayout.getCustomViewContainer().setVisibility(View.GONE);
            mCustomView.setVisibility(View.GONE);
            mWebLayout.getCustomViewContainer().removeView(mCustomView);
            mCustomViewCallback.onCustomViewHidden();
            mCustomView = null;
        }
    };

    public static GHWebView create(Context context, int width, int height) {
        LayoutInflater inflater = LayoutInflater.from(context);

        // Note that in dependency 'android-surface.aar', there's a R.layout.web_layout.
        // Thus we should use a different layout name here.
        FrameLayout frameLayout = (FrameLayout)inflater.inflate(R.layout.gh_web_layout, null, false);

        // Cast the frame layout to WebLayout
        WebLayout layout = (WebLayout)frameLayout;

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
