package uk.co.darkerwaters.scorepal.controllers;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;

public abstract class Controller<TListener extends Controller.ControllerListener> {

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

    // we need to map the pattern to the required action
    public static ControllerAction PatternToAction(ControllerPattern pattern) {
        ApplicationPreferences preferences = ApplicationState.Instance().getPreferences();
        Controller.ControllerAction selectedAction;
        switch (pattern) {
            case SingleClick:
                selectedAction = preferences.getIsControlTeams() ?
                        Controller.ControllerAction.PointTeamOne :
                        Controller.ControllerAction.PointServer;
                break;
            case DoubleClick:
                selectedAction = preferences.getIsControlTeams() ?
                        Controller.ControllerAction.PointTeamTwo :
                        Controller.ControllerAction.PointReceiver;
                break;
            case LongClick:
                selectedAction = Controller.ControllerAction.UndoLastPoint;
                break;
            case TripleClick:
            default:
                selectedAction = Controller.ControllerAction.AnnouncePoints;
                break;
        }
        // and return this
        return selectedAction;
    }

    public interface ControllerListener {
        void onControllerInteraction(Controller.ControllerAction action);
        void onControllerError(String message);
    }

    protected final Set<TListener> listeners = new HashSet<>();

    public boolean addListener(TListener listener) {
        synchronized (listeners) {
            return listeners.add(listener);
        }
    }

    public boolean removeListener(TListener listener) {
        synchronized (listeners) {
            return listeners.remove(listener);
        }
    }

    protected void informListeners(Controller.ControllerAction action) {
        synchronized (listeners) {
            for (TListener listener: listeners) {
                listener.onControllerInteraction(action);
            }
        }
    }

    protected void informListeners(Controller.ControllerPattern pattern) {
        // use the other one
        this.informListeners(PatternToAction(pattern));
    }

    protected void informListeners(String errorMessage) {
        synchronized (listeners) {
            for (TListener listener: listeners) {
                listener.onControllerError(errorMessage);
            }
        }
    }
}
