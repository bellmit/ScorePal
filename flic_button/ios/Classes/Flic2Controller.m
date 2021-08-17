#import <Foundation/Foundation.h>
#import "Flic2Controller.h"
#import "Flic2ControllerListener.h"

@ import flic2lib;

@implementation Flic2Controller
{
    id<Flic2ControllerListener> callback;
    NSMutableDictionary* buttonsDiscovered;
}

- (id)initWithListener:(id<Flic2ControllerListener>)callback {
    if (self = [super init]) {
        self->callback = callback;
        self->buttonsDiscovered = [[NSMutableDictionary alloc] init];
        // and initialize the Flic2 singleton
        [FLICManager configureWithDelegate:self buttonDelegate:self background:YES];
    }
    return self;
    
}

- (void)dispose {
    // shut everything down
    self->callback = nil;
}

- (void)manager:(nonnull FLICManager *)manager didUpdateState:(FLICManagerState)state {
    switch (state)
    {
        case FLICManagerStatePoweredOn:
            // Flic buttons can now be scanned and connected.
            NSLog(@"Bluetooth is turned on");
            break;
        case FLICManagerStatePoweredOff:
            // Bluetooth is not powered on.
            NSLog(@"Bluetooth is turned off");
            break;
        case FLICManagerStateUnsupported:
            // The framework can not run on this device.
            NSLog(@"FLICManagerStateUnsupported");
        default:
            break;
    }
}

- (void)initializeButton:(FLICButton*)button {
    // setup the button properly then please
    if (button.triggerMode != FLICButtonTriggerModeClickAndDoubleClickAndHold) {
        // change the mode of the button to tell us everything please
        NSLog(@"changing button to inform about all types of button press");
        button.triggerMode = FLICButtonTriggerModeClickAndDoubleClickAndHold;
    }
}

- (void)managerDidRestoreState:(nonnull FLICManager *)manager {
    // The manager was restored and can now be used.
    for (FLICButton *button in manager.buttons) {
        NSLog(@"Did restore Flic: %@", button.name);
        // and set it up then
        [self initializeButton:button];
    }
}

- (void)startButtonScanning {
    [[FLICManager sharedManager] scanForButtonsWithStateChangeHandler:^(FLICButtonScannerStatusEvent event) {
        // You can use these events to update your UI.
        switch (event)
        {
            case FLICButtonScannerStatusEventDiscovered:
                NSLog(@"A Flic was discovered.");
                break;
            case FLICButtonScannerStatusEventConnected:
                NSLog(@"A Flic is being verified.");
                break;
            case FLICButtonScannerStatusEventVerified:
                NSLog(@"The Flic was verified successfully.");
                break;
            case FLICButtonScannerStatusEventVerificationFailed:
                NSLog(@"The Flic verification failed.");
                break;
            default:
                break;
        }
    } completion:^(FLICButton *button, NSError *error) {
        NSLog(@"Scanner completed with error: %@", error);
        if (!error)
        {
            NSLog(@"Successfully verified: %@, %@, %@", button.name, button.bluetoothAddress, button.serialNumber);
            // Listen to single click only.
            button.triggerMode = FLICButtonTriggerModeClick;
        }
    }];
}

- (void)button:(nonnull FLICButton *)button didDisconnectWithError:(NSError * _Nullable)error {
    NSLog(@"Did disconnect Flic: %@", button.name);
}

- (void)button:(nonnull FLICButton *)button didFailToConnectWithError:(NSError * _Nullable)error {
    NSLog(@"Did fail to connect Flic: %@", button.name);
}

- (void)buttonDidConnect:(nonnull FLICButton *)button {
    NSLog(@"Did connect Flic: %@", button.name);
    // and set it up then
    [self initializeButton:button];
}

- (void)buttonIsReady:(nonnull FLICButton *)button {
    NSLog(@"Button ready: %@", button.name);
    // and set it up then
    [self initializeButton:button];
}

- (void)button:(FLICButton *)button didReceiveButtonClick:(BOOL)queued age:(NSInteger)age {
    NSLog(@"Flic: %@ was clicked", button.name);
    if (nil != self->callback) {
        // pass this to the callback then
        [self->callback onButtonClicked:button wasQueued:queued at:age withClicks:1];
    }
}

- (void)button:(FLICButton *)button didReceiveButtonDoubleClick:(BOOL)queued age:(NSInteger)age {
    NSLog(@"Flic: %@ was double-clicked", button.name);
    if (nil != self->callback) {
        // pass this to the callback then
        [self->callback onButtonClicked:button wasQueued:queued at:age withClicks:2];
    }
    
}

- (void)button:(FLICButton *)button didReceiveButtonHold:(BOOL)queued age:(NSInteger)age {
    NSLog(@"Flic: %@ was held", button.name);
    if (nil != self->callback) {
        // pass this to the callback then
        [self->callback onButtonClicked:button wasQueued:queued at:age withClicks:3];
    }
    
}

@end
