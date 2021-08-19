import React, {Component} from 'react';
import {
  Button,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  View,
  RefreshControl,
} from 'react-native';
import {Interstitial, Banner, NativeAdsManager} from 'react-native-ad-manager';
import NativeAdView from './NativeAdView';

const BannerExample = ({style, title, children, ...props}) => (
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
    Interstitial.setTestDevices([Interstitial.simulatorId]);
    Interstitial.setAdUnitID('/83069739/jeff');

    Interstitial.addEventListener('adLoaded', () =>
      console.log('Interstitial adLoaded'),
    );
    Interstitial.addEventListener('adFailedToLoad', error =>
      console.warn(error),
    );
    Interstitial.addEventListener('adOpened', () =>
      console.log('Interstitial => adOpened'),
    );
    Interstitial.addEventListener('adClosed', () => {
      console.log('Interstitial => adClosed');
      Interstitial.requestAd().catch(error => console.warn(error));
    });

    Interstitial.requestAd().catch(error => console.warn(error));

    // const adsList = [{type: 'banner'}];
    // this.setState({adsList: adsList});
  }

  componentWillUnmount() {
    Interstitial.removeAllListeners();
  }

  showInterstitial() {
    Interstitial.showAd().catch(error => console.warn(error));
  }

  onAdLoaded = nativeAd => {
    // console.log(nativeAd);
  };

  showBanner = (adsManager, index) => {
    return (
      <BannerExample title={`${index}. DFP - Fluid Ad Size`}>
        <View
          style={[
            {backgroundColor: '#f3f', paddingVertical: 10},
            {alignItems: 'center', width: '100%'},
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
          <Banner
            onAdLoaded={this.onAdLoaded}
            adSize="mediumRectangle"
            validAdSizes={['mediumRectangle']}
            adUnitID={'/83069739/jeff'}
            targeting={{
              customTargeting: {group: 'nzme_user_test'},
              categoryExclusions: ['media'],
              contentURL: 'nzmetest://',
              publisherProvidedID: 'provider_id_nzme',
            }}
          />
        </View>
      </BannerExample>
    );
  };

  showNative = (adsManager, index) => {
    const adTargeting = {
      customTargeting: {adtype: "rectangle",
        arc_uuid: "5ce210e7f45fef6c88f16bf0",
        av: "2.0",
        pos: "1",
        pt: "home",
        subscriber: "true"},
      publisherProvidedID: "6c43f0be912249289a0286edab3fbb72"
    };
    const correlator = "0333965063464928";
    const adLayout =  "horizontal";
    const adUnitID = "/6499/example/native";
    const customTemplateIds = ["10063170"];
    const customClickTemplateIds = [];
    const validAdTypes = ['template'];
    return (
      <BannerExample style={{padding: 20}} title={`${index}. DFP - Native ad`}>
        <View style={{alignItems: 'center', width: '100%'}}>
          <NativeAdView
            correlator={correlator}
            targeting={adTargeting}
            style={{width: '100%'}}
            adsManager={adsManager}
            adLayout={adLayout}
            validAdTypes={validAdTypes}
            customTemplateIds={customTemplateIds}
            onAdLoaded={this.onAdLoaded}
            adUnitID={adUnitID}
            onAdFailedToLoad={error => {
              console.log(error);
            }}
            customClickTemplateIds={customClickTemplateIds}
            onAdCustomClick={adData => {
              console.log('adData', adData);
            }}
          />
        </View>
      </BannerExample>
    );
  };

  addAd = type => {
    const {adsList} = this.state;
    if (type === 'banner') {
      adsList.push({type: 'banner'});
    } else {
      adsList.push({type: 'native'});
    }
    this.setState({adsList: adsList});
  };

  onRefreshScrollView = () => {
    const adsList = [{type: 'banner'}];
    this.setState({adsList: adsList});
  };

  render() {
    // const adsManager = new NativeAdsManager("/6499/example/native", [AdMobInterstitial.simulatorId]);
    const adsManager = new NativeAdsManager('/6499/example/native', [
      Interstitial.simulatorId,
    ]);
    const {adsList, refreshingScrollView} = this.state;

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
              return (
                <View key={index}>
                  {this.showBanner(adsManager, index + 1)}
                </View>
              );
            } else {
              return (
                <View key={index}>
                  {this.showNative(adsManager, index + 1)}
                </View>
              );
            }
          })}
          <BannerExample title="Add more adds" style={{paddingBottom: 40}}>
            <Button
              title="Add Banner"
              onPress={() => this.addAd('banner')}
              style={styles.button}
            />
            <Button
              title="Add Native"
              onPress={() => this.addAd('native')}
              style={styles.button}
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
  example: {
    paddingVertical: 10,
  },
  title: {
    margin: 10,
    fontSize: 20,
  },
  button: {
    backgroundColor: '#CC5500',
  },
});
