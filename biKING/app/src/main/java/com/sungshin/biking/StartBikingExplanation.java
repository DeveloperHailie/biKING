package com.sungshin.biking;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;


public class StartBikingExplanation extends AppCompatActivity {

    Adapter adapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);
        // 아까 만든 view
        viewPager = (ViewPager)findViewById(R.id.view);
        //adapter 초기화
        adapter = new Adapter(this);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
