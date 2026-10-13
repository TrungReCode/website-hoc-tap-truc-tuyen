/**
 * Biểu đồ cột đơn giản cho xu hướng doanh thu theo tháng — không phụ thuộc
 * thư viện chart ngoài (chỉ dùng div/flexbox), để không phải cài thêm gói
 * npm nào cho tính năng này.
 */
export default function BieuDoDoanhThu({ diem }) {
  if (!diem || diem.length === 0) {
    return <div className="empty-state">Chưa có dữ liệu để hiển thị biểu đồ.</div>;
  }

  const giaTriToiDa = Math.max(...diem.map((d) => Number(d.doanhThu) || 0), 1);

  return (
    <div style={{ display: 'flex', alignItems: 'flex-end', gap: 10, height: 180, padding: '10px 4px 0' }}>
      {diem.map((d) => {
        const doanhThu = Number(d.doanhThu) || 0;
        const chieuCaoPhanTram = Math.max((doanhThu / giaTriToiDa) * 100, doanhThu > 0 ? 3 : 0);
        return (
          <div key={d.nhan} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', height: '100%' }}>
            <div style={{ flex: 1, display: 'flex', alignItems: 'flex-end', width: '100%' }} title={`${d.nhan}: ${doanhThu.toLocaleString('vi-VN')} đ`}>
              <div
                style={{
                  width: '100%',
                  maxWidth: '60px',
                  height: `${chieuCaoPhanTram}%`,
                  background: 'var(--primary)',
                  borderRadius: '4px 4px 0 0',
                  minHeight: doanhThu > 0 ? 3 : 0,
                  transition: 'height 0.2s ease',
                }}
              />
            </div>
            <div style={{ fontSize: 10.5, color: 'var(--text-3)', marginTop: 6, whiteSpace: 'nowrap' }}>{d.nhan}</div>
          </div>
        );
      })}
    </div>
  );
}
