package com.env.activity;
import java.util.HashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.env.nfc.NfcActivity;
import com.env.utils.HttpUtil;
import com.env.easypatrol.R;

public class ActivityEachTagWebView extends NfcActivity implements OnClickListener{
	
	private WebView webView;
	private TextView back,refresh,name;
	private HashMap<String, String> mData;
	private ProgressBar mProgressBar;
	private int mTagID;
	private int mTagType;
	private String urlPath;
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mData = (HashMap<String, String>)getIntent().getExtras().getSerializable("task");
		mTagID = Integer.valueOf(mData.get("PatrolTagID"));
		mTagType = Integer.valueOf(mData.get("ResultType"));
		setContentView(R.layout.patroltagwebview);
		mProgressBar = (ProgressBar)findViewById(R.id.patroltagwebview_progress1);
		name = (TextView)findViewById(R.id.patroltagwebview_tagname);
		refresh = (TextView)findViewById(R.id.patroltagwebview_options);
		back = (TextView)findViewById(R.id.patroltagwebview_back);
		webView = (WebView)findViewById(R.id.patroltagwebview_webview1);
		back.setOnClickListener(this);
		refresh.setOnClickListener(this);
		name.setText(mData.get("PatrolName"));
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setJavaScriptEnabled(true); 
		webView.getSettings().setBuiltInZoomControls(false); 
		webView.getSettings().setSupportZoom(false);
		webView.getSettings().setDefaultTextEncodingName("utf-8");
		urlPath = "http://"+HttpUtil.getInstance(ActivityEachTagWebView.this).getIPAddr()+"/80.htm?PatrolTagID="+mTagID+"&resulttype="+mTagType;
		webView.loadUrl(urlPath); 
		webView.setWebViewClient(new WebViewClient(){       
            public boolean shouldOverrideUrlLoading(WebView view, String url) {       
                view.loadUrl(url);       
                return super.shouldOverrideUrlLoading(view, url);       
            }
            @Override
            public void onPageFinished(WebView view, String url) {
            	super.onPageFinished(view, url);
            	mProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            	super.onPageStarted(view, url, favicon);
            	mProgressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            	super.onReceivedError(view, errorCode, description, failingUrl);
            	view.loadUrl("file:///android_asset/html/errorpage.html");
            }
		});
		webView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				mProgressBar.setProgress(newProgress);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.patroltagwebview_back) {
			finish();
		} else if (id == R.id.patroltagwebview_options) {
			webView.loadUrl(urlPath);
		}
		
	}
}
