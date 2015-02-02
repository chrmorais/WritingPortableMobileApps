//
//  photohAppDelegate.h
//  photoh
//
//  Copyright 2015 Elvis Pfützenreuter. All rights reserved.
//

#import <UIKit/UIKit.h>

@class photohViewController;

@interface photohAppDelegate : NSObject <UIApplicationDelegate>;

@property (strong, nonatomic) UIWindow *window;
@property (strong, nonatomic) photohViewController *viewController;

@end

