package com.github.nekit508.mappainter.ui.dialogs;

import arc.Core;
import arc.Events;
import arc.func.Boolp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.style.Drawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Strings;
import com.github.nekit508.mappainter.control.MPKeyBindings;
import com.github.nekit508.mappainter.control.keys.keyboard.KeyBinding;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import rhino.NativeJavaObject;
import rhino.Undefined;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ObjectEditorDialog extends BaseDialog {
    public KeyBinding bind = MPKeyBindings.openObjectEditor;

    public Seq<Object> objectsHistory = new Seq<>();
    public Table objectsHistoryTable;

    public Object editingObject = null;
    public Table editingObjectTable;

    public Object lastSelectedObject = null;

    public String runOutput = "";
    public TextArea runsOutputsField;

    public TextArea objectSelector, objectEditor;

    public ObjectEditingInfo objectEditingInfo;

    public boolean useCollapseForFields;

    public ObjectEditorDialog(String title) {
        super(title);
        shown(this::build);
        setModal(false);
        Events.run(EventType.Trigger.update, () -> {
            if (bind.active())
                toggle();
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
        var objectEditorWidth = Core.graphics.getWidth() * 0.5f;

        cont.clear();

        cont.setLayoutEnabled(false);
        cont.fill(main -> {
            main.defaults().fill().pad(0);

            main.table(left -> {
                editingObjectTable = left;
                rebuildEditingObjectTable();
            }).expandY().width(objectEditorWidth);
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
                            rightTopButtons.button("run code", Icon.settings, Styles.cleart, () -> {
                            }).height(Vars.iconMed).pad(5).width(200);
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
                            rightTopButtons.button("run code", Icon.settings, Styles.cleart, () -> {
                                var scripts = Vars.mods.getScripts();
                                try {
                                    lastSelectedObject = scripts.context.evaluateString(scripts.scope, objectSelector.getText(), "ObjectSelector.js", 0);
                                    String output = "Returned " + getObjectInfoString(lastSelectedObject).get();
                                    if (lastSelectedObject instanceof Undefined) {
                                        lastSelectedObject = null;
                                    } else if (lastSelectedObject instanceof NativeJavaObject) {
                                        lastSelectedObject = ((NativeJavaObject) lastSelectedObject).unwrap();
                                    }
                                    output += "Proceed as " + getObjectInfoString(lastSelectedObject).get();
                                    runOutput = output;
                                    updateTextField();
                                } catch (Throwable t) {
                                    runOutput = getError(t, false);
                                    updateTextField();
                                }
                            }).height(Vars.iconMed).pad(5);
                            AtomicReference<Label> labelRef = new AtomicReference<>();
                            rightTopButtons.labelWrap(() ->
                                    lastSelectedObject == null ?
                                            "no object selected" :
                                            lastSelectedObject.toString()
                            ).with(l -> {
                                labelRef.set(l);
                                l.setEllipsis(true);
                            }).tooltip(t -> t.label(() -> labelRef.get().getText().toString())).center();
                            rightTopButtons.button("capture selected object", Icon.save, Styles.cleart, () -> {
                                if (lastSelectedObject == null) return;
                                objectsHistory.add(lastSelectedObject);
                                rebuildObjectsHistoryTable();
                                lastSelectedObject = null;
                            }).height(Vars.iconMed).pad(5);
                        }).expandX();
                    }).grow();
                }).grow().uniformY();
                right.row();
                right.image(Tex.whiteui, Pal.accent).growX().height(3f);
                right.row();
                right.table(rightBottom -> {
                    runsOutputsField = new TextArea(runOutput){
                        @Override protected InputListener createInputListener() { return new TextAreaListener() { @Override public boolean keyTyped(InputEvent event, char character) { return false; } }; }
                        @Override protected void drawCursor(Drawable cursorPatch, Font font, float x, float y) {}
                    };
                    runsOutputsField.update(new Runnable() {
                        String text;
                        @Override
                        public void run() {
                            if (!Objects.equals(text, runsOutputsField.getText())) {
                                text = runsOutputsField.getText();
                                int c = 1;
                                for (int i = 0; i < text.length(); i++)
                                    if (text.charAt(i) == '\n')
                                        c += 1;
                                runsOutputsField.setPrefRows(c);
                            }
                        }
                    });
                    rightBottom.add(new ScrollPane(runsOutputsField)).grow();

                    rightBottom.image(Tex.whiteui, Pal.accent).growY().width(3f);

                    rightBottom.pane(pane -> {
                        objectsHistoryTable = pane;
                        rebuildObjectsHistoryTable();
                    }).growY().width(Core.graphics.getWidth() * 0.2f).with(p -> p.setForceScroll(true, true));
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
                objectTable.label(getObjectInfoString(object)).with(l -> l.setEllipsis(true)).left().growX()
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
        editingObjectTable.clear();

        editingObjectTable.defaults().growX().fillY();

        editingObjectTable.pane(pane -> {
            if (editingObject != null)
                objectEditingInfo = buildObjectEditorFor(pane, editingObject);
        }).expandY().with(pane -> pane.setForceScroll(true, true));
    }

    public ObjectEditingInfo buildObjectEditorFor(Table table, Object object) {
        table.top().defaults().growX();

        var editingInfo = new ObjectEditingInfo(object);

        var refTable = new AtomicReference<Table>();
        var refFieldsMap = new AtomicReference<>(findFieldsRecursively(object.getClass()));
        var refClasses = new AtomicReference<>(refFieldsMap.get().keys().toSeq());

        var collapser = new Collapser(t -> {
            t.defaults().grow();
            refTable.set(t);
        }, true);

        table.table(head -> {
            head.defaults().expandX().left();

            head.button(Icon.downOpen, Styles.emptyi, Vars.iconMed, () -> {
                collapser.toggle(false);
                if (!collapser.isCollapsed()) {
                    var classes = refClasses.get();
                    var fieldsMap = refFieldsMap.get();

                    for (Class<?> clazz : classes)
                        buildFieldsEditorFor(refTable.get(), clazz, fieldsMap, object, editingInfo);
                } else
                    refTable.get().clear();
            }).update(i -> i.getStyle().imageUp = (!collapser.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(Vars.iconMed);
            head.button(Icon.save, Styles.emptyi, Vars.iconMed, editingInfo::apply).size(Vars.iconMed);
            head.label(() -> Strings.truncate(object.toString(), 20)).left().color(Pal.techBlue).tooltip(t -> t.label(object::toString).expandX());
        });
        table.row();

        table.add(collapser).padLeft(32);
        table.row();

        return editingInfo;
    }

    public void buildFieldsEditorFor(Table table, Class<?> clazz, ObjectMap<Class<?>, Seq<Field>> fieldsMap, Object object, ObjectEditingInfo editingInfo) {
        if (useCollapseForFields) {
            var collapser = new Collapser(t -> {
                t.defaults().growX().padBottom(10).padTop(10);

                var fields = fieldsMap.get(clazz);
                for (Field field : fields) {
                    t.table(fieldTable -> {
                        fieldTable.defaults().growX();
                        fieldTable.labelWrap(field.getName()).left();

                        AtomicReference<Element> elementWrapper = new AtomicReference<>();
                        AtomicReference<Prov<Object>> valueProviderWrapper = new AtomicReference<>();
                        AtomicReference<Boolp> isReadyProviderWrapper = new AtomicReference<>();

                        getAdjustmentElement(field, null, null, elementWrapper, valueProviderWrapper, isReadyProviderWrapper, object);

                        editingInfo.field(field, valueProviderWrapper.get(), isReadyProviderWrapper.get());
                        fieldTable.add(elementWrapper.get()).right();
                    }).tooltip(field.getType().getCanonicalName());
                    t.row();
                }
            }, true);

            table.table(head -> {
                head.defaults().growX();

                head.add(clazz.getCanonicalName()).left().color(Pal.accent);
                head.button(Icon.downOpen, Styles.emptyi, () -> {
                    collapser.toggle(true);
                }).update(i -> i.getStyle().imageUp = (!collapser.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(40f).right();
            });
            table.row();

            table.add(collapser).padLeft(32);
            table.row();
        } else {
            table.table(head -> {
                head.defaults().growX();
                head.add(clazz.getCanonicalName()).left().color(Pal.accent);
            });
            table.row();

            var fields = fieldsMap.get(clazz);
            for (Field field : fields) {
                table.labelWrap(field.getName()).with(l -> l.setEllipsis(true)).left();

                AtomicReference<Element> elementWrapper = new AtomicReference<>();
                AtomicReference<Prov<Object>> valueProviderWrapper = new AtomicReference<>();
                AtomicReference<Boolp> isReadyProviderWrapper = new AtomicReference<>();

                getAdjustmentElement(field, null, null, elementWrapper, valueProviderWrapper, isReadyProviderWrapper, object);

                editingInfo.field(field, valueProviderWrapper.get(), isReadyProviderWrapper.get());
                table.add(elementWrapper.get()).right();

                table.row();
            }
        }
    }

    public void setEditingObject(Object object) {
        if (editingObject != null) {

        }
        editingObject = object;
        rebuildEditingObjectTable();
    }

    public Prov<CharSequence> getObjectInfoString(Object object) {
        return object == null ? () -> "null" : object::toString;
    }

    public void updateTextField() {
        runsOutputsField.clearText();
        runsOutputsField.setText(runOutput);
    }

    public void getAdjustmentElement(@Nullable Field field, @Nullable Class<?> type, @Nullable Object defaultObject, AtomicReference<Element> outElement, AtomicReference<Prov<Object>> outValueProvider, AtomicReference<Boolp> outIsReadyProvider, Object object) {
        try {
            outElement.set(null);
            outValueProvider.set(() -> ObjectEditingInfo.notEditableFieldMarker);
            outIsReadyProvider.set(() -> true);

            if (field != null) {
                var modifiers = field.getModifiers();
                if ((modifiers & Modifier.STATIC) != 0)
                    throw new IllegalArgumentException("Static fields not allowed " + field + ".");
                if ((modifiers & Modifier.PUBLIC) == 0) {
                    outElement.set(new Label("not public field") {{
                        setEllipsis(true);
                        setWrap(true);
                    }});
                    return;
                } else if ((modifiers & Modifier.FINAL) != 0) {
                    outElement.set(new Label("final field") {{
                        setEllipsis(true);
                        setWrap(true);
                    }});
                    return;
                }

                defaultObject = field.get(object);
                type = field.getType();
            }

            Object finalDefaultObject = defaultObject;
            outValueProvider.set(() -> finalDefaultObject);

            if (type.isPrimitive()) {
                if (type != boolean.class) {
                    var tf = new TextField();
                    outIsReadyProvider.set(tf::isValid);
                    outElement.set(tf);

                    if (type == int.class) {
                        tf.setValidator(Strings::canParseInt);
                        outValueProvider.set(() -> Strings.parseInt(tf.getText()));
                        tf.setText(Integer.toString((Integer) defaultObject));
                    } else if (type == float.class) {
                        tf.setValidator(Strings::canParseFloat);
                        outValueProvider.set(() -> Strings.parseFloat(tf.getText()));
                        tf.setText(Float.toString((Float) defaultObject));
                    } else if (type == short.class) {
                        tf.setValidator(t -> {
                            try {
                                Short.valueOf(tf.getText());
                                return true;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        });
                        outValueProvider.set(() -> Short.valueOf(tf.getText()));
                        tf.setText(Short.toString((Short) defaultObject));
                    } else if (type == double.class) {
                        tf.setValidator(t -> {
                            try {
                                Double.valueOf(tf.getText());
                                return true;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        });
                        outValueProvider.set(() -> Double.valueOf(tf.getText()));
                        tf.setText(Double.toString((Double) defaultObject));
                    } else if (type == long.class) {
                        tf.setValidator(t -> {
                            try {
                                Long.valueOf(tf.getText());
                                return true;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        });
                        outValueProvider.set(() -> Long.valueOf(tf.getText()));
                        tf.setText(Long.toString((Long) defaultObject));
                    } else if (type == byte.class) {
                        tf.setValidator(t -> {
                            try {
                                Byte.valueOf(tf.getText());
                                return true;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        });
                        outValueProvider.set(() -> Byte.valueOf(tf.getText()));
                        tf.setText(Byte.toString((Byte) defaultObject));
                    } else if (type == char.class) {
                        tf.setValidator(t -> t.length() == 1);
                        outValueProvider.set(() -> tf.getText().charAt(0));
                        tf.setText(String.valueOf((char) defaultObject));
                    }
                } else {
                    var button = Elem.newCheck("", b -> {});
                    button.setChecked((Boolean) defaultObject);
                    outIsReadyProvider.set(() -> true);
                    outValueProvider.set(button::isChecked);
                    outElement.set(button);
                }
            } else if (type.isEnum()) {
                var buttons = new ButtonGroup<CheckBox>();
                buttons.setMaxCheckCount(1);
                buttons.setMinCheckCount(1);

                var table = new Table();
                table.defaults().growX();

                var enums = type.getEnumConstants();
                for (Object anEnum : enums)
                    table.check(anEnum.toString(), b -> {}).with(buttons::add).with(Button::toggle).with(b -> b.userObject = anEnum).size(Vars.iconMed).row();

                outValueProvider.set(() -> buttons.getChecked().userObject);

                outElement.set(table);
            } else if (type.isArray()) {
                var table = new Table();
                table.defaults().growX();

                var array = (Object[]) field.get(object);
                if (array != null) {
                    Seq<ObjectEditingInfo> infos = new Seq<>();
                    for (Object obj : array)
                        if (obj != null)
                            infos.add(buildObjectEditorFor(table, obj));
                        else
                            table.label(() -> "null").row();;

                    outValueProvider.set(() -> {
                        for (int i = 0; i < infos.size; i++) array[i] = infos.get(i);
                        return array;
                    });
                } else {
                    table.label(() -> "null").row();
                    outValueProvider.set(() -> null);
                }

                outElement.set(table);
            } else {
                if (type == String.class) {
                    var tf = new TextField();
                    tf.setText((String) defaultObject);
                    outValueProvider.set(tf::getText);
                    outElement.set(tf);
                } else {
                    var table = new Table();

                    if (defaultObject != null) {
                        var editingInfo = buildObjectEditorFor(table, defaultObject);
                        outValueProvider.set(() -> {
                            editingInfo.apply();
                            return editingInfo.editingObject;
                        });
                    }

                    outElement.set(table);
                }
            }

            if (outElement.get() == null) {
                outElement.set(new Label("not adjustable") {{
                    setEllipsis(true);
                    setWrap(true);
                }});
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectMap<Class<?>, Seq<Field>> findFieldsRecursively(Class<?> clazz) {
        var out = new ObjectMap<Class<?>, Seq<Field>>();

        for (Class<?> current = clazz; current.getSuperclass() != null; current = current.getSuperclass())
            if (!current.isAnonymousClass())
                out.put(current, new Seq<>(current.getDeclaredFields()).removeAll(f -> (f.getModifiers() & Modifier.STATIC) != 0));

        return out;
    }

    public static class ObjectEditingInfo {
        public static final Object notEditableFieldMarker = new Object();

        public Object editingObject;
        public ObjectMap<Field, Boolp> isReadyProviders = new ObjectMap<>();
        public ObjectMap<Field, Prov<Object>> valueProviders = new ObjectMap<>();
        public Seq<Field> fields = new Seq<>();

        public ObjectEditingInfo(Object obj) {
            editingObject = obj;
        }

        public void apply() {
            try {
                fieldsApply: {
                    for (Field field : fields)
                        if (!isReadyProviders.get(field).get())
                            break fieldsApply;

                    for (Field field : fields) {
                        var newValue = valueProviders.get(field).get();
                        if (newValue != notEditableFieldMarker)
                            field.set(editingObject, newValue);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void field(Field field, Prov<Object> valueProvider, Boolp isReadyProvider) {
            fields.add(field);
            valueProviders.put(field, valueProvider);
            isReadyProviders.put(field, isReadyProvider);
        }
    }
}
