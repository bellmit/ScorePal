// UPLOAD OUR CODE HERE
// https://www.espruino.com/ide/#

// make a HID device to just send media messages to Scorepal to control it
//TODO want to share our battery level, and change pace at which we advertise
// our presence but it doesn't seem to work... just becommed unfindable when we change
var controls = require("ble_hid_controls");
NRF.setServices(undefined, { hid : controls.report });

// ON RELEASE DISABLE UART to prevent user editing
const isEnableUART = true;      // when in production - disable this

const CLICKNONE         = 0;          // duh
const CLICKSINGLE       = 1;          // obvious
const CLICKDOUBLE       = 2;          // obvious
const CLICKTRIPLE       = 3;          // obvious
const CLICKLONG         = -1;         // not so
const CLICKSUPERLONG    = -2;         // not so

const FLASHDELAY = 200;           // the FLASHING on / off interval
const PULSEDELAY = 1000;          // time to flash the LED for
const BTSCANDELAY = 30000;        // just leave on for a bit if not connected (someone will connect to us)
const BTSLEEPDELAY = 600000;      // 600,000 is 10 minutes of inactivity before we sleep
const CLICKPERIOD = 400;          // the time in which the next click has to happen
const LONGCLICKPERIOD = 1000;     // the time the button can be held for a long click
const SUPERLONGCLICKPERIOD = 8000;// the time the button can be held for a super-long click to turn off
//const ADVERTISINGDELAY = 8000;    // the delay between advertising our state
const ADVERTISINGDELAYSCCAN = 200;// the delay between advertising our state while people are scanning

// VARIABLES
var isBluetoothEnabled = true;
var isBluetoothConnected = false;
var isBluetoothScanning = false;
var isFlashingLED = false;
var ledValue = 0;
var ledFlashCount = 0;
var ledFlashTarget = 0;

var btScanFlashState = 0;
var isLog = false;

var bluetoothSleepTimer = null;
var ledFlashingTimer = null;

var clickTimer = null;
var clickCount = 0;
var lastClick = CLICKNONE;

function log(output) {
    if (isLog) {
        console.log(output);
    }
}

function advertiseState() {
    // set the advertisement of our current state
    //** tried to change the state so when connected turned of discoverable and showName but
    // this resulted in unreliable discovery after a restart - instead just advertise the same regardless
    try {
        log("Advertising with the single standard setup");
        // just do basic common advertising
        NRF.setAdvertising({}, {
            name: "Scorepal Remote",
            showName: true,
            discoverable: true,
            manufacturer: 0x0590,
            manufacturerData:[Puck.getBatteryPercentage()],
            interval: ADVERTISINGDELAYSCCAN
        });
        /*if (isBluetoothScanning) {
            log("Advertising at the scan interval");
            // someone is scanning for this, set a short interval and advertise our name
            NRF.setAdvertising({}, {
                name: "Scorepal Remote",
                showName: true,
                discoverable: true,
                manufacturer: 0x0590,
                manufacturerData:[Puck.getBatteryPercentage()],
                interval: ADVERTISINGDELAYSCCAN
            });
        }
        else {
            log("Advertising at the sleepy interval");
            // just do basic, slow, information to keep everything alive and well
            NRF.setAdvertising({}, {
                name: "Scorepal Remote",
                showName: false,
                discoverable: false,
                manufacturer: 0x0590,
                manufacturerData:[Puck.getBatteryPercentage()],
                interval: ADVERTISINGDELAY
            });
        }*/
    }
    catch(err) {
        log("Failed to advertise  " + err);
    }
}

function flashLED(delay) {
    if (ledFlashingTimer !== null) {
        clearTimeout(ledFlashingTimer);
        ledFlashingTimer = null;
    }

    // turn off the others - one at a time at this time in code
    if (!isFlashingLED) {
        LED2.write(0);
    }
    else {
        // set our LED to the correct value
        LED2.write(ledValue);
        // and turn on / off accordingly
        if (ledValue == 1 && ledFlashTarget > 0) {
            // we want to increment the count of on times
            ledFlashCount = ledFlashCount + 1;
        }
        if (ledValue == 1 || ledFlashTarget == 0 || ledFlashCount < ledFlashTarget) {
            // we just turned it on (always want to turn off at the end, or there is no target, or we haven't met it yet)
            ledValue = !ledValue;
            // and call this function again in a sec to alternate the state
            ledFlashingTimer = setTimeout(function() {
                // kill the timer
                ledFlashingTimer = null;
                // and change the LED
                flashLED(delay);
            }, delay);
        }
    }
}

function setLEDFlashing(target, delay) {
    // set the variables
    isFlashingLED = true;
    ledValue = 1;
    ledFlashCount = 0;
    ledFlashTarget = target;
    // start turning on / flashing
    flashLED(delay);
}

function resetLED() {
    // set the variables
    isFlashingLED = false;
    ledValue = 0;
    // and turn the LED off
    flashLED(FLASHDELAY);
}

function stopScanning() {
    isBluetoothScanning = false;
    clearInterval();
    btScanFlashState = 0;
    LED3.write(btScanFlashState);
}

function killBluetoothTimer() {
    if (bluetoothSleepTimer !== null) {
        clearTimeout(bluetoothSleepTimer);
        bluetoothSleepTimer = null;
    }
}

function sleepBluetooth() {
    // kill any old sleep timer running
    killBluetoothTimer();
    // stop flashing LED3
    stopScanning();
    if (isBluetoothConnected) {
        NRF.disconnect();
        isBluetoothConnected = false;
    }
    resetLED();
    // and actually sleep
    if (isBluetoothEnabled) {
        try {
            log("sleeping bluetooth.");
            NRF.sleep();
            // and show the user they managed something
            setTimeout(function() {
                digitalPulse(LED3,1,500);
            });
        }
        catch(err) {
            log("Failed to sleep " + err);
        }
    }
    // BT is disabled now we are going to sleep
    isBluetoothEnabled = false;
}

function wakeUpBluetooth() {
    // be sure BT is awake
    if (!isBluetoothEnabled) {
        try {
            log("waking up bluetooth.");
            NRF.wake();
        }
        catch(err) {
            log("Failed to wake up " + err);
        }
    }
    // can fail if already on - still, we are on.
    isBluetoothEnabled = true;
    // kill any old sleep timer running
    if (bluetoothSleepTimer !== null) {
        clearTimeout(bluetoothSleepTimer);
        bluetoothSleepTimer = null;
    }
    var delay = BTSLEEPDELAY;
    if (!isBluetoothConnected) {
        // if we are not connected, go to sleep earlier
        delay = BTSCANDELAY;
        if (!isBluetoothScanning) {
            // we are disconnected, show something to the user that we are waiting for a connection
            setTimeout(function() {
                setInterval("digitalWrite(LED3,btScanFlashState++ % 4 == 0);",400);
            });
            isBluetoothScanning = true;
        }
    }
    log("Sleeping in " + (delay / 60000) + " mins");
    if (isBluetoothEnabled) {
        // start a new sleep timer to shutdown bluetooth after much inactivity
        bluetoothSleepTimer = setTimeout(function() {
            // timer has activated, need to put this to sleep now
            bluetoothSleepTimer = null;
            // and sleep
            sleepBluetooth();
        }, delay);
    }
}

// on connect, we want to show that we are connected and change the LED
NRF.on('connect',function(addr) {
    // remember this
    setTimeout(function() {
        isBluetoothConnected = true;
        // show that we just connected
        digitalPulse(LED1, 1, 500);
        // and kill any flashing on LED3 we were doing
        stopScanning();
        // wake up bluetooth now we are connected
        wakeUpBluetooth();
        // setup some simple advertising to keep everything alive and active
        advertiseState();
    });
});

NRF.on('disconnect',function(addr) {
    // remember this
    setTimeout(function() {
        isBluetoothConnected = false;
        stopScanning();
        // show that we just disconnected
        digitalPulse(LED1, 1, 1000);
        if (isBluetoothEnabled) {
            // bluetooth is enabled, this is important as if it is disabled then it is a disconnection
            // caused by us when we turn ourselves off, don't want to turn back on now do we?
            wakeUpBluetooth();
            // waking up BT will cause it to sleep in a short time if there is no new connection
            // finally we can setup less intensive advertising to keep everything informed at a more battery-friendly level
            advertiseState();
        }
    });
});

function killClickTimer() {
    if (clickTimer !== null) {
        clearTimeout(clickTimer);
        clickTimer = null;
    }
}

function processLastClick() {
    var operation = null;
    switch(lastClick) {
        case CLICKSINGLE:
            log("single click");
            operation = function() {
                controls.next();
                setLEDFlashing(1, FLASHDELAY);
            };
            break;
        case CLICKDOUBLE:
            log("double click");
            operation = function() {
                controls.prev();
                setLEDFlashing(2, FLASHDELAY);
            };
            break;
        case CLICKTRIPLE:
            log("triple click");
            operation = function() {
                controls.stop();
                setLEDFlashing(3, FLASHDELAY);
            };
            break;
        case CLICKLONG:
            log("long click");
            operation = function() {
                controls.playpause();
                setLEDFlashing(1, PULSEDELAY);
            };
            break;
        case CLICKSUPERLONG:
            log("super-long click");
            // pulse right away to show we are disconnecting
            setLEDFlashing(1, PULSEDELAY);
            // and sleep
            sleepBluetooth();
            break;
        default:
            // just reset the LED in case it sticks on, no operation to perform
            log("unsupported click number of " + lastClick);
            resetLED();
            break;
    }
    if (operation !== null) {
        // perform the operation in the main thread on it's own to not crash us
        setTimeout(function() {
            try {
                // cancel any previous lights before we fail and throw
                resetLED();
                // wake up bluetooth first
                wakeUpBluetooth();
                // perform the operation
                operation();
                // advertise our new state
                advertiseState();
            }
            catch(err) {
                log("Failed to send the control message " + err);
                // show this was a problem to the operator
                setTimeout(function() {
                    digitalPulse(LED1,1,500);
                });
            }
        });
    }
    // reset our click counter
    clickCount = 0;
}

// listen for the button going down, ready for a LONG click - holding it for a second
setWatch(function() {
    // do all of this on the main thread
    setTimeout(function() {
        // kill the old timer
        killClickTimer();
        // clear the last click as we are starting again as soon as this goes down
        lastClick = CLICKNONE;
        // and cancel this in a long time, if we are still down at the end this is a long click
        clickTimer = setTimeout(function () {
            // ended, clear it
            clickTimer = null;
            // this has activated, this is a long click
            lastClick = CLICKLONG;
            // and process this
            processLastClick();
            // also wait for another time to see if they super-long press
            clickTimer = setTimeout(function () {
                // ended, clear it
                clickTimer = null;
                // this has activated, this is a super-long click
                lastClick = CLICKSUPERLONG;
                // and process this
                processLastClick();
            }, SUPERLONGCLICKPERIOD - LONGCLICKPERIOD);
        }, LONGCLICKPERIOD);
    });
}, BTN, {edge:"rising", debounce:50, repeat:true});

// watch for a button click releasing so we can handle it properly
setWatch(function() {
    // do all of this on the main thread
    setTimeout(function() {
        // kill the old timer, will also kill any long click initiated
        killClickTimer();
        if (lastClick >= 0) {
            // this was not a long click being released, increment the counter of short clicks
            clickCount += 1;
            // and cancel this in a short time, counting another click if they click in that time
            clickTimer = setTimeout(function () {
                // this has activated, let another one start
                clickTimer = null;
                // set the last click (if more than 3 then nothing)
                lastClick = clickCount;
                // and process this
                processLastClick();
            }, CLICKPERIOD);
        }
    });
}, BTN, {edge:"falling", debounce:50, repeat:true});

E.on('init', function() {
    // start / stop logging right away
    isLog = false;
    // drive straight to the call-stack to prevent threading issues
    setTimeout(function() {
        // initialise all our data
        log("data initialising");
        isBluetoothEnabled = true;
        isBluetoothConnected = false;
        isBluetoothScanning = false;
        isFlashingLED = false;
        ledValue = 0;
        ledFlashCount = 0;
        ledFlashTarget = 0;

        log("timers initialising");
        killClickTimer();
        killBluetoothTimer();
        ledFlashingTimer = null;

        clickTimer = null;
        clickCount = 0;
        lastClick = CLICKNONE;
        // wake up now
        log("bluetooth initialising");
        wakeUpBluetooth();
        // and advertise our initial state
        setTimeout(function() {
            log("advertising initialising");
            advertiseState();
            log("advertising initialised");
        });
        log("initialisation complete");
    });
});