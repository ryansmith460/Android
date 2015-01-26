package com.example.rysm4200.androidapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

public class RegionSelectionActivity extends Activity {
    //
    int GET_COORDINATES_ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_selection);
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

    public void eraseButtonHandler()
    {
        Intent data = new Intent();
        //data.setData(getRegions());
        setResult(GET_COORDINATES_ID, data);
        finish();
    }

}
