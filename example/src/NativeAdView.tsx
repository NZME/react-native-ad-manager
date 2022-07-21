import React from 'react';
import { Text, View, Image, StyleSheet } from 'react-native';
import {
  withNativeAd,
  TriggerableView,
  IAdManagerEventLoadedBanner,
  IAdManagerEventLoadedTemplate,
  IAdManagerEventLoadedNative,
  IAdManagerTemplateImage,
} from 'react-native-ad-manager';

interface INativeAdViewProps {
  nativeAd:
    | IAdManagerEventLoadedBanner
    | IAdManagerEventLoadedTemplate
    | IAdManagerEventLoadedNative;
}

interface INativeAdViewState {}

interface INativeAdElement {
  headline?: string;
  bodyText?: string;
  advertiserName?: string;
  starRating?: string;
  storeName?: string;
  price?: string;
  callToActionText?: string;
  icon?: IAdManagerTemplateImage;
  images?: IAdManagerTemplateImage[];
}

export class NativeAdView extends React.Component<
  INativeAdViewProps,
  INativeAdViewState
> {
  render() {
    const { nativeAd } = this.props;
    if (!['native', 'template'].includes(nativeAd?.type)) {
      return null;
    }

    let data: INativeAdElement = {};
    if (nativeAd?.type === 'native') {
      data.headline = nativeAd?.headline;
      data.bodyText = nativeAd?.bodyText;
      data.advertiserName = nativeAd?.advertiserName;
      data.starRating = nativeAd?.starRating;
      data.storeName = nativeAd?.storeName;
      data.price = nativeAd?.price;
      data.icon = nativeAd?.icon;
      data.callToActionText = nativeAd?.callToActionText;
      data.images = nativeAd?.images;
    } else if (nativeAd?.type === 'template') {
      data.headline = (nativeAd?.title || nativeAd?.Headline) as string;
      data.bodyText = (nativeAd?.text || nativeAd?.Caption) as string;
      data.advertiserName = nativeAd?.label as string;
      data.starRating = nativeAd?.imptrk as string;
      data.storeName = nativeAd?.headline as string;
      data.price = undefined;
      data.icon = (nativeAd?.image ||
        nativeAd?.MainImage) as IAdManagerTemplateImage;
      data.callToActionText = undefined;
      data.images = [];
    }

    return (
      <View style={styles.container}>
        <TriggerableView style={styles.triggerableView} />
        <View style={styles.native}>
          <View style={styles.nativeContainer}>
            {data?.headline && (
              <Text style={styles.nativeHeadline}>{data.headline}</Text>
            )}
            {data?.bodyText && (
              <Text style={styles.nativeBodyText}>{data.bodyText}</Text>
            )}
            <View style={styles.nativeContent}>
              <Text>{data?.advertiserName}</Text>
              <Text>{data?.starRating}</Text>
              <Text>{data?.storeName}</Text>
              <Text>{data?.price}</Text>
            </View>
          </View>
          {data?.icon?.uri && (
            <Image style={styles.nativeIcon} source={{ uri: data.icon.uri }} />
          )}
        </View>
        {data?.callToActionText && (
          <View style={styles.callToActionHolder}>
            <View>
              <Text style={styles.callToActionText}>
                {data.callToActionText}
              </Text>
            </View>
          </View>
        )}
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'column',
    borderWidth: 1,
    position: 'relative',
  },
  triggerableView: {
    backgroundColor: 'rgba(52, 52, 52, 0.5)',
    position: 'absolute',
    zIndex: 99,
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
  },

  native: { flexDirection: 'row', padding: 10 },
  nativeContainer: { flexDirection: 'column', flex: 1 },
  nativeHeadline: { fontSize: 18 },
  nativeBodyText: { fontSize: 10 },
  nativeContent: { flexDirection: 'row' },
  nativeIcon: { width: 80, height: 80 },
  callToActionHolder: { alignItems: 'center' },
  callToActionText: {
    fontSize: 15,
    color: '#a70f0a',
    paddingVertical: 10,
    paddingHorizontal: 30,
    elevation: 3,
    borderTopWidth: 0,
    margin: 10,
    borderRadius: 6,
  },
});

export default withNativeAd(NativeAdView);
