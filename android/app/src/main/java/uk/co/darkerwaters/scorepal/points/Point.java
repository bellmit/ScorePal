package uk.co.darkerwaters.scorepal.points;

import android.content.Context;

public interface Point {

    String K_SPEAKING_SPACE = " ";
    String K_SPEAKING_PAUSE = ". ";
    String K_SPEAKING_PAUSE_SLIGHT = ", ";
    String K_SPEAKING_PAUSE_LONG = "… ";//"... ";//""… ";

    int val();

    String displayString(Context context);
    String speakString(Context context);
    String speakString(Context context, int number);
    String speakAllString(Context context);
}
