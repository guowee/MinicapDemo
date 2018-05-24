package com.uowee.constansts;

public enum ImageIconType {
    CONNECTFAILED("CONNECTFAILED"),
    CONNECTING("CONNECTING"),
    WAITFORCONNECT("WAITFORCONNECT");
    
    String code;

    ImageIconType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
