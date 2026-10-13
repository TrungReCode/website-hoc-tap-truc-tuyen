import { Fragment, useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { timKiemCongKhai } from '../services/khoaHocService';
import { layDanhSachDanhMuc } from '../services/danhMucService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

const TUY_CHON_SAP_XEP = [
  { nhan: 'Mới nhất', gia_tri: 'ngayTao,desc' },
  { nhan: 'Giá tăng dần', gia_tri: 'gia,asc' },
  { nhan: 'Giá giảm dần', gia_tri: 'gia,desc' },
];

const TUY_CHON_GIA = [
  { nhan: 'Tất cả', gia_tri: '' },
  { nhan: 'Miễn phí', gia_tri: 'MIEN_PHI' },
  { nhan: 'Có phí', gia_tri: 'CO_PHI' },
];

export default function ChoKhoaHoc() {
  const navigate = useNavigate();
  const [hoTen] = useState(localStorage.getItem('hoTen'));
  const [danhMucs, setDanhMucs] = useState([]);
  const [maDanhMuc, setMaDanhMuc] = useState(null);
  const [tuKhoa, setTuKhoa] = useState('');
  const [oTimKiem, setOTimKiem] = useState('');
  const [sapXep, setSapXep] = useState(TUY_CHON_SAP_XEP[0].gia_tri);
  const [caNhanLoc, setCaNhanLoc] = useState('');
  const [dayHocLoc, setDayHocLoc] = useState('');
  const [trang, setTrang] = useState({ content: [], totalPages: 0, totalElements: 0, number: 0 });
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    layDanhSachDanhMuc().then(setDanhMucs).catch(() => { });
  }, []);

  // Backend trả danh sách PHẲNG kèm maDanhMucCha (null nếu là danh mục cha).
  // Dựng lại thành cây cha-con ở đây để hiển thị đúng phân cấp trong dropdown,
  // thay vì liệt kê ngang hàng như chip trước đây (không phân biệt được đâu
  // là danh mục cha, đâu là danh mục con thuộc về cha nào).
  const cayDanhMuc = useMemo(() => {
    const danhMucCha = danhMucs.filter((d) => !d.maDanhMucCha);
    const conTheoCha = new Map();
    danhMucs.forEach((d) => {
      if (d.maDanhMucCha) {
        const ds = conTheoCha.get(d.maDanhMucCha) || [];
        ds.push(d);
        conTheoCha.set(d.maDanhMucCha, ds);
      }
    });
    return danhMucCha.map((cha) => ({ ...cha, con: conTheoCha.get(cha.maDanhMuc) || [] }));
  }, [danhMucs]);

  const taiKhoaHoc = useCallback(
    async (trangSo = 0) => {
      setDangTai(true);
      setLoi('');
      try {
        const ketQua = await timKiemCongKhai({
          maDanhMuc, tuKhoa, caNhanLoc, dayHocLoc, page: trangSo, size: 12, sort: sapXep,
        });
        setTrang(ketQua);
      } catch (err) {
        setLoi(layThongBaoLoi(err));
      } finally {
        setDangTai(false);
      }
    },
    [maDanhMuc, tuKhoa, caNhanLoc, dayHocLoc, sapXep]
  );

  useEffect(() => {
    taiKhoaHoc(0);
  }, [taiKhoaHoc]);

  function xuLyTimKiem(e) {
    e.preventDefault();
    setTuKhoa(oTimKiem.trim());
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="global-header">
        <div style={{ fontSize: 16, fontWeight: 600, color: 'var(--primary)', cursor: 'pointer' }} onClick={() => navigate('/trang-chu')}>
          <i className="ti ti-school" aria-hidden="true"></i> EduPlatform
        </div>
        <form onSubmit={xuLyTimKiem} style={{ flex: 1, maxWidth: 320 }}>
          <input
            className="gh-search"
            placeholder="Tìm kiếm khóa học..."
            value={oTimKiem}
            onChange={(e) => setOTimKiem(e.target.value)}
          />
        </form>
        <div className="gh-spacer" />
        {hoTen ? (
          <div className="avatar" title={hoTen} style={{ cursor: 'pointer' }} onClick={() => navigate('/trang-chu')}>
            {hoTen.charAt(0).toUpperCase()}
          </div>
        ) : (
          <button className="btn btn-primary btn-sm" onClick={() => navigate('/dang-nhap')}>
            Đăng nhập
          </button>
        )}
      </div>

      <div style={{ padding: '16px 20px 0 20px' }}>
        <div className="mp-hero">
          <h1>Khám phá các khóa học, thanh toán an toàn qua VNPay với cơ chế ký quỹ hội tụ.</h1>
          <div className="mp-hero-actions">
            <button className="btn btn-accent" onClick={() => navigate('/giang-vien')}>
              <i className="ti ti-chalkboard" aria-hidden="true"></i> Trở thành người hướng dẫn
            </button>
            {!hoTen && (
              <button className="btn btn-outline-light" onClick={() => navigate('/dang-ky')}>
                Đăng ký miễn phí
              </button>
            )}
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        <div style={{ width: 200, borderRight: '1px solid var(--border)', padding: 16, overflowY: 'auto', flexShrink: 0 }}>
          <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-3)', marginBottom: 8 }}>DANH MỤC</div>
          <select
            className="form-input"
            style={{ fontSize: 12, height: 30 }}
            value={maDanhMuc ?? ''}
            onChange={(e) => setMaDanhMuc(e.target.value ? Number(e.target.value) : null)}
          >
            <option value="">Tất cả khóa học</option>
            {cayDanhMuc.map((cha) => (
              // Không dùng <optgroup> nữa vì label của optgroup KHÔNG BAO GIỜ
              // click/chọn được (giới hạn của HTML, không phải lỗi CSS/JS) —
              // đưa danh mục cha thành 1 <option> bình thường để bấm chọn được,
              // các con thụt lề bên dưới bằng dấu "—" để vẫn thấy rõ phân cấp.
              <Fragment key={cha.maDanhMuc}>
                <option value={cha.maDanhMuc}>{cha.tenDanhMuc}</option>
                {cha.con.map((con) => (
                  <option key={con.maDanhMuc} value={con.maDanhMuc}>
                    &nbsp;&nbsp;- {con.tenDanhMuc}
                  </option>
                ))}
              </Fragment>
            ))}
          </select>

          <div style={{ marginTop: 16, fontSize: 12, fontWeight: 600, color: 'var(--text-3)', marginBottom: 8 }}>SẮP XẾP</div>
          <select className="form-input" style={{ fontSize: 12, height: 30 }} value={sapXep} onChange={(e) => setSapXep(e.target.value)}>
            {TUY_CHON_SAP_XEP.map((t) => (
              <option key={t.gia_tri} value={t.gia_tri}>
                {t.nhan}
              </option>
            ))}
          </select>

          <div style={{ marginTop: 16, fontSize: 12, fontWeight: 600, color: 'var(--text-3)', marginBottom: 8 }}>GIÁ MUA CÁ NHÂN</div>
          <select className="form-input" style={{ fontSize: 12, height: 30 }} value={caNhanLoc} onChange={(e) => setCaNhanLoc(e.target.value)}>
            {TUY_CHON_GIA.map((t) => (
              <option key={t.gia_tri} value={t.gia_tri}>
                {t.nhan}
              </option>
            ))}
          </select>

          <div style={{ marginTop: 16, fontSize: 12, fontWeight: 600, color: 'var(--text-3)', marginBottom: 8 }}>PHÍ MỞ LỚP DẠY</div>
          <select className="form-input" style={{ fontSize: 12, height: 30 }} value={dayHocLoc} onChange={(e) => setDayHocLoc(e.target.value)}>
            {TUY_CHON_GIA.map((t) => (
              <option key={t.gia_tri} value={t.gia_tri}>
                {t.nhan}
              </option>
            ))}
          </select>
        </div>

        <div style={{ flex: 1, padding: 16, overflowY: 'auto' }}>
          {loi && <div className="alert alert-danger">{loi}</div>}

          {!dangTai && (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <span style={{ fontSize: 13, color: 'var(--text-2)' }}>
                Hiển thị <strong>{trang.content.length}</strong> / {trang.totalElements} khóa học
              </span>
            </div>
          )}

          {dangTai ? (
            <div className="empty-state">Đang tải khóa học...</div>
          ) : trang.content.length === 0 ? (
            <div className="empty-state">
              <i className="ti ti-mood-empty" style={{ fontSize: 32 }} aria-hidden="true"></i>
              <p style={{ marginTop: 8 }}>Không tìm thấy khóa học phù hợp</p>
            </div>
          ) : (
            <div className="grid-3" style={{ gap: 14 }}>
              {trang.content.map((kh) => (
                <div key={kh.maKhoaHoc} className="course-card" onClick={() => navigate(`/khoa-hoc/${kh.maKhoaHoc}`)}>
                  <div className="course-thumb">
                    {kh.anhBia ? (
                      <img src={kh.anhBia} alt={kh.tieuDe} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    ) : (
                      <i className="ti ti-book-2" aria-hidden="true"></i>
                    )}
                  </div>
                  <div className="course-info">
                    <div className="course-title">{kh.tieuDe}</div>
                    <div className="course-meta">Giảng viên: {kh.tenTacGia || 'Đang cập nhật'}</div>
                    <div className="star-row">
                      <span className="star">{'★'.repeat(Math.round(kh.diemTrungBinh || 0))}{'☆'.repeat(5 - Math.round(kh.diemTrungBinh || 0))}</span>
                      <span style={{ fontSize: 11, color: 'var(--text-3)' }}>
                        {kh.diemTrungBinh ? kh.diemTrungBinh.toFixed(1) : 'Mới'} ({kh.soLuongDanhGia || 0})
                      </span>
                    </div>
                    <div style={{ marginTop: 8 }}>
                      {/* Giá học viên */}
                      <div className="course-price" style={{ ...(Number(kh.gia) === 0 ? { color: 'var(--success)' } : {}), fontSize: 14 }}>
                        Giá mua: {Number(kh.gia) === 0 ? 'Miễn phí' : `${Number(kh.gia).toLocaleString('vi-VN')} đ`}
                      </div>

                      {/* Phí giấy phép giảng dạy (Chỉ hiện khi phiDayHoc khác null) */}
                      {kh.phiDayHoc != null && (
                        <div style={{ fontSize: 14, color: '#d97706', fontWeight: 600, marginTop: 4, display: 'flex', alignItems: 'center', gap: 4 }}>
                          Phí dạy học: {Number(kh.phiDayHoc) === 0 ? 'Miễn phí' : `${Number(kh.phiDayHoc).toLocaleString('vi-VN')} đ`}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {trang.totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', gap: 6, marginTop: 20 }}>
              <button className="btn btn-secondary btn-sm" disabled={trang.number === 0} onClick={() => taiKhoaHoc(trang.number - 1)}>
                ‹
              </button>
              {Array.from({ length: trang.totalPages }, (_, i) => i)
                .slice(Math.max(0, trang.number - 2), trang.number + 3)
                .map((i) => (
                  <button key={i} className={`btn btn-sm ${i === trang.number ? 'btn-primary' : 'btn-secondary'}`} onClick={() => taiKhoaHoc(i)}>
                    {i + 1}
                  </button>
                ))}
              <button className="btn btn-secondary btn-sm" disabled={trang.number >= trang.totalPages - 1} onClick={() => taiKhoaHoc(trang.number + 1)}>
                ›
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}