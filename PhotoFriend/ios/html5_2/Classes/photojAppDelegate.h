//
//  photojAppDelegate.h
//  photoj
//
//  Copyright 2015 Elvis Pfützenreuter. All rights reserved.
//

#import <UIKit/UIKit.h>

@class photojViewController;

@interface photojAppDelegate : NSObject <UIApplicationDelegate>;

@property (strong, nonatomic) UIWindow *window;
@property (strong, nonatomic) photojViewController *viewController;

@end

