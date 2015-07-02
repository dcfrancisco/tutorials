package com.korsbergtools.ImpactPhysics;

import android.graphics.Paint;
import android.graphics.PointF;


/**
 * The class representing each ball on the screen
 * @author Ed Korsberg
 * 		Original code from John Henckel, henckel@vnet.ibm.com June 1996, 
 * 		(C)  COPYRIGHT International Business Machines Corp. 1996
 *
 */
public class Ball
{
	public android.graphics.PointF mBallPos;
	public final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	public PointF loc;
	float x,y;            // location

	float radiusBase;         // radius, 
	float radius;         // radius, working
	float vx,vy;          // velocity
	int color;            // color
	float mass;           // mass
	boolean hit;          // scratch field
	float ox,oy;          // old location (for smooth redraw)
	final float vmin = (float) 1e-20;      // a weak force to prevent overlapping
	
	/**
	 * Constructor
	 * @param colorx	the color to draw for this ball
	 * @param massx		the mass of this ball
	 * @param radiusx	the radius of this ball
	 * @param loc		initial location
	 * @param sx		initial x velocity
	 * @param sy		initial y velocity
	 */
	Ball(int colorx, float massx, float radiusx, android.graphics.PointF loc, float sx, float sy) 
	{
	    mass=massx; 
	    this.loc = loc;
	    x=loc.x; y=loc.y; 
	    radius=(float) (radiusx-0.5); 
	    radiusBase = radius;
	    color=colorx;
	    if (radius<0.5) radius = (float) Math.min(Math.sqrt(Math.abs(mass)),Math.min(x,y));
	    if (radius<0.5) radius=(float) 0.5;

	    vx=sx; vy=sy;
	    mBallPos = new android.graphics.PointF();
	    
	    mPaint.setColor(color); //not transparent. color is green
	    mPaint.setStyle(Paint.Style.FILL);// STROKE); 
	    //mPaint.setStrokeWidth(3);
	}	//Ball
	
	
	/**
	 * This updates a ball according to the physical universe.
	 * The reason I exempt a ball from gravity during a hit is 
	 * to simulate "at rest" equilibrium when the ball is resting on the floor or on another ball.
	 * @param a	The view onto which this is drawn
	 */
	public void update(AnimatorView a) 
	{
	    x += vx;
	    if (x+radius > a.mScrWidth)
	        if (a.bWrap) x -= a.mScrWidth;
	        else
	        {
	            if (vx > 0) vx *= a.restitution;          // restitution
	            vx = -Math.abs(vx)-vmin;        // reverse velocity
	            hit = true;
	            //  Check if location is completely off screen
	            if (x-radius > a.mScrWidth) x = a.mScrWidth + radius;
	        }
	    if (x-radius < 0)
	        if (a.bWrap) x += a.mScrWidth;
	        else
	        {
	            if (vx < 0) vx *= a.restitution;
	            vx = Math.abs(vx)+vmin;
	            hit = true;
	            if (x+radius < 0) x = -radius;
	        }
	    y += vy;
	    if (y+radius > a.mScrHeight)
	        if (a.bWrap) y -= a.mScrHeight;
	        else
	        {
	            if (vy > 0) vy *= a.restitution;
	            vy = -Math.abs(vy)-vmin;
	            hit = true;
	            if (y-radius > a.mScrHeight) y = a.mScrHeight + radius;
	        }
	    if (y-radius < 0)
	        if (a.bWrap) y += a.mScrHeight;
	        else
	        {
	            if (vy < 0) vy *= a.restitution;
	            vy = Math.abs(vy)+vmin;
	            hit = true;
	            if (y+radius < 0) y = -radius;
	        }
	    if (a.friction > 0 && mass != 0)            // viscosity
	    {
	        float t = (float) (100/(100 + a.friction*hypot(vx,vy)*radius*radius/mass));
	        vx *= t; vy *= t;
	    }
	    if (!hit) 
	    {
	    	// if not hit, exert gravity
	    	//note this is updated from original, takes gravity from accelerometer
	    	vx -= a.vertGravity;    
	    	vy += a.horzGravity;   
	    }
	    hit = false;                // reset flag
	}	//update
	
	
	
	/**
	 * This computes the interaction of two balls, either collision or gravitational force.
	 * @param b
	 * @param a
	 * @return		TRUE if ball b should be deleted.
	 */
	public boolean interact(Ball b, AnimatorView a) 
	{
	    float p = b.x - x;
	    float q = b.y - y;
	    if (a.bWrap)                         // wrap around, use shortest distance
	    {
	        if (p > a.mScrWidth/2) p-=a.mScrWidth;
	        else if (p < -a.mScrWidth/2) p+=a.mScrWidth;
	        if (q > a.mScrHeight/2) q-=a.mScrHeight;
	        else if (q < -a.mScrHeight/2) q+=a.mScrHeight;
	    }
	    float h2 = p*p + q*q;
	    if (a.bCollide)                        // collisions enabled
	    {
	        float h = (float) Math.sqrt(h2);
	        if (h < radius+b.radius)                  // HIT
	        {
	            hit = b.hit = true;
	            if (a.bMush)                     // mush together
	            {
	                if (mass < b.mass) 
	                {
	                	color=b.color;           // color
	                }
	                if (b.mass+mass != 0)
	                {
	                    float t = b.mass/(b.mass+mass);
	                    x += p*t;
	                    y += q*t;
	                    vx += (b.vx - vx)*t;
	                    vy += (b.vy - vy)*t;
	                    if (x > a.mScrWidth) x -= a.mScrWidth;
	                    if (x < 0) x += a.mScrWidth;
	                    if (y > a.mScrHeight) y -= a.mScrHeight;
	                    if (y < 0) y += a.mScrHeight;
	                }
	                mass += b.mass;
	                radius = hypot(b.radius,radius);
	                return true;         // delete b
	            }
	            else if (h > 1e-10)
	            {
	                //  Compute the elastic collision of two balls.
	                //  The math involved here is not for the faint of heart!
	
	                float v1,v2,r1,r2,s,t,v;
	                p /= h;  q /= h;              // normalized impact direction
	                v1 = vx*p + vy*q;
	                v2 = b.vx*p + b.vy*q;         // impact velocity
	                r1 = vx*q - vy*p;
	                r2 = b.vx*q - b.vy*p;         // remainder velocity
	                if (v1<v2) return false;
	                s = mass + b.mass;            // total mass
	                if (s==0) return false;
	
	                t = (v1*mass + v2*b.mass)/s;
	                v = (float) (t + a.restitution*(v2 - v1)*b.mass/s);
	                vx = v*p + r1*q;
	                vy = v*q - r1*p;
	                v = (float) (t + a.restitution*(v1 - v2)*mass/s);
	                b.vx = v*p + r2*q;
	                b.vy = v*q - r2*p;
	            }
	        }
	    }	//if collide
	    
	    if (a.massGravity != 0 && h2 > 1e-10 && !hit && !b.hit)        // gravity is enabled
	    {
	        float dv;
	        dv = (float) (a.massGravity*b.mass/h2);
	        vx += dv*p;
	        vy += dv*q;
	        dv = (float) (a.massGravity*mass/h2);
	        b.vx -= dv*p;
	        b.vy -= dv*q;
	    }
	    return false;
	}	//interact
	
	/**
	 * Static math function to compute the hypotenuse of a triangle given two variables
	 * @param x		Length or width
	 * @param y		Length or width
	 * @return		hypotenuse
	 */
    public static float hypot(float x, float y) 
    {
        return (float) Math.sqrt(x*x + y*y);
    }
}	//Ball

