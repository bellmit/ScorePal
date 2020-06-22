package uk.co.darkerwaters.scorepal.ui.apphome;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ui.matchlists.MatchDateViewAdapter;

public class FragmentMainHistory extends Fragment {

    private RecyclerView recyclerView;
    private MatchDateViewAdapter dateViewAdapter;
    private RecyclerView.LayoutManager layoutManager;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_history, container, false);

        recyclerView = root.findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter with the data - arranged by dates
        final Context context = root.getContext();
        dateViewAdapter = new MatchDateViewAdapter(context);
        recyclerView.setAdapter(dateViewAdapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // we need to check that the list of matches is still valid, might have deleted some
        dateViewAdapter.createDataToShow(getContext());
        dateViewAdapter.notifyDataSetChanged();
    }
}
