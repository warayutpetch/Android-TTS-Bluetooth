package com.example.androidbtcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class View1 extends Activity {
    private TextView tv,tv2;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view1);

        tv = (TextView) findViewById(R.id.text1);
        tv2 = (TextView) findViewById(R.id.text2);
        iv = (ImageView) findViewById(R.id.ivbsl);
        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.mytrasion);
        tv.setAnimation(myanim);
        tv2.setAnimation(myanim);
        iv.setAnimation(myanim);
        final Intent i = new Intent(this, MainMenuActivity.class);
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(i);
                    finish();
                }
            }

        };
        timer.start();
    }

    public void onBackPressed() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Exit");
        dialog.setIcon(R.drawable.bsl);
        dialog.setCancelable(true);
        dialog.setMessage("ต้องการออกจากโปรแกรมหรือไม่?");
        dialog.setPositiveButton("ใช่", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        dialog.setNegativeButton("ไม่ใช้", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.create();
            }
        });
        dialog.show();
    }
}
