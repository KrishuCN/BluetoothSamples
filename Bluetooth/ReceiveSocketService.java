package com.example.administrator.myapplication.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by Huyao on 2017/6/28.
 * 接收消息的服务
 */
class ReceiveSocketService {
    private static InputStream inputStream;
    private static OutputStream outputStream;

    static void receiveMessage(final Handler handler, final BluetoothSocket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MYTAG", "ReceiveSocketService");
                if (socket == null || handler == null) return;
                try {
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    // 从客户端获取信息
                    BufferedReader bff = new BufferedReader(new InputStreamReader(inputStream));
                    String json;
                    String replay = "Bluetooth recive message success!! \n";
                    byte[] bufferIn = new byte[1024];
                    byte[] bufferOut = replay.getBytes();
                    while (true) {
                        int count = inputStream.read(bufferIn);
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = new String(bufferIn, 0, count, "utf-8");
                        handler.sendMessage(msg);

                        outputStream.write(bufferOut);

//                        while ((json = bff.readLine()) != null)
//                        {
//                            Message message = new Message();
//                            message.obj = json;
//                            message.what = 1;
//                            handler.sendMessage(message);
                        //说明接下来会接收到一个文件流
/*                            if ("file".equals(json))
                            {
                                FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/test.jpg");
                                int length;
                                int fileSzie = 0;
                                byte[] b = new byte[1024];
                                // 2、把socket输入流写到文件输出流中去
                                while ((length = inputStream.read(b)) != -1)
                                {
                                    fos.write(b, 0, length);
                                    fileSzie += length;
                                    System.out.println("当前大小：" + fileSzie);
                                }
                                fos.close();
                                message.obj = "文件:保存成功";
                                message.what = 2;
                                handler.sendMessage(message);
                            }*/
//                        }
                    }
                } catch (IOException e) {
                    Log.d("MYTAG", "ReceiveSocketService : bluetooth incorrect disconnected !!");

                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        }).start();
    }
}
