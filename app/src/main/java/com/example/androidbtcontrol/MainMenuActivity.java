package com.example.androidbtcontrol;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.example.androidbtcontrol.MyTTS;
public class MainMenuActivity extends AppCompatActivity {

    private ListView listView;
    List<Dog> dogs = new ArrayList<>();
    int dataSize;
    public Dog listDog = new Dog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);


        Button next = (Button) findViewById(R.id.btngo);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainMenuActivity.this, MainActivity.class);
                startActivity(i);
                finish();

            }
        });


        initInstances();


    }

    private void initInstances() {

//        prepareData();
//
//        ListAdapter adapter = new ListAdapter(MainMenuActivity.this, listDog);
//        listView = (ListView) findViewById(R.id.list_item);
//        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(listViewClickListener);

    }

    private void prepareData() {

        int resId[] = {R.drawable.book, R.drawable.care, R.drawable.hospital, R.drawable.conclude, R.drawable.bibliography};

        String breed[] = {"บทนำ", "ดูแลแผลกดทับอย่างไร?", "ส่งเสริมการหายของแผลกดทับ", "สรุป", "บรรณานุกรม"};

        String description[] = {getString(R.string.german_shepherd_des), getString(R.string.labrador_retriever_des), getString(R.string.blue_dog_des), getString(R.string.beagle_des), getString(R.string.boxer_des)};
        dataSize = resId.length;


        Log.d("khem", "dataSize " + resId.length);
        Log.d("khem", "breed " + resId.length);
        Log.d("khem", "description " + resId.length);


        for (int i = 0; i < dataSize; i++) {
            Log.d("khem", " " + i);
            Dog dog = new Dog(resId[i], breed[i], description[i]);
            dogs.add(dog);
        }

        listDog.setDogs(dogs);

        //Log.d("khem",listDog);
    }


    /*************************
     * Listener
     ***************************/

    AdapterView.OnItemClickListener listViewClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            Toast.makeText(MainMenuActivity.this, "POSITION:= " + position, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainMenuActivity.this, ShowDetailActivity.class);
            intent.putExtra("resId", listDog.getDogs().get(position).getResId());
            intent.putExtra("breed", listDog.getDogs().get(position).getBreed());
            intent.putExtra("desc", listDog.getDogs().get(position).getDescription());

            startActivity(intent);
        }
    };


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
