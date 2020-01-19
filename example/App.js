import React, { Component } from 'react';
import {
  Button,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  View,
  RefreshControl
} from 'react-native';
import {
  AdMobInterstitial,
  PublisherBanner,
  NativeAdsManager,
} from 'react-native-ad-manager';
import NativeAdView from './NativeAdView';

const BannerExample = ({ style, title, children, ...props }) => (
  <View {...props} style={[styles.example, style]}>
    <Text style={styles.title}>{title}</Text>
    <View>{children}</View>
  </View>
);

const bannerWidths = [200, 250, 320];

export default class Example extends Component {
  constructor() {
    super();
    this.state = {
      fluidSizeIndex: 0,
      adsList: [],
      refreshingScrollView: false,
    };
  }

  componentDidMount() {
    AdMobInterstitial.setTestDevices([AdMobInterstitial.simulatorId]);
    AdMobInterstitial.setAdUnitID('/83069739/jeff');

    AdMobInterstitial.addEventListener('adLoaded', () =>
      console.log('AdMobInterstitial adLoaded'),
    );
    AdMobInterstitial.addEventListener('adFailedToLoad', error =>
      console.warn(error),
    );
    AdMobInterstitial.addEventListener('adOpened', () =>
      console.log('AdMobInterstitial => adOpened'),
    );
    AdMobInterstitial.addEventListener('adClosed', () => {
      console.log('AdMobInterstitial => adClosed');
      AdMobInterstitial.requestAd().catch(error => console.warn(error));
    });
    AdMobInterstitial.addEventListener('adLeftApplication', () =>
      console.log('AdMobInterstitial => adLeftApplication'),
    );

    AdMobInterstitial.requestAd().catch(error => console.warn(error));

    const adsList = [
      {type: 'banner'},
    ];
    this.setState({ adsList: adsList });
  }

  componentWillUnmount() {
    AdMobInterstitial.removeAllListeners();
  }

  showInterstitial() {
    AdMobInterstitial.showAd().catch(error => console.warn(error));
  }

  onAdLoaded = nativeAd => {
    // console.log(nativeAd);
  };

  showBanner = (adsManager, index) => {
    return <BannerExample title={`${index}. DFP - Fluid Ad Size`}>
      <View
        style={[
          { backgroundColor: '#f3f', paddingVertical: 10 },
          {alignItems: 'center', width: '100%'}
        ]}>
        {/*<NativeAdView
          targeting={{
            customTargeting: { group: 'nzme_user_test' },
            categoryExclusions: ['media'],
            contentURL: 'nzmetest://',
            publisherProvidedID: 'provider_id_nzme',
          }}
          // style={{ width: '100%'}}
          adsManager={adsManager}
          // adLayout={'horizontal'}
          validAdTypes={['banner']}
          adSize="mediumRectangle"
          validAdSizes={['mediumRectangle']}
          onAdLoaded={this.onAdLoaded}
          adUnitID={'/83069739/jeff'}
          onAdFailedToLoad={error => {
            console.log(error);
          }}
        />*/}
        <PublisherBanner
          onAdLoaded={this.onAdLoaded}
          adSize="mediumRectangle"
          validAdSizes={['mediumRectangle']}
          adUnitID={'/83069739/jeff'}
          targeting={{
            customTargeting: { group: 'nzme_user_test' },
            categoryExclusions: ['media'],
            contentURL: 'nzmetest://',
            publisherProvidedID: 'provider_id_nzme',
          }}
        />
      </View>
    </BannerExample>;
  };

  showNative = (adsManager, index) => {
    return <BannerExample
      style={{ padding: 20}}
      title={`${index}. DFP - Native ad`}>
      <View style={{alignItems: 'center', width: '100%'}}>
        <NativeAdView
          targeting={{
            customTargeting: { group: 'nzme_user_test' },
            categoryExclusions: ['media'],
            contentURL: 'nzmetest://',
            publisherProvidedID: 'provider_id_nzme',
          }}
          style={{ width: '100%'}}
          adsManager={adsManager}
          // adLayout={'horizontal'}
          validAdTypes={['native', 'template']}
          customTemplateId="11891103"
          onAdLoaded={this.onAdLoaded}
          adUnitID={'/83069739/jeff'}
          onAdFailedToLoad={error => {
            console.log(error);
          }}
        />
      </View>
    </BannerExample>;
  };

  addAd = type => {
    const { adsList } = this.state;
    if (type === 'banner') {
      adsList.push({type: 'banner'});
    } else {
      adsList.push({type: 'native'});
    }
    this.setState({ adsList: adsList });
  };

  onRefreshScrollView = () => {
    const adsList = [
      {type: 'banner'},
    ];
    this.setState({ adsList: adsList });
  };

  render() {
    // const adsManager = new NativeAdsManager("/6499/example/native", [AdMobInterstitial.simulatorId]);
    const adsManager = new NativeAdsManager("/83069739/jeff", [AdMobInterstitial.simulatorId]);
    const { adsList, refreshingScrollView } = this.state;

    return (
      <View style={styles.container}>
        <ScrollView
          refreshControl={
            <RefreshControl
              refreshing={refreshingScrollView}
              onRefresh={this.onRefreshScrollView}
            />
          }>
          <BannerExample title="Interstitial">
            <Button
              title="Show Interstitial and preload next"
              onPress={this.showInterstitial}
            />
          </BannerExample>
          {adsList?.map((curItem, index) => {
            if (curItem.type === 'banner') {
              return <View key={index}>{this.showBanner(adsManager, index+1)}</View>;
            } else {
              return <View key={index}>{this.showNative(adsManager, index+1)}</View>;
            }
          })}
          <BannerExample
            title="Add more adds"
            style={{ paddingBottom: 40 }}>
            <Button
              title="Add Banner"
              onPress={() => this.addAd('banner')}
              style={styles.button}
            />
            {/*<Button*/}
            {/*  title="Add Native"*/}
            {/*  onPress={() => this.addAd('native')}*/}
            {/*  style={styles.button}*/}
            {/*/>*/}
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
  example: {
    paddingVertical: 10,
  },
  title: {
    margin: 10,
    fontSize: 20,
  },
  button: {
    backgroundColor: "#CC5500"
  }
});
