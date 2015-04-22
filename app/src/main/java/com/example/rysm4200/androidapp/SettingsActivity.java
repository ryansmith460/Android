package com.example.rysm4200.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
    public byte whiteboard[] = new byte[8];
    int storedX = 0;
    int storedY = 0;

    Bitmap bitmapDrawingPane;
    Canvas canvasDrawingPane;

    ImageView imageDrawingPane;

    int GET_BOARD_COORDINATES_ID;
    ImageView settingsWhiteboardImageView;

    boolean debug = false;
    boolean save = false;
    int sensitivity = 51;

    RelativeLayout settingsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsWhiteboardImageView = (ImageView) findViewById(R.id.settingsWhiteboardImageView);
        Bundle extras = getIntent().getExtras();
        int[] intColors = extras.getIntArray("COLORS");
        whiteboard = extras.getByteArray("COORDINATES");
        int width = extras.getInt("WIDTH");
        int height = extras.getInt("HEIGHT");

        //Assign image to Region selection activity
        Bitmap bmpImage = Bitmap.createBitmap(intColors, width, height, Bitmap.Config.ARGB_8888);

        settingsWhiteboardImageView.setImageBitmap(bmpImage);
        settingsWhiteboardImageView.setOnTouchListener(listener);

        settingsLayout = (RelativeLayout) findViewById(R.id.settingslayout);
        settingsLayout.getViewTreeObserver().addOnGlobalLayoutListener(myOnGlobalLayoutListener);

        imageDrawingPane = (ImageView) findViewById(R.id.drawingpane);
    }

    OnGlobalLayoutListener myOnGlobalLayoutListener =
            new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (debug) {
                        m_downXValue = 250;
                        m_upXValue = 550;
                        m_downYValue = 350;
                        m_upYValue = 650;
                    }
                    //if not debug, request coordinates from bluetooth
                    if (!debug) {
                        //ask for coordinates from bluetooth

                        m_downXValue = (whiteboard[0] << 8) | (0x00FF & (int)whiteboard[1]);
                        m_upXValue = (whiteboard[4] << 8) | (0x00FF & (int)whiteboard[5]);
                        m_upYValue = (whiteboard[2] << 8) | (0x00FF & (int)whiteboard[3]);
                        m_downYValue = (whiteboard[6] << 8) | (0x00FF & (int)whiteboard[7]);

                        int lowerHeight = (settingsWhiteboardImageView.getHeight() / 2) - (3 * settingsWhiteboardImageView.getWidth() / 8);
                        int upperHeight = lowerHeight + (3 * settingsWhiteboardImageView.getWidth() / 4);
                        double picHeight = (double) upperHeight - (double) lowerHeight;
                        double ratio;

                        ratio = (double) settingsWhiteboardImageView.getWidth() / 320.0;
                        m_downXValue = m_downXValue*(float)ratio;

                        ratio = (double) settingsWhiteboardImageView.getWidth() / 320.0;
                        m_upXValue = m_upXValue*(float)ratio;

                        ratio = picHeight / 240.0;
                        m_downYValue = m_downYValue*(float)ratio + lowerHeight;

                        ratio = picHeight / 240.0;
                        m_upYValue = m_upYValue*(float)ratio + lowerHeight;
                    }


                    int whiteX = settingsWhiteboardImageView.getWidth();
                    int whiteY = settingsWhiteboardImageView.getHeight();
                    bitmapDrawingPane = Bitmap.createBitmap(whiteX, whiteY, Bitmap.Config.ARGB_8888);
                    imageDrawingPane.setImageBitmap(bitmapDrawingPane);
                    canvasDrawingPane = new Canvas(bitmapDrawingPane);
                    Paint paint = new Paint();
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.GREEN);
                    paint.setStrokeWidth(8);
                    canvasDrawingPane.drawRect(m_downXValue, m_downYValue, m_upXValue, m_upYValue, paint);
                    imageDrawingPane.setImageBitmap(bitmapDrawingPane);
                }
            };

    public void goBackSettingsButtonHandler(View view) {
        //Use the Main Activity to exit this screen without erasing regions
        Intent data = new Intent();
        data.putExtra("Coordinates", whiteboard);
        setResult(RESULT_OK, data);
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


    /*public void exitSaveButtonHandler(View view) {
        Intent data = new Intent();
        data.putExtra("Coordinates", whiteboard);
        setResult(RESULT_OK, data);
        finish();
    }*/


    //used to add region that is currently drawn
    public void saveButtonHandler(View view) {
        save = true;
        drawOnRectProjectedBitMap((ImageView) settingsWhiteboardImageView, bitmapDrawingPane, storedX, storedY, position);
        if (m_downXValue < m_upXValue) {
            lowX = m_downXValue;
            highX = m_upXValue;
        } else {
            lowX = m_upXValue;
            highX = m_downXValue;
        }
        if (m_downYValue < m_upYValue) {
            lowY = m_downYValue;
            highY = m_upYValue;
        } else {
            lowY = m_upYValue;
            highY = m_downYValue;
        }
        //Report relative to imageview
        int[] imgViewLocation = new int[2];
        settingsWhiteboardImageView.getLocationOnScreen(imgViewLocation);

        int lowerHeight = (settingsWhiteboardImageView.getHeight()/2)-(3*settingsWhiteboardImageView.getWidth()/8);
        int upperHeight = lowerHeight + (3*settingsWhiteboardImageView.getWidth()/4);
        double picHeight = (double)upperHeight - (double)lowerHeight;
        double ratio;
        double value;

        ratio = (double)settingsWhiteboardImageView.getWidth()/320.0;
        value = lowX/ratio;
        value = ((int) value & 0xFF00) >> 8;
        whiteboard[0] = (byte)value;

        ratio = (double)settingsWhiteboardImageView.getWidth()/320.0;
        value = lowX/ratio;
        value = (int) value & 0xFF;
        whiteboard[1] = (byte)value;

        ratio = picHeight/240.0;
        value = (lowY - lowerHeight)/ratio;
        value = ((int) value & 0xFF00) >> 8;
        whiteboard[2] = (byte)value;

        ratio = picHeight/240.0;
        value = (lowY - lowerHeight)/ratio;
        value = (int) value & 0xFF;
        whiteboard[3] = (byte)value;

        ratio = (double)settingsWhiteboardImageView.getWidth()/320.0;
        value = highX/ratio;
        value = ((int) value & 0xFF00) >> 8;
        whiteboard[4] = (byte)value;

        ratio = (double)settingsWhiteboardImageView.getWidth()/320.0;
        value = highX/ratio;
        value = (int) value & 0xFF;
        whiteboard[5] = (byte)value;

        ratio = picHeight/240.0;
        value = (highY - (double)lowerHeight)/ratio;
        value = ((int) value & 0xFF00) >> 8;
        whiteboard[6] = (byte)value;

        ratio = picHeight/240.0;
        value = (highY - (double)lowerHeight)/ratio;
        value = (int) value & 0xFF;
        whiteboard[7] = (byte)value;

        //whiteboard[0] = (byte) ((((int) (lowX - imgViewLocation[0])) & 0xFF00) >> 8);
        //whiteboard[1] = (byte) ((int) (lowX - imgViewLocation[0]) & 0xFF);
        //whiteboard[2] = (byte) ((((int) (lowY - imgViewLocation[1])) & 0xFF00) >> 8);
        //whiteboard[3] = (byte) ((int) (lowY - imgViewLocation[1]) & 0xFF);
        //whiteboard[4] = (byte) ((((int) (highX - imgViewLocation[0])) & 0xFF00) >> 8);
        //whiteboard[5] = (byte) ((int) (highX - imgViewLocation[0]) & 0xFF);
        //whiteboard[6] = (byte) ((((int) (highY - imgViewLocation[1])) & 0xFF00) >> 8);
        //whiteboard[7] = (byte) ((int) (highY - imgViewLocation[1]) & 0xFF);
    }

    // Defines the one method for the interface, which is called when the View is long-clicked
    //public boolean onLongClick(View v) {

    int position = 0;

    View.OnTouchListener listener = new View.OnTouchListener() {

        public boolean onTouch(View settingsWhiteboardImageView, MotionEvent e) {
            int action = e.getAction();
            int xTouch = (int) e.getX();
            int yTouch = (int) e.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    //store the X value when the user's finger was pressed down
                    if ((Math.abs(xTouch - (int) m_downXValue) < sensitivity) && (Math.abs(yTouch - (int) m_downYValue) < sensitivity)) {
                        position = 1;
                    } else if ((Math.abs(xTouch - (int) m_upXValue) < sensitivity) && (Math.abs(yTouch - (int) m_downYValue) < sensitivity)) {
                        position = 2;
                    } else if ((Math.abs(xTouch - (int) m_upXValue) < sensitivity) && (Math.abs(yTouch - (int) m_upYValue) < sensitivity)) {
                        position = 3;
                    } else if ((Math.abs(xTouch - (int) m_downXValue) < sensitivity) && (Math.abs(yTouch - (int) m_upYValue) < sensitivity)) {
                        position = 4;
                    }else if ((Math.abs(xTouch - (int) m_downXValue) < sensitivity) && (((yTouch < m_downYValue) && (yTouch > m_upYValue)) || ((yTouch > m_downYValue) && (yTouch < m_upYValue)))) {
                        position = 5;
                    }else if ((Math.abs(xTouch - (int) m_upXValue) < sensitivity) && (((yTouch < m_downYValue) && (yTouch > m_upYValue)) || ((yTouch > m_downYValue) && (yTouch < m_upYValue)))){
                        position = 6;
                    }else if ((Math.abs(yTouch - (int) m_downYValue) < sensitivity) && (((xTouch < m_downXValue) && (xTouch > m_upXValue)) || ((xTouch > m_downXValue) && (xTouch < m_upXValue)))){
                        position = 7;
                    }else if ((Math.abs(yTouch - (int) m_upYValue) < sensitivity) && (((xTouch < m_downXValue) && (xTouch > m_upXValue)) || ((xTouch > m_downXValue) && (xTouch < m_upXValue)))){
                        position = 8;
                    }

                    else{
                        position = 0;
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    save = false;
                    drawOnRectProjectedBitMap((ImageView) settingsWhiteboardImageView, bitmapDrawingPane, xTouch, yTouch, position);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    //store the X value when the user's finger was pressed down
                    storedX = xTouch;
                    storedY = yTouch;
                    break;
                }
            }
            return true;
        }
    };

    private void drawOnRectProjectedBitMap(ImageView iv, Bitmap bm, int x, int y, int corner){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        if(!save) {
            paint.setColor(Color.RED);
        }
        else {
            paint.setColor(Color.GREEN);
        }
        paint.setStrokeWidth(8);
        int lowerHeight = (iv.getHeight()/2)-(3*iv.getWidth()/8);
        int upperHeight = lowerHeight + (3*iv.getWidth()/4);
        //clear canvasDrawingPane
        canvasDrawingPane.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if(x < 0 || y < lowerHeight || x > iv.getWidth() || y > upperHeight) {
            //outside ImageView
            if (x < 0) {
                x = 0;
            }
            if (y < lowerHeight) {
                y = lowerHeight;
            }
            if (x > iv.getWidth()) {
                x = iv.getWidth();
            }
            if (y > upperHeight) {
                y = upperHeight;
            }
        }
        if (corner == 1) {
            m_downXValue = x;
            m_downYValue = y;
        } else if (corner == 2) {
            m_downYValue = y;
            m_upXValue = x;
        } else if (corner == 3) {
            m_upXValue = x;
            m_upYValue = y;
        } else if (corner == 4) {
            m_downXValue = x;
            m_upYValue = y;
        } else if (corner == 5) {
            m_downXValue = x;
        } else if (corner == 6) {
            m_upXValue = x;
        } else if (corner == 7) {
            m_downYValue = y;
        } else if (corner == 8) {
            m_upYValue = y;
        }
        if (corner != 0) {
            canvasDrawingPane.drawRect(m_downXValue, m_downYValue, m_upXValue, m_upYValue, paint);
            imageDrawingPane.setImageBitmap(bitmapDrawingPane);
        }
    }
}