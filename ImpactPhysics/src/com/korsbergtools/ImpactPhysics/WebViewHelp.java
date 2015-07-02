package com.korsbergtools.ImpactPhysics;

import com.korsbergtools.ImpactPhysics.R;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewHelp extends Activity 
{
	WebView mWebView;

	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        
        mWebView = (WebView) findViewById(R.id.webviewhelp);
        if (mWebView == null)
        {
        	System.out.println("Could not load webviewhelp");
        }
        else
        {
	        mWebView.getSettings().setJavaScriptEnabled(true);
	        //mWebView.loadUrl("http://www.google.com");
	        mWebView.loadUrl("file:///android_asset/about.html"); //this loads the browser and view the file
        }
	}	//onCreate
	
}	//WebViewHelp
