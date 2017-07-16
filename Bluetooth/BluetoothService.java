package com.example.administrator.myapplication.Bluetooth;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;
import static com.example.administrator.myapplication.Bluetooth.BLTContant.SCAN_PERIOD;
import static com.example.administrator.myapplication.Bluetooth.BluetoothUtils.checkBltAvailable;
import static com.example.administrator.myapplication.Bluetooth.BluetoothUtils.isSupportBle;
import static com.example.administrator.myapplication.Bluetooth.BluetoothUtils.log;

/**
 * Created by Huyao on 2017/6/30 0030.
 */

public class BluetoothService extends Service {

    private BluetoothReceiver receiver;
    private BluetoothAdapter bleAdapter;
    private BluetoothAdapter bltAdapter;
    private ScanCallback scanCallback;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private Handler mHandler;
    private BluetoothLeScanner scanner;
    private BluetoothSocket socket;
    private Timer timer;
    private TimerTask task;

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
    }

    private void initData() {
        //创建成前台服务
        Notification notification = new Notification();
        startForeground(0x15, notification);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bleAdapter = BLEManager.getBleAdapter(bluetoothManager);
        bltAdapter = BLTManager.getDefaultAdapter();
//        BLTService.getInstance().run(mHandler); //开启接收消息的socket
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = bleAdapter.getBluetoothLeScanner();
        }
        /*注册广播*/
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.EXTRA_CONNECTION_STATE);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        receiver = new BluetoothReceiver();
        registerReceiver(receiver, filter);

        mHandler = new Handler(new Handler.Callback() {
            BluetoothSocket bluetoothSocket;

            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case 1: //消息信息
                        log("received message: " + message.obj.toString());
                        break;
                    case 2: //调试提示信息
                        log(message.obj.toString());
                        break;
                    case 3: //当作为客户端连接到蓝牙设备成功时
                        bluetoothSocket = (BluetoothSocket) message.obj;
                        socket = bluetoothSocket;
                        log("device" + socket.getRemoteDevice().getName() + " connect success");
                        break;
                    case 4: //当作为服务端有设备连接进来时
                        BluetoothDevice device1 = (BluetoothDevice) message.obj;
                        log("device" + device1.getName() + "connect success");
                        break;
                }
                return true;
            }
        });

        //定时开启蓝牙任务
        timer = new Timer();

        task = new TimerTask() {
            @Override
            public void run() {
                if (!bleAdapter.isEnabled()) {
                    BluetoothUtils.openBluetooth(bleAdapter, getApplication());
                } else {
                    timer.cancel();
                }
            }
        };

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (bleAdapter.isEnabled() && checkBltAvailable(bleAdapter)) {
            log("Bluetooth opened");
            startSearch();
        } else {
            log("Bluetooth disabled");
            timer.schedule(task, 2000, 2000);
        }
        return super.onStartCommand(intent, START_STICKY, startId);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startSearch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!bleAdapter.isDiscovering()) {
                    if (bleAdapter.isEnabled()) {
                        /* 5.0 以上的API 使用第一套连接方案 */
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                                && isSupportBle(BluetoothService.this)) {
                            log("scan for first scheme");
                            scanCallback = new ScanCallback() {
                                @Override
                                public void onScanResult(int callbackType, ScanResult result) {
                                    super.onScanResult(callbackType, result);
                                    BluetoothDevice device = result.getDevice();
                                    log("first scheme scan result：" + device.getName() + " MAC: " + device.getAddress());
                                    // TODO 按照设备名字匹配
                                    if (device.getName().equals("")) {
                                        BLEManager.getInstance().connectToGATTServer(device, BluetoothService.this, mHandler);
                                    }
                                }

                                @Override
                                public void onScanFailed(int errorCode) {
                                    super.onScanFailed(errorCode);
                                    log("first scheme scan failed，error code: " + errorCode);
                                }
                            };
                            scanLeDevice(true, BLTContant.SCAN_TYPE_SCANER_SCAN);
                        } else {

                            /* 5.0 以下但是支持BLE的设备 使用第二套连接方案 */
                            if (isSupportBle(BluetoothService.this)) {
                                log("scan for second scheme");
                                leScanCallback = new BluetoothAdapter.LeScanCallback() {
                                    @Override
                                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                                        log("second scheme scan result ： " + device.getName() + " RSSI: " + rssi + " scanRecord: " + Arrays.toString(scanRecord));
                                    }
                                };
//                                scanLeDevice(true, BLTContant.SCAN_TYPE_LESCAN);  // 5.0设备以下会出现NoClassDefFoundError错误
                                scanLeDevice(true, BLTContant.SCAN_TYPE_NORMAL);
                            } else {
                                /* 都不符合则使用基本蓝牙功能 */
                                scanLeDevice(true, BLTContant.SCAN_TYPE_NORMAL);
                                log("normal scheme start scan");
                            }
                        }
                    } else {
                        log("bluetooth is not available");
                    }
                } else {
                    log("searching ...");
                }
            }
        }).start();
    }


    /**
     * 蓝牙扫描
     *
     * @param enable   关闭/打开扫描
     * @param scanType 扫描方案
     */
    private void scanLeDevice(final boolean enable, int scanType) {

        if (bleAdapter.isEnabled()) {
            switch (scanType) {
                case BLTContant.SCAN_TYPE_SCANER_SCAN:
                    if (enable) {
                        // TODO 按照要求去扫描 startScan(List<ScanFilter> filters, ScanSettings settings,
                        // TODO final ScanCallback callback)
                        scanner.startScan(scanCallback);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scanner.stopScan(scanCallback);
                                log("first scheme scan stop");
                            }
                        }, SCAN_PERIOD);
                    } else {
                        scanner.stopScan(scanCallback);
                    }
                    break;
                case BLTContant.SCAN_TYPE_LESCAN:
                    if (enable) {
                        // TODO 按照UUID扫描 startLeScan(final UUID[] serviceUuids, final LeScanCallback callback)
                        bleAdapter.startLeScan(leScanCallback);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bleAdapter.stopLeScan(leScanCallback);
                                log("second scheme scan stop");
                            }
                        }, SCAN_PERIOD);
                    } else {
                        bleAdapter.stopLeScan(leScanCallback);
                    }
                    break;
                case BLTContant.SCAN_TYPE_NORMAL:
                    if (enable) {
                        bltAdapter.startDiscovery();
                    } else {
                        bleAdapter.cancelDiscovery();
                    }
                    break;
            }
        } else {
            timer.schedule(task, 2000, 2000);
        }
    }

    /**
     * BLT 设备广播
     * BLE 不生效
     */

    public class BluetoothReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String action = intent.getAction();

            if (ACTION_REQUEST_ENABLE.equals(action)) {
                startSearch();
            }

            if (state == STATE_DISCONNECTED) {
                startSearch();
            }

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                log("发起配对请求");
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                log("配对连接请求结束");
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                log("请求断开连接");
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                log("开始BLT扫描");
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                log("终止搜索");
            }

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        log("正在配对");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        log("配对成功");
                        BLTManager.getInstance().createBond(device, mHandler);
                        break;
                    case BluetoothDevice.BOND_NONE:
                        log("取消配对");
                    default:
                        log("未配对");
                        break;
                }
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                log("normal scheme scaned device : " + device.getName());
                //TODO 更换匹配规则
                if (device.getName().equals("vivo V3")) {
                    BLTManager.getInstance().createBond(device, mHandler);
                }
            }
        }
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        /*释放内存确保蓝牙报警服务可用
        * 待测试!
        * */

//        System.gc();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);

        scanLeDevice(false, BLTContant.SCAN_TYPE_NORMAL);
        scanLeDevice(false, BLTContant.SCAN_TYPE_LESCAN);
        scanLeDevice(false, BLTContant.SCAN_TYPE_SCANER_SCAN);

        startService(new Intent(this, BluetoothService.class));
    }
}
