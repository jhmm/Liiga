package com.jere.liiga;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

	public void exitBrowser(View view)
	{
		this.finish();		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
		
		String url = "http://www.liiga.fi";
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
		    url = extras.getString("url");
		}
		
		 WebView webview = (WebView) findViewById(R.id.webview);
			
			webview.setWebViewClient(new WebViewClient() {

			    @Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
			        // TODO Auto-generated method stub

			        view.loadUrl(url);
			        return true;

			    }
			 });

			 webview.loadUrl(url);
	}
}
