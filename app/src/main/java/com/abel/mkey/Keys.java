package com.abel.mkey;

import java.io.Serializable;

public class Keys  implements Serializable {

    private String name;
    private String usr;
    private String pass;


    public Keys(String name, String usr, String pass) {
        this.name = name;
        this.usr = usr;
        this.pass = pass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsr() {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
