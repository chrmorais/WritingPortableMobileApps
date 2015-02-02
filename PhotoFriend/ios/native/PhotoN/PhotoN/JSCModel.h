//
//  JSCModel.h
//  PhotoN
//
//  Copyright (c) 2015 Elvis Pfutzenreuter. All rights reserved.
//

#ifndef PhotoN_JSCModel_h
#define PhotoN_JSCModel_h

#include "ViewController.h"
@import JavaScriptCore;

@interface JSCModel : NSObject {
}

- (id) initWithObserver: (ViewController*) observer;
- (void) start;
- (void) input: (NSString*) name delta: (int) delta;
- (void) pictureWithIso: (NSString*) iso aperture: (NSString*) aperture shutter: (NSString*) shutter;

@property (nonatomic, retain) ViewController *observer;
@property (nonatomic, retain) JSContext *context;

@end;

#endif
