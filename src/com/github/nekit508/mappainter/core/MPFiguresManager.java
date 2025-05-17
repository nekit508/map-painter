package com.github.nekit508.mappainter.core;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.nekit508.mappainter.graphics.figure.FigureType;
import mindustry.game.EventType;
import mindustry.io.SaveFileReader;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MPFiguresManager implements SaveFileReader.CustomChunk {
    public Seq<FigureType.Figure> figures = new Seq<>();

    public ObjectMap<String, FigureType> figureTypesMap = new ObjectMap<>();
    public Seq<FigureType> figureTypes = new Seq<>();

    public MPFiguresManager() {
        Events.on(EventType.WorldLoadEvent.class, event -> {
            figures.each(FigureType.Figure::dispose);
            figures.clear();
        });
    }

    public int addType(FigureType type) {
        figureTypesMap.put(type.name, type);
        figureTypes.add(type);

        return figureTypes.size;
    }

    public FigureType getFigureType(String name) {
        return figureTypesMap.get(name);
    }

    public FigureType getFigureType(int id) {
        return figureTypes.get(id);
    }

    public FigureType.Figure addFigure(FigureType.Figure figure) {
        figures.add(figure);
        return figure;
    }

    public void removeFigure(FigureType.Figure figure) {
        figures.remove(figure);
    }

    @Override
    public void write(DataOutput stream) throws IOException {
        Writes writes = new Writes(stream);

        writes.i(figures.size);
        figures.each(figure -> {
            writes.str(figure.type.name);

            figure.write(writes);
        });
    }

    @Override
    public void read(DataInput stream) throws IOException {
        Reads reads = new Reads(stream);

        int size = reads.i();
        for (int i = 0; i < size; i++) {
            FigureType type = getFigureType(reads.str());
            FigureType.Figure figure = type.create();

            figure.read(reads);

            addFigure(figure);
        }
    }
}
