package com.example.administrator.myapplication.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;

import java.util.List;

/**
 * Created by Huyao on 2017/6/30 0030.
 * <p>
 * BLE设备操作类
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEManager {

    private static BLEManager bleManager = null;
    private static BluetoothAdapter bleAdapter = null;
    private List<BluetoothGattService> mServiceList;
    private Handler handler;

    static BLEManager getInstance() {
        if (bleManager == null) {
            synchronized (BLEManager.class) {
                if (bleManager == null) {
                    bleManager = new BLEManager();
                }
            }
        }
        return bleManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    static BluetoothAdapter getBleAdapter(BluetoothManager manager) {
        if (bleAdapter == null) {
            synchronized (BLTManager.class) {
                if (bleAdapter == null) {
                    bleAdapter = manager.getAdapter();
                }
            }
        }
        return bleAdapter;
    }


    /**
     * 建立GATT连接
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    void connectToGATTServer(BluetoothDevice device, Context context, android.os.Handler mHandler) {
        if (mHandler != null) {
            handler = mHandler;
        }

        //函数成功，返回BluetoothGatt对象，它是GATT profile的封装。通过这个对象，我们就能进行GATT Client端的相关操作。BluetoothGattCallback用于传递一些连接状态及结果。
        BluetoothGatt bluetoothGatt = device.connectGatt(context, false, gattCallback);
        boolean discoverResult = bluetoothGatt.discoverServices();

/*        //连接远程设备
        boolean connectResult = bluetoothGatt.connect();
        //搜索连接设备所支持的service
        //断开与远程设备的GATT连接
        bluetoothGatt.disconnect();
        //关闭GATT Client端
        bluetoothGatt.close();
        //读取指定的characteristic。
        //boolean readResult = bluetoothGatt.readCharacteristic(characteristic);
        //设置当指定characteristic值变化时，发出通知
        //boolean setResult = bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        //获取远程设备所支持的services
        List<BluetoothGattService> gattServices = bluetoothGatt.getServices();*/
    }


    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        /*BLE连接状态的回调*/
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    sendToHandler("gatt connect success");
                    gatt.discoverServices();//连接成功，开始搜索服务

                    // TODO 更换设备UUID 得到BLE设备对应的Service
                    BluetoothGattService service = gatt.getService(BLTContant.SPP_UUID);
                    if (service != null) {
                        // TODO 更换UUID 得到服务对应的Characteristic
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BLTContant.SPP_UUID);

                        //设置characteristic的通知，为true onCharacteristicRead和onCharacteristicWrite才会被回调
                        gatt.setCharacteristicNotification(characteristic, true);

                        //向设备写入数据  ps:读和写是异步操作 收数据应该放在回调中进行
                        characteristic.setValue(BluetoothUtils.hexStringToBytes("This data is test for write！"));
                        gatt.writeCharacteristic(characteristic);
                    }

                }
            }

            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                sendToHandler("gatt disconnect");
                gatt.close();
            }
        }

        /*发现设备Service的回调*/
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            mServiceList = gatt.getServices();
            sendToHandler("onServicesDiscovered status" + status);
            if (mServiceList != null) {
                sendToHandler("target device opened " + mServiceList.size() + " services");

                for (BluetoothGattService service : mServiceList) {
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    sendToHandler("scaned servic's UUID ：" + service.getUuid());

                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        sendToHandler("scaned characteristic UUID: " + characteristic.getUuid());
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String value = BluetoothUtils.parseBytesToHexString(characteristic.getValue());
            sendToHandler("onCharacteristicChanged ： " + value);
        }

        /*读和写的回调*/
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            String value = BluetoothUtils.parseBytesToHexString(characteristic.getValue());
            sendToHandler( "received message: " + value + "from device ：" + gatt.getDevice().getName() +" UUID : " + characteristic.getUuid());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            sendToHandler("write to  " + gatt.getDevice().getName() + " success !");

            //发送指令后获得设备返回码
            gatt.readCharacteristic(characteristic);
            //gatt.disconnect();

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }
    };

    /**
     * 发送结果回handler
     *
     * @param str
     */
    private void sendToHandler(String str) {
        Message msg = new Message();
        msg.what = 2;
        msg.obj = str;
        handler.sendMessage(msg);
    }

}
