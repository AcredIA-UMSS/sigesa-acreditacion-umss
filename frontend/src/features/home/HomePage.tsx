import { HomeAccessBanners } from './HomeAccessBanners';
import { HomeDueaSection } from './HomeDueaSection';
import { HomeHero } from './HomeHero';
import { HomeNewsSection } from './HomeNewsSection';
import { HomeProcessSection } from './HomeProcessSection';
import { PublicSiteShell } from './PublicSiteShell';
import { useHomeDemoNavigate } from './useHomeDemoNavigate';

export function HomePage() {
  const onDemo = useHomeDemoNavigate();

  return (
    <PublicSiteShell>
      <main>
        <HomeHero onDemo={onDemo} />
        <HomeDueaSection />
        <HomeAccessBanners />
        <HomeProcessSection />
        <HomeNewsSection />
      </main>
    </PublicSiteShell>
  );
}
