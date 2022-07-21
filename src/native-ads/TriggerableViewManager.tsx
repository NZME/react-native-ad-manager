import React from 'react';
import { Text, ViewProps } from 'react-native';
import type { TReactNodeHandleRef } from '../AdManagerTypes';

interface ITriggerableContext {
  register: (child: TReactNodeHandleRef) => void;
  unregister: (child: TReactNodeHandleRef) => void;
}

export const TriggerableContext = React.createContext<ITriggerableContext>({
  register: (child: TReactNodeHandleRef) => {
    throw new Error('Stub! ' + child);
  },
  unregister: (child: TReactNodeHandleRef) => {
    throw new Error('Stub! ' + child);
  },
});

class TriggerableViewChild extends React.Component {
  static contextType = TriggerableContext;
  wrapperRef = null;

  // TODO: convert to ref type
  handleWrapperRef = (ref: any) => {
    if (this.wrapperRef) {
      this.context.unregister(this.wrapperRef);
      this.wrapperRef = null;
    }
    if (ref) {
      this.context.register(ref);
      this.wrapperRef = ref;
    }
  };

  render() {
    return <Text {...this.props} ref={this.handleWrapperRef} />;
  }
}

interface ITriggerableViewProps extends ViewProps {}

export class TriggerableView extends React.Component<ITriggerableViewProps> {
  render() {
    return (
      <TriggerableContext.Consumer>
        {(contextValue) => (
          <TriggerableViewChild {...this.props} {...contextValue} />
        )}
      </TriggerableContext.Consumer>
    );
  }
}
