package layout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.ScoreActivity;

public class ScoreTypeButtonsFragment extends Fragment {
    private Context parentContext = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_score_type_buttons, container, false);

        view.findViewById(R.id.score_device_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parentContext.getApplicationContext(), ScoreActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.score_manual_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(parentContext, "Sorry not done this yet...", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        // remember the context we attach to (or owning Activity)
        parentContext = context;
        super.onAttach(context);
    }
}
