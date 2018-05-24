package com.uowee.constansts;

public enum KeyPanelEventType {
    BACK("BACK"),
    HOME("HOME"),
    MENU("MENU");
    String code;

    KeyPanelEventType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
