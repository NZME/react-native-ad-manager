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

interface IAdManagerAdaptiveBannerPropsBase extends ViewProps {
  /**
   * Adaptive banner position.
   */
  adPosition?:
    | 'currentOrientationAnchored'
    | 'currentOrientationInline'
    | 'portraitInline'
    | 'landscapeInline'
    | 'inline';

  /**
   * Max height of the adaptive banner. Only works with "inline" adPosition.
   */
  maxHeight?: number;

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

interface IAdManagerAdaptiveBannerProps
  extends IAdManagerAdaptiveBannerPropsBase {
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

interface IAdManagerAdaptiveBannerState {
  style: {
    width?: number;
    height?: number;
  };
}

interface IAdManagerAdaptiveBannerNativeProps
  extends IAdManagerAdaptiveBannerPropsBase {
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

const ComponentName = 'CTKAdaptiveBannerView';

const AdManagerAdaptiveBannerView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<IAdManagerAdaptiveBannerNativeProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

export class AdaptiveBanner extends React.Component<
  IAdManagerAdaptiveBannerProps,
  IAdManagerAdaptiveBannerState
> {
  constructor(props: IAdManagerAdaptiveBannerProps) {
    super(props);
    this.handleSizeChange = this.handleSizeChange.bind(this);
    this.state = {
      style: {},
    };
  }

  shouldComponentUpdate(
    nextProps: IAdManagerAdaptiveBannerProps,
    nextState: IAdManagerAdaptiveBannerState
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
      <AdManagerAdaptiveBannerView
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
