package com.example.fota_stm32f411;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class BluetoothSupport {
    static final int  REQ_CODE_EN_BLUETOOTH=101;
    private  Handler handler;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter BA;
    private BluetoothSocket bluetoothSocket=null;
    private Context context;
    private ConnectThread connectThread;
    private  StringBuilder dataReceive;
    private OnReceiveData onReceiveData;
    private OnConnect onConnect;
    private String endOfLine ="!";
    public void setOnConnectListener(OnConnect onConnect)
    {
        this.onConnect=onConnect;
    }
    public void close()
    {
        if(BA.isEnabled())BA.disable();
    }
    public boolean isConnect()
    {
        if(bluetoothSocket!=null) return true;
        else return false;
    }
    public void setOnReceiveDataListener(OnReceiveData onReceiveData)
    {
        this.onReceiveData=onReceiveData;
    }
    public void setOnReceiveDataListener(String endOfLine,OnReceiveData onReceiveData)
    {
        this.onReceiveData=onReceiveData;
        this.endOfLine=endOfLine;
    }
    public BluetoothSupport(Context context)
    {
        dataReceive=new StringBuilder();
        this.context=context;
        BA= BluetoothAdapter.getDefaultAdapter();
        if(!BA.isEnabled())
        {
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((MainActivity)context).startActivityForResult(intent,REQ_CODE_EN_BLUETOOTH);
        }
        handler=new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what==0)
                {
                    String data= (String) msg.obj;
                    dataReceive.append(data);
                    int endOfLine=dataReceive.indexOf(BluetoothSupport.this.endOfLine);
                    if(endOfLine>0)
                    {
                        //Log.d("test",dataReceive.toString());
                        String dataHienThi=dataReceive.substring(0,endOfLine);
                        if(onReceiveData!=null) onReceiveData.receive(dataHienThi);
                        dataReceive.delete(0,dataReceive.length());
                    }
                }
                else if(msg.what==1)//connected
                {
                    if (onConnect!=null) onConnect.connected();
                }
                else //error
                {
                    if (onConnect!=null) onConnect.error();
                }
            }
        };
    }
    public void write(String data)
    {
        connectThread.sendData(data);
    }
    public void disConnect()
    {
        try {
            bluetoothSocket.close();
            bluetoothSocket=null;
            if(onConnect!=null) onConnect.error();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void connect(String addr)
    {

        BluetoothDevice bluetoothDevice=BA.getRemoteDevice(addr);
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e1) {
            //  Toast.makeText(getBaseContext(), "Lỗi tạo socket", Toast.LENGTH_SHORT).show();
            //if (onConnect!=null) onConnect.error();
            handler.sendEmptyMessage(2);//error
        }
        Thread tr=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bluetoothSocket.connect();
                    connectThread=new BluetoothSupport.ConnectThread(bluetoothSocket);
                    connectThread.start();
                    handler.sendEmptyMessage(1);//connected
                } catch (IOException e) {
                    try {
                        bluetoothSocket.close();        //If IO exception occurs attempt to close socket
                        bluetoothSocket=null;
                        handler.sendEmptyMessage(2);//error
                    } catch (IOException e2) {
                    }
                }
            }
        });
        tr.start();
        //if (onConnect!=null) onConnect.connected();
    }
    interface OnConnect
    {
        public void connected();
        public void error();
    }
    interface OnReceiveData
    {
        public void receive(String data);
    }
    private class ConnectThread extends Thread
    {
        OutputStream outputStream;
        InputStream inputStream;
        public ConnectThread(BluetoothSocket bluetoothSocket) {
            try
            {
                inputStream=bluetoothSocket.getInputStream();
                outputStream=bluetoothSocket.getOutputStream();
            }catch (Exception e)
            {};
        }
        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = inputStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    handler.obtainMessage(0, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void sendData(String data)
        {
            byte[]bytes= new byte[0];
            try {
                bytes = data.getBytes("ISO_8859_1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                //Toast.makeText(SendReceiveDataActivity.this, "Không kết nối được tới thiết bị", Toast.LENGTH_SHORT).show();
                //error=true;
                // finish();
            }
        }
    }
}
