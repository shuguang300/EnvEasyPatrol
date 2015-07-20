package com.env.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.env.bean.RequestResult;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.RemoteDataHelper;

/**
 * Created by Administrator on 2015/7/17.
 */
public class ActivityDeviceInfo extends NfcActivity implements View.OnClickListener{

    private int deviceId;
    private TextView back,title,refresh;
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deviceinfo);
        iniData();
        iniView();
    }

    @Override
    public void iniData() {
        super.iniData();
        Intent intent = getIntent();
        deviceId = intent.getIntExtra("deviceId",0);
    }

    @Override
    public void iniView() {
        super.iniView();
        back = (TextView)findViewById(R.id.back);
        title = (TextView)findViewById(R.id.title);
        refresh = (TextView)findViewById(R.id.refresh);
        webView = (WebView)findViewById(R.id.webView);
        progressBar = (ProgressBar)findViewById(R.id.progress);


        back.setOnClickListener(this);
        refresh.setOnClickListener(this);

        GetDeviceInfoByDeviceId getDeviceInfoByDeviceId = new GetDeviceInfoByDeviceId();
        getDeviceInfoByDeviceId.execute(deviceId);

//      initialWebView();


    }

    private void initialWebView(){
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDefaultTextEncodingName("utf-8");

        webView.loadUrl("file:///android_asset/html/deviceinfo.html");

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                view.loadUrl("file:///android_asset/html/errorpage.html");
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                onBackPressed();
                break;
            case R.id.refresh:
                webView.reload();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    private class GetDeviceInfoByDeviceId extends AsyncTask<Integer,Integer,RequestResult>{

        @Override
        protected RequestResult doInBackground(Integer... params) {
            RequestResult rs = RemoteDataHelper.getDeviceInfo(params[0]);
            return rs;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(RequestResult rs) {
            super.onPostExecute(rs);
            progressBar.setVisibility(View.GONE);
            if(rs.getErrorcode()==RequestResult.NO_ERROR){

            }else {
                Toast.makeText(ActivityDeviceInfo.this,"获取设备信息失败",Toast.LENGTH_SHORT).show();
            }

        }
    }

}
