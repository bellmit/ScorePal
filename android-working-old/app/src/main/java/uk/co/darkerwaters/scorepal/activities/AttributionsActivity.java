package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.AttributionRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.Application;

public class AttributionsActivity extends BaseActivity {

    private RecyclerView attributionsView;
    private AttributionRecyclerAdapter adapter;
    private Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attributions);

        // initialise the activity
        setupActivity(R.string.menu_attributions);

        this.application = (Application) this.getApplication();

        this.attributionsView = findViewById(R.id.attributionsView);

        this.adapter = new AttributionRecyclerAdapter(this.application, this);
        // setup the list
        this.attributionsView.setLayoutManager(new GridLayoutManager(this, 1));
        this.attributionsView.setAdapter(this.adapter);
    }
}
