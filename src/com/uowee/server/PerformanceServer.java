package com.uowee.server;

import com.android.ddmlib.*;
import com.uowee.bean.ProfermanceInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PerformanceServer implements Runnable {
    private IDevice device = null;
    private String packageName = "";
    private String meminfoUsedCommand = "dumpsys meminfo %s|grep TOTAL";
    private String memFreeCommand = "cat /proc/meminfo|grep MemFree";
    private String getUidCommand = "dumpsys package|grep packageSetting|grep %s/";
    private String netinfoCommand = "cat /proc/uid_stat/%s/tcp_rcv";
    private String cpuInfoCommand = "top -n 1|grep %s";
    private List<ProfermanceInfo> list = new ArrayList<ProfermanceInfo>();

    public PerformanceServer(IDevice device, String packageName) {
        this.device = device;
        this.packageName = packageName;
    }


    public List<ProfermanceInfo> getResultList() {
        return this.list;
    }

    @Override
    public void run() {
        String uid = _getUid();
        if (uid == null) {
            return;
        }
        int begin = _getNetInfoBegin(uid);
        while (true) {
            ProfermanceInfo pcinfo = new ProfermanceInfo();
            int netUsed = this.getNetUsed(uid, begin);
            int memUsed = this.getMemUsed();
            int memfree = this.getMemFree();
            int cpuUsed = this.getCpuUsed();
            pcinfo.setMemFree(memfree);
            pcinfo.setNetUsed(netUsed);
            pcinfo.setMemUsed(memUsed);
            pcinfo.setCpuUsed(cpuUsed);
            list.add(pcinfo);
            System.out.println(pcinfo.toString());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public int getCpuUsed() {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        String command = String.format(cpuInfoCommand, packageName);
        try {
            device.executeShellCommand(command, output, 0);
        } catch (TimeoutException | AdbCommandRejectedException
                | ShellCommandUnresponsiveException | IOException e) {
            e.printStackTrace();
        }
        String result = output.getOutput().trim();

        if ("".equals(result)) {
            return 0;
        }
        String[] array = result.split("\\s+");
        return Integer.parseInt(array[2].replace("%", ""));
    }


    public int getMemUsed() {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        String memUsed = String.format(meminfoUsedCommand, packageName);
        try {
            device.executeShellCommand(memUsed, output, 0);
        } catch (TimeoutException | AdbCommandRejectedException
                | ShellCommandUnresponsiveException | IOException e) {
            e.printStackTrace();
        }
        String result = output.getOutput().trim();
        if ("".equals(result)) {
            return 0;
        }
        String[] array = result.split("\\s+");
        return Integer.parseInt(array[6]) / 1024;
    }


    public int getMemFree() {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        String memFree = String.format(memFreeCommand, packageName);
        try {
            device.executeShellCommand(memFree, output, 0);
        } catch (TimeoutException | AdbCommandRejectedException
                | ShellCommandUnresponsiveException | IOException e) {
            e.printStackTrace();
        }
        String result = output.getOutput().trim();
        String[] array = result.split("\\s+");
        return Integer.parseInt(array[1]) / 1024;
    }


    public int getNetUsed(String uid, int begin) {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        String command = String.format(netinfoCommand, uid);
        try {
            device.executeShellCommand(command, output, 0);
        } catch (TimeoutException | AdbCommandRejectedException
                | ShellCommandUnresponsiveException | IOException e) {
            e.printStackTrace();
        }
        String result = output.getOutput().trim();
        return Integer.parseInt(result) / 1024 - begin;
    }


    private String _getUid() {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        String command = String.format(getUidCommand, packageName);
        try {
            device.executeShellCommand(command, output, 0);
        } catch (TimeoutException | AdbCommandRejectedException
                | ShellCommandUnresponsiveException | IOException e) {
            e.printStackTrace();
        }
        String result = output.getOutput().trim();
        if (("").equals(result)) {
            return null;
        }
        String[] array = result.split("/");
        return array[1].replace("}", "").trim();
    }

    private int _getNetInfoBegin(String uid) {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        String command = String.format(netinfoCommand, uid);
        try {
            device.executeShellCommand(command, output, 0);
        } catch (TimeoutException | AdbCommandRejectedException
                | ShellCommandUnresponsiveException | IOException e) {
            e.printStackTrace();
        }
        String result = output.getOutput().trim();
        return Integer.parseInt(result) / 1024;
    }


}
