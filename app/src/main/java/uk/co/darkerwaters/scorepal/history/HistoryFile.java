package uk.co.darkerwaters.scorepal.history;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ScoreData;

/**
 * Created by douglasbrain on 28/05/2017.
 */

public class HistoryFile implements Comparable<HistoryFile> {

    private final ScoreData scoreData;
    private final String filename;

    private HistoryFile(String filename, ScoreData scoreData) {
        this.filename = filename;
        this.scoreData = scoreData;
    }

    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final String K_FILENAME_PREFIX = "match_";
    private static final String K_FILENAME_EXT = ".spl";

    public static HistoryFile createEmptyContainer(String filename, Context context) {
        HistoryFile file = null;
        if (HistoryFile.isValidFilename(filename)) {
            // this is a valid filename, create the unloaded container
            file = new HistoryFile(filename, new ScoreData());
        }
        return file;
    }

    public static HistoryFile readFileContent(String filename, Context context) {
        HistoryFile file = null;
        try {
            FileInputStream fis = context.openFileInput(filename);
            StringBuilder fileStringBuilder = new StringBuilder();
            char current;
            while (fis.available() > 0) {
                current = (char) fis.read();
                fileStringBuilder.append(current);
            }
            // read this data into the score data
            ScoreData scoreData = new ScoreData(fileStringBuilder);
            if (null != scoreData && null != scoreData.toString()) {
                file = new HistoryFile(filename, scoreData);
            }
            else {
                // not a valid file
                context.deleteFile(filename);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static HistoryFile writeFileContent(String filename, ScoreData scoreData, Activity context) {
        HistoryFile file = new HistoryFile(filename, scoreData);
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(scoreData.toString().getBytes());
            fos.close();
            // this worked, remember the filename this data is stored as to overwrite if asked again
            scoreData.filename = filename;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static String createNewFilename(Context context, Date date, String playerOne, String playerTwo, ScoreData data) {
        // return the filename to use to store this match
        String currentDateandTime = fileDateFormat.format(date);
        String scoreString = getScoreString(context, data);
        return K_FILENAME_PREFIX +
                playerOne + "_" +
                playerTwo + "_" +
                scoreString + "_[" +
                data.currentScoreMode + "]_" +
                currentDateandTime +
                K_FILENAME_EXT;
    }

    private static String getScoreString(Context context, ScoreData data) {
        String scoreString = "";
        boolean isTennis = false;
        boolean isBadminton = false;
        // set the nice image
        switch (data.currentScoreMode) {
            case 1:
            case 2:
                // this is a nice game of tennis
                isTennis = true;
                break;
            case 3:
            case 4:
                // this is a nice game of badminton
                isBadminton = true;
                break;
            default:
                // this is something we score points in
                break;
        }
        if (data != null) {
            if (isTennis && data.sets != null && data.sets.first + data.sets.second > 0) {
                // the score is the number of sets they won - show this, we have to check
                // is we are in tennis because badminton puts their games in this data
                scoreString = context.getResources().getString(R.string.sets);
                scoreString += "-" + data.sets.first + "-" + data.sets.second;
            }
            else if (data.games != null && data.games.first + data.games.second > 0) {
                // the score is the number of games they won - show this
                scoreString = context.getResources().getString(R.string.games);
                scoreString += "-" + data.games.first + "-" + data.games.second;
            }
            else {
                // show the points string
                scoreString = context.getResources().getString(R.string.points);
                scoreString += "-" + data.getPointsAsString(data.points.first) +
                        "-" + data.getPointsAsString(data.points.second);
            }
        }
        else {
            // show the null data
            scoreString = context.getResources().getString(R.string.points) + "-0-0";
        }
        return scoreString;
    }

    public static boolean isValidFilename(String filename) {
        return filename.startsWith(K_FILENAME_PREFIX, 0) && filename.endsWith(K_FILENAME_EXT);
    }

    public String getFilename() {
        return this.filename;
    }

    public String getPlayerOne(Context context) {
        return getPlayerOne(context, filename);
    }

    public static String getPlayerOne(Context context, String filename) {
        // the player title is in the filename, remove the prefix
        String playerOne = context.getResources().getString(R.string.player_one);
        String content = filename.replace(K_FILENAME_PREFIX, "");
        // noe the player name is the first string up to the first underscore
        int index = content.indexOf("_");
        if (index > -1) {
            playerOne = content.substring(0, index);
        }
        return playerOne;
    }

    public String getPlayerTwo(Context context) {
        return getPlayerTwo(context, filename);
    }

    public static String getPlayerTwo(Context context, String filename) {
        // the player title is in the filename, remove the prefix
        String playerTwo = context.getResources().getString(R.string.player_two);
        String content = filename.replace(K_FILENAME_PREFIX, "");
        // now the player name is the first string up to the first underscore
        int oneIndex = content.indexOf("_");
        if (oneIndex > -1) {
            // but this is player one, find player two
            int twoIndex = content.indexOf("_", oneIndex + 1);
            if (twoIndex > -1) {
                playerTwo = content.substring(oneIndex + 1, twoIndex);
            }
        }
        return playerTwo;
    }

    public String getScoreString(Context context) {
        return getScoreString(context, filename);
    }

    public static String getScoreString(Context context, String filename) {
        // the summary of the score is in the filename, remove the prefix
        String scoreString = context.getResources().getString(R.string.points) + "-0-0";
        String content = filename.replace(K_FILENAME_PREFIX, "");
        // now the player name is the first string up to the first underscore
        int oneIndex = content.indexOf("_");
        if (oneIndex > -1) {
            // but this is player one, find player two
            int twoIndex = content.indexOf("_", oneIndex + 1);
            if (twoIndex > -1) {
                // this is player two, the next one is the score string
                int scoreIndex = content.indexOf("_", twoIndex + 1);
                if (scoreIndex > -1) {
                    // Finally - the score, return this
                    scoreString = content.substring(twoIndex + 1, scoreIndex);
                }
            }
        }
        return scoreString;
    }

    public int getGameMode() {
        return getGameMode(filename);
    }

    public static int getGameMode(String filename) {
        int gameMode = 1;
        // the game mode is encapsulated in [], so search for those
        int startIndex = filename.indexOf("[");
        int endIndex = filename.indexOf("]");
        if (startIndex > -1 && endIndex > -1 && endIndex > startIndex) {
            String modeString = filename.substring(startIndex + 1, endIndex);
            try {
                gameMode = Integer.parseInt(modeString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return gameMode;
    }

    public static String getScoreStringType(Context context, String scoreString) {
        // the score type is the first entry before the dash
        String scoreType = context.getResources().getString(R.string.points);
        // the type is the first string up to the first dash
        int index = scoreString.indexOf("-");
        if (index > -1) {
            scoreType = scoreString.substring(0, index);
        }
        return scoreType;
    }

    public static String getScoreStringPoints(Context context, String scoreString) {
        // the score is the data after the type (40-40) etc
        String score = "0-0";
        // the player type is the first string up to the first dash
        int index = scoreString.indexOf("-");
        if (index > -1) {
            // this is the dash separating the scores, return the score
            score = scoreString.substring(index + 1, scoreString.length());
        }
        return score;
    }

    public Date getDatePlayed() {
        return getDatePlayed(filename);
    }

    public  static Date getDatePlayed(String filename) {
        // the player title is in the filename, remove the prefix
        Date datePlayed = null;
        String content = filename.replace(K_FILENAME_PREFIX, "");
        content = content.replace(K_FILENAME_EXT, "");
        // the date is at the end
        int index = content.lastIndexOf("_");
        if (index > -1) {
            // parse this string
            String dateString = content.substring(index + 1, content.length());
            try {
                datePlayed = fileDateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        // and return the date
        return datePlayed;
    }

    public static boolean isFileDatesSame(Date fileDate1, Date fileDate2) {
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

    public String getSummary() {
        return this.scoreData == null ? "null" : this.scoreData.points.first + " - " + this.scoreData.points.second;
    }

    public ScoreData getScoreData() {
        return this.scoreData;
    }

    @Override
    public int compareTo(@NonNull HistoryFile other) {
        int result = 0;
        if (other != null ) {
            Date thisDatePlayed = getDatePlayed();
            Date otherDatePlayed = other.getDatePlayed();
            if (null != thisDatePlayed && null != otherDatePlayed) {
                result = thisDatePlayed.compareTo(otherDatePlayed);
            }
        }
        return result;
    }
}
