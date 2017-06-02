package uk.co.darkerwaters.scorepal.history;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by douglasbrain on 28/05/2017.
 */

public class HistoryManager {

    private static final HistoryManager INSTANCE = new HistoryManager();
    private Date matchStartedDate;

    private HistoryManager () {
        // on creation remember the date and time the latest match started
        this.matchStartedDate = new Date();
    }

    public static HistoryManager getManager() {
        return INSTANCE;
    }

    public Date getMatchStartedDate() {
        return this.matchStartedDate;
    }

    public void resetMatchStartedDate() {
        this.matchStartedDate = new Date();
    }

    public void deleteFilesForCurrentMatch(Context context) {
        // list all our local files to delete the ones that correspond to the match start date
        String[] files = context.fileList();
        for (String filename : files) {
            if (HistoryFile.isValidFilename(filename)) {
                // this is a match file, get the date this was played
                Date datePlayed = HistoryFile.getDatePlayed(filename);
                if (HistoryFile.isFileDatesSame(datePlayed, this.matchStartedDate)) {
                    // this file is for our current match, delete it
                    context.deleteFile(filename);
                }
            }
        }
    }

    public List<HistoryFile> listHistory(Context context) {
        ArrayList<HistoryFile> fileList = new ArrayList<HistoryFile>();
        // list all our local files
        String[] files = context.fileList();
        for (String filename : files) {
            if (HistoryFile.isValidFilename(filename)) {
                // this is a match file, add to the list of files to return
                try {
                    HistoryFile file = HistoryFile.createEmptyContainer(filename, context);
                    fileList.add(file);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // to be nice, sort the list
        Collections.sort(fileList);
        return fileList;


        /*FileList files = driveService.files().list()
                .setSpaces("appDataFolder")
                .setFields("nextPageToken, files(id, name)")
                .setPageSize(10)
                .execute();
        for(File file: files.getFiles()) {
            System.out.printf("Found file: %s (%s)\n",
                    file.getName(), file.getId());
        }
        File fileMetadata = new File();
        fileMetadata.setName("config.json");
        fileMetadata.setParents(Collections.singletonList("appDataFolder"));
        java.io.File filePath = new java.io.File("files/config.json");
        FileContent mediaContent = new FileContent("application/json", filePath);
        File file = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
        System.out.println("File ID: " + file.getId());*/
    }
}
