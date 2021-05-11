package com.example.myapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

public class MainActivity extends AppCompatActivity {
    BarChart stackedChart;
    BarChart stackedChart1;

    int[] colorArray = new int[]{Color.RED,Color.MAGENTA,Color.BLUE};
    float[] array = new float[100];
    float[] array1 = new float[100];

    public Socket s;
    Button btn1, btn2, btn3, btn4, btn5;
    EditText text1;
    EditText text2;
    TextView sensor1, sensor2;
    BufferedReader reader;
    PrintWriter writer;
    public void ToastMessage(Socket s) {
        if (!s.isConnected()) {
            Toast.makeText(this, "서버와 연결해주세요", Toast.LENGTH_SHORT);
        }
    }
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button) findViewById(R.id.button);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        btn4 = (Button) findViewById(R.id.button4);
        btn5 = (Button) findViewById(R.id.button5);
        text1 = (EditText) findViewById(R.id.editTextTextPersonName);
        text2 = (EditText) findViewById(R.id.editTextTextPersonName2);
        sensor1 = (TextView) findViewById(R.id.textView5);
        sensor2 = (TextView) findViewById(R.id.textView6);
        stackedChart = (BarChart)findViewById(R.id.stacked_BarChart);
        stackedChart1 = (BarChart)findViewById(R.id.stacked_BarChart1);

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
                BarDataSet barDataSet = new BarDataSet(dataValues1(),"Bar set");
                BarDataSet barDataSet1 = new BarDataSet(dataValues2(),"Bar set");

                barDataSet.setColors(colorArray);
                barDataSet1.setColors(colorArray);
                BarData barData =new BarData();
                BarData barData1 =new BarData();
                barData.addDataSet(barDataSet);
                barData1.addDataSet(barDataSet1);
                stackedChart.setData(barData);
                stackedChart1.setData(barData1);
                stackedChart.invalidate();
                stackedChart1.invalidate();
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
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendThread send = new SendThread("0");
                send.start();
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
                reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                writer = new PrintWriter(s.getOutputStream(), true);
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
            if (s.isConnected()) {
                Log.d("MyTag", "연결됨");
                writer.println(bit);
                writer.flush();
            }

        }
    }

    class ReceiveThread extends Thread {
        public void run() {
            try {
                if(s.isConnected())
                {
                    Log.d("MyTag", "연결됨2");

                    while(true)
                    {
                        for(int i = 0; i < 5; i++)
                        {
                            String data = reader.readLine();
                            String data2 = reader.readLine();

                            array[i] = Float.parseFloat(data);
                            array1[i] = Float.parseFloat(data2);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    sensor1.setText(data);
                                    sensor2.setText(data2);
                                }
                            });
                        }
                    }
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
    private ArrayList<BarEntry> dataValues1(){
        ArrayList<BarEntry> dataVals = new ArrayList<>();

        dataVals.add(new BarEntry(0, new float[]{array[0]}));
        dataVals.add(new BarEntry(1, new float[]{array[1]}));
        dataVals.add(new BarEntry(2, new float[]{array[2]}));
        dataVals.add(new BarEntry(3, new float[]{array[3]}));
        dataVals.add(new BarEntry(4, new float[]{array[4]}));

        return dataVals;
    }

    private ArrayList<BarEntry> dataValues2(){
        ArrayList<BarEntry> dataVals = new ArrayList<>();

        dataVals.add(new BarEntry(0, new float[]{array1[0]}));
        dataVals.add(new BarEntry(1, new float[]{array1[1]}));
        dataVals.add(new BarEntry(2, new float[]{array1[2]}));
        dataVals.add(new BarEntry(3, new float[]{array1[3]}));
        dataVals.add(new BarEntry(4, new float[]{array1[4]}));

        return dataVals;
    }
}