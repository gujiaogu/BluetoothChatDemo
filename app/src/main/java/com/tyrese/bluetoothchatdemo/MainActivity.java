package com.tyrese.bluetoothchatdemo;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
    public static final int MESSAGE_SUCCESS = 5;
    public static final int MESSAGE_FAIL = 6;
    public static final int CONNECTING = 7;
    public static final int CONNECTING_FAIL = 8;
    public static final int CONNECTING_SUCCESS = 9;
    public static final int CONNECTION_LOST = 10;
    public static final int MESSAGE_START = 11;


    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListPaired;
    private ListView mListScan;
    private BluetoothListAdapter mAdapterPaired;
    private BluetoothListAdapter mAdapterScan;
    private ConnectThread connectThread;
    private ConnectFileThread connectFileThread;
    private BluetoothDevice connectedDevice;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_START:
                    Toast.makeText(MainActivity.this, "开始传输文件，请不要退出", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_SUCCESS:
                    Toast.makeText(MainActivity.this, "文件传输成功！", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_FAIL:
                    Toast.makeText(MainActivity.this, "文件传输失败！", Toast.LENGTH_SHORT).show();
                    break;
                case CONNECTING:
                    getSupportActionBar().setSubtitle("连接中");
                    break;
                case CONNECTING_FAIL:
                    getSupportActionBar().setSubtitle("连接失败");
                    break;
                case CONNECTING_SUCCESS:
                    getSupportActionBar().setSubtitle("连接成功");
                    break;
                case CONNECTION_LOST:
                    getSupportActionBar().setSubtitle("失去连接");
                    connectThread = null;
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setSubtitle("未连接");

        mListPaired = (ListView) findViewById(R.id.paired_list);
//        mListScan = (ListView) findViewById(R.id.scan_list);
        mAdapterPaired = new BluetoothListAdapter(this);
        mAdapterScan = new BluetoothListAdapter(this);
        mListPaired.setAdapter(mAdapterPaired);
//        mListScan.setAdapter(mAdapterScan);

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
                if (connectThread != null && connectThread.getConnectState() == ConnectThread.STATE_CONNECTED) {
                    Toast.makeText(MainActivity.this, "你已经连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                DeviceItem item = (DeviceItem) parent.getItemAtPosition(position);
                connectThread = new ConnectThread(item.getDevice(), mHandler);
                connectThread.start();
                connectedDevice = item.getDevice();
            }
        });

//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, filter);
    }

//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                mAdapterScan.addItem(new DeviceItem(device));
//            }
//        }
//    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send:
                sendMessage();
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
    }

    @Override
    protected void onDestroy() {
//        unregisterReceiver(mReceiver);
        if (connectThread != null) {
            connectThread.cancel();
        }
        if (connectFileThread != null) {
            connectFileThread.cancel();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (REQUEST_CODE_FOR_IMAGE == requestCode) {
                final Uri uri = data.getData();
                LogWrapper.d(uri.toString());
                String path = "";
                if (uri.toString().startsWith("content")) {
                    path = getRealPathFromURI(uri);
                } else if (uri.toString().startsWith("file")) {
                    path = uri.getPath();
                } else {
                    return;
                }
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_SEND);
//                intent.setType("*/*");
//                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(uri.getPath())));
//                startActivity(intent);
                if (connectedDevice == null
                        || connectThread == null
                        || connectThread.getConnectState() == ConnectThread.STATE_OFF) {
                    Toast.makeText(MainActivity.this, "你还没有连接设备", Toast.LENGTH_SHORT).show();
                    return;
                }
                connectFileThread = new ConnectFileThread(connectedDevice, mHandler, path);
                connectFileThread.start();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (connectFileThread != null
                && connectFileThread.getSendState() == ConnectFileThread.STATE_FILE_SENDING) {
            Toast.makeText(this, "正在传送文件请不要退出！", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    private void selectPic() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, REQUEST_CODE_FOR_IMAGE);
    }

    private void sendMessage() {
        if (connectThread == null ||
                connectThread.getConnectState() != ConnectThread.STATE_CONNECTED) {
            Toast.makeText(MainActivity.this, "你还没有连接设备", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发送消息");
        View view = LayoutInflater.from(this).inflate(R.layout.message_input, null);
        final EditText content = (EditText) view.findViewById(R.id.text_input);
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String msg = content.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    return;
                }

                if (connectThread == null ||
                        connectThread.getConnectState() != ConnectThread.STATE_CONNECTED) {
                    Toast.makeText(MainActivity.this, "你还没有连接设备", Toast.LENGTH_SHORT).show();
                    return;
                }

                connectThread.write(msg.getBytes());
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        StreamUtil.close(cursor);
        return res;
    }
}
