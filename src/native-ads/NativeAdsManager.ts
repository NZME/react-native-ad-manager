import { NativeModules } from 'react-native';
import type { TReactNodeHandleRef } from '../AdManagerTypes';

const { CTKAdManageNativeManager } = NativeModules;

export class NativeAdsManager {
  isValid: boolean;
  adUnitID: string;

  constructor(adUnitID: string, testDevices: string[]) {
    // Indicates whether AdsManager is ready to serve ads
    this.isValid = true;
    this.adUnitID = adUnitID;
    CTKAdManageNativeManager.init(adUnitID, testDevices);
  }

  static async registerViewsForInteractionAsync(
    nativeAdViewTag: TReactNodeHandleRef,
    clickable: TReactNodeHandleRef[]
  ) {
    return await CTKAdManageNativeManager.registerViewsForInteraction(
      nativeAdViewTag,
      clickable
    );
  }

  /**
   * Set the native ad manager.
   */
  toJSON() {
    return this.adUnitID;
  }
}
