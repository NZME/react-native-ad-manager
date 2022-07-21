export type TReactNodeHandleRef = number;

export interface IAdManagerTargetingLocation {
  latitude?: number;
  longitude?: number;
  accuracy?: number;
}

export interface IAdManagerTargeting {
  /**
   * Arbitrary object of custom targeting information.
   */
  customTargeting?: Record<string, string>;

  /**
   * Array of exclusion labels.
   */
  categoryExclusions?: string[];

  /**
   * Array of keyword strings.
   */
  keywords?: string[];

  /**
   * Applications that monetize content matching a webpage's content may pass
   * a content URL for keyword targeting.
   */
  contentURL?: string;

  /**
   * You can set a publisher provided identifier (PPID) for use in frequency
   * capping, audience segmentation and targeting, sequential ad rotation, and
   * other audience-based ad delivery controls across devices.
   */
  publisherProvidedID?: string;

  /**
   * The user’s current location may be used to deliver more relevant ads.
   */
  /**
   * @deprecated Location has deleted from Ad Request since location data is not used
   *  by Google to target ads. Use third-party APIs to provide the
   *  information to third-party ad networks if required.
   */
  location?: IAdManagerTargetingLocation;

  /**
   * Correlator string to pass to ad loader.
   */
  correlator?: string;
}

export interface IAdManagerTemplateImage {
  uri: string;
  width: number;
  height: number;
  scale: number;
}
