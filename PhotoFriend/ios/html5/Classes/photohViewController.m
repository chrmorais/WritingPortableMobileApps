//
//  photohViewController.m
//  photoh
//
//  Copyright 2015 Elvis Pfützenreuter. All rights reserved.
//

#import "photohViewController.h"

@implementation photohViewController

// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
    }
    return self;
}

- (void) loadPage
{
    NSURL *url = [NSURL fileURLWithPath:[[NSBundle mainBundle]
                       pathForResource:@"indexa" ofType:@"html"] isDirectory:NO];
    [html loadRequest:[NSURLRequest requestWithURL:url]];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    html.delegate = self;
    
    NSUserDefaults* prefs = [NSUserDefaults standardUserDefaults];
    [prefs registerDefaults:
        [NSDictionary dictionaryWithObjectsAndKeys: @"", @"data", nil]];
    
    [html setBackgroundColor: [UIColor colorWithRed:55.0/255.0 green:52.0/255.0 blue:53.0/255.0 alpha:1.0]];
    self.view.backgroundColor = [UIColor colorWithRed:96.0/255.0 green:144.0/255.0 blue:96.0/255.0 alpha:1.0];
    splash_fadedout = NO;

    [self loadPage];
}

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    NSString *data  = [prefs stringForKey: @"data"];
    NSString *f = [NSString stringWithFormat: @"preload(\"%@\");", data];
    [html stringByEvaluatingJavaScriptFromString: f];
    f = [NSString stringWithFormat: @"init();"];
    [html stringByEvaluatingJavaScriptFromString: f];
    
    if (splash_fadedout)
        return;

    splash_fadedout = YES;

    [UIView beginAnimations:nil context:NULL];
    [UIView setAnimationDuration:0.5];
    [UIView animateWithDuration:0.7
                        delay:0.7
                        options: 0
                        animations:^{
                             [html setAlpha: 1.00];
                        }
                        completion:^(BOOL finished){
                        }];
}

- (BOOL) webView:(UIWebView *)view 
shouldStartLoadWithRequest:(NSURLRequest *)request 
  navigationType:(UIWebViewNavigationType)navigationType {
    
	NSString *req = [[request URL] absoluteString];
    
    if ([[req substringToIndex:7] isEqualToString:@"photoh:"]) {
        if ([[req substringToIndex:12] isEqualToString:@"photoh:save:"]) {
            NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
            [prefs setObject: [req substringFromIndex: 12] forKey: @"data"];
        }
        return NO;
    }

	return YES;
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return NO;
}

- (BOOL)shouldAutorotate
{
    return NO;
}

- (BOOL)prefersStatusBarHidden
{
    return YES;
}

-(NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskPortrait | UIInterfaceOrientationMaskPortraitUpsideDown;
}

- (void)dealloc {
    NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
    [center removeObserver:self];
}

@end
