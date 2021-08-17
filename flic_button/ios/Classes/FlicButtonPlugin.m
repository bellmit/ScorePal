#import "FlicButtonPlugin.h"
#import "Flic2Controller.h"

static NSString* const ChannelName = @"flic_button";
static NSString* const MethodNameInitialise = @"initializeFlic2";
static NSString* const MethodNameDispose = @"disposeFlic2";
static NSString* const MethodNameCallback = @"callListener";

static NSString* const MethodNameStartFlic2Scan = @"startFlic2Scan";
static NSString* const MethodNameStopFlic2Scan = @"stopFlic2Scan";
static NSString* const MethodNameStartListenToFlic2 = @"startListenToFlic2";
static NSString* const MethodNameStopListenToFlic2 = @"stopListenToFlic2";

static NSString* const MethodNameGetButtons = @"getButtons";
static NSString* const MethodNameGetButtonsByAddr = @"getButtonsByAddr";

static NSString* const MethodNameConnectButton = @"connectButton";
static NSString* const MethodNameDisconnectButton = @"disconnectButton";
static NSString* const MethodNameForgetButton = @"forgetButton";

#define ERROR_CRITICAL @"CRITICAL"
#define ERROR_NOT_STARTED @"NOT_STARTED"
#define ERROR_ALREADY_STARTED @"ALREADY_STARTED"
#define ERROR_INVALID_ARGUMENTS @"INVALID_ARGUMENTS"

#define METHOD_FLIC2_DISCOVER_PAIRED @(100)
#define METHOD_FLIC2_DISCOVERED @(101)
#define METHOD_FLIC2_CONNECTED @(102)
#define METHOD_FLIC2_CLICK @(103)
#define METHOD_FLIC2_SCANNING @(104)
#define METHOD_FLIC2_SCAN_COMPLETE @(105)
#define METHOD_FLIC2_FOUND @(106)
#define METHOD_FLIC2_ERROR @(200)

@implementation FlicButtonPlugin
{
    FlutterMethodChannel* channel;
    Flic2Controller* flic2Controller;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:ChannelName
                                     binaryMessenger:[registrar messenger]];
    FlicButtonPlugin* instance = [[FlicButtonPlugin alloc] initWithChannel:channel];
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (id)initWithChannel:(FlutterMethodChannel*)channel {
    if (self = [super init]) {
        self->channel = channel;
    }
    return self;
}

- (NSString*)buttonToJson:(FLICButton*)button {
    return [NSString stringWithFormat:
         @"{"
         @"\"uuid\":\"%@\","
         @"\"bdAddr\":\"%@\","
         @"\"readyTime\":%d,"
         @"\"name\":\"%@\","
         @"\"serialNo\":\"%@\","
         @"\"connection\":%d,"
         @"\"firmwareVer\":%d,"
         @"\"battPerc\":%d,"
         @"\"battTime\":%d,"
         @"\"battVolt\":%f,"
         @"\"pressCount\":%d"
         @"}",
         button.uuid,
            button.bluetoothAddress,
            0,
            button.name,
            button.serialNumber,
            [NSNumber numberWithLong:button.state].intValue,
            button.firmwareRevision,
            0,
            0,
            button.batteryVoltage,
            button.pressCount];
}

- (void)informListenersOfMethod:(NSNumber*)methodId withData:(NSString*)data {
    // just call the callback code right away with the data specified on the UI thread please
    dispatch_async(dispatch_get_main_queue(), ^{
        // Call the desired channel message here.
        [self->channel invokeMethod:MethodNameCallback arguments:@{@"method": methodId, @"data" : data}];
    });
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([MethodNameInitialise isEqualToString:call.method]) {
        // initialize the Flic2 manager here then please
        if (nil != self->flic2Controller) {
            // already running, this isn't great
            result([FlutterError errorWithCode:ERROR_ALREADY_STARTED message: @"Flic 2 has been initialized already" details: @"Flic 2 started already, okay to call twice but won't do anything..."]);
        } else {
            // create the controller that does all the actual work then
            self->flic2Controller = [[Flic2Controller alloc] initWithListener:self];
            // and return the success of this
            result(@(YES));
        }
    }
    else if ([MethodNameDispose isEqualToString:call.method]) {
        // dispose of the Flic2 manager here then please
        if (nil == self->flic2Controller) {
            // trying to stop something that isn't started
            result([FlutterError errorWithCode:ERROR_NOT_STARTED message: @"Flic 2 hasn't been initialized" details: @"Flic 2 isn't running so we can't stop it..."]);
        } else {
            // inform the controller that we are disposing it here
            [self->flic2Controller dispose];
            // and clear it1
            self->flic2Controller = nil;
            // and return the success of this
            result(@(YES));
        }
    }
    else if ([MethodNameStartFlic2Scan isEqualToString:call.method]) {
        // start scanning for Flic2 buttons
        if (nil == self->flic2Controller) {
            // not started so we can't scan for sure
            result([FlutterError errorWithCode:ERROR_NOT_STARTED message: @"Flic 2 hasn't been initialized" details: @"Flic 2 isn't running so we can't scan..."]);
        } else {
            [self->flic2Controller startButtonScanning];
            // and return the success of this
            result(@(YES));
        }
    }
    else if ([MethodNameStopFlic2Scan isEqualToString:call.method]) {
        // stop any scanning in progress
        
        // and return the success of this
        result(@(YES));
    }
    else if ([MethodNameGetButtons isEqualToString:call.method]) {
        // get all the buttons here
        
        // and return the success of this
        result([NSArray alloc]);
    }
    else if ([MethodNameGetButtonsByAddr isEqualToString:call.method]) {
        // get the button object for the specified address then please
        // the first argument is the address of the button to return
        NSString* buttonAddress = (NSString*) call.arguments[0];
        
        // and return the success of this
        result(@"this is button data then");
    }
    else if ([MethodNameStartListenToFlic2 isEqualToString:call.method]) {
        // listen to the specified button, the first argument being the UUID of the button
        NSString* buttonUuid = (NSString*) call.arguments[0];
        
        // and return the success of this
        result(@(YES));
    }
    else if ([MethodNameStopListenToFlic2 isEqualToString:call.method]) {
        // stop listening to the specified button, the first argument being the UUID of the button
        NSString* buttonUuid = (NSString*) call.arguments[0];
        
        // and return the success of this
        result(@(YES));
    }
    else if ([MethodNameConnectButton isEqualToString:call.method]) {
        // connect to the specified button, the first argument being the UUID of the button
        NSString* buttonUuid = (NSString*) call.arguments[0];
        
        // and return the success of this
        result(@(YES));
    }
    else if ([MethodNameDisconnectButton isEqualToString:call.method]) {
        // disconnect from the specified button, the first argument being the UUID of the button
        NSString* buttonUuid = (NSString*) call.arguments[0];
        
        // and return the success of this
        result(@(YES));
    }
    else if ([MethodNameForgetButton isEqualToString:call.method]) {
        // forget the specified button, the first argument being the UUID of the button
        NSString* buttonUuid = (NSString*) call.arguments[0];
        
        // and return the success of this
        result(@(YES));
    }
    else {
        result(FlutterMethodNotImplemented);
    }
}

- (void)onButtonClicked:(FLICButton *)button wasQueued:(BOOL)queued at:(NSInteger)age withClicks:(NSInteger)clicks {
    // need to convert all this to a nice JSON structure to send then
    NSString* jsonData = [NSString stringWithFormat:
                          @"{"
                          @"\"wasQueued\":%s,"
                          @"\"clickAge\":%ld,"
                          @"\"lastQueued\":%s,"
                          @"\"timestamp\":%d,"
                          @"\"isSingleClick\":%s,"
                          @"\"isDoubleClick\":%s,"
                          @"\"isHold\":%s,"
                          @"\"button\":%@"
                          @"}",
                          queued ? "true" : "false",
                          age,
                          queued ? "true" : "false",
                          0,
                          clicks == 1 ? "true" : "false",
                          clicks == 2 ? "true" : "false",
                          clicks == 3 ? "true" : "false",
                          [self buttonToJson:button]];
    // just send this method with the correct data then please
    [self informListenersOfMethod:METHOD_FLIC2_CLICK withData:jsonData];
}

- (void)onButtonConnected {
    // just send this method with the correct data then please
    [self informListenersOfMethod:METHOD_FLIC2_CONNECTED withData:nil];
}

- (void)onButtonDiscovered:(NSString *)buttonAddress {
    // just send this method with the correct data then please
    [self informListenersOfMethod:METHOD_FLIC2_DISCOVERED withData:buttonAddress];
}

- (void)onButtonFound:(FLICButton *)button {
    // just send this method with the correct data then please
    [self informListenersOfMethod:METHOD_FLIC2_FOUND withData:[self buttonToJson:button]];
}

- (void)onButtonScanningStarted {
    // just send this method with the correct data then please
    [self informListenersOfMethod:METHOD_FLIC2_SCANNING withData:nil];
}

- (void)onButtonScanningStopped {
    // just send this method with the correct data then please
    [self informListenersOfMethod:METHOD_FLIC2_SCAN_COMPLETE withData:nil];
}

- (void)onError:(NSString *)errorString {
    // just send this method with the correct data then please
    [self informListenersOfMethod:METHOD_FLIC2_ERROR withData:errorString];
}

- (void)onPairedButtonFound:(FLICButton *)button {
    // just send this method with the correct data then please
    [self informListenersOfMethod:METHOD_FLIC2_DISCOVER_PAIRED withData:[self buttonToJson:button]];
}

@end
