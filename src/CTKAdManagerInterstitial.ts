import { NativeEventEmitter, NativeModules } from 'react-native';
import { createErrorFromErrorData } from './utils';
import type {
  IAdManagerEventBase,
  IAdManagerEventErrorPayload,
  IAdManagerEventLoadedInterstitial,
} from './AdManagerEvent';
import { LINKING_ERROR } from './Constants';
import type { IAdManagerTargeting } from './AdManagerTypes';

const CTKInterstitial = NativeModules.CTKInterstitial
  ? NativeModules.CTKInterstitial
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const eventEmitter = new NativeEventEmitter(CTKInterstitial);

const eventMap = {
  adLoaded: 'interstitialAdLoaded',
  adFailedToLoad: 'interstitialAdFailedToLoad',
  adOpened: 'interstitialAdOpened',
  adClosed: 'interstitialAdClosed',
};

type TAdManagerInterstitialEvent =
  | 'adLoaded'
  | 'adFailedToLoad'
  | 'adOpened'
  | 'adClosed';

type TAdManagerInterstitialHandler = (
  event: Error | IAdManagerEventBase | IAdManagerEventLoadedInterstitial
) => void;

const _subscriptions = new Map();

const addEventListener = (
  event: TAdManagerInterstitialEvent,
  handler: TAdManagerInterstitialHandler
) => {
  const mappedEvent = eventMap[event];
  if (mappedEvent) {
    let listener;
    if (event === 'adFailedToLoad') {
      listener = eventEmitter.addListener(
        mappedEvent,
        (error: IAdManagerEventErrorPayload) =>
          handler(createErrorFromErrorData(error))
      );
    } else {
      listener = eventEmitter.addListener(mappedEvent, handler);
    }
    _subscriptions.set(handler, listener);
    return {
      remove: () => removeEventListener(event, handler),
    };
  } else {
    console.warn(`Trying to subscribe to unknown event: "${event}"`);
    return {
      remove: () => {},
    };
  }
};

const removeEventListener = (
  _event: TAdManagerInterstitialEvent,
  handler: TAdManagerInterstitialHandler
) => {
  const listener = _subscriptions.get(handler);
  if (!listener) {
    return;
  }
  listener.remove();
  _subscriptions.delete(handler);
};

const removeAllListeners = () => {
  _subscriptions.forEach((listener, key, map) => {
    listener.remove();
    map.delete(key);
  });
};

const simulatorId = 'SIMULATOR';

const setAdUnitID = (adUnitID: string) => {
  CTKInterstitial.setAdUnitID(adUnitID);
};

const setTestDevices = (testDevices: string[]) => {
  CTKInterstitial.setTestDevices(testDevices);
};

const setTargeting = (targeting: IAdManagerTargeting) => {
  CTKInterstitial.setTargeting(targeting);
};

const requestAd = (): Promise<null> => {
  return CTKInterstitial.requestAd();
}

const showAd = (): Promise<null> => {
  return CTKInterstitial.showAd();
}

const isReady = (callback: (isReady: number) => void): Promise<null> => {
  return CTKInterstitial.isReady(callback);
}

export default {
  addEventListener,
  removeEventListener,
  removeAllListeners,
  simulatorId,
  setAdUnitID,
  setTestDevices,
  setTargeting,
  requestAd,
  showAd,
  isReady
}
