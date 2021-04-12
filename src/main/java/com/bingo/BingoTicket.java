package com.bingo;

public class BingoTicket {

    private int[][] strip;


    public BingoTicket(int[][] strip){
        this.strip = strip;
    }


    public int[][] getStrip() {
        return strip;
    }

    public void setStrip(int[][] strip) {
        this.strip = strip;
    }
}
