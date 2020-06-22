package uk.co.darkerwaters.scorepal.points;

import java.lang.reflect.Constructor;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.data.BadmintonMatch;
import uk.co.darkerwaters.scorepal.data.BadmintonSetup;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.PingPongMatch;
import uk.co.darkerwaters.scorepal.data.PingPongSetup;
import uk.co.darkerwaters.scorepal.data.TennisMatch;
import uk.co.darkerwaters.scorepal.data.TennisSetup;
import uk.co.darkerwaters.scorepal.ui.matchinit.FragmentInitBadminton;
import uk.co.darkerwaters.scorepal.ui.matchinit.FragmentInitPingPong;
import uk.co.darkerwaters.scorepal.ui.matchinit.FragmentInitTennis;
import uk.co.darkerwaters.scorepal.ui.matchinit.FragmentMatchInit;
import uk.co.darkerwaters.scorepal.ui.matchplay.FragmentMatchPlay;
import uk.co.darkerwaters.scorepal.ui.matchplay.FragmentPlayBadminton;
import uk.co.darkerwaters.scorepal.ui.matchplay.FragmentPlayPingPong;
import uk.co.darkerwaters.scorepal.ui.matchplay.FragmentPlayTennis;
import uk.co.darkerwaters.scorepal.ui.matchresults.FragmentMatchResults;
import uk.co.darkerwaters.scorepal.ui.matchresults.FragmentResultsBadminton;
import uk.co.darkerwaters.scorepal.ui.matchresults.FragmentResultsPingPong;
import uk.co.darkerwaters.scorepal.ui.matchresults.FragmentResultsTennis;
import uk.co.darkerwaters.scorepal.ui.matchsetup.FragmentSetupBadminton;
import uk.co.darkerwaters.scorepal.ui.matchsetup.FragmentSetupPingPong;
import uk.co.darkerwaters.scorepal.ui.matchsetup.FragmentSetupTennis;
import uk.co.darkerwaters.scorepal.ui.matchsetup.FragmentSetupMatch;

public enum Sport {
    TENNIS(1, R.string.title_tennis
            , R.drawable.ic_sports_tennisplay_black_24dp
            ,"images/tennis.jpg"
            , TennisMatch.class
            , TennisSetup.class
            , FragmentSetupTennis.class
            , FragmentInitTennis.class
            , FragmentPlayTennis.class
            , FragmentResultsTennis.class),
    BADMINTON(2, R.string.title_badminton
            , R.drawable.ic_sports_badminton_black_24dp
            , "images/badminton.jpg"
            , BadmintonMatch.class
            , BadmintonSetup.class
            , FragmentSetupBadminton.class
            , FragmentInitBadminton.class
            , FragmentPlayBadminton.class
            , FragmentResultsBadminton.class),
    PINGPONG(3, R.string.title_pingpong
            , R.drawable.ic_sports_pingpong_black_24dp
            , "images/ping_pong.jpg"
            , PingPongMatch.class
            , PingPongSetup.class
            , FragmentSetupPingPong.class
            , FragmentInitPingPong.class
            , FragmentPlayPingPong.class
            , FragmentResultsPingPong.class);

    public final int id;
    public final int strRes;
    public final int iconRes;
    public final String imageFilename;
    public final Class<? extends Match> matchClass;
    public final Class<? extends MatchSetup> setupClass;
    public final Class<? extends FragmentSetupMatch> setupFragmentClass;
    public final Class<? extends FragmentMatchInit> initFragmentClass;
    public final Class<? extends FragmentMatchPlay> playFragmentClass;
    public final Class<? extends FragmentMatchResults> resultsFragmentClass;

    Sport(int id, int resId, int iconResId, String imageFilename,
          Class<? extends Match> matchClass,
          Class<? extends MatchSetup> setupClass,
          Class<? extends FragmentSetupMatch> setupFragmentClass,
          Class<? extends FragmentMatchInit> initFragmentClass,
          Class<? extends FragmentMatchPlay> playFragmentClass,
          Class<? extends FragmentMatchResults> resultsFragmentClass) {
        this.id = id;
        this.strRes = resId;
        this.iconRes = iconResId;
        this.imageFilename = imageFilename;
        this.matchClass = matchClass;
        this.setupClass = setupClass;
        this.setupFragmentClass = setupFragmentClass;
        this.initFragmentClass = initFragmentClass;
        this.playFragmentClass = playFragmentClass;
        this.resultsFragmentClass = resultsFragmentClass;
    }

    public MatchSetup newSetup() {
        return createClass(setupClass);
    }

    public FragmentSetupMatch newSetupFragment() {
        return createClass(setupFragmentClass);
    }

    public FragmentMatchInit newInitFragment() { return createClass(initFragmentClass); }

    public FragmentMatchPlay newPlayFragment() {
        return createClass(playFragmentClass);
    }

    public FragmentMatchResults newResultsFragment(boolean isAllowExpandContract) {
        FragmentMatchResults newInstance = null;
        try {
            // there has to be an empty constructor for the most derived settings classes
            Constructor<? extends FragmentMatchResults> ctor = resultsFragmentClass.getConstructor(boolean.class);
            // create the setup class from the default empty constructor for the settings
            newInstance = ctor.newInstance(isAllowExpandContract);
        } catch (Exception e) {
            Log.error("Failed to create results fragment, needs constructor with bool", e);
        }
        return newInstance;
    }

    private<T> T createClass(Class<? extends T> classToCreate) {
        T newInstance = null;
        try {
            // there has to be an empty constructor for the most derived settings classes
            Constructor<? extends T> ctor = classToCreate.getConstructor();
            // create the setup class from the default empty constructor for the settings
            newInstance = ctor.newInstance();
        } catch (Exception e) {
            Log.error("Failed to create fragment, needs empty constructor", e);
        }
        return newInstance;
    }

    public static Sport valueOf(int id) {
        for (Sport sport: Sport.values()) {
            if (sport.id == id) {
                return sport;
            }
        }
        return null;
    }
}
