package com.lenovo.settings.db;

public class AppInfoBean {
    public int ID;
    public String password;
    public int lockState;
    public String packageName;

    @Override
    public String toString() {
        return "ID = " + ID + " password = " + password + " packageName = " + packageName
                + " lockState = " + lockState;
    }
}
