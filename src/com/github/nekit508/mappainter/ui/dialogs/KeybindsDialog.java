package com.github.nekit508.mappainter.ui.dialogs;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import com.github.nekit508.mappainter.control.keys.Adjustable;
import com.github.nekit508.mappainter.control.keys.Category;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class KeybindsDialog extends BaseDialog {
    protected final Seq<Adjustable> bindings = new Seq<>();
    protected final ObjectMap<Category, Seq<Adjustable>> bindingsSorted = new ObjectMap<>();

    protected boolean isAdjustingBinding = false;
    protected Table adjustingBindingTable;
    protected Table paneTable;

    protected String searchedKey, searchedCategory, searchedBind;
    protected String searchText;
    protected boolean searchTextValid = true;

    public KeybindsDialog() {
        super("@map-painter-keybindings-dialog");

        shown(this::shown);
        hidden(this::hidden);

        addCloseButton();
        makeButtonOverlay();

        Vars.ui.menufrag.addButton("@map-painter-keybindings-dialog", Icon.settings, this::show);
    }

    protected void parseSearchText() {
        searchTextValid = true;
        searchedBind = searchedKey = searchedCategory = null;

        if (searchText.isEmpty()) return;

        var keyInd = searchText.indexOf("key[");
        if (keyInd != -1) {
            var key = searchText.substring(keyInd + "key[".length());
            var ind = key.indexOf(']');

            if (ind == -1)
                searchTextValid = false;
            else {
                key = key.substring(0, ind);
                searchedKey = key;
            }
        }

        var categoryInd = searchText.indexOf("category[");
        if (categoryInd != -1) {
            var category = searchText.substring(categoryInd + "category[".length());
            var ind = category.indexOf(']');

            if (ind == -1)
                searchTextValid = false;
            else {
                category = category.substring(0, ind);
                searchedCategory = category;
            }
        }

        var bindInd = searchText.indexOf("bind[");
        if (bindInd != -1) {
            var bind = searchText.substring(bindInd + "bind[".length());
            var ind = bind.indexOf(']');

            if (ind == -1)
                searchTextValid = false;
            else {
                bind = bind.substring(0, ind);
                searchedBind = bind;
            }
        }

        if (searchTextValid)
            if (keyInd == -1 && bindInd == -1 && categoryInd == -1)
                searchedBind = searchText;
    }

    public void rebuild() {
        isAdjustingBinding = false;

        cont.defaults();

        cont.table(searchTable -> {
            searchTable.image(Icon.zoom).size(Vars.iconMed).padRight(5).left();
            searchTable.field("", str -> {}).with(field -> field.addListener(new InputListener() {
                @Override
                public boolean keyUp(InputEvent event, KeyCode keycode) {
                    searchText = field.getText();
                    parseSearchText();
                    rebuildPaneTable();
                    return false;
                }
            })).valid(str -> searchTextValid).growX();
        }).growX().row();

        paneTable = new Table();
        paneTable.defaults().growX().pad(5);
        paneTable.center().top();
        rebuildPaneTable();

        var pane = new ScrollPane(paneTable);
        pane.visible(() -> !isAdjustingBinding);

        adjustingBindingTable = new Table();
        adjustingBindingTable.visible(() -> isAdjustingBinding);
        adjustingBindingTable.background(Tex.buttonRed);

        cont.stack(pane, adjustingBindingTable).growY().width(Core.graphics.getWidth());
    }

    public void shown() {
        rebuild();
    }

    public void rebuildPaneTable() {
        paneTable.clear();

        var categories = bindingsSorted.keys();
        for (var category : categories) {
            if (!category.filterValid(searchedCategory)) continue;

            var categoryTable = new Table();
            categoryTable.defaults().growX();

            var hasAnyMember = false;
            var bindings = bindingsSorted.get(category);
            for (var binding : bindings) {
                if (!binding.bindFilterValid(searchedBind) || !binding.keyFilterValid(searchedKey)) continue;
                hasAnyMember = true;

                categoryTable.labelWrap(binding.id()).left();

                categoryTable.table(info -> {
                    info.right();

                    binding.buildInfo(info);

                    info.defaults().size(Vars.iconMed).padRight(10);
                    info.button(Icon.settings, Styles.clearNonei, Vars.iconMed, () -> showAdjustingBindingTable(binding));
                    info.button(Icon.refresh, Styles.clearNonei, Vars.iconMed, binding::defaults);
                    info.button(Icon.trash, Styles.clearNonei, Vars.iconMed, binding::setNull);
                }).row();
            }

            if (!hasAnyMember)
                continue;

            paneTable.image().color(Color.gray).fillX().colspan(4).row();
            paneTable.add(category.id()).color(Color.gray).padTop(5).padBottom(10).with(l -> {
                l.setAlignment(Align.center);
            }).row();

            paneTable.add(categoryTable).padBottom(20).get();
            paneTable.row();
        }
    }

    public void reloadBindings() {
        bindings.each(Adjustable::save);
        bindings.each(Adjustable::load);
    }

    public void hidden() {
        cont.reset();
        adjustingBindingTable = null;
        reloadBindings();
    }

    public void showAdjustingBindingTable(Adjustable binding) {
        adjustingBindingTable.clear();
        binding.buildSettings(adjustingBindingTable);
        isAdjustingBinding = true;
    }

    public void hideAdjustingBindingTable() {
        isAdjustingBinding = false;
        adjustingBindingTable.clear();

        reloadBindings();
        rebuildPaneTable();
    }

    public void addKeybindings(Adjustable... binds) {
        bindings.addAll(binds);

        for (var bind : binds) {
            if (!bindingsSorted.containsKey(bind.category()))
                bindingsSorted.put(bind.category(), new Seq<>());
            bindingsSorted.get(bind.category()).add(bind);
        }
    }

    @Override
    public void hide() {
        if (!isAdjustingBinding)
            super.hide();
        else
            hideAdjustingBindingTable();
    }
}
