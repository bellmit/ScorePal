#import <Flutter/Flutter.h>
#import "FlicButtonPlugin.h"

@ import flic2lib;

@interface Flic2Controller : NSObject<FLICButtonDelegate, FLICManagerDelegate>
- (id)initWithPlugin:(FlicButtonPlugin*)plugin;
- (void)dispose;
@end
