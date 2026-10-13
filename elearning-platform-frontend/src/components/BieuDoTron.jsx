const MAU_MAC_DINH = ['#4f46e5', '#f59e0b', '#10b981', '#ef4444', '#06b6d4', '#8b5cf6'];

/**
 * Biểu đồ tròn (donut) đơn giản vẽ bằng SVG thuần — không cần cài thư viện
 * chart ngoài. duLieu: [{ nhan, giaTri }]
 */
export default function BieuDoTron({ duLieu, dinhDangGiaTri }) {
  const tong = (duLieu || []).reduce((s, d) => s + (Number(d.giaTri) || 0), 0);

  if (!duLieu || duLieu.length === 0 || tong <= 0) {
    return <div className="empty-state">Chưa có dữ liệu để hiển thị biểu đồ.</div>;
  }

  const banKinh = 60;
  const doDayVanh = 26;
  const chuVi = 2 * Math.PI * banKinh;

  let gocDaVe = 0;
  const phanDoan = duLieu.map((d, i) => {
    const giaTri = Number(d.giaTri) || 0;
    const tyLe = giaTri / tong;
    const doDaiCung = tyLe * chuVi;
    const doLech = -gocDaVe; // vẽ theo chiều kim đồng hồ bắt đầu từ 12h
    gocDaVe += doDaiCung;
    return {
      ...d,
      tyLe,
      mau: MAU_MAC_DINH[i % MAU_MAC_DINH.length],
      dashArray: `${doDaiCung} ${chuVi - doDaiCung}`,
      dashOffset: doLech,
    };
  });

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 24, flexWrap: 'wrap' }}>
      <svg width="150" height="150" viewBox="0 0 150 150" style={{ transform: 'rotate(-90deg)', flexShrink: 0 }}>
        {phanDoan.map((p, i) => (
          <circle
            key={i}
            cx="75" cy="75" r={banKinh}
            fill="transparent"
            stroke={p.mau}
            strokeWidth={doDayVanh}
            strokeDasharray={p.dashArray}
            strokeDashoffset={p.dashOffset}
          />
        ))}
      </svg>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {phanDoan.map((p, i) => (
          <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13 }}>
            <span style={{ width: 12, height: 12, borderRadius: 3, background: p.mau, flexShrink: 0 }} />
            <span style={{ minWidth: 90 }}>{p.nhan}</span>
            <strong>{dinhDangGiaTri ? dinhDangGiaTri(p.giaTri) : p.giaTri}</strong>
            <span style={{ color: 'var(--text-3)' }}>({(p.tyLe * 100).toFixed(1)}%)</span>
          </div>
        ))}
      </div>
    </div>
  );
}
