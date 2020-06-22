package uk.co.darkerwaters.scorepal.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import uk.co.darkerwaters.scorepal.R;

public class ActivityAttributions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attributions);

        findViewById(R.id.freePikLinkButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink(R.string.linkFreepik);
            }
        });
        findViewById(R.id.tennisLinkButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink(R.string.linkTennis);
            }
        });
        findViewById(R.id.badmintonLinkButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink(R.string.linkBadminton);
            }
        });
        findViewById(R.id.pingPongLinkButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink(R.string.linkPingPong);
            }
        });
        findViewById(R.id.pointsLinkButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink(R.string.linkPoints);
            }
        });
        findViewById(R.id.squashLinkButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink(R.string.linkSquash);
            }
        });
    }

    private void openLink(int stringId) {
        // send the user to the web
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(stringId)));
        startActivity(intent);
    }

}
