import { HomeAccessBanners } from './HomeAccessBanners';
import { HomeDueaSection } from './HomeDueaSection';
import { HomeFooter } from './HomeFooter';
import { HomeHeader } from './HomeHeader';
import { HomeHero } from './HomeHero';
import { HomeNewsSection } from './HomeNewsSection';
import { HomeProcessSection } from './HomeProcessSection';
import { useHomeDemoNavigate } from './useHomeDemoNavigate';
import './home.css';

export function HomePage() {
  const onDemo = useHomeDemoNavigate();

  return (
    <div className="home-page bg-body">
      <HomeHeader />
      <main>
        <HomeHero onDemo={onDemo} />
        <HomeDueaSection />
        <HomeAccessBanners />
        <HomeProcessSection />
        <HomeNewsSection />
      </main>
      <HomeFooter />
    </div>
  );
}
