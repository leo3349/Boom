package com.leo.boom.activity;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.leo.boom.R;
import com.leo.boom.adapter.BluetoothDeviceAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements BluetoothDeviceAdapter.OnItemClickListener {
    private final String TAG = "MainActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 0X0001;
    private final int RESULT_OK_BT = -0X0001;
    private final Handler mHandler = new Handler();
    private BtFoundReceiver mBtFoundReceiver;
    private RecyclerView mRvDevice;
    private BluetoothDeviceAdapter mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();

        initBluetooth();
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.not_support_bluetooth, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "not support bluetooth");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        getDevices();
    }

    private void initView() {
        mRvDevice = (RecyclerView) findViewById(R.id.rv);
        mRvDevice.setLayoutManager(new LinearLayoutManager(this));
        mDeviceAdapter = new BluetoothDeviceAdapter();
        mRvDevice.setAdapter(mDeviceAdapter);
        mRvDevice.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final int OFFSET = 6;

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(0, 0, 0, OFFSET);
            }

            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
                final int left = parent.getPaddingLeft();
                final int right = parent.getWidth() - parent.getPaddingRight();

                final int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    final View child = parent.getChildAt(i);
                    final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                            .getLayoutParams();
                    final int top = child.getBottom() + params.bottomMargin;
                    final int bottom = top + OFFSET;
                    Rect rect = new Rect(left, top, right, bottom);
                    Paint paint = new Paint();
                    paint.setColor(Color.BLACK);
                    c.drawRect(rect, paint);
                }
            }
        });

        mDeviceAdapter.setOnItemClickListener(this);
    }


    private void initData() {
    }

    private void getDevices() {
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        mDeviceAdapter.addAll(bondedDevices);
        Log.d(TAG, "bonded devices " + bondedDevices.size());
        discoveryBtDevice();
    }

    private void discoveryBtDevice() {
        mBtFoundReceiver = new BtFoundReceiver();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(mBtFoundReceiver, filter);
        boolean b = mBluetoothAdapter.startDiscovery();
        Log.d(TAG, "start descovery " + b);
    }

    private void stopDiscoveryBtDevice() {
        mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (RESULT_OK_BT == resultCode) {
                getDevices();
            } else {
                Toast.makeText(this, R.string.please_open_bluetooth, Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1500);
            }
        }
    }

    @Override
    public void onItemCLick(int postion, BluetoothDevice device, View view) {
        stopDiscoveryBtDevice();
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            //connect
            connectBt(device);
        } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            pair(device);
        }

    }

    private void connectBt(final BluetoothDevice device) {
        mBluetoothAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                BluetoothA2dp p = (BluetoothA2dp) proxy;
                int connectionState = p.getConnectionState(device);
                Log.d(TAG, "onServiceConnected " + connectionState);
                if (connectionState != BluetoothProfile.STATE_CONNECTED) {
                    try {
                        p.getClass()
                                .getMethod("connect", BluetoothDevice.class)
                                .invoke(p, device);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, "请播放音乐", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {

            }
        }, BluetoothProfile.A2DP);
    }

    private void pair(BluetoothDevice device) {
        device.createBond();
    }

    private class BtFoundReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "device " + device.getAddress() + " " + device.getName());
                mDeviceAdapter.addItem(device);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "device " + device.getAddress() + " " + device.getName());
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("BlueToothTestActivity", "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d("BlueToothTestActivity", "完成配对");
                        connectBt(device);//连接设备
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d("BlueToothTestActivity", "取消配对");
                    default:
                        Log.d("BlueToothTestActivity", "配对 " + device.getBondState());
                        break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothAdapter.isDiscovering())
            stopDiscoveryBtDevice();
        if (mBtFoundReceiver != null)
            unregisterReceiver(mBtFoundReceiver);
    }
}
