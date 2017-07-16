package com.example.administrator.myapplication.Bluetooth;

import java.util.UUID;

/**
 * Created by Administrator on 2017/6/28 0028.
 */

class BLTContant {
    static final long SCAN_PERIOD = 60000; //蓝牙搜索时间60s
    static final int SCAN_TYPE_SCANER_SCAN = 3120; //5.0以下但是支持BLE类型
    static final int  SCAN_TYPE_LESCAN = 3130; //5.0以上
    static final int SCAN_TYPE_NORMAL = 3140; // 普通蓝牙
    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //一般设备通用UUID
}
