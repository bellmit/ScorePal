package uk.co.darkerwaters.scorepal.controllers;

import android.util.Pair;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class KeyController extends Controller {

    private static final long K_CLICKPERIODGAP = 50;
    private static final long K_CLICKPERIODSILENCE = 400;

    private final Object clickListLock;
    private ArrayList<Long>[] clickTimes;

    private final Thread processingThread;
    private volatile boolean isProcessClickTimes = true;
    private final Object waitObject = new Object();

    private long lastLongKeyDownTime = 0L;

    public KeyController(final ControllerButton[] buttons) {
        super(buttons);
        // set the click times list from this
        this.clickListLock = new Object();
        setClickTimes(buttons);
        // create the thread to do the work
        this.processingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                processClickTimes();
            }
        });
    }

    @Override
    public void setButtons(ControllerButton[] newButtons) {
        // let the base
        super.setButtons(newButtons);
        if (null != this.clickTimes) {
            setClickTimes(newButtons);
        }
    }

    private void setClickTimes(ControllerButton[] buttons) {
        // setup our synchronised list of click times
        synchronized (this.clickListLock) {
            // and clear the click times
            this.clickTimes = new ArrayList[buttons.length];
            for (int i = 0; i < this.clickTimes.length; ++i) {
                this.clickTimes[i] = new ArrayList<>();
            }
        }
    }

    @Override
    public boolean start() {
        // and start the thread processing data
        this.isProcessClickTimes = true;
        this.processingThread.start();
        // return that we are working
        return isProcessClickTimes;
    }

    @Override
    public boolean close() {
        this.isProcessClickTimes = false;
        synchronized (this.waitObject) {
            this.waitObject.notifyAll();
        }
        // return that we are now not working
        return false == this.isProcessClickTimes;
    }

    public boolean processKeyEvent(int keyCode, KeyEvent keyEvent) {
        // find the button for this
        boolean isProcessed = false;
        ControllerButton[] buttons = getButtons();
        for (int i = 0; i < buttons.length; ++i) {
            if (buttons[i].containsCode(keyCode)) {
                // this is the one
                isProcessed = processButtonPress(buttons[i], clickTimes[i], keyEvent);
                break;
            }
        }
        return isProcessed;
    }

    public boolean processLongButtonKeyEvent(int keyCode, long downTime) {
        // find the button for this
        boolean isProcessed = false;
        ControllerButton[] buttons = getButtons();
        for (ControllerButton button : buttons) {
            if (button.containsCode(keyCode)) {
                // this is the one
                isProcessed = processLongButtonPress(button, downTime);
                break;
            }
        }
        return isProcessed;
    }

    protected boolean processButtonPress(ControllerButton button, ArrayList<Long> clickTimes, KeyEvent keyEvent) {
        // process each of the actions
        if (keyEvent.isLongPress()) {
            // inform listeners of this press for some visual feedback
            processLongButtonPress(button, keyEvent.getDownTime());
        }
        else if (keyEvent.getRepeatCount() == 0 && keyEvent.getAction() == KeyEvent.ACTION_UP) {
            if (keyEvent.getDownTime() == lastLongKeyDownTime) {
                // this is the up of the long click, ignore
                this.lastLongKeyDownTime = 0L;
                // but inform listeners this is cleared now
                informControllerListeners(null);
            }
            else {
                // we just have to add the click to the list and let the thread handle it later
                synchronized (clickTimes) {
                    clickTimes.add(System.currentTimeMillis());
                }
                // inform listeners of this press for some visual feedback
                informControllerListeners(new KeyPress[] {KeyPress.Short});
            }
        }
        // we used this, don't pass it on to other people to use
        return true;
    }

    protected boolean processLongButtonPress(ControllerButton button, long downTime) {
        // inform the listeners of this long click
        informControllerListeners(new KeyPress[] {KeyPress.Long});
        // if it is a long press we can do the work right here
        for (Pair<ControllerPattern, ControllerAction> action : button.actions) {
            if (action.first == ControllerPattern.LongClick) {
                // this is a long click action - this had been done here and now
                informControllerListeners(button.codes, action.first, action.second);
            }
        }
        // remember the last down time to ignore the release
        this.lastLongKeyDownTime = downTime;
        // we used this, don't pass it on to other people to use
        return true;
    }

    private void processClickTimes() {
        while (this.isProcessClickTimes) {
            long currentTime = System.currentTimeMillis();

            // extract lists to work on
            ControllerButton[] safeButtons = getButtons();
            ArrayList<Long>[] safeClickTimes;
            synchronized (this.clickListLock) {
                safeClickTimes = Arrays.copyOf(this.clickTimes, this.clickTimes.length);
            }
            if (safeButtons.length == safeClickTimes.length) {
                // these lists are in sync, process
                for (int i = 0; i < safeButtons.length; ++i) {
                    // count the clicks for this button
                    ArrayList<Long> clickTime = safeClickTimes[i];
                    int clickCount = 0;
                    synchronized (clickTime) {
                        // count the clicks for the time period
                        if (false == clickTime.isEmpty()) {
                            int j = clickTime.size() - 1;
                            long lastClickTime = clickTime.get(j);
                            if (currentTime - lastClickTime > K_CLICKPERIODSILENCE) {
                                // the last one is a while ago, this is a click of that number
                                for (; j >= 0; --j) {
                                    long detectedClick = clickTime.get(j);
                                    if (lastClickTime - detectedClick > K_CLICKPERIODGAP) {
                                        // this is a different click, not pressing two buttons at the
                                        // same time to get a double effect, count this
                                        ++clickCount;
                                    }
                                    // this is the last time now
                                    lastClickTime = detectedClick;
                                }
                                clickCount = clickTime.size();
                                // this is processed now, the last one was long enough ago
                                // clear the list of these used clicks
                                clickTime.clear();
                            }
                        }
                    }
                    if (clickCount > 0) {
                        // inform listeners of this clearance
                        informControllerListeners(null);
                        for (Pair<ControllerPattern, ControllerAction> action : safeButtons[i].actions) {
                            // for each action, is this ok?
                            switch (action.first) {
                                case SingleClick:
                                    if (clickCount == 1) {
                                        informControllerListeners(safeButtons[i].codes, action.first, action.second);
                                    }
                                    break;
                                case DoubleClick:
                                    if (clickCount == 2) {
                                        informControllerListeners(safeButtons[i].codes, action.first, action.second);
                                    }
                                    break;
                                case TripleClick:
                                    if (clickCount == 3) {
                                        informControllerListeners(safeButtons[i].codes, action.first, action.second);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
            synchronized (this.waitObject) {
                try {
                    waitObject.wait(50);
                }
                catch (InterruptedException e) {
                    // whatever
                }
            }
        }
    }
}
