package com.uowee.constansts;

public enum MouseEventType {
    CLICK("click"),
    MOTION("motion");
    String code;

    MouseEventType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
