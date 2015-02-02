//
//  photohViewController.h
//  photoh
//
//  Copyright 2015 Elvis Pfützenreuter. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <AudioToolbox/AudioToolbox.h>

@interface photohViewController : UIViewController <UIWebViewDelegate> {
    IBOutlet UIWebView *html;
    BOOL splash_fadedout;
}

- (BOOL) webView:(UIWebView *)view shouldStartLoadWithRequest:(NSURLRequest *)request 
  navigationType:(UIWebViewNavigationType)navigationType;

@end

