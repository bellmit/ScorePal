package uk.co.darkerwaters.scorepal.controllers;

import android.util.Pair;

public abstract class ButtonController extends Controller<ButtonController.ButtonControllerListener> {

    public static class ControllerButton {
        public final int[] codes;
        public final Pair<ControllerPattern, ControllerAction>[] actions;

        ControllerButton(int code, Pair<ControllerPattern, ControllerAction>[] actions) {
            this(new int[] {code}, actions);
        }
        ControllerButton(int[] codes, Pair<ControllerPattern, ControllerAction>[] actions) {
            this.codes = codes;
            this.actions = actions;
        }

        public boolean containsCode(int keyCode) {
            for (int code : this.codes) {
                if (code == keyCode) {
                    return true;
                }
            }
            return false;
        }
    }

    protected final ControllerButton[] buttons;

    public enum KeyPress {
        Short,
        Long,
    }

    public interface ButtonControllerListener extends Controller.ControllerListener {
        void onControllerKeyPress(KeyPress[] keyPresses);
    }

    public ButtonController(final ControllerButton[] buttons) {
        // create the objects to protect the lists
        this.buttons = buttons;
    }

    public abstract boolean start();
    public abstract boolean close();

    protected void informListeners(KeyPress[] keyPresses) {
        synchronized (this.listeners) {
            for (ButtonControllerListener listener : this.listeners) {
                listener.onControllerKeyPress(keyPresses);
            }
        }
    }
}
