package com.example.androidbtcontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowDetailActivity extends AppCompatActivity {
    private int resId;
    private String breed;
    private String description;
    private ImageView imageView;
    private TextView tvDescription;
    private TextView tvBreed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail);

        initInstances();
    }

    private void initInstances() {

        resId = getIntent().getIntExtra("resId", 0);
        breed = getIntent().getStringExtra("breed");
        description = getIntent().getStringExtra("desc");

        tvDescription = (TextView) findViewById(R.id.desc);
        tvBreed = (TextView) findViewById(R.id.title);
        imageView = (ImageView) findViewById(R.id.image);

        tvDescription.setText(description);
        tvBreed.setText(breed);
        imageView.setImageResource(resId);
    }
}
