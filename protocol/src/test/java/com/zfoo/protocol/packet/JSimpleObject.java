package com.zfoo.protocol.packet;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * @author linda
 * @create 2023-03-18-15:24
 **/
public class JSimpleObject {

    @Protobuf
    private int c;

    @Protobuf
    private boolean g;

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public boolean isG() {
        return g;
    }

    public void setG(boolean g) {
        this.g = g;
    }
}