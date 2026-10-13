// set-cors.js — CHẠY 1 LẦN DUY NHẤT rồi có thể xóa file này.
//
// Dùng để bật CORS cho Firebase Storage bucket, thay thế cho lệnh
// `gsutil cors set` khi máy chưa cài Google Cloud SDK.
//
// CÁCH DÙNG:
//   1) npm install @google-cloud/storage      (chạy 1 lần trong thư mục chứa file này)
//   2) Sửa 2 biến SERVICE_ACCOUNT_PATH và BUCKET_NAME bên dưới cho đúng
//   3) node set-cors.js

const { Storage } = require('@google-cloud/storage');

const SERVICE_ACCOUNT_PATH = process.env.FIREBASE_CREDENTIALS_PATH || '';
const BUCKET_NAME = process.env.FIREBASE_STORAGE_BUCKET || 'your-storage-bucket.appspot.com';
const FRONTEND_URLS = [
  process.env.FRONTEND_URL,
  'http://localhost:5173',
  'http://127.0.0.1:5173',
  ...(process.env.ALLOWED_FRONTEND_ORIGINS || '').split(',').map((origin) => origin.trim()).filter(Boolean),
].filter(Boolean);

const CORS_CONFIG = [
  {
    origin: [...new Set(FRONTEND_URLS)],
    method: ['GET', 'HEAD'],
    maxAgeSeconds: 3600,
    responseHeader: ['Content-Type', 'Content-Length', 'Content-Range', 'Accept-Ranges', 'ETag'],
  },
];

async function main() {
  const storage = new Storage({ keyFilename: SERVICE_ACCOUNT_PATH });
  const bucket = storage.bucket(BUCKET_NAME);

  await bucket.setCorsConfiguration(CORS_CONFIG);
  console.log('✅ Đã set CORS thành công cho bucket:', BUCKET_NAME);

  const [metadata] = await bucket.getMetadata();
  console.log('CORS hiện tại:', JSON.stringify(metadata.cors, null, 2));
}

main().catch((err) => {
  console.error('❌ Lỗi khi set CORS:', err.message);
  process.exit(1);
});