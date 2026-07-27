export function getPostLoginPath(role: string): string {
  if (role === 'JD') {
    return '/admin/users';
  }
  return '/dashboard';
}
