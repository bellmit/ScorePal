#import <Flutter/Flutter.h>
#import "Flic2ControllerListener.h"

@ import flic2lib;

@interface Flic2Controller : NSObject<FLICButtonDelegate, FLICManagerDelegate>
- (id)initWithListener:(id<Flic2ControllerListener>)callback;
- (void)dispose;

- (void)startButtonScanning;
@end
