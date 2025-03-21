package com.github.nekit508.mappainter.core;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import com.github.nekit508.mappainter.graphics.figure.FigureType;

public class MPFiguresManager {
    public Seq<FigureType.Figure> figures = new Seq<>();

    public ObjectMap<String, FigureType> figureTypesMap = new ObjectMap<>();
    public Seq<FigureType> figureTypes = new Seq<>();

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
}
