package com.example.fragment;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment3 extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnTouchListener{

    private TextView tv_Before, tv_sides;
    private CheckBox check_rotation, check_auto_send;
    private Button btnUp, btnDown, btnLeft, btnRight, btnStop, btnapply;
    private EditText edit_Delayms;

    private float[] orientations = new float[3];//保存获取的陀螺仪的值
    private int Send_Data = 0;

    private Boolean ischecked = false;//是否启用陀螺仪
    private Boolean isOpen = false;//陀螺仪监听是否开启
    private Boolean auto_send = false;//是否自动发送

    public Fragment3() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment3, container, false);

        init(view);
        Listener();
        AllThread();

        return view;
    }

    private void init(View view){
        tv_Before = view.findViewById(R.id.tv_angle_before_and_after);
        tv_sides = view.findViewById(R.id.tv_angle_left_and_right);

        btnUp = view.findViewById(R.id.btn_up);
        btnDown = view.findViewById(R.id.btn_down);
        btnLeft = view.findViewById(R.id.btn_left);
        btnRight = view.findViewById(R.id.btn_right);
        btnStop = view.findViewById(R.id.btn_stop);
        btnapply = view.findViewById(R.id.btn_apply);

        edit_Delayms = view.findViewById(R.id.edit_delayms);

        check_rotation = view.findViewById(R.id.cb_check);
        check_auto_send = view.findViewById(R.id.cb_auto_send);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void Listener(){
        btnapply.setOnClickListener(this);

        btnUp.setOnTouchListener(this);
        btnDown.setOnTouchListener(this);
        btnLeft.setOnTouchListener(this);
        btnRight.setOnTouchListener(this);
        btnStop.setOnTouchListener(this);


        check_rotation.setOnCheckedChangeListener(this);
        check_auto_send.setOnCheckedChangeListener(this);

    }

    public void AllThread(){
        //陀螺仪线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if( ((MainActivity) getActivity()).isEnd() ) {
                        try {
                            if (ischecked) {

                                if (!isOpen) {
                                    isOpen = ((MainActivity) getActivity()).setRotationListener();//开启陀螺仪监听
                                }

                                if (((MainActivity) getActivity()).isEnd()) {
                                    break;
                                }

                                Thread.sleep(60);
                                orientations = ((MainActivity) getActivity()).GetOrientation();

                                tv_Before.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_Before.setText(String.format("%s", (int) orientations[1]));
                                        tv_sides.setText(String.format("%s", (int) orientations[2]));
                                    }
                                });
                            } else {
                                if (isOpen) {
                                    isOpen = ((MainActivity) getActivity()).closeRotationListener();//关闭监听
                                }
                                Thread.sleep(60);
                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
                        break;
                    }
                }
            }
        }).start();

        //自动发送线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if( !((MainActivity) getActivity()).isEnd() ) {
                        if (((MainActivity) getActivity()).isReady()) {
                            if (auto_send) {
                                try {
                                    ((MainActivity) getActivity()).SendNum(Send_Data);
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                if (Send_Data != 0) {
                                    ((MainActivity) getActivity()).SendNum(Send_Data);
                                    Send_Data = 0;
                                }
                            }
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }else{
                        break;
                    }
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        if( ((MainActivity)getActivity()).isReady() || (v.getId() == R.id.btn_apply) ) {
            switch (v.getId()) {
                case R.id.btn_apply:
                    int ms = Integer.valueOf(String.valueOf(edit_Delayms.getText()), 10);
                    ((MainActivity) getActivity()).SetSleepMs(ms);
                    ToastShow("应用成功");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.cb_check:
                if( isChecked ){
                    ischecked = true;
                }else{
                    ischecked = false;
                }
                break;
            case R.id.cb_auto_send:
                if( isChecked ){
                    auto_send = true;
                }else{
                    auto_send = false;
                }
                break;
            default:
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if( action == MotionEvent.ACTION_DOWN ) {
            switch (v.getId()) {
                case R.id.btn_up:
                    Send_Data = 0x01;
                    break;
                case R.id.btn_down:
                        Send_Data = 0x02;
                    break;
                case R.id.btn_left:
                        Send_Data = 0x04;
                    break;
                case R.id.btn_right:
                        Send_Data = 0x08;
                    break;
                case R.id.btn_stop:
                        Send_Data = 0x10;
                    break;
                default:
                    break;
            }
        }else if( action == MotionEvent.ACTION_UP && auto_send ){
            Send_Data = 0x00;
        }
        return false;
    }

    private void ToastShow(String text){
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setText("- "+text+" -");
        toast.show();
    }



}
