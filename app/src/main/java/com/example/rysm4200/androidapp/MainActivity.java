package com.example.rysm4200.androidapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import java.io.IOException;
import android.view.View;
import android.graphics.Bitmap;
import java.util.UUID;

public class MainActivity extends Activity {
    //Bluetooth
    private BluetoothAdapter adapter = null;
    private BluetoothSocket socket = null;
    private static final UUID bT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "30:14:10:15:02:96";
    BluetoothThread bT;
    public boolean bluetoothRunning = false;

    //Hard-coded image size
    int width = 320;
    int height = 240;
    int numImagePts = width * height * 3;

    //Image variables
    int alpha = 255;
    int[] imageIntegers = new int[numImagePts];
    byte[] imageBytes = new byte[numImagePts];
    int[] intColors = new int[numImagePts / 3];

    //Region Selection Activity
    RegionSelectionActivity regionSelection;
    int GET_COORDINATES_ID = 1;

    //Settings Activity
    SettingsActivity settingsActivity;
    int GET_BOARD_COORDINATES_ID = 2;

    //Erase All
    boolean eraseAll = false;
    int eraseAllCode = 1;

    //Emergency Stop
    boolean emergencyStop = true;
    int emergencyStopCode = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Configure main screen
        //getActionBar().setIcon(R.drawable.eraser);

        //Set up Bluetooth Communication
        adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bTDevice = adapter.getRemoteDevice(address);

        try
        {
            socket = bTDevice.createRfcommSocketToServiceRecord(bT_UUID);
        }
        catch (IOException e) {}

        adapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try
        {
            socket.connect();
        }
        catch (IOException e)
        {
            try
            {
                socket.close();
            }
            catch (IOException e2) {}
        }

        bT = new BluetoothThread();

        if(!bT.InitBluetoothThread(socket, numImagePts)) {
            //Display an error message
        }

        //Create Region selection activity
        regionSelection = new RegionSelectionActivity();
        regionSelection.Init(GET_COORDINATES_ID);

        //Create Settings Activity
        settingsActivity = new SettingsActivity();
        settingsActivity.Init(GET_BOARD_COORDINATES_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    //Settings button handler
    public void settingsButtonHandler(View view) {
        //Get an image of the board
        if (bluetoothRunning == false)
        {
            bT.start();
            bluetoothRunning = true;
        }
        bT.startSaving();

        //Wait until image is ready, then get the image
        //COMMENTED OUT HERE
        //while (bT.getSaveStatus() == true) ;
        //imageBytes = bT.getImage();

        byte tempValue;
        //Convert negative values to positives
        for (int i = 0; i < imageBytes.length; i++) {
            tempValue = imageBytes[i];
            if (tempValue < 0)
                imageIntegers[i] = (int) tempValue + 256;
            else
                imageIntegers[i] = (int) tempValue;
        }

        //Check width and height are consistent with array size
        if ((numImagePts / 3) != (width * height)) {
            throw new ArrayStoreException();
        }

        //Convert to bitmap
        for (int intIndex = 0; intIndex < numImagePts - 2; intIndex = intIndex + 3) {
            intColors[intIndex / 3] = (alpha << 24) | (imageIntegers[intIndex] << 16) | (imageIntegers[intIndex + 1] << 8) | imageIntegers[intIndex + 2];
        }

        //Assign image to settings activity
        //Bitmap bmpImage = Bitmap.createBitmap(intColors, width, height, Bitmap.Config.ARGB_8888);
        //settingsActivity.setWhiteboardImage(bmpImage);

        //Use the Settings Activity to get the regions
        Intent intent = new Intent(this, settingsActivity.getClass());
        intent.putExtra("COLORS", intColors);
        startActivityForResult(intent, GET_BOARD_COORDINATES_ID);
    }

    //Region selection button handler
    public void regionSelectionButtonHandler(View view) {
        //Get an image of the board
        if (bluetoothRunning == false)
        {
            bT.start();
            bluetoothRunning = true;
        }
        bT.startSaving();

        //Wait until image is ready, then get the image
        //COMMENTED OUT HERE
        //while (bT.getSaveStatus() == true) ;
        //imageBytes = bT.getImage();

        byte tempValue;
        //Convert negative values to positives
        for (int i = 0; i < imageBytes.length; i++) {
            tempValue = imageBytes[i];
            if (tempValue < 0)
                imageIntegers[i] = (int) tempValue + 256;
            else
                imageIntegers[i] = (int) tempValue;
        }

        //Check width and height are consistent with array size
        if ((numImagePts / 3) != (width * height)) {
            throw new ArrayStoreException();
        }

        /*//Convert to bitmap
        for (int intIndex = 0; intIndex < numImagePts - 2; intIndex = intIndex + 3) {
            intColors[intIndex / 3] = (alpha << 24) | (imageIntegers[intIndex] << 16) | (imageIntegers[intIndex + 1] << 8) | imageIntegers[intIndex + 2];
        }*/

        //Convert to bitmap
        for (int intIndex = 0; intIndex < numImagePts/3; intIndex = intIndex + 1) {
            intColors[intIndex] = (alpha << 24) | (imageIntegers[intIndex] << 16) | (imageIntegers[numImagePts/3+intIndex ] << 8) | imageIntegers[2*numImagePts/3+intIndex];
        }

        //Use the Region Selection Activity to get the regions
        Intent intent = new Intent(this, regionSelection.getClass());
        intent.putExtra("COLORS", intColors);
        startActivityForResult(intent, GET_COORDINATES_ID);
    }

    //Used to send coordinates defined in RegionSelectionActivity
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == GET_COORDINATES_ID)
        {
            if(resultCode == RESULT_OK)
            {
                int [] coordinates = data.getIntArrayExtra("Coordinates");
                //Send the coordinates to the Camera module
                byte [] test = {1,2,3,4};
                if (!bT.sendData(test)) {
                    //Display error message
                }
            }
        }
    }

    public void eraseAllButtonHandler(View view)
    {
        if (bluetoothRunning == false)
        {
            bT.start();
            bluetoothRunning = true;
        }
        eraseAll = true;
        if (eraseAll == true)
        {
            //bT.sendData(eraseAllCode);
            eraseAll = false;
        }

    }

    public void emergencyStopButtonHandler(View view) {
        if (bluetoothRunning == false)
        {
            bT.start();
            bluetoothRunning = true;
        }
        emergencyStop = true;
        if (emergencyStop == true)
        {
            //bT.sendData(emergencyStopCode);
            emergencyStop = false;
        }
    }
}
