package com.korsbergtools.ImpactPhysics;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.Toast;


/**
 * 
 * @author Ed Korsberg 2011
 * 
 * This is the main view for this application in which the animation is performed.  
 * Much of this application is performed within this class where as the main Activity ImpactPhysics
 * handles most of the UI events.  However in essence this class is an extention to the main ImpactPhysics class.
 * It contains an array of Ball objects which contain attributes such as position, size, color, etc.
 * This animator thread 
 *
 * @see ImpactPhysics
 * @note implement OnTouchListener
 */
public class AnimatorView extends View implements OnTouchListener
{
	//===================================================
	//static constants
	//===================================================
	static int MAXBALLS = 500;
	
	enum PinchActions {
		/**
		 * user not touching the screen
		 */
		NONE
		/**
		 * Pinch or Zoom action in progress , 2 fingers in play
		 */
		,ZOOM

		/**
		 * single finger drag in progress
		 */
		,DRAG
	}
	
    //===================================================
    //instance variables
    //===================================================
	Ball ballArray[] = null;
	int currCount = 0;		//number of balls in play
	int currentBall;
	float clippedScaleFactor = 1.0f;
	float mx,my;				//port from Impact
	
	Timer mTmr = null;
	TimerTask mTsk = null;

	PointF mBallSpd;
	Handler RedrawHandler = new Handler(); //so redraw occurs in main thread
	android.widget.FrameLayout mainFrame;
	AnimatorView pThis;
	Context pContext;
	GameOptionParams gameParams;
	
	Random placementRand = new Random();
	
	//when pinchOption is true
	PinchActions pinchMode = PinchActions.NONE;
	float oldDist = 0.0f;
	float newDist = 0.0f;

	
    //  The following are some "physical" properties.  Each property
    //  has a value and a control.  The values are updated once per
    //  animation loop (this is for efficiency).
    public double vertGravity,massGravity,friction,restitution;
    public double horzGravity;
    public boolean bTilt,bCollide,bMush,bWrap,bSmooth;
	int mScrWidth, mScrHeight;
    long speed = 10;
    
    //shake it up options
    boolean bShakeItUpInPlay = false;
    long mShakeCountdown = 0;
    long mShakeCountdownCurTime = 0;
         
    private static final int HELLO_ID = 1;
    
   
    /**
     * Show an event in the LogCat view, for debugging
     * @param event
     */
    @SuppressWarnings("unused")
    private void dumpEvent(MotionEvent event) {
       String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
          "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
       StringBuilder sb = new StringBuilder();
       int action = event.getAction();
       int actionCode = action & MotionEvent.ACTION_MASK;
       sb.append("event ACTION_" ).append(names[actionCode]);
       if (actionCode == MotionEvent.ACTION_POINTER_DOWN
             || actionCode == MotionEvent.ACTION_POINTER_UP) {
          sb.append("(pid " ).append(
          action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
          sb.append(")" );
       }
       sb.append("[" );
       for (int i = 0; i < event.getPointerCount(); i++) {
          sb.append("#" ).append(i);
          sb.append("(pid " ).append(event.getPointerId(i));
          sb.append(")=" ).append((int) event.getX(i));
          sb.append("," ).append((int) event.getY(i));
          if (i + 1 < event.getPointerCount())
             sb.append(";" );
       }
       sb.append("]" );
       Log.d("dumpEvent", sb.toString());
    }    //dumpEvent
    
    
	/**
	 * To find out how far apart two fingers are, we first construct a vector (x, y) which is the difference 
	 * between the two points. Then we use the formula for Euclidean distance to calculate the spacing:
	 * @param event
	 * @return
	 */
	private float spacing(MotionEvent event) {
	   float x = event.getX(0) - event.getX(1);
	   float y = event.getY(0) - event.getY(1);
	   return (float)Math.sqrt(x * x + y * y);
	}    

	/**
	 * Calculating a point in the middle of two points
	 * @param point
	 * @param event
	 */
	@SuppressWarnings("unused")
	private void midPoint(PointF point, MotionEvent event) {
		   float x = event.getX(0) + event.getX(1);
		   float y = event.getY(0) + event.getY(1);
		   point.set(x / 2, y / 2);
		}    	

    
    /**
     * Constructor 
     * 
     * @param context	Binds to main activity
     * @param mainFrame	The frame to which to listen for touch events
     * @param ScrWidth	Width of screen
     * @param ScrHeight	Height of screen
     */
    public AnimatorView(Context context, FrameLayout mainFrame, int ScrWidth, int ScrHeight) {
        super(context);
        pContext = context;
        pThis = this;
        ballArray = new Ball[MAXBALLS];
        mScrWidth = ScrWidth;
        mScrHeight = ScrHeight;
        my = -17;       // mouse up (hack) TODO fix
        
        //TODO read control properties from initial state
        bCollide = true;		//collide
        restitution = 17.0/20.0;	//restitution
        massGravity = 0.5;		//mgravity

        //create variables for ball position and speed
    	mBallSpd = new PointF(1,2);
    	
        //create initial 2 balls      
    	PointF loc = new PointF(100, 100);            
        addBall(Color.YELLOW, 40, 40, loc, (float) 0.48, 0);
    	loc = new PointF(100, 50);            
        addBall(Color.CYAN, 10, 10, loc, (float) -3, 0);
    		
        //listener for touch event, calls onTouch
        setOnTouchListener(this);
   	
    }	//AnimatorView
    
    /**
     * {@inheritDoc}
     * @note why is @Override not needed?
     */ 
    public boolean onTouch(android.view.View v, android.view.MotionEvent e) 
    {        	
    	boolean consumed = true;	//assume we consumed this event
		float x = e.getX();
		float y = e.getY();  	
		
		switch (e.getAction() & MotionEvent.ACTION_MASK)
		{
		case MotionEvent.ACTION_DOWN:
			//ignore
			pinchMode = PinchActions.DRAG;
    		currentBall = nearestBall(x,y,currCount);
    		mx = x; my = y;
			break;
			
		case MotionEvent.ACTION_UP:
    		// this magic number means that the mouse is up
    		my = -17;	//seems like a hack
    		pinchMode = PinchActions.NONE;
			break;
			
		case MotionEvent.ACTION_POINTER_DOWN:
			//this action occurs when 2nd finger is down
            oldDist = spacing(e);
            Log.d("ImpactMove", "oldDist=" + oldDist);
            if (oldDist > 10f) {
            	pinchMode = PinchActions.ZOOM;
            	//Log.d("ImpactMove", "mode=ZOOM" );
            }
			
			break;
		case MotionEvent.ACTION_POINTER_UP:
			//I think this means the 2nd finger was raised
			pinchMode = PinchActions.DRAG;
			break;

        case MotionEvent.ACTION_MOVE:
            if (pinchMode == PinchActions.ZOOM) 
            {
                //these moves happen frequently, apply a kind of low pass filter
                //to slow it down

                newDist = spacing(e);
                //Log.d("ImpactMove", "newDist =" + newDist );
                if (newDist > 10f) {
                    float scale = newDist / oldDist;
                    Log.d("ImpactMove", "mode=ZOOM scale =" + scale );
                    reScaleBalls(scale);
                }
            }
            else if (pinchMode == PinchActions.DRAG)
            {
            	mx = x; my = y;
            }
            break;
			
		default:
			consumed = false;
			break;
		}
		
    	//dumpEvent(e);

		//return True if the listener has consumed the event, false otherwise.
    	return consumed;
    }	//onTouch
    
    
    /**
     * resize ball radius but clip at some reasonable min/max size
     * apply scaling factor to make balls larger or smaller
     * as soon as 2nd finger goes down then new==old and thus scale=1.0
     * this will immediate reset scale, I think we want some form of delta
     * or ramping up/down
     * @param scale
     */
    void reScaleBalls(float scale)
    {
    	float temp = clippedScaleFactor*scale;
    	temp = Math.max(0.2f, temp); //clip floor at 20%
    	clippedScaleFactor = Math.min(5.0f, temp); //clip ceiling at 200%
    	
    	Log.d("clippedScaleFactor", "val = "+clippedScaleFactor);
        for (int i=0; i<currCount; i++ )
        {
        	ballArray[i].radius = ballArray[i].radiusBase*clippedScaleFactor;
        }
    }	//reScaleBalls
    
    /**
     * called from main activity after user chooses new game options
     * @param options	Serialized set of all options
     * @todo get max values from xml
     */
    public void updateOptions(GameOptionParams options)
    {
    	gameParams = options;
    	speed = 10 + gameParams.speedBarPosition;
    	massGravity = gameParams.mgravityBarPosition/20.0;
    	friction = gameParams.viscosityBarPosition/20.0;
    	restitution = gameParams.restititionBarPosition/20.0;
    	bTilt = gameParams.tilt;
    	bCollide = gameParams.collide;
    	bMush = gameParams.mush;
    	bWrap = gameParams.wrap;
    	//check buttons
    	if (gameParams.centerClick)
    		center();
    	else if (gameParams.driftClick)
    		zeroMomentum();
    	else if (gameParams.orbitClick)
    		orbit();

    }	//updateOptions
    
    
    /**
     * called from main activity during onSensorChanged
     * @param xg	X component from accelerometer, but in this app is equated to x velocity
     * @param yg	Y component from accelerometer, but in this app is equated to y velocity
     */
    void updateXYgravity(float xg, float yg)
    {
    	if (bTilt == true)
    	{
    		//apply the accelerometer readings
    		vertGravity = xg;
    		horzGravity = yg;
    	}
    	else
    	{
    		//reset vectors to 0
    		vertGravity = 0;
    		horzGravity = 0;
    	}
    }	//updateXYgravity
    
    
    /**
     * called from main activity during onSensorChanged on 'shake'
     */
    void shakeItUp()
    {
    	if (bShakeItUpInPlay == false)
    	{
	    	bShakeItUpInPlay = true;
	    	if (massGravity > 0.0)
	    		massGravity = -massGravity; //turn attractive gravity into repulsive force
	    	mShakeCountdown = 2000; //TODO what units? ticks, time? msec
	    	mShakeCountdownCurTime = System.currentTimeMillis();
    	}
    }	//shakeItUp
    
    
    
    /**
     * called from main activity
     */
    public void addRandomBall()
    {
    	for (int i=0; i<gameParams.addBallsBarPosition; i++)
    	{
    		
    		int x = placementRand.nextInt(mScrWidth/2);
    		int y = placementRand.nextInt(mScrHeight/2);
    		int r = 3 + placementRand.nextInt(30);
    		int mass = r;	//TODO make mass proportional to radius
    		int c = Color.rgb(placementRand.nextInt(255), placementRand.nextInt(255), placementRand.nextInt(255));
    		PointF loc = new PointF(x, y);   
    		addBall(c, mass, r, loc, (float) 0.48, 0);
    	}
    	
    	/*
		//for fun, add toast message
		Context context = getContext();
		CharSequence text = "Number of balls = " + currCount;
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();    	
		
		//for fun lets try the status bar notification
		if (currCount > 100) showStatus();
		*/
    }	//addRandomBall
    

    /**
     * Private to this class, adds a new ball instance to the array of balls
     * @param color
     * @param mass
     * @param radius
     * @param loc
     * @param sx
     * @param sy
     */
    private void addBall(int color, float mass, float radius, PointF loc, float sx, float sy)
    {
    	if (currCount < MAXBALLS)
    	{
    		//TODO read from gameOptions
    		ballArray[currCount] = new Ball(color, mass, radius, loc, sx, sy);
    		currCount++;
    	}
    	invalidate();	//redraw balls
    }   //addBall
    
    
    /**
     * called from main activity to return to the original population of balls
     */
    public void deleteAllButFirstBalls()
    {
    	clippedScaleFactor = 1.0f;
    	while (currCount > 2)
    	{
    		ballArray[currCount-1] = null;
    		currCount--;
    	}
        for (int i=0; i<currCount; i++ )
        {
        	ballArray[i].radius = ballArray[i].radiusBase*clippedScaleFactor;
        }    	
    	invalidate();	//redraw balls
    }	//deleteAllButFirstBalls
    	
    
    
    /**
     * {@inheritDoc}
     * called by invalidate()
     * todo maybe idea of 1)grab ball and mark as the victim, 2) drag away, 3) release
     * 4) when victim collides the explode it
     * todo is there a way to dynamically create a bitmap from the current ball ?
     */    
    @Override
    protected void onDraw(Canvas canvas) 
    {
        super.onDraw(canvas);
        //Log.d("BallView", "onDraw this " +this +":" + mX + ":" + mY);
        
        if (bShakeItUpInPlay)
        {
        	long curTime = System.currentTimeMillis();
        	if (curTime > (mShakeCountdownCurTime + mShakeCountdown))
        	{
        		if (massGravity < 0.0)
        			massGravity = -massGravity;	//restore back to normal attractive gravity
        		bShakeItUpInPlay = false;
        	}
        }
        for (int i=0; i<currCount; i++ )
        {
        	canvas.drawCircle(ballArray[i].x, ballArray[i].y, ballArray[i].radius, ballArray[i].mPaint);
        }
    } 	//onDraw
    

    /**
     * called from main activity, ImpactPhysics, in the OnPause handler
     */
    public void OnPauseProxy()
    {
    	mTmr.cancel(); //kill\release timer (our only background thread)
    	mTmr = null;
    	mTsk = null;
    }	//OnPauseProxy
    

    /**
     * called from main activity, ImpactPhysics, in the OnResume handler
     * @note is there a way to more formally request/command this view be the recipient of the OnResume callback?
     */
    public void OnResumeProxy()
    {
        mTmr = new Timer(); 
        mTsk = new TimerTask() 
        {
        	//TODO I am a little unclear on this usage
        	//anonymous method  
			public void run() 
			{
				//if debugging with external device, 
				//  a cat log viewer will be needed on the device
	            for (int i=0; i<currCount-1; ++i)
	                for (int j=i+1; j<currCount; ++j)
	                    if (ballArray[i].interact(ballArray[j],pThis)) // true = delete b[j]
	                    {
	                        ballArray[j] = ballArray[currCount-1];
	                        ballArray[--currCount] = null;
	                        //clearAll = true;
	                    }
	            for (int i=0; i<currCount; ++i)    	
	            	ballArray[i].update(pThis);
	            
	            if (my!=-17)              // mouse is dragging
	            {
	                ballArray[currentBall].vx = (mx-ballArray[currentBall].x)/10;
	                ballArray[currentBall].vy = (my-ballArray[currentBall].y)/10;
	            }
	            
				//redraw ball. Must run in background thread to prevent thread lock.
				RedrawHandler.post(new Runnable() 
				{
				    public void run() 
				    {	
			    		invalidate();
				    }
				});
				
			}
		}; // TimerTask

		//task, long delay, long period
        mTmr.schedule(mTsk,speed, speed); //start timer
    } //OnResumeProxy
    
    //===============================================
    //code from original Impact.java mostly untouched
    //===============================================
    
  
    /**
     * This returns the index of the ball nearest to x,y excluding ex.  (wrap is not taken into account).
     * @param x
     * @param y
     * @param ex
     * @return		index of the ball nearest to x,y excluding ex.  (wrap is not taken into account)
     */
    int nearestBall(float x, float y, int ex) 
    {
        double d=1e20,t; int j=0;
        for (int i=0; i<currCount; ++i)
        {
            t = Ball.hypot(x - ballArray[i].x, y - ballArray[i].y);
            if (t < d && i!=ex)
            {
                d = t; j = i;
            }
        }
        return j;
    }	//nearestBall
    

    /**
     * This causes the current ball to orbit the other ball nearest to it.
     */
    void orbit() 
    {
        int a=currentBall,b;
        if (a>=currCount) a=currCount-1;
        if (a<0) return;
        b = nearestBall((int)ballArray[a].x, (int)ballArray[a].y, a);
        if (b==a) return;

        double d,m,dx,dy,t;
        d = Ball.hypot(ballArray[a].x-ballArray[b].x,ballArray[a].y-ballArray[b].y);
        if (d<1e-20) return;        // too close
        m = ballArray[a].mass + ballArray[b].mass;
        if (m<1e-100) return;       // too small
        t = Math.sqrt(massGravity/m)/d;
        dy = t*(ballArray[a].x - ballArray[b].x);   // perpedicular direction vector
        dx = t*(ballArray[b].y - ballArray[a].y);

        ballArray[a].vx = (float) (ballArray[b].vx - dx*ballArray[b].mass);
        ballArray[a].vy = (float) (ballArray[b].vy - dy*ballArray[b].mass);
        ballArray[b].vx += dx*ballArray[a].mass;
        ballArray[b].vy += dy*ballArray[a].mass;
    }	//orbit
    
    /**
     * This adjusts the frame of reference so that the total momentum becomes zero.
     */
    void zeroMomentum() 
    {
        double mx=0,my=0,M=0;
        for (int i=0; i<currCount; ++i)
        {
            mx += ballArray[i].vx * ballArray[i].mass;
            my += ballArray[i].vy * ballArray[i].mass;
            M += ballArray[i].mass;
        }
        if (M != 0)
            for (int i=0; i<currCount; ++i)
            {
                ballArray[i].vx -= mx/M;
                ballArray[i].vy -= my/M;
            }
    }	//zeroMomentum

    /**
     * This adjusts the centroid to the center of the canvas.
     * Note, the "while" loops here could be simply use %= but some interpreters have bugs with %=.
     */
    void center() 
    {
        double x,y,cx=0,cy=0,M=0;
        for (int i=0; i<currCount; ++i)
        {
            x = ballArray[i].x;  y = ballArray[i].y;
            if (bWrap)    // if wrap, convert the top 1/4 to negative
            {
                if (x > mScrWidth*0.75) x -= mScrWidth;
                if (y > mScrHeight*0.75) y -= mScrHeight;
            }
            cx += ballArray[i].x * ballArray[i].mass;
            cy += ballArray[i].y * ballArray[i].mass;
            M += ballArray[i].mass;
        }
        if (M != 0)
            for (int i=0; i<currCount; ++i)
            {
                ballArray[i].x += mScrWidth/2 - cx/M;
                ballArray[i].y += mScrHeight/2 - cy/M;
                while (ballArray[i].x < 0) ballArray[i].x += mScrWidth;
                while (ballArray[i].x > mScrWidth) ballArray[i].x -= mScrWidth;
                while (ballArray[i].y < 0) ballArray[i].y += mScrHeight;
                while (ballArray[i].y > mScrHeight) ballArray[i].y -= mScrHeight;
            }
    }	//center

    /**
     * for fun lets try the status bar notification
     */
    void showStatus()
    {
    	//get ref to NotificationManager
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) pContext.getSystemService(ns);
    	
    	//instantiate it
    	int icon = R.drawable.notification_icon;
    	CharSequence tickerText = "balls = "+currCount;
    	long when = System.currentTimeMillis();

    	Notification notification = new Notification(icon, tickerText, when);
    	
    	//Define the notification's message and PendingIntent: 
    	Context context = pContext.getApplicationContext();
    	CharSequence contentTitle = "ImpactPhysics";
    	CharSequence contentText = "Ball status";
    	Intent notificationIntent = new Intent();
    	PendingIntent contentIntent = PendingIntent.getActivity(pContext, 0, notificationIntent, 0);

    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    	
    	//Pass the Notification to the NotificationManager: 
    	mNotificationManager.notify(HELLO_ID, notification);
    }
    
}	//AnimatorView

