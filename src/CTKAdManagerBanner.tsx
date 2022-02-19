import React from 'react';
import {
  requireNativeComponent,
  UIManager,
  ViewProps,
  findNodeHandle,
  NativeSyntheticEvent,
} from 'react-native';
import { createErrorFromErrorData } from './utils';
import type {
  IAdManagerEventBase,
  IAdManagerEventError,
  IAdManagerEventAppEvent,
  IAdManagerEventLoadedBanner,
  IAdManagerEventSize,
} from './AdManagerEvent';
import type { IAdManagerTargeting } from './AdManagerTypes';
import { LINKING_ERROR } from './Constants';

interface IAdManagerBannerPropsBase extends ViewProps {
  /**
   * DFP iOS library banner size constants
   * (https://developers.google.com/admob/ios/banner)
   * banner (320x50, Standard Banner for Phones and Tablets)
   * fullBanner (468x60, IAB Full-Size Banner for Tablets)
   * largeBanner (320x100, Large Banner for Phones and Tablets)
   * mediumRectangle (300x250, IAB Medium Rectangle for Phones and Tablets)
   * leaderboard (728x90, IAB Leaderboard for Tablets)
   * skyscraper (120x600, Skyscraper size for the iPad. Mediation only. AdMob/Google does not offer this size)
   * fluid (An ad size that spans the full width of its container, with a height dynamically determined by the ad)
   * {\d}x{\d} (Dynamic size determined byt the user, 300x250, 300x100 etc.)
   *
   * banner is default
   */
  adSize?: string;

  /**
   * Optional array specifying all valid sizes that are appropriate for this slot.
   */
  validAdSizes?: string[];

  /**
   * DFP ad unit ID
   */
  adUnitID?: string;

  /**
   * Array of test devices. Use Banner.simulatorId for the simulator
   */
  testDevices?: string[];

  targeting?: IAdManagerTargeting;
}

interface IAdManagerBannerProps extends IAdManagerBannerPropsBase {
  /**
   * DFP library events
   */
  onSizeChange?: (event: IAdManagerEventSize) => void;
  onAdLoaded?: (event: IAdManagerEventLoadedBanner) => void;
  onAdFailedToLoad?: (error: Error) => void;
  onAppEvent?: (event: IAdManagerEventAppEvent) => void;
  onAdOpened?: (event: IAdManagerEventBase) => void;
  onAdClosed?: (event: IAdManagerEventBase) => void;
}

interface IAdManagerBannerState {
  style: {
    width?: number;
    height?: number;
  };
}

interface IAdManagerBannerNativeProps extends IAdManagerBannerPropsBase {
  /**
   * DFP library events
   */
  onSizeChange?: (event: NativeSyntheticEvent<IAdManagerEventSize>) => void;
  onAdLoaded?: (
    event: NativeSyntheticEvent<IAdManagerEventLoadedBanner>
  ) => void;
  onAdFailedToLoad?: (
    event: NativeSyntheticEvent<IAdManagerEventError>
  ) => void;
  onAppEvent?: (event: NativeSyntheticEvent<IAdManagerEventAppEvent>) => void;
  onAdOpened?: (event: NativeSyntheticEvent<IAdManagerEventBase>) => void;
  onAdClosed?: (event: NativeSyntheticEvent<IAdManagerEventBase>) => void;
}

const ComponentName = 'CTKBannerView';

const AdManagerBannerView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<IAdManagerBannerNativeProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

export class Banner extends React.Component<
  IAdManagerBannerProps,
  IAdManagerBannerState
> {
  constructor(props: IAdManagerBannerProps) {
    super(props);
    this.handleSizeChange = this.handleSizeChange.bind(this);
    this.state = {
      style: {},
    };
  }

  shouldComponentUpdate(
    nextProps: IAdManagerBannerProps,
    nextState: IAdManagerBannerState
  ) {
    if (
      Object.entries(this.state.style).toString() ===
        Object.entries(nextState.style).toString() &&
      Object.entries(this.props).toString() ===
        Object.entries(nextProps).toString()
    ) {
      return false;
    }
    return true;
  }

  componentDidMount() {
    this.loadBanner();
  }

  loadBanner() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      UIManager.getViewManagerConfig(ComponentName).Commands.loadBanner,
      []
    );
  }

  handleSizeChange({ nativeEvent }: NativeSyntheticEvent<IAdManagerEventSize>) {
    const { height, width } = nativeEvent;
    this.setState({ style: { width, height } });
    this.props.onSizeChange && this.props.onSizeChange(nativeEvent);
  }

  render() {
    return (
      <AdManagerBannerView
        {...this.props}
        style={[this.props.style, this.state.style]}
        onSizeChange={this.handleSizeChange}
        onAdLoaded={(event) =>
          this.props.onAdLoaded && this.props.onAdLoaded(event.nativeEvent)
        }
        onAdFailedToLoad={(event) =>
          this.props.onAdFailedToLoad &&
          this.props.onAdFailedToLoad(
            createErrorFromErrorData(event.nativeEvent.error)
          )
        }
        onAppEvent={(event) =>
          this.props.onAppEvent && this.props.onAppEvent(event.nativeEvent)
        }
        onAdOpened={(event) =>
          this.props.onAdOpened && this.props.onAdOpened(event.nativeEvent)
        }
        onAdClosed={(event) =>
          this.props.onAdClosed && this.props.onAdClosed(event.nativeEvent)
        }
      />
    );
  }
}
