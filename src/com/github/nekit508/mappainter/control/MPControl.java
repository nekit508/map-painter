package com.github.nekit508.mappainter.control;

public class MPControl {
    public FiguresControl figuresControl;
    public TesterControl testerControl;

    public MPControl() {
        figuresControl = new FiguresControl();
        testerControl = new TesterControl();
    }

    public void init() {
        figuresControl.init();
        testerControl.init();
    }
}
