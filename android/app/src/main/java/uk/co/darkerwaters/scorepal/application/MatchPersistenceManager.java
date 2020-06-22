package uk.co.darkerwaters.scorepal.application;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Pair;

import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.dataui.MatchWriter;
import uk.co.darkerwaters.scorepal.ui.UiHelper;
import uk.co.darkerwaters.scorepal.ui.login.ActivityLogin;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;

public class MatchPersistenceManager {

    public static final String K_EXPORT_EXT = ".mtch";
    private static final String K_RECENT_EXT = "r" + K_EXPORT_EXT;
    private static final String K_RESTORED_EXT = "r" + K_RECENT_EXT;
    private static final String K_HIDDEN_EXT = "h" + K_EXPORT_EXT;
    private static final String K_DELETED_EXT = "d" + K_EXPORT_EXT;

    private static final String[] K_EXTENSIONS = new String[] {
            /*FIRST*/K_RESTORED_EXT,
            K_RECENT_EXT,
            K_HIDDEN_EXT,
            K_DELETED_EXT,
            /*LAST*/K_EXPORT_EXT
    }; //HAS TO BE LONGEST FIRST, SHORTEST LAST SO DOESN'T FIND RECENT WHEN ACTUALLY RESTORED

    private static final long K_FILE_SIZE_MAX = 20L /*MB*/ * 1024L * 1024L;
    private static final int K_DAYS_TO_KEEP_RECENT = 10;
    private static final int K_DAYS_TO_KEEP_DELETED = 10;

    public static final int K_VERSION = 1;
    public static final boolean K_ISDORECENT = true;

    private class MatchCache {
        private static final int K_CACHE_SIZE = 5;

        private final ArrayList<Pair<String, Match>> cachedMatches;

        MatchCache() {
            this.cachedMatches = new ArrayList<>();
        }

        synchronized Pair<String, Match> cacheFile(String matchFileId, Match match) {
            for (int i = 0; i < this.cachedMatches.size(); ++i) {
                Pair<String, Match> cached = this.cachedMatches.get(i);
                if (cached.first.equals(matchFileId)) {
                    // this is the cached one, remove it
                    this.cachedMatches.remove(i);
                    break;
                }
            }
            // cache this match then
            Pair<String, Match> matchData = new Pair(matchFileId, match);
            this.cachedMatches.add(matchData);
            while (this.cachedMatches.size() > K_CACHE_SIZE) {
                this.cachedMatches.remove(0);
            }
            // return the data created to be / was cached
            return matchData;
        }

        synchronized Match getCached(String matchFileId) {
            for (int i = 0; i < this.cachedMatches.size(); ++i) {
                Pair<String, Match> cached = this.cachedMatches.get(i);
                if (cached.first.equals(matchFileId)) {
                    // this is the cached one
                    return cached.second;
                }
            }
            // not cached
            return null;
        }

        synchronized void clear(String matchFileId) {
            for (int i = 0; i < this.cachedMatches.size(); ++i) {
                Pair<String, Match> cached = this.cachedMatches.get(i);
                if (cached.first.equals(matchFileId)) {
                    // this is the cached one
                    this.cachedMatches.remove(i);
                    // and stop looking as just invalidated the list of matches
                    break;
                }
            }
        }

        synchronized void clear() {
            this.cachedMatches.clear();
        }
    }

    private final MatchCache cache = new MatchCache();

    private static MatchPersistenceManager INSTANCE = null;

    public static MatchPersistenceManager GetInstance() {
        if (null == INSTANCE) {
            INSTANCE = new MatchPersistenceManager();
        }
        return INSTANCE;
    }

    private MatchPersistenceManager() {
        // constructor
    }

    public void clearCache() {
        // clear the cache of all matches
        cache.clear();
    }

    public boolean isFileHidden(MatchId matchId, Context context) {
        File matchFile = findMatchFile(matchId, context);
        return null != matchFile && matchFile.getName().endsWith(K_HIDDEN_EXT);
    }

    public boolean isFileRecent(MatchId matchId, Context context) {
        File matchFile = findMatchFile(matchId, context);
        return null != matchFile && matchFile.getName().endsWith(K_RECENT_EXT);
    }

    public boolean isFileRestored(MatchId matchId, Context context) {
        File matchFile = findMatchFile(matchId, context);
        return null != matchFile && matchFile.getName().endsWith(K_RESTORED_EXT);
    }

    public boolean isFileDeleted(MatchId matchId, Context context) {
        File matchFile = findMatchFile(matchId, context);
        return null != matchFile && matchFile.getName().endsWith(K_DELETED_EXT);
    }

    public void checkForExcessiveFiles(Context context) {
        // first time we save let's have a look at the files we have hanging around
        // and clear excessive files
        Log.info("Clearing old files from our store");
        hideOldFiles(context);
        Log.info("Clearing deleted files from our store");
        deleteDeletedFiles(context);
        Log.info("Clearing excessive files from our store");
        clearExcessiveFiles(context);
    }

    synchronized private void clearExcessiveFiles(Context context) {
        long folderSize = getFileFolderSize(context.getFilesDir());
        if (folderSize > K_FILE_SIZE_MAX) {
            // while there is data to delete, delete it
            // go through from the oldest to the newest deleting until we are less
            // than the limit, only doing 10 at a time to prevent going mental
            int iDeleted = 0;
            long minToDelete = folderSize - K_FILE_SIZE_MAX;
            float sizeMB = folderSize / 1024f / 1024f;
            Log.info("match file contents add up to " + sizeMB + " MB - deleting some");
            File[] files = listMatches(new String[] {K_EXPORT_EXT}, -1, context);
            // file names are all as strings, sorting on the name works
            sortFiles(files);
            // let's delete enough data, or 10 files at least (so we don't do this every time)
            for (File file : files) {
                // delete the file
                try {
                    if (file.delete()) {
                        // remove the size of the file from what we need to delete
                        minToDelete -= file.length();
                        ++iDeleted;
                        Log.info("Deleted " + file.getName() + " to free up some space");
                    } else {
                        // try to ask it to later?
                        file.deleteOnExit();
                        Log.info("Deleting " + file.getName() + " later to free up some space");
                    }
                } catch (Exception e) {
                    Log.error("Failed to delete a file", e);
                }
                if (minToDelete <= 0 && iDeleted > 10) {
                    // we deleted enough, and at least ten - stop looking
                    break;
                }
            }
            sizeMB = getFileFolderSize(context.getFilesDir()) / 1024f / 1024f;
            Log.info("match file contents now add up to " + sizeMB + " MB - deleted " + iDeleted);
        }
    }

    public static int DaysDifference(Date now, Date matchDate) {
        return ((int)((now.getTime() / (24*60*60*1000))
                - (int)(matchDate.getTime() / (24*60*60*1000))));
    }

    synchronized private void hideOldFiles(Context context) {
        // also in here we can check for old files
        File[] files = listMatches(new String[] {K_EXPORT_EXT}, -1, context);
        sortFiles(files);
        Date now = new Date();
        for (File file : files) {
            String filename = file.getName();
            // get the date from this
            MatchId matchId = new MatchId(removeExtensionFromString(filename));
            if (matchId.isValid()) {
                if (DaysDifference(now, matchId.getDate()) > K_DAYS_TO_KEEP_RECENT
                        && false == filename.endsWith(K_RESTORED_EXT)
                        && false == filename.endsWith(K_HIDDEN_EXT)
                        && false == filename.endsWith(K_DELETED_EXT) ) {
                    // this is 10 days old, or more, perm delete it
                    if (false == file.delete()) {
                        Log.info("Failed delete of " + matchId);
                        file.deleteOnExit();
                    }
                    Log.info(matchId.getSport() + " match file " + matchId + " old so permanently deleted...");
                }
            }
        }
    }

    synchronized private void deleteDeletedFiles(Context context) {
        // also in here we can check for old deleted files
        File[] files = listMatches(new String[] {K_DELETED_EXT}, -1, context);
        Date now = new Date();
        for (File file : files) {
            String filename = file.getName();
            // get the date from this
            MatchId matchId = new MatchId(removeExtensionFromString(filename));
            if (!matchId.isValid() || DaysDifference(now, matchId.getDate()) > K_DAYS_TO_KEEP_DELETED) {
                // this is invalid or 10 days old, or more, perm delete it
                if (false == file.delete()) {
                    Log.info("Failed delete of " + matchId);
                    file.deleteOnExit();
                }
                Log.info(matchId.getSport() + " match file " + matchId + " old so permanently deleted...");
            }
        }
    }

    private void sortFiles(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                // sort in filename order to put oldest first and newest last
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    synchronized private long getFileFolderSize(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    size += file.length();
                } else
                    size += getFileFolderSize(file);
            }
        } else if (dir.isFile()) {
            size += dir.length();
        }
        return size;
    }

    synchronized public Match loadMatch(MatchId matchId, Context context) {
        // find the actual file for this match id
        File file = findMatchFile(matchId, context);
        if (null == file) {
            // doesn't exist
            return null;
        }
        // here we can get the actual file match id (includes extension)
        Match matchData = cache.getCached(file.getName());
        if (null == matchData) {
            // load the data in I suppose then...
            StringBuilder json = new StringBuilder();
            try {
                FileInputStream stream = new FileInputStream(file);
                int size;
                while ((size = stream.available()) > 0) {
                    // while there is data, get it out
                    byte[] buffer = new byte[size];
                    stream.read(buffer);
                    // append this buffer to the string builder
                    json.append(new String(buffer, StandardCharsets.UTF_8));
                }
                stream.close();
                // and load this data in properly
                matchData = loadMatch(json.toString());
                // and cache this loaded data
                cache.cacheFile(file.getName(), matchData);
            } catch (FileNotFoundException e) {
                Log.error("Failed to read the file", e);
            } catch (IOException e) {
                Log.error("Failed to read the JSON", e);
            } catch (JSONException e) {
                Log.error("Failed to create the JSON", e);
            } catch (Throwable e) {
                Log.error("Failed match loading seriously: " + e.getMessage());
            }
        }
        return matchData;
    }

    private synchronized Match loadMatch(String matchDataString) throws Exception {
        // now we have ths string, we can create JSON from it
        JSONObject obj = new JSONObject(matchDataString);
        // get the version number
        final int version = obj.getInt("ver");
        // get the settings from the data
        String settingsString = obj.getString("settings");
        // and create the settings from this
        MatchSetup setup = MatchSetup.CreateFromJSON(settingsString);

        // create the match from this
        String matchString = obj.getString("match");
        Match match = Match.CreateFromJSON(matchString, setup);
        // this is all well loaded, return this
        return match;
    }

    public boolean isMatchDataPersisted(Match match) {
        return match.isDataPersisted();
    }

    synchronized public MatchId saveAcceptedMatchToFile(Match match, Context context) {
        // save the match and change the end to be recent / hidden depending on if we are doing that
        return saveMatchToFile(match, K_ISDORECENT ? K_RECENT_EXT : K_HIDDEN_EXT, context);
    }

    synchronized public MatchId saveMatchToFile(Match match, Context context) {
        // save the match with the existing file extension
        MatchId matchId = new MatchId(match);
        File existingFile = findMatchFile(matchId, context);
        String fileExt = null;
        if (null != existingFile && existingFile.exists()) {
            // we have a file that already exists, use the extension from it
            for (String extension : K_EXTENSIONS) {
                if (existingFile.getName().endsWith(extension)) {
                    // this is the extension
                    fileExt = extension;
                    break;
                }
            }
        }
        if (null != fileExt) {
            // use this new file extension (the existing one) to save the file
            return saveMatchToFile(match, fileExt, context);
        }
        else {
            // we don't have an extension, just use a good default
            return saveAcceptedMatchToFile(match, context);
        }
    }

    synchronized public MatchId saveMatchToFile(Match match, String newFileExt, Context context) {
        File file;
        try {
            // the storage of a match is based on it's played date
            Date matchPlayedDate = match.getDateMatchStarted();
            YearFolder fileFolder = new YearFolder(matchPlayedDate, context);
            if (!fileFolder.folder.exists()) {
                fileFolder.folder.mkdirs();
            }
            MatchId matchId = new MatchId(match);
            MatchSetup setup = match.getSetup();
            // delete any match files that are hanging around for this ID
            for (File toDelete : findMatchFiles(matchId, context)) {
                // delete each one right away as we are to replace this
                try {
                    toDelete.delete();
                }
                catch (Exception e) {
                    Log.error("Failed to delete the file that already exists", e);
                }
            }
            // create the file here from the ID and the correct extension
            file = new File(fileFolder.folder, matchId.toString() + newFileExt);
            Writer output = new BufferedWriter(new FileWriter(file));
            JSONObject obj = new JSONObject();
            // put the version number in
            obj.put("ver", K_VERSION);
            // put the settings in the object first, these will determine the match created too
            obj.put("settings", setup.getAsJSON());
            obj.put("match", match.getAsJSON());
            // inform the match the data is saved
            match.setDataPersisted();
            // close the file
            output.write(obj.toString());
            output.close();
            // this is all well saved - probably with new data, cache this
            cache.cacheFile(file.getName(), match);

        } catch (Exception e) {
            Log.error("Failed to write the JSON match file", e);
            file = null;
        }
        return fileToMatchId(file);
    }

    private final class YearFolder {
        final File folder;
        final int year;
        YearFolder(Date matchPlayedDate, Context context) {
            if (null == matchPlayedDate || null == context) {
                year = -1;
                folder = null;
            }
            else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(matchPlayedDate);
                year = cal.get(Calendar.YEAR);
                folder = new File(context.getFilesDir().getPath() + "/mtc" + year);
            }
        }
        YearFolder(File folder) {
            this.folder = folder;
            int discoveredYear = -1;
            if (null != this.folder && this.folder.isDirectory()) {
                String yearString = this.folder.getName();
                if (null != yearString && yearString.length() == 7 && yearString.startsWith("mtc")) {
                    try {
                        // parse the last 4 chars into the year number
                        discoveredYear = Integer.parseInt(yearString.substring(3));
                    } catch (NumberFormatException e) {
                        // not a year
                        Log.debug(yearString + " is not a year folder");
                    }
                }
            }
            this.year = discoveredYear;
        }

        public boolean isValid() {
            return year != -1;
        }
    }

    synchronized public Integer[] listMatchYears(Context context) {
        // return the years (folders in the directory)
        List<Integer> years = new ArrayList<>();
        for (File yearDirectory : context.getFilesDir().listFiles()) {
            YearFolder folder = new YearFolder(yearDirectory);
            if (folder.isValid()) {
                // this is a valid year folder
                years.add(folder.year);
            }
        }
        // return this as an array of integers
        return years.toArray(new Integer[0]);
    }

    public MatchId[] listMatches(int year, Context context) {
        File[] fileList = listMatches(new String[] {K_RECENT_EXT, K_HIDDEN_EXT}, year, context);
        return filesToMatchIds(fileList);
    }

    public MatchId[] listRecentMatches(int year, Context context) {
        File[] fileList = listMatches(new String[] {K_RECENT_EXT}, year, context);
        return filesToMatchIds(fileList);
    }

    public MatchId[] listHiddenMatches(int year, Context context) {
        File[] fileList = listMatches(new String[] {K_HIDDEN_EXT}, year, context);
        return filesToMatchIds(fileList);
    }

    public MatchId[] listDeletedMatches(int year, Context context) {
        File[] fileList = listMatches(new String[] {K_DELETED_EXT}, year, context);
        return filesToMatchIds(fileList);
    }

    private MatchId[] filesToMatchIds(File[] files) {
        MatchId[] matchIds = new MatchId[files.length];
        for (int i = 0; i < matchIds.length; ++i) {
            matchIds[i] = fileToMatchId(files[i]);
        }
        return matchIds;
    }

    private MatchId fileToMatchId(File file) {
        // remove the extension from the filename and this is the ID
        return new MatchId(file == null ? "" : removeExtensionFromString(file.getName()));
    }

    synchronized private File findMatchFile(MatchId matchId, Context context) {
        // we can find the file by reconstructing where it would be
        // first we need the match date from the id
        Date matchDate = matchId.getDate();
        // this gives us the year, which is the folder
        YearFolder matchFolder = new YearFolder(matchDate, context);
        File file = null;
        if (matchFolder.isValid() && matchFolder.folder.exists()) {
            // have the folder, the file can be in here, but with a number of extensions
            // on the end, hidden, recent, restored are the 3...
            // rather than a search, let's just see if they each exist
            for (String extension: K_EXTENSIONS) {
                file = new File(matchFolder.folder, matchId + extension);
                if (file.exists()) {
                    // this is it
                    break;
                }
                else {
                    // this isn't any good
                    file = null;
                }
            }
        }
        return file;
    }

    synchronized private File[] findMatchFiles(MatchId matchId, Context context) {
        // we can find the file by reconstructing where it would be
        // first we need the match date from the id
        Date matchDate = matchId.getDate();
        // this gives us the year, which is the folder
        YearFolder matchFolder = new YearFolder(matchDate, context);
        List<File> files = new ArrayList<>();
        if (matchFolder.isValid() && matchFolder.folder.exists()) {
            // have the folder, the file can be in here, but with a number of extensions
            // on the end, hidden, recent, restored are the 3...
            // rather than a search, let's just see if they each exist
            for (String extension: K_EXTENSIONS) {
                File file = new File(matchFolder.folder, matchId + extension);
                if (file.exists()) {
                    // this is one
                    files.add(file);
                }
            }
        }
        return files.toArray(new File[0]);
    }

    synchronized private File[] listMatches(final String[] extensions, int year, Context context) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                boolean isValid = false;
                for (String extension : extensions) {
                    if (s.endsWith(extension)) {
                        // this is the correct extension, need to remove it
                        String matchId = removeExtensionFromString(s);
                        isValid = MatchId.IsMatchIdValid(matchId);
                        break;
                    }
                }
                return isValid;
            }
        };
        // return the files that are valid matches
        List<File> filesFound = new ArrayList<>();
        for (File yearDirectory : context.getFilesDir().listFiles()) {
            // list all the folders
            YearFolder yearFolder = new YearFolder(yearDirectory);
            if (yearFolder.isValid() && yearFolder.folder.isDirectory()) {
                if (year == -1 || yearFolder.year == year) {
                    // we are interested in this year add the entire contents
                    filesFound.addAll(Arrays.asList(yearDirectory.listFiles(filter)));
                }
            }
        }
        // return this as an array
        return filesFound.toArray(new File[0]);
    }

    private String removeExtensionFromString(String s) {
        // go through the extension, longest first, and remove the most we can
        for (String extension : K_EXTENSIONS) {
            if (s.endsWith(extension)) {
                // this is is, remove this
                return s.substring(0, s.length() - extension.length());
            }
        }
        // there isn't one
        Log.error("cannot remove not-existent extension from " + s);
        return s;
    }

    synchronized public boolean deleteMatchFile(final MatchId matchId, Context context) {
        File matchFile = findMatchFile(matchId, context);
        if (null != matchFile) {
            // just rename this file to the different extension
            String existingName = matchFile.getName();
            // clear the old extension
            String newFilename = removeExtensionFromString(existingName);
            // and add the deleted extension
            newFilename += K_DELETED_EXT;
            // and rename the file to this
            if (renameFile(matchFile.getParentFile(), existingName, newFilename)) {
                UiHelper.showUserMessage(context, R.string.deleted_toast);
                return true;
            }
            else {
                // failed
                return false;
            }
        }
        else {
            // not found so not deleted
            return false;
        }
    }

    synchronized public void wipeDeletedMatchFile(final MatchId matchId, final Runnable onDeleted, final Runnable onNotDeleted, final Activity context) {
        // warn them, then do the deed
        new CustomSnackbar(context,
                R.string.matchDeleteConfirmation,
                R.drawable.ic_help_black_24dp,
                R.string.yes, R.string.no,
                new CustomSnackbar.SnackbarListener() {
                    @Override
                    public void onButtonOnePressed() {
                        //Yes button clicked
                        //Yes button clicked, delete this data!
                        File matchFile = findMatchFile(matchId, context);
                        if (null != matchFile && matchFile.delete()) {
                            // deleted! tell the caller to remove from the list
                            if (null != onDeleted) {
                                onDeleted.run();
                            }
                        }
                        else {
                            UiHelper.showUserMessage(context, R.string.delete_failure);
                        }
                    }
                    @Override
                    public void onButtonTwoPressed() {
                        // no, do nothing
                        if (null != onNotDeleted) {
                            onNotDeleted.run();
                        }
                    }
                    @Override
                    public void onDismissed() {
                        // closed, do nothing
                        if (null != onNotDeleted) {
                            onNotDeleted.run();
                        }
                    }
                });
    }

    synchronized public void wipeAllMatchFiles(Context context) {
        for (File matchFile : listMatches(K_EXTENSIONS, -1, context)) {
            // for every match file we found, all extension all years, delete each one
            if (!matchFile.delete()) {
                // failed to delete, delete on exit instead to try our hardest here
                matchFile.deleteOnExit();
            }
        }
    }

    public boolean hideMatchFile(final MatchId matchId, final Context context, final CustomSnackbar.SnackbarListener listener) {
        if (hideMatchFile(findMatchFile(matchId, context), context)) {
            // tell them this
            if (context instanceof Activity) {
                // can give them the option to undo this
                new CustomSnackbar((Activity) context,
                        R.string.hidden_toast,
                        R.drawable.ic_history_black_24dp,
                        R.string.undoBtn,
                        new CustomSnackbar.SnackbarListener() {
                            @Override
                            public void onButtonOnePressed() {
                                // undo button clicked
                                restoreMatchFile(matchId, context);
                                if (null != listener) {
                                    listener.onButtonOnePressed();
                                }
                            }
                            @Override
                            public void onButtonTwoPressed() {
                                // no, do nothing
                                if (null != listener) {
                                    listener.onButtonTwoPressed();
                                }
                            }
                            @Override
                            public void onDismissed() {
                                // closed, do nothing
                                if (null != listener) {
                                    listener.onDismissed();
                                }
                            }
                        });
            }
            else {
                // bit helpless so just showing a message
                UiHelper.showUserMessage(context, R.string.hidden_toast);
            }
            return true;
        }
        else {
            // failed for some reason
            return false;
        }
    }

    synchronized private boolean hideMatchFile(final File matchFile, Context context) {
        if (null != matchFile) {
            // just rename this file to the different extension
            String existingName = matchFile.getName();
            // clear the old extension
            String newFilename = removeExtensionFromString(existingName);
            // and add the hidden extension
            newFilename += K_HIDDEN_EXT;
            // and rename the file
            if (renameFile(matchFile.getParentFile(), existingName, newFilename)) {
                return true;
            }
            else {
                // failed
                return false;
            }
        }
        else {
            // not found
            return false;
        }
    }

    synchronized public boolean restoreMatchFile(MatchId matchId, Context context) {
        // find this file
        File matchFile = findMatchFile(matchId, context);
        if (null != matchFile) {
            // just rename this file to the different extension
            String existingName = matchFile.getName();
            // clear the old extension
            String newFilename = removeExtensionFromString(existingName);
            if (isFileDeleted(matchId, context)) {
                // delete goes to hidden
                newFilename += K_HIDDEN_EXT;
            }
            else {
                // go back to restored
                newFilename += K_RESTORED_EXT;
            }
            // and rename the file to restore it (will work on hidden, deleted, restored - everything)
            if (renameFile(matchFile.getParentFile(), existingName, newFilename)) {
                UiHelper.showUserMessage(context, R.string.restored_toast);
                return true;
            }
            else {
                // this failed
                return false;
            }
        }
        else {
            // not found
            return false;
        }
    }

    synchronized private boolean renameFile(File directory, String existingName, String newFilename) {
        boolean isRenamed = false;
        if (!newFilename.equals(existingName)) {
            // the names are different, rename the file
            File from = new File(directory, existingName);
            File to = new File(directory, newFilename);
            if (to.exists()) {
                // for robustness, let's get rid of this
                to.delete();
            }
            if (from.exists() && !to.exists()) {
                // from is there and to is not, go for it
                isRenamed = from.renameTo(to);
            }
        }
        if (isRenamed) {
            // we just made the filenames invalid in the cache, clear them from the cache
            cache.clear(existingName);
            cache.clear(newFilename);
        }
        return isRenamed;
    }

    synchronized public MatchId importMatchData(Context context, Uri uri) {
        MatchId matchImported = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            inputStream.close();

            // and load this match data
            Match match = loadMatch(stringBuilder.toString());
            if (null != match) {
                // we have the match, so we want to save this as the latest match file
                matchImported = saveAcceptedMatchToFile(match, context);
                // make this part of our stats
                MatchStatistics.OnMatchResultsAccepted(match, context);
            }
        }
        catch (Exception e) {
            Log.error("Failed to import match", e);
        }
        return matchImported;
    }

    synchronized public void shareMatchData(Match match, final MatchId matchId, final boolean isSendFile, Context context) {
        // find this file
        File matchFile = findMatchFile(matchId, context);
        // create the intent for sharing the data
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        // put all this in the intent
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, match.getDescription(MatchWriter.DescriptionLevel.LONG, context));

        if (isSendFile && null != matchFile) {
            // copy the file somewhere to where we can send it from
            String filename = matchId.toString() + MatchPersistenceManager.K_EXPORT_EXT;
            File directory = context.getExternalCacheDir();
            //File directory = getExternalFilesDir(null);
            File destination = new File(directory, filename);
            try {
                InputStream in = new FileInputStream(matchFile);
                OutputStream out = new FileOutputStream(destination);
                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                // close the streams
                in.close();
                out.close();
                // get the file URI in a shareable format
                Uri fileUri = FileProvider.getUriForFile(context,
                        context.getString(R.string.file_provider_authority),
                        destination);
                // put this in the intent to share it
                //sharingIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                // and delete the file
                destination.deleteOnExit();
            }
            catch (IOException e) {
                Log.error("Failed to copy file to external directory", e);
            }
            catch (Throwable e) {
                Log.error("Failed to share the file " + e.getMessage());
            }
        }
        // and start the intent
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}
