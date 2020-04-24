package com.example.fota_stm32f411;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;


public class ListBluetoothActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    ListView listBluetooth;
    ArrayList<String> list ;
    ArrayAdapter<String> adapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_list_bluetooth);
        listBluetooth = findViewById(R.id.listBluetooth);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
        listBluetooth.setAdapter(adapter);

        if(!bluetoothAdapter.isEnabled())
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,MainActivity.Request_open_bluetooth);
        }
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device  : devices ) {
            list.add(device.getName()+"\n"+device.getAddress());
        }
        adapter.notifyDataSetChanged();
        listBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = list.get(position);
                String [] ten_diachi = data.split("\n");
                String diachi = ten_diachi[1];
                String ten = ten_diachi[0];
                //Toast.makeText(ListblutoothActivity.this,diachi,Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("addr",diachi);
                intent.putExtra("ten",ten);
                setResult(RESULT_OK,intent);

                finish();
            }
        });
    }

}
