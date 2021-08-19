#import "RNAdManagerInterstitial.h"
#import "RNAdManagerUtils.h"

#import <React/RCTUtils.h>

static NSString *const kEventAdLoaded = @"interstitialAdLoaded";
static NSString *const kEventAdFailedToLoad = @"interstitialAdFailedToLoad";
static NSString *const kEventAdOpened = @"interstitialAdOpened";
static NSString *const kEventAdFailedToOpen = @"interstitialAdFailedToOpen";
static NSString *const kEventAdClosed = @"interstitialAdClosed";

@implementation RNAdManagerInterstitial
{
    GADInterstitialAd  *_interstitial;
    NSString *_adUnitID;
    NSArray *_testDevices;
    NSDictionary *_targeting;

    RCTPromiseResolveBlock _requestAdResolve;
    RCTPromiseRejectBlock _requestAdReject;
    BOOL hasListeners;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

RCT_EXPORT_MODULE(CTKInterstitial)

- (NSArray<NSString *> *)supportedEvents
{
    return @[
             kEventAdLoaded,
             kEventAdFailedToLoad,
             kEventAdOpened,
             kEventAdFailedToOpen,
             kEventAdClosed ];
}

#pragma mark exported methods

RCT_EXPORT_METHOD(setAdUnitID:(NSString *)adUnitID)
{
    _adUnitID = adUnitID;
}

RCT_EXPORT_METHOD(setTestDevices:(NSArray *)testDevices)
{
    _testDevices = RNAdManagerProcessTestDevices(testDevices, kGADSimulatorID);
}

RCT_EXPORT_METHOD(setTargeting:(NSDictionary *)targeting)
{
    _targeting = targeting;
}

RCT_EXPORT_METHOD(requestAd:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    _requestAdResolve = nil;
    _requestAdReject = nil;

    BOOL hasBeenUsed =  [_interstitial canPresentFromRootViewController:[UIApplication sharedApplication].delegate.window.rootViewController error:nil];
    if (hasBeenUsed || _interstitial == nil) {
        _requestAdResolve = resolve;
        _requestAdReject = reject;

        GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers = _testDevices;
        GAMRequest *request = [GAMRequest request];
        
        if (_targeting != nil) {
            NSDictionary *customTargeting = [_targeting objectForKey:@"customTargeting"];
            if (customTargeting != nil) {
                request.customTargeting = customTargeting;
            }
            NSArray *categoryExclusions = [_targeting objectForKey:@"categoryExclusions"];
            if (categoryExclusions != nil) {
                request.categoryExclusions = categoryExclusions;
            }
            NSArray *keywords = [_targeting objectForKey:@"keywords"];
            if (keywords != nil) {
                request.keywords = keywords;
            }
            NSString *contentURL = [_targeting objectForKey:@"contentURL"];
            if (contentURL != nil) {
                request.contentURL = contentURL;
            }
            NSString *publisherProvidedID = [_targeting objectForKey:@"publisherProvidedID"];
            if (publisherProvidedID != nil) {
                request.publisherProvidedID = publisherProvidedID;
            }
            NSDictionary *location = [_targeting objectForKey:@"location"];
            if (location != nil) {
                CGFloat latitude = [[location objectForKey:@"latitude"] doubleValue];
                CGFloat longitude = [[location objectForKey:@"longitude"] doubleValue];
                CGFloat accuracy = [[location objectForKey:@"accuracy"] doubleValue];
                [request setLocationWithLatitude:latitude longitude:longitude accuracy:accuracy];
            }
        }

        [GADInterstitialAd loadWithAdUnitID:_adUnitID request:request completionHandler:^(GADInterstitialAd * _Nullable interstitialAd, NSError * _Nullable error) {
            if (error) {
                if (hasListeners) {
                    NSDictionary *jsError = RCTJSErrorFromCodeMessageAndNSError(@"E_AD_REQUEST_FAILED", error.localizedDescription, error);
                    [self sendEventWithName:kEventAdFailedToLoad body:jsError];
                }
                _requestAdReject(@"E_AD_REQUEST_FAILED", error.localizedDescription, error);
                _interstitial = nil;
                return;
            }
            
            if (hasListeners) {
                [self sendEventWithName:kEventAdLoaded body:nil];
            }
            _requestAdResolve(nil);
            
            _interstitial = interstitialAd;
            _interstitial.fullScreenContentDelegate = self;
            
        }];
    } else {
        reject(@"E_AD_ALREADY_LOADED", @"Ad is already loaded.", nil);
    }
}

RCT_EXPORT_METHOD(showAd:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    // may not need to check if it is ready..
    // BOOL isReady =  [_interstitial canPresentFromRootViewController:[UIApplication sharedApplication].delegate.window.rootViewController error:nil];
    if (_interstitial) {
        [_interstitial presentFromRootViewController:[UIApplication sharedApplication].delegate.window.rootViewController];
        resolve(nil);
    }
    else {
        reject(@"E_AD_NOT_READY", @"Ad is not ready.", nil);
    }
}

RCT_EXPORT_METHOD(isReady:(RCTResponseSenderBlock)callback)
{
    BOOL isReady =  [_interstitial canPresentFromRootViewController:[UIApplication sharedApplication].delegate.window.rootViewController error:nil];
    callback(@[[NSNumber numberWithBool:isReady]]);
}

- (void)startObserving
{
    hasListeners = YES;
}

- (void)stopObserving
{
    hasListeners = NO;
}

#pragma mark GADFullScreenContentDelegate

- (void)adDidPresentFullScreenContent:(id)ad {
      NSLog(@"Ad did present full screen content.");
    if (hasListeners){
        [self sendEventWithName:kEventAdOpened body:nil];
    }
}


- (void)ad:(id)ad didFailToPresentFullScreenContentWithError:(NSError *)error {
    NSLog(@"Ad failed to present full screen content with error %@.", [error localizedDescription]);
    if (hasListeners) {
        NSDictionary *jsError = RCTJSErrorFromCodeMessageAndNSError(@"E_AD_PRESENT_FAILED", error.localizedDescription, error);
        [self sendEventWithName:kEventAdFailedToOpen body:jsError];
    }
}

- (void)adDidDismissFullScreenContent:(id)ad {
    NSLog(@"Ad did dismiss full screen content.");
    if (hasListeners) {
        [self sendEventWithName:kEventAdClosed body:nil];
    }
}

@end
