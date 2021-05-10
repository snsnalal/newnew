package com.example.myapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Socket s;
    Button btn1, btn2, btn3, btn4;
    EditText text1;
    EditText text2;
    TextView sensor1, sensor2;

    public void ToastMessage(Socket s) {
        if (!s.isConnected()) {
            Toast.makeText(this, "서버와 연결해주세요", Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button) findViewById(R.id.button);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        btn4 = (Button) findViewById(R.id.button4);
        text1 = (EditText) findViewById(R.id.editTextTextPersonName);
        text2 = (EditText) findViewById(R.id.editTextTextPersonName2);
        sensor1 = (TextView) findViewById(R.id.textView5);
        sensor2 = (TextView) findViewById(R.id.textView6);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MyTag", "클릭");
                if (text1.getText().toString() != "") {
                    Log.d("MyTag", "진입");
                    String add = text1.getText().toString();
                    ConnectThread thread = new ConnectThread(add);
                    thread.start();
                }

            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(text2.getText().toString() == "")) {
                    String a = text2.getText().toString();
                    SendThread send = new SendThread(a);
                    send.start();

                }
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReceiveThread rec = new ReceiveThread();
                rec.start();
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ActivityCompat.finishAffinity(MainActivity.this);
                System.exit(0);
            }
        });
    }

    class ConnectThread extends Thread {
        String ipaddr;

        public ConnectThread(String addr) {
            ipaddr = addr;
        }

        public void run() {
            Log.d("MyTag", "스레드 실행");
            try {
                Log.d("MyTag", ipaddr);
                s = new Socket(ipaddr, 5500);
                Log.d("MyTag", "연결");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (s.isConnected()) {
                            Toast.makeText(getApplicationContext(), "연결됨", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "연결안됨", Toast.LENGTH_LONG).show();
                        }

                    }
                });
                Log.d("MyTag", "끝");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    class SendThread extends Thread {
        String bit;
        String tem, hum;

        public SendThread(String bit) {
            this.bit = bit;
        }

        public void run() {
            try {
                if (s.isConnected()) {
                    Log.d("MyTag", "연결됨");
                    byte[] b = bit.getBytes();
                    OutputStream output = s.getOutputStream();

                    output.write(b);
                    Log.d("MyTag", b.toString());
                    output.flush();
                    output.close();
                }
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "통신 실패", Toast.LENGTH_SHORT);
                    }
                });
            }

        }
    }

    class ReceiveThread extends Thread {
        byte[] bit;

        public void run() {
            try {
                byte[] buffer = new byte[256];
                InputStream input = s.getInputStream();
                int b = input.read(buffer);
                bit = Integer.toString(b).getBytes();
                String c = bit.toString();
                String d[] = c.split(" ");
                input.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sensor1.setText(d[0]);
                        sensor2.setText(d[1]);

                    }
                });
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "통신 실패", Toast.LENGTH_SHORT);
                    }
                });
            }

        }
    }
}