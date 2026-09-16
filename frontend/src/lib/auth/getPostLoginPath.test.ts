import { describe, expect, it } from 'vitest';
import { getPostLoginPath } from './getPostLoginPath';

describe('getPostLoginPath', () => {
  it('shouldSendJdToUsersAdmin', () => {
    expect(getPostLoginPath('JD')).toBe('/admin/users');
  });

  it('shouldSendOtherRolesToDashboard', () => {
    expect(getPostLoginPath('CC')).toBe('/dashboard');
    expect(getPostLoginPath('TD')).toBe('/dashboard');
    expect(getPostLoginPath('EE')).toBe('/dashboard');
  });
});
