#import "RNAdManagerAdaptiveBannerView.h"

#import <GoogleMobileAds/GoogleMobileAds.h>
#import <React/RCTUtils.h>

#import <React/RCTLog.h>

#import "RNAdManagerUtils.h"

@interface RNAdManagerAdaptiveBannerView () <GADBannerViewDelegate, GADAdSizeDelegate, GADAppEventDelegate>

@property (nonatomic, strong) GAMBannerView *bannerView;

@end

@implementation RNAdManagerAdaptiveBannerView
{
    BOOL _isFrameLayout;
    BOOL _loadOnFrameLayout;
}

- (void)dealloc
{

    _bannerView.delegate = nil;
    _bannerView.adSizeDelegate = nil;
    _bannerView.appEventDelegate = nil;
    _bannerView.rootViewController = nil;
}

- (void)setAdUnitID:(NSString *)adUnitID
{
  _adUnitID = adUnitID;
}

- (void)setAdPosition:(NSString *)adPosition
{
  _adPosition = adPosition;
}

- (void)setMaxHeight:(NSNumber *)maxHeight
{
  _maxHeight = maxHeight;
}

- (void)setTargeting:(NSDictionary *)targeting
{
  _targeting = targeting;
}

- (void)setCorrelator:(NSString *)correlator
{
  _correlator = correlator;
}

// Initialise BannerAdView as soon as all the props are set
- (void)createViewIfCan
{
    if (!_adUnitID) {
        return;
    }

    if (_bannerView) {
        [_bannerView removeFromSuperview];
    }
    CGRect frame = self.frame;
    // Here safe area is taken into account, hence the view frame is used after the
    // view has been laid out.
    if (@available(iOS 11.0, *)) {
      frame = UIEdgeInsetsInsetRect(self.frame, self.safeAreaInsets);
    }
    CGFloat viewWidth = frame.size.width;
    // Here the current interface orientation is used. If the ad is being preloaded
    // for a future orientation change or different orientation, the function for the
    // relevant orientation should be used.
    GADAdSize adSize;
    if ([_adPosition isEqualToString:@"currentOrientationAnchored"]) {
        adSize = GADCurrentOrientationAnchoredAdaptiveBannerAdSizeWithWidth(viewWidth);
    } else if ([_adPosition isEqualToString:@"currentOrientationInline"]) {
        adSize = GADPortraitInlineAdaptiveBannerAdSizeWithWidth(viewWidth);
    } else if ([_adPosition isEqualToString:@"portraitInline"]) {
        adSize = GADPortraitInlineAdaptiveBannerAdSizeWithWidth(viewWidth);
    } else if ([_adPosition isEqualToString:@"landscapeInline"]) {
        adSize = GADLandscapeInlineAdaptiveBannerAdSizeWithWidth(viewWidth);
    } else { // _adPosition == "inline"
        CGFloat adMaxHeight;
        if (_maxHeight == nil) {
            CGFloat viewHeight = frame.size.height;
            if (viewHeight == 0) {
                viewHeight = 350;
            }
            adMaxHeight = viewHeight;
        } else {
            adMaxHeight = [_maxHeight doubleValue];
        }
        adSize = GADInlineAdaptiveBannerAdSizeWithWidthAndMaxHeight(viewWidth, adMaxHeight);
    }
    GAMBannerView *bannerView = [[GAMBannerView alloc] initWithAdSize:adSize];

    bannerView.delegate = self;
    bannerView.adSizeDelegate = self;
    bannerView.appEventDelegate = self;
    bannerView.rootViewController = RCTPresentedViewController();
    bannerView.translatesAutoresizingMaskIntoConstraints = YES;

    GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers = _testDevices;
    GAMRequest *request = [GAMRequest request];

    GADExtras *extras = [[GADExtras alloc] init];
    if (_correlator == nil) {
        _correlator = getCorrelator(_adUnitID);
    }
    extras.additionalParameters = [[NSDictionary alloc] initWithObjectsAndKeys:
                                   _correlator, @"correlator",
                                   nil];
    [request registerAdNetworkExtras:extras];

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
        NSString *content_url = [_targeting objectForKey:@"content_url"];
        if (content_url != nil) {
            request.contentURL = content_url;
        }
        NSString *publisherProvidedID = [_targeting objectForKey:@"publisherProvidedID"];
        if (publisherProvidedID != nil) {
            request.publisherProvidedID = publisherProvidedID;
        }
    }

    bannerView.adUnitID = _adUnitID;
    bannerView.adSize = adSize;

    [bannerView loadRequest:request];

    [self addSubview:bannerView];

    _bannerView = bannerView;
}

- (void)loadBanner {
    if (_isFrameLayout) {
        [self createViewIfCan];
    } else {
        _loadOnFrameLayout = YES;
    }
}

-(void)layoutSubviews
{
    [super layoutSubviews];
    if (!_isFrameLayout) {
        _isFrameLayout = YES;
        if (_loadOnFrameLayout) {
            _loadOnFrameLayout = NO;
            [self createViewIfCan];
        }
    }
}

# pragma mark GADBannerViewDelegate

/// Tells the delegate an ad request loaded an ad.
- (void)bannerViewDidReceiveAd:(nonnull GADBannerView *)bannerView
{
    if (self.onSizeChange) {
        self.onSizeChange(@{
                            @"type": @"banner",
                            @"width": @(bannerView.frame.size.width),
                            @"height": @(bannerView.frame.size.height) });
    }
    if (self.onAdLoaded) {
        self.onAdLoaded(@{
            @"type": @"banner",
            @"gadSize": @{@"adSize": NSStringFromGADAdSize(bannerView.adSize),
                          @"width": @(bannerView.frame.size.width),
                          @"height": @(bannerView.frame.size.height)},
            @"isFluid": @"false",
            @"measurements": @{@"adWidth": @(bannerView.adSize.size.width),
                               @"adHeight": @(bannerView.adSize.size.height),
                               @"width": @(bannerView.frame.size.width),
                               @"height": @(bannerView.frame.size.height),
                               @"left": @(bannerView.frame.origin.x),
                               @"top": @(bannerView.frame.origin.y)},
        });
    }
}

/// Tells the delegate an ad request failed.
- (void)bannerView:(nonnull GADBannerView *)bannerView
    didFailToReceiveAdWithError:(nonnull NSError *)error
{
    if (self.onAdFailedToLoad) {
        self.onAdFailedToLoad(@{ @"error": @{ @"message": [error localizedDescription] } });
    }
    _bannerView.delegate = nil;
    _bannerView.adSizeDelegate = nil;
    _bannerView.appEventDelegate = nil;
    _bannerView.rootViewController = nil;
    _bannerView = nil;
}

- (void)bannerViewDidRecordImpression:(nonnull GADBannerView *)bannerView
{
    if (self.onAdRecordImpression) {
        self.onAdRecordImpression(@{});
    }
}

- (void)bannerViewDidRecordClick:(nonnull GADBannerView *)bannerView
{
    if (self.onAdRecordClick) {
        self.onAdRecordClick(@{});
    }
}

/// Tells the delegate that a full screen view will be presented in response
/// to the user clicking on an ad.
- (void)bannerViewWillPresentScreen:(nonnull GADBannerView *)bannerView
{
    if (self.onAdOpened) {
        self.onAdOpened(@{});
    }
}

/// Tells the delegate that the full screen view will be dismissed.
- (void)bannerViewWillDismissScreen:(nonnull GADBannerView *)bannerView
{
    if (self.onAdClosed) {
        self.onAdClosed(@{});
    }
}

# pragma mark GADAdSizeDelegate

- (void)adView:(GADBannerView *)bannerView willChangeAdSizeTo:(GADAdSize)size
{
    CGSize adSize = CGSizeFromGADAdSize(size);
    self.onSizeChange(@{
                        @"type": @"banner",
                        @"width": @(adSize.width),
                        @"height": @(adSize.height) });
}

# pragma mark GADAppEventDelegate

- (void)adView:(GADBannerView *)banner didReceiveAppEvent:(NSString *)name withInfo:(NSString *)info
{
    if (self.onAppEvent) {
        self.onAppEvent(@{ @"name": name, @"info": info });
    }
}

@end
