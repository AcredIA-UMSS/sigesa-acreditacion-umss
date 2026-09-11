import { defineConfig, devices } from '@playwright/test';

const isHeaded = process.env.HEADLESS === 'false' || process.argv.includes('--headed');

export default defineConfig({
	testDir: './test', 
	reporter: [
		['list'],
		['html', { open: 'never' }],
	],
	use: {
		baseURL: 'http://localhost:3000',
		trace: 'on-first-retry',
		headless: !isHeaded,
		launchOptions: {
			slowMo: isHeaded ? 400 : 0,
		},
	}, 
	projects: [
		{
			name: 'chromium', 
			use: { 
				...devices['Desktop Chrome'], 
				...((process.env.CHROMIUM_PATH || process.env.CHROMUM_PATH)
					? { 
						launchOptions: { 
							executablePath: process.env.CHROMIUM_PATH || process.env.CHROMUM_PATH,
							slowMo: isHeaded ? 400 : 0,
						} 
					  }
					: {})
			}
		}
	], 
	webServer: {
		command: 'docker compose up',
		url: 'http://localhost:8080/health',
		reuseExistingServer: !process.env.CI,
		timeout: 30_000,
	},
});