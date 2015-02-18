package com.example.rysm4200.androidapp;

import android.bluetooth.BluetoothSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Created by rysm4200 on 1/25/2015.
 */
public class BluetoothThread extends Thread{
    private InputStream Instream;
    private OutputStream OutStream;
    byte[] image;
    private boolean isSavingData = false;
    BluetoothSocket socket;
    int numImagePts;

    //Init Routine
    public boolean InitBluetoothThread(BluetoothSocket socket, int numImagePts){
        image = new byte[numImagePts];

        //Create I/0 Streams
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try
        {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        }
        catch (IOException e)
        {
            return false;
        }

        Instream = tmpIn;
        OutStream = tmpOut;
        return true;
    }

    //Public methods
    public void startSaving()
    {
        isSavingData = true;
    }

    public void stopSaving()
    {
        isSavingData = false;
    }

    public boolean getSaveStatus()
    {
        return isSavingData;
    }

    public byte[] getImage()
    {
        return image;
    }

    public boolean sendData(byte[] data)
    {
        try
        {
            OutStream.write(data);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Automatically called when data is available on InputStream
    public void run() {
        int bytes = 0;
        if(isSavingData) {
            try
            {
                while (Instream.available() == 0) ;
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }

            isSavingData = true;

            while (bytes < image.length) {
                try {
                    bytes += Instream.read(image, bytes, image.length - bytes);
                } catch (IOException t) {
                    break;
                }
            }

            isSavingData = false;
        }
    }
}
