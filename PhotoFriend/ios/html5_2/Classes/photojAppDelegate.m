//
//  photojAppDelegate.m
//  photoj
//
//  Copyright 2015 Elvis Pfützenreuter. All rights reserved.
//

#import "photojAppDelegate.h"
#import "photojViewController.h"

@implementation photojAppDelegate

@synthesize window = _window;
@synthesize viewController = _viewController;


#pragma mark -
#pragma mark Application lifecycle

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    self.viewController = [[photojViewController alloc] initWithNibName:@"photojViewController" bundle:nil];

    self.window.rootViewController = self.viewController;
    [self.window makeKeyAndVisible];
    return YES;
}


@end
