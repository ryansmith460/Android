package com.example.rysm4200.androidapp;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;


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
    int numberOfRegions = 0;
    public byte regions[] = new byte[128];

    Uri source;

    Bitmap bitmapMaster;
    Canvas canvasMaster;
    Bitmap bitmapDrawingPane;
    Canvas canvasDrawingPane;

    projectPt startPt;

    ImageView imageResult, imageDrawingPane;

    final int RQS_IMAGE1 = 1;

    int GET_COORDINATES_ID;
    ImageView whiteboardImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_selection);

        whiteboardImageView = (ImageView)findViewById(R.id.whiteboardImageView);
        Bundle extras = getIntent().getExtras();
        int [] intColors  = extras.getIntArray("COLORS");
        int width = extras.getInt("WIDTH");
        int height = extras.getInt("HEIGHT");

        //Assign image to Region selection activity
        Bitmap bmpImage = Bitmap.createBitmap(intColors, width, height, Bitmap.Config.ARGB_8888);

        whiteboardImageView.setImageBitmap(bmpImage);
        whiteboardImageView.setOnTouchListener(listener);

        imageResult = (ImageView)findViewById(R.id.result);
        imageDrawingPane = (ImageView)findViewById(R.id.drawingpane);
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


    public void eraseButtonHandler(View view) {
        //Only send data for the nRegions that were selected
        byte resultRegions[] = new byte[numberOfRegions*8];

        for (int i = 0; i < numberOfRegions; i++) {
            resultRegions[8*i] = regions[8*i];
            resultRegions[8*i+1] = regions[8*i+1];
            resultRegions[8*i+2] = regions[8*i+2];
            resultRegions[8*i+3] = regions[8*i+3];
            resultRegions[8*i+4] = regions[8*i+4];
            resultRegions[8*i+5] = regions[8*i+5];
            resultRegions[8*i+6] = regions[8*i+6];
            resultRegions[8*i+7] = regions[8*i+7];
        }
        Intent data = new Intent();
        data.putExtra("Coordinates", resultRegions);
        data.putExtra("nRegions", numberOfRegions);
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
        //Report relative to imageview
        int [] imgViewLocation = new int [2];
        whiteboardImageView.getLocationOnScreen(imgViewLocation);

        regions[(8*numberOfRegions)] = (byte)((((int)(lowX-imgViewLocation[0]))&0xFF00)>> 8);
        regions[(8*numberOfRegions)+1] =  (byte)((int)(lowX-imgViewLocation[0])&0xFF);
        regions[(8*numberOfRegions) + 2] = (byte)((((int)(lowY-imgViewLocation[1]))&0xFF00)>> 8);
        regions[(8*numberOfRegions) + 3] = (byte)((int)(lowY-imgViewLocation[1])&0xFF);
        regions[(8*numberOfRegions) + 4] = (byte)((((int)(highX-imgViewLocation[0]))&0xFF00)>> 8);
        regions[(8*numberOfRegions) + 5] = (byte)((int)(highX-imgViewLocation[0])&0xFF);
        regions[(8*numberOfRegions) + 6] = (byte)((((int)(highY-imgViewLocation[1]))&0xFF00)>> 8);
        regions[(8*numberOfRegions) + 7] = (byte)((int)(highY-imgViewLocation[1])&0xFF);
        numberOfRegions++;
    }

    View.OnTouchListener listener = new View.OnTouchListener() {

        public boolean onTouch(View whiteboardImageView, MotionEvent e) {

            int whiteX = whiteboardImageView.getWidth();
            int whiteY = whiteboardImageView.getHeight();
            bitmapDrawingPane = Bitmap.createBitmap(whiteX, whiteY, Bitmap.Config.ARGB_8888);
            canvasDrawingPane = new Canvas(bitmapDrawingPane);

            int action = e.getAction();
            int x = (int) e.getX();
            int y = (int) e.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    //store the X value when the user's finger was pressed down
                    m_downXValue = e.getX();
                    m_downYValue = e.getY();
                    startPt = projectXY((ImageView)whiteboardImageView, bitmapDrawingPane, x, y);
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                    drawOnRectProjectedBitMap((ImageView)whiteboardImageView, bitmapDrawingPane, x, y);
                    break;
                case MotionEvent.ACTION_UP: {
                    //store the X value when the user's finger was pressed down
                    m_upXValue = e.getX();
                    m_upYValue = e.getY();
                    drawOnRectProjectedBitMap((ImageView)whiteboardImageView, bitmapDrawingPane, x, y);
                    finalizeDrawing();
                    break;
                }
            }
            return true;

        }
    };

    class projectPt{
        int x;
        int y;

        projectPt(int tx, int ty){
            x = tx;
            y = ty;
        }
    }

    private projectPt projectXY(ImageView iv, Bitmap bm, int x, int y){
        if(x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight()){
            //outside ImageView
            return null;
        }else{
            int projectedX = (int)((double)x * ((double)bm.getWidth()/(double)iv.getWidth()));
            int projectedY = (int)((double)y * ((double)bm.getHeight()/(double)iv.getHeight()));

            return new projectPt(projectedX, projectedY);
        }
    }

    private void drawOnRectProjectedBitMap(ImageView iv, Bitmap bm, int x, int y){
        if(x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight()){
            //outside ImageView
            return;
        }else{
            int projectedX = (int)((double)x * ((double)bm.getWidth()/(double)iv.getWidth()));
            int projectedY = (int)((double)y * ((double)bm.getHeight()/(double)iv.getHeight()));

            //clear canvasDrawingPane
            canvasDrawingPane.drawColor(Color.TRANSPARENT, Mode.CLEAR);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(3);
            canvasDrawingPane.drawRect(startPt.x, startPt.y, projectedX, projectedY, paint);
            imageDrawingPane.setImageBitmap(bitmapDrawingPane);
        }
    }

    private void finalizeDrawing(){
        canvasMaster.drawBitmap(bitmapDrawingPane, 0, 0, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap tempBitmap;

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case RQS_IMAGE1:
                    source = data.getData();

                    try {
                        //tempBitmap is Immutable bitmap,
                        //cannot be passed to Canvas constructor
                        tempBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(source));

                        Config config;
                        if(tempBitmap.getConfig() != null){
                            config = tempBitmap.getConfig();
                        }else{
                            config = Config.ARGB_8888;
                        }

                        //bitmapMaster is Mutable bitmap
                        bitmapMaster = Bitmap.createBitmap(tempBitmap.getWidth(), tempBitmap.getHeight(), config);

                        canvasMaster = new Canvas(bitmapMaster);
                        canvasMaster.drawBitmap(tempBitmap, 0, 0, null);

                        imageResult.setImageBitmap(bitmapMaster);

                        //Create bitmap of same size for drawing
                        bitmapDrawingPane = Bitmap.createBitmap(tempBitmap.getWidth(), tempBitmap.getHeight(), Config.ARGB_8888);
                        canvasDrawingPane = new Canvas(bitmapDrawingPane);
                        imageDrawingPane.setImageBitmap(bitmapDrawingPane);


                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    break;
            }
        }
    }
}
