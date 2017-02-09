package com.leo.boom.modle;

import android.bluetooth.BluetoothDevice;

/**
 * Created by leo on 17/2/9.
 */

public class Device {
    private String address;
    private String name;
    private int state;

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getAddress() {

        return address;
    }

    public String getName() {
        return name;
    }

    public int getState() {
        return state;
    }
    
}
