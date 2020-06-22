package uk.co.darkerwaters.scorepal.ui.matchinit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.points.Sport;

public abstract class FragmentMatchInit<TSetup extends MatchSetup> extends Fragment {
    protected final int fragmentId;

    protected TSetup matchSetup = null;
    protected final Sport sport;

    protected FragmentMatchInit(Sport sport, int fragmentId) {
        this.fragmentId = fragmentId;
        this.sport = sport;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(this.fragmentId, container, false);
        // setup the controls on this new fragment
        setupControls(root);
        // and return the view
        return root;
    }

    protected abstract void setupControls(View root);

    public void setMatchSetup(TSetup newSetup) {
        this.matchSetup = newSetup;
    }
}
