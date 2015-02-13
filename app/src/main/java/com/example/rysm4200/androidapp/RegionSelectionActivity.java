package com.example.rysm4200.androidapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.content.ClipData;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.view.DragEvent;
import android.content.ClipDescription;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View.OnLongClickListener;
import android.view.MotionEvent;


public class RegionSelectionActivity extends Activity {

    //variables for motion events
    float m_downXValue = 0;
    float m_downYValue = 0;
    float m_upXValue = 0;
    float m_upYValue = 0;
    float lowX = 0;
    float lowY = 0;
    float highX = 0;
    float highY = 0;
    boolean touchEvent = false;
    int numberOfRegions = 0;
    public int regions[] = new int[64];


    //
    int GET_COORDINATES_ID;
    ImageView whiteboardImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_selection);
        whiteboardImageView = (ImageView)findViewById(R.id.whiteboardImageView);
        whiteboardImageView.setOnTouchListener(listener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_region_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void Init(int get_coordinates_id) {
        //Define the get_coordinates id
        GET_COORDINATES_ID = get_coordinates_id;
    }

    public void setWhiteboardImage(Bitmap bmpImage) {
    }

    private int[] getRegions() {



        //int[] regionStub = {67, 78};
        return regions;
    }

    public void eraseButtonHandler(View view) {
        Intent data = new Intent();
        data.putExtra("Coordinates", getRegions());
        setResult(RESULT_OK, data);
        finish();
    }

    //used to go back to main screen from region selection
    public void goBackButtonHandler(View view) {
        //Use the Main Activity to exit this screen without erasing regions
        finish();
    }

    //used to add region that is currently drawn
    public void saveSelectionButtonHandler(View view)
    {
        if (m_downXValue < m_upXValue)
        {
            lowX = m_downXValue;
            highX = m_upXValue;
        }
        else
        {
            lowX = m_upXValue;
            highX = m_downXValue;
        }
        if (m_downYValue < m_upYValue)
        {
            lowY = m_downYValue;
            highY = m_upYValue;
        }
        else
        {
            lowY = m_upYValue;
            highY = m_downYValue;
        }
        regions[(4*numberOfRegions)] = (int)lowX;
        regions[(4*numberOfRegions) + 1] = (int)lowY;
        regions[(4*numberOfRegions) + 2] = (int)highX;
        regions[(4*numberOfRegions) + 3] = (int)highY;
        numberOfRegions++;
    }

    //



    // Defines the one method for the interface, which is called when the View is long-clicked
    //public boolean onLongClick(View v) {


    View.OnTouchListener listener = new View.OnTouchListener() {

        public boolean onTouch(View whiteboardImageView, MotionEvent e) {
            touchEvent = true;
            if (touchEvent == true) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        //store the X value when the user's finger was pressed down
                        m_downXValue = e.getX();
                        m_downYValue = e.getY();
                        Log.d("down values", "down x:" +m_downXValue);
                        Log.d("down values", "down y:" +m_downYValue);

                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        //store the X value when the user's finger was pressed down
                        m_upXValue = e.getX();
                        m_upYValue = e.getY();

                        break;
                    }
                }
            }
            return true;

        }
    };
}
