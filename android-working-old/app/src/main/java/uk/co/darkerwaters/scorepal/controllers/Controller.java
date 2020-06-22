package uk.co.darkerwaters.scorepal.controllers;

import android.content.Context;
import android.util.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uk.co.darkerwaters.scorepal.R;

public abstract class Controller {

    public enum ControllerPattern {
        SingleClick,
        DoubleClick,
        LongClick,
        TripleClick,
    }

    public enum ControllerAction {
        PointTeamOne,
        PointTeamTwo,
        PointServer,
        PointReceiver,
        UndoLastPoint,
        AnnouncePoints;

        public String toString(Context context) {
            switch (this) {
                case PointTeamOne:
                    return context.getString(R.string.action_ptTeamOne);
                case PointTeamTwo:
                    return context.getString(R.string.action_ptTeamTwo);
                case PointServer:
                    return context.getString(R.string.action_ptServer);
                case PointReceiver:
                    return context.getString(R.string.action_ptReceiver);
                case UndoLastPoint:
                    return context.getString(R.string.action_ptUndo);
                case AnnouncePoints:
                    return context.getString(R.string.action_ptAnnounce);
            }
            return this.toString();
        }

        public String toStringShort(Context context) {
            switch (this) {
                case PointTeamOne:
                    return context.getString(R.string.action_ptTeamOne_short);
                case PointTeamTwo:
                    return context.getString(R.string.action_ptTeamTwo_short);
                case PointServer:
                    return context.getString(R.string.action_ptServer_short);
                case PointReceiver:
                    return context.getString(R.string.action_ptReceiver_short);
                case UndoLastPoint:
                    return context.getString(R.string.action_ptUndo_short);
                case AnnouncePoints:
                    return context.getString(R.string.action_ptAnnounce_short);
            }
            return this.toString();
        }
    }

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

    private ControllerButton[] buttons;
    private final Object listLock;

    public enum KeyPress {
        Short,
        Long,
    }

    public interface ControllerListener {
        void onControllerInput(ControllerAction action);
        void onControllerKeyPress(KeyPress[] keyPresses);
    }

    private final Set<ControllerListener> listeners;

    public Controller(final ControllerButton[] buttons) {
        // create the objects to protect the lists
        this.listLock = new Object();
        // set the buttons
        setButtons(buttons);
        // create the list of listeners
        this.listeners = new HashSet<>();
    }

    public abstract boolean start();
    public abstract boolean close();

    public void setButtons(ControllerButton[] newButtons) {
        synchronized (this.listLock) {
            this.buttons = Arrays.copyOf(newButtons, newButtons.length);
        }
    }

    public ControllerButton[] getButtons() {
        synchronized (this.listLock) {
            return Arrays.copyOf(this.buttons, this.buttons.length);
        }
    }

    public boolean addListener(ControllerListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(ControllerListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    protected void informControllerListeners(int[] keyCodes, ControllerPattern pattern, ControllerAction action) {
        synchronized (this.listeners) {
            for (ControllerListener listener : this.listeners) {
                listener.onControllerInput(action);
            }
        }
    }

    protected void informControllerListeners(KeyPress[] keyPresses) {
        synchronized (this.listeners) {
            for (ControllerListener listener : this.listeners) {
                listener.onControllerKeyPress(keyPresses);
            }
        }
    }
}
