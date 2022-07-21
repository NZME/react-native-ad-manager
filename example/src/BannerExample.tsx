import React from 'react';
import { StyleSheet, Text, View, ViewProps } from 'react-native';

interface IBannerExampleProps extends ViewProps {
  title: string;
  children: React.ReactNode;
}
export const BannerExample = ({
  style,
  title,
  children,
  ...props
}: IBannerExampleProps) => (
  <View {...props} style={[styles.example, style]}>
    <Text style={styles.title}>{title}</Text>
    <View>{children}</View>
  </View>
);

const styles = StyleSheet.create({
  example: {
    paddingVertical: 10,
  },
  title: {
    margin: 10,
    fontSize: 20,
  },
});
