import type { IAdManagerTemplateImage } from './AdManagerTypes';

export interface IAdManagerEventBase {
  target?: number;
}

export interface IAdManagerEventErrorPayload {
  message: string;
  framesToPop?: number;
}

export interface IAdManagerEventError extends IAdManagerEventBase {
  error: IAdManagerEventErrorPayload;
}

export interface IAdManagerEventAppEvent extends IAdManagerEventBase {
  name: string;
  info: string;
}

export interface IAdManagerEventLoadedInterstitial extends IAdManagerEventBase {
  type: 'interstitial';
}

export interface IAdManagerEventLoadedBanner extends IAdManagerEventBase {
  type: 'banner';
  gadSize: {
    adSize: string;
    width: number;
    height: number;
  };
  isFluid?: string;
  measurements?: {
    adWidth: number;
    adHeight: number;
    width: number;
    height: number;
    left: number;
    top: number;
  };
}

export interface IAdManagerEventLoadedTemplate {
  type: 'template';
  templateID: string;
  [key: string]: IAdManagerTemplateImage | string;
}

export interface IAdManagerEventLoadedNative {
  type: 'native';
  headline?: string;
  bodyText?: string;
  callToActionText?: string;
  advertiserName?: string;
  starRating?: string;
  storeName?: string;
  price?: string;
  icon?: IAdManagerTemplateImage;
  images?: IAdManagerTemplateImage[];
  socialContext?: string;
}

export interface IAdManagerEventSize extends IAdManagerEventBase {
  type: 'banner';
  width: number;
  height: number;
}

export interface IAdManagerEventCustomClick {
  assetName: string;
  [key: string]: string;
}
