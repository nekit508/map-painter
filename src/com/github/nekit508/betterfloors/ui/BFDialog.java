package com.github.nekit508.betterfloors.ui;

import arc.util.Log;
import com.github.nekit508.betterfloors.tools.BFPreset;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.FileChooser;

public class BFDialog extends BaseDialog {
    public BFDialog(String title, DialogStyle style) {
        super(title, style);
    }

    public BFDialog(String title) {
        super(title);
    }

    public void build() {
        cont.clear();

        cont.top().left();
        cont.table(tools -> {
            tools.top();
            tools.labelWrap("@bfdialog-tools").growX().center();

            tools.button("@bfdialog-generate-preset", Icon.file, Styles.flatt, () -> {
                FileChooser.setLastDirectory(Vars.dataDirectory.child("presets"));
                Vars.platform.showFileChooser(false, BFPreset.readableExtension, fi -> {
                    Log.info(fi);
                    BFPreset preset = new BFPreset();
                    preset.fillWithCurrentContent();
                    preset.writeReadable(fi);
                });
            });
        }).expand();

        addCloseButton();
    }
}
