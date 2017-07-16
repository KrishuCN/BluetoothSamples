package com.example.administrator.myapplication.Bluetooth.GrabOrder;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by Huyao on 2017/7/3 0003.
 * <p>
 * 一键抢单
 */



public class GrabOrder {


    /*使用input方法模拟*/

    public void inputSwipe(String startX,String startY,String endX,String endY){
        exec("su",new String[]{"input touchscreen swipe "+startX+" "+startY+" "+endX+" "+endY});
    }

    public void inputClick(String rawx,String rawy){
        exec("su",new String[]{"input tap "+rawx+" "+rawy});
    }

    public void inputHome(){
        exec("su",new String[]{"input keyevent 3"});
    }
    public void inputBack(){
        exec("su",new String[]{"input keyevent 4"});
    }
    public void inputMenu(){
        exec("su",new String[]{"input keyevent 82"});
        Log.d("MYTAG","inputMenu");
    }
    public void inputText(String text){
        exec("su",new String[]{"input text "+text});
    }

    /*使用endEvent方法模拟操作  由于厂商ROM自己定制  此方法不稳定*/

    public void sendEventHome()
    {
        String[] orders = {
                "sendevent /dev/input/event1 0 0 0",
                "sendevent /dev/input/event1 1 102 1",
                "sendevent /dev/input/event1 0 0 0",
                "sendevent /dev/input/event1 1 102 0",
                "sendevent /dev/input/event1 0 0 0",
                "sendevent /dev/input/event1 0 0 0"};
        exec("su", orders);
    }

    public void sendEventClick()
    {
        String[] orders = {
                "sendevent /dev/input/event0 3 0 300",
                "sendevent /dev/input/event0 3 1 200", //点击位置
                "sendevent /dev/input/event0 1 330 1",
                "sendevent /dev/input/event0 0 0 0", //点击down
                "sendevent /dev/input/event0 1 330 0",
                "sendevent /dev/input/event0 0 0 0", //点击up
        };
        exec("su", orders);
    }

    /**
     * 提交cmd命令
     *
     * @param cmd
     * @param orders
     */
    private void exec(String cmd, String[] orders)
    {
        try
        {
            Process process = Runtime.getRuntime().exec(cmd);
            DataOutputStream dataOut = new DataOutputStream(
                    process.getOutputStream());
            if (orders != null)
            {
                for (String order : orders)
                    dataOut.writeBytes(order + ";");
            }
            dataOut.flush();
            dataOut.close();
            process.waitFor();
            InputStream in = process.getInputStream();
            BufferedReader bufferReader = new BufferedReader(
                    new InputStreamReader(in));
            BufferedReader err = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            String line = null;
            while ((line = err.readLine()) != null)
                Log.d("MYTAG", "1.>>>" + line);
            while ((line = bufferReader.readLine()) != null)
                Log.d("MYTAG", "2.>>>" + line);
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {

        }
    }

    public void getRootAh(String cmd){
        new GetRootAhTheard(cmd).start();
    }

    private class GetRootAhTheard extends Thread{

        private String cmd;
        GetRootAhTheard(String cmd){
                this.cmd = cmd;
        }

        @Override
        public void run()
        {
            super.run();
            getRootAhth(cmd);
        }
    }

    /**
     * 获取ROOT权限
     *
     * @param cmd su命令
     * @return ture 获取最高权限失败
     * 获取成功不会返回
     */
    public synchronized boolean getRootAhth(String cmd)
    {
        Process process = null;
        DataOutputStream os = null;
        try
        {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd);
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (Exception e)
        {
            Log.d("*** GrabOrder ***", "ROOT Unexpected error : "
                    + e.getMessage());
            return false;
        } finally
        {
            try
            {
                if (os != null)
                {
                    os.close();
                }
                process.destroy();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}

/*
* adb input使用方法

   1,模拟输入文本信息：input text HelloWorld


    2.模拟物理按键操作： input keyevent KEYCODE_VOLUME_DOWN


    3.模拟点击操作：input tap 500 500


    4.模拟滑动操作：input swipe 200 500  400 500


    5.模拟轨迹球操作

    adb sendevent使用方法

     sendevent [device] [type] [code] [value]

    device: 设备名字 可以通过adb shell getevent获取
    type: 操作类型 1:按键点击 3:坐标点击
    code: type为1时code就为按键值 type为3时code就为坐标值
    value: 也一样，type是1的时候value就是的值标示keydown，keyup。
*

   inputCode键值对应表

0 –> “KEYCODE_UNKNOWN”
1 –> “KEYCODE_MENU”
2 –> “KEYCODE_SOFT_RIGHT”
3 –> “KEYCODE_HOME”
4 –> “KEYCODE_BACK”
5 –> “KEYCODE_CALL”
6 –> “KEYCODE_ENDCALL”
7 –> “KEYCODE_0”
8 –> “KEYCODE_1”
9 –> “KEYCODE_2”
10 –> “KEYCODE_3”
11 –> “KEYCODE_4”
12 –> “KEYCODE_5”
13 –> “KEYCODE_6”
14 –> “KEYCODE_7”
15 –> “KEYCODE_8”
16 –> “KEYCODE_9”
17 –> “KEYCODE_STAR”
18 –> “KEYCODE_POUND”
19 –> “KEYCODE_DPAD_UP”
20 –> “KEYCODE_DPAD_DOWN”
21 –> “KEYCODE_DPAD_LEFT”
22 –> “KEYCODE_DPAD_RIGHT”
23 –> “KEYCODE_DPAD_CENTER”
24 –> “KEYCODE_VOLUME_UP”
25 –> “KEYCODE_VOLUME_DOWN”
26 –> “KEYCODE_POWER”
27 –> “KEYCODE_CAMERA”
28 –> “KEYCODE_CLEAR”
29 –> “KEYCODE_A”
30 –> “KEYCODE_B”
31 –> “KEYCODE_C”
32 –> “KEYCODE_D”
33 –> “KEYCODE_E”
34 –> “KEYCODE_F”
35 –> “KEYCODE_G”
36 –> “KEYCODE_H”
37 –> “KEYCODE_I”
38 –> “KEYCODE_J”
39 –> “KEYCODE_K”
40 –> “KEYCODE_L”
41 –> “KEYCODE_M”
42 –> “KEYCODE_N”
43 –> “KEYCODE_O”
44 –> “KEYCODE_P”
45 –> “KEYCODE_Q”
46 –> “KEYCODE_R”
47 –> “KEYCODE_S”
48 –> “KEYCODE_T”
49 –> “KEYCODE_U”
50 –> “KEYCODE_V”
51 –> “KEYCODE_W”
52 –> “KEYCODE_X”
53 –> “KEYCODE_Y”
54 –> “KEYCODE_Z”
55 –> “KEYCODE_COMMA”
56 –> “KEYCODE_PERIOD”
57 –> “KEYCODE_ALT_LEFT”
58 –> “KEYCODE_ALT_RIGHT”
59 –> “KEYCODE_SHIFT_LEFT”
60 –> “KEYCODE_SHIFT_RIGHT”
61 –> “KEYCODE_TAB”
62 –> “KEYCODE_SPACE”
63 –> “KEYCODE_SYM”
64 –> “KEYCODE_EXPLORER”
65 –> “KEYCODE_ENVELOPE”
66 –> “KEYCODE_ENTER”
67 –> “KEYCODE_DEL”
68 –> “KEYCODE_GRAVE”
69 –> “KEYCODE_MINUS”
70 –> “KEYCODE_EQUALS”
71 –> “KEYCODE_LEFT_BRACKET”
72 –> “KEYCODE_RIGHT_BRACKET”
73 –> “KEYCODE_BACKSLASH”
74 –> “KEYCODE_SEMICOLON”
75 –> “KEYCODE_APOSTROPHE”
76 –> “KEYCODE_SLASH”
77 –> “KEYCODE_AT”
78 –> “KEYCODE_NUM”
79 –> “KEYCODE_HEADSETHOOK”
80 –> “KEYCODE_FOCUS”
81 –> “KEYCODE_PLUS”
82 –> “KEYCODE_MENU”
83 –> “KEYCODE_NOTIFICATION”
84 –> “KEYCODE_SEARCH”
85 –> “TAG_LAST_KEYCODE”

*
*
* */