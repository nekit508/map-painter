package com.github.nekit508.mappainter.net.packets;

import mindustry.net.Net;

public class MPPackets {
    public static void init() {
        Net.registerPacket(FigureCreationPacket::new);
    }
}
