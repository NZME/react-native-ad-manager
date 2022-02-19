import React, { JSXElementConstructor } from 'react';
import {
  findNodeHandle,
  NativeSyntheticEvent,
  requireNativeComponent,
  UIManager,
  ViewProps,
} from 'react-native';
import { TriggerableContext } from './TriggerableViewManager';
import { NativeAdsManager } from './NativeAdsManager';
import type {
  IAdManagerTargeting,
  TReactNodeHandleRef,
} from '../AdManagerTypes';
import type {
  IAdManagerEventBase,
  IAdManagerEventCustomClick,
  IAdManagerEventAppEvent,
  IAdManagerEventLoadedBanner,
  IAdManagerEventLoadedNative,
  IAdManagerEventLoadedTemplate,
  IAdManagerEventSize,
  IAdManagerEventError,
} from '../AdManagerEvent';
import { createErrorFromErrorData, stripProperties } from '../utils';
import { LINKING_ERROR } from '../Constants';

/*
@property (nonatomic, copy) NSDictionary *targeting;

 */
interface INativeAdPropsBase extends ViewProps {
  adSize?: string;
  correlator?: string;
  customTemplateIds?: string[];
  validAdSizes?: string[];
  validAdTypes?: ('banner' | 'native' | 'template')[];
  customClickTemplateIds?: string[];
  targeting?: IAdManagerTargeting;
}

interface INativeAdNativeProps extends INativeAdPropsBase {
  children: React.ReactNode;
  adsManager: string;
  loaderIndex?: string;
  onSizeChange?: (event: NativeSyntheticEvent<IAdManagerEventSize>) => void;
  onAdLoaded?: (
    event: NativeSyntheticEvent<
      | IAdManagerEventLoadedBanner
      | IAdManagerEventLoadedTemplate
      | IAdManagerEventLoadedNative
    >
  ) => void;
  onAdFailedToLoad?: (
    event: NativeSyntheticEvent<IAdManagerEventError>
  ) => void;
  onAppEvent?: (event: NativeSyntheticEvent<IAdManagerEventAppEvent>) => void;
  onAdOpened?: (event: NativeSyntheticEvent<IAdManagerEventBase>) => void;
  onAdClosed?: (event: NativeSyntheticEvent<IAdManagerEventBase>) => void;
  onAdCustomClick?: (
    event: NativeSyntheticEvent<IAdManagerEventCustomClick>
  ) => void;
}

interface INativeAdProps extends INativeAdPropsBase {
  adsManager: NativeAdsManager;
  adLoaderIndex?: string;
  onSizeChange?: (event: IAdManagerEventSize) => void;
  onAdLoaded?: (
    event:
      | IAdManagerEventLoadedBanner
      | IAdManagerEventLoadedTemplate
      | IAdManagerEventLoadedNative
  ) => void;
  onAdFailedToLoad?: (error: Error) => void;
  onAppEvent?: (event: IAdManagerEventAppEvent) => void;
  onAdOpened?: (event: IAdManagerEventBase) => void;
  onAdClosed?: (event: IAdManagerEventBase) => void;
  onAdCustomClick?: (event: IAdManagerEventCustomClick) => void;
}

interface INativeAdState {
  clickableChildren: Set<TReactNodeHandleRef>;
  style: {
    width?: number;
    height?: number;
  };
  nativeAd?:
    | IAdManagerEventLoadedBanner
    | IAdManagerEventLoadedTemplate
    | IAdManagerEventLoadedNative;
}

const areSetsEqual = (
  a: Set<TReactNodeHandleRef>,
  b: Set<TReactNodeHandleRef>
) => {
  if (a.size !== b.size) return false;
  for (const aItem of a) {
    if (!b.has(aItem)) return false;
  }
  return true;
};

const ComponentName = 'CTKAdManageNative';

const NativeAdView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<INativeAdNativeProps>(ComponentName)
    : () => {
        // eslint-disable-next-line no-undef
        throw new Error(LINKING_ERROR);
      };

export default (Component: JSXElementConstructor<any>) =>
  class NativeAdWrapper extends React.Component<
    INativeAdProps,
    INativeAdState
  > {
    clickableChildrenNodeHandles = new Map();

    constructor(props: INativeAdProps) {
      super(props);
      this.state = {
        // iOS requires a non-null value
        clickableChildren: new Set(),
        style: {},
      };
    }

    componentDidMount() {
      this.reloadAd();
    }

    registerClickableChild = (child: TReactNodeHandleRef) => {
      const handle = findNodeHandle(child);
      if (!handle) {
        return;
      }
      this.clickableChildrenNodeHandles.set(child, handle);
      this.setState({
        clickableChildren: this.state.clickableChildren.add(handle),
      });
    };

    unregisterClickableChild = (child: TReactNodeHandleRef) => {
      this.setState(({ clickableChildren }) => {
        const newClickableChildren = new Set(clickableChildren);
        newClickableChildren.delete(
          this.clickableChildrenNodeHandles.get(child)
        );
        this.clickableChildrenNodeHandles.delete(child);
        return { clickableChildren: newClickableChildren };
      });
    };

    handleSizeChange = ({
      nativeEvent,
    }: NativeSyntheticEvent<IAdManagerEventSize>) => {
      const { height, width } = nativeEvent;
      this.setState({ style: { width, height } });
      this.props.onSizeChange && this.props.onSizeChange(nativeEvent);
    };

    handleAdLoaded = ({
      nativeEvent,
    }: NativeSyntheticEvent<
      | IAdManagerEventLoadedBanner
      | IAdManagerEventLoadedTemplate
      | IAdManagerEventLoadedNative
    >) => {
      this.setState({ nativeAd: nativeEvent });
      this.props.onAdLoaded && this.props.onAdLoaded(nativeEvent);
    };

    shouldComponentUpdate(
      nextProps: INativeAdProps,
      nextState: INativeAdState
    ) {
      if (
        Object.entries(this.state).toString() ===
          Object.entries(nextState).toString() &&
        Object.entries(this.props).toString() ===
          Object.entries(nextProps).toString()
      ) {
        return false;
      }
      return true;
    }

    componentDidUpdate(_: INativeAdProps, prevState: INativeAdState) {
      const clickableChildrenChanged = areSetsEqual(
        prevState.clickableChildren,
        this.state.clickableChildren
      );
      if (clickableChildrenChanged) {
        const viewHandle = findNodeHandle(this);
        if (!viewHandle) {
          // Skip registration if the view is no longer valid.
          return;
        }

        NativeAdsManager.registerViewsForInteractionAsync(viewHandle, [
          ...this.state.clickableChildren,
        ])
          .then(() => {
            // do nothing for now
          })
          .catch(() => {
            // do nothing for now
          });
      }
    }

    reloadAd() {
      UIManager.dispatchViewManagerCommand(
        findNodeHandle(this),
        UIManager.getViewManagerConfig(ComponentName).Commands.reloadAd,
        []
      );
    }

    renderAdComponent(componentProps: INativeAdProps) {
      if (!this.state.nativeAd) {
        return null;
      }

      return (
        <TriggerableContext.Provider
          value={{
            register: this.registerClickableChild,
            unregister: this.unregisterClickableChild,
          }}
        >
          <Component {...componentProps} nativeAd={this.state.nativeAd} />
        </TriggerableContext.Provider>
      );
    }

    render() {
      // Cast to any until https://github.com/Microsoft/TypeScript/issues/10727 is resolved
      const _a = this.props,
        { adsManager } = _a,
        rest = stripProperties(_a, ['adsManager', 'onAdLoaded']);

      return (
        <NativeAdView
          style={[this.props.style, this.state.style]}
          adSize={this.props.adSize}
          loaderIndex={this.props.adLoaderIndex}
          correlator={this.props.correlator}
          customTemplateIds={this.props.customTemplateIds}
          validAdSizes={this.props.validAdSizes}
          validAdTypes={this.props.validAdTypes}
          onSizeChange={this.handleSizeChange}
          onAdLoaded={this.handleAdLoaded}
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
          onAdCustomClick={(event) =>
            this.props.onAdCustomClick &&
            this.props.onAdCustomClick(event.nativeEvent)
          }
          targeting={this.props.targeting}
          customClickTemplateIds={this.props.customClickTemplateIds}
          adsManager={adsManager.toJSON()}
        >
          {this.renderAdComponent(rest)}
        </NativeAdView>
      );
    }
  };
