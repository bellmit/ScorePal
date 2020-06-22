package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import uk.co.darkerwaters.scorepal.application.Application;

public abstract class BaseListedActivity extends AppCompatActivity {

    protected GridLayoutManager layoutManager;
    protected RecyclerView recyclerView;
    protected RecyclerView.Adapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupRecyclerView(int viewId, int maxColumns, RecyclerView.Adapter adapter) {
        setupRecyclerView(viewId, maxColumns, 550f, adapter);
    }

    protected void setupRecyclerView(int viewId, int maxColumns, float singleColWidth, RecyclerView.Adapter adapter) {
        this.recyclerView = findViewById(viewId);
        this.recyclerViewAdapter = adapter;
        float displaySize = Application.getDisplaySize(this).getWidth();
        int noColumns = 1 + (int)(displaySize / singleColWidth);
        layoutManager = new GridLayoutManager(this, Math.min(maxColumns, noColumns));
        if (null != this.recyclerView) {
            this.recyclerView.setLayoutManager(layoutManager);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != this.recyclerView) {
            // update the cards from the possible settings change
            this.recyclerView.setAdapter(this.recyclerViewAdapter);
        }
    }
}
