#import "RNAdManagerUtils.h"

static NSMutableDictionary<NSString*, NSString*> *correlators;

NSArray *__nullable RNAdManagerProcessTestDevices(NSArray *__nullable testDevices, id _Nonnull simulatorId)
{
    if (testDevices == NULL) {
        return testDevices;
    }
    NSInteger index = [testDevices indexOfObject:@"SIMULATOR"];
    if (index == NSNotFound) {
        return testDevices;
    }
    NSMutableArray *values = [testDevices mutableCopy];
    [values removeObjectAtIndex:index];
    [values addObject:simulatorId];
    return values;
}

NSString *__nullable getRandomPINString(NSInteger length)
{
    NSMutableString *returnString = [NSMutableString stringWithCapacity:length];

    NSString *numbers = @"0123456789";

    // First number cannot be 0
    [returnString appendFormat:@"%C", [numbers characterAtIndex:(arc4random() % ([numbers length]-1))+1]];

    for (int i = 1; i < length; i++)
    {
        [returnString appendFormat:@"%C", [numbers characterAtIndex:arc4random() % [numbers length]]];
    }

    return returnString;
}

NSString *__nullable getCorrelator(NSString *adUnitID)
{
    if (correlators == nil) {
        correlators = [NSMutableDictionary new];
    }

    NSString *correlator = [correlators objectForKey:adUnitID];

    if (correlator == nil) {
        correlator = getRandomPINString(16);
        [correlators setValue:correlator forKey:adUnitID];
    }

    return correlator;
}
