package uk.co.darkerwaters.scorepal.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.ScoreHistory;
import uk.co.darkerwaters.scorepal.ui.UiHelper;

public class MatchMomentumGraph extends View {

    private static final float K_AXIS_WIDTH_DP = 3f;
    private static final float K_LINE_WIDTH_DP = 3f;
    private static final float K_IMPR_WIDTH_DP = 4f;
    private static final float K_DOT_WIDTH_DP = 8f;
    private static final float K_POINT_SEP_DP = 16f;
    private static final float K_HIT_RADIUS_DP = 8f;

    private static final int K_DEFAULT_MAX_POINTS = 20;
    private static final float K_ANIMATION_STEPS = 20;
    private static final long K_ANIMATION_STEP_INTERVAL = 5L;

    private final Paint backgroundPaint;
    private final Paint axisPaint;
    private final Paint linePaint;
    private final Paint dotPaint;
    private final Paint textPaint;
    private float pointSep;
    private final float dotRadius;
    private final float hitRadius;

    private final List<ScoreHistory.HistoryValue> pointHistory = new ArrayList<>();
    private int[] straightPointsToWin = null;
    private RectF[] touchablePoints = new RectF[0];

    // calculate the level that 0 points is at in the data and draw the axis there
    private int maxValue = 0;
    private int minValue = 0;

    private int maxPoints = 0;
    private int noPoints = 0;
    private int startPoint = 0;
    private int startSway = 0;

    private float viewZoom = 1f;
    private int viewZoomOffset = 0;

    private RectF drawingRect = null;
    private final Rect textBounds = new Rect();
    private final PointF drawPoint = new PointF();
    private final PointF previousPoint = new PointF();
    private int highlightedIndex = -1;
    private MatchSetup.Team teamOnVerticalAxis = MatchSetup.Team.T_ONE;

    private boolean isShowEntireMatchData = false;
    private boolean isShowDots = false;
    private final Rect invalidationRect = new Rect();
    private final Handler invalidationHandler;

    private class HighlightPoint {
        String data;
        float x;
        float y;
        int color;
        int sway;
    }
    private final HighlightPoint highlightPoint = new HighlightPoint();

    public MatchMomentumGraph(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.invalidationHandler = new Handler();

        this.backgroundPaint = new Paint();
        this.backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.backgroundPaint.setColor(ContextCompat.getColor(context, R.color.primaryColor));
        this.backgroundPaint.setAntiAlias(true);

        this.axisPaint = new Paint();
        this.axisPaint.setStyle(Paint.Style.STROKE);
        this.axisPaint.setColor(ContextCompat.getColor(context, R.color.primaryDarkColor));
        this.axisPaint.setAntiAlias(true);
        this.axisPaint.setStrokeWidth(UiHelper.convertDpToPixel(K_AXIS_WIDTH_DP, context));

        this.linePaint = new Paint();
        this.linePaint.setStyle(Paint.Style.STROKE);
        this.linePaint.setAntiAlias(true);
        this.linePaint.setStrokeWidth(UiHelper.convertDpToPixel(K_LINE_WIDTH_DP, context));

        this.textPaint = new Paint();
        this.textPaint.setStyle(Paint.Style.FILL);
        this.textPaint.setColor(ContextCompat.getColor(context, R.color.secondaryColor));
        this.textPaint.setAntiAlias(true);
        this.textPaint.setTextAlign(Paint.Align.LEFT);
        this.textPaint.setTextSize(UiHelper.convertDpToPixel(K_POINT_SEP_DP, context));

        this.dotPaint = new Paint();
        this.dotPaint.setStyle(Paint.Style.FILL);
        this.dotPaint.setAntiAlias(true);

        this.dotRadius = UiHelper.convertDpToPixel(K_DOT_WIDTH_DP, context) * 0.5f;
        this.pointSep = UiHelper.convertDpToPixel(K_POINT_SEP_DP, context);
        this.hitRadius = UiHelper.convertDpToPixel(K_HIT_RADIUS_DP, context);

        // create a fake point history to work with
        ScoreHistory.HistoryValue[] fakeHistory = new ScoreHistory.HistoryValue[30];
        int index = 0;
        for (; index < 5; ++index) {
            fakeHistory[index] = new ScoreHistory.HistoryValue(MatchSetup.Team.T_ONE, 0, 0);
        }
        for (; index < 12; ++index) {
            fakeHistory[index] = new ScoreHistory.HistoryValue(MatchSetup.Team.T_TWO, 0, 0);
        }
        Random random = new Random();
        for (; index < fakeHistory.length; ++index) {
            fakeHistory[index] = new ScoreHistory.HistoryValue(random.nextBoolean() ? MatchSetup.Team.T_ONE : MatchSetup.Team.T_TWO, 0, 0);
        }
        setMatchData(fakeHistory, new int[] {1});
    }

    public void setMatchData(Match match) {
        if (null != match) {
            setMatchData(match.getWinnersHistory(), match.getSetup().getStraightPointsToWin());
        }
    }

    public void setIsShowEntireMatchData(boolean isShowEntireMatchData) {
        this.isShowEntireMatchData = isShowEntireMatchData;
    }

    public boolean offsetZoomPosition(float factor) {
        return setZoomOffset(this.viewZoomOffset + (int)(factor * getPointsShowing(this.viewZoom)));
    }

    public int getZoomOffset() {
        return this.viewZoomOffset;
    }

    public boolean setZoomOffset(int newOffset) {
        if (newOffset > 0) {
            // only offsetting down as by default show the end
            return false;
        }
        else if (newOffset != this.viewZoomOffset) {
            // this is a change
            int noPoints;
            synchronized (this.pointHistory) {
                noPoints = calculateNoPoints(this.pointHistory, this.straightPointsToWin);
            }
            // cap the offset to never show less than the default points
            newOffset = Math.max(newOffset, -(noPoints - getPointsShowing(this.viewZoom) - 1));
            // set the offset
            this.viewZoomOffset = newOffset;
            // and update the view
            recalculateDataOnViewChanges();
            // and redraw the data accordingly
            //refreshGraphDataView();
            // and draw this immediately
            invalidate();
        }
        // no change is also fine
        return true;
    }

    private int getPointsShowing(float zoomValue) {
        int noPoints;
        synchronized (this.pointHistory) {
            noPoints = calculateNoPoints(this.pointHistory, this.straightPointsToWin);
        }
        return (int)(noPoints * (1f / zoomValue));
    }

    public float getViewZoom() {
        return this.viewZoom;
    }

    public void setIsShowDots(boolean value) { this.isShowDots = value; }

    public boolean setViewZoom(float newZoom) {
        if (newZoom < 0f) {
            // too small
            return false;
        }
        if (this.viewZoom != newZoom) {
            // this is a change, how many points will this show on the view if we do it
            if (getPointsShowing(newZoom) < K_DEFAULT_MAX_POINTS) {
                // this is zooming too much, don't accept this
                return false;
            }
            this.viewZoom = newZoom;
            // and update the view
            recalculateDataOnViewChanges();
            // and redraw the data accordingly
            //refreshGraphDataView();
            // and draw this immediately
            invalidate();
        }
        // no change is fine too
        return true;
    }

    public void setMatchData(ScoreHistory.HistoryValue[] winnersHistory, int[] straightPointsToWin) {
        // calculate the max points that can fit so we know where to start

        // set the number of points
        this.noPoints = calculateNoPoints(winnersHistory, straightPointsToWin);
        int totalPoints = this.noPoints;
        if (null != drawingRect && 0 != this.pointSep) {
            // calculate the max points that can fit
            if (this.isShowEntireMatchData) {
                this.maxPoints = (int)(this.noPoints * (1f / this.viewZoom));
                this.pointSep = drawingRect.width() / Math.max(K_DEFAULT_MAX_POINTS, this.maxPoints);
            }
            else {
                this.pointSep = UiHelper.convertDpToPixel(K_POINT_SEP_DP, getContext());
                this.maxPoints = (int) (drawingRect.width() / this.pointSep);
            }
        } else {
            // just use a standard number
            this.maxPoints = K_DEFAULT_MAX_POINTS;
        }
        this.startPoint = -1;
        if (this.noPoints > this.maxPoints) {
            // there are too many points to draw, start later in the list
            this.startPoint = this.noPoints - this.maxPoints - 1;
            this.noPoints = this.maxPoints;
            if (this.viewZoom != 1f) {
                // we are zooming, apply the offset
                this.startPoint += this.viewZoomOffset;
            }
            if (this.startPoint < -1) {
                // ggargh - panned too far, cap this to prevent errors in the drawing
                this.startPoint = -1;
            }
        }
        // reset our starting data points
        this.startSway = 0;
        this.maxValue = 0;
        this.minValue = 0;
        // transfer the data to our member list to draw later
        synchronized (this.pointHistory) {
            this.pointHistory.clear();
            this.straightPointsToWin = straightPointsToWin;
            int pointSway = 0;
            // create the empty array of points
            this.touchablePoints = new RectF[totalPoints];
            // add the new data while calculating the data ranges from it
            int pointIndex = 0;
            for (int i = 0; i < winnersHistory.length; ++i) {
                // it is usually one point per point, but there might be more for different levels
                int pointsToAdd = this.straightPointsToWin[winnersHistory[i].level];
                for (int j = 0; j < pointsToAdd; ++j) {
                    // add the value to our list for each point the level represents, level being 0 (repeated) use the state only on the lat
                    ScoreHistory.HistoryValue historyValue = new ScoreHistory.HistoryValue(winnersHistory[i].team, 0, 0);
                    if (j == pointsToAdd - 1) {
                        // this is the last item of our constructed points, use the state and top level etc for this actualpoint
                        historyValue.state = winnersHistory[i].state;
                        // and copy of the top level and the string
                        historyValue.topLevelChanged = winnersHistory[i].topLevelChanged;
                        historyValue.scoreString = winnersHistory[i].scoreString;
                    }
                    // and add the copy to our list
                    this.pointHistory.add(historyValue);
                    // and create the point for this item
                    this.touchablePoints[pointIndex] = new RectF(0, 0, 0, 0);
                    // and calculate the thresholds
                    if (winnersHistory[i].team == this.teamOnVerticalAxis) {
                        // if team zero ) then point to the player one
                        ++pointSway;
                    } else {
                        // point to player two
                        --pointSway;
                    }
                    if (pointIndex >= startPoint) {
                        // is this the actual start?
                        if (pointIndex == startPoint) {
                            // remember the sway at the start point
                            startSway = pointSway;
                        }
                        // we are past the start point, start measuring the min and max values
                        // we are going to show
                        minValue = Math.min(pointSway, minValue);
                        maxValue = Math.max(pointSway, maxValue);
                    }
                    // move on our global index
                    ++pointIndex;
                }
            }
        }
        // and draw this immediately
        invalidate();
    }

    private int calculateNoPoints(List<ScoreHistory.HistoryValue> winnersHistory, int[] straightPointsToWin) {
        int noPoints = 0;
        for (ScoreHistory.HistoryValue value : winnersHistory) {
            // it is usually one point per point, but there might be more for different levels
            noPoints += straightPointsToWin[value.level];
        }
        return noPoints;
    }

    private int calculateNoPoints(ScoreHistory.HistoryValue[] winnersHistory, int[] straightPointsToWin) {
        int noPoints = 0;
        for (int i = 0; i < winnersHistory.length; ++i) {
            // it is usually one point per point, but there might be more for different levels
            noPoints += straightPointsToWin[winnersHistory[i].level];
        }
        return noPoints;
    }

    public void setGraphFocus(MatchSetup.Team team) {
        if (team != this.teamOnVerticalAxis) {
            // change the value
            this.teamOnVerticalAxis = team;
            synchronized (this.pointHistory) {
                // we also need to invert all the data
                this.minValue *= -1;
                this.maxValue *= -1;
                this.startSway *= -1;
                for (RectF touchable : this.touchablePoints) {
                    // invert each rect
                    touchable.set(
                            touchable.left,
                            touchable.top * -1,
                            touchable.right,
                            touchable.bottom * -1);
                }
            }
            refreshGraphDataView();
        }
    }

    private void refreshGraphDataView() {
        if (null != this.drawingRect) {
            // we are drawing this, we want to invalidate gradually
            this.invalidationRect.set(
                    (int)this.drawingRect.left,
                    (int)this.drawingRect.top,
                    (int)this.drawingRect.left,
                    (int)this.drawingRect.bottom);
            // and change the view this amount
            invalidate(this.invalidationRect);
        }
        else {
            // just do it all
            invalidate();
        }
    }

    private void recalculateDataOnViewChanges() {
        synchronized (this.pointHistory) {
            // just call set again with the same data
            setMatchData(this.pointHistory.toArray(new ScoreHistory.HistoryValue[0]), this.straightPointsToWin);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // calculate the rect we can draw in
        boolean isFirstDraw = false;
        if (null == this.drawingRect) {
            this.drawingRect = new RectF();
            isFirstDraw = true;
        }
        this.drawingRect.set(
                getPaddingStart(),
                getPaddingTop(),
                getWidth() - getPaddingEnd(),
                getHeight() - getPaddingBottom());

        // draw the graph
        if (invalidationRect.height() > 0) {
            // ** )O: I wanted to just increase the width of the rect to draw over and
            // and use the effect to do the rest but this doesn't seem to work, so instead
            // I am limiting the clipping to gradually reveal the data instead
            canvas.clipRect(0, 0, this.invalidationRect.right, getHeight());
        }

        // clear the canvas
        this.backgroundPaint.setAlpha(0);
        canvas.drawRect(this.drawingRect, this.backgroundPaint);
        Context context = getContext();

        // protect our use of the data variables and array
        synchronized (this.pointHistory) {
            // remember the colors based on who is the focused team
            int positiveColor = ContextCompat.getColor(context, this.teamOnVerticalAxis == MatchSetup.Team.T_ONE ? R.color.teamOneColor : R.color.teamTwoColor);
            int negativeColor = ContextCompat.getColor(context, this.teamOnVerticalAxis == MatchSetup.Team.T_ONE ? R.color.teamTwoColor : R.color.teamOneColor);

            // get the range and values to use
            int pointRange = this.maxValue - this.minValue;
            float pointHeight = this.drawingRect.height() / pointRange;
            float zeroY = this.drawingRect.bottom - ((0 - this.minValue) * pointHeight);

            // text the widths of the labels to find the biggest
            this.textPaint.getTextBounds("=", 0, 1, this.textBounds);
            float equalWidth = this.textBounds.width();
            float equalOffset = this.textBounds.height() * 0.7f;

            String minString = "+" + Math.abs(this.minValue);
            this.textPaint.getTextBounds(minString, 0, minString.length(), this.textBounds);
            float textWidth = Math.max(textBounds.width(), equalWidth * 2);

            String maxString = "+" + Math.abs(this.maxValue);
            this.textPaint.getTextBounds(maxString, 0, maxString.length(), this.textBounds);
            textWidth = Math.max(this.textBounds.width(), textWidth);

            // label the Y axis for the top
            this.textPaint.setColor(positiveColor);
            canvas.drawText(maxString,
                    this.drawingRect.left,
                    this.drawingRect.top + this.textBounds.height() * 0.7f,
                    this.textPaint);
            boolean isDrawEquals =
                    Math.abs(zeroY - this.drawingRect.top) > this.textBounds.height() * 2f &&
                            Math.abs(zeroY - this.drawingRect.bottom) > this.textBounds.height() * 2f;
            if (isDrawEquals) {
                // far enough from the top and bottom to draw equals sign
                canvas.drawText("=",
                        this.drawingRect.left + equalWidth * 0.5f,
                        zeroY + equalOffset,
                        this.textPaint);
            }

            // label the Y axis for the bottom
            this.textPaint.setColor(negativeColor);
            canvas.drawText(minString,
                    this.drawingRect.left,
                    this.drawingRect.bottom,
                    this.textPaint);
            /*
            if (isDrawEquals) {
                // far enough from the top and bottom to draw equals sign
                canvas.drawText("=",
                        this.drawingRect.left + equalWidth * 0.5f,
                        zeroY - equalOffset,
                        this.textPaint);
            }*/

            // move the rect over
            float offset = textWidth + this.axisPaint.getStrokeWidth() * 1.5f;
            this.drawingRect.set(
                    this.drawingRect.left + offset,
                    this.drawingRect.top,
                    this.drawingRect.right - offset,
                    this.drawingRect.bottom);

            if (isFirstDraw) {
                // the first draw needs to reset the data now the rect has been calculated
                // reset the match data to calculate our data on the bounds properly
                recalculateDataOnViewChanges();
            }

            // draw in the axis - vertical
            canvas.drawLine(this.drawingRect.left, this.drawingRect.top, this.drawingRect.left, this.drawingRect.bottom, this.axisPaint);
            // horizontal - on the zero line
            canvas.drawLine(this.drawingRect.left, zeroY, this.drawingRect.right, zeroY, this.axisPaint);

            // draw in the data
            int color;
            int pointSway = this.startSway;
            this.previousPoint.set(this.drawingRect.left, this.drawingRect.bottom - ((pointSway - this.minValue) * pointHeight));
            this.highlightPoint.data = null;
            int totalPoints = this.pointHistory.size();
            for (int i = this.startPoint + 1; i < this.startPoint + 1 + this.noPoints && i < totalPoints; ++i) {
                // get the sway up or down that this point causes
                ScoreHistory.HistoryValue point = this.pointHistory.get(i);
                if (point.team == this.teamOnVerticalAxis) {
                    color = positiveColor;
                    ++pointSway;
                } else {
                    color = negativeColor;
                    --pointSway;
                }
                // coloring team one or team two
                this.dotPaint.setColor(color);
                this.linePaint.setColor(color);
                // calculate where to draw this point
                this.drawPoint.x = this.drawingRect.left + ((i - this.startPoint) * this.pointSep);
                this.drawPoint.y = this.drawingRect.bottom - ((pointSway - this.minValue) * pointHeight);
                // highlight the important points over the unimportant ones
                switch (point.topLevelChanged) {
                    case 0:
                        // fine
                        break;
                    case 1:
                        // draw in a line
                        canvas.drawLine(this.drawPoint.x,
                                this.drawPoint.y,
                                this.drawPoint.x,
                                zeroY,
                                linePaint);
                        break;
                    case 2:
                        // draw in a thick line
                        this.linePaint.setStrokeWidth(UiHelper.convertDpToPixel(K_IMPR_WIDTH_DP, context));
                        canvas.drawLine(this.drawPoint.x,
                                this.drawPoint.y,
                                this.drawPoint.x,
                                zeroY,
                                linePaint);
                        // put it back
                        this.linePaint.setStrokeWidth(UiHelper.convertDpToPixel(K_LINE_WIDTH_DP, context));
                        break;
                }
                // draw the line from the previous to this
                canvas.drawLine(this.previousPoint.x, this.previousPoint.y, this.drawPoint.x, this.drawPoint.y, this.linePaint);
                // draw in the circle on top of the line
                if (isShowDots) {
                    canvas.drawCircle(this.drawPoint.x, this.drawPoint.y, this.dotRadius, this.dotPaint);
                }

                if (this.highlightedIndex == i && null != point.scoreString) {
                    // set all the data to draw this on top of everything else later
                    this.highlightPoint.data = point.scoreString;
                    this.highlightPoint.x = this.drawPoint.x;
                    this.highlightPoint.y = this.drawPoint.y;
                    this.highlightPoint.color = color;
                    this.highlightPoint.sway = pointSway;
                }

                // remember this point to handle the touch - bigger than the dot to allow for fat fingers

                /*float touchDistance = pointSep * 0.5f;
                this.touchablePoints[i].set(
                        this.drawPoint.x - touchDistance,
                        this.drawPoint.y - touchDistance,
                        this.drawPoint.x + touchDistance,
                        this.drawPoint.y + touchDistance);*/
                this.touchablePoints[i].set(
                        this.drawPoint.x - this.hitRadius,
                        this.drawPoint.y - this.hitRadius,
                        this.drawPoint.x + this.hitRadius,
                        this.drawPoint.y + this.hitRadius);

                // and remember the previous
                this.previousPoint.set(this.drawPoint);
            }

            if (null != this.highlightPoint.data) {
                // this is highlighted, show the score
                String[] lines = this.highlightPoint.data.split("\n");
                if (lines.length > 0) {
                    this.textPaint.getTextBounds(lines[0], 0, lines[0].length(), this.textBounds);
                    float lineHeight = this.textBounds.height() * 1.5f;
                    float yStart;
                    if (this.highlightPoint.y <= this.drawingRect.centerY()) {
                        yStart = this.highlightPoint.y + lineHeight;
                    } else {
                        yStart = this.highlightPoint.y - lineHeight * lines.length;
                        yStart += lineHeight * 0.6f;
                    }
                    // clear the background of this
                    this.backgroundPaint.setAlpha(200);
                    canvas.drawRect(this.highlightPoint.x,
                            yStart - lineHeight,
                            this.highlightPoint.x + this.textBounds.width() * 1.1f,
                            yStart + lines.length * lineHeight * 0.7f,
                            backgroundPaint);

                    // show the highlighted circle bigger
                    this.dotPaint.setColor(this.highlightPoint.color);
                    canvas.drawCircle(this.highlightPoint.x, this.highlightPoint.y, dotRadius * 3f, this.dotPaint);
                    // and the text lines
                    this.textPaint.setColor(context.getColor(R.color.teamOneColor));
                    for (String line : lines) {
                        canvas.drawText(line, this.highlightPoint.x, yStart, this.textPaint);
                        this.textPaint.setColor(context.getColor(R.color.teamTwoColor));
                        yStart += lineHeight;
                    }
                }
            }

            // are we partially invalidating this view?
            if (this.invalidationRect.height() > 0) {
                if (this.invalidationRect.width() < this.drawingRect.width()) {
                    // are are invalidating but we haven't invalidated everything, increase the width
                    int sizeIncrease = (int) (this.drawingRect.width() / K_ANIMATION_STEPS);
                    // if for some reason this is zero, this will loop infinately
                    if (sizeIncrease <= 0) {
                        sizeIncrease = (int) dotRadius * 5;
                    }
                    // draw a slice here to show?
                    canvas.drawLine(this.invalidationRect.right, this.drawingRect.top, this.invalidationRect.right, this.drawingRect.bottom, this.axisPaint);

                    // and set the new rect
                    this.invalidationRect.set(
                            this.invalidationRect.left,
                            this.invalidationRect.top,
                            (this.invalidationRect.right + sizeIncrease),
                            this.invalidationRect.bottom);
                    // and change the view this amount
                    invalidationHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            invalidate(invalidationRect);
                        }
                    }, K_ANIMATION_STEP_INTERVAL);
                } else {
                    // we are done, reset the rect so we just draw normally the next time
                    this.invalidationRect.set(0, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get masked (not specific to a pointer) action
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // process this press
                onViewPressed(event);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                // can ignore this
                break;

        }
        // return our result
        return true;
    }

    private boolean onViewPressed(MotionEvent e) {
        MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
        int previousHighlight = this.highlightedIndex;
        this.highlightedIndex = -1;
        for (int i = 0; i < e.getPointerCount(); ++i) {
            e.getPointerCoords(i, pointerCoords);
            // check this for a point to have been hit
            RectF[] pointsToTest;
            int startPoint;
            synchronized (this.pointHistory) {
                pointsToTest = Arrays.copyOf(this.touchablePoints, this.touchablePoints.length);
                startPoint = this.startPoint;
            }
            // test each one for a hit
            for (int j = startPoint + 1; j < pointsToTest.length; ++j) {
                RectF testPoint = pointsToTest[j];
                if (null != testPoint && testPoint.contains(pointerCoords.x, pointerCoords.y)) {
                    // this is hit, show this data at the point
                    this.highlightedIndex = j;
                    // break from the inner loop - stop checking for a hit
                    break;
                }
            }
        }
        if (previousHighlight != this.highlightedIndex) {
            // this is a change in the highlight state, redraw the view
            invalidate();
        }
        // return that this was handled OK and we don't want any more information
        return false;
    }
}
