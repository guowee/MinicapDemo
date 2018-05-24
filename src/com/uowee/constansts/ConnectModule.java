package com.uowee.constansts;

public enum ConnectModule {
    USB_MODULE("USB"),
    WIFI_MODULE("WIFI");
    private String code;

    ConnectModule(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
