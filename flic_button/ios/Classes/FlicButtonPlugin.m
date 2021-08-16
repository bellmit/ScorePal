#import "FlicButtonPlugin.h"

@implementation FlicButtonPlugin
{
    NSMutableDictionary* callbackById;
    FlutterMethodChannel* channel;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flic_button"
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

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"initializeService" isEqualToString:call.method]) {
        // Prepare callback dictionary
      if (self->callbackById == nil)
        self->callbackById = [NSMutableDictionary new];
      // Get callback id
      NSString* currentListenerId = [(NSNumber*) call.arguments[0] stringValue];

      // just callback the code right away for a test without a timer
      [channel invokeMethod:@"callListener"
        arguments:@{
            @"id" : (NSNumber*) call.arguments[0],
            @"method": @"hello_function",
            @"data" : @"some nice data"
            }
        ];

      // Prepare a timer like self calling task
      void (^callback)(void) = ^() {
          void (^callback)(void) = [self->callbackById valueForKey:currentListenerId];
          if ([self->callbackById valueForKey:currentListenerId] != nil) {
              int time = (int) CFAbsoluteTimeGetCurrent();

              [channel invokeMethod:@"callListener"
                               arguments:@{
                                   @"id" : (NSNumber*) call.arguments[0],
                                   @"method": @"hello_function",
                                   @"data" : [NSString stringWithFormat:@"Hello Listener! %d", time]
                               }
              ];

              dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC), dispatch_get_main_queue(), callback);
          }
      };
      // Run task
      [self->callbackById setObject:callback forKey:currentListenerId];
      dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC), dispatch_get_main_queue(), callback);

      // Return immediately
      result(nil);
  }
  else if ([@"cancelListening" isEqualToString:call.method]) {
        // Get callback id
      NSString* currentListenerId = [(NSNumber*) call.arguments[0] stringValue];
      // Remove callback
      [self->callbackById removeObjectForKey:currentListenerId];
      // Do additional stuff if required to cancel the listener

      result(nil);
  }
  else {
    result(FlutterMethodNotImplemented);
  }
}

@end
