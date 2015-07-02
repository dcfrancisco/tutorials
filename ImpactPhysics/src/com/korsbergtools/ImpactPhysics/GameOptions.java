package com.korsbergtools.ImpactPhysics;


//import java.io.Serializable;

//import com.korsbergtools.R;
import com.korsbergtools.ImpactPhysics.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;


public class GameOptions extends Activity 
{

	//Your member variable declaration here
	MySeekbar speedBar;
	MySeekbar addBallsBar;
	MySeekbar mgravityBar;
	MySeekbar viscosityBar;
	MySeekbar restititionBar;
	MySeekbar volumeBar;
	GameOptionParams gameParams;
	
	Button buttonQuit;	
	Button buttonOrbit;
	Button buttonDrift;
	Button buttonCenter;
	Button buttonClean;
	
	CheckBox pauseCheckbox;		//1
	CheckBox tiltCheckbox;		//2
	CheckBox collideCheckbox;	//3
	CheckBox mushCheckbox;		//4
	CheckBox wrapCheckbox;		//5
	CheckBox shakeItUpCheckbox;	//6
	CheckBox bubbleSoundCheckbox;	//7
	

	
	//local copies of contents of /res/attrs.xml


	/**
	 * Called when the activity is first created.
	 * @note maybe don't set initial values here but take from defaults in xml file?
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);
        
        //retrieve intended options
        Bundle b = getIntent().getExtras();
        gameParams = (GameOptionParams) b.getSerializable("options");

           	
        //create enhanced speedbar's
        speedBar       = new MySeekbar((SeekBar) findViewById(R.id.seekBar1), (TextView) findViewById(R.id.textView1), gameParams.speedBarPosition);
    	addBallsBar    = new MySeekbar((SeekBar) findViewById(R.id.seekBar2), (TextView) findViewById(R.id.textView2), gameParams.addBallsBarPosition);
    	mgravityBar    = new MySeekbar((SeekBar) findViewById(R.id.seekBar3), (TextView) findViewById(R.id.textView3), gameParams.mgravityBarPosition);
    	viscosityBar   = new MySeekbar((SeekBar) findViewById(R.id.seekBar4), (TextView) findViewById(R.id.textView4), gameParams.viscosityBarPosition);
    	restititionBar = new MySeekbar((SeekBar) findViewById(R.id.seekBar5), (TextView) findViewById(R.id.textView5), gameParams.restititionBarPosition);
    	volumeBar      = new MySeekbar((SeekBar) findViewById(R.id.seekBar6), (TextView) findViewById(R.id.textView6), gameParams.volumeBarPosition);
    	       
    	//
    	//set click handlers
    	//
    	
    	//the apply and exit button
    	buttonQuit = (Button) findViewById(R.id.button2);
    	buttonQuit.setOnClickListener(quitHandler);
    	
    	//the remaining buttons
    	buttonOrbit = (Button) findViewById(R.id.button4);
    	buttonDrift = (Button) findViewById(R.id.button5);
    	buttonCenter = (Button) findViewById(R.id.button6);
    	buttonOrbit.setOnClickListener(buttonHandler);
    	buttonDrift.setOnClickListener(buttonHandler);
    	buttonCenter.setOnClickListener(buttonHandler);

    	
    	//get widgets
    	pauseCheckbox		= (CheckBox) findViewById(R.id.checkBox1);
    	tiltCheckbox		= (CheckBox) findViewById(R.id.checkBox2);
    	collideCheckbox		= (CheckBox) findViewById(R.id.checkBox3);
    	mushCheckbox		= (CheckBox) findViewById(R.id.checkBox4);
    	wrapCheckbox		= (CheckBox) findViewById(R.id.checkBox5);
    	shakeItUpCheckbox	= (CheckBox) findViewById(R.id.checkBox6);
    	shakeItUpCheckbox.setOnClickListener(checkBoxHandler);
    	bubbleSoundCheckbox = (CheckBox) findViewById(R.id.checkBox7);
    	
    	//restore state of checkboxes
    	pauseCheckbox.setChecked(gameParams.pinchZoom);
    	tiltCheckbox.setChecked(gameParams.tilt);
    	collideCheckbox.setChecked(gameParams.collide);
    	mushCheckbox.setChecked(gameParams.mush);
    	wrapCheckbox.setChecked(gameParams.wrap);
    	shakeItUpCheckbox.setChecked(gameParams.shakeItUp);
    	bubbleSoundCheckbox.setChecked(gameParams.bubbleSound);

	}	//onCreate
	
	/**
	 * called when 'apply and quit' button is pressed
	 */
	View.OnClickListener quitHandler = new View.OnClickListener() {
		public void onClick(View v) {
			// read widget values
			gameParams.speedBarPosition = speedBar.GetPosition();
			gameParams.addBallsBarPosition = addBallsBar.GetPosition();
			gameParams.mgravityBarPosition = mgravityBar.GetPosition();
			gameParams.viscosityBarPosition = viscosityBar.GetPosition();
			gameParams.restititionBarPosition = restititionBar.GetPosition();
			gameParams.volumeBarPosition = volumeBar.GetPosition();
			
			gameParams.pinchZoom = pauseCheckbox.isChecked();
			gameParams.tilt = tiltCheckbox.isChecked();
			gameParams.collide = collideCheckbox.isChecked();
			gameParams.mush = mushCheckbox.isChecked();
			gameParams.wrap = wrapCheckbox.isChecked();
			gameParams.shakeItUp = shakeItUpCheckbox.isChecked();
			gameParams.bubbleSound = bubbleSoundCheckbox.isChecked();
			
			// prepare to send results back to invoker
			Intent resultIntent = new Intent();
			Bundle b = new Bundle();
			b.putSerializable("options", gameParams);
			resultIntent.putExtras(b);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
		}	//onClick
	};
    
	/**
	 * called when the other buttons are clicked
	 */
	View.OnClickListener buttonHandler = new View.OnClickListener() {
		  public void onClick(View v) {
		      if( buttonOrbit.getId() == ((Button)v).getId() )
		      {
		    	  gameParams.orbitClick = true;
		      }
		      else if( buttonDrift.getId() == ((Button)v).getId() )
		      {
		    	  gameParams.driftClick = true;
		      }
		      else if( buttonCenter.getId() == ((Button)v).getId() )
		      {
		    	  gameParams.centerClick = true;
		      }

		  }	//onClick
		};

		/**
		 * called when the other buttons are clicked
		 */
		View.OnClickListener checkBoxHandler = new View.OnClickListener() {
			  public void onClick(View v) {
			      if( shakeItUpCheckbox.getId() == ((CheckBox)v).getId() )
			      {
			    	  if (shakeItUpCheckbox.isChecked() == true)
			    	  {
			    		  //tiltCheckbox.setChecked(false);
			    	  }
			      }

			  }	//onClick
			};
}
