/**
 * Thanh tiến độ dùng chung cho mọi chức năng upload file (video bài học,
 * tài liệu đính kèm, ảnh bìa...). phanTram là số nguyên 0-100, hoặc null
 * nếu chưa bắt đầu / server chưa cho biết tổng dung lượng (trường hợp hiếm,
 * fallback sang thanh "indeterminate" chạy vô định).
 */
export default function ThanhTienDoTaiLen({ phanTram, nhan = 'Đang tải lên...' }) {
  const xacDinh = typeof phanTram === 'number' && !Number.isNaN(phanTram);

  return (
    <div style={{ marginTop: 8, marginBottom: 4 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: 'var(--text-3)', marginBottom: 4 }}>
        <span>{nhan}</span>
        {xacDinh && <span style={{ fontWeight: 600, color: 'var(--primary)' }}>{phanTram}%</span>}
      </div>
      <div style={{ height: 6, borderRadius: 4, background: 'var(--surface-2, #e5e7eb)', overflow: 'hidden' }}>
        {xacDinh ? (
          <div
            style={{
              height: '100%', borderRadius: 4, background: 'var(--primary)',
              width: `${Math.min(100, Math.max(0, phanTram))}%`,
              transition: 'width 0.2s ease',
            }}
          />
        ) : (
          <div className="thanh-tien-do-vo-dinh" style={{ height: '100%', borderRadius: 4, background: 'var(--primary)', width: '40%' }} />
        )}
      </div>
    </div>
  );
}
