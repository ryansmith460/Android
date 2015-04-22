package com.example.rysm4200.androidapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
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

import java.util.UUID;

/**
 * Created by clairerichardson on 2/17/15.
 */
public class RobotActivity extends Activity {

    private BluetoothAdapter adapter = null;
    private BluetoothSocket socket = null;
    private static final UUID bT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "30:14:10:15:02:96";
    BluetoothThread bT;
    int numImagePts;
    int code;
    byte[] robotCoordinateBytes;

    float x1Robot = 0;
    float x2Robot = 0;
    float x3Robot = 0;
    float x4Robot = 0;
    float y1Robot = 0;
    float y2Robot = 0;
    float y3Robot = 0;
    float y4Robot = 0;
    float lowX = 0;
    float lowY = 0;
    float highX = 0;
    float highY = 0;
    boolean touchEvent = false;
    public byte robot[] = new byte[16];
    int storedX = 0;
    int storedY = 0;

    Bitmap bitmapDrawingPane;
    Canvas canvasDrawingPane;

    ImageView imageDrawingPane;

    int GET_ROBOT_COORDINATES_ID;
    ImageView robotImageView;

    boolean debug = false;
    boolean save = false;
    int sensitivity = 50;

    RelativeLayout robotLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot);
        robotImageView = (ImageView) findViewById(R.id.robotImageView);
        Bundle extras = getIntent().getExtras();
        int[] intColors = extras.getIntArray("COLORS");
        robot = extras.getByteArray("ROBOTCOORDINATES");

        int width = extras.getInt("WIDTH");
        int height = extras.getInt("HEIGHT");

        //Assign image to Region selection activity
        Bitmap bmpImage = Bitmap.createBitmap(intColors, width, height, Bitmap.Config.ARGB_8888);

        robotImageView.setImageBitmap(bmpImage);
        //robotImageView.setOnTouchListener(listener);

        robotLayout = (RelativeLayout) findViewById(R.id.robotlayout);
        robotLayout.getViewTreeObserver().addOnGlobalLayoutListener(myOnGlobalLayoutListener);

        imageDrawingPane = (ImageView) findViewById(R.id.drawingpane);

    }

    OnGlobalLayoutListener myOnGlobalLayoutListener =
            new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (debug) {
                        x1Robot = 50;
                        x2Robot = 0;
                        x3Robot = 0;
                        x4Robot = 50;
                        y1Robot = 0;
                        y2Robot = 0;
                        y3Robot = 50;
                        y4Robot = 50;

                        int lowerHeight = (robotImageView.getHeight() / 2) - (3 * robotImageView.getWidth() / 8);
                        int upperHeight = lowerHeight + (3 * robotImageView.getWidth() / 4);
                        double picHeight = (double) upperHeight - (double) lowerHeight;
                        double ratio;

                        ratio = (double) robotImageView.getWidth() / 320.0;
                        x1Robot = x1Robot*(float)ratio;
                        x2Robot = x2Robot*(float)ratio;
                        x3Robot = x3Robot*(float)ratio;
                        x4Robot = x4Robot*(float)ratio;

                        ratio = picHeight / 240.0;
                        y1Robot = y1Robot*(float)ratio + lowerHeight;
                        y2Robot = y2Robot*(float)ratio + lowerHeight;
                        y3Robot = y3Robot*(float)ratio + lowerHeight;
                        y4Robot = y4Robot*(float)ratio + lowerHeight;
                    }
                    //if not debug, request coordinates from bluetooth
                    if (!debug) {
                        //ask for coordinates from bluetooth
                        x1Robot = (robot[0] << 8) | (0x00FF & (int)robot[1]);
                        x2Robot = (robot[4] << 8) | (0x00FF & (int)robot[5]);
                        x3Robot = (robot[2] << 8) | (0x00FF & (int)robot[3]);
                        x4Robot = (robot[6] << 8) | (0x00FF & (int)robot[7]);
                        y1Robot = (robot[8] << 8) | (0x00FF & (int)robot[9]);
                        y2Robot = (robot[10] << 8) | (0x00FF & (int)robot[11]);
                        y3Robot = (robot[12] << 8) | (0x00FF & (int)robot[13]);
                        y4Robot = (robot[14] << 8) | (0x00FF & (int)robot[15]);

                        int lowerHeight = (robotImageView.getHeight() / 2) - (3 * robotImageView.getWidth() / 8);
                        int upperHeight = lowerHeight + (3 * robotImageView.getWidth() / 4);
                        double picHeight = (double) upperHeight - (double) lowerHeight;
                        double ratio;

                        ratio = (double) robotImageView.getWidth() / 320.0;
                        x1Robot = x1Robot*(float)ratio;
                        x2Robot = x2Robot*(float)ratio;
                        x3Robot = x3Robot*(float)ratio;
                        x4Robot = x4Robot*(float)ratio;

                        ratio = picHeight / 240.0;
                        y1Robot = y1Robot*(float)ratio + lowerHeight;
                        y2Robot = y2Robot*(float)ratio + lowerHeight;
                        y3Robot = y3Robot*(float)ratio + lowerHeight;
                        y4Robot = y4Robot*(float)ratio + lowerHeight;
                    }

                    int whiteX = robotImageView.getWidth();
                    int whiteY = robotImageView.getHeight();
                    bitmapDrawingPane = Bitmap.createBitmap(whiteX, whiteY, Bitmap.Config.ARGB_8888);
                    imageDrawingPane.setImageBitmap(bitmapDrawingPane);
                    canvasDrawingPane = new Canvas(bitmapDrawingPane);
                    Paint paint = new Paint();
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(8);
                    paint.setColor(Color.parseColor("#FF9933"));
                    canvasDrawingPane.drawLine(x1Robot, y1Robot, x3Robot, y3Robot, paint);
                    canvasDrawingPane.drawLine(x2Robot, y2Robot, x3Robot, y3Robot, paint);
                    canvasDrawingPane.drawLine(x2Robot, y2Robot, x4Robot, y4Robot, paint);
                    canvasDrawingPane.drawLine(x4Robot, y4Robot, x1Robot, y1Robot, paint);

                }
            };

    public void yesButtonHandler(View view) {
        //Use the Main Activity to exit this screen without erasing regions
        finish();
    }

    public void noButtonHandler(View view) {
        Intent data = new Intent();
        data.putExtra("robotCoordinates", robot);
        setResult(RESULT_OK, data);
        finish();
    //need to rerequest the robot outline and need to reopen the robot confirmation page
    }

    /* //KEEPING FOR REFERENCE
    public void refindRobotButtonHandler(View view) {
        //Use this to refind the robot & display the newly found robot

        Intent intent2 = new Intent();
        intent2.putExtra("ROBOTCOORDINATES",robotCoordinateBytes);

        //bluetooth thread for receiving new robot coordinates
        //new code number needed = 7
        // another bluetooth thread for the coordinates
        bT = new BluetoothThread();
        bT.settings();
        bT.InitBluetoothThread(socket, numImagePts);
        bT.start();
        bT.startSaving();
        code = 7;
        byte [] robotCoordinateRequestCode = {(byte)code};
        bT.sendData(robotCoordinateRequestCode);
        while (bT.getSaveStatus() == true);
        robotCoordinateBytes = bT.getRobotCoordinates();
        bT = null;

        Bundle extras = getIntent().getExtras();
        robot = extras.getByteArray("ROBOTCOORDINATES");

        x1Robot = (robot[0] << 8) | (0x00FF & (int)robot[1]);
        x2Robot = (robot[4] << 8) | (0x00FF & (int)robot[5]);
        x3Robot = (robot[2] << 8) | (0x00FF & (int)robot[3]);
        x4Robot = (robot[6] << 8) | (0x00FF & (int)robot[7]);
        y1Robot = (robot[8] << 8) | (0x00FF & (int)robot[9]);
        y2Robot = (robot[10] << 8) | (0x00FF & (int)robot[11]);
        y3Robot = (robot[12] << 8) | (0x00FF & (int)robot[13]);
        y4Robot = (robot[14] << 8) | (0x00FF & (int)robot[15]);


        int lowerHeight = (settingsWhiteboardImageView.getHeight() / 2) - (3 * settingsWhiteboardImageView.getWidth() / 8);
        int upperHeight = lowerHeight + (3 * settingsWhiteboardImageView.getWidth() / 4);
        double picHeight = (double) upperHeight - (double) lowerHeight;
        double ratio;

        ratio = (double) settingsWhiteboardImageView.getWidth() / 320.0;
        x1Robot = x1Robot*(float)ratio;
        x2Robot = x2Robot*(float)ratio;
        x3Robot = x3Robot*(float)ratio;
        x4Robot = x4Robot*(float)ratio;

        ratio = picHeight / 240.0;
        y1Robot = y1Robot*(float)ratio + lowerHeight;
        y2Robot = y2Robot*(float)ratio + lowerHeight;
        y3Robot = y3Robot*(float)ratio + lowerHeight;
        y4Robot = y4Robot*(float)ratio + lowerHeight;

        Paint paintRobot = new Paint();
        paintRobot.setStyle(Paint.Style.STROKE);
        paintRobot.setColor(Color.parseColor("#FF6600"));
        imageDrawingPane.setImageBitmap(bitmapDrawingPane);
        paintRobot.setStrokeWidth(8);
        canvasDrawingPane.drawLine(x1Robot, y1Robot, x2Robot, y2Robot, paintRobot);
        canvasDrawingPane.drawLine(x2Robot, y2Robot, x3Robot, y3Robot, paintRobot);
        canvasDrawingPane.drawLine(x3Robot, y3Robot, x4Robot, y4Robot, paintRobot);
        canvasDrawingPane.drawLine(x4Robot, y4Robot, x1Robot, y1Robot, paintRobot);
    }*/

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

    public void Init(int get_robot_coordinates_id) {
        //Define the get_board_coordinates id
        GET_ROBOT_COORDINATES_ID = get_robot_coordinates_id;
    }

   private void drawOnRectProjectedBitMap(ImageView iv, Bitmap bm, int x, int y, int corner){
        Paint paintRobot = new Paint();
        paintRobot.setStyle(Paint.Style.STROKE);
        paintRobot.setColor(Color.parseColor("#FF6600"));
        imageDrawingPane.setImageBitmap(bitmapDrawingPane);
        paintRobot.setStrokeWidth(8);
            imageDrawingPane.setImageBitmap(bitmapDrawingPane);
            paintRobot.setColor(Color.parseColor("#FF6600"));
            canvasDrawingPane.drawLine(x1Robot, y1Robot, x2Robot, y2Robot, paintRobot);
            canvasDrawingPane.drawLine(x2Robot, y2Robot, x3Robot, y3Robot, paintRobot);
            canvasDrawingPane.drawLine(x3Robot, y3Robot, x4Robot, y4Robot, paintRobot);
            canvasDrawingPane.drawLine(x4Robot, y4Robot, x1Robot, y1Robot, paintRobot);
        }


    int position = 0;

    View.OnTouchListener listener = new View.OnTouchListener() {

        public boolean onTouch(View robotImageView, MotionEvent e) {

            return true;
        }
    };
}

