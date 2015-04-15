package com.madness.XPaint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Gebruiker on 5-7-14.
 */

public class MenuActivity extends Activity {
    public static String type = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
    }

    public void openApp(View v){
        Intent intent = new Intent(this, MainActivity.class);
        type = String.valueOf(v.getId());
        intent.putExtra(type, type);
        startActivity(intent);
    }
}
