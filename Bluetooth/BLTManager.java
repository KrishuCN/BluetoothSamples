package com.example.administrator.myapplication.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static com.example.administrator.myapplication.Bluetooth.ReceiveSocketService.receiveMessage;

/**
 * Created by Administrator on 2017/6/27 0027.
 */

public class BLTManager {

    private static BluetoothAdapter bltAdapter = null;
    private static BLTManager bltManager = null;
    private BluetoothSocket mBluetoothSocket;

    static BluetoothAdapter getDefaultAdapter() {
        if (bltAdapter == null) {
            synchronized (BLTManager.class) {
                if (bltAdapter == null) {
                    bltAdapter = BluetoothAdapter.getDefaultAdapter();
                }
            }
        }
        return bltAdapter;
    }

    public static BLTManager getInstance() {
        if (bltManager == null) {
            synchronized (BLTManager.class) {
                if (bltAdapter == null) {
                    bltManager = new BLTManager();
                }
            }
        }
        return bltManager;
    }

    /**
     * 尝试配对和连接
     * 如果这个设备取消了配对，则尝试配对
     * 如果这个设备已经配对完成，则尝试连接
     *
     * @param btDev
     */
    void createBond(BluetoothDevice btDev, Handler handler) {
        if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                btDev.createBond();
            }
        } else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
            connect(btDev, handler);

        }
    }


    /**
     * 开启服务连接蓝牙
     * UUID连接不稳定  使用反射作为补充进行二次连接
     * 效果相同
     */

    private void connect(final BluetoothDevice btDev, final Handler handler) {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    //需要服务商提供UUID
                    mBluetoothSocket = btDev.createRfcommSocketToServiceRecord(BLTContant.SPP_UUID);

                    if (mBluetoothSocket != null)
                        BluetoothUtils.log("bluetooth start connect...");
                    if (bltAdapter.isDiscovering()) {
                        bltAdapter.cancelDiscovery();
                    }
                    if (!mBluetoothSocket.isConnected()) {
                        mBluetoothSocket.connect();
                    }

                    if (handler == null) return;
                    Message message = new Message();
                    message.what = 3;
                    message.obj = mBluetoothSocket;
                    handler.sendMessage(message);

                    receiveMessage(handler, mBluetoothSocket); // 连接成功后开启sock流

                } catch (Exception e) {
                    BluetoothUtils.log("bluetooth connect failed,try to use reflect");
                    try {
                        mBluetoothSocket.close();
                        mBluetoothSocket = null;
                        mBluetoothSocket = (BluetoothSocket) btDev.getClass().
                                getMethod("createRfcommSocket", new Class[]{int.class}).invoke(btDev, 1);

                        if (mBluetoothSocket.isConnected()
                                && mBluetoothSocket.getRemoteDevice().getName().equals(btDev.getName())
                                && mBluetoothSocket.getRemoteDevice().getAddress().equals(btDev.getAddress())
                                ) {
                            BluetoothUtils.log("reflect connect success: " + "device name: " +
                                    mBluetoothSocket.getRemoteDevice().getName()
                                    + "  MAC address : " + mBluetoothSocket.getRemoteDevice().getAddress());
                            Message msg = new Message();
                            msg.what = 3;
                            msg.obj = mBluetoothSocket;
                            handler.sendMessage(msg);
                            receiveMessage(handler, mBluetoothSocket); // 连接成功后开启sock流
                        }
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    } catch (InvocationTargetException | NoSuchMethodException | IOException e1) {
                        BluetoothUtils.log("reflect connect failed");
                        e1.printStackTrace();
                    }
/*                    try
                    {
                        mBluetoothSocket.close();
                    } catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }*/
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
