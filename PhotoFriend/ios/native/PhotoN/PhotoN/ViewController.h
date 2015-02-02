//
//  ViewController.h
//  PhotoN
//
//  Copyright (c) 2015 Elvis Pfutzenreuter. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController <UIImagePickerControllerDelegate, UINavigationControllerDelegate> {
}

- (NSString*) loadPreferences;
- (void) savePreferences: (NSString*) data;
- (void) show: (NSString*) name text: (NSString*) text index: (NSNumber*) index;

@property (nonatomic, retain) IBOutlet UIButton* shoot;
@property (nonatomic, retain) IBOutlet UILabel* ev;
@property (nonatomic, retain) IBOutlet UILabel* evdesc;
@property (nonatomic, retain) IBOutlet UILabel* iso;
@property (nonatomic, retain) IBOutlet UILabel* shutter;
@property (nonatomic, retain) IBOutlet UILabel* aperture;
@property (nonatomic, retain) IBOutlet UILabel* length;
@property (nonatomic, retain) IBOutlet UILabel* distance;
@property (nonatomic, retain) IBOutlet UILabel* dof;
@property (nonatomic, retain) IBOutlet UIStepper* evinput;
@property (nonatomic, retain) IBOutlet UIStepper* isoinput;
@property (nonatomic, retain) IBOutlet UIStepper* shutterinput;
@property (nonatomic, retain) IBOutlet UIStepper* apertureinput;
@property (nonatomic, retain) IBOutlet UIStepper* lengthinput;
@property (nonatomic, retain) IBOutlet UIStepper* distanceinput;
@property (nonatomic, retain) IBOutlet UIImageView* viewfinder;

- (IBAction) evChanged:(id) sender;
- (IBAction) isoChanged:(id) sender;
- (IBAction) shutterChanged:(id) sender;
- (IBAction) apertureChanged:(id) sender;
- (IBAction) lengthChanged:(id) sender;
- (IBAction) distanceChanged:(id) sender;
- (IBAction) shootSample;

@end

