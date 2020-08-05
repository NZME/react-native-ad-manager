#import "RNAdManageNativeManager.h"

#import <React/RCTView.h>
#import <React/RCTBridge.h>
#import <React/RCTComponent.h>

#import <GoogleMobileAds/GoogleMobileAds.h>

@class RCTEventDispatcher;

@interface RNAdManageNativeView : RCTView <GADUnifiedNativeAdLoaderDelegate, GADUnifiedNativeAdDelegate, DFPBannerAdLoaderDelegate, GADNativeCustomTemplateAdLoaderDelegate, GADVideoControllerDelegate>

/// You must keep a strong reference to the GADAdLoader during the ad loading process.
@property(nonatomic, strong) IBOutlet GADAdLoader *adLoader;

/// The native ad that is being loaded.
@property(nonatomic, strong) IBOutlet GADUnifiedNativeAd *nativeAd;

/// The native ad view
@property(nonatomic, strong) IBOutlet GADUnifiedNativeAdView *nativeAdView;

/// The DFP banner view.
@property(nonatomic, strong) IBOutlet DFPBannerView *bannerView;

/// The native custom template ad
@property(nonatomic, strong) IBOutlet GADNativeCustomTemplateAd *nativeCustomTemplateAd;

@property (nonatomic, copy) NSString *loaderIndex;
@property (nonatomic, copy) NSArray *customTemplateIds;
@property (nonatomic, copy) NSArray *validAdTypes;
@property (nonatomic, copy) NSString *adSize;
@property (nonatomic, copy) NSArray *validAdSizes;
@property (nonatomic, copy) NSDictionary *targeting;
@property (nonatomic, copy) NSString *correlator;

@property (nonatomic, copy) RCTBubblingEventBlock onSizeChange;
@property (nonatomic, copy) RCTBubblingEventBlock onAppEvent;
@property (nonatomic, copy) RCTBubblingEventBlock onAdLoaded;
@property (nonatomic, copy) RCTBubblingEventBlock onAdFailedToLoad;
@property (nonatomic, copy) RCTBubblingEventBlock onAdOpened;
@property (nonatomic, copy) RCTBubblingEventBlock onAdClosed;
@property (nonatomic, copy) RCTBubblingEventBlock onAdLeftApplication;

- (instancetype)initWithBridge:(RCTBridge *)bridge;
- (void)registerViewsForInteraction:(NSArray<UIView *> *)clickableViews;
- (void)reloadAd;
- (void)loadAd:(RNAdManageNativeManager *)adManager;

@end
