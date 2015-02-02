//
//  photohAppDelegate.m
//  photoh
//
//  Copyright 2015 Elvis Pfützenreuter. All rights reserved.
//

#import "photohAppDelegate.h"
#import "photohViewController.h"

@implementation photohAppDelegate

@synthesize window = _window;
@synthesize viewController = _viewController;


#pragma mark -
#pragma mark Application lifecycle

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    self.viewController = [[photohViewController alloc] initWithNibName:@"photohViewController" bundle:nil];

    self.window.rootViewController = self.viewController;
    [self.window makeKeyAndVisible];
    return YES;
}


@end
