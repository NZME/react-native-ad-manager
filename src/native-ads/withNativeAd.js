import React from 'react';
import { findNodeHandle, requireNativeComponent, UIManager } from 'react-native';
import { TriggerableContext } from './TriggerableViewManager';
import AdsManager from './NativeAdsManager';
import {string} from "prop-types";

const areSetsEqual = (a, b) => {
  if (a.size !== b.size)
    return false;
  for (const aItem of a) {
    if (!b.has(aItem))
      return false;
  }
  return true;
};

const __rest = (this && this.__rest) || function (s, e) {
  var t = {};
  for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
    t[p] = s[p];
  if (s != null && typeof Object.getOwnPropertySymbols === "function")
    for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) if (e.indexOf(p[i]) < 0)
      t[p[i]] = s[p[i]];
  return t;
};

const NativeAdView = requireNativeComponent('RNNativeAdsAdView');

export default (Component) => class NativeAdWrapper extends React.Component {
  constructor(props) {
    super(props);
    this.registerClickableChild = (child) => {
      const handle = findNodeHandle(child);
      if (!handle) {
        return;
      }
      this.clickableChildrenNodeHandles.set(child, handle);
      this.setState({
        clickableChildren: this.state.clickableChildren.add(handle)
      });
    };
    this.unregisterClickableChild = (child) => {
      this.setState(({ clickableChildren }) => {
        const newClickableChildren = new Set(clickableChildren);
        newClickableChildren.delete(this.clickableChildrenNodeHandles.get(child));
        this.clickableChildrenNodeHandles.delete(child);
        return { clickableChildren: newClickableChildren };
      });
    };

    this.handleOnAdLoaded = ({ nativeEvent }) => {
      this.setState({ nativeAd: nativeEvent });
      this.props.onAdLoaded &&
      this.props.onAdLoaded(nativeEvent);
    };
    this.handleOnSizeChange = ({ nativeEvent }) => {
      const { height, width } = nativeEvent;
      this.setState({ style: { width, height } });
      this.props.handleOnSizeChange &&
      this.props.handleOnSizeChange(nativeEvent);
    };
    this.handleOnAdFailedToLoad = ({ nativeEvent }) => {
      this.props.handleOnAdFailedToLoad &&
      this.props.handleOnAdFailedToLoad(nativeEvent);
    };
    this.handleOnAdOpened = ({ nativeEvent }) => {
      this.props.handleOnAdOpened &&
      this.props.handleOnAdOpened(nativeEvent);
    };
    this.handleOnAdClosed = ({ nativeEvent }) => {
      this.props.handleOnAdClosed &&
      this.props.handleOnAdClosed(nativeEvent);
    };
    this.handleOnAdLeftApplication = ({ nativeEvent }) => {
      this.props.handleOnAdLeftApplication &&
      this.props.handleOnAdLeftApplication(nativeEvent);
    };
    this.handleOnAppEvent = ({ nativeEvent }) => {
      this.props.handleOnAppEvent &&
      this.props.handleOnAppEvent(nativeEvent);
    };

    this.handleNativeAdViewMount = (ref) => {
      this.nativeAdViewRef = ref;
    };
    this.registerFunctionsForTriggerables = {
      register: this.registerClickableChild,
      unregister: this.unregisterClickableChild
    };
    this.clickableChildrenNodeHandles = new Map();
    this.state = {
      // iOS requires a non-null value
      clickableChildren: new Set(),
      style: {},
    };
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (Object.entries(this.state).toString() === Object.entries(nextState).toString()
      && Object.entries(this.props).toString() === Object.entries(nextProps).toString()) {
      return false;
    }
    return true;
  }

  componentDidUpdate(_, prevState) {
    const clickableChildrenChanged = areSetsEqual(prevState.clickableChildren, this.state.clickableChildren);
    if (clickableChildrenChanged) {
      const viewHandle = findNodeHandle(this.nativeAdViewRef);
      if (!viewHandle) {
        // Skip registration if the view is no longer valid.
        return;
      }

      AdsManager.registerViewsForInteractionAsync(viewHandle, [...this.state.clickableChildren])
        .then(data => {
          // do nothing for now
        })
        .catch(e => {
          // do nothing for now
        });
    }
  }

  reloadAd() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.nativeAdViewRef),
      UIManager.getViewManagerConfig('RNNativeAdsAdView').Commands
        .reloadAd,
      null
    );
  }

  renderAdComponent(componentProps) {
    if (!this.state.nativeAd) {
      return null;
    }

    return (
      <TriggerableContext.Provider value={this.registerFunctionsForTriggerables}>
        <Component {...componentProps} nativeAd={this.state.nativeAd}/>
      </TriggerableContext.Provider>
    );
  }

  render() {
    // Cast to any until https://github.com/Microsoft/TypeScript/issues/10727 is resolved
    const _a = this.props, { adsManager, onAdLoaded } = _a, rest = __rest(_a, ["adsManager", "onAdLoaded"]);

    return (
      <NativeAdView
        style={[this.props.style, this.state.style]}
        adSize={this.props.adSize}
        customTemplateId={this.props.customTemplateId}
        validAdSizes={this.props.validAdSizes}
        validAdTypes={this.props.validAdTypes}
        ref={this.handleNativeAdViewMount}
        adsManager={adsManager.toJSON()}
        onAdLoaded={this.handleOnAdLoaded}
        onSizeChange={this.handleOnSizeChange}
        onAdFailedToLoad={this.handleOnAdFailedToLoad}
        onAdOpened={this.handleOnAdOpened}
        onAdClosed={this.handleOnAdClosed}
        onAdLeftApplication={this.handleOnAdLeftApplication}
        onAppEvent={this.handleOnAppEvent}
        targeting={this.props.targeting}
      >
        {this.renderAdComponent(rest)}
      </NativeAdView>
    );
  }
};
