import type { ReactNode } from 'react';
import { HomeFooter } from './HomeFooter';
import { HomeHeader } from './HomeHeader';
import './home.css';

type PublicSiteShellProps = {
  children: ReactNode;
};

export function PublicSiteShell({ children }: PublicSiteShellProps) {
  return (
    <div className="home-page bg-body">
      <HomeHeader />
      {children}
      <HomeFooter />
    </div>
  );
}
