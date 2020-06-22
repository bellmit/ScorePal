package uk.co.darkerwaters.scorepal.ui.matchlists;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayInputStream;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.application.MatchStatistics;
import uk.co.darkerwaters.scorepal.points.Sport;
import uk.co.darkerwaters.scorepal.ui.views.CircularProgressBar;
import uk.co.darkerwaters.scorepal.ui.views.ContactResolver;

public class CardHolderStatistics extends RecyclerView.ViewHolder {

    private final View parent;

    public static class StatisticsCard {
        public final String title;
        public final String imageUrl;
        public final String description;
        public final int imageRes;
        public final Pair<Integer, Integer> winsLosses;

        public StatisticsCard(Context context, String opponentName, ContactResolver contactResolver) {
            MatchStatistics matchStatistics = MatchStatistics.GetInstance(context, true);
            // set the data here, the name and image if we have one
            this.title = opponentName;
            // set the image of the user if they have one
            this.imageRes = R.drawable.ic_person_black_24dp;
            this.imageUrl = contactResolver.getContactImage(opponentName);
            this.winsLosses = matchStatistics.getWinsLossesAgainstOpponent(opponentName);
            this.description = String.format(context.getString(R.string.opponent_summary),
                    opponentName, this.winsLosses.first, this.winsLosses.second);
        }

        public StatisticsCard(Context context, Sport sport) {
            MatchStatistics matchStatistics = MatchStatistics.GetInstance(context, true);
            // set the data here, the name and image if we have one
            this.title = context.getString(sport.strRes);
            this.imageRes = sport.iconRes;
            this.imageUrl = null;
            // get the stats for the sport
            int wins = matchStatistics.getTotalWins(sport);
            int losses = matchStatistics.getTotalLosses(sport);
            this.winsLosses = new Pair<>(wins, losses);
            // set the description
            this.description = context.getString(R.string.matches_played, wins + losses);
        }

    }

    private final TextView itemTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;
    private final TextView statisticsSummary;

    private final CircularProgressBar wins;
    private final CircularProgressBar losses;

    public CardHolderStatistics(View itemView) {
        super(itemView);
        this.parent = itemView;

        // card is created, find all our children views and stuff here
        this.itemImage = this.parent.findViewById(R.id.statisticsImage);
        this.itemTitle = this.parent.findViewById(R.id.statisticsTitle);
        this.itemDetail = this.parent.findViewById(R.id.statisticsDescription);
        this.statisticsSummary = this.parent.findViewById(R.id.statisticsSummary);

        this.wins = this.parent.findViewById(R.id.statisticsProgressWins);
        this.losses = this.parent.findViewById(R.id.statisticsProgressLosses);

        // set the title colours to match
        int textColor = parent.getContext().getColor(R.color.primaryTextColor);
        wins.setTitleColor(textColor);
        wins.setSubTitleColor(textColor);
        losses.setTitleColor(textColor);
        losses.setSubTitleColor(textColor);
    }

    public void initialiseCard(final Context context, final StatisticsCard data) {

        // set the data here, the name and image if we have one
        this.itemTitle.setText(data.title);
        this.statisticsSummary.setText(data.description);

        boolean isImageSet = false;
        if (null != data.imageUrl && !data.imageUrl.isEmpty()) {
            try {
                // set the URL
                this.itemImage.setImageURI(Uri.parse(data.imageUrl));
                isImageSet = true;
            }
            catch (Exception e) {
                // fail
                Log.error("Failed to set the image URL", e);
            }
        }
        if (!isImageSet) {
            // reset to the standard resource instead
            this.itemImage.setImageResource(data.imageRes);
            // and colour this nice and green to match everything else
            this.itemImage.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.primaryDarkColor)));
        }

        // and the number of wins / losses
        // and get the numbers to show
        float total = data.winsLosses.first + data.winsLosses.second;

        if (total > 0) {
            float progress = data.winsLosses.first / total * 100f;
            this.wins.setProgress((int)progress);
            progress = data.winsLosses.second / total * 100f;
            this.losses.setProgress((int)progress);
        }
        else {
            this.wins.setProgress(0);
            this.losses.setProgress(0);
        }
        // set the text for the numbers
        this.wins.setTitle(Integer.toString(data.winsLosses.first));
        this.losses.setTitle(Integer.toString(data.winsLosses.second));
        // and the subtitles
        this.wins.setSubTitle(context.getString(R.string.statistics_wins));
        this.losses.setSubTitle(context.getString(R.string.statistics_losses));
    }

    private long getContactIDFromNumber(String contactName, Context context) {
        String UriContactName = Uri.encode(contactName);
        // create the cursor to find this
        Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, UriContactName),
                new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID}, null, null, null);
        long id = -1;
        while (contactLookupCursor.moveToNext()) {
            id = contactLookupCursor.getLong(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
        }
        contactLookupCursor.close();
        return id;
    }

    private Bitmap openPhoto(long contactId, Context context) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
