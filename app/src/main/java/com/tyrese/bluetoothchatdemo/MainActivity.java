package com.tyrese.bluetoothchatdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_CODE_FOR_IMAGE = 4;
    private static final String TAG_FIEL = "fileiscoming";

    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListPaired;
    private ListView mListScan;
    private BluetoothListAdapter mAdapterPaired;
    private BluetoothListAdapter mAdapterScan;
    private ConnectThread connectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListPaired = (ListView) findViewById(R.id.paired_list);
        mListScan = (ListView) findViewById(R.id.scan_list);
        mAdapterPaired = new BluetoothListAdapter(this);
        mAdapterScan = new BluetoothListAdapter(this);
        mListPaired.setAdapter(mAdapterPaired);
        mListScan.setAdapter(mAdapterScan);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "您的设备不支持蓝牙！", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mAdapterPaired.addItem(new DeviceItem(device));
            }
        }

        mListPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DeviceItem item = (DeviceItem) parent.getItemAtPosition(position);
                connectThread = new ConnectThread(item.getDevice(), null);
                connectThread.start();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mAdapterScan.addItem(new DeviceItem(device));
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send:
                connectThread.write("测试消息".getBytes());
                break;
            case R.id.menu_send_pic:
                selectPic();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectThread != null) {
            connectThread.cancel();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (REQUEST_CODE_FOR_IMAGE == requestCode) {
                final Uri uri = data.getData();
                LogWrapper.d(uri.toString());
                Thread t = new WriteFileTask(uri.getPath());
                t.start();
            }
        }
    }

    private void selectPic() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, REQUEST_CODE_FOR_IMAGE);
    }

    private class WriteFileTask extends Thread {

        private String filePath;

        public WriteFileTask(String path) {
            this.filePath = path;
        }

        @Override
        public void run() {
            LogWrapper.d("Write file thread was started");
            FileInputStream fis;
            try {
                File file = new File(filePath);
                if (!file.isFile() || !file.exists()) {
                    return;
                }
                connectThread.write(TAG_FIEL.getBytes());
                fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                while (fis.read(buffer, 0, buffer.length) != -1) {
                    connectThread.write(buffer);
                }
                connectThread.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
