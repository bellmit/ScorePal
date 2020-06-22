package uk.co.darkerwaters.scorepal.application;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.lib.FlicButton;
import uk.co.darkerwaters.scorepal.controllers.ButtonController;
import uk.co.darkerwaters.scorepal.controllers.Controller;
import uk.co.darkerwaters.scorepal.controllers.Flic1Controller;
import uk.co.darkerwaters.scorepal.controllers.Flic2Controller;
import uk.co.darkerwaters.scorepal.controllers.MediaController;
import uk.co.darkerwaters.scorepal.data.Match;

public class MatchServiceControllers implements
        Flic2Controller.Flic2Listener,
        Flic1Controller.Flic1Listener, ButtonController.ButtonControllerListener {

    private final MatchService service;
    private MediaController mediaController = null;

    public MatchServiceControllers(MatchService parentService) {
        // construct this
        this.service = parentService;

        // listen to any controllers that are global
        Flic2Controller.Initialise(service).addListener(this);
        // and any that are initialised elsewhere
        Flic1Controller flic1Controller = Flic1Controller.Instance();
        if (null != flic1Controller) {
            // listen to this initialised controller
            flic1Controller.addListener(this);
        }
        // and the media controller
        this.mediaController = new MediaController(service);
        this.mediaController.start();
        this.mediaController.addListener(this);
    }

    @Override
    public void onFlic1ButtonConnected(FlicButton button) {
        // whatever
    }

    @Override
    public void onFlic2ButtonConnected(Flic2Button button) {
        // whatever
    }

    @Override
    public void onControllerInteraction(Controller.ControllerAction action) {
        // this action has been received by someone, perform this action on the
        // currently active match
        Match activeMatch = service.getActiveMatch();
        if (null != activeMatch) {
            // do the action
            activeMatch.onControllerInput(action);
        }
    }

    @Override
    public void onControllerKeyPress(ButtonController.KeyPress[] keyPresses) {
        //Log.debug("received a keypress message");
    }

    @Override
    public void onControllerError(String message) {
        // whatever
        Log.error("unhandled controller error in MatchServive:" + message);
    }

    public void destroy() {
        // stop listening to any controllers that are global
        Flic2Controller flic2Controller = Flic2Controller.Instance();
        if (null != flic2Controller) {
            // stop listening to this initialised controller
            flic2Controller.removeListener(this);
        }
        // stop listening to flic1 too
        Flic1Controller flic1Controller = Flic1Controller.Instance();
        if (null != flic1Controller) {
            // stop listening to this initialised controller
            flic1Controller.removeListener(this);
        }
        // release FLIC to stop listening to FLIC things
        Flic2Controller.Release();
        // and do flic1 - which is global too, if initialised elsewhere...
        Flic1Controller.Release();
        // and the media controller
        if (null != this.mediaController) {
            this.mediaController.removeListener(this);
            this.mediaController.close();
            this.mediaController = null;
        }
    }
}
