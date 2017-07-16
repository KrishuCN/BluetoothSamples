package com.example.administrator.myapplication.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Huyao on 七月.
 */

public class BluetoothUtils {


    public static void log(String msg)
    {
        Log.d("Bluetooth", "-------------->蓝牙服务日志 : " + msg + " <----------------");
    }



    /**
     * 启动蓝牙服务
     */
    public static void startBLTService(Context context)
    {
        context.startService(new Intent(context, BluetoothService.class));
    }

    /**
     * 检查蓝牙模块是否可用
     */
    static boolean checkBltAvailable(BluetoothAdapter adapter)
    {
        if (adapter == null)
        {
            return false;
        } else
        {
            return true;
        }
    }

    /**
     * 获取已配对的蓝牙列表
     */
    static List<Map<String, String>> getBondedDevices(BluetoothAdapter adapter)
    {
        Map<String, String> map;
        List<Map<String, String>> deviceMapList = new ArrayList<>();
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        if (devices.size() > 0)
        {
            for (BluetoothDevice device : devices)
            {
                map = new HashMap<>();
                map.put("devicename", device.getName());
                map.put("deviceaddress", device.getAddress());
                deviceMapList.add(map);
            }
        }

        return deviceMapList;
    }

    /**
     * 检测是否支持Ble
     */
    static boolean isSupportBle(Context context)
    {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }


    /**
     * 打开蓝牙
     */
    static void openBluetooth(BluetoothAdapter bleAdapter, Context context) {
        if (!checkBltAvailable(bleAdapter) || !isSupportBle(context)) {
            log("Bluetooth not available");
        } else {
            if (!bleAdapter.isEnabled()) {
                bleAdapter.enable();
                BluetoothUtils.setDiscoverableTimeout(0, bleAdapter);
            }
        }
    }

    /**
     * 后台打开 可见性一直保持
     */
    static void setDiscoverableTimeout(int timeout, BluetoothAdapter adapter)
    {
        try
        {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(adapter, timeout);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeout);
            Log.d("MYTAG", "setDiscoverableTimeout");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * 关闭蓝牙可见性
     */
    private static void closeBluetoothDiscoverable(BluetoothAdapter adapter)
    {

        try
        {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, 1);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE, 1);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }





    /**
     *  Byte[]转为String
     * @param b
     * @return
     */
    static String parseBytesToHexString(byte[] b)
    {
        String a = "";
        for (int i = 0; i < b.length; i++)
        {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            a = a + hex;
        }
        return a;
    }

    /**
     *  String 转 byte[]
     * @param hexString
     * @return
     */
    static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }
        return d;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }



}
