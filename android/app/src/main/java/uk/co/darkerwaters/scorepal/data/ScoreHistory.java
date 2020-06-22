package uk.co.darkerwaters.scorepal.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import uk.co.darkerwaters.scorepal.application.Log;

public class ScoreHistory {

    public enum Importance {
        LOW,
        MEDIUM,
        HIGH;
        public static Importance fromLevel(int level) {
            return level <= 0 ? LOW : level == 1 ? MEDIUM : HIGH;
        }
    }

    public static class HistoryValue {

        public final MatchSetup.Team team;
        public final int level;
        public int state;
        public int topLevelChanged;
        public String scoreString;

        public HistoryValue(MatchSetup.Team team, int level, int state) {
            this.team = team;
            this.level = level;
            this.state = state;
            this.topLevelChanged = level;
            this.scoreString = "";
        }

        public HistoryValue copy() {
            HistoryValue copy = new HistoryValue(this.team, this.level, this.state);
            copy.topLevelChanged = topLevelChanged;
            copy.scoreString = scoreString;
            return copy;
        }

        public void recordTopLevel(int level) {
            this.topLevelChanged = Math.max(this.topLevelChanged, level);
        }
    }


    private final Stack<HistoryValue> levelHistory;

    ScoreHistory() {
        // we will also store the entire history played
        this.levelHistory = new Stack<>();
    }

    public void clear() {
        this.levelHistory.clear();
    }

    public boolean isEmpty() {
        return this.levelHistory.isEmpty();
    }

    public int getSize() {
        return this.levelHistory.size();
    }

    public HistoryValue get(int index) {
        return this.levelHistory.get(index);
    }

    public HistoryValue pop() {
        return this.levelHistory.pop();
    }

    public void push(MatchSetup.Team team, int level, int state) {
        this.levelHistory.push(new HistoryValue(team, level, state));
    }

    public void describe(int newState, String description) {
        if (!this.levelHistory.empty()) {
            // set the last item description
            HistoryValue historyValue = this.levelHistory.peek();
            if (newState >= 0) {
                // use this new state
                historyValue.state = newState;
            }
            // and the description
            historyValue.scoreString = description;
        }
    }

    public void measureLevel(int level) {
        if (!this.levelHistory.empty()) {
            // keep the highest level changed in the last history item
            this.levelHistory.peek().recordTopLevel(level);
        }
    }

    public List<Integer> getHistoryAsPointHistory(int[] levelStraightPoints) {
        // first we need to convert the history to a more basic point only history
        List<Integer> pointHistory = new ArrayList<>();
        for (HistoryValue value : levelHistory) {
            if (value.level < levelStraightPoints.length) {
                // there is a value to represent how many points constitite a change at this level
                for (int i = 0; i < levelStraightPoints[value.level]; ++i) {
                    // for each number that represents a straight win, add the team that won that point
                    pointHistory.add(value.team.index);
                }
            }
            else {
                // just one-to-one
                pointHistory.add(value.team.index);
                Log.error("the levelStraightPoints should have the points at each level that constitute a win");
            }
        }
        // and return
        return pointHistory;
    }

    public String getPointHistoryAsString(int[] levelStraightPoints) {
        StringBuilder recDataString = new StringBuilder();
        List<Integer> pointHistory = getHistoryAsPointHistory(levelStraightPoints);
        int noHistoricPoints = pointHistory.size();
        // first write the number of historic points we are going to store
        recDataString.append(noHistoricPoints);
        recDataString.append(':');
        // and then all the historic points we have
        int bitCounter = 0;
        int dataPacket = 0;
        for (int i = 0; i < noHistoricPoints; ++i) {
            // get the team as a binary value
            int binaryValue = pointHistory.get(i);
            // add this value to the data packet
            dataPacket |= binaryValue << bitCounter;
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
        return recDataString.toString();
    }

    public void restorePointHistoryFromString(StringBuilder pointHistoryString) {
        // the value before the colon is the number of subsequent values
        int noHistoricPoints = extractValueToColon(pointHistoryString);
        int dataCounter = 0;
        while (dataCounter < noHistoricPoints) {
            // while there are points to get, get them
            int dataReceived = extractHistoryValue(pointHistoryString);
            // this char contains somewhere between one and eight values all bit-shifted, extract them now
            int bitCounter = 0;
            while (bitCounter < 10 && dataCounter < noHistoricPoints) {
                int bitValue = 1 & (dataReceived >> bitCounter++);
                // add this to the list of value received and inc the counter of data
                this.levelHistory.push(
                        // the value is a point for team one if zero and team two if 1
                        new HistoryValue(bitValue == 0 ? MatchSetup.Team.T_ONE : MatchSetup.Team.T_TWO,
                                // this only does points and we don't store the state like this
                                0, 0));
                // increment the counter
                ++dataCounter;
            }
        }
    }

    private int extractHistoryValue(StringBuilder recDataString) {
        // get the string as a double char value
        String hexString = extractChars(2, recDataString);
        return Integer.parseInt(hexString, 32);
    }

    private int extractValueToColon(StringBuilder recDataString) {
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

    private String extractChars(int charsLength, StringBuilder recDataString) {
        String extracted;
        if (recDataString.length() >= charsLength) {
            extracted = recDataString.substring(0, charsLength);
        }
        else {
            throw new StringIndexOutOfBoundsException();
        }
        recDataString.delete(0, charsLength);
        return extracted;
    }
}
