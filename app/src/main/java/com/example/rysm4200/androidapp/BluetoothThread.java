package com.example.rysm4200.androidapp;

import android.bluetooth.BluetoothSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import android.os.Handler;
import android.os.Message;

/**
 * Created by rysm4200 on 1/25/2015.
 */
public class BluetoothThread extends Thread{
    private InputStream Instream;
    private OutputStream OutStream;
    byte[] image;
    byte[] coordinates;
    byte[] robotCoordinates;
    private boolean isSavingData = false;
    BluetoothSocket socket;
    int numImagePts;
    Handler progressHandler;
    boolean whiteboard = false;
    boolean robot = false;

    //Init Routine
    public boolean InitBluetoothThread(BluetoothSocket socket, int numImagePts){
        if (whiteboard == false && robot == false) {
            image = new byte[numImagePts];
        }
        if (whiteboard == false && robot == true) {
            //image = new byte[numImagePts];
            //coordinates = new byte[8];
            robotCoordinates = new byte[16];
        }
        if (whiteboard == true && robot == false){
            coordinates = new byte[8];
        }

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

    public byte[] getCoordinates()
    {
        return coordinates;
    }

    public byte[] getRobotCoordinates()
    {
        return robotCoordinates;
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

    public void whiteboard()
    {
        whiteboard = true;
    }

    public void robot()
    {
        robot = true;
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
            if (!whiteboard && !robot) {
                while (bytes < image.length) {
                    try {
                        bytes += Instream.read(image, bytes, image.length - bytes);
                        //Message msg = progressHandler.obtainMessage();
                        //msg.arg1=bytes;
                        //msg.sendToTarget();
                    } catch (IOException t) {
                        break;
                    }
                }
            }
           else if (whiteboard && !robot) {
                    while (bytes < coordinates.length) {
                        try {
                            bytes += Instream.read(coordinates, bytes, coordinates.length - bytes);
                        } catch (IOException t) {
                            break;
                        }
                    }
                    whiteboard = false;
                }
           else if(!whiteboard && robot){
                    while (bytes < robotCoordinates.length) {
                        try {
                            bytes += Instream.read(robotCoordinates, bytes, robotCoordinates.length - bytes);
                        } catch (IOException t) {
                            break;
                        }
                    }
                    robot = false;
                }
            isSavingData = false;
            }


    }
}
