//
//  photojViewController.h
//  photoj
//
//  Copyright 2015 Elvis Pfützenreuter. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <AudioToolbox/AudioToolbox.h>
#import <WebKit/WebKit.h>

@interface photojViewController : UIViewController <WKScriptMessageHandler, WKNavigationDelegate> {
    WKWebView *html;
    BOOL splash_fadedout;
}

@end

