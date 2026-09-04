package com.hotcmds;

class KeyCommandPair {
    public int key;
    public String command;
    public boolean inMenu;
    public boolean keyState;

    public KeyCommandPair(int k, String str) {
        key = k;
        command = str;
        inMenu = false;
        keyState = false;
    }
    public KeyCommandPair(int k, String str, boolean menu) {
        key = k;
        command = str;
        inMenu = menu;
        keyState = false;
    }

}
