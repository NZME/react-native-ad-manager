import type { IAdManagerEventErrorPayload } from './AdManagerEvent';

export const createErrorFromErrorData = (
  errorData: IAdManagerEventErrorPayload
) => {
  const { message, ...extraErrorInfo } = errorData || {};
  const error = new Error(message);
  extraErrorInfo.framesToPop = 1;
  return Object.assign(error, extraErrorInfo);
};

export const stripProperties = (s: any, e: string[]): any => {
  const t: any = {};
  for (let p in s)
    if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
      t[p] = s[p];
  if (s != null && typeof Object.getOwnPropertySymbols === 'function')
    for (let i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++)
      if (e.indexOf(String(p[i])) < 0) t[p[i]] = s[p[i]];
  return t;
};
