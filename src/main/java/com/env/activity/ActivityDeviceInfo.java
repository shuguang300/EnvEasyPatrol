package com.env.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.env.bean.DeviceInfo;
import com.env.bean.RequestResult;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.HttpUtil;
import com.env.utils.RemoteDataHelper;
import com.env.utils.SystemMethodUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/7/17.
 */
public class ActivityDeviceInfo extends NfcActivity{

    private HashMap<String,String> hash;
    private WebView webView;
    private ProgressBar progressBar;
    private String type,value;
    private ActionBar actionBar;

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
        hash = (HashMap<String,String>)intent.getSerializableExtra("data");
        type = hash.get("Type").toString();
        value = hash.get("Id").toString();
    }

    private void initialActionBar(){
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (type=="1")actionBar.setTitle("设备信息");
        else if (type=="0") actionBar.setTitle("泵房信息");
        actionBar.show();
    }

    @Override
    public void iniView() {
        super.iniView();
        initialActionBar();
        progressBar = (ProgressBar)findViewById(R.id.progress);
        webView = (WebView)findViewById(R.id.webview);
        initialWebView();

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

        webView.addJavascriptInterface(this, "deviceInfo");
    }



    @JavascriptInterface
    public String getDataId(){
        return value;
    }

    @JavascriptInterface
    public String getDataType(){
        return type;
    }
    @JavascriptInterface
    public String getUrlPath(){
        String url = HttpUtil.URL_Datacenter+"GetDeviceInfoByDeviceId";
        return url;
    }
    @JavascriptInterface
    public void showWebInfo(String content){
        Toast.makeText(ActivityDeviceInfo.this,content,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "刷新");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == 0) {
            webView.loadUrl("javascript:getDeviceInfo()");
        }
        return super.onOptionsItemSelected(item);
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
                String msg = rs.getData();
                Gson gson = new GsonBuilder().setDateFormat(SystemMethodUtil.StandardDateTimeSdf).create();
                try{
                    TypeToken typeToken = new TypeToken<ArrayList<DeviceInfo>>(){};
                    ArrayList<DeviceInfo> deviceInfos = gson.fromJson(msg,typeToken.getType());
                    if(deviceInfos.size()>0){
                    }
                }catch (Exception ex){
                }
            }else {
                Toast.makeText(ActivityDeviceInfo.this,"获取设备信息失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
