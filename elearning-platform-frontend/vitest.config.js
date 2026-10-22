import { defineConfig } from 'vitest/config';

// Cấu hình chạy test riêng (không đụng vào vite.config.js của ứng dụng).
// Chạy: npm test
export default defineConfig({
  esbuild: { jsx: 'automatic' },
  test: { environment: 'jsdom', include: ['src/**/*.test.{js,jsx}'], css: false },
});