package uk.co.darkerwaters.scorepal;

import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by douglasbrain on 30/05/2017.
 */

public class ScoreData {

    /*
        {		— as the first char
        u		— for sending score
        1 or 0	— current server
        1 or 0	— current north player
        n:		— no sets player one (base 10)
        n:		— no sets player two (base 10)
        for no sets
            n:		— no games player one (base 10)
            n:		— no games player two (base 10)
        n:		— no points player one (base 10)
        n:		— no points player two (base 10)
        n:		— total points player one (base 10)
        n:		— total points player two (base 10)
        n:		— total number historic points (base 10)
        for total history
            1 or 0	— historic winner
        }		— as the last char

        expecting ‘r’ in response to data received
     */

    public int currentServer = 0;
    public int currentNorth = 0;
    public int currentScoreMode = 0;
    public boolean isInTieBreak = false;
    public Integer matchWinner = null;
    public Pair<Integer, Integer> sets;
    public ArrayList<Pair<Integer, Integer>> previousSets;
    public Pair<Integer, Integer> points;
    public Pair<Integer, Integer> games;
    public Pair<Integer, Integer> totalPoints;
    public int noHistoricPoints;
    public ArrayList<Integer> historicPoints;

    public int dataCode = 0;
    public String dataCommand = "u";

    public String filename = null;

    public ScoreData() {
        this.currentServer = 0;
        this.currentNorth = 0;
        this.currentScoreMode = 0;
        this.isInTieBreak = false;
        this.matchWinner = null;
        this.sets = new Pair<Integer, Integer>(0, 0);
        this.previousSets = new ArrayList<Pair<Integer, Integer>>();
        this.points = new Pair<Integer, Integer>(0, 0);
        this.games = new Pair<Integer, Integer>(0, 0);
        this.totalPoints = new Pair<Integer, Integer>(0, 0);
        this.noHistoricPoints = 0;
        this.historicPoints = new ArrayList<Integer>();
    }

    @Override
    public String toString() {
        try {
            // return all the data in this class as a properly formatted data string
            StringBuilder recDataString = new StringBuilder();
            // now write all the data, first comes the command character
            writeChar(this.dataCommand, recDataString);
            // then the data code, this has a colon
            writeStringWithColon(dataCode, recDataString);
            // now the active mode
            writeChar(this.currentScoreMode, recDataString);
            // the match winner
            if (this.matchWinner == null) {
                // no match winner
                writeChar(9, recDataString);
            } else {
                // there is a winner, write it
                writeChar(this.matchWinner, recDataString);
            }
            // are we in a tie
            writeChar(this.isInTieBreak ? 1 : 0, recDataString);
            // the server
            writeChar(this.currentServer, recDataString);
            writeChar(this.currentNorth, recDataString);
            // now the sets
            writeStringWithColon(sets.first, recDataString);
            writeStringWithColon(sets.second, recDataString);
            // now the games for each set player
            for (Pair<Integer, Integer> pair : previousSets) {
                writeStringWithColon(pair.first, recDataString);
                writeStringWithColon(pair.second, recDataString);
            }
            // points
            writeStringWithColon(points.first, recDataString);
            writeStringWithColon(points.second, recDataString);
            // games
            writeStringWithColon(games.first, recDataString);
            writeStringWithColon(games.second, recDataString);
            // total points
            writeStringWithColon(totalPoints.first, recDataString);
            writeStringWithColon(totalPoints.second, recDataString);
            // no historic points
            writeStringWithColon(noHistoricPoints, recDataString);
            // and then all the historic points we have
            int bitCounter = 0;
            int dataPacket = 0;
            for (int i = 0; i < noHistoricPoints; ++i) {
                // add this value to the data packet
                dataPacket |= historicPoints.get(i) << bitCounter;
                // and increment the counter, sending as radix32 number means we can store 10 bits of data (up to 1023 base 10)
                if (++bitCounter >= 10) {
                    // exceeded the size for next time, send this packet
                    if (dataPacket < 32) {
                        // this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
                        // this is true for hex - who knows how a radix32 number is printed - but whatever nice that we get 10 values
                        recDataString.append('0');
                    }
                    recDataString.append(Integer.toString(dataPacket, 32));
                    // and reset the counter and data
                    bitCounter = 0;
                    dataPacket = 0;
                }
            }
            if (bitCounter > 0) {
                // there was data we failed to send, only partially filled - send this anyway
                if (dataPacket < 32) {
                    // this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
                    // this is true for hex - who knows how a radix64 number is printed - but whatever nice that we get 10 values
                    recDataString.append('0');
                }
                recDataString.append(Integer.toString(dataPacket, 32));
            }
            // and return the results
            return recDataString.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public ScoreData(StringBuilder recDataString) {
        if (null == recDataString) {
            // there is no data here, fine just leave all as default
        }
        else {
            // remove the first char, which should be the command
            dataCommand = extractChars(1, recDataString);
            if (dataCommand.equals("u")) {
                // get the code that we need to respond with
                dataCode = extractValueToColon(recDataString);
                // get the active mode
                currentScoreMode = Integer.parseInt(extractChars(1, recDataString));
                int matchWinnerData = Integer.parseInt(extractChars(1, recDataString));
                if (matchWinnerData <= 1) {
                    // there is a winner, 0 or 1 - set this
                    matchWinner = new Integer(matchWinnerData);
                }
                isInTieBreak = Integer.parseInt(extractChars(1, recDataString)) == 1;
                // get the current server
                currentServer = Integer.parseInt(extractChars(1, recDataString));
                currentNorth = Integer.parseInt(extractChars(1, recDataString));
                sets = new Pair<Integer, Integer>(extractValueToColon(recDataString), extractValueToColon(recDataString));
                // do the games for each set played
                int totalSets = sets.first + sets.second;
                previousSets = new ArrayList<Pair<Integer, Integer>>(totalSets);
                for (int i = 0; i < totalSets; ++i) {
                    previousSets.add(new Pair<Integer, Integer>(extractValueToColon(recDataString), extractValueToColon(recDataString)));
                }
                points = new Pair<Integer, Integer>(extractValueToColon(recDataString), extractValueToColon(recDataString));
                games = new Pair<Integer, Integer>(extractValueToColon(recDataString), extractValueToColon(recDataString));
                totalPoints = new Pair<Integer, Integer>(extractValueToColon(recDataString), extractValueToColon(recDataString));
                // now do all the historic points
                noHistoricPoints = extractValueToColon(recDataString);
                historicPoints = new ArrayList<Integer>(noHistoricPoints);
                int dataCounter = 0;
                while (dataCounter < noHistoricPoints) {
                    // while there are points to get, get them
                    int dataReceived = extractHistoryValue(recDataString);
                    // this char contains somewhere between one and eight values all bit-shifted, extract them now
                    int bitCounter = 0;
                    while (bitCounter < 10 && dataCounter < noHistoricPoints) {
                        int bitValue = 1 & (dataReceived >> bitCounter++);
                        // add this to the list of value received
                        historicPoints.add(bitValue);
                        ++dataCounter;
                    }
                }
            }
        }
    }

    public String getPointsAsString(int points) {
        switch (currentScoreMode) {
            case 1:
            case 2:
                // this is ITF scoring mode
                if (isInTieBreak) {
                    //just fall through to show the number of points
                } else {
                    return getItfScore(points);
                }
            default:
                // just numbers, fall through to the default
                return Integer.toString(points);
        }
    }

    public static String getItfScore(int value) {
        switch (value) {
            case 0:
                return "00";
            case 1:
                return "15";
            case 2:
                return "30";
            case 3:
                return "40";
            case 4:
                return "ad";
            default:
                return Integer.toString(value);
        }
    }

    private int extractValueToColon(StringBuilder recDataString) {
        int value = 0;
        int colonIndex = recDataString.indexOf(":");
        if (colonIndex == -1) {
            throw new StringIndexOutOfBoundsException();
        }
        // extract this data as a string
        String extracted = extractChars(colonIndex, recDataString);
        // and the colon
        recDataString.delete(0, 1);
        // return the data as an integer
        return Integer.parseInt(extracted);
    }

    private int extractHistoryValue(StringBuilder recDataString) {
        // get the string as a double char value
        String hexString = extractChars(2, recDataString);
        return Integer.parseInt(hexString, 32);
    }

    private String extractChars(int charsLength, StringBuilder recDataString) {
        String extracted = "";
        if (recDataString.length() >= charsLength) {
            extracted = recDataString.substring(0, charsLength);
        }
        else {
            throw new StringIndexOutOfBoundsException();
        }
        recDataString.delete(0, charsLength);
        return extracted;
    }

    private void writeChar(String data, StringBuilder recDataString) {
        if (data.length() != 1) {
            // oops
            throw new StringIndexOutOfBoundsException();
        }
        else {
            recDataString.append(data);
        }
    }

    private void writeChar(int data, StringBuilder recDataString) {
        if (data > 9 || data < 0) {
            // oops
            throw new StringIndexOutOfBoundsException();
        }
        else {
            recDataString.append(Integer.toString(data));
        }
    }

    private void writeStringWithColon(int data, StringBuilder recDataString) {
        recDataString.append(Integer.toString(data));
        recDataString.append(':');
    }

    private void writeStringWithColon(String data, StringBuilder recDataString) {
        recDataString.append(data);
        recDataString.append(':');
    }
}
