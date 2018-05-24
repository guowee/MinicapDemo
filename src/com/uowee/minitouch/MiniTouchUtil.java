package com.uowee.minitouch;

import com.android.ddmlib.*;

import javax.swing.*;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MiniTouchUtil {
    private static final String MINITOUCH = "minitouch";
    private IDevice device;
    private int width;
    private int height;
    private Socket socket;
    private Banner banner = new Banner();
    private OutputStream outputStream = null;
    private String ABI_COMMAND = "ro.product.cpu.abi";
    private static final int PORT = 1111;
    private final static String CHECK_SOCKET_SERVER = "adb -s %s shell ps";

    public MiniTouchUtil(IDevice device, int width, int height) {
        this.device = device;
        this.width = width;
        this.height = height;
        init();
    }

    private void init() {
        String abi = device.getProperty(ABI_COMMAND);
        String MINITOUCH_LOCAL_PATH = "minitouch/" + abi + "/minitouch";
        try {
            device.pushFile(MINITOUCH_LOCAL_PATH, "/data/local/tmp/minitouch");
            executeShellCommand("chmod 777 /data/local/tmp/minitouch");
            device.createForward(PORT, MINITOUCH, IDevice.DeviceUnixSocketNamespace.ABSTRACT);// 端口转发

        } catch (IOException e) {
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (SyncException e) {
            e.printStackTrace();
        }

    }

    private String executeShellCommand(String command) {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        try {
            device.executeShellCommand(command, output, 0);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            e.printStackTrace();
        } catch (ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.getOutput();
    }

    public void touchDown(Point downpoint) {
        Point realpoint = pointConvert(downpoint);
        executeTouch(String.format("d 0 %s %s 50\n", (int) realpoint.getX(), (int) realpoint.getY()));
    }

    public void touchUp() {
        executeTouch("u 0\n");
    }

    public void touchMove(Point movepoint) {
        Point realpoint = pointConvert(movepoint);
        executeTouch(String.format("m 0 %s %s 50\n", (int) realpoint.getX(), (int) realpoint.getY()));
    }


    public void executeTouch(String command) {
        if (outputStream != null) {
            try {
                outputStream.write(command.getBytes());
                outputStream.flush();
                String endCommand = "c\n";
                outputStream.write(endCommand.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Point pointConvert(Point point) {
        Point realpoint = new Point((int) (point.getX() / width * banner.getMaxx()), (int) (point.getY() / height * banner.getMaxy()));
        return realpoint;
    }


    public void startMiniTouch() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                executeShellCommand("/data/local/tmp/minitouch");
            }
        }).start();
        try {
            parseBanner();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "MiniTouch初始化失败", "错误", JOptionPane.CLOSED_OPTION);
            throw new Exception("MiniTouch启动异常");
        }
    }

    private void parseBanner() throws Exception {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        socket = new Socket("localhost", PORT);
        InputStream stream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        int len = 64;
        byte[] buffer = new byte[len];
        int realLen = stream.read(buffer);
        if (buffer.length != realLen) {
            buffer = subByteArray(buffer, 0, realLen);
        }
        String result = new String(buffer);
        String array[] = result.split(" |\n");
        banner.setVersion(Integer.valueOf(array[1]));// minitouch协议的版本
        banner.setMaxcontacts(Integer.valueOf(array[3]));//contact为触摸点索引，从0开始，可以有多个触摸点
        banner.setMaxx(Integer.valueOf(array[4]));//触摸的x坐标的最大值
        banner.setMaxy(Integer.valueOf(array[5]));//触摸的y坐标的最大值
        banner.setMaxpressure(Integer.valueOf(array[6]));//压力值
    }

    private byte[] subByteArray(byte[] byte1, int start, int end) {
        byte[] byte2 = new byte[0];
        try {
            byte2 = new byte[end - start];
        } catch (NegativeArraySizeException e) {
            e.printStackTrace();
        }
        System.arraycopy(byte1, start, byte2, 0, end - start);
        return byte2;
    }

    public void changePointConvert(int width, int height) {
        this.width = width;
        this.height = height;
    }

}
