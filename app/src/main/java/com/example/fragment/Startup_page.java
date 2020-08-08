package com.example.fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class Startup_page extends AppCompatActivity {

    private volatile Boolean isGetPermission = false;
    private BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_page);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //启动页面等待时间
                    Thread.sleep(1000);
                    //等待权限
                    while (!isGetPermission);
                    //跳转
                    Intent intent = new Intent(Startup_page.this, MainActivity.class);
                    startActivity(intent);

                    //打开蓝牙
                    if( !bluetoothAdapter.isEnabled() ){
                        Intent intent1 = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent1, 0);//请求打开蓝牙
                    }

                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        CheckBluetoothPermission();

    }

    private void CheckBluetoothPermission(){
        if(Build.VERSION.SDK_INT >= 23){//判断安卓版本是否大于Android6.0
            if( ContextCompat.checkSelfPermission(Startup_page.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){//如果没有获取权限
                ActivityCompat.requestPermissions(Startup_page.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},0);
            }else{//已经获取了权限
                isGetPermission = true;
            }
        }else{//Android版本低于6.0
            isGetPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( requestCode == 0 ){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                isGetPermission = true;
            }else{
                isGetPermission = true;
                Toast toast = Toast.makeText(Startup_page.this, " ", Toast.LENGTH_LONG);
                toast.setText("获取定位权限失败\n可能会影响软件后面使用");
                toast.show();

//                finish();
            }
        }
    }
}