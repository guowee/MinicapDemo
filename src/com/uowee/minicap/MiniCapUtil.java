package com.uowee.minicap;

import com.android.ddmlib.*;
import com.android.ddmlib.IDevice.DeviceUnixSocketNamespace;
import com.uowee.observer.AndroidConnectObserver;
import com.uowee.observer.AndroidScreenObserver;
import com.uowee.observer.ConnectSubject;
import com.uowee.observer.ScreenSubject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


public class MiniCapUtil implements ScreenSubject, ConnectSubject {

    private static final String MINICAP = "minicap";
    // CPU架构的种类
    public static final String ABIS_ARM64_V8A = "arm64-v8a";
    public static final String ABIS_ARMEABI_V7A = "armeabi-v7a";
    public static final String ABIS_X86 = "x86";
    public static final String ABIS_X86_64 = "x86_64";

    private Queue<byte[]> dataQueue = new LinkedBlockingQueue<byte[]>();
    private List<AndroidScreenObserver> Screentobservers = new ArrayList<AndroidScreenObserver>();
    private List<AndroidConnectObserver> Connectobservers = new ArrayList<AndroidConnectObserver>();
    private Banner banner = new Banner();
    private static final int PORT = 1717;
    private Socket socket;
    private IDevice device;
    private String ABI_COMMAND = "ro.product.cpu.abi";
    private String SDK_COMMAND = "ro.build.version.sdk";
    private String MINICAP_WM_SIZE_COMMAND = "wm size";
    private String MINICAP_START_COMMAND = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/0 -Q 100";
    private boolean isRunning = false;
    private String size;

    public MiniCapUtil(IDevice device) {
        this.device = device;
        try {
            init();
        } catch (SyncException e) {
            e.printStackTrace();
        }
    }


    /**
     * 将minicap的二进制和.so文件push到/data/local/tmp文件夹下，启动minicap服务
     *
     * @throws SyncException
     */
    private void init() throws SyncException {
        String abi = device.getProperty(ABI_COMMAND);
        String sdk = device.getProperty(SDK_COMMAND);
        String MINICAP_LOCAL_PATH = "minicap/bin/" + abi + "/minicap";
        String MINICAPSO_LOCAL_PATH = "minicap/shared/" + "android-" + sdk + "/" + abi + "/minicap.so";
        try {
            // 将minicap的可执行文件和.so文件一起push到设备中
            device.pushFile(MINICAP_LOCAL_PATH, "/data/local/tmp/minicap");
            device.pushFile(MINICAPSO_LOCAL_PATH, "/data/local/tmp/minicap.so");
            executeShellCommand("chmod 777 /data/local/tmp/minicap");
            executeShellCommand("chmod 777 /data/local/tmp/minicap.so");
            // 端口转发
            device.createForward(PORT, MINICAP,
                    DeviceUnixSocketNamespace.ABSTRACT);

            // 获取设备屏幕的尺寸
            String output = executeShellCommand(MINICAP_WM_SIZE_COMMAND);
            size = output.split(":")[1].trim();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
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

    public void startScreenListener() {
        isRunning = true;
        Thread frame = new Thread(new ImageBinaryFrameCollector());
        frame.start();

        Thread convert = new Thread(new ImageConverter());
        convert.start();
    }

    public void stopScreenListener() {
        isRunning = false;
    }

    private BufferedImage createImageFromByte(byte[] binaryData) {
        BufferedImage bufferedImage = null;
        InputStream in = new ByteArrayInputStream(binaryData);
        try {
            bufferedImage = ImageIO.read(in);
            if (bufferedImage == null) {
                System.out.println("BufferImage is null.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bufferedImage;
    }


    // java合并两个byte数组
    private static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    private byte[] subByteArray(byte[] byte1, int start, int end) {
        byte[] byte2 = new byte[0];
        try {
            byte2 = new byte[end - start];
        } catch (NegativeArraySizeException e) {
            e.printStackTrace();
        }
        //src:源数组； srcPos:源数组要复制的起始位置； dest:目的数组； destPos:目的数组放置的起始位置； length:复制的长度。
        System.arraycopy(byte1, start, byte2, 0, end - start);
        return byte2;
    }

    class ImageBinaryFrameCollector implements Runnable {
        private InputStream stream = null;

        public void run() {
            try {
                final String startCommand = String.format(
                        MINICAP_START_COMMAND, size, size);
                // 启动minicap服务
                new Thread(new Runnable() {
                    public void run() {
                        executeShellCommand(startCommand);
                    }
                }).start();
                Thread.sleep(1 * 1000);
                socket = new Socket("localhost", PORT);
                stream = socket.getInputStream();
                int len = 1024 * 4;
                while (isRunning) {
                    byte[] buffer;
                    buffer = new byte[len];
                    int realLen = stream.read(buffer);
                    if (buffer.length != realLen) {
                        buffer = subByteArray(buffer, 0, realLen);
                    }
                    dataQueue.add(buffer);
                }
            } catch (Exception e) {            //子线程的异常不能抛出
                notifyConnectObservers();
            } finally {
                if (socket != null && socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class ImageConverter implements Runnable {
        private int readBannerBytes = 0;
        private int bannerLength = 2;
        private int readFrameBytes = 0;
        private int frameBodyLength = 0;
        private byte[] frameBody = new byte[0];


        public void run() {
            while (isRunning) {
                if (dataQueue.isEmpty()) {
                    continue;
                }
                byte[] buffer = dataQueue.poll();
                int len = buffer.length;
                for (int cursor = 0; cursor < len; ) {
                    int byte10 = buffer[cursor] & 0xff;
                    if (readBannerBytes < bannerLength) {
                        // Banner 模块
                        cursor = parserBanner(cursor, byte10);
                    } else if (readFrameBytes < 4) {
                        // 携带图片大小信息和图片二进制信息模块,第二次的缓冲区中前4位数字和为frame的缓冲区大小
                        frameBodyLength += (byte10 << (readFrameBytes * 8)) >>> 0;
                        cursor += 1;
                        readFrameBytes += 1;
                    } else {
                        // 只携带图片二进制信息模块
                        if (len - cursor >= frameBodyLength) {
                            byte[] subByte = subByteArray(buffer, cursor,
                                    cursor + frameBodyLength);
                            frameBody = byteMerger(frameBody, subByte);
                            if ((frameBody[0] != -1) || frameBody[1] != -40) {
                                return;
                            }
                            final byte[] finalBytes = subByteArray(frameBody,
                                    0, frameBody.length);
                            // 转化成bufferImage
                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    Image image = createImageFromByte(finalBytes);
                                    notifyScreenObservers(image);
                                }
                            }).start();
                            cursor += frameBodyLength;
                            restore();
                        } else {
                            byte[] subByte = subByteArray(buffer, cursor, len);
                            frameBody = byteMerger(frameBody, subByte);
                            frameBodyLength -= (len - cursor);
                            readFrameBytes += (len - cursor);
                            cursor = len;
                        }
                    }
                }
            }

        }

        private void restore() {
            frameBodyLength = 0;
            readFrameBytes = 0;
            frameBody = new byte[0];
        }

        private int parserBanner(int cursor, int byte10) {
            switch (readBannerBytes) {
                case 0:
                    // version 版本
                    banner.setVersion(byte10);
                    break;
                case 1:
                    // length 该Banner信息的长度，方便循环使用
                    bannerLength = byte10;
                    banner.setLength(byte10);
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    // pid 相加得到进程id号
                    int pid = banner.getPid();
                    pid += (byte10 << ((readBannerBytes - 2) * 8)) >>> 0;
                    banner.setPid(pid);
                    break;
                case 6:
                case 7:
                case 8:
                case 9:
                    // real width 累加得到设备真实宽度
                    int realWidth = banner.getReadWidth();
                    realWidth += (byte10 << ((readBannerBytes - 6) * 8)) >>> 0;
                    banner.setReadWidth(realWidth);
                    break;
                case 10:
                case 11:
                case 12:
                case 13:
                    // real height 累加得到设备真实高度
                    int realHeight = banner.getReadHeight();
                    realHeight += (byte10 << ((readBannerBytes - 10) * 8)) >>> 0;
                    banner.setReadHeight(realHeight);
                    break;
                case 14:
                case 15:
                case 16:
                case 17:
                    // virtual width 累加得到设备的虚拟宽度
                    int virtualWidth = banner.getVirtualWidth();
                    virtualWidth += (byte10 << ((readBannerBytes - 14) * 8)) >>> 0;
                    banner.setVirtualWidth(virtualWidth);

                    break;
                case 18:
                case 19:
                case 20:
                case 21:
                    // virtual height 累加得到设备的虚拟高度
                    int virtualHeight = banner.getVirtualHeight();
                    virtualHeight += (byte10 << ((readBannerBytes - 18) * 8)) >>> 0;
                    banner.setVirtualHeight(virtualHeight);
                    break;
                case 22:
                    // orientation 设备的方向
                    banner.setOrientation(byte10 * 90);
                    break;
                case 23:
                    // quirks 设备信息获取策略
                    banner.setQuirks(byte10);
                    break;
            }

            cursor += 1;
            readBannerBytes += 1;

            return cursor;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.wuba.utils.screenshot.ScreenSubject#registerObserver(com.wuba.utils
     * .screenshot.AndroidScreenObserver)
     */
    public void registerScreenObserver(AndroidScreenObserver o) {
        Screentobservers.add(o);
    }


    public void removeScreenObserver(AndroidScreenObserver o) {
        // TODO Auto-generated method stub
        int index = Screentobservers.indexOf(o);
        if (index != -1) {
            Screentobservers.remove(o);
        }
    }


    @Override
    public void notifyScreenObservers(Image image) {
        for (AndroidScreenObserver observer : Screentobservers) {
            observer.frameImageChange(image);
        }
    }


    @Override
    public void registerConnectObserver(AndroidConnectObserver o) {
        Connectobservers.add(o);
    }


    @Override
    public void removeConnectObserver(AndroidConnectObserver o) {
        int index = Connectobservers.indexOf(o);
        if (index != -1) {
            Connectobservers.remove(o);
        }

    }


    @Override
    public void notifyConnectObservers() {
        for (AndroidConnectObserver observer : Connectobservers) {
            observer.onDisConnect();
        }
    }
}
