package com.example.fragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private ViewPager mViewPager;
    private List<Fragment> mFragmentList;
    private ImageView mImage_home;
    private ImageView mImage_tool;
    private TextView tv_connect, tv_device, tv_control;
    private TextView tv_Title, tv_SecTitle;
    private LinearLayout linearLayout1;
    private LinearLayout linearLayout2;
    private LinearLayout linearLayout3;

    private int in = -1;
    private int SleepMS = 0;//接收间隔
    private Boolean isready=false;//是否已经连接
    private Boolean isend = false;
    private String str=null;
    private String CharsetName = "gb2312";//编码

    private BluetoothDevice device=null;
    private BluetoothSocket socket=null;
    private BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
    private OutputStream output=null;
    private InputStream input=null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setListener();

        //设置开始页面
        mViewPager.setCurrentItem(0);

    }

    private void setListener() {
        OnClick onclick = new OnClick();
        //点击事件
        tv_connect.setOnClickListener(onclick);
        tv_device.setOnClickListener(onclick);
        tv_control.setOnClickListener(onclick);
    }

    public void init(){
        //ViewPager
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new Fragment1());
        fragments.add(new Fragment2());
        fragments.add(new Fragment3());
        mViewPager = findViewById(R.id.vp);
        MyFragmentAdapter myFragmentAdapter = new MyFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(myFragmentAdapter);
        //设置接收间隔
        SetSleepMs(80);
        //layout
        linearLayout1=findViewById(R.id.LL1);
        linearLayout2=findViewById(R.id.LL2);
        linearLayout3=findViewById(R.id.LL3);

        //TextView
        tv_connect = findViewById(R.id.tv1);
        tv_device = findViewById(R.id.tv2);
        tv_control = findViewById(R.id.tv3);

        tv_Title = findViewById(R.id.tv_title);
        tv_SecTitle = findViewById(R.id.tv_SecondTitle);

        //初始化Drawable
        connect_x32 = getResources().getDrawable(R.drawable.connect_x32);
        connect_x32_press = getResources().getDrawable(R.drawable.connect_x32_press);
        device_x32 = getResources().getDrawable(R.drawable.device_x32);
        device_x32_press = getResources().getDrawable(R.drawable.device_x32_press);
        control_x32 = getResources().getDrawable(R.drawable.control_x32);
        control_x32_press = getResources().getDrawable(R.drawable.control_x32_press);

        //设置图片绘制大小
        connect_x32.setBounds(0,0,connect_x32.getMinimumWidth(), connect_x32.getMinimumHeight());
        connect_x32_press.setBounds(0,0,connect_x32_press.getMinimumWidth(), connect_x32_press.getMinimumHeight());
        device_x32.setBounds(0,0,device_x32.getMinimumWidth(), device_x32.getMinimumHeight());
        device_x32_press.setBounds(0,0,device_x32_press.getMinimumWidth(), device_x32_press.getMinimumHeight());
        control_x32.setBounds(0,0,control_x32.getMinimumWidth(), control_x32.getMinimumHeight());
        control_x32_press.setBounds(0,0,control_x32_press.getMinimumWidth(), control_x32_press.getMinimumHeight());
    }



/*-------------------------------------------------公共方法----------------------------------------------*/
    //发送字符串
    public void SendStr(String string){
        try {
            output.write(string.getBytes(CharsetName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //发送数字
    public void SendNum(int num){
        try {
            output.write(num);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //连接设备
    public BluetoothDevice ConnectDevice(BluetoothDevice bluetoothDevice){
        if(bluetoothDevice!=null) {
            device = bluetoothDevice;
            try{
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            }catch (IOException e){ }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        socket.connect();
                        SetMainTitle("已连接设备: "+device.getName());
                        SetSecondaryTitle("连接成功"+" ");
                        isready = true;

                        output = socket.getOutputStream();
                        output.write("- Connected -\r\n".getBytes(CharsetName));
                    }catch (IOException e){}
                }
            }).start();
        }
        return device;
    }

    //断开连接
    public void CloseConnected(){
        try {
            if(isready) {
                isready = false;
                socket.close();
            }
            SetMainTitle("未连接设备");
            SetSecondaryTitle("已断开连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //设置主要Title
    public void SetMainTitle(String str){
        tv_Title.setText(str);
    }

    //设置次要Title
    public void SetSecondaryTitle(String str){
        tv_SecTitle.setText(str);
    }

    //获取socket
    public BluetoothSocket GetSocket(){
        return socket;
    }
    //得到接收间隔
    public int GetSleepMs(){
        return SleepMS;
    }
    //设置接收间隔
    public void SetSleepMs(int ms){
        SleepMS = ms;
    }
    //是否准备好
    public Boolean isReady(){
        return isready;
    }
    //应用退出
    public Boolean isEnd(){
        return isend;
    }
    //将byte数据转化为无符short数据
    public void Byte2Unsigned(byte bytes[], int count, int shorts[]){
        for(int i=0;i<count;i++){
            if(bytes[i]<0){
                shorts[i]= (short) (256+bytes[i]);
            }else{
                shorts[i]=bytes[i];
            }
        }
    }
    //自定义简单通信协议  数据处理
    public Boolean DataDeal(int PreviousShorts[], int BehindShort[], int count){
        Boolean isFirstData = false;
        Boolean isSencondData = false;
        Boolean isReadyToEnd = false;

        int j=0;//新数组计数

        for(int i=0;i<count;){
            if(PreviousShorts[i] == 0xDF){
                isFirstData = false;
                isSencondData = false;
                return true;
            }else if(isFirstData && isSencondData){
                if(i <= 2){
                    BehindShort[j] = PreviousShorts[i];
                }else{
                    BehindShort[j] = (PreviousShorts[i]*256+PreviousShorts[i+1]);
                    if( (PreviousShorts[i]*256+PreviousShorts[i+1]) >= 65536){
                        BehindShort[j] = -1;
                    }
                    i++;
                }
                j++;
            }
            else if(PreviousShorts[i] == 0xAA && !isFirstData)   isFirstData = true;
            else if(PreviousShorts[i] == 0x55 && !isSencondData) isSencondData = true;
            i++;
        }
        return false;
    }

    //
    public Boolean DealData(int PreviousShorts[], int BehindShort[], int count){

        int READY_TO_GO = 1;
        int RECEIVE = 2;
        int READY_TO_STOP = 3;
        int END = 4;

        int statue = 4;
        int[] buff = new int[10];
        int Count = 0;
        int MAX_BUFF = 9;

        for( int i=0; i<count; i++) {
            if (PreviousShorts[i] == 0xDF && statue == RECEIVE) {
                statue = READY_TO_STOP;
            }
            else if (PreviousShorts[i] == 0xFD && statue == READY_TO_STOP) {
                statue = END;
                Count = 0;
                for (int j = 0; j < MAX_BUFF; j++) {
                    if( j<1 ) {
                        BehindShort[Count++] = buff[j];
                    }else{
                        BehindShort[Count++] = (buff[j]<<8) + buff[j+1];
                        if( BehindShort[Count-1] > 65535 ){
                            BehindShort[Count-1] = -1;
                        }
                        j++;
                    }
                }

                for (int j = 0; j < MAX_BUFF; j++) {
                    buff[j] = 0;
                }
                Count = 0;

                return true;
            } else {
                if (statue == READY_TO_STOP) {
                    buff[Count++] = 0xDF;
                    statue = RECEIVE;
                }
            }

            if (PreviousShorts[i] == 0xAA && statue == END) {
                statue = READY_TO_GO;
            } else if (PreviousShorts[i] == 0x55 && statue == READY_TO_GO) {
                statue = RECEIVE;
            } else if (statue == RECEIVE) {
                buff[Count++] = PreviousShorts[i];
            }
        }
        return false;
    }



    //获取陀螺仪的值
    public float[] GetOrientation(){
        return orientations;
    }

    //设置陀螺仪监听器
    public Boolean setRotationListener(){
        //陀螺仪
        ((SensorManager)getSystemService(SENSOR_SERVICE)).registerListener(
                this,
                ((SensorManager)getSystemService(SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),//TYPE_GYROSCOPE        加速计
                SensorManager.SENSOR_DELAY_UI); //UI 60ms刷新   GAME 20ms  Normal 200ms   Fast 0ms               //TYPE_ROTATION_VECTOR  陀螺仪
        return true;
        //加速计
//        ((SensorManager)getSystemService(SENSOR_SERVICE)).registerListener(
//                this,
//                ((SensorManager)getSystemService(SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_GYROSCOPE),      //TYPE_GYROSCOPE        加速计
//                SensorManager.SENSOR_DELAY_NORMAL);                                                             //TYPE_ROTATION_VECTOR  陀螺仪
    }

    //关闭陀螺仪监听器
    public Boolean closeRotationListener(){
        //注销陀螺仪监听
        ((SensorManager)getSystemService(SENSOR_SERVICE)).unregisterListener(
                this,
                ((SensorManager)getSystemService(SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
        return false;
    }

/*------------------------------------------------公共方法END--------------------------------------------*/


/*--------------------------------------------------陀螺仪-----------------------------------------------*/
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp = 0.0f;
    private float angle[] = new float[3];
    private float[] orientations = new float[3];

    private float[] remapped = new float[16];
    private float[] rotation = new float[16];

    private float value1_off = -0.00012207031f;
    private float value2_off =  0.00056457520f;
    private float value3_off =  0.0f;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if( timestamp != 0 && (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) ){//加速计
            final float dT = (event.timestamp - timestamp) * NS2S;

            event.values[0] -= value1_off;
            event.values[1] -= value2_off;
            event.values[2] -= value3_off;

            angle[0] += event.values[0]*dT;
            angle[1] += event.values[1]*dT;
            angle[2] += event.values[2]*dT;
        }
        if( timestamp != 0 && (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) ){//陀螺仪
            SensorManager.getRotationMatrixFromVector(rotation, event.values);
            SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Y, remapped);
            SensorManager.getOrientation(remapped, orientations);

            for(int i=0;i<3;i++) {
                orientations[i] = (float)Math.toDegrees(orientations[i]);
            }

        }
        timestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
/*------------------------------------------------陀螺仪END----------------------------------------------*/



    class MyFragmentAdapter extends FragmentPagerAdapter{
        public MyFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragmentList = fragments;
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }

    @Override
    public void onBackPressed() {//退出确认
        new AlertDialog.Builder(this)
            .setIcon(R.drawable.icon_x64)//显示图标
            .setTitle("是否退出程序?")//标题
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {//按键监听
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isready = true;
                    isend = true;
                    finish();
                    dialog.dismiss();
                }
            }).show();
    }


    private Drawable connect_x32;
    private Drawable connect_x32_press;
    private Drawable device_x32;
    private Drawable device_x32_press;
    private Drawable control_x32;
    private Drawable control_x32_press;

    private String Green = "#1AFA29";
    private String Black = "#000000";

    private class OnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv1:
                    mViewPager.setCurrentItem(0);
                    //设置图片颜色
                    tv_connect.setCompoundDrawables(null,connect_x32_press,null,null);
                    tv_device.setCompoundDrawables(null,device_x32,null,null);
                    tv_control.setCompoundDrawables(null,control_x32,null,null);

                    //设置文字颜色
                    tv_connect.setTextColor(Color.parseColor(Green));
                    tv_device.setTextColor(Color.parseColor(Black));
                    tv_control.setTextColor(Color.parseColor(Black));

                    //设置标题
                    SetSecondaryTitle("连接");

                    break;
                case R.id.tv2:
                    //跳转页码
                    mViewPager.setCurrentItem(1);

                    //设置图片颜色
                    tv_connect.setCompoundDrawables(null,connect_x32,null,null);
                    tv_device.setCompoundDrawables(null,device_x32_press,null,null);
                    tv_control.setCompoundDrawables(null,control_x32,null,null);

                    //设置文字颜色
                    tv_connect.setTextColor(Color.parseColor(Black));
                    tv_device.setTextColor(Color.parseColor(Green));
                    tv_control.setTextColor(Color.parseColor(Black));

                    //设置标题
                    SetSecondaryTitle("上位机");

                    break;
                case R.id.tv3:
                    mViewPager.setCurrentItem(2);

                    //设置图片颜色
                    tv_connect.setCompoundDrawables(null,connect_x32,null,null);
                    tv_device.setCompoundDrawables(null,device_x32,null,null);
                    tv_control.setCompoundDrawables(null,control_x32_press,null,null);

                    //设置文字颜色
                    tv_connect.setTextColor(Color.parseColor(Black));
                    tv_device.setTextColor(Color.parseColor(Black));
                    tv_control.setTextColor(Color.parseColor(Green));

                    //设置标题
                    SetSecondaryTitle("控制");

                    break;
                default:
                    break;

            }
        }
    }
}

