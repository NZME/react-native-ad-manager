#import "RNAdManagerInterstitial.h"
#import "RNAdManagerUtils.h"

#import <React/RCTUtils.h>

static NSString *const kEventAdLoaded = @"interstitialAdLoaded";
static NSString *const kEventAdFailedToLoad = @"interstitialAdFailedToLoad";
static NSString *const kEventAdOpened = @"interstitialAdOpened";
static NSString *const kEventAdFailedToOpen = @"interstitialAdFailedToOpen";
static NSString *const kEventAdClosed = @"interstitialAdClosed";
static NSString *const kEventAdLeftApplication = @"interstitialAdLeftApplication";

@implementation RNAdManagerInterstitial
{
    GADInterstitial  *_interstitial;
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
             kEventAdClosed,
             kEventAdLeftApplication ];
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

    if ([_interstitial hasBeenUsed] || _interstitial == nil) {
        _requestAdResolve = resolve;
        _requestAdReject = reject;

        _interstitial = [[GADInterstitial alloc] initWithAdUnitID:_adUnitID];
        _interstitial.delegate = self;

        GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers = _testDevices;
        DFPRequest *request = [DFPRequest request];

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

        [_interstitial loadRequest:request];
    } else {
        reject(@"E_AD_ALREADY_LOADED", @"Ad is already loaded.", nil);
    }
}

RCT_EXPORT_METHOD(showAd:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([_interstitial isReady]) {
        [_interstitial presentFromRootViewController:[UIApplication sharedApplication].delegate.window.rootViewController];
        resolve(nil);
    }
    else {
        reject(@"E_AD_NOT_READY", @"Ad is not ready.", nil);
    }
}

RCT_EXPORT_METHOD(isReady:(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNumber numberWithBool:[_interstitial isReady]]]);
}

- (void)startObserving
{
    hasListeners = YES;
}

- (void)stopObserving
{
    hasListeners = NO;
}

#pragma mark GADInterstitialDelegate

- (void)interstitialDidReceiveAd:(__unused GADInterstitial *)ad
{
    if (hasListeners) {
        [self sendEventWithName:kEventAdLoaded body:nil];
    }
    _requestAdResolve(nil);
}

- (void)interstitial:(__unused GADInterstitial *)interstitial didFailToReceiveAdWithError:(GADRequestError *)error
{
    if (hasListeners) {
        NSDictionary *jsError = RCTJSErrorFromCodeMessageAndNSError(@"E_AD_REQUEST_FAILED", error.localizedDescription, error);
        [self sendEventWithName:kEventAdFailedToLoad body:jsError];
    }
    _requestAdReject(@"E_AD_REQUEST_FAILED", error.localizedDescription, error);
    _interstitial = nil;
}

- (void)interstitialWillPresentScreen:(__unused GADInterstitial *)ad
{
    if (hasListeners){
        [self sendEventWithName:kEventAdOpened body:nil];
    }
}

- (void)interstitialDidFailToPresentScreen:(__unused GADInterstitial *)ad
{
    if (hasListeners){
        [self sendEventWithName:kEventAdFailedToOpen body:nil];
    }
}

- (void)interstitialWillDismissScreen:(__unused GADInterstitial *)ad
{
    if (hasListeners) {
        [self sendEventWithName:kEventAdClosed body:nil];
    }
}

- (void)interstitialWillLeaveApplication:(__unused GADInterstitial *)ad
{
    if (hasListeners) {
        [self sendEventWithName:kEventAdLeftApplication body:nil];
    }
}

@end
