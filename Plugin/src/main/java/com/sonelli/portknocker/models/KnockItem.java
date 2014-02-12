package com.sonelli.portknocker.models;

public class KnockItem {

    public static final int TYPE_TCP_PACKET = 0;
    public static final int TYPE_UDP_PACKET = 1;
    public static final int TYPE_PAUSE = 2;


    private int type;
    private int value;

    /**
     * Creates a new knock sequence item that pauses the sequence for a number of milliseconds
     * @param milliseconds Length of time to pause in milliseconds
     * @return A pause KnockItem
     */
    public static KnockItem pause(int milliseconds){
        KnockItem item = new KnockItem();
        item.setType(TYPE_PAUSE);
        item.setValue(milliseconds);
        return item;
    }

    /**
     * Creates a new knock sequence item that sends a TCP packet to a specified port
     * @param port the port to send the packet to
     * @return a TCP KnockItem
     */
    public static KnockItem tcp(int port){
        KnockItem item = new KnockItem();
        item.setType(TYPE_TCP_PACKET);
        item.setValue(port);
        return item;
    }

    /**
     * Creates a new knock sequence item that sends a UDP packet to a specified port
     * @param port the port to send the packet to
     * @return a UDP KnockItem
     */
    public static KnockItem udp(int port){
        KnockItem item = new KnockItem();
        item.setType(TYPE_UDP_PACKET);
        item.setValue(port);
        return item;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
