#import "FlicButtonPlugin.h"

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

#define METHOD_FLIC2_DISCOVER_PAIRED 100
#define METHOD_FLIC2_DISCOVERED 101
#define METHOD_FLIC2_CONNECTED 102
#define METHOD_FLIC2_CLICK 103
#define METHOD_FLIC2_SCANNING 104
#define METHOD_FLIC2_SCAN_COMPLETE 105
#define METHOD_FLIC2_FOUND 106
#define METHOD_FLIC2_ERROR 200

@implementation FlicButtonPlugin
{
    FlutterMethodChannel* channel;
    NSMutableDictionary* buttonsDiscovered;
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
        self->buttonsDiscovered = [[NSMutableDictionary alloc] init];
    }
    return self;
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

- (void)informListenersOfMethod:(NSNumber*)methodId withData:(NSString*)data {
    // just call the callback code right away with the data specified
    [channel invokeMethod:MethodNameCallback arguments:@{@"method": methodId, @"data" : data}];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([MethodNameInitialise isEqualToString:call.method]) {
    // initialize the Flic2 manager here then please

    // and return the success of this
    result(@(YES));
  }
  else if ([MethodNameDispose isEqualToString:call.method]) {
    // dispose of the Flic2 manager here then please

    // and return the success of this
    result(@(YES));
  }
  else if ([MethodNameStartFlic2Scan isEqualToString:call.method]) {
    // start scanning for Flic2 buttons

    // and return the success of this
    result(@(YES));
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
    result([self buttonToJson:buttonAddress]);
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

@end
