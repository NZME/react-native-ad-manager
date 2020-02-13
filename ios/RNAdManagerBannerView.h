#import <React/RCTView.h>
#import <React/RCTComponent.h>

@interface RNAdManagerBannerView : RCTView

@property (nonatomic, copy) NSArray *validAdSizes;
@property (nonatomic, copy) NSArray *testDevices;
@property (nonatomic, copy) NSDictionary *targeting;
@property (nonatomic, copy) NSString *adSize;
@property (nonatomic, strong) NSString *adUnitID;
@property (nonatomic, copy) NSString *correlator;

@property (nonatomic, copy) RCTBubblingEventBlock onSizeChange;
@property (nonatomic, copy) RCTBubblingEventBlock onAppEvent;
@property (nonatomic, copy) RCTBubblingEventBlock onAdLoaded;
@property (nonatomic, copy) RCTBubblingEventBlock onAdFailedToLoad;
@property (nonatomic, copy) RCTBubblingEventBlock onAdOpened;
@property (nonatomic, copy) RCTBubblingEventBlock onAdClosed;
@property (nonatomic, copy) RCTBubblingEventBlock onAdLeftApplication;

- (void)loadBanner;

@end
