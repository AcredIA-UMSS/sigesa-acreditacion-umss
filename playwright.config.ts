import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
	testDir: './test', 
	use: {
		baseURL: 'http://localhost:3000',
	    trace: 'on-first-retry',
	    headless: false
	}, 
	projects: [
		{
			name: 'chromium', 
			use: { 
				...devices['Desktop Chrome'], 
				...(process.env.CHROMUM_PATH? {launchingOptions: {executablePath: process.env.CHROMUM_PATH } } : {})
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