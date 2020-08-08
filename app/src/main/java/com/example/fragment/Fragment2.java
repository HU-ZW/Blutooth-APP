package com.example.fragment;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.icu.text.UFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment2 extends Fragment implements View.OnClickListener{

    private Button button_Synchronization;//同步
    private Button btnClear, btnStop, btnStart;
    private EditText edit1;
    private EditText edit_data1,edit_data2,edit_data3,edit_data4, edit_control;
    private RadioGroup rg_Choose;
    private TextView tv_input_data1, tv_input_data2, tv_input_data3, tv_input_data4, tv_input_control;

    private BluetoothSocket socket=null;
    private InputStream inputStream=null;
    private String str=null;

    private Boolean isHEX = true;

    public Fragment2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment2, container, false);

        init(view);
        Listener();


        rg_Choose.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.rb_HEX:
                        isHEX = true;
                        break;
                    case R.id.rb_decimal:
                        isHEX = false;
                        break;
                    default:
                        break;
                }
            }
        });

        new Thread(new Runnable() {//数据接收线程
            @Override
            public void run() {
                while( ! (((MainActivity) Objects.requireNonNull(getActivity())).isReady()) );

                try {
                    if( !(((MainActivity)getActivity()).isEnd()) ) {
                        socket = ((MainActivity) getActivity()).GetSocket();
                        inputStream = socket.getInputStream();
                        while (true) {
                            final byte[] buffer = new byte[1024];
                            final int[] shorts = new int[1024];
                            final int[] DealShorts = new int[1024];
                            Boolean isAgreementData = false;

                            if (!((MainActivity) getActivity()).isReady()) {//断开连接后退出线程
                                break;
                            }

                            if (inputStream.available() <= 0) {
                                continue;
                            } else {
                                Thread.sleep(((MainActivity) getActivity()).GetSleepMs());
                            }
                            final int count = inputStream.read(buffer);
                            ((MainActivity) getActivity()).Byte2Unsigned(buffer, count, shorts);//将数据转换为无符
                            isAgreementData = ((MainActivity) getActivity()).DealData(shorts, DealShorts, count);//将数据整合 并判断是否是所需数据
                            str = new String(buffer, 0, count, "gb2312");
                            final Boolean finalIsAgreementData = isAgreementData;
                            edit1.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (isHEX == true) {
                                        if (finalIsAgreementData) {
                                            final String[] str_data = new String[5];
                                            //添加到监听窗口
                                            for (int i = 0; i < count / 2 - 1; i++) {
                                                edit1.append(DealShorts[i] + " ");
                                            }
                                            edit1.append("\n");

                                            tv_input_control.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String s = Integer.toHexString(DealShorts[0]).toUpperCase();
                                                    tv_input_control.setText( s.length()<=1 ? "0x0"+s: "0x"+s);

                                                    tv_input_data1.setText(DealShorts[1]+"");
                                                    tv_input_data2.setText(DealShorts[2]+"");
                                                    tv_input_data3.setText(DealShorts[3]+"");
                                                    tv_input_data4.setText(DealShorts[4]+"");
                                                }
                                            });
                                        } else {
                                            for (int i = 0; i < count; i++) {
                                                edit1.append(buffer[i] + " ");
                                            }
                                        }
                                    }else{
                                        edit1.append(str + "  ");
                                    }
                                }
                            });
                        }
                    }
                }catch (IOException e){ }
                 catch (InterruptedException e) {
//                    e.printStackTrace();
                }
            }
        }).start();

        return view;
    }

    private void ToastShow(String text){
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setText("- "+text+" -");
        toast.show();
    }

    private void init(View view){
        button_Synchronization = view.findViewById(R.id.btnSynchronization);
        btnClear = view.findViewById(R.id.btnClear);
        btnStop = view.findViewById(R.id.btnStop);
        btnStart = view.findViewById(R.id.btnStart);

        edit1 = view.findViewById(R.id.edit1);
        edit_data1 = view.findViewById(R.id.edit_data1);
        edit_data2 = view.findViewById(R.id.edit_data2);
        edit_data3 = view.findViewById(R.id.edit_data3);
        edit_data4 = view.findViewById(R.id.edit_data4);
        edit_control = view.findViewById(R.id.edit_control);
        edit1.setKeyListener(null);//设置编辑框1 只读

        tv_input_data1 = view.findViewById(R.id.tv_input_data1);
        tv_input_data2 = view.findViewById(R.id.tv_input_data2);
        tv_input_data3 = view.findViewById(R.id.tv_input_data3);
        tv_input_data4 = view.findViewById(R.id.tv_input_data4);
        tv_input_control = view.findViewById(R.id.tv_input_control);

        rg_Choose = view.findViewById(R.id.rg_choose);
    }

    private void Listener(){
        btnClear.setOnClickListener(this);
        button_Synchronization.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        int[] HeadData = new int[2];
        int[] TailData = new int[2];
        HeadData[0] = 0xAA;
        HeadData[1] = 0x55;
        TailData[0] = 0xDF;
        TailData[1] = 0xFD;
        Boolean isClear = false;
        int control = Integer.valueOf(String.valueOf(edit_control.getText()), 16);

        switch (v.getId()){
            case R.id.btnSynchronization://同步按钮

                /*TODO: 10进制与16进制的相互转换*/
//                int a = Integer.valueOf(String.valueOf(edit_control.getText()), 16);//16进制转换为10进制
//                String h = Integer.toHexString(255);//10进制转换为16进制字符串
//                edit1.append(String.format("%d",a)+" ");

                break;
            case R.id.btnStop://停止按钮
                control = (control | 0x80);//高位置1
                break;
            case R.id.btnStart://启动按钮
                control = (control & 0x7F);//高位置0
                break;
            case R.id.btnClear:     //清空按钮+
                edit1.setText("");
                isClear = true;
                break;
            default:
                break;
        }

        if( !isClear ) {
            if (((MainActivity) getActivity()).isReady()) {
                int data1 = Integer.valueOf(String.valueOf(edit_data1.getText()), 10);//将字符串转化为十进制int型
                int data2 = Integer.valueOf(String.valueOf(edit_data2.getText()), 10);
                int data3 = Integer.valueOf(String.valueOf(edit_data3.getText()), 10);
                int data4 = Integer.valueOf(String.valueOf(edit_data4.getText()), 10);

                //帧头
                ((MainActivity) getActivity()).SendNum(HeadData[0]);
                ((MainActivity) getActivity()).SendNum(HeadData[1]);

                //数据部分
                ((MainActivity) getActivity()).SendNum(control);

                ((MainActivity) getActivity()).SendNum(data1 >> 8);     //高八位
                ((MainActivity) getActivity()).SendNum(data1 & 0x00FF); //低八位

                ((MainActivity) getActivity()).SendNum(data2 >> 8);
                ((MainActivity) getActivity()).SendNum(data2 & 0x00FF);

                ((MainActivity) getActivity()).SendNum(data3 >> 8);
                ((MainActivity) getActivity()).SendNum(data3 & 0x00FF);

                ((MainActivity) getActivity()).SendNum(data4 >> 8);
                ((MainActivity) getActivity()).SendNum(data4 & 0x00FF);

                //帧尾
                ((MainActivity) getActivity()).SendNum(TailData[0]);
                ((MainActivity) getActivity()).SendNum(TailData[1]);
            } else {
                ToastShow("请先连接蓝牙");
            }
        }
    }
}
