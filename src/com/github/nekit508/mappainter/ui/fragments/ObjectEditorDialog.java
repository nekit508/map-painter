package com.github.nekit508.mappainter.ui.fragments;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.input.InputProcessor;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.style.Drawable;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextArea;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import com.github.nekit508.mappainter.ui.scene.ViewPane;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import rhino.NativeJavaObject;
import rhino.Undefined;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

public class ObjectEditorDialog extends BaseDialog {
    public KeyCode keyBind = KeyCode.f12;

    public Seq<Object> objectsHistory = new Seq<>();
    public Table objectsHistoryTable;

    public Object editingObject = null;
    public Table editingObjectTable;

    public Object lastSelectedObject = null;

    public Seq<String> runsOutputs = new Seq<>();
    public TextArea runsOutputsField;

    public TextArea objectSelector, objectEditor;
    public ViewPane classGraphView;

    public ObjectMap<Field, Prov<Object>> fieldValueProviders = new ObjectMap<>();
    public Object fieldNotChanged = new Object();

    public ObjectEditorDialog(String title) {
        super(title);
        shown(this::build);
        setModal(false);
        Core.input.addProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(KeyCode keycode) {
                if (keycode == keyBind) {
                    if (isShown()) hide();
                    else show();
                    return true;
                }
                return false;
            }
        });

        addCloseButton();
    }

    @Override
    public void updateScrollFocus() {

    }

    @Override
    public void addCloseButton(float width) {
        buttons.image(Tex.whiteui, Pal.accent).growX().height(3f).pad(4f);
        buttons.row();
        super.addCloseButton(width);
    }

    public void build() {
        fieldValueProviders.clear();
        cont.clear();

        cont.setLayoutEnabled(false);
        cont.fill(main -> {
            main.defaults().fill().pad(0);

            main.table(left -> {
                editingObjectTable = left;
                rebuildEditingObjectTable();
            }).expandY().width(Core.graphics.getWidth() * 0.4f);
            main.image(Tex.whiteui, Pal.accent).growY().width(3f);
            main.table(right -> {
                right.defaults();

                right.table(rightTop -> {
                    rightTop.defaults();

                    rightTop.table(rightTopLeft -> {
                        rightTopLeft.defaults().fill();

                        rightTopLeft.labelWrap("object editor").width(200).left().growX().tooltip("Execute script on object.\nUse local variable \"editingObject\" to access it.");
                        rightTopLeft.row();
                        rightTopLeft.pane(pane -> {
                            pane.area("", text -> {}).grow().with(a -> objectEditor = a);
                        }).expand();
                        rightTopLeft.row();
                        rightTopLeft.table(rightTopButtons -> {
                            rightTopButtons.left();
                            rightTopButtons.button("run code", Icon.settings, Styles.defaultt, () -> {
                            }).width(200);
                        }).expandX();
                    }).grow();

                    rightTop.image(Tex.whiteui, Pal.accent).growY().width(3f);

                    rightTop.table(rightTopRight -> {
                        rightTopRight.defaults().fill();

                        rightTopRight.labelWrap("object selector").width(200).left().growX().tooltip("Object returned by this script will be selected.");
                        rightTopRight.row();
                        rightTopRight.pane(pane -> {
                            pane.area("", text -> {}).grow().with(a -> objectSelector = a);
                        }).expand();
                        rightTopRight.row();
                        rightTopRight.table(rightTopButtons -> {
                            rightTopButtons.center();
                            rightTopButtons.defaults().growX();
                            rightTopButtons.button("run code", Icon.settings, Styles.defaultt, () -> {
                                var scripts = Vars.mods.getScripts();
                                try {
                                    lastSelectedObject = scripts.context.evaluateString(scripts.scope, objectSelector.getText(), "ObjectSelector.js", 0);
                                    String output = "Returned " + getObjectInfoString(lastSelectedObject);
                                    if (lastSelectedObject instanceof Undefined) {
                                        lastSelectedObject = null;
                                    } else if (lastSelectedObject instanceof NativeJavaObject) {
                                        lastSelectedObject = ((NativeJavaObject) lastSelectedObject).unwrap();
                                    }
                                    output += "\nProceed as " + getObjectInfoString(lastSelectedObject);
                                    runsOutputs.add(output);
                                    updateTextField();
                                } catch (Throwable t) {
                                    runsOutputs.add(getError(t, false));
                                    updateTextField();
                                }
                            });
                            AtomicReference<Label> labelRef = new AtomicReference<>();
                            rightTopButtons.labelWrap(() ->
                                    lastSelectedObject == null ?
                                            "no object selected" :
                                            getObjectInfoString(lastSelectedObject)
                            ).with(l -> {
                                labelRef.set(l);
                                l.setEllipsis(true);
                            }).tooltip(t -> {
                                t.label(() -> labelRef.get().getText().toString());
                            }).center();
                            rightTopButtons.button("capture selected object", Icon.save, Styles.defaultt, () -> {
                                if (lastSelectedObject == null) return;
                                objectsHistory.add(lastSelectedObject);
                                rebuildObjectsHistoryTable();
                                lastSelectedObject = null;
                            });
                        }).expandX();
                    }).grow();
                }).grow().uniformY();
                right.row();
                right.image(Tex.whiteui, Pal.accent).growX().height(3f);
                right.row();
                right.table(rightBottom -> {
                    runsOutputsField = new TextArea(""){
                        @Override protected InputListener createInputListener() { return new TextAreaListener() { @Override public boolean keyTyped(InputEvent event, char character) { return false; } }; }
                        @Override protected void drawCursor(Drawable cursorPatch, Font font, float x, float y) {}
                    };
                    runsOutputsField.update(() -> runsOutputsField.setPrefRows(runsOutputs.size));
                    rightBottom.add(new ScrollPane(runsOutputsField)).grow();

                    rightBottom.image(Tex.whiteui, Pal.accent).growY().width(3f);

                    rightBottom.pane(pane -> {
                        objectsHistoryTable = pane;
                        rebuildObjectsHistoryTable();
                    }).growY().width(Core.graphics.getWidth() * 0.2f).with(p -> p.setForceScroll(false, true));
                }).grow().uniformY();
            }).expand();
        });
    }

    public String getError(Throwable t, boolean log){
        if(log) Log.err(t);
        return t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
    }

    public void rebuildObjectsHistoryTable() {
        objectsHistoryTable.clear();
        objectsHistoryTable.defaults().growX();

        for (Object object : objectsHistory) {
            objectsHistoryTable.table(objectTable -> {
                objectTable.defaults().height(32);
                objectTable.image(Icon.info, Color.white).width(32);
                AtomicReference<Label> labelRef = new AtomicReference<>();
                objectTable.labelWrap(getObjectInfoString(object)).with(l -> l.setEllipsis(true)).left().growX()
                        .with(labelRef::set).tooltip(t -> t.labelWrap(labelRef.get().getText().toString()));
                objectTable.button(Icon.trash, Styles.cleari, () -> {
                    objectsHistory.remove(object);
                    rebuildObjectsHistoryTable();
                }).width(32).tooltip("Remove from capture list");
                objectTable.button(Icon.pencil, Styles.cleari, () -> {
                    setEditingObject(object);
                }).width(32).tooltip("Edit captured object.");
            });
            objectsHistoryTable.row();
        }
    }

    public void rebuildEditingObjectTable() {
        fieldValueProviders.clear();
        editingObjectTable.clear();

        editingObjectTable.defaults().growX().fillY();
        editingObjectTable.add(classGraphView = new ViewPane(classGraphView -> {
            if (editingObject == null) return;
            classGraphView.defaults().size(300, 100);

            classGraphView.add(new Element(){
                @Override
                public void draw() {
                    super.draw();
                    Fonts.def.draw("TODO", x, y, Color.green, 1, false, Align.center);
                }
            }).size(1, 1);
        })).height(Core.graphics.getHeight() * 0.2f);
        editingObjectTable.row();
        editingObjectTable.image(Tex.whiteui, Pal.accent).growX().height(3f);
        editingObjectTable.row();
        editingObjectTable.pane(pane -> {
            if (editingObject == null) return;
            pane.defaults();
        }).expandY().with(pane -> pane.setForceScroll(false, true));
    }

    public void setEditingObject(Object object) {
        fieldValueProviders.clear();
        if (editingObject != null) {

        }
        editingObject = object;
        rebuildEditingObjectTable();
    }

    public String getObjectInfoString(Object object) {
        return object == null ? "null" : object.toString();
    }

    public void updateTextField() {
        runsOutputsField.clearText();
        for (String runsOutput : runsOutputs) {
            runsOutputsField.appendText(runsOutput + "\n\r" + ">>>^^^^<<<\n\r");
        }
    }

    public void getFieldAdjustmentElement(Field field, AtomicReference<Element> outElement, AtomicReference<Prov<Object>> outValueProvider) {
        outElement.set(null);
        outValueProvider.set(null);

        if (field.getType() == int.class) {
            outElement.set(new TextField());
        }

        if (outElement.get() == null) {
            outElement.set(new Label("not adjustable") {{
                setEllipsis(true);
                setWrap(true);
            }});
        }
    }
}
