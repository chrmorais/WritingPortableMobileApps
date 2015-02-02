//
//  JSCModel.m
//  PhotoN
//
//  Copyright (c) 2015 Elvis Pfutzenreuter. All rights reserved.
//

#import "JSCModel.h"

@interface JSCModel() {
}

@end

@implementation JSCModel

- (id) initWithObserver: (ViewController*) obs
{
    self = [super init];
    self.observer = obs;
    
    self.context = [JSContext new];
    
    [self.context setExceptionHandler:^(JSContext *context, JSValue *value) {
        NSLog(@"Javascript exception %@", value);
    }];

    self.context[@"load_cb_native"] = ^ {
        NSString* data = [[self observer] loadPreferences];
        JSValue *ret = [JSValue valueWithObject: data inContext: self.context];
        return ret;
    };
    
    self.context[@"save_cb_native"] = ^ (JSValue *data) {
        [[self observer] savePreferences: [data toString]];
    };
    
    self.context[@"result_cb_native"] = ^ (JSValue* name, JSValue* value, JSValue *index) {
        [[self observer] show: [name toString] text: [value toString] index: [index toNumber]];
    };

    self.context[@"log_native"] = ^ (JSValue* text) {
        NSLog(@"%@", [text toString]);
    };

    self.context[@"setTimeout_native"] = ^ (JSValue* to, JSValue *handle) {
        NSNumber *handlen = [handle toNumber];
        double fto = (0.0 + [to toInt32]) / 1000.0;
        [self performSelector: @selector(jsTimeoutAlarm:) withObject: handlen afterDelay: fto];
    };
    
    NSString *fprepatch = [[NSBundle mainBundle] pathForResource:@"patch_pre" ofType:@"js"];
    NSData *prepatch = [NSData dataWithContentsOfFile:fprepatch];
    NSString *sprepatch = [[NSString alloc] initWithData:prepatch encoding:NSUTF8StringEncoding];

    NSString *fmodel = [[NSBundle mainBundle] pathForResource:@"photo_model" ofType:@"js"];
    NSData *model = [NSData dataWithContentsOfFile:fmodel];
    NSString *smodel = [[NSString alloc] initWithData:model encoding:NSUTF8StringEncoding];
    
    NSString *fpatch = [[NSBundle mainBundle] pathForResource:@"patch" ofType:@"js"];
    NSData *patch = [NSData dataWithContentsOfFile:fpatch];
    NSString *spatch = [[NSString alloc] initWithData:patch encoding:NSUTF8StringEncoding];
    
    [self.context evaluateScript: sprepatch];
    [self.context evaluateScript: smodel];
    [self.context evaluateScript: spatch];
    
    return self;
}

- (JSValue*) exec: (NSString*) f args: (NSArray*) args
{
    JSValue *jf = self.context[f];
    if (! jf) {
        NSLog(@"JS function %@ not found", f);
        return nil;
    }
    JSValue *value = [jf callWithArguments: args];
    return value;
}

- (void) start
{
    [self exec: @"model_init" args: @[]];
}

- (void) input:(NSString *)name delta:(int)delta
{
    [self exec: @"model_input" args: @[name, delta > 0 ? @"plus" : @"minus"]];
}

- (void) pictureWithIso: (NSString*) iso aperture: (NSString*) aperture shutter: (NSString*) shutter
{
    [self exec: @"model_sample_picture" args: @[iso, aperture, shutter]];
}

- (void) jsTimeoutAlarm: (NSNumber *) handle
{
    [self exec: @"setTimeoutCallback" args: @[handle]];
}


@end
