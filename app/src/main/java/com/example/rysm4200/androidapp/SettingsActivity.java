package com.example.rysm4200.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Rect;

/**
 * Created by clairerichardson on 2/17/15.
 */
public class SettingsActivity extends Activity {

    float m_downXValue = 0;
    float m_downYValue = 0;
    float m_upXValue = 0;
    float m_upYValue = 0;
    float lowX = 0;
    float lowY = 0;
    float highX = 0;
    float highY = 0;
    boolean touchEvent = false;
    public byte whiteboard [] = new byte[8];

    int GET_BOARD_COORDINATES_ID;
    ImageView settingsWhiteboardImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsWhiteboardImageView = (ImageView)findViewById(R.id.settingsWhiteboardImageView);
        Bundle extras = getIntent().getExtras();
        int [] intColors  = extras.getIntArray("COLORS");
        int width = extras.getInt("WIDTH");
        int height = extras.getInt("HEIGHT");

        //Assign image to Region selection activity
        Bitmap bmpImage = Bitmap.createBitmap(intColors, width, height, Bitmap.Config.ARGB_8888);

        settingsWhiteboardImageView.setImageBitmap(bmpImage);
        settingsWhiteboardImageView.setOnTouchListener(listener);
    }


    public void goBackSettingsButtonHandler(View view) {
        //Use the Main Activity to exit this screen without erasing regions
        finish();
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

    public void Init(int get_board_coordinates_id) {
        //Define the get_board_coordinates id
        GET_BOARD_COORDINATES_ID = get_board_coordinates_id;
    }


    public void exitSaveButtonHandler(View view) {
        Intent data = new Intent();
        data.putExtra("Coordinates", whiteboard);
        setResult(RESULT_OK, data);
        finish();
    }

    //used to go back to main screen from region selection
    public void goBackButtonHandler(View view) {
        //Use the Main Activity to exit this screen without erasing regions
        finish();
    }

    //used to add region that is currently drawn
    public void saveButtonHandler(View view)
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
        //Report relative to imageview
        int [] imgViewLocation = new int [2];
        settingsWhiteboardImageView.getLocationOnScreen(imgViewLocation);

        whiteboard[0] = (byte)((((int)(lowX-imgViewLocation[0]))&0xFF00)>> 8);
        whiteboard[1] =   (byte)((int)(lowX-imgViewLocation[0])&0xFF);
        whiteboard[2] =  (byte)((((int)(lowY-imgViewLocation[1]))&0xFF00)>> 8);
        whiteboard[3] = (byte)((int)(lowY-imgViewLocation[1])&0xFF);
        whiteboard[4] =  (byte)((((int)(highX-imgViewLocation[0]))&0xFF00)>> 8);
        whiteboard[5] = (byte)((int)(highX-imgViewLocation[0])&0xFF);
        whiteboard[6] = (byte)((((int)(highY-imgViewLocation[1]))&0xFF00)>> 8);
        whiteboard[7] = (byte)((int)(highY-imgViewLocation[1])&0xFF);
    }

    // Defines the one method for the interface, which is called when the View is long-clicked
    //public boolean onLongClick(View v) {

    View.OnTouchListener listener = new View.OnTouchListener() {

        public boolean onTouch(View settingsWhiteboardImageView, MotionEvent e) {
            touchEvent = true;
            if (touchEvent == true) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        //store the X value when the user's finger was pressed down
                        m_downXValue = e.getX();
                        m_downYValue = e.getY();

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

