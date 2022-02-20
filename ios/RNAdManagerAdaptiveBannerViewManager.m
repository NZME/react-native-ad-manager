#import "RNAdManagerAdaptiveBannerViewManager.h"
#import "RNAdManagerAdaptiveBannerView.h"

#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>
#import <React/RCTEventDispatcher.h>

@implementation RNAdManagerAdaptiveBannerViewManager

RCT_EXPORT_MODULE(CTKAdaptiveBannerViewManager)

@synthesize bridge = _bridge;

- (UIView *)view
{
  return [RNAdManagerAdaptiveBannerView new];
}

RCT_EXPORT_VIEW_PROPERTY(size, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(placementId, NSString)
RCT_EXPORT_VIEW_PROPERTY(onAdLoad, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdPress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdError, RCTBubblingEventBlock)

RCT_EXPORT_METHOD(loadBanner:(nonnull NSNumber *)reactTag)
{
    [_bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, RNAdManagerAdaptiveBannerView *> *viewRegistry) {
        RNAdManagerAdaptiveBannerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[RNAdManagerAdaptiveBannerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting RNAdManagerAdaptiveBannerView, got: %@", view);
        } else {
            [view loadBanner];
        }
    }];
}

RCT_EXPORT_VIEW_PROPERTY(adPosition, NSString)
RCT_EXPORT_VIEW_PROPERTY(maxHeight, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(adUnitID, NSString)
RCT_EXPORT_VIEW_PROPERTY(correlator, NSString)
RCT_EXPORT_VIEW_PROPERTY(testDevices, NSArray)
RCT_EXPORT_VIEW_PROPERTY(targeting, NSDictionary)

RCT_EXPORT_VIEW_PROPERTY(onSizeChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAppEvent, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdLoaded, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdFailedToLoad, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdOpened, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdRecordImpression, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdRecordClick, RCTBubblingEventBlock)

RCT_EXPORT_VIEW_PROPERTY(onAdClosed, RCTBubblingEventBlock)

@end
