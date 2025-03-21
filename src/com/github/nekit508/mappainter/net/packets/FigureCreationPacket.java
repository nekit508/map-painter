package com.github.nekit508.mappainter.net.packets;

import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.nekit508.mappainter.core.MPCore;
import com.github.nekit508.mappainter.graphics.figure.FigureType;
import mindustry.Vars;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

public class FigureCreationPacket extends Packet {
    public FigureType.Figure figure;

    @Override
    public void read(Reads reads) {
        figure = FigureType.figureTypesMap.get(reads.str()).create();
        figure.read(reads);
    }

    @Override
    public void write(Writes writes) {
        writes.str(figure.type.name);
        figure.write(writes);
    }

    @Override
    public void handleClient() {
        // делаем чо-то с принятым на клиенте пакетом
        MPCore.renderer.add(figure);
    }

    @Override
    public void handleServer(NetConnection con) {
        MPCore.renderer.add(figure);

        // распространяем между остальными клиентами принятый на сервере пакет
        Vars.net.sendExcept(con, figure, true);
    }
}
