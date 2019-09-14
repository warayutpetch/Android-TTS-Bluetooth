/*
Android Example to connect to and communicate with Bluetooth
In this exercise, the target is a Arduino Due + HC-06 (Bluetooth Module)

Ref:
- Make BlueTooth connection between Android devices
http://android-er.blogspot.com/2014/12/make-bluetooth-connection-between.html
- Bluetooth communication between Android devices
http://android-er.blogspot.com/2014/12/bluetooth-communication-between-android.html
 */
package com.example.androidbtcontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.media.RingtoneManager;
import android.media.Ringtone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter;

    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    Uri uriAlarm;
    Ringtone ringTone;
    TextView textInfo, textStatus, textTest, textResult;
    ImageView imgLocation;
    ListView listViewPairedDevice;
    LinearLayout inputPane;
    EditText inputField;
    Button btnSend;
    View leftTop, topCenter, rightTop, leftCenter, centerLeft, center, centerRight, rightCenter, leftBot, centerBot, rightBot;
    ArrayAdapter<String> pairedDeviceAdapter;
    ImageView mImageView;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    TextView tv;
    ImageView iv;
    boolean looper = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main); //เริ่มต้นเรียกหน้า main ขึ้นมา
//        MyTTS.getInstance(MainActivity.this).setLocale(new Locale("th"));
//        MyTTS.getInstance(MainActivity.this).speak("เลือกอุปกรณ์ที่ต้องการเชื่อมต่อ");
        textInfo = (TextView) findViewById(R.id.info);
        textStatus = (TextView) findViewById(R.id.status);
        listViewPairedDevice = (ListView) findViewById(R.id.pairedlist);

        uriAlarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringTone = RingtoneManager
                .getRingtone(getApplicationContext(), uriAlarm);

        inputPane = (LinearLayout) findViewById(R.id.inputpane);
        inputField = (EditText) findViewById(R.id.input);
        btnSend = (Button) findViewById(R.id.send);
        btnSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (myThreadConnected != null) {
                    byte[] bytesToSend = inputField.getText().toString().getBytes();
                    myThreadConnected.write(bytesToSend);
                }
            }
        });

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) { /// เช็คว่าอุปกรณ์ Support Bluetooth มั้ย
            Toast.makeText(this,
                    "FEATURE_BLUETOOTH NOT support",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //using the well-known SPP UUID
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //เรียกใช้งาน Buletooth
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String stInfo = bluetoothAdapter.getName() + "\n" +
                bluetoothAdapter.getAddress();
        textInfo.setText(stInfo);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF เปิด Bluetooth ถ้าปิดอยู่
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        setup();
    }

    private void setup() {
        final Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice pairedDevice : bluetoothAdapter.getBondedDevices()) {

            Log.d("log", "\tDevice Name: " + pairedDevice.getName());
            Log.d("log", "\tDevice MAC: " + pairedDevice.getAddress());

        }
        if (pairedDevices.size() > 0) { //// แสดงรายการอุปกรณ์ที่จับคู่อยู่
            pairedDeviceArrayList = new ArrayList<BluetoothDevice>();
            final List<String> s = new ArrayList<String>();
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device);
                s.add(device.getName());
            }

            pairedDeviceAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, s); /// เอารายการที่ถูกจับคู่มาแสดงใน listView
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);

            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() { /// เช็คว่าเรากดรายการไหนแล้วเริ่มทำการเชื่อมต่อ

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) { //เมื่อคลิกที่อุปกรณ์ที่ถูกจับคุ๋ให้เริ่มเส้นทางเชื่อมต่อ
//                    BluetoothDevice device =
//                            (BluetoothDevice) parent.getItemAtPosition(position);
                    String selected = s.get(position);
                    for (Iterator<BluetoothDevice> it = pairedDevices.iterator(); it.hasNext(); ) {
                        BluetoothDevice bt = it.next();

                        if (bt.getName().equals(selected)) { // ถ้าตรงที่คลิก ให้เริ่มทำการเชื่อมต่อ

                            myThreadConnectBTdevice = new ThreadConnectBTdevice(bt); //เรียกฟังก์ชั่นเชื่อมต่อ
                            myThreadConnectBTdevice.start(); // เริ่มกระบวนการเชื่อมต่อ
                            Log.d("log", "\tDevice MAC: " + bt);
                        }
                    }


                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


        if (myThreadConnectBTdevice != null) {
            myThreadConnectBTdevice.cancel();
            Log.d("test", "Closed Thread");
        }
        looper = false;
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                setup();
            } else {
                Toast.makeText(this,
                        "BlueTooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainMenuActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket) {

        myThreadConnected = new ThreadConnected(socket); //เมื่อเชื่อมต่อสำเร็จให้เริ่มกระบวนการรับข้อมูล
        myThreadConnected.start(); /// เริ่มเทรดรับส่งข้อมูล
    }

    /*
    ThreadConnectBTdevice:
    Background Thread to handle BlueTooth connecting
    */
    private class ThreadConnectBTdevice extends Thread {  ///เริ่ม เทรด ในการรับข้อมูล

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                textStatus.setText("Connecting...");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            boolean success = false;
            try {
                bluetoothSocket.connect(); ///เช็คว่ายังเชื่อมต่ออยู่ไหม ถ้าไม่จะหยุดเทรด
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                final String eMessage = e.getMessage();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textStatus.setText("Connect fail");
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if (success) {
                //connect successful
                final String msgconnected = "Connect successful";

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        try {


                            setContentView(R.layout.activity_notification);

                            setContentView(R.layout.activity_notification);
                            ConstraintLayout ll = (ConstraintLayout) findViewById(R.id.linearLayout);
                            ll.setBackgroundResource(R.drawable.location);
                            textResult = (TextView) findViewById(R.id.textRes);
                            textResult.setText("ไม่พบสถานที่");
                            imgLocation= (ImageView) findViewById(R.id.imageView);
                            imgLocation.setImageResource(R.drawable.img_404);
                            MyTTS.getInstance(MainActivity.this).setLocale(new Locale("th"));
                            MyTTS.getInstance(MainActivity.this).speak("ไม่พบสถานที่");
                            //  leftTop,topCenter,rightTop,leftCenter,centerLeft,center,centerRight,rightCenter,leftBot,centerBot,rightBot

                            listViewPairedDevice.setVisibility(View.GONE);
                            inputPane.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "Error Open Again",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

                startThreadConnected(bluetoothSocket);

            } else {
                //fail
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();
//            Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);
//            startActivity(intent);
//            finish();
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }


    }

    /*
    ThreadConnected:
    Background Thread to handle Bluetooth data communication
    after connected
     */
    private class ThreadConnected extends Thread { //เทรดที่ใช้สำหรับรับค่า มันจะวื่งรอรับค่าไปเรื่อยๆ
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        private void customVibratePatternNoRepeat() {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // 0 : Start without a delay
            // 400 : Vibrate for 400 milliseconds
            // 200 : Pause for 200 milliseconds
            // 400 : Vibrate for 400 milliseconds
            long[] mVibratePattern = new long[]{0, 400, 200, 400};

            // -1 : Do not repeat this pattern
            // pass 0 if you want to repeat this pattern from 0th index
            v.vibrate(mVibratePattern, -1);
        }

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (looper) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes); //// ตัวอักษรที่ส่งมาจะอยู่ในรูปแบบ Byte จะต้องแปลงก่อนถึงจะเห็นเป็นข้อความ
                    Log.d("zzzzzzzzzzzzzzzzzzzzzzz", strReceived);

                    runOnUiThread(new Runnable() {
                        ///////recive
                        boolean play = false;

                        @Override
                        public void run() {
                            //  leftTop,topCenter,rightTop,leftCenter,centerLeft,center,centerRight,rightCenter,leftBot,centerBot,rightBot
                            if (!MyTTS.getInstance(MainActivity.this).checkSpeak()) { ///เช็คว่ามีการพูดเสร็จหรือยัง ถ้ายัง จะไม่เข้าไปเช็คเงื่อนไขต่อไป

                                if (strReceived.equals("1")) { //// เปรียบเทียบตามเงื่อนไข

                                    customVibratePatternNoRepeat();
                                    ConstraintLayout ll = (ConstraintLayout) findViewById(R.id.linearLayout);
//                                ll.setBackgroundResource(R.drawable.location);
                                    textResult = (TextView) findViewById(R.id.textRes);
                                    textResult.setText("อาคาร 18 อาคารเรียนรวมสาขาวิชาไฟฟ้า");
                                    imgLocation= (ImageView) findViewById(R.id.imageView);
                                    imgLocation.setImageResource(R.drawable.img_18);
                                    MyTTS.getInstance(MainActivity.this).setLocale(new Locale("th")); ///เซ็ตเสียงให้พูดเป็นภาษาไทย
                                    MyTTS.getInstance(MainActivity.this).speak("อาคาร 18 อาคารเรียนรวมสาขาวิชาไฟฟ้า"); ///สั่งให้พูดคำที่ส่งไป


                                }

                                if (strReceived.equals("2")) {
                                    customVibratePatternNoRepeat();
                                    ConstraintLayout ll = (ConstraintLayout) findViewById(R.id.linearLayout);
//                                ll.setBackgroundResource(R.drawable.location);
                                    textResult = (TextView) findViewById(R.id.textRes);
                                    textResult.setText("อาคาร 36 คณะวิศวกรรมศาสตร์และสถาปัตยกรรมศาสตร์");
                                    imgLocation= (ImageView) findViewById(R.id.imageView);
                                    imgLocation.setImageResource(R.drawable.img_36);
                                    MyTTS.getInstance(MainActivity.this).setLocale(new Locale("th"));
                                    MyTTS.getInstance(MainActivity.this).speak("อาคาร 36 คณะวิศวกรรมศาสตร์และสถาปัตยกรรมศาสตร์");

                                }
                                if (strReceived.equals("3")) {
                                    customVibratePatternNoRepeat();
                                    ConstraintLayout ll = (ConstraintLayout) findViewById(R.id.linearLayout);
//                                ll.setBackgroundResource(R.drawable.location);
                                    textResult = (TextView) findViewById(R.id.textRes);
                                    textResult.setText("อาคาร 35 อาคารเรียนรวม");
                                    imgLocation= (ImageView) findViewById(R.id.imageView);
                                    imgLocation.setImageResource(R.drawable.img_35);
                                    MyTTS.getInstance(MainActivity.this).setLocale(new Locale("th"));
                                    MyTTS.getInstance(MainActivity.this).speak("อาคาร 35 อาคารเรียนรวม");

                                }
                                if (strReceived.equals("4")) {
                                    customVibratePatternNoRepeat();
                                    ConstraintLayout ll = (ConstraintLayout) findViewById(R.id.linearLayout);
//                                ll.setBackgroundResource(R.drawable.location);
                                    textResult = (TextView) findViewById(R.id.textRes);
                                    textResult.setText("หอพักนักศึกษา");
                                    imgLocation= (ImageView) findViewById(R.id.imageView);
                                    imgLocation.setImageResource(R.drawable.img_dom);
                                    MyTTS.getInstance(MainActivity.this).setLocale(new Locale("th"));
                                    MyTTS.getInstance(MainActivity.this).speak("หอพักนักศึกษา");

                                }
                                if (strReceived.equals("5")) {
                                    customVibratePatternNoRepeat();
                                    ConstraintLayout ll = (ConstraintLayout) findViewById(R.id.linearLayout);
//                                ll.setBackgroundResource(R.drawable.location);
                                    textResult = (TextView) findViewById(R.id.textRes);
                                    textResult.setText("โรงอาหาร ม ท ร อีสาน");
                                    imgLocation= (ImageView) findViewById(R.id.imageView);
                                    imgLocation.setImageResource(R.drawable.img_canteen);
                                    MyTTS.getInstance(MainActivity.this).setLocale(new Locale("th"));
                                    MyTTS.getInstance(MainActivity.this).speak("โรงอาหาร ม ท ร อีสาน");

                                }
                                if (strReceived.equals("6")) {
                                    customVibratePatternNoRepeat();
                                    ConstraintLayout ll = (ConstraintLayout) findViewById(R.id.linearLayout);
//                                ll.setBackgroundResource(R.drawable.location);
                                    textResult = (TextView) findViewById(R.id.textRes);
                                    textResult.setText("ไม่พบสถานที่");
                                    imgLocation= (ImageView) findViewById(R.id.imageView);
                                    imgLocation.setImageResource(R.drawable.img_404);
                                    MyTTS.getInstance(MainActivity.this).setLocale(new Locale("th"));
                                    MyTTS.getInstance(MainActivity.this).speak("ไม่พบสถานที่");
                                }

                                Log.d("zzzzzzzzzzzzzzzzzzzzzzz", strReceived);

                            }

                            //                            textStatus.setText(msgReceived);
                        }

                    });

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost";
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            textStatus.setText(msgConnectionLost);
                        }
                    });
                }
            }

        }


        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }


}
