package com.github.nekit508.mappainter.control;

import arc.Core;
import arc.Events;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import com.github.nekit508.mappainter.control.keys.keyboard.KeyBinding;
import mindustry.Vars;
import mindustry.game.EventType;

public abstract class ControlReceiver {
    public static final Seq<ControlReceiver> activeReceiversStack = new Seq<>();

    protected boolean enabled = false;

    public WidgetGroup group;

    public ControlReceiver() {
        Events.run(EventType.Trigger.update, () -> {
            if (Vars.state.isGame())
                update();
            else if (enabled)
                setEnabled(false);
        });
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected float defaultMinZoom = Vars.renderer.minZoom;
    public void setEnabled(boolean newState) {
        if (enabled != newState)
            enabled = newState;
        else return;

        Vars.ui.hudfrag.shown = !enabled;
        if (!enabled) {
            Vars.renderer.minZoom = defaultMinZoom;

            if (activeReceiversStack.peek() != this)
                Log.warn("Disabling not enabled control receiver @.", this);
            else {
                activeReceiversStack.pop();
                if (!activeReceiversStack.isEmpty())
                    activeReceiversStack.peek().setEnabled(true);
            }
        } else {
            if (!activeReceiversStack.isEmpty())
                activeReceiversStack.peek().setEnabled(false);

            defaultMinZoom = Vars.renderer.minZoom;
            Vars.renderer.minZoom = 0.5f;

            activeReceiversStack.add(this);
        }
    }

    public void update() {
        if (switchButton().active())
            setEnabled(!enabled);

        if (!enabled) return;

        Core.camera.position.add(Tmp.v1.setZero().add(MPAxisBindings.moveX.axis(), MPAxisBindings.moveY.axis()).nor().scl((MPKeyBindings.moveBoost.active() ? 45f : 15f) * Time.delta));
    }

    public void init() {
        Vars.control.input.inputLocks.add(() -> enabled);

        group = new WidgetGroup();
        group.setFillParent(true);
        group.touchable = Touchable.enabled;
        group.visible(() -> enabled);
        Core.scene.add(group);
    }

    public abstract KeyBinding switchButton();
}
