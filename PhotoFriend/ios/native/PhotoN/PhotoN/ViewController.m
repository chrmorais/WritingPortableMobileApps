//
//  ViewController.m
//  PhotoN
//
//  Copyright (c) 2015 Elvis Pfutzenreuter. All rights reserved.
//

#import "ViewController.h"
#include "JSCModel.h"

@interface ViewController () {
    NSArray *ksteppers;
    NSArray *klabels;
    NSArray *steppers;
    NSArray *labels;
    BOOL camera;
    JSCModel *model;
    NSString *near;
    NSString *far;
}

@end

@implementation ViewController

- (int) index: (NSString*) s in: (NSArray*) a
{
    for (int i = 0; i < [a count]; ++i) {
        if ([[a objectAtIndex: i] isEqualToString: s]) {
            return i;
        }
    }
    NSLog(@"Label %@ not found", s);
    return -1;
}

- (UILabel*) glabel: (NSString*) name
{
    return [labels objectAtIndex: [self index: name in: klabels]];
}

- (UIStepper*) gstepper: (NSString*) name
{
    return [steppers objectAtIndex: [self index: name in: ksteppers]];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    NSUserDefaults* prefs = [NSUserDefaults standardUserDefaults];
    [prefs registerDefaults:
     [NSDictionary dictionaryWithObjectsAndKeys: @"", @"data", nil]];
    
    near = @"";
    far = @"";
    
    camera = [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera];
    if (! camera) {
        [self shoot].hidden = YES;
        [self viewfinder].hidden = YES;
    }
    ksteppers = @[@"ev", @"iso", @"shutter", @"aperture", @"focallength", @"distance"];
    klabels = @[@"ev", @"iso", @"shutter", @"aperture", @"focallength", @"distance",
                @"evdesc", @"dof"];
    steppers = [NSArray arrayWithObjects: _evinput, _isoinput, _shutterinput,
                _apertureinput, _lengthinput, _distanceinput, nil];
    labels = [NSArray arrayWithObjects: _ev, _iso, _shutter, _aperture, _length,
                _distance, _evdesc, _dof, nil];
    for (UIStepper* stepper in steppers) {
        [stepper setMinimumValue: -100];
        [stepper setMaximumValue: +100];
        stepper.value = 0;
    }
    
    model = [[JSCModel alloc] initWithObserver: self];
    [model start];
}

- (void) show: (NSString*) name text: (NSString*) text index: (NSNumber*) index
{
    if ([name isEqualToString: @"near"]) {
        near = text;
        [self.dof setText: [NSString stringWithFormat: @"%@ to %@", near, far]];
    } else if ([name isEqualToString: @"far"]) {
        far = text;
        [self.dof setText: [NSString stringWithFormat: @"%@ to %@", near, far]];
    } else {
        UILabel* label = [self glabel: name];
        [label setText: text];
    }
}

- (void) handle: (NSString*) name sender: (UIStepper*) sender
{
    int i = sender.value > 0 ? 1 : (sender.value < 0 ? -1 : 0);
    sender.value = 0;
    [model input: name delta: i];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction) shootSample
{
    if (! camera) {
        return;
    }
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    picker.delegate = self;
    picker.allowsEditing = NO;
    picker.sourceType = UIImagePickerControllerSourceTypeCamera;
    // flash off so we get the truly available light
    picker.cameraFlashMode = UIImagePickerControllerCameraFlashModeOff;
    [self presentViewController:picker animated:YES completion:NULL];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    UIImage *chosenImage = info[UIImagePickerControllerOriginalImage];
    self.viewfinder.image = chosenImage;
    [picker dismissViewControllerAnimated:YES completion:NULL];
    NSDictionary *metaData = [info objectForKey:@"UIImagePickerControllerMediaMetadata"];
    if (metaData) {
        NSDictionary *exif = [metaData objectForKey:@"{Exif}"];
        if (exif) {
            NSString *shutter = [exif objectForKey:@"ExposureTime"];
            NSString *aperture =[exif objectForKey:@"FNumber"];
            NSString *iso = [exif objectForKey:@"ISOSpeedRatings"];
            if (shutter && aperture && iso) {
                [model pictureWithIso: iso aperture: aperture shutter: shutter];
                return;
            }
        }
    }
    NSLog(@"Picture did not have enouth EXIF data");
}

- (NSString*) loadPreferences
{
    NSUserDefaults* prefs = [NSUserDefaults standardUserDefaults];
    return [prefs objectForKey: @"data"];
}


- (void) savePreferences: (NSString*) data
{
    NSUserDefaults* prefs = [NSUserDefaults standardUserDefaults];
    [prefs setObject: data forKey: @"data"];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    
    [picker dismissViewControllerAnimated:YES completion:NULL];
    
}

- (IBAction) evChanged:(id) sender
{
    [self handle: @"ev" sender: sender];
}

- (IBAction) isoChanged:(id) sender
{
    [self handle: @"iso" sender: sender];
}

- (IBAction) shutterChanged:(id) sender
{
    [self handle: @"shutter" sender: sender];
}

- (IBAction) apertureChanged:(id) sender
{
    [self handle: @"aperture" sender: sender];
}

- (IBAction) lengthChanged:(id) sender
{
    [self handle: @"focallength" sender: sender];
}

- (IBAction) distanceChanged:(id) sender
{
    [self handle: @"distance" sender: sender];
}

@end
