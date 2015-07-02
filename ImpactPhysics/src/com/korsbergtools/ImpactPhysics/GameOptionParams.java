package com.korsbergtools.ImpactPhysics;


import java.io.Serializable;

/**
 * Class containing all the game options exchanged between app and secondary activity
 * that acts like a custom dialog box.  This class implements Serializable so that it
 * can be used as the extra data of an Intent sent to/from the secondary activity.
 * @author ed
 *
 */
public class GameOptionParams implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	//direct attributes to widgets
	public int speedBarPosition;
	public int addBallsBarPosition;
	public int mgravityBarPosition;
	public int viscosityBarPosition;
	public int restititionBarPosition;
	public int volumeBarPosition;
	
	//checkbox's
	public boolean pinchZoom;
	public boolean tilt;
	public boolean collide;
	public boolean mush;
	public boolean wrap;
	public boolean shakeItUp;
	public boolean bubbleSound;
	
	//buttons
	boolean orbitClick;		//user clicked on orbit button
	boolean driftClick;		//user clicked on drift button
	boolean centerClick;	//user clicked on center button
	boolean cleanClick;		//user clicked on clean button

}	//GameOptionParams
