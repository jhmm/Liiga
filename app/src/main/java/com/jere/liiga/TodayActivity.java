package com.jere.liiga;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/*
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
*/
import java.net.HttpURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TodayActivity extends Activity
{
	private ArrayList<UpdatableGameView> gameViews;
	
	int updateFrequency = 30;
	int timeUntilUpdate = 0;
	
	Handler handler = new Handler();
	
	private boolean mIsInForegroundMode;
	
	
	TextView textViewTimeUntilUpdate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_today);
		
		textViewTimeUntilUpdate = (TextView) findViewById(R.id.textViewTimeUntilUpdate);
		gameViews = new ArrayList<UpdatableGameView>();
		
		updateResults();
		
		handler.post(updateTextRunnable);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		 mIsInForegroundMode = false;
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    mIsInForegroundMode = true;
	    
	    updateResults();
	}
	
	public boolean isInForeground() {
	    return mIsInForegroundMode;
	}
	
	Runnable updateTextRunnable=new Runnable(){  
		public void run() {
			
			if(isInForeground())
		    {
				if(timeUntilUpdate > 0)
				{
					timeUntilUpdate--;
				}
			
				updateTimeUntilTextView();
				if(timeUntilUpdate == 0)
				{
					updateResults();
				}
		    }
		    
		    handler.postDelayed(this, 1000);  
		    
		   /* Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
			    public void run() {
			    	updateResults();
			    }
			}, updateFrequency * 1000);*/
		}  	
	};
	
	void updateTimeUntilTextView()
	{
		textViewTimeUntilUpdate.setText(timeUntilUpdate + " sek");
	}
		


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.today, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls)
        {
            return doHttpGet(urls[0]);
        }
        
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result)
        {
        	updateView(result);
       }
    }
	
	public static String doHttpGet(String url){
        InputStream inputStream = null;
        String result = "";
        try {
            /*
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
 
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
 
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();*/

            URL urlObj = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
            inputStream = urlConnection.getInputStream();

            // convert input stream to string
            if(inputStream != null)
            {
                result = convertInputStreamToString(inputStream);
            }
 
        }
        catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
 
        return result;
	}
	
	private static String convertInputStreamToString(InputStream inputStream) throws IOException
	{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        
        while((line = bufferedReader.readLine()) != null)
        {
        	result += line;
        }
 
        inputStream.close();
        return result;
    }
	
	private void updateResults()
	{
		setUpdateMode(true);
		long unixTimestamp = System.currentTimeMillis();
		new HttpAsyncTask().execute("http://liiga.fi/media/game-tracking/today.json?_="+Long.toString(unixTimestamp));
	}
	
	private void setUpdateMode(boolean doUpdate)
	{
		LinearLayout layoutBeingUpdated = (LinearLayout) findViewById(R.id.layoutLoadingPanel);
		LinearLayout layoutTimeUntil = (LinearLayout) findViewById(R.id.layoutTimeUntilUpdate);
		
		if(doUpdate)
		{
			layoutTimeUntil.setVisibility(View.GONE);
			layoutBeingUpdated.setVisibility(View.VISIBLE);			
		}
		else
		{
			timeUntilUpdate = updateFrequency;
			updateTimeUntilTextView();
			layoutBeingUpdated.setVisibility(View.GONE);
			layoutTimeUntil.setVisibility(View.VISIBLE);
		}
	}
	
	private void updateView(String resultData)
	{
		try
		{
			// Start by parsing the response JSON and create VehicleInformation object
			// of every new vehicle and for the existing ones, update it
			JSONObject json = new JSONObject(resultData);
			
			JSONArray games = json.getJSONArray("games");

			for(int i = 0; i < games.length(); i++)
			{
				JSONObject currentGame = games.getJSONObject(i);	
				
				Game game = new Game();
				
				if(currentGame.has("id"))
				{
					game.gameId = currentGame.getInt("id");
					
					if(currentGame.has("finished"))
					{
						game.isFinished = currentGame.getBoolean("finished");
					}
					if(currentGame.has("started"))
					{
						game.isStarted = currentGame.getBoolean("started");
					}
					if(currentGame.has("status"))
					{
						game.currentStatus = currentGame.getString("status");
					}
					
					// Set teams and make sure they are available
					if(currentGame.has("home") && currentGame.has("away")
						&& currentGame.getJSONObject("home").has("name")
						&& currentGame.getJSONObject("away").has("name")
						)
					{
						// Set team names
						game.homeTeam = currentGame.getJSONObject("home").getString("name");
						game.awayTeam = currentGame.getJSONObject("away").getString("name");

						
						// Set logos if available
						if( 	currentGame.getJSONObject("home").has("logo")
								&& currentGame.getJSONObject("away").has("logo")
						)
						{
							game.homeLogo = currentGame.getJSONObject("home").getString("logo");
							game.awayLogo = currentGame.getJSONObject("away").getString("logo");
						}
						
						// Set goals if available
						if(		currentGame.getJSONObject("home").has("goals")
								&& currentGame.getJSONObject("away").has("goals")
						)
						{
							String homeGoals = currentGame.getJSONObject("home").getString("goals");
							String awayGoals = currentGame.getJSONObject("away").getString("goals");
							if(!homeGoals.equalsIgnoreCase("-"))
							{
								game.homeGoals = currentGame.getJSONObject("home").getInt("goals");
							}
							if(!awayGoals.equalsIgnoreCase("-"))
							{
								game.awayGoals = currentGame.getJSONObject("away").getInt("goals");
							}
						}
						
						// Set latest event if available
						if(		currentGame.has("latest-event")
								&& !currentGame.isNull("latest-event")
								&& currentGame.getJSONObject("latest-event").has("team")
								&& currentGame.getJSONObject("latest-event").has("text")
								&& currentGame.getJSONObject("latest-event").has("time")
							)
						{
							game.latestEventTeam = currentGame.getJSONObject("latest-event").getString("team");
							game.latestEventText = currentGame.getJSONObject("latest-event").getString("text");
							game.latestEventTime = currentGame.getJSONObject("latest-event").getString("time");
						}
						else
						{
							if(game.isStarted)
							{
								game.latestEventText = "Ei tapahtumia";
							}
						}
						
						// Set report link if available
						if(currentGame.has("report"))
						{
							game.urlReport = currentGame.getString("report");
						}
					
						// Do updates
						updateGame(game);
					}
				}
			}
	
		}
		catch (JSONException e)
		{
			Toast.makeText(getApplicationContext(), "Pelien haku ei onnistunut", Toast.LENGTH_LONG).show();
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setUpdateMode(false);
	}
	
	private UpdatableGameView createGame(Game game)
	{
		// Holder for accessing updatable properties of a game
		UpdatableGameView gameView = new UpdatableGameView(game.gameId);
		
		// Adjust things based of pixel densities
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		int bigFontSize = 24;
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, metrics);
		int padding2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics);
		int padding3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, metrics);
		
		// General layout containing all the games
		LinearLayout gamesLayout = (LinearLayout) findViewById(R.id.gamesViewLayout);
		
		// Layout for a single game
		LinearLayout gameLayout = new LinearLayout(this);
		gameLayout.setOrientation(LinearLayout.VERTICAL);
		
		// When a game is clicked one can access the report of the game
		final String reportUrl = game.urlReport;
		gameLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent webView = new Intent(getApplicationContext(), WebViewActivity.class);
				webView.putExtra("url", "http://www.liiga.fi" + reportUrl);
			    startActivity(webView);
			}
		});

		// View containing the current status (eg. time played) of the game
		gameView.statusView = new TextView(this);
		gameView.statusView.setTextColor(Color.WHITE);
	
		// View for showing the latest event (eg. penalty)
		LinearLayout latestEventLayout = new LinearLayout(this);
		LinearLayout latestEventTeamTimeLayout = new LinearLayout(this);
		latestEventLayout.setOrientation(LinearLayout.HORIZONTAL);	
		latestEventTeamTimeLayout.setOrientation(LinearLayout.VERTICAL);
		
		gameView.latestEventTextView = new TextView(this);
		gameView.latestEventTeamView = new TextView(this);
		gameView.latestEventTimeView = new TextView(this);
		gameView.latestEventTextView.setTextColor(Color.WHITE);
		gameView.latestEventTeamView.setTextColor(Color.WHITE);
		gameView.latestEventTimeView.setTextColor(Color.WHITE);
		
		latestEventTeamTimeLayout.addView(gameView.latestEventTeamView);
		latestEventTeamTimeLayout.addView(gameView.latestEventTimeView);
		
		latestEventLayout.addView(latestEventTeamTimeLayout);
		latestEventLayout.addView(gameView.latestEventTextView);
		
		// Team layout contains the team names and logos and the current goal situation
		LinearLayout teamLayout = new LinearLayout(this);
		teamLayout.setOrientation(LinearLayout.HORIZONTAL);

		ImageView homeLogo = new ImageView(this);
		ImageView awayLogo = new ImageView(this);
		
		if(game.homeLogo != null)
		{
			new DownloadImage().execute("http://liiga.fi"+game.homeLogo, homeLogo);
		}
		if(game.awayLogo != null)
		{
			new DownloadImage().execute("http://liiga.fi"+game.awayLogo, awayLogo);
		}
		
		TextView homeTeam = new TextView(this);
		TextView awayTeam = new TextView(this);
		homeTeam.setTextSize( TypedValue.COMPLEX_UNIT_SP, bigFontSize );
		homeTeam.setTextColor(Color.WHITE);
		awayTeam.setTextSize( TypedValue.COMPLEX_UNIT_SP, bigFontSize );
		awayTeam.setTextColor(Color.WHITE);
		
		if(game.homeTeam != null)
		{
			homeTeam.setText(game.homeTeam);
		}
		if(game.awayTeam != null)
		{
			awayTeam.setText(game.awayTeam);
		}
		
		gameView.homeGoals = new TextView(this);
		gameView.awayGoals = new TextView(this);
		gameView.goalSeparator = new TextView(this);
		gameView.homeGoals.setTextSize( TypedValue.COMPLEX_UNIT_SP, bigFontSize );
		gameView.homeGoals.setTextColor(Color.WHITE);
		gameView.awayGoals.setTextSize( TypedValue.COMPLEX_UNIT_SP, bigFontSize );
		gameView.awayGoals.setTextColor(Color.WHITE);
		gameView.goalSeparator.setTextSize( TypedValue.COMPLEX_UNIT_SP, bigFontSize );
		gameView.goalSeparator.setTextColor(Color.WHITE);
		
		
		// Layout containing team logos, names and goals
		teamLayout.addView(homeTeam);
		teamLayout.addView(homeLogo);
		teamLayout.addView(gameView.homeGoals);
		teamLayout.addView(gameView.goalSeparator);
		teamLayout.addView(gameView.awayGoals);
		teamLayout.addView(awayLogo);
		teamLayout.addView(awayTeam);
		
		// Add status view, team view and latest event view to the layout of single game
		gameLayout.addView(gameView.statusView);
		gameLayout.addView(teamLayout);
		gameLayout.addView(latestEventLayout);
		
		// Add some bottom margin for a single game layout so there is some spacing between the games
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics));
		
		// Add single game to main layout
		gamesLayout.addView(gameLayout, layoutParams);
		
		// Finally, resize the logos of the teams here
		int height = homeLogo.getLayoutParams().height;
		homeLogo.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics);
		homeLogo.getLayoutParams().width = calculateLogoWidth(homeLogo.getLayoutParams().height, height, homeLogo.getLayoutParams().width);
		
		height = awayLogo.getLayoutParams().height;
		awayLogo.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics);
		awayLogo.getLayoutParams().width = calculateLogoWidth(awayLogo.getLayoutParams().height, height, awayLogo.getLayoutParams().width);
		
		// Try to move some things to center
		gameLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		teamLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		gameView.statusView.setGravity(Gravity.CENTER_HORIZONTAL);
		
		// Background color of single game view
		gameLayout.setBackgroundColor(Color.parseColor("#333439"));
		
		// Fill the whole display width with single game
		gameLayout.getLayoutParams().width = LayoutParams.FILL_PARENT;
		
		// Add some paddings
		gameLayout.setPadding(padding, padding, padding, padding);
		gameView.statusView.setPadding(0,  padding3,  0,  padding);
		gameView.homeGoals.setPadding(padding2,  padding,  padding2,  padding);
		gameView.awayGoals.setPadding(padding2,  padding,  padding2,  padding);
		homeTeam.setPadding(0,  padding,  padding2,  padding);
		awayTeam.setPadding(padding2,  padding,  0,  padding);
		homeLogo.setPadding(padding2,  padding,  padding2,  padding);
		awayLogo.setPadding(padding2,  padding,  padding2,  padding);
		gameView.goalSeparator.setPadding(padding2,  padding,  padding2,  padding);
	
		gameView.latestEventTeamView.setPadding(padding,  padding3,  padding,  padding3);
		gameView.latestEventTimeView.setPadding(padding,  0,  padding,  padding3);
		gameView.latestEventTextView.setPadding(padding,  padding3,  padding,  padding3);
		
		return gameView;
	}
	
	UpdatableGameView getGameView(Game game)
	{
		for(int i = 0; i < gameViews.size(); i++)
		{
			if(gameViews.get(i).gameId == game.gameId)
			{
				return gameViews.get(i);
			}
		}
		
		UpdatableGameView gameView = createGame(game);
		gameViews.add(gameView);
		return gameView;
	}
	
	public void updateGame(Game game)
	{
		UpdatableGameView gameView = getGameView(game);
		
		gameView.statusView.setText(game.currentStatus);
		
		if(game.latestEventText != null && game.latestEventTeam != null && game.latestEventTime != null)
		{
			gameView.latestEventTextView.setText( android.text.Html.fromHtml(game.latestEventText).toString() );
			gameView.latestEventTeamView.setText(game.latestEventTeam);
			gameView.latestEventTimeView.setText(game.latestEventTime);
		}
		
		if(game.isStarted == true || game.isFinished == true)
		{
			gameView.homeGoals.setText( Integer.toString(game.homeGoals) );
			gameView.awayGoals.setText( Integer.toString(game.awayGoals) );
			gameView.goalSeparator.setText(" - ");
		}
		else
		{
			gameView.goalSeparator.setText(" vs ");
			gameView.homeGoals.setText("");
			gameView.awayGoals.setText("");
		}
	}
	
	private int calculateLogoWidth(int newHeight, int oldHeight, int oldWidth)
	{
		double multiplier = newHeight/oldHeight;
		int newWidth = (int) (oldWidth*multiplier);
		
		return newWidth;
	}
	
	private void setImage(Drawable drawable, ImageView view)
	{
	    view.setBackgroundDrawable(drawable);
	}
	
	public class DownloadImage extends AsyncTask<Object, Integer, Drawable> {
		ImageView updatableView;
		
	    @Override
	    protected Drawable doInBackground(Object... arg0) {

	    	String url = (String) arg0[0];
	    	updatableView = (ImageView) arg0[1];
	    	
	        return downloadImage(url);
	    }
	    protected void onPostExecute(Drawable image)
	    {
	        setImage(image, updatableView);
	    }

	    @SuppressWarnings("deprecation")
		private Drawable downloadImage(String _url)
	    {
	        //Prepare to download image
	        URL url;        
	        BufferedOutputStream out;
	        InputStream in;
	        BufferedInputStream buf;

	        try
	        {
	            url = new URL(_url);
	            in = url.openStream();
	            buf = new BufferedInputStream(in);

	            Bitmap bMap = BitmapFactory.decodeStream(buf);
	            if (in != null) {
	                in.close();
	            }
	            if (buf != null) {
	                buf.close();
	            }

	            return new BitmapDrawable(bMap);

	        } catch (Exception e) {
	            Log.e("Error reading file", e.toString());
	        }

	        return null;
	    }
	}
}
