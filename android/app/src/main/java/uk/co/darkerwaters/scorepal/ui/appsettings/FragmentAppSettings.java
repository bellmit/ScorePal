package uk.co.darkerwaters.scorepal.ui.appsettings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.ActivityMain;
import uk.co.darkerwaters.scorepal.ui.matchplay.ActivityPlayMatch;

public abstract class FragmentAppSettings extends Fragment {

    protected final int fragmentId;
    protected final int settingsNavId;
    private BottomNavigationView settingsSelector;

    protected FragmentAppSettings(int fragmentId, int settingsNavId) {
        this.fragmentId = fragmentId;
        this.settingsNavId = settingsNavId;
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

        settingsSelector = root.findViewById(R.id.app_settings_nav_view);
        settingsSelector.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // show this page on the main activity now
                if (menuItem.getItemId() != settingsNavId) {
                    // this is not ourselves
                    FragmentActivity activity = getActivity();
                    if (activity instanceof ActivityMain) {
                        // this can jump to the correct setting then
                        ((ActivityMain) activity).changeSettings(menuItem.getItemId());
                    } else if (activity instanceof ActivityPlayMatch) {
                        // do it on this too
                        ((ActivityPlayMatch) activity).changeSettings(menuItem.getItemId());
                    }
                }
                return true;
            }
        });
        // setup the navigation controls right away
        setupNavControls();
        // setup the controls on this new fragment
        setupControls(root);
        // and return the view
        return root;
    }

    protected void setupNavControls() {
        // set the nav icon to show what this sport is
        settingsSelector.setSelectedItemId(settingsNavId);
    }

    protected abstract void setupControls(View root);

    protected abstract void setDataToControls();

    @Override
    public void onResume() {
        super.onResume();
        // and be sure the nav controls are initialised
        setupNavControls();

        // set all the data to be up-to-date now
        setDataToControls();
    }

    protected void setSwitchChecked(Switch control, boolean isChecked) {
        control.setChecked(isChecked);
        updateSwitchCheck(control);
    }

    protected void updateSwitchCheck(Switch control) {
        if (control.isChecked()) {
            // when we are checked, it is a yellow that you can't really see - change this to dark
            if (control.isEnabled()) {
                control.setThumbTintList(ContextCompat.getColorStateList(getContext(), R.color.primaryLightColor));
            }
            else {
                // show disbled a little lighter
                control.setThumbTintList(ContextCompat.getColorStateList(getContext(), R.color.primaryLightColorFaded));
            }
        }
        else {
            // put it back
            control.setThumbTintList(null);
        }
    }
}
