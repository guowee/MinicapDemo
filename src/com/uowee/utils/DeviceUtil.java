package com.uowee.utils;

import com.uowee.constansts.ConnectModule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;


public class DeviceUtil {
    private static Command command = new Command();
    private final static String USB_MODULE = "adb usb";
    private final static String WIFI_MODULE = "adb tcpip 5555";
    private final static String WIFI_ADDRESS = "adb shell netcfg";
    private final static String WIFI_CONNECT = "adb connect %s";
    private final static String KILL_ADB = "adb kill-server";
    private final static String START_ADB = "adb start-server";


    public void startDeviceModule(String module) {
        command.executeCommand(KILL_ADB, null);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        command.executeCommand(START_ADB, null);
        if (module.equals(ConnectModule.USB_MODULE.getCode().toString())) {        //USB模式，默认为USB模式
            Command.CommandResult cr = command.executeCommand(USB_MODULE, null);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (cr.result != 0) {
                JOptionPane.showMessageDialog(null, "Usb模式切换失败,请检查连接后重试", "错误", JOptionPane.CLOSED_OPTION);
            }
        } else if (module.equals(ConnectModule.WIFI_MODULE.getCode())) {
            Command.CommandResult wifimodule = command.executeCommand(WIFI_MODULE, null);    // WIFI模式
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (wifimodule.result == 0) {
                String wifi_address = getWifiAddress();
                if ("".equals(wifi_address)) {
                    return;
                }
                String c = String.format(WIFI_CONNECT, wifi_address);
                Command.CommandResult connect = command.executeCommand(c, null);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (connect.result != 0) {
                    JOptionPane.showMessageDialog(null, "Wifi模式连接失败", "错误", JOptionPane.CLOSED_OPTION);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Wifi模式切换失败,请检查连接", "错误", JOptionPane.CLOSED_OPTION);
            }
        }
    }



    public String getWifiAddress() {
        String result = "";
        Command.CommandResult cr = command.executeCommand(WIFI_ADDRESS, "wlan0");
        if (cr.result == 0) {
            result = cr.successMsg;
        } else {
            JOptionPane.showMessageDialog(null, "Wifi地址获取失败", "��ʾ", JOptionPane.CLOSED_OPTION);
            return result;
        }
        String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(result);
        while (m.find()) {
            result = m.group(0);
        }
        Logger.debug("Get Mobile Wifi Address:" + result);
        return result;
    }


}
