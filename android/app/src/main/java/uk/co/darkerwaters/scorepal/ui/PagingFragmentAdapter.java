package uk.co.darkerwaters.scorepal.ui;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.ui.login.FragmentLoginName;

public class PagingFragmentAdapter<T extends Fragment> extends FragmentStateAdapter {

    public interface BackPressedHandler<T extends Fragment> {
        boolean handleBackPressed();
    }

    public static <T extends Fragment> T  FindFragment(AppCompatActivity activity, Class<T> type) {
        return FindFragment(activity.getSupportFragmentManager(), type);
    }

    public static <T extends Fragment> List<T> FindFragments(AppCompatActivity activity, Class<T> type) {
        return FindFragments(activity.getSupportFragmentManager(), type);
    }

    public static <T extends Fragment> T  FindFragment(FragmentManager fragmentManager, Class<T> type) {
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (type.isAssignableFrom(fragment.getClass())) {
                // this is one of ours, return it
                return (T) fragment;
            }
        }
        return null;
    }

    public static <T extends Fragment> List<T> FindFragments(FragmentManager fragmentManager, Class<T> type) {
        List<T> foundList = new ArrayList<>();
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (type.isAssignableFrom(fragment.getClass())) {
                // this is one of ours, add to the list
                foundList.add((T)fragment);
            }
        }
        return foundList;
    }

    private final Class<T>[] fragmentClasses;
    private int currentNavSelection = -1;
    private final Stack<Integer> pageHistory;
    private final List<BackPressedHandler> listeners;
    private Integer lastPage = null;
    private boolean saveToHistory;

    public PagingFragmentAdapter(FragmentManager fragmentManager, Lifecycle lifecycle, ViewPager2 container, Class<T>[] fragments) {
        super(fragmentManager, lifecycle);
        this.fragmentClasses = fragments;
        this.listeners = new ArrayList<>();
        this.pageHistory = new Stack<Integer>();
        initialise(container);
    }

    private void initialise(ViewPager2 fragmentContainer) {
        // listen to this container
        fragmentContainer.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int currentPage) {
                if(saveToHistory && lastPage != null && lastPage != currentPage) {
                    // save the last page to our local history
                    pageHistory.push(lastPage);
                }
                lastPage = Integer.valueOf(currentPage);
            }
        });
        saveToHistory = true;
    }

    public void addListener(BackPressedHandler listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
    }

    public void removeListener(BackPressedHandler listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }
    }

    public void setIsSaveToHistory(boolean value) {
        saveToHistory = value;
    }

    public boolean getIsSaveToHistory() {
        return saveToHistory;
    }

    public T getFragment(AppCompatActivity activity, int position) {
        // using the expected type, return the fragment expected at this location
        return FindFragment(activity, this.fragmentClasses[position]);
    }

    @Override
    public Fragment createFragment(int position) {
        // create the new fragment here
        try {
            // just create new (default constructor) of the passed type they want at this position
            return this.fragmentClasses[position].newInstance();
        } catch (IllegalAccessException e) {
            Log.error("Failed to create a fragement specified by class", e);
        } catch (InstantiationException e) {
            Log.error("Failed to create a fragement specified by class", e);
        }
        // oops
        return null;
    }

    @Override
    public int getItemCount() {
        return fragmentClasses.length;
    }

    public boolean emptyHistory() {
        return this.pageHistory.empty();
    }

    public int popHistory() {
        return pageHistory.pop().intValue();
    }

    public void setupNavBarToViewPagerMapping(final BottomNavigationView navBar, final ViewPager2 container, final int[] navBarIds) {
        navBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // find the index of this selection
                int newSelection = -1;
                int newNavSelection = menuItem.getItemId();
                if (newNavSelection != currentNavSelection) {
                    // this is a change, update to show the first relevant page for this selection
                    currentNavSelection = newNavSelection;
                    for (int i = 0; i < navBarIds.length; ++i) {
                        if (navBarIds[i] == currentNavSelection) {
                            // this is the ID of the page to select
                            newSelection = i;
                            break;
                        }
                    }
                    if (newSelection >= 0 && newSelection != container.getCurrentItem()) {
                        // this is a new selection, set it here
                        container.setCurrentItem(newSelection, true);
                    }
                }
                return true;
            }
        });
        // as the page changes, update the nav bar selection
        container.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (currentNavSelection != navBarIds[position]) {
                    // update the nav bar accordingly
                    currentNavSelection = navBarIds[position];
                    navBar.setSelectedItemId(currentNavSelection);
                }
            }
        });
    }
}
