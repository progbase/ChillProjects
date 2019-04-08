package com.example.chillcrawler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;
    private int ratio = 500;
    private JoystickListener joystickCallback;

    void setupDimensions() {
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth(), getHeight()) / 3;  //Establish a canvas
        hatRadius = Math.min(getWidth(), getHeight()) / 8;
    }

    private void drawJoystick(float newX, float newY) {
        if (getHolder().getSurface().isValid()) {
            //only if SurfaceView was created
            Canvas myCanvas = this.getHolder().lockCanvas(); //get access to Canvas
            Paint colors = new Paint();

            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //Clear Canvas

            float hypotenuse = (float) Math.sqrt(Math.pow(newX - centerX, 2) + Math.pow(newY - centerY, 2));
            float sin = (newY - centerY) / hypotenuse;
            float cos = (newX - centerX) / hypotenuse;
            colors.setARGB(255, 50, 50, 50); //set the color
            myCanvas.drawCircle(centerX, centerY, baseRadius, colors); //draw the base
            for (int i = 1; i <= (int) (baseRadius / ratio); i++) {
                colors.setARGB((150 / i), 255, 0, 0);
                myCanvas.drawCircle(newX - cos * hypotenuse * (ratio / baseRadius) * i, newY - sin * hypotenuse * (ratio / baseRadius) * i, i * (hatRadius * ratio / baseRadius), colors);
            }
            colors.setARGB(255, 0, 230, 118);
            myCanvas.drawCircle(newX, newY, hatRadius, colors);//draw hat
            getHolder().unlockCanvasAndPost(myCanvas); //print to SurfaceView
        }
    }

    public boolean onTouch(View v, MotionEvent e) {
        if (v.equals(this)) {
            float displacement = (float) Math.sqrt(Math.pow(e.getX() - centerX, 2) + Math.pow(e.getY() - centerY, 2));

            if (e.getAction() != e.ACTION_UP) { //while you hold the finger
                if (displacement < baseRadius) {
                    drawJoystick(e.getX(), e.getY());
                    joystickCallback.onJoystickMoved((e.getX() - centerX) / baseRadius, (e.getY() - centerY) / baseRadius, getId());
                } else {
                    float ratio = baseRadius / displacement;
                    float constrainedX = centerX + (e.getX() - centerX) * ratio;
                    float constrainedY = centerY + (e.getY() - centerY) * ratio;
                    drawJoystick(constrainedX, constrainedY); //constrain the hat
                    joystickCallback.onJoystickMoved((constrainedX - centerX) / baseRadius, (constrainedY - centerY) / baseRadius, getId());
                }
            } else {
                drawJoystick(centerX, centerY); //if you let go e.getAction() == e.ACTION_UP
                joystickCallback.onJoystickMoved(0, 0, getId());
            }
        }
        return true;
    }


    public interface JoystickListener   //joystick interface
    {
        void onJoystickMoved(float xPercent, float yPercent, int source); //change however u want, currently calculates percentage moved of total displacement, source is for the case of multiple joysticks
    }


    public JoystickView(Context context) {
        super(context);
        getHolder().addCallback(this); // surfaceholder  callback
        setOnTouchListener(this);// touch input
        if (context instanceof JoystickListener) {
            joystickCallback = (JoystickListener) context;
        }
    }

    public JoystickView(Context context, AttributeSet a, int style) {
        super(context, a, style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if (context instanceof JoystickListener) {
            joystickCallback = (JoystickListener) context;
        }
    }

    public JoystickView(Context context, AttributeSet a) {
        super(context, a);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if (context instanceof JoystickListener) {
            joystickCallback = (JoystickListener) context;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupDimensions();
        drawJoystick(centerX, centerY);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }


}


