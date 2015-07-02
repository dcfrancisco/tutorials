package com.korsbergtools.ImpactPhysics;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * A custom version of the standard android SeekBar widget
 * @author Ed Korsberg
 *
 */
public class MySeekbar implements OnSeekBarChangeListener
{
	//Your member variable declaration here
	private final SeekBar mybar;
	private final TextView mText;
	private final CharSequence basename;

	
	//our value but how to return/expose to app?
	private int mCurrentProgress;


    /**
     * constructor
     * @param mybar		The real SeekBar widget we extend
     * @param textview	A TextView box associated with this SeekBar
     * @param progress	Initial value of the SeekBar progress attribute
     */
    public MySeekbar(SeekBar mybar, TextView textview, int progress)
    {
        this.mybar = mybar;
        this.mText = textview;
        this.basename = mText.getText();
        this.mybar.setOnSeekBarChangeListener(this);
        
        mCurrentProgress = progress;
        mybar.setProgress(progress);
        		
        
        //get mCurrentPos from either 1) intent, 2) classOptions, 3)persistent storage?
    }
    
    
    /**
     * Get SeekBar progress attribute
     * @return current SeekBar progress attribute
     */
    public int GetPosition() {return mCurrentProgress;}


    /**
     * {@inheritDoc}
     */
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
	{
		// TODO Auto-generated method stub
		mCurrentProgress = progress;
		mText.setText(basename + " = " + Integer.toString(progress));
		
	}

    /**
     * {@inheritDoc}
     */
	public void onStartTrackingTouch(SeekBar seekBar) 
	{
		// TODO Auto-generated method stub
		
	}

    /**
     * {@inheritDoc}
     */
	public void onStopTrackingTouch(SeekBar seekBar) 
	{
		// TODO Auto-generated method stub
		
	}
	
}	//MySeekbar
