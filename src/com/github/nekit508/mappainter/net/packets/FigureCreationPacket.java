package com.github.nekit508.mappainter.net.packets;

import arc.util.Log;
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
        figure = MPCore.figuresManager.getFigureType(reads.str()).create();
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
        Log.debug("Received @ from server", this);

        MPCore.figuresManager.addFigure(figure);
    }

    @Override
    public void handleServer(NetConnection con) {
        Log.debug("Received @ from @", this, con);
        MPCore.figuresManager.addFigure(figure);

        // распространяем между остальными клиентами принятый на сервере пакет
        Vars.net.sendExcept(con, figure, true);
    }
}
