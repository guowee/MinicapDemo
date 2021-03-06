package com.uowee.bean;

public class ProfermanceInfo {
    private int MemUsed;
    private int MemFree;
    private int NetUsed;
    private int CpuUsed;

    public int getMemUsed() {
        return MemUsed;
    }

    public void setMemUsed(int memUsed) {
        MemUsed = memUsed;
    }

    public int getMemFree() {
        return MemFree;
    }

    public void setMemFree(int memFree) {
        MemFree = memFree;
    }

    public int getNetUsed() {
        return NetUsed;
    }

    public void setNetUsed(int netUsed) {
        NetUsed = netUsed;
    }

    public int getCpuUsed() {
        return CpuUsed;
    }

    public void setCpuUsed(int cpuUsed) {
        CpuUsed = cpuUsed;
    }

    @Override
    public String toString() {
        return "ProfermanceInfo [MemUsed=" + MemUsed + ", MemFree=" + MemFree
                + ", NetUsed=" + NetUsed + ", CpuUsed=" + CpuUsed + "]";
    }

}
