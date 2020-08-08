package com.example.fragment;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment1 extends Fragment implements View.OnClickListener{

    Boolean ConnectOrDis = true;
    Boolean isOpen = false;
    private int a;
    //更新UI图片
    private Drawable connect_x32;
    private Drawable connect_x32_press;
    private Drawable device_x32;
    private Drawable device_x32_press;
    private Drawable control_x32;
    private Drawable control_x32_press;
    //更新UI颜色
    private String Green = "#1AFA29";
    private String Black = "#000000";

    private TextView tv_connect, tv_device, tv_control;
    private ViewPager viewPager;
    private ListView mlistView;
    private Button btnLookup,btnDisConnect;

    static BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private OutputStream output = null;
    private InputStream input =null;

    //定义一个列表，存蓝牙设备的地址。
    static public ArrayList<String> arrayList=new ArrayList<>();
    //定义一个列表，存蓝牙设备地址，用于显示。
    static public ArrayList<String> deviceName=new ArrayList<>();

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    MyListAdapter myListAdapter = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public Fragment1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_fragment1, container, false);

        init(view);//初始化
        Listener();//监听  更新UI

        return view;
    }

    private void init(View view){

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//注册广播接收信号
        getActivity().getApplicationContext().registerReceiver(bluetoothReceiver, intentFilter);//用BroadcastReceiver 来取得结果

        //获取控件
        btnLookup = view.findViewById(R.id.btnLookup);
        btnDisConnect = view.findViewById(R.id.btnDisConnect);

        //设置点击事件
        btnLookup.setOnClickListener(this);
        btnDisConnect.setOnClickListener(this);

        //ListView
        mlistView = view.findViewById(R.id.LV1);
        myListAdapter = new MyListAdapter(getActivity(), deviceName);
        mlistView.setAdapter(myListAdapter);

        //获取MainActivity的控件
        MainActivity activity = (MainActivity) getActivity();
        tv_connect = activity.findViewById(R.id.tv1);
        tv_device = activity.findViewById(R.id.tv2);
        tv_control = activity.findViewById(R.id.tv3);
        viewPager = activity.findViewById(R.id.vp);

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

        //ListView里面的点击事件
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            String string;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                device = bluetoothAdapter.getRemoteDevice(arrayList.get(position));
                bluetoothAdapter.cancelDiscovery();
                ((MainActivity)getActivity()).SetSecondaryTitle("正在连接..");
                ((MainActivity)getActivity()).ConnectDevice(device);
            }
        });
    }

    private void Listener() {
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                RenewUI(position);//更新UI
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    private void ToastShow(String text){
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setText("- "+text+" -");
        toast.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLookup:
                if(bluetoothAdapter.isEnabled()) {
                    myListAdapter.Clear();//清空ListView
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();//取消搜索
                    }
                    bluetoothAdapter.startDiscovery();//开始搜索
                    ((MainActivity)getActivity()).SetSecondaryTitle("正在搜索..");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(12000);
                                ((MainActivity)getActivity()).SetSecondaryTitle("搜索完成");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    ToastShow("开始搜索");
                }else{
                    if( bluetoothAdapter != null ) {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        getActivity().startActivityForResult(intent, 0);//请求打开蓝牙

                        myListAdapter.Clear();//清空ListView
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while(!bluetoothAdapter.isEnabled());
                                isOpen = true;
                                ((MainActivity)getActivity()).SetSecondaryTitle("正在搜索..");
                                if (bluetoothAdapter.isDiscovering()) {
                                    bluetoothAdapter.cancelDiscovery();//取消搜索
                                }
                                bluetoothAdapter.startDiscovery();//开始搜索
                                while(true){
                                    try {
                                        Thread.sleep(12000);
                                        ((MainActivity)getActivity()).SetSecondaryTitle("搜索完成");
                                        break;
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                    }
                    else{
                        ToastShow("该设备不支持蓝牙");
                    }
                }
                break;
            case R.id.btnDisConnect:
                ((MainActivity) Objects.requireNonNull(getActivity())).CloseConnected();
                ToastShow("已断开连接");
                break;
            default:
                break;
        }
    }

    public void RenewUI(int p) {
        switch (p){
            case 0:
                //设置图片颜色
                tv_connect.setCompoundDrawables(null,connect_x32_press,null,null);
                tv_device.setCompoundDrawables(null,device_x32,null,null);
                tv_control.setCompoundDrawables(null,control_x32,null,null);

                //设置文字颜色
                tv_connect.setTextColor(Color.parseColor(Green));
                tv_device.setTextColor(Color.parseColor(Black));
                tv_control.setTextColor(Color.parseColor(Black));

                ((MainActivity)getActivity()).SetSecondaryTitle("连接");

                break;
            case 1:
                //设置图片颜色
                tv_connect.setCompoundDrawables(null,connect_x32,null,null);
                tv_device.setCompoundDrawables(null,device_x32_press,null,null);
                tv_control.setCompoundDrawables(null,control_x32,null,null);

                //设置文字颜色
                tv_connect.setTextColor(Color.parseColor(Black));
                tv_device.setTextColor(Color.parseColor(Green));
                tv_control.setTextColor(Color.parseColor(Black));

                ((MainActivity)getActivity()).SetSecondaryTitle("上位机");

                break;
            case 2:
                //设置图片颜色
                tv_connect.setCompoundDrawables(null,connect_x32,null,null);
                tv_device.setCompoundDrawables(null,device_x32,null,null);
                tv_control.setCompoundDrawables(null,control_x32_press,null,null);

                //设置文字颜色
                tv_connect.setTextColor(Color.parseColor(Black));
                tv_device.setTextColor(Color.parseColor(Black));
                tv_control.setTextColor(Color.parseColor(Green));

                ((MainActivity)getActivity()).SetSecondaryTitle("控制");

                break;
            default:
                break;
        }
    }

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if( BluetoothDevice.ACTION_FOUND.equals(action) ){
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceName.add("设备名："+device.getName()+"\n" +"设备地址："+device.getAddress() + "\n");//将搜索到的蓝牙名称和地址添加到列表。
                arrayList.add( device.getAddress());//将搜索到的蓝牙地址添加到列表。
                myListAdapter.notifyDataSetChanged();//更新
            }
        }
    };


    public static class MyListAdapter extends BaseAdapter{

        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private ArrayList<String> mArrayList;

        public MyListAdapter(Context context, ArrayList<String> arrayList1){
            this.mContext = context;
            this.mArrayList = arrayList1;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public final class ViewHolder{
            public TextView textView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        public void Clear(){
            if(mArrayList!=null) {
                mArrayList.clear();
            }
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.listview_item, null);
                viewHolder = new ViewHolder();
                viewHolder.textView = convertView.findViewById(R.id.TV1);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //给控件赋值
            if(device != null){
                viewHolder.textView.setText(mArrayList.get(position));
//                viewHolder.textView.setText(("设备名："+device.getName()+"\n"+"设备地址："+device.getAddress()));
            }else {
                viewHolder.textView.setText("设备名：null\n设备地址：null\n");
            }
//            viewHolder.textView.setText("设备名：null\n设备地址：null\n");
            return convertView;
        }
    }

//    //    1.创建接口和方法
//    public interface MessageSendListener{
//        void MessageSendNuber(int num);
//        void MessageSendString(String str);
//    }
//    //    2.定义接口变量
//    public MessageSendListener messageSendListener;

//    //    4.暴露一个公共的方法
//    public void setMessageSendListener(MessageSendListener mmessageSendListener) {
//        this.messageSendListener = mmessageSendListener;
//    }
}
