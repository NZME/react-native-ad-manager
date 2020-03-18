#import "RNAdManagerBannerViewManager.h"
#import "RNAdManagerBannerView.h"

#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>
#import <React/RCTEventDispatcher.h>

@implementation RNAdManagerBannerViewManager

RCT_EXPORT_MODULE(CTKBannerViewManager)

@synthesize bridge = _bridge;

- (UIView *)view
{
//  if (![EXFacebook facebookAppIdFromNSBundle]) {
//    RCTLogWarn(@"No Facebook app id is specified. Facebook ads may have undefined behavior.");
//  }
  return [RNAdManagerBannerView new];
}

RCT_EXPORT_VIEW_PROPERTY(size, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(placementId, NSString)
RCT_EXPORT_VIEW_PROPERTY(onAdLoad, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdPress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdError, RCTBubblingEventBlock)

RCT_EXPORT_METHOD(loadBanner:(nonnull NSNumber *)reactTag)
{
    [_bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, RNAdManagerBannerView *> *viewRegistry) {
        RNAdManagerBannerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[RNAdManagerBannerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting RNAdManagerBannerView, got: %@", view);
        } else {
            [view loadBanner];
        }
    }];
}

//RCT_REMAP_VIEW_PROPERTY(adSize, _bannerView.adSize, GADAdSize)
//RCT_REMAP_VIEW_PROPERTY(adUnitID, _bannerView.adUnitID, NSString)
RCT_EXPORT_VIEW_PROPERTY(adSize, NSString)
RCT_EXPORT_VIEW_PROPERTY(adUnitID, NSString)
RCT_EXPORT_VIEW_PROPERTY(correlator, NSString)
RCT_EXPORT_VIEW_PROPERTY(validAdSizes, NSArray)
RCT_EXPORT_VIEW_PROPERTY(testDevices, NSArray)
RCT_EXPORT_VIEW_PROPERTY(targeting, NSDictionary)

RCT_EXPORT_VIEW_PROPERTY(onSizeChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAppEvent, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdLoaded, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdFailedToLoad, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdOpened, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdClosed, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdLeftApplication, RCTBubblingEventBlock)

@end
