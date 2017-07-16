package com.example.administrator.myapplication.Bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.administrator.myapplication.Application.BaseApplication;

import java.io.IOException;

/**
 * Created by Huyao on 2017/6/28.
 * 当普通蓝牙作为蓝牙服务端时开启的服务
 */
public class BLTService {

    /**
     * 单例
     */
    private BLTService() {
        createBltService();
    }

    private static class BlueToothServices {
        private static BLTService bltService = new BLTService();
    }

    public static BLTService getInstance() {
        return BlueToothServices.bltService;
    }

    private BluetoothServerSocket bluetoothServerSocket;
    private BluetoothSocket socket;

    public BluetoothSocket getSocket() {
        return socket;
    }

    public BluetoothServerSocket getBluetoothServerSocket() {
        return bluetoothServerSocket;
    }

    /**
     * 从蓝牙适配器中创建一个蓝牙服务作为服务端，在获得蓝牙适配器后创建服务器端
     */
    private void createBltService() {
        try {
            if (BLTManager.getDefaultAdapter() != null && BLTManager.getDefaultAdapter().isEnabled()) {
                // TODO 服务端可自定义UUID
                bluetoothServerSocket = BLTManager.getDefaultAdapter().listenUsingRfcommWithServiceRecord("com.zhonghang.bletest", BLTContant.SPP_UUID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run(final Handler handler) {
        //服务器端的bltsocket需要传入uuid和一个独立存在的字符串
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //线程阻塞，直到有蓝牙设备链接进来才会往下走
                        socket = getBluetoothServerSocket().accept();
                        if (socket != null) {
                            BaseApplication.bluetoothSocket = socket;
                            //回调结果通知
                            Message message = new Message();
                            message.what = 4;
                            message.obj = socket.getRemoteDevice();
                            handler.sendMessage(message);
                            getBluetoothServerSocket().close();
                            //break;
                        }
                    } catch (IOException e) {
                        try {
                            getBluetoothServerSocket().close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }).start();
    }


    public void cancel() {
        try {
            getBluetoothServerSocket().close();
        } catch (IOException e) {
            Log.e("blueTooth", "关闭服务器socket失败");
        }
    }
}
