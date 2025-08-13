package com.github.nekit508.mappainter.ui.dialogs;

import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import com.github.nekit508.mappainter.control.keys.Adjustable;
import com.github.nekit508.mappainter.control.keys.Category;
import com.github.nekit508.mappainter.ui.scene.OverlayCollapser;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class KeybindsDialog extends BaseDialog {
    protected final Seq<Adjustable> bindings = new Seq<>();
    protected final ObjectMap<Category, Seq<Adjustable>> bindingsSorted = new ObjectMap<>();

    protected boolean isAdjustingBinding = false;
    protected Table adjustingBindingTable;
    protected Table paneTable;

    protected TextField searchField;
    protected String searchedKey, searchedCategory, searchedBind;
    protected String searchText;
    protected boolean searchTextValid = true;

    protected Runnable onAdjustingDestroyed = null;
    protected Runnable onInfoDestroyed = null;

    public KeybindsDialog() {
        super("@map-painter-keybindings-dialog");

        shown(this::shown);
        hidden(this::hidden);

        addCloseButton();
        makeButtonOverlay();

        resized(this::resized);

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

        var bindInd = searchText.indexOf("name[");
        if (bindInd != -1) {
            var bind = searchText.substring(bindInd + "name[".length());
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

    public void resized() {
        destroy();
        rebuild();
    }

    public void rebuild() {
        isAdjustingBinding = false;

        cont.defaults();

        cont.table(searchTable -> {
            var searchHelperCollapser = new OverlayCollapser((table, collapser) -> {
                table.background(Styles.black6);

                table.image().color(Pal.power).fillX().row();

                table.defaults().expand().width(150).height(30).pad(3);

                table.button("key", Styles.cleart, () -> {
                    searchField.setText(searchField.getText() + " key[]");
                    collapser.toggle();
                }).row();
                table.button("name", Styles.cleart, () -> {
                    searchField.setText(searchField.getText() + " name[]");
                    collapser.toggle();
                }).row();
                table.button("category", Styles.cleart, () -> {
                    searchField.setText(searchField.getText() + " category[]");
                    collapser.toggle();
                }).row();
            }, true);
            searchHelperCollapser.yOffset = 10;
            KeybindsDialog.this.hidden(searchHelperCollapser::clear);

            var searchHelper = new ImageButton(Icon.zoom, Styles.emptyi);
            searchHelper.resizeImage(Vars.iconMed);
            searchHelper.clicked(searchHelperCollapser::toggle);

            searchTable.stack(searchHelperCollapser, searchHelper).padRight(5);
            searchTable.field("", str -> {}).with(field -> field.addListener(new InputListener() {
                @Override
                public boolean keyUp(InputEvent event, KeyCode keycode) {
                    searchText = field.getText();
                    parseSearchText();
                    rebuildPaneTable();
                    return false;
                }
            })).valid(str -> searchTextValid).with(f -> searchField = f).growX();
        }).growX().row();

        paneTable = new Table();
        paneTable.defaults().growX().pad(5);
        paneTable.center().top();
        rebuildPaneTable();

        var pane = new ScrollPane(paneTable);
        pane.visible(() -> !isAdjustingBinding);

        adjustingBindingTable = new Table();
        adjustingBindingTable.visible(() -> isAdjustingBinding);

        cont.stack(pane, adjustingBindingTable).grow();
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

                    onInfoDestroyed = binding.buildInfo(info);

                    info.defaults().size(Vars.iconMed).padRight(10);
                    info.button(Icon.settings, Styles.emptyi, Vars.iconMed, () -> showAdjustingBindingTable(binding));
                    info.button(Icon.refresh, Styles.emptyi, Vars.iconMed, () -> {
                        binding.defaults();
                        rebuildPaneTable();
                    });
                    info.button(Icon.trash, Styles.emptyi, Vars.iconMed, () -> {
                        binding.setNull();
                        rebuildPaneTable();
                    });
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

    public void destroy() {
        cont.reset();
        destroyAdjustingBindingTable();

        if (onInfoDestroyed != null) {
            onInfoDestroyed.run();
            onInfoDestroyed = null;
        }

        paneTable = null;
        searchField = null;
    }

    public void hidden() {
        destroy();
        reloadBindings();
    }

    public void showAdjustingBindingTable(Adjustable binding) {
        adjustingBindingTable.clear();
        onAdjustingDestroyed = binding.buildSettings(adjustingBindingTable);
        isAdjustingBinding = true;
    }

    public void destroyAdjustingBindingTable() {
        if (onAdjustingDestroyed != null) {
            onAdjustingDestroyed.run();
            onAdjustingDestroyed = null;
        }

        isAdjustingBinding = false;
        adjustingBindingTable.clear();

        reloadBindings();
        rebuildPaneTable();
    }

    public void hideAdjustingBindingTable() {
        destroyAdjustingBindingTable();

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
