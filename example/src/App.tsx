import React from 'react';
import {
  Button,
  Platform,
  ScrollView,
  StyleSheet,
  View,
  RefreshControl,
  ViewProps,
  Dimensions,
} from 'react-native';
import {
  Interstitial,
  Banner,
  NativeAdsManager,
  IAdManagerEventLoadedBanner,
  IAdManagerEventLoadedTemplate,
  IAdManagerEventLoadedNative,
} from 'react-native-ad-manager';
import { BannerExample } from './BannerExample';
import NativeAdView from './NativeAdView';

const TEST_AD_NATIVE = '/6499/example/native';
const TEST_AD_INTERSTITIAL = '/6499/example/interstitial';
const TEST_AD_BANNER = '/6499/example/banner';
const TEST_AD_BANNER_FLUID = '/6499/example/APIDemo/Fluid';
const TEST_AD_TEMPLATE = '/6499/example/native';
const TEST_AD_TEMPLATE_ID = '10104090';

type TAdsListType =
  | 'banner'
  | 'banner-320x50'
  | 'banner-fluid'
  | 'native'
  | 'native-template'
  | 'native-banner';

interface IAdsListElement {
  adType: TAdsListType;
}

interface IExampleProps extends ViewProps {}

interface IExampleState {
  adsList: IAdsListElement[];
  refreshingScrollView: boolean;
}

export default class Example extends React.Component<
  IExampleProps,
  IExampleState
> {
  constructor(props: IExampleProps) {
    super(props);
    this.state = {
      adsList: [],
      refreshingScrollView: false,
    };
  }

  componentDidMount() {
    Interstitial.setTestDevices([Interstitial.simulatorId]);
    Interstitial.setAdUnitID(TEST_AD_INTERSTITIAL);

    Interstitial.addEventListener('adLoaded', () =>
      console.log('Interstitial adLoaded')
    );
    Interstitial.addEventListener('adFailedToLoad', (error) =>
      console.warn(error)
    );
    Interstitial.addEventListener('adOpened', () =>
      console.log('Interstitial => adOpened')
    );
    Interstitial.addEventListener('adClosed', () => {
      console.log('Interstitial => adClosed');
      Interstitial.requestAd().catch((error) => console.warn(error));
    });

    Interstitial.requestAd().catch((error) => console.warn(error));

    // const adsList = [{type: 'banner'}];
    // this.setState({adsList: adsList});
  }

  componentWillUnmount() {
    Interstitial.removeAllListeners();
  }

  showInterstitial() {
    Interstitial.showAd().catch((error) => console.warn(error));
  }

  onAdLoaded = (
    nativeAd:
      | IAdManagerEventLoadedBanner
      | IAdManagerEventLoadedTemplate
      | IAdManagerEventLoadedNative
  ) => {
    console.log('nativeAd', nativeAd);
  };

  showBanner = (index: number, adType: TAdsListType) => {
    return (
      <BannerExample title={`${index}. DFP - ${adType} Ad Size`}>
        <View style={[styles.banner, styles.center]}>
          {adType === 'banner' && (
            <Banner
              onAdLoaded={this.onAdLoaded}
              adSize="mediumRectangle"
              validAdSizes={['mediumRectangle']}
              adUnitID={TEST_AD_BANNER}
              targeting={{
                customTargeting: { group: 'nzme_user_test' },
                categoryExclusions: ['media'],
                contentURL: 'nzmetest://',
                publisherProvidedID: 'provider_id_nzme',
              }}
            />
          )}
          {adType === 'banner-320x50' && (
            <Banner
              onAdLoaded={this.onAdLoaded}
              adSize="320x50"
              validAdSizes={['320x50']}
              adUnitID={TEST_AD_BANNER}
              targeting={{
                customTargeting: { group: 'nzme_user_test' },
                categoryExclusions: ['media'],
                contentURL: 'nzmetest://',
                publisherProvidedID: 'provider_id_nzme',
              }}
            />
          )}
          {adType === 'banner-fluid' && (
            <Banner
              style={styles.fluid}
              onAdLoaded={this.onAdLoaded}
              adSize="fluid"
              validAdSizes={['fluid']}
              adUnitID={TEST_AD_BANNER_FLUID}
              targeting={{
                customTargeting: { group: 'nzme_user_test' },
                categoryExclusions: ['media'],
                contentURL: 'nzmetest://',
                publisherProvidedID: 'provider_id_nzme',
              }}
            />
          )}
        </View>
      </BannerExample>
    );
  };

  showNative = (index: number, adType: TAdsListType) => {
    let adUnitID = '';
    switch (adType) {
      case 'native-banner':
        adUnitID = TEST_AD_BANNER;
        break;
      case 'native-template':
        adUnitID = TEST_AD_TEMPLATE;
        break;
      case 'native':
      default:
        adUnitID = TEST_AD_NATIVE;
    }
    const adsManager = new NativeAdsManager(adUnitID, [
      Interstitial.simulatorId,
    ]);
    const adTargeting = {
      customTargeting: {
        adtype: 'rectangle',
        arc_uuid: '5ce210e7f45fef6c88f16bf0',
        av: '2.0',
        pos: '1',
        pt: 'home',
        subscriber: 'true',
      },
      publisherProvidedID: '6c43f0be912249289a0286edab3fbb72',
    };
    const correlator = '0333965063464928';
    const customTemplateIds = [TEST_AD_TEMPLATE_ID];
    const customClickTemplateIds = [] as string[];
    //adType
    return (
      <BannerExample
        style={styles.nativeHolder}
        title={`${index}. DFP - Native ad`}
      >
        <View style={styles.center}>
          {adType === 'native-banner' && (
            <NativeAdView
              targeting={{
                customTargeting: { group: 'nzme_user_test' },
                categoryExclusions: ['media'],
                contentURL: 'nzmetest://',
                publisherProvidedID: 'provider_id_nzme',
              }}
              // style={{ width: '100%'}}
              adsManager={adsManager}
              validAdTypes={['banner']}
              adSize="mediumRectangle"
              validAdSizes={['mediumRectangle']}
              onAdLoaded={this.onAdLoaded}
              onAdFailedToLoad={(error) => {
                console.log(error);
              }}
            />
          )}
          {adType === 'native-template' && (
            <NativeAdView
              correlator={correlator}
              targeting={adTargeting}
              style={styles.nativeAd}
              adsManager={adsManager}
              validAdTypes={['template']}
              customTemplateIds={customTemplateIds}
              onAdLoaded={this.onAdLoaded}
              onAdFailedToLoad={(error) => {
                console.log(error);
              }}
              customClickTemplateIds={customClickTemplateIds}
              onAdCustomClick={(adData) => {
                console.log('adData', adData);
              }}
            />
          )}
          {adType === 'native' && (
            <NativeAdView
              correlator={correlator}
              targeting={adTargeting}
              style={styles.nativeAd}
              adsManager={adsManager}
              validAdTypes={['native']}
              customTemplateIds={customTemplateIds}
              onAdLoaded={this.onAdLoaded}
              onAdFailedToLoad={(error) => {
                console.log(error);
              }}
              customClickTemplateIds={customClickTemplateIds}
              onAdCustomClick={(adData) => {
                console.log('adData', adData);
              }}
            />
          )}
        </View>
      </BannerExample>
    );
  };

  addAd = (adType: TAdsListType) => {
    const { adsList } = this.state;
    adsList.push({ adType: adType });
    this.setState({ adsList: adsList });
  };

  onRefreshScrollView = () => {
    this.setState({ adsList: [{ adType: 'banner' }] });
  };

  render() {
    const { adsList, refreshingScrollView } = this.state;

    return (
      <View style={styles.container}>
        <ScrollView
          refreshControl={
            <RefreshControl
              refreshing={refreshingScrollView}
              onRefresh={this.onRefreshScrollView}
            />
          }
        >
          <BannerExample title="Interstitial">
            <Button
              title="Show Interstitial and preload next"
              onPress={this.showInterstitial}
            />
          </BannerExample>
          {adsList?.map((curItem, index) => {
            if (
              ['banner', 'banner-320x50', 'banner-fluid'].indexOf(
                curItem.adType
              ) >= 0
            ) {
              return (
                <View key={index}>
                  {this.showBanner(index + 1, curItem.adType)}
                </View>
              );
            } else {
              return (
                <View key={index}>
                  {this.showNative(index + 1, curItem.adType)}
                </View>
              );
            }
          })}
          <BannerExample title="Add more adds" style={styles.buttonHolder}>
            <Button
              title="Add Banner"
              onPress={() => this.addAd('banner')}
              color={'#CC5500'}
            />
            <Button
              title="Add Native"
              onPress={() => this.addAd('native')}
              color={'#CC5500'}
            />
            <Button
              title="Add Native Template"
              onPress={() => this.addAd('native-template')}
              color={'#CC5500'}
            />
            <Button
              title="Add Native Banner"
              onPress={() => this.addAd('native-banner')}
              color={'#CC5500'}
            />
            <Button
              title="Add Banner - 320x50"
              onPress={() => this.addAd('banner-320x50')}
              color={'#CC5500'}
            />
            <Button
              title="Add Banner - Fluid"
              onPress={() => this.addAd('banner-fluid')}
              color={'#CC5500'}
            />
          </BannerExample>
        </ScrollView>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    marginTop: Platform.OS === 'ios' ? 30 : 10,
  },
  buttonHolder: {
    paddingBottom: 40,
  },
  banner: {
    backgroundColor: '#f3f',
    paddingVertical: 10,
  },
  nativeHolder: {
    padding: 20,
  },
  center: {
    alignItems: 'center',
    width: '100%',
  },
  nativeAd: {
    width: '100%',
  },
  fluid: {
    width: Dimensions.get('screen').width,
    height: 350,
  },
});
