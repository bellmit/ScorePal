package uk.co.darkerwaters.scorepal.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.darkerwaters.scorepal.Common;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.storage.uk.co.darkerwaters.scorepal.storage.data.Match;
import uk.co.darkerwaters.scorepal.storage.ScoreData;
import uk.co.darkerwaters.scorepal.storage.StorageManager;

/**
 * Created by douglasbrain on 26/05/2017.
 */

public class MatchHistoryListAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater mLayoutInflater;

    private ArrayList<Match> mEntries = new ArrayList<Match>();

    public MatchHistoryListAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // return the size of th elist
        return mEntries.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < mEntries.size()) {
            return mEntries.get(position);
        }
        else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout itemView;
        if (convertView == null) {
            itemView = (RelativeLayout) mLayoutInflater.inflate(R.layout.list_item_match, parent, false);

        } else {
            itemView = (RelativeLayout) convertView;
        }

        ImageView imageView = (ImageView) itemView.findViewById(R.id.listImage);
        TextView titleText = (TextView) itemView.findViewById(R.id.listTitle);
        TextView scoreTypeText = (TextView) itemView.findViewById(R.id.history_row_score_type);
        TextView scoreText = (TextView) itemView.findViewById(R.id.history_row_score);
        TextView descriptionText = (TextView) itemView.findViewById(R.id.listDescription);
        Button deleteButton = (Button) itemView.findViewById(R.id.history_row_delete);

        if (position < mEntries.size()) {
            final Match match = mEntries.get(position);
            if (null != match) {
                // set up the delete click handler
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDeleteMatch(match);
                    }
                });
                // and the extra information click handler
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onShowMatchDetails(match);
                    }
                });
                String title = match.getPlayerOneTitle() +
                        " " +
                        mContext.getResources().getString(R.string.vs) +
                        " " +
                        match.getPlayerTwoTitle();
                titleText.setText(title);
                // set the nice image
                switch (match.getScoreMode()) {
                    case K_SCOREWIMBLEDON3:
                    case K_SCOREWIMBLEDON5:
                    case K_SCOREFAST4:
                        // this is a nice game of tennis
                        imageView.setImageResource(R.drawable.tennis_court);
                        break;
                    case K_SCOREBADMINTON3:
                    case K_SCOREBADMINTON5:
                        // this is a nice game of badminton
                        imageView.setImageResource(R.drawable.badminton_court);
                        break;
                    default:
                        // this is something we score points in
                        imageView.setImageResource(R.drawable.court);
                        break;
                }
                Date datePlayed = match.getMatchPlayedDate();
                // get the date and time played to show this
                String description;
                if (null == datePlayed) {
                    description = "Sorry, no data...";
                }
                else {
                    Format dateFormat = DateFormat.getDateFormat(mContext.getApplicationContext());
                    description = dateFormat.format(datePlayed);

                    Format timeFormat = DateFormat.getTimeFormat(mContext.getApplicationContext());
                    description += " " + timeFormat.format(datePlayed);
                }
                descriptionText.setText(description);
                // get the score type to show this
                String scoreString = match.getScoreSummary();
                // get the type
                scoreTypeText.setText(ScoreData.getScoreStringType(mContext, scoreString));
                // and the actual points
                scoreText.setText(ScoreData.getScoreStringPoints(mContext, scoreString));
                // is this device connected?
                if (Match.isFileDatesSame(datePlayed, StorageManager.getManager().getMatchStartedDate())) {
                    // this is the current match
                    itemView.setBackgroundColor(Common.getThemeAccentColor(mContext));
                }
                else {
                    itemView.setBackgroundColor(Common.getThemeBackgroundColor(mContext));
                }
            }
        }
        // return the expanded view
        return itemView;
    }

    private void onShowMatchDetails(Match match) {
        Intent intent = new Intent(mContext, MatchDetailsActivity.class);
        intent.putExtra("uk.co.darkerwaters.scorepal.match.userid", match.getUserId());
        intent.putExtra("uk.co.darkerwaters.scorepal.match.matchid", match.getMatchId());
        mContext.startActivity(intent);
    }

    private void onDeleteMatch(final Match match) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(mContext.getResources().getString(R.string.warning));
        alert.setMessage(mContext.getResources().getString(R.string.sure_to_delete));
        alert.setPositiveButton(mContext.getResources().getString(R.string.yes), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // delete the specified match data
                match.delete(StorageManager.getManager().getTopLevel());
                mEntries.remove(match);
                notifyDataSetChanged();
            }
        });
        alert.setNegativeButton(mContext.getResources().getString(R.string.no), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // whatever
            }
        });
        alert.show();
    }

    public void upDateEntries(List<Match> entries) {
        mEntries.clear();
        if (null != entries) {
            for (Match match : entries) {
                mEntries.add(match);
            }
        }
        notifyDataSetChanged();
    }

    public void add(Match match) {
        mEntries.add(0, match);
        notifyDataSetChanged();
    }
}