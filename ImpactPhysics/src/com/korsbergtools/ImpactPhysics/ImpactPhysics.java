package com.korsbergtools.ImpactPhysics;

/*
 some general notes from google groups
 1. Try to avoid Serializable. It is very slow compared to Parcelable.
2. Take a look at startActivityForResult and onActivityResult.
3. If you reeeaaally need to update activity A about events from activity B,
consider putting your own subclass of ResultReceiver in the Intent's
'extras'. Then activity B can call 'send(...)' on that ResultReceiver and
this will be received in activity A as an 'onReceiveResult(...)' call-back. 

 */

//import org.xmlpull.v1.XmlPullParser;

//import droid.pkg.R;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
//import android.widget.Toast;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

//color picker
import com.example.android.apis.graphics.ColorPickerDialog;
import com.example.android.apis.graphics.ColorPickerDialog.OnColorChangedListener;
import com.korsbergtools.ImpactPhysics.R;

/**
 * ImpactPhysics is an Android application ported and updated from a Java applet written in 1996
 * by John Henckel, henckel@vnet.ibm.com June 1996, (C)  COPYRIGHT International Business Machines Corp. 1996 
 * The general purpose of this application is to create 2D ball objects that have attributes of 
 * mass, size, color, coefficient of restitution and then place them in a medium with a degree of viscosity.
 * The balls then interact with each other being attracted to each other by gravity and when they collide
 * will rebound as a function their coefficient of restitution.
 * In addition the user can interact with the ball via the touch sensor and via the accelerometer.
 * 
 * @author Ed Korsberg
 * @version 1.0
 * 
 *
 */
public class ImpactPhysics extends Activity implements OnColorChangedListener, SensorEventListener
{
	//===================================================
	//static constants
	//===================================================
	static final int STATIC_OPTIONS_VALUE = 0;
    static int EXIT_MENUID = 1;
    static int CLEAR_MENUID = 2;
    static int ADD_MENUID = 3;
    static int COLOR_MENUID = 4;
    static int OPTION_MENUID = 5;
    static int POP_MENUID = 6;
    static int ABOUT_MENUID = 7;
    static final int COLOR_DIALOG_ID = 10;

    //===================================================
    //instance variables
    //===================================================
    ImpactPhysics pThis = this;

    // handle to main screen
    android.widget.FrameLayout mainFrame;
    
    //most of the heavy work is done in the animator view
    AnimatorView mBallView = null;
    
    // parameters passed to/from GameOptions
    GameOptionParams gameParams = new GameOptionParams();
       
    //screen width and height
	int mScrWidth, mScrHeight;
	
	//newColor is set via colorChanged callback
	int newColor = Color.BLUE;
	
	//for accelerometer
	SensorManager mSensorManager = null;
	SensorEventListener mSensorListener = null;
    
    private long lastUpdate = -1;
    private float x, y, z;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 400;
    
    //music
    MediaPlayer mediaPlayerBubbles = null;
    MediaPlayer mediaPlayerSprong = null;
    MediaPlayer mpPop = null;

    
    //=======================
    //===================================================================
    //Overrides for this activity
    //===================================================================

	//'main' entry for this activity
    @Override
    /**
     * {@inheritDoc}
     */    
    public void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar       
        //getWindow().setFlags(0xFFFFFFFF, LayoutParams.FLAG_FULLSCREEN|LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(0xFFFFFFFF, LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //create pointer to main screen
        mainFrame = (android.widget.FrameLayout) findViewById(R.id.main_view);
        
        //get screen dimensions
        Display display = getWindowManager().getDefaultDisplay();  
        mScrWidth = display.getWidth(); 
        mScrHeight = display.getHeight();
        
        
        //create the view that will do the hard work (animator)
        mBallView = new AnimatorView(this, mainFrame, mScrWidth, mScrHeight);  
        mainFrame.addView(mBallView); //link view this context
             
        		    
        //create initial game options
        //TODO restore from persistent storage or options.xml?
        //apparently these resources are not initialized or available outside of the
        //activity (or view or context?) that displays them, ie GameOptions.java
        //as it stands now, these manual settings may not reflect the initial state
        //of the values in options.xml
        //TODO find way to read these directly from options.xml
        if (savedInstanceState == null)
        {
	        gameParams.speedBarPosition			= 10;
	        gameParams.addBallsBarPosition		= 50; 
	        gameParams.mgravityBarPosition		= 10;
	        gameParams.viscosityBarPosition		= 2;
	        gameParams.restititionBarPosition	= 17;
	        gameParams.volumeBarPosition	    = 100;
	        gameParams.collide 					= true;
	        gameParams.pinchZoom				= false;
	        gameParams.tilt						= true;
	    	gameParams.mush						= false;
	    	gameParams.wrap						= false;
	    	gameParams.shakeItUp				= true;
        }
        else
        {
        	//TODO this never gets called
	        gameParams.speedBarPosition			= savedInstanceState.getInt("speedBarPosition");
	        gameParams.addBallsBarPosition		= savedInstanceState.getInt("addBallsBarPosition");
	        gameParams.mgravityBarPosition		= savedInstanceState.getInt("mgravityBarPosition");
	        gameParams.viscosityBarPosition		= savedInstanceState.getInt("viscosityBarPosition");
	        gameParams.restititionBarPosition	= savedInstanceState.getInt("restititionBarPosition");
	        gameParams.volumeBarPosition	    = savedInstanceState.getInt("volumeBarPosition");
	        gameParams.collide 					= savedInstanceState.getBoolean("collide");
	        gameParams.pinchZoom				= savedInstanceState.getBoolean("pinchZoom");
	        gameParams.tilt						= savedInstanceState.getBoolean("tilt");
	    	gameParams.mush						= savedInstanceState.getBoolean("mush");
	    	gameParams.wrap						= savedInstanceState.getBoolean("wrap");

        }
    	//apply options 
    	mBallView.updateOptions(gameParams);
    	
    	//music
    	mediaPlayerBubbles = MediaPlayer.create(this, R.raw.bubbles);
    	mediaPlayerBubbles.setLooping(true);    	
    	
    	mediaPlayerSprong = MediaPlayer.create(this, R.raw.sprong);
    	mediaPlayerSprong.setLooping(false);
    	
    	//TODO pop.mp3 not working on phone but does on emulator?
    	//pop.wav does not crash but no sound on phone but does on emulator
    	mpPop = MediaPlayer.create(this, R.raw.zip); 
    	mpPop.setLooping(false);
    	
    	//sensors
    	mSensorManager = ((SensorManager)getSystemService(Context.SENSOR_SERVICE));

    } //OnCreate
    
    @Override
    /**
     * {@inheritDoc}
     */    
    public void onSaveInstanceState(Bundle savedInstanceState) {
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
        savedInstanceState.putInt("speedBarPosition", gameParams.speedBarPosition);
        savedInstanceState.putInt("addBallsBarPosition", gameParams.addBallsBarPosition);
        savedInstanceState.putInt("mgravityBarPosition", gameParams.mgravityBarPosition);
        savedInstanceState.putInt("viscosityBarPosition", gameParams.viscosityBarPosition);
        savedInstanceState.putInt("restititionBarPosition", gameParams.restititionBarPosition);
        savedInstanceState.putInt("volumeBarPosition", gameParams.volumeBarPosition);
        savedInstanceState.putBoolean("collide", gameParams.collide);
        savedInstanceState.putBoolean("pinchZoom", gameParams.pinchZoom);
        savedInstanceState.putBoolean("tilt", gameParams.tilt);
    	savedInstanceState.putBoolean("mush", gameParams.mush);
    	savedInstanceState.putBoolean("wrap", gameParams.wrap);
    	savedInstanceState.putBoolean("bubbleSound", gameParams.bubbleSound);

      // etc.
      super.onSaveInstanceState(savedInstanceState);
    }

    //will get passed in to onCreate() and also onRestoreInstanceState()
    @Override
    /**
     * {@inheritDoc}
     */    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.
		  gameParams.speedBarPosition			= savedInstanceState.getInt("speedBarPosition");
		  gameParams.addBallsBarPosition		= savedInstanceState.getInt("addBallsBarPosition");
		  gameParams.mgravityBarPosition		= savedInstanceState.getInt("mgravityBarPosition");
		  gameParams.viscosityBarPosition		= savedInstanceState.getInt("viscosityBarPosition");
		  gameParams.restititionBarPosition		= savedInstanceState.getInt("restititionBarPosition");
		  gameParams.volumeBarPosition		    = savedInstanceState.getInt("volumeBarPosition");
		  gameParams.collide 					= savedInstanceState.getBoolean("collide");
		  gameParams.pinchZoom					= savedInstanceState.getBoolean("pinchZoom");
		  gameParams.tilt						= savedInstanceState.getBoolean("tilt");
		  gameParams.mush						= savedInstanceState.getBoolean("mush");
		  gameParams.wrap						= savedInstanceState.getBoolean("wrap");
		  gameParams.bubbleSound				= savedInstanceState.getBoolean("bubbleSound");
    }	//onRestoreInstanceState

    
    //listener for menu button on phone
    @Override
    /**
     * {@inheritDoc}
     */    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        menu.add(Menu.NONE,EXIT_MENUID,Menu.NONE,"Exit");
        menu.add(Menu.NONE,CLEAR_MENUID,Menu.NONE,"Clear");
        menu.add(Menu.NONE,ADD_MENUID,Menu.NONE,"Add balls");
        menu.add(Menu.NONE,OPTION_MENUID,Menu.NONE,"Options...");
        menu.add(Menu.NONE,ABOUT_MENUID,Menu.NONE,"About...");
        //the following menu items get lumped into the "More" category
        menu.add(Menu.NONE,POP_MENUID,Menu.NONE,"Pop");
        menu.add(Menu.NONE,COLOR_MENUID,Menu.NONE,"Choose color");
        return super.onCreateOptionsMenu(menu);
    }
    
    //listener for menu item clicked 
    @Override
    /**
     * {@inheritDoc}
     */    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	// Handle item selection 	
    	if (item.getItemId() == EXIT_MENUID)	//user clicked Exit
    		finish(); //will exit the activity
    	else if (item.getItemId() == CLEAR_MENUID)	//user clicked Clear
    		clearScreen();
    	else if (item.getItemId() == ADD_MENUID)	//user clicked Add balls
    		addBallChoice();
    	else if (item.getItemId() == COLOR_MENUID)	//user clicked choose color
    		chooseColor();
    	else if (item.getItemId() == OPTION_MENUID)	//user clicked Options...
    		options();
    	else if (item.getItemId() == POP_MENUID)	//user clicked Pop
    		pop();
    	else if (item.getItemId() == ABOUT_MENUID)	//user clicked About...
    		aboutBox();		
   		return super.onOptionsItemSelected(item);    
    }
    
    //For state flow see http://developer.android.com/reference/android/app/Activity.html
    @Override
    /**
     * {@inheritDoc}
     */    
    public void onPause() //app moved to background, stop background threads
    {
    	mBallView.OnPauseProxy();
    	super.onPause();
    }
    
    //For state flow see http://developer.android.com/reference/android/app/Activity.html
    @Override
    /**
     * {@inheritDoc}
     */    
    public void onResume() //app moved to foreground (also occurs at app startup)
    {
    	mBallView.OnResumeProxy();
        super.onResume();
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);

    } // onResume
    
    //For state flow see http://developer.android.com/reference/android/app/Activity.html
    @Override
    /**
     * {@inheritDoc}
     */    
    public void onDestroy() //main thread stopped
    {
    	super.onDestroy();
		if (mediaPlayerBubbles != null) {
			try {
				mediaPlayerBubbles.stop();
				mediaPlayerBubbles.release();
			} finally {
				mediaPlayerBubbles = null;
			}
		}
		
		if (mSensorListener != null && mSensorManager!=null)
		{
			mSensorManager.unregisterListener(mSensorListener);
		}
    	
    	System.runFinalizersOnExit(true); //wait for threads to exit before clearing app
    	android.os.Process.killProcess(android.os.Process.myPid());  //remove app from memory 
    }
    
    /**
     * {@inheritDoc}
     */    
    @Override
    protected void onStop() 
    {
		if (mSensorListener != null && mSensorManager!=null)
		{
			mSensorManager.unregisterListener(mSensorListener);
		}
    	super.onStop();
    }

   
    
    /**
     * {@inheritDoc}
     * 
     * listener for config change. 
     * This is called when user tilts phone enough to trigger landscape view
     * we want our app to stay in portrait view, so bypass event 
     */    
    @Override 
    public void onConfigurationChanged(Configuration newConfig)
	{
       super.onConfigurationChanged(newConfig);
	}
    
    /**
     * {@inheritDoc}
     * 
     * return handler from invocation to GameOptions
     */ 
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {     
      super.onActivityResult(requestCode, resultCode, data); 
      switch(requestCode) 
      { 
        case (STATIC_OPTIONS_VALUE) : 
        { 
          if (resultCode == Activity.RESULT_OK) 
          { 
              //retrieve intended options
              Bundle b = data.getExtras();
              gameParams = (GameOptionParams) b.getSerializable("options");
              //TODO there must be a better way to share data but for now pass this onto
              //animator
              mBallView.updateOptions(gameParams);
              //reset one shot buttons
              gameParams.centerClick = false;
              gameParams.cleanClick = false;
              gameParams.driftClick = false;
              gameParams.orbitClick = false;
              
              //sound
              if (gameParams.bubbleSound == true)
              {
        		  float level = (float)gameParams.volumeBarPosition/100;
        		  mediaPlayerBubbles.setVolume(level,level);  //range 0.0 to 1.0f
            	  if (mediaPlayerBubbles.isPlaying() == false)
            	  {
            		  mediaPlayerBubbles.start(); // no need to call prepare(); create() does that for you
            	  }
              }
              else
              {
            	  if (mediaPlayerBubbles.isPlaying() == true)
            	  {
                   	  mediaPlayerBubbles.pause();
            	  }
              }
          } 
          break; 
        } 
      } 
    }   
    
    /**
     * {@inheritDoc}
     */     
     @Override
    protected Dialog onCreateDialog(int id)
    {
	    switch (id) 
	    {
	    case COLOR_DIALOG_ID:
	    	return new ColorPickerDialog(this, this, Color.RED);
	
	    }
	    return null;
    }

    //end of overrides...
    		
    //==============================================================
    //Implementation of SensorEventListener method onAccuracyChanged
    //==============================================================
     
    /**
     * {@inheritDoc}
     */ 
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    //==============================================================
    //Implementation of SensorEventListener method onSensorChanged
    //==============================================================
    /**
     * {@inheritDoc}
     */     
    public void onSensorChanged(SensorEvent event) {
        //int sensor = event.type;
        //float[] values = event.values;

        //Log.d("Impact", "onSensorChanged: " +  " x: " + event.values[0] + ", y: " + event.values[1] + ", z: " + event.values[2]);
	    long curTime = System.currentTimeMillis();
	    boolean shakeDetected = false;
	    // only allow one update every 100ms.
	    if ((curTime - lastUpdate) > 100) 
	    {
			long diffTime = (curTime - lastUpdate);
			lastUpdate = curTime;
	 
			x = event.values[0];
			y = event.values[1];
			z = event.values[2];
	 
			float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
			if (speed > SHAKE_THRESHOLD) 
			{
			    // yes, this is a shake action! Do something about it!
				//string seq = new string("shake detected w/ speed: " + string. speed);
				//CharSequence seq = new CharSequence("shake detected w/ speed: " + speed);
				//Toast.makeText(pThis, "shake detected w/ speed: ", Toast.LENGTH_SHORT).show();
				//mBallView.shakeItUp();
				shakeDetected = true;
			}
			last_x = x;
			last_y = y;
			last_z = z;
	    }
	    
	    if (gameParams.shakeItUp && shakeDetected == true)
	    {
	    	if (mBallView.bShakeItUpInPlay==false)
	    	{
	    		mBallView.shakeItUp();
	        	float level = (float)gameParams.volumeBarPosition/100;
	        	mediaPlayerSprong.setVolume(level,level);  //range 0.0 to 1.0f	    		
	    		mediaPlayerSprong.start();
	    	}
	    }
	    mBallView.updateXYgravity(event.values[0], event.values[1]);
	}	//onSensorChanged
    


    //===============
    //private methods
    //===============
    
    /**
     * Menu action item to delete all but the original balls
     */
    void clearScreen()
    {
    	mBallView.deleteAllButFirstBalls();
    }
    
	/**
	 * this implements the ColorPickerDialog.OnColorChangedListener interface 
	 */
	public void colorChanged(int color)
	{
		newColor = color;
	}
	/**
	 * Menu action item to open ColorPickerDialog to choose new color for added balls
	 */
    void chooseColor()
    {
    	showDialog(COLOR_DIALOG_ID);
    }
    /**
     * Menu action item to add a random set of balls
     */
    void addBallChoice()
    {
    	mBallView.addRandomBall();
    }    

    /**
     * invoke the 2nd activity which contains the main program options
     */
    void options()
    {
    	Intent myIntent = new Intent(this, GameOptions.class);

    	Bundle b = new Bundle();
    	b.putSerializable("options", gameParams);
    	myIntent.putExtras(b);
    	startActivityForResult(myIntent,STATIC_OPTIONS_VALUE);
    }
    
    void aboutBox()
    {
    	Intent myIntent = new Intent(this, WebViewHelp.class);
    	startActivity(myIntent);
    }    
    /**
     * just a test for now.  the idea was to add a feature where the user hits a ball
     * and it gets destroyed followed by a 'pop' sound
     */
    void pop()
    {
    	//add sound
    	float level = (float)gameParams.volumeBarPosition/100;
    	mpPop.setVolume(level,level);  //range 0.0 to 1.0f
        mpPop.start();
    }
    
    /**
     * Just a test for getting a list of all intents
     *
    void logAllIntents()
    {
    	final PackageManager pm = getPackageManager();
    	//get a list of installed apps.
          List<ApplicationInfo> packages = pm
                  .getInstalledApplications(PackageManager.GET_META_DATA);

          for (ApplicationInfo packageInfo : packages) {

              Log.d("Impact", "Installed package :" + packageInfo.packageName);
              Log.d("Impact",
                      "Launch Activity :"
                              + pm.getLaunchIntentForPackage(packageInfo.packageName)); 

          }// the getLaunchIntentForPackage returns an intent that you can use with startActivity() 
      }
      */
    
    
}	//ImpactPhysics
