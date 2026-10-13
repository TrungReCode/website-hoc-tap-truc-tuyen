/**
 * Bộ lọc thời gian nhanh — Hôm nay / Hôm qua / Tuần này / Tháng này, thay vì
 * bắt người dùng tự chọn ngày thủ công cho các nhu cầu xem nhanh phổ biến
 * nhất. Vẫn giữ 2 ô ngày thủ công bên dưới cho trường hợp cần khoảng tùy ý.
 */
export default function BoLocThoiGianNhanh({ tuNgay, denNgay, onDoiKhoang }) {
  function dinhDangNgay(d) {
    return d.toISOString().slice(0, 10);
  }

  function chonNhanh(loai) {
    const homNay = new Date();
    let tu, den;

    if (loai === 'hom-nay') {
      tu = den = homNay;
    } else if (loai === 'hom-qua') {
      const homQua = new Date(homNay);
      homQua.setDate(homQua.getDate() - 1);
      tu = den = homQua;
    } else if (loai === 'tuan-nay') {
      const thuTrongTuan = homNay.getDay() === 0 ? 7 : homNay.getDay(); // CN=7 để tính từ Thứ 2
      tu = new Date(homNay);
      tu.setDate(homNay.getDate() - (thuTrongTuan - 1));
      den = homNay;
    } else if (loai === 'thang-nay') {
      tu = new Date(homNay.getFullYear(), homNay.getMonth(), 1);
      den = homNay;
    }

    onDoiKhoang(dinhDangNgay(tu), dinhDangNgay(den));
  }

  const nutNhanh = [
    { nhan: 'Hôm nay', loai: 'hom-nay' },
    { nhan: 'Hôm qua', loai: 'hom-qua' },
    { nhan: 'Tuần này', loai: 'tuan-nay' },
    { nhan: 'Tháng này', loai: 'thang-nay' },
  ];

  return (
    <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'flex-end', marginBottom: 16 }}>
      <div style={{ display: 'flex', gap: 6 }}>
        {nutNhanh.map((n) => (
          <button key={n.loai} className="btn btn-secondary btn-sm" onClick={() => chonNhanh(n.loai)}>
            {n.nhan}
          </button>
        ))}
      </div>

      <label style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 12, color: 'var(--text-3)' }}>
        Từ ngày
        <input type="date" className="form-input" value={tuNgay} onChange={(e) => onDoiKhoang(e.target.value, denNgay)} />
      </label>
      <label style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 12, color: 'var(--text-3)' }}>
        Đến ngày
        <input type="date" className="form-input" value={denNgay} onChange={(e) => onDoiKhoang(tuNgay, e.target.value)} />
      </label>
      {(tuNgay || denNgay) && (
        <button className="btn btn-secondary btn-sm" onClick={() => onDoiKhoang('', '')}>
          <i className="ti ti-x" aria-hidden="true"></i> Xóa lọc
        </button>
      )}
    </div>
  );
}
