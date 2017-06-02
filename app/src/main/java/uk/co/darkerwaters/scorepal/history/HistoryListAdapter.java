package uk.co.darkerwaters.scorepal.history;

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
import uk.co.darkerwaters.scorepal.HistoryDetailsActivity;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.ScoreData;

/**
 * Created by douglasbrain on 26/05/2017.
 */

public class HistoryListAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater mLayoutInflater;

    private ArrayList<HistoryFile> mEntries = new ArrayList<HistoryFile>();

    public HistoryListAdapter(Context context) {
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
            itemView = (RelativeLayout) mLayoutInflater.inflate(R.layout.history_list_view_item, parent, false);

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
            final HistoryFile file = mEntries.get(position);
            if (null != file) {
                // set up the delete click handler
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDeleteFile(file);
                    }
                });
                // and the extra information click handler
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onShowFileDetails(file);
                    }
                });
                String title = file.getPlayerOne(mContext) +
                        " " +
                        mContext.getResources().getString(R.string.vs) +
                        " " +
                        file.getPlayerTwo(mContext);
                titleText.setText(title);
                // set the nice image
                switch (file.getGameMode()) {
                    case K_SCOREWIMBLEDON3:
                    case K_SCOREWIMBLEDON5:
                    case K_SCOREFAST4:
                        // this is a nice game of tennis
                        imageView.setImageResource(R.drawable.tennis);
                        break;
                    case K_SCOREBADMINTON3:
                    case K_SCOREBADMINTON5:
                        // this is a nice game of badminton
                        imageView.setImageResource(R.drawable.badminton);
                        break;
                    default:
                        // this is something we score points in
                        imageView.setImageResource(R.drawable.points);
                        break;
                }
                Date datePlayed = file.getDatePlayed();
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
                String scoreString = file.getScoreString(mContext);
                // get the type
                scoreTypeText.setText(file.getScoreStringType(mContext, scoreString));
                // and the actual points
                scoreText.setText(file.getScoreStringPoints(mContext, scoreString));
                // is this device connected?
                ScoreData latest = BtManager.getManager().getLatestScoreData();
                if (HistoryFile.isFileDatesSame(datePlayed, HistoryManager.getManager().getMatchStartedDate())) {
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

    private void onShowFileDetails(HistoryFile file) {
        Intent intent = new Intent(mContext, HistoryDetailsActivity.class);
        // the score data in this file is not fully loaded, fully load it now
        HistoryFile loadedFile = file.readFileContent(file.getFilename(), mContext);
        intent.putExtra("uk.co.darkerwaters.scorepal.historyfilename", file.getFilename());
        intent.putExtra("uk.co.darkerwaters.scorepal.historyfile", loadedFile.getScoreData().toString());
        mContext.startActivity(intent);
    }

    private void onDeleteFile(final HistoryFile file) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(mContext.getResources().getString(R.string.warning));
        alert.setMessage(mContext.getResources().getString(R.string.sure_to_delete));
        alert.setPositiveButton(mContext.getResources().getString(R.string.yes), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // delete the specified file
                mContext.deleteFile(file.getFilename());
                mEntries.remove(file);
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

    public void upDateEntries(List<HistoryFile> entries) {
        mEntries.clear();
        if (null != entries) {
            for (HistoryFile file : entries) {
                mEntries.add(file);
            }
        }
        notifyDataSetChanged();
    }

    public void add(HistoryFile file) {
        mEntries.add(file);
        notifyDataSetChanged();
    }
}