#import "RNAdManageNativeView.h"
#import "RNAdManageNativeManager.h"
#import "RNAdManagerUtils.h"
#import <React/RCTConvert.h>
#import <React/RCTUtils.h>

#import <React/RCTBridgeModule.h>
#import <React/UIView+React.h>
#import <React/RCTLog.h>
#import <MetaAdapter/MetaAdapter.h>

#include "RCTConvert+GADAdSize.h"
#import "RNAdManagerUtils.h"

static NSString *const kAdTypeBanner = @"banner";
static NSString *const kAdTypeNative = @"native";
static NSString *const kAdTypeTemplate = @"template";

@interface RNAdManageNativeView ()

@property (nonatomic, strong) RCTBridge *bridge;

@end

@implementation RNAdManageNativeView
{
    NSString *_nativeCustomTemplateAdClickableAsset;
    NSString *_adUnitID;
    NSArray *_testDevices;
}

- (instancetype)initWithBridge:(RCTBridge *)bridge
{
  if (self = [super init]) {
    _bridge = bridge;
  }
  return self;
}

- (void)dealloc
{
    self.adLoader.delegate = nil;
    self.adLoader = nil;
    self.nativeAd.delegate = nil;
    self.nativeAd = nil;

    self.nativeAdView = nil;

    self.bannerView.delegate = nil;
    self.bannerView.adSizeDelegate = nil;
    self.bannerView.appEventDelegate = nil;
    self.bannerView.rootViewController = nil;
    self.bannerView = nil;
}

-(void)layoutSubviews
{
    [super layoutSubviews];
    self.nativeAdView.frame = self.bounds;
    self.bannerView.frame = self.bounds;
}

- (void)loadAd:(RNAdManageNativeManager *)adManager {
    _adUnitID = adManager.adUnitID;
    _testDevices = adManager.testDevices;

    if (_validAdTypes == nil) {
        _validAdTypes = @[
            kAdTypeBanner,
            kAdTypeNative,
            kAdTypeTemplate
        ];
    }

    self.adLoader = [adManager getAdLoader:_adUnitID validAdTypes:_validAdTypes loaderIndex:_loaderIndex];
    self.adLoader.delegate = self;

    GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers = _testDevices;
//    DFPRequest *request = [DFPRequest request];
//
//    GADExtras *extras = [[GADExtras alloc] init];
//    if (_correlator == nil) {
//        _correlator = getCorrelator(_adUnitID);
//    }
//    extras.additionalParameters = [[NSDictionary alloc] initWithObjectsAndKeys:
//                                   _correlator, @"correlator",
//                                   nil];
//    [request registerAdNetworkExtras:extras];
//
//    if (_targeting != nil) {
//        NSDictionary *customTargeting = [_targeting objectForKey:@"customTargeting"];
//        if (customTargeting != nil) {
//            request.customTargeting = customTargeting;
//        }
//        NSArray *categoryExclusions = [_targeting objectForKey:@"categoryExclusions"];
//        if (categoryExclusions != nil) {
//            request.categoryExclusions = categoryExclusions;
//        }
//        NSArray *keywords = [_targeting objectForKey:@"keywords"];
//        if (keywords != nil) {
//            request.keywords = keywords;
//        }
//        NSString *contentURL = [_targeting objectForKey:@"contentURL"];
//        if (contentURL != nil) {
//            request.contentURL = contentURL;
//        }
//        NSString *publisherProvidedID = [_targeting objectForKey:@"publisherProvidedID"];
//        if (publisherProvidedID != nil) {
//            request.publisherProvidedID = publisherProvidedID;
//        }
//        NSDictionary *location = [_targeting objectForKey:@"location"];
//        if (location != nil) {
//            CGFloat latitude = [[location objectForKey:@"latitude"] doubleValue];
//            CGFloat longitude = [[location objectForKey:@"longitude"] doubleValue];
//            CGFloat accuracy = [[location objectForKey:@"accuracy"] doubleValue];
//            [request setLocationWithLatitude:latitude longitude:longitude accuracy:accuracy];
//        }
//    }
//
//    @try {
//        [self.adLoader loadRequest:request];
//    }
//    @catch ( NSException *e ) {
//        if (self.onAdFailedToLoad) {
//            self.onAdFailedToLoad(@{ @"error": @{ @"message": [e.userInfo valueForKey:NSLocalizedDescriptionKey] } });
//        }
//    }
}

- (void)reloadAd {
    if (self.adLoader == nil) {
        return;
    }

    GAMRequest *request = [GAMRequest request];

    // Meta Audience network
    GADFBNetworkExtras * fbExtras = [[GADFBNetworkExtras alloc] init];
    fbExtras.nativeAdFormat = GADFBAdFormatNativeBanner;
    [request registerAdNetworkExtras:fbExtras];

    GADExtras *extras = [[GADExtras alloc] init];
    if (_correlator == nil) {
        _correlator = getCorrelator(_adUnitID);
    }
    
    NSMutableDictionary *additionalParams = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                             _correlator, @"correlator",
                                             nil];
    if (_servePersonalizedAds == NO) {
        [additionalParams setObject:[NSNumber numberWithInt:1] forKey:@"npa"];
    }

    if (_allowDataProcessing == NO) {
        [additionalParams setObject:[NSNumber numberWithInt:1] forKey:@"rdp"];
    }
    
    // Set the dictionary to extras.additionalParameters
    extras.additionalParameters = [NSDictionary dictionaryWithDictionary:additionalParams];
    
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
//        NSDictionary *location = [_targeting objectForKey:@"location"];
//        if (location != nil) {
//            CGFloat latitude = [[location objectForKey:@"latitude"] doubleValue];
//            CGFloat longitude = [[location objectForKey:@"longitude"] doubleValue];
//            CGFloat accuracy = [[location objectForKey:@"accuracy"] doubleValue];
//            [request setLocationWithLatitude:latitude longitude:longitude accuracy:accuracy];
//        }
    }

    @try {
        [self.adLoader loadRequest:request];
    }
    @catch ( NSException *e ) {
        if (self.onAdFailedToLoad) {
            self.onAdFailedToLoad(@{ @"error": @{ @"message": [e.userInfo valueForKey:NSLocalizedDescriptionKey] } });
        }
    }
}

- (void)setCustomTemplateIds:(NSArray *)customTemplateIds
{
    _customTemplateIds = customTemplateIds;
}

- (void)setCustomClickTemplateIds:(NSArray *)customClickTemplateIds
{
    _customClickTemplateIds = customClickTemplateIds;
}

- (void)setValidAdTypes:(NSArray *)adTypes
{
    __block NSMutableArray *validAdTypes = [[NSMutableArray alloc] initWithCapacity:adTypes.count];
    [adTypes enumerateObjectsUsingBlock:^(id jsonValue, NSUInteger idx, __unused BOOL *stop) {
        [validAdTypes addObject:[RCTConvert NSString:jsonValue]];
    }];
    _validAdTypes = validAdTypes;
}

- (void)setAdSize:(NSString *)adSize
{
    _adSize = adSize;
}

- (void)setLoaderIndex:(NSString *)loaderIndex
{
    _loaderIndex = loaderIndex;
}

- (void)setValidAdSizes:(NSArray *)adSizes
{
    __block NSMutableArray *validAdSizes = [[NSMutableArray alloc] initWithCapacity:adSizes.count];
    [adSizes enumerateObjectsUsingBlock:^(id jsonValue, NSUInteger idx, __unused BOOL *stop) {
        GADAdSize adSize = [RCTConvert GADAdSize:jsonValue];
        if (GADAdSizeEqualToSize(adSize, GADAdSizeInvalid)) {
            RCTLogWarn(@"Invalid adSize %@", jsonValue);
        } else if (![validAdSizes containsObject:NSValueFromGADAdSize(adSize)]) {
            [validAdSizes addObject:NSValueFromGADAdSize(adSize)];
        }
    }];
    _validAdSizes = validAdSizes;
}

- (void)setCorrelator:(NSString *)correlator
{
    _correlator = correlator;
}

- (void)setServePersonalizedAds:(BOOL)servePersonalizedAds
{
  _servePersonalizedAds = servePersonalizedAds;
}

- (void)setAllowDataProcessing:(BOOL)allowDataProcessing
{
  _allowDataProcessing = allowDataProcessing;
}


#pragma mark GADAdLoaderDelegate implementation

/// Tells the delegate an ad request failed.UnifiedNativeAdView
- (void)adLoader:(nonnull GADAdLoader *)adLoader didFailToReceiveAdWithError:(nonnull NSError *)error {
    if (self.onAdFailedToLoad) {
        self.onAdFailedToLoad(@{ @"error": @{ @"message": [error localizedDescription] } });
    }

    self.nativeAdView = nil;


    if (self.bannerView != nil) {
        self.bannerView.delegate = nil;
        self.bannerView.adSizeDelegate = nil;
        self.bannerView.appEventDelegate = nil;
        self.bannerView.rootViewController = nil;
        self.bannerView = nil;
    }

    self.nativeCustomTemplateAd = nil;

    if (self.adLoader != nil) {
        self.adLoader.delegate = nil;
        self.adLoader = nil;
    }

    if (self.nativeAd != nil) {
        self.nativeAd.delegate = nil;
        self.nativeAd = nil;
    }
}

#pragma mark GADNativeAdLoaderDelegate implementation

- (void)adLoader:(GADAdLoader *)adLoader didReceiveNativeAd:(GADNativeAd *)nativeAd {
    [self.bannerView removeFromSuperview];
    [self.nativeAdView removeFromSuperview];

    GADNativeAdView *nativeAdView = [[GADNativeAdView alloc] init];
    self.nativeAdView = nativeAdView;

    if (self.frame.size.width <= 0 || self.frame.size.height <= 0) {
        CGFloat width = self.frame.size.width;
        if (width <= 0) {
            width = 32;
        }
        CGFloat height = self.frame.size.height;
        if (height <= 0) {
            height = 32;
        }
        [self setFrame:CGRectMake(self.frame.origin.x, self.frame.origin.y, width, height)];
    }
    self.nativeAdView.frame = self.bounds;
    self.nativeAdView.translatesAutoresizingMaskIntoConstraints = NO;
    self.nativeAdView.contentMode = UIViewContentModeScaleAspectFit;
    self.nativeAdView.clipsToBounds = YES;

    self.nativeAd = nativeAd;
    self.nativeAdView.nativeAd = _nativeAd;

    [self addSubview:self.nativeAdView];

    // Set ourselves as the ad delegate to be notified of native ad events.
    self.nativeAd.delegate = self;

    [self triggerAdLoadedEvent:self.nativeAd];

    if (self.bannerView != nil) {
        self.bannerView.delegate = nil;
        self.bannerView.adSizeDelegate = nil;
        self.bannerView.appEventDelegate = nil;
        self.bannerView.rootViewController = nil;
        self.bannerView = nil;
    }

    self.nativeCustomTemplateAd = nil;

    if (self.adLoader != nil) {
        self.adLoader.delegate = nil;
        self.adLoader = nil;
    }
}

- (void)triggerAdLoadedEvent:(GADNativeAd *)nativeAd {
    if (self.onAdLoaded) {
        NSMutableDictionary *ad = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                   kAdTypeNative, @"type",
                                   nativeAd.headline, @"headline",
                                   nativeAd.body, @"bodyText",
                                   nativeAd.callToAction, @"callToActionText",
                                   nativeAd.advertiser, @"advertiserName",
                                   nativeAd.starRating, @"starRating",
                                   nativeAd.store, @"storeName",
                                   nativeAd.price, @"price",
                                   nil, @"icon",
                                   nil, @"images",
                                   nil];

        NSString *socialContext = nativeAd.extraAssets[GADFBSocialContext];
        if (socialContext != nil) {
            ad[@"socialContext"] = socialContext;
        }

        if (nativeAd.icon != nil) {
            ad[@"icon"] = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                nativeAd.icon.imageURL.absoluteString, @"uri",
                [[NSNumber numberWithFloat:nativeAd.icon.image.size.width] stringValue], @"width",
                [[NSNumber numberWithFloat:nativeAd.icon.image.size.height] stringValue], @"height",
                [[NSNumber numberWithFloat:nativeAd.icon.scale] stringValue], @"scale",
                nil];
        }

        if (nativeAd.images != nil) {
            NSMutableArray *images = [[NSMutableArray alloc] init];
            [nativeAd.images enumerateObjectsUsingBlock:^(GADNativeAdImage *value, NSUInteger idx, __unused BOOL *stop) {
                [images addObject:[[NSMutableDictionary alloc] initWithObjectsAndKeys:
                    value.imageURL.absoluteString, @"uri",
                    [[NSNumber numberWithFloat:value.image.size.width] stringValue], @"width",
                    [[NSNumber numberWithFloat:value.image.size.height] stringValue], @"height",
                    [[NSNumber numberWithFloat:value.scale] stringValue], @"scale",
                    nil]];
            }];
            ad[@"images"] = images;
        }

        self.onAdLoaded(ad);
    }
}

- (void)triggerAdCustomClickEvent:(nonnull NSString *)assetID {
    if (self.onAdCustomClick) {
        NSMutableDictionary *ad = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                   assetID, @"assetName",
                                   nil];

        [self.nativeCustomTemplateAd.availableAssetKeys enumerateObjectsUsingBlock:^(NSString *value, NSUInteger idx, __unused BOOL *stop) {
            if ([self.nativeCustomTemplateAd stringForKey:value] != nil) {
                NSString *assetVal = [self.nativeCustomTemplateAd stringForKey:value];
                ad[value] = assetVal;
            }
        }];

        self.onAdCustomClick(ad);
    }
}

#pragma mark GAMBannerAdLoaderDelegate implementation

- (nonnull NSArray<NSValue *> *)validBannerSizesForAdLoader:(nonnull GADAdLoader *)adLoader {
    NSMutableArray *validAdSizes = [NSMutableArray arrayWithArray:_validAdSizes];
    if (_adSize != nil) {
        GADAdSize adSize = [RCTConvert GADAdSize:_adSize];
        if (GADAdSizeEqualToSize(adSize, GADAdSizeInvalid)) {
            RCTLogWarn(@"Invalid adSize %@", _adSize);
        } else if (![validAdSizes containsObject:NSValueFromGADAdSize(adSize)]) {
            [validAdSizes addObject:NSValueFromGADAdSize(adSize)];
        }
    }
    return validAdSizes;
}

- (void)adLoader:(nonnull GADAdLoader *)adLoader didReceiveGAMBannerView:(nonnull GAMBannerView *)bannerView {
    [self.bannerView removeFromSuperview];
    [self.nativeAdView removeFromSuperview];
    self.bannerView = bannerView;
    self.bannerView.translatesAutoresizingMaskIntoConstraints = YES;

    [self addSubview:self.bannerView];

//    bannerView.delegate = self;
//    bannerView.adSizeDelegate = self;
//    bannerView.appEventDelegate = self;

    if (self.onSizeChange) {
        self.onSizeChange(@{
                            @"type": kAdTypeBanner,
                            @"width": @(self.bannerView.frame.size.width),
                            @"height": @(self.bannerView.frame.size.height) });
    }

    if (self.onAdLoaded) {
        self.onAdLoaded(@{
            @"type": kAdTypeBanner,
            @"gadSize": @{@"adSize": NSStringFromGADAdSize(self.bannerView.adSize),
                          @"width": @(self.bannerView.frame.size.width),
                          @"height": @(self.bannerView.frame.size.height)},
            @"isFluid": GADAdSizeIsFluid(self.bannerView.adSize) ? @"true" : @"false",
            @"measurements": @{@"adWidth": @(self.bannerView.adSize.size.width),
                               @"adHeight": @(self.bannerView.adSize.size.height),
                               @"width": @(self.bannerView.frame.size.width),
                               @"height": @(self.bannerView.frame.size.height),
                               @"left": @(self.bannerView.frame.origin.x),
                               @"top": @(self.bannerView.frame.origin.y)},
        });
    }

    self.nativeAdView = nil;

    self.nativeCustomTemplateAd = nil;

    if (self.adLoader != nil) {
        self.adLoader.delegate = nil;
        self.adLoader = nil;
    }

    if (self.nativeAd != nil) {
        self.nativeAd.delegate = nil;
        self.nativeAd = nil;
    }
}

#pragma mark GADCustomNativeAdLoaderDelegate implementation

- (void)adLoader:(nonnull GADAdLoader *)adLoader didReceiveCustomNativeAd:(nonnull GADCustomNativeAd *)customNativeAd {
    [self.bannerView removeFromSuperview];
    [self.nativeAdView removeFromSuperview];

    self.nativeCustomTemplateAd = customNativeAd;

    if (self.customClickTemplateIds != nil && [self.customClickTemplateIds containsObject:customNativeAd.formatID]) {
        [self.nativeCustomTemplateAd setCustomClickHandler:^(NSString *assetID){
            [self triggerAdCustomClickEvent:assetID];
        }];
    }

    [self triggerCustomAdLoadedEvent:self.nativeCustomTemplateAd];

    [self.nativeCustomTemplateAd recordImpression];

    self.nativeAdView = nil;

    if (self.bannerView != nil) {
        self.bannerView.delegate = nil;
        self.bannerView.adSizeDelegate = nil;
        self.bannerView.appEventDelegate = nil;
        self.bannerView.rootViewController = nil;
        self.bannerView = nil;
    }

    if (self.adLoader != nil) {
        self.adLoader.delegate = nil;
        self.adLoader = nil;
    }

    if (self.nativeAd != nil) {
        self.nativeAd.delegate = nil;
        self.nativeAd = nil;
    }
}

- (nonnull NSArray<NSString *> *)customNativeAdFormatIDsForAdLoader:(nonnull GADAdLoader *)adLoader {
    if (_customTemplateIds == nil) {
        _customTemplateIds = @[ @"11891103" ];
    }

    return _customTemplateIds;
}

- (void)triggerCustomAdLoadedEvent:(GADCustomNativeAd *)nativeCustomTemplateAd {
    if (self.onAdLoaded) {
        NSMutableDictionary *ad = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                   kAdTypeTemplate, @"type",
                                   nativeCustomTemplateAd.formatID, @"templateID",
                                   nil];

        [nativeCustomTemplateAd.availableAssetKeys enumerateObjectsUsingBlock:^(NSString *value, NSUInteger idx, __unused BOOL *stop) {
            if ([nativeCustomTemplateAd stringForKey:value] != nil) {
                NSString *assetVal = [nativeCustomTemplateAd stringForKey:value];
                if (_nativeCustomTemplateAdClickableAsset == nil && assetVal.length > 2) {
                    _nativeCustomTemplateAdClickableAsset = value;
                }
                ad[value] = assetVal;
            } else if ([nativeCustomTemplateAd imageForKey:value] != nil) {
                GADNativeAdImage *image = [nativeCustomTemplateAd imageForKey:value];
                ad[value] = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                             image.imageURL.absoluteString, @"uri",
                             [[NSNumber numberWithFloat:image.image.size.width] stringValue], @"width",
                             [[NSNumber numberWithFloat:image.image.size.height] stringValue], @"height",
                             [[NSNumber numberWithFloat:image.scale] stringValue], @"scale",
                             nil];
            }
        }];

        self.onAdLoaded(ad);
    }
}

#pragma mark GADVideoControllerDelegate implementation

//- (void)videoControllerDidEndVideoPlayback:(GADVideoController *)videoController {
//    NSLog(@"%s", __PRETTY_FUNCTION__);
//}

#pragma mark GADNativeAdDelegate

//- (void)nativeAdDidRecordClick:(nonnull GADNativeAd *)nativeAd {
//    NSLog(@"%s", __PRETTY_FUNCTION__);
//}

- (void)nativeAdDidRecordImpression:(nonnull GADNativeAd *)nativeAd {
    if (self.onAdRecordImpression) {
      self.onAdRecordImpression(@{});
    }
}

- (void)nativeAdWillPresentScreen:(nonnull GADNativeAd *)nativeAd {
    if (self.onAdOpened) {
        self.onAdOpened(@{});
    }
}

//- (void)nativeAdWillDismissScreen:(nonnull GADNativeAd *)nativeAd {
//    NSLog(@"%s", __PRETTY_FUNCTION__);
//}

- (void)nativeAdDidDismissScreen:(nonnull GADNativeAd *)nativeAd {
    if (self.onAdClosed) {
        self.onAdClosed(@{});
    }
}

- (void)registerViewsForInteraction:(NSArray<UIView *> *)clickableViews {
    if (self.nativeCustomTemplateAd != nil && _nativeCustomTemplateAdClickableAsset != nil) {
        [clickableViews enumerateObjectsUsingBlock:^(UIView *view, NSUInteger idx, __unused BOOL *stop) {
            [view addGestureRecognizer:[[UITapGestureRecognizer alloc]
                                        initWithTarget:self
                                        action:@selector(performClickOnCustomAd)]];
            view.userInteractionEnabled = YES;
        }];
    } else if (self.nativeAdView != nil) {
        [clickableViews enumerateObjectsUsingBlock:^(UIView *view, NSUInteger idx, __unused BOOL *stop) {
            [view removeFromSuperview];
            view.userInteractionEnabled = NO;
            [self.nativeAdView addSubview:view];
            self.nativeAdView.callToActionView = view;
        }];
        // a HACK to get the overlay to display on the top
        for (UIView *subview in self.nativeAdView.subviews)
        {
            if ([NSStringFromClass([subview class]) isEqual:@"GADNativeAdAttributionView"]) {
//                [subview setFrame:CGRectMake(0, 0,  subview.frame.size.width,  subview.frame.size.height)];
                [subview setFrame:CGRectMake(0, 0, 20, 20)];
                [subview removeFromSuperview];
                [self.nativeAdView addSubview:subview];
            }
        }
    }
}

- (void)performClickOnCustomAd {
    if (self.nativeCustomTemplateAd != nil) {
        [self.nativeCustomTemplateAd performClickOnAssetWithKey:_nativeCustomTemplateAdClickableAsset];
    }
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wobjc-missing-super-calls"
- (void)insertReactSubview:(UIView *)subview atIndex:(NSInteger)atIndex
{
    if (self.nativeAdView != nil) {
        [subview removeFromSuperview];
        subview.userInteractionEnabled = NO;
        [self.nativeAdView addSubview:subview];
        self.nativeAdView.callToActionView = subview;
    } else {
        [super insertReactSubview:subview atIndex:atIndex];
    }
}
#pragma clang diagnostic pop

@end
