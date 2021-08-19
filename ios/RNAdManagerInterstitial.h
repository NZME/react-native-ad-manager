#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#import <GoogleMobileAds/GoogleMobileAds.h>

@interface RNAdManagerInterstitial : RCTEventEmitter <RCTBridgeModule, GADFullScreenContentDelegate>

@end
