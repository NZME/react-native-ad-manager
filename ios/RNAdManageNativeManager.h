#import <React/RCTViewManager.h>
#import <GoogleMobileAds/GoogleMobileAds.h>

@interface RNAdManageNativeManager : RCTViewManager

@property (strong, nonatomic) NSString *adUnitID;
@property (strong, nonatomic) NSArray *testDevices;

- (RNAdManageNativeManager *) getAdsManager:(NSString *)adUnitID;
- (GADAdLoader *) getAdLoader:(NSString *)adUnitID validAdTypes:(NSArray *)validAdTypes loaderIndex:(NSString *)loaderIndex;

@end
