#import <Foundation/Foundation.h>
#import "Flic2Controller.h"

@ import flic2lib;

@implementation Flic2Controller
{
    FlicButtonPlugin* plugin;
    NSMutableDictionary* buttonsDiscovered;
}

- (id)initWithPlugin:(FlicButtonPlugin*)plugin {
    if (self = [super init]) {
        self->plugin = plugin;
        self->buttonsDiscovered = [[NSMutableDictionary alloc] init];
        // and initialize the Flic2 singleton
        [FLICManager configureWithDelegate:self buttonDelegate:self background:YES];
    }
    return self;
    
}

- (void)dispose {
    // shut everything down
    self->plugin = nil;
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

- (void)managerDidRestoreState:(nonnull FLICManager *)manager {
    // The manager was restored and can now be used.
    for (FLICButton *button in manager.buttons)
    {
        NSLog(@"Did restore Flic: %@", button.name);
    }
}

- (NSString*)buttonToJson:(NSObject*)button {
    return @"{"
    @"\"uuid\":\"" @"button.getUuid()" @"\","
    @"\"bdAddr\":\"" @"button.getBdAddr()" @"\","
    @"\"readyTime\":" @"button.getReadyTimestamp()" @","
    @"\"name\":\"" @"button.getName()" @"\","
    @"\"serialNo\":\"" @"button.getSerialNumber()" @"\","
    @"\"connection\":" @"button.getConnectionState()" @","
    @"\"firmwareVer\":" @"button.getFirmwareVersion()" @","
    @"\"battPerc\":" @"button.getLastKnownBatteryLevel().getEstimatedPercentage()" @","
    @"\"battTime\":" @"button.getLastKnownBatteryLevel().getTimestampUtcMs()" @","
    @"\"battVolt\":" @"button.getLastKnownBatteryLevel().getVoltage()" @","
    @"\"pressCount\":" @"button.getPressCount()" ""
    @"}";
}

- (NSString*)buttonClickToJson:(NSObject*)buttonClick {
    return @"{"
    @"\"wasQueued\":" @"wasQueued" @","
    @"\"clickAge\":" @"(wasQueued ? button.getReadyTimestamp() - timestamp : 0)" @","
    @"\"lastQueued\":" @"lastQueued" @","
    @"\"timestamp\":" @"timestamp" @","
    @"\"isSingleClick\":" @"isSingleClick" @","
    @"\"isDoubleClick\":" @"isDoubleClick" @","
    @"\"isHold\":" @"isHold" @","
    @"\"button\":" @"ButtonToJson(button)"
    @"}";
}

- (void)startScan {
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
}

- (void)buttonIsReady:(nonnull FLICButton *)button {
    NSLog(@"Button ready: %@", button.name);
}

- (void)button:(FLICButton *)button didReceiveButtonClick:(BOOL)queued age:(NSInteger)age {
    NSLog(@"Flic: %@ was clicked", button.name);
}

@end
