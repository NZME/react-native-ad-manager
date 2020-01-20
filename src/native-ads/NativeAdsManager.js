import { NativeModules, NativeEventEmitter } from 'react-native';

const CTKAdManageNativeManager = NativeModules.CTKAdManageNativeManager;

export default class NativeAdsManager {
  constructor(adUnitID, testDevices) {
    // Indicates whether AdsManager is ready to serve ads
    this.isValid = true;
    this.adUnitID = adUnitID;
    CTKAdManageNativeManager.init(adUnitID, testDevices);
  }

  static async registerViewsForInteractionAsync(nativeAdViewTag, clickable) {
    const result = await CTKAdManageNativeManager.registerViewsForInteraction(
      nativeAdViewTag,
      clickable
    );
    return result;
  }

  /**
   * Set the native ads manager.
   */
  toJSON() {
    return this.adUnitID;
  }
}
