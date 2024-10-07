# react-native-ad-manager

A react-native module for Google Ad Manager Banners, Interstitials and Native ads.

The banner types are implemented as components while the interstitial and rewarded video have an imperative API.

Native ads are implemented as wrapper for a native view.

## Installation

You can use npm or Yarn to install the latest version.

**npm:**

```sh
npm i --save react-native-ad-manager
```
**Yarn:**

```sh
yarn add react-native-ad-manager
```

### Mostly automatic installation

If your react native project does not use auto linking, you can link manually by running:

    `$ react-native link react-native-ad-manager`

Alternatively for iOS you can install the library with CocoaPods by adding a line to your `Podfile`;

    pod 'react-native-ad-manager', path: '../node_modules/react-native-ad-manager'

### iOS

For iOS you will have to add the [Google Mobile Ads SDK](https://developers.google.com/ad-manager/mobile-ads-sdk/ios/quick-start#import_the_mobile_ads_sdk) to your Xcode project.

### Android

On Android the Ad Manager library code is part of Play Services, which is automatically added when this library is linked.

But you still have to manually update your `AndroidManifest.xml`, as described in the [Google Mobile Ads SDK documentation](https://developers.google.com/ad-manager/mobile-ads-sdk/android/quick-start#import_the_mobile_ads_sdk).

### GAM

**iOS**

Activate as Ad Manager app by editing your Info.plist
```
+ <key>GADIsAdManagerApp</key>
+ <true/>
```
Add transport security rules in Info.plist
```
<key>NSAppTransportSecurity</key>
<dict>
<key>NSAllowsArbitraryLoads</key>
<true/>
<key>NSAllowsArbitraryLoadsForMedia</key>
<true/>
<key>NSAllowsArbitraryLoadsInWebContent</key>
<true/>
</dict>
```

**Android**

Activate as Ad Manager app
```
<manifest>
  <application>

+   <meta-data android:name="com.google.android.gms.ads.AD_MANAGER_APP" android:value="true"/>

  </application>
</manifest>
```

## Usage

```jsx
import {
  Banner,
  Interstitial,
  PublisherBanner,
  NativeAdsManager,
} from 'react-native-ad-manager'

// Display a DFP Publisher banner
<Banner
  adSize="fullBanner"
  adUnitID="your-ad-unit-id"
  testDevices={[PublisherBanner.simulatorId]}
  onAdFailedToLoad={error => console.error(error)}
  onAppEvent={event => console.log(event.name, event.info)}
/>

// Display an interstitial
Interstitial.setAdUnitID('your-ad-unit-id');
Interstitial.setTestDevices([Interstitial.simulatorId]);
Interstitial.requestAd().then(() => Interstitial.showAd());

// Native ad
import NativeAdView from './NativeAdView';
const adsManager = new NativeAdsManager('your-ad-unit-id', [
    Interstitial.simulatorId,
]);
<NativeAdView
    targeting={{
        customTargeting: {group: 'user_test'},
        categoryExclusions: ['media'],
        contentURL: 'test://',
        publisherProvidedID: 'provider_id',
    }}
    style={{width: '100%'}}
    adsManager={adsManager}
    validAdTypes={['native', 'template']}
    customTemplateIds={['your-template-id-1', 'your-template-id-2']}
    onAdLoaded={ad => {
        console.log(ad);
    }}
    onAdFailedToLoad={error => {
        console.log(error);
    }}
/>
```

See the NativeAdView component in the [example NativeAdView](example/NativeAdView.js).
For a full example reference to the [example project](example).

## Events

| Event                   | Description                                                                         | Output                 |
| ---------------------- |-------------------------------------------------------------------------------------| ---------------------------- |
| onSizeChange           | Called when the size of Ad changes.                                                 | ```{ height: number, type: string, target: number, width: number }``` |
| onAdLoaded             | Called when Ad has finished loading.                                                | ```{ gadSize: { adSize: string, height: number, width: number }, isFluid: string, measurements: { adHeight: number, adWidth: number, height: number, left: number, top: number, width: number }``` |
| onAdFailedToLoad       | Called when Ad request fails.                                                       | Error |
| onAppEvent             | Called when the Ad receives an app event.                                           |                              |
| onAdOpened             | Called when the Ad is opened.                                                       |                              |
| onAdClosed             | Called when the Ad is closed.                                                       |     |
| onAdCustomClick        | Called when the Ad is clicked from a custom template                                | ```{ Attribution: string, Calltoaction: string, DeeplinkUrl: string, assetName: string, label: string, target: number, text: string, title: string }``` |
| onAdRecordImpression   | Called when the Ad records an impression.                                           | ```{ target: number }``` |


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
