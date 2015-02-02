//
//  photojViewController.m
//  photoj
//
//  Copyright 2015 Elvis Pfützenreuter. All rights reserved.
//

#import "photojViewController.h"

@implementation photojViewController

// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    return self;
}

#define IOS_8_0_WKWEBVIEW_BUG_CANT_LOAD_BUNDLE_FILES YES

- (void)viewDidLoad {
    [super viewDidLoad];

    WKWebViewConfiguration *config =[[WKWebViewConfiguration alloc] init];
    [config.userContentController addScriptMessageHandler:self name:@"gateway"];
    
    html = [[WKWebView alloc] initWithFrame:self.view.frame configuration: config];
    [self.view addSubview:html];
    [html setNavigationDelegate: self];
    
    NSUserDefaults* prefs = [NSUserDefaults standardUserDefaults];
    [prefs registerDefaults:
        [NSDictionary dictionaryWithObjectsAndKeys: @"", @"data", nil]];
    
    [html setBackgroundColor: [UIColor colorWithRed:55.0/255.0 green:52.0/255.0 blue:53.0/255.0 alpha:1.0]];
    self.view.backgroundColor = [UIColor colorWithRed:96.0/255.0 green:144.0/255.0 blue:96.0/255.0 alpha:1.0];
    [html setAlpha: 0];

    splash_fadedout = NO;
    
    if (IOS_8_0_WKWEBVIEW_BUG_CANT_LOAD_BUNDLE_FILES) {
        // Workaround: Copy resource files to tmp and open URL in there
        // Learnt from https://github.com/shazron/WKWebViewFIleUrlTest

        NSString *res = [[NSBundle mainBundle] resourcePath];
        NSString *tmp = NSTemporaryDirectory();
        NSString *ufolder = [[NSProcessInfo processInfo] globallyUniqueString];
        NSString *folder = [tmp stringByAppendingPathComponent: ufolder];
        NSURL *furl = [NSURL fileURLWithPath:folder isDirectory:YES];
        [[NSFileManager defaultManager] createDirectoryAtURL:furl withIntermediateDirectories:YES
                                attributes:nil error:nil];
        
        NSFileManager* fileManager = [NSFileManager defaultManager];
        NSArray *files = @[@"photo_controller_ios.js", @"photo.css", @"photo_model.js", @"photo_controller.js",
                           @"indexa.html", @"favicon.ico", @"cache.manifest", @"photo.png"];
        for (NSString* file in files) {
            NSString *src = [res stringByAppendingPathComponent: file];
            NSString *dest = [folder stringByAppendingPathComponent: file];
            [fileManager removeItemAtPath:dest error:nil];
            NSLog(@"Copying file from %@ to %@", src, dest);
            [fileManager copyItemAtPath:src toPath:dest error:nil];
        }
        
        NSURL *url = [NSURL fileURLWithPath:[folder stringByAppendingPathComponent: @"indexa.html"]];
        NSLog(@"Opening URL %@", url);
        [html loadRequest:[NSURLRequest requestWithURL:url]];

    } else {
        // Works in emulator for IOS 8.0, but not in real device (tested in 8.0.2)
        NSURL *url = [NSURL fileURLWithPath:[[NSBundle mainBundle]
                                             pathForResource:@"indexa" ofType:@"html"]];
        [html loadRequest:[NSURLRequest requestWithURL:url]];
    }
}

- (void) webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation
{
    NSLog(@"didFinishNavigation");
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    NSString *data  = [prefs stringForKey: @"data"];

    NSString *f = [NSString stringWithFormat: @"preload(\"%@\");", data];
    [html evaluateJavaScript:f completionHandler: nil];
    [html evaluateJavaScript:@"init()" completionHandler: nil];

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

- (void)userContentController: (WKUserContentController *) userContentController
      didReceiveScriptMessage:(WKScriptMessage *)message
{
    NSLog(@"userContentController");
    NSDictionary *sent = (NSDictionary*) message.body;
    NSString *log = [sent objectForKey: @"log"];
    if (log) {
        NSLog(@"Log from HTML5 %@", log);
    }
    NSString *data = [sent objectForKey: @"data"];
    if (data) {
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        NSLog(@"Data from HTML5 %@", data);
        [prefs setObject: data forKey: @"data"];
    }
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
