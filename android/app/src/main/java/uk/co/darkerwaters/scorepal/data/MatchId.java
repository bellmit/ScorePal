package uk.co.darkerwaters.scorepal.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.points.Sport;

public class MatchId {

    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private final String idString;

    public MatchId(Match parent) {
        this.idString =
                fileDateFormat.format(parent.getDateMatchStarted())
                + "_"
                + parent.getSport().name();
    }

    public MatchId(String idString) {
        this.idString = idString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchId matchId = (MatchId) o;
        return idString.equals(matchId.idString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idString);
    }

    @Override
    public String toString() {
        return this.idString;
    }

    public boolean isValid() {
        return IsMatchIdValid(this.idString);
    }

    public Date getDate() {
        return DateFromMatchId(this.idString);
    }

    public Sport getSport() {
        return SportFromMatchId(this.idString);
    }

    public static Date DateFromMatchId(String matchId) {
        Date played = null;
        if (null != matchId) {
            int sportSep = matchId.indexOf('_');
            String dateString = matchId;
            if (sportSep >= 0) {
                // there is an underscore, after this is the sport, before is the date
                dateString = matchId.substring(0, sportSep);
            }
            try {
                played = fileDateFormat.parse(dateString);
            } catch (ParseException e) {
                Log.error("Failed to create the match date from the match id " + matchId, e);
            }
        }
        return played;
    }

    public static Sport SportFromMatchId(String matchId) {
        Sport sport = null;
        if (null != matchId) {
            int sportSep = matchId.indexOf('_');
            String sportString = matchId;
            if (sportSep >= 0) {
                // there is an underscore, after this is the sport, before is the date
                sportString = matchId.substring(sportSep + 1);
            }
            try {
                sport = Sport.valueOf(sportString);
            } catch (Exception e) {
                Log.error("Failed to create the sport from the match id " + matchId, e);
            }
        }
        return sport;
    }

    public static boolean IsMatchIdValid(String matchId) {
        boolean isValid = false;
        try {
            fileDateFormat.parse(matchId);
            isValid = true;
        } catch (ParseException e) {
            // whatever, just isn't valid is all
        }
        return isValid;
    }

    public static boolean IsFileDatesSame(Date fileDate1, Date fileDate2) {
        // compare only up to seconds as only up to seconds stored in the filename
        // for simplicities sake we can use the same formatter we use for the filename and compare strings
        if (fileDate1 != null && fileDate2 == null) {
            return false;
        }
        else if (fileDate1 == null && fileDate2 != null) {
            return false;
        }
        else if (fileDate1 == null && fileDate2 == null) {
            return true;
        }
        else {
            // do the actual comparing then
            String stringDate1 = fileDateFormat.format(fileDate1);
            String stringDate2 = fileDateFormat.format(fileDate2);
            return stringDate1.equals(stringDate2);
        }
    }
}
