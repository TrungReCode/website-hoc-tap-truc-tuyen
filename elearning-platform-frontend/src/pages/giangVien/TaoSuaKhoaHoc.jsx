import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { layDanhSachDanhMuc } from '../../services/danhMucService';
import { taoKhoaHoc, capNhatKhoaHoc, layChiTietChoTacGia, guiKiemDuyet, taiLenAnhBia } from '../../services/khoaHocService';
import { themChuong, xoaChuong, themBaiHoc, xoaBaiHoc, themCauHoi } from '../../services/noiDungKhoaHocService';
import ThanhTienDoTaiLen from '../../components/ThanhTienDoTaiLen';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';

const FORM_RONG = { tieuDe: '', mieuTa: '', maDanhMuc: '', gia: 0, phiDayHoc: '', anhBia: '' };

export default function TaoSuaKhoaHoc() {
  const { maKhoaHoc } = useParams();
  const dangSua = !!maKhoaHoc;
  const navigate = useNavigate();

  const [buoc, setBuoc] = useState(1);
  const [danhMucs, setDanhMucs] = useState([]);
  const [form, setForm] = useState(FORM_RONG);
  const [anhBiaFile, setAnhBiaFile] = useState(null);
  const [anhBiaXemTruoc, setAnhBiaXemTruoc] = useState('');
  const [dangTaiAnh, setDangTaiAnh] = useState(false);
  const [tienDoAnh, setTienDoAnh] = useState(null);
  const [khoaHoc, setKhoaHoc] = useState(null);
  const [dangXuLy, setDangXuLy] = useState(false);
  const [loi, setLoi] = useState('');

  // dangSua chỉ đúng ngay lúc mount (dựa vào URL). Sau khi tạo khóa học nháp
  // ở bước 1, URL được cập nhật âm thầm bằng history.replaceState (không qua
  // navigate của router) để tránh unmount lại wizard — nên useParams() sẽ
  // KHÔNG tự cập nhật theo. Dùng thêm state `khoaHoc` (đã có id thật) để suy
  // ra đúng trạng thái "đang sửa" cho tiêu đề trang.
  const dangSuaHienTai = dangSua || !!khoaHoc;

  useEffect(() => {
    layDanhSachDanhMuc().then(setDanhMucs).catch(() => {});
  }, []);

  useEffect(() => {
    if (!dangSua) return;
    layChiTietChoTacGia(maKhoaHoc)
      .then((kh) => {
        setKhoaHoc(kh);
        setForm({
          tieuDe: kh.tieuDe,
          mieuTa: kh.mieuTa || '',
          maDanhMuc: kh.maDanhMuc,
          gia: kh.gia,
          phiDayHoc: kh.phiDayHoc ?? '',
          anhBia: kh.anhBia || '',
        });
        if (kh.anhBia) setAnhBiaXemTruoc(kh.anhBia); // hiển thị ảnh bìa hiện có
      })
      .catch((err) => setLoi(layThongBaoLoi(err)));
  }, [dangSua, maKhoaHoc]);

  function taiLaiKhoaHoc(id) {
    return layChiTietChoTacGia(id).then(setKhoaHoc);
  }

  async function xuLyChonAnh(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    // Hiển thị preview tức thì bằng blob URL (không cần đợi upload xong)
    const blobUrl = URL.createObjectURL(file);
    setAnhBiaXemTruoc(blobUrl);
    setAnhBiaFile(file);

    // Upload ngay lên server để lấy URL thật
    setDangTaiAnh(true);
    setTienDoAnh(0);
    setLoi('');
    try {
      const url = await taiLenAnhBia(file, setTienDoAnh);
      setForm(f => ({ ...f, anhBia: url }));
    } catch (err) {
      setLoi('Tải ảnh bìa thất bại: ' + layThongBaoLoi(err));
      setAnhBiaXemTruoc('');
      setAnhBiaFile(null);
    } finally {
      setDangTaiAnh(false);
      setTienDoAnh(null);
    }
  }

  async function xuLyLuuBuoc1(e) {
    e.preventDefault();
    setLoi('');
    setDangXuLy(true);
    const yeuCau = {
      tieuDe: form.tieuDe,
      mieuTa: form.mieuTa,
      maDanhMuc: Number(form.maDanhMuc),
      gia: Number(form.gia) || 0,
      phiDayHoc: form.phiDayHoc === '' ? null : Number(form.phiDayHoc),
      anhBia: form.anhBia || null,
    };
    try {
      if (khoaHoc) {
        const ketQua = await capNhatKhoaHoc(khoaHoc.maKhoaHoc, yeuCau);
        setKhoaHoc(ketQua);
      } else {
        const ketQua = await taoKhoaHoc(yeuCau);
        setKhoaHoc(ketQua);
        // CHỈ cập nhật thanh địa chỉ (để F5 giữa chừng không tạo trùng khóa
        // học nháp), KHÔNG dùng navigate() của react-router — vì điều đó sẽ
        // khớp sang <Route> khác (kể cả trỏ đúng "/sua/:id") và làm unmount
        // lại component này, khiến state `buoc` bị reset về 1 và người dùng
        // lại thấy "nhảy trang" y như lỗi cũ, chỉ khác là nhảy đúng route.
        window.history.replaceState(null, '', `/giang-vien/khoa-hoc/sua/${ketQua.maKhoaHoc}`);
      }
      setBuoc(2);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLy(false);
    }
  }

  async function xuLyGuiDuyet() {
    if (!khoaHoc) return;
    setLoi('');
    setDangXuLy(true);
    try {
      await guiKiemDuyet(khoaHoc.maKhoaHoc);
      navigate('/giang-vien/khoa-hoc');
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLy(false);
    }
  }

  const tenBuoc = ['Thông tin thương mại', 'Giáo trình', 'Xem lại & Gửi duyệt'];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title">
          <i className="ti ti-edit" aria-hidden="true"></i> {dangSuaHienTai ? `Sửa: ${khoaHoc?.tieuDe || ''}` : 'Tạo khóa học mới'}
        </span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/giang-vien/khoa-hoc')}>
          Thoát
        </button>
      </div>

      <div style={{ padding: 20, maxWidth: 760, width: '100%', margin: '0 auto' }}>
        <div className="step-bar">
          {tenBuoc.map((ten, i) => (
            <div key={ten} style={{ display: 'flex', alignItems: 'center', flex: i < tenBuoc.length - 1 ? 1 : undefined }}>
              <div className="step-node">
                <div className={`step-circle ${buoc === i + 1 ? '' : buoc > i + 1 ? '' : 'inactive'}`}>
                  {buoc > i + 1 ? <i className="ti ti-check" aria-hidden="true"></i> : i + 1}
                </div>
                <span className="step-label">{ten}</span>
              </div>
              {i < tenBuoc.length - 1 && <div className="step-sep" />}
            </div>
          ))}
        </div>

        {loi && <div className="alert alert-danger">{loi}</div>}
        {khoaHoc?.lyDoTuChoi && khoaHoc.trangThai === 'CHO_BO_SUNG' && (
          <div className="alert alert-warning">
            <div>
              <strong>Quản trị viên yêu cầu bổ sung:</strong> {khoaHoc.lyDoTuChoi}
            </div>
            <div style={{ fontSize: 12, marginTop: 4 }}>
              Sau khi chỉnh sửa xong, hãy dùng nút "Gửi duyệt" ở bước cuối để gửi lại cho Quản trị viên.
            </div>
          </div>
        )}
        {khoaHoc?.lyDoTuChoi && khoaHoc.trangThai !== 'CHO_BO_SUNG' && (
          <div className="alert alert-warning">
            <div>
              <strong>Khóa học từng bị từ chối:</strong> {khoaHoc.lyDoTuChoi}
            </div>
          </div>
        )}

        {buoc === 1 && (
          <form onSubmit={xuLyLuuBuoc1} className="card">
            <div className="form-group">
              <label className="form-label">Tiêu đề khóa học</label>
              <input className="form-input" value={form.tieuDe} onChange={(e) => setForm({ ...form, tieuDe: e.target.value })} required />
            </div>
            <div className="form-group">
              <label className="form-label">Mô tả</label>
              <textarea
                className="form-input"
                style={{ height: 80, padding: 8 }}
                value={form.mieuTa}
                onChange={(e) => setForm({ ...form, mieuTa: e.target.value })}
              />
            </div>
            <div className="grid-2">
              <div className="form-group">
                <label className="form-label">Danh mục</label>
                <select className="form-input" value={form.maDanhMuc} onChange={(e) => setForm({ ...form, maDanhMuc: e.target.value })} required>
                  <option value="" disabled>
                    Chọn danh mục
                  </option>
                  {danhMucs.map((d) => (
                    <option key={d.maDanhMuc} value={d.maDanhMuc}>
                      {d.tenDanhMuc}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Ảnh bìa khóa học</label>
                {/* Vùng upload — bấm để chọn file hoặc kéo thả */}
                <div
                  className="upload-zone"
                  style={{ padding: 0, overflow: 'hidden', minHeight: 120, position: 'relative' }}
                  onClick={() => document.getElementById('inp-anh-bia').click()}
                >
                  {anhBiaXemTruoc ? (
                    <img
                      src={anhBiaXemTruoc}
                      alt="Ảnh bìa"
                      style={{ width: '100%', maxHeight: 180, objectFit: 'cover', display: 'block' }}
                    />
                  ) : (
                    <div style={{ padding: '30px 16px', textAlign: 'center' }}>
                      <i className="ti ti-photo" style={{ fontSize: 28 }} aria-hidden="true"></i>
                      <div style={{ marginTop: 6, fontSize: 13 }}>Bấm để chọn ảnh bìa</div>
                      <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 2 }}>JPG, PNG, WebP — tối đa 5 MB</div>
                    </div>
                  )}
                  {dangTaiAnh && (
                    <div style={{ position: 'absolute', inset: 0, background: 'rgba(255,255,255,.85)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '0 20px' }}>
                      <div style={{ width: '100%', maxWidth: 220 }}>
                        <ThanhTienDoTaiLen phanTram={tienDoAnh} nhan="Đang tải ảnh lên..." />
                      </div>
                    </div>
                  )}
                </div>
                <input
                  id="inp-anh-bia"
                  type="file"
                  accept="image/*"
                  style={{ display: 'none' }}
                  onChange={xuLyChonAnh}
                />
                {anhBiaXemTruoc && (
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    style={{ marginTop: 6 }}
                    onClick={() => { setAnhBiaXemTruoc(''); setAnhBiaFile(null); setForm(f => ({ ...f, anhBia: '' })); }}
                  >
                    <i className="ti ti-trash" aria-hidden="true"></i> Xóa ảnh
                  </button>
                )}
              </div>
            </div>
            <div className="grid-2">
              <div className="form-group">
                <label className="form-label">Giá Giấy phép Cá nhân (VNĐ)</label>
                <input className="form-input" type="number" min="0" value={form.gia} onChange={(e) => setForm({ ...form, gia: e.target.value })} required />
              </div>
              <div className="form-group">
                <label className="form-label">Giá Giấy phép Giảng dạy (VNĐ, để trống nếu không bán)</label>
                <input className="form-input" type="number" min="0" value={form.phiDayHoc} onChange={(e) => setForm({ ...form, phiDayHoc: e.target.value })} />
              </div>
            </div>
            <div className="btn-row">
              <button type="submit" className="btn btn-primary" disabled={dangXuLy || dangTaiAnh}>
                {dangTaiAnh ? 'Đang tải ảnh...' : dangXuLy ? 'Đang lưu...' : 'Lưu & tiếp tục'}
              </button>
            </div>
          </form>
        )}

        {buoc === 2 && khoaHoc && (
          <BuocGiaoTrinh khoaHoc={khoaHoc} taiLaiKhoaHoc={() => taiLaiKhoaHoc(khoaHoc.maKhoaHoc)} onQuayLai={() => setBuoc(1)} onTiepTuc={() => setBuoc(3)} />
        )}

        {buoc === 3 && khoaHoc && (
          <div className="card">
            <div className="card-title">Xem lại trước khi gửi duyệt</div>
            <p style={{ fontSize: 13, color: 'var(--text-2)', marginBottom: 4 }}>
              <strong>{khoaHoc.tieuDe}</strong> - {khoaHoc.danhSachChuong.length} chương,{' '}
              {khoaHoc.danhSachChuong.reduce((t, c) => t + c.danhSachBaiHoc.length, 0)} bài học
            </p>
            <p style={{ fontSize: 12, color: 'var(--text-3)', marginBottom: 14 }}>
              Sau khi gửi duyệt, khóa học sẽ chuyển sang trạng thái "Chờ duyệt" và bạn sẽ không thể chỉnh sửa cho đến khi Quản trị viên xử lý xong.
            </p>
            <div className="btn-row">
              <button className="btn btn-secondary" onClick={() => setBuoc(2)}>
                Quay lại
              </button>
              <button
                className="btn btn-primary"
                disabled={dangXuLy || khoaHoc.danhSachChuong.length === 0}
                onClick={xuLyGuiDuyet}
              >
                {dangXuLy ? 'Đang gửi...' : 'Gửi duyệt'}
              </button>
            </div>
            {khoaHoc.danhSachChuong.length === 0 && (
              <p style={{ fontSize: 12, color: 'var(--danger)', marginTop: 8 }}>Cần ít nhất 1 chương có bài học trước khi gửi duyệt.</p>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

function BuocGiaoTrinh({ khoaHoc, taiLaiKhoaHoc, onQuayLai, onTiepTuc }) {
  const [tieuDeChuongMoi, setTieuDeChuongMoi] = useState('');
  const [dangXuLy, setDangXuLy] = useState(false);
  const [loi, setLoi] = useState('');
  const [chuongDangThemBaiHoc, setChuongDangThemBaiHoc] = useState(null);

  async function xuLyThemChuong(e) {
    e.preventDefault();
    if (!tieuDeChuongMoi.trim()) return;
    setDangXuLy(true);
    setLoi('');
    try {
      await themChuong(khoaHoc.maKhoaHoc, { tieuDe: tieuDeChuongMoi, viTri: khoaHoc.danhSachChuong.length + 1 });
      setTieuDeChuongMoi('');
      await taiLaiKhoaHoc();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLy(false);
    }
  }

  async function xuLyXoaChuong(maChuong) {
    if (!window.confirm('Xóa chương này và toàn bộ bài học bên trong?')) return;
    try {
      await xoaChuong(maChuong);
      await taiLaiKhoaHoc();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    }
  }

  async function xuLyXoaBaiHoc(maBaiHoc) {
    if (!window.confirm('Xóa bài học này?')) return;
    try {
      await xoaBaiHoc(maBaiHoc);
      await taiLaiKhoaHoc();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    }
  }

  return (
    <div>
      {loi && <div className="alert alert-danger">{loi}</div>}

      {khoaHoc.danhSachChuong.map((chuong) => (
        <div key={chuong.maChuong} className="card sm">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <div style={{ fontSize: 13, fontWeight: 600 }}>{chuong.tieuDe}</div>
            <button className="btn btn-danger btn-sm" onClick={() => xuLyXoaChuong(chuong.maChuong)}>
              <i className="ti ti-trash" aria-hidden="true"></i>
            </button>
          </div>

          {chuong.danhSachBaiHoc.map((b) => (
            <div key={b.maBaiHoc} className="drag-item">
              <span className="drag-handle"><i className="ti ti-grip-vertical" aria-hidden="true"></i></span>
              <span className="di-type orig">{b.loai}</span>
              <span style={{ flex: 1 }}>{b.tieuDe}</span>
              {b.xemThu && <span className="badge badge-green">Xem</span>}
              {b.loai === 'QUIZ' && !(b.baiKiemTra?.danhSachCauHoi?.length) && (
                <span className="badge badge-amber" title="Bài kiểm tra chưa có câu hỏi — không thể gửi duyệt khóa học">
                  <i className="ti ti-alert-triangle" aria-hidden="true"></i> Chưa có câu hỏi
                </span>
              )}
              <div className="di-actions">
                <button className="btn btn-secondary btn-sm" onClick={() => xuLyXoaBaiHoc(b.maBaiHoc)}>
                  <i className="ti ti-trash" aria-hidden="true"></i>
                </button>
              </div>
            </div>
          ))}

          {chuongDangThemBaiHoc === chuong.maChuong ? (
            <FormThemBaiHoc
              maChuong={chuong.maChuong}
              viTriTiepTheo={chuong.danhSachBaiHoc.length + 1}
              onXong={async () => {
                setChuongDangThemBaiHoc(null);
                await taiLaiKhoaHoc();
              }}
              onHuy={() => setChuongDangThemBaiHoc(null)}
            />
          ) : (
            <button className="btn btn-secondary btn-sm" style={{ marginTop: 6 }} onClick={() => setChuongDangThemBaiHoc(chuong.maChuong)}>
              <i className="ti ti-plus" aria-hidden="true"></i> Thêm bài học
            </button>
          )}
        </div>
      ))}

      <form onSubmit={xuLyThemChuong} className="card sm" style={{ display: 'flex', gap: 8 }}>
        <input
          className="form-input"
          placeholder="Tên chương mới, vd: Chương 1 - Nhập môn"
          value={tieuDeChuongMoi}
          onChange={(e) => setTieuDeChuongMoi(e.target.value)}
        />
        <button type="submit" className="btn btn-primary btn-sm" disabled={dangXuLy}>
          Thêm chương
        </button>
      </form>

      <div className="btn-row">
        <button className="btn btn-secondary" onClick={onQuayLai}>
          Quay lại
        </button>
        <button className="btn btn-primary" disabled={khoaHoc.danhSachChuong.length === 0} onClick={onTiepTuc}>
          Tiếp tục
        </button>
      </div>
    </div>
  );
}

/** Form tải lên bài học mới (S23) - hiện inline ngay dưới danh sách bài học của 1 chương. */
function FormThemBaiHoc({ maChuong, viTriTiepTheo, onXong, onHuy }) {
  const [tieuDe, setTieuDe] = useState('');
  const [loai, setLoai] = useState('VIDEO');
  const [xemThu, setXemThu] = useState(true);
  const [file, setFile] = useState(null);
  const [diemToiThieu, setDiemToiThieu] = useState(70);
  const [thoiGian, setThoiGian] = useState('');
  const [dangTai, setDangTai] = useState(false);
  const [tienDoTai, setTienDoTai] = useState(null); // 0-100 trong lúc gửi lên backend, null = chưa bắt đầu/đang xử lý phía server
  const [loi, setLoi] = useState('');
  const [baiHocVuaTao, setBaiHocVuaTao] = useState(null);

  async function xuLySubmit(e) {
    e.preventDefault();
    if (!tieuDe.trim()) return;
    if (loai !== 'QUIZ' && !file) {
      setLoi('Vui lòng chọn tệp để tải lên');
      return;
    }
    setLoi('');
    setDangTai(true);
    setTienDoTai(loai === 'QUIZ' ? null : 0);
    try {
      const ketQua = await themBaiHoc(maChuong, {
        tieuDe,
        loai,
        viTri: viTriTiepTheo,
        xemThu,
        diemToiThieu: loai === 'QUIZ' ? Number(diemToiThieu) : undefined,
        thoiGian: loai === 'QUIZ' && thoiGian !== '' ? Number(thoiGian) : undefined,
        file,
      }, (phanTram) => {
        // Khi trình duyệt gửi xong 100% lên backend, video vẫn còn đang được
        // backend tải tiếp lên Cloudinary — chuyển sang trạng thái vô định
        // (tienDoTai = null) để không gây hiểu lầm là đã xong.
        setTienDoTai(phanTram >= 100 ? null : phanTram);
      });
      if (loai === 'QUIZ') {
        setBaiHocVuaTao(ketQua); // cho phép thêm câu hỏi ngay, chưa đóng form
      } else {
        onXong();
      }
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangTai(false);
      setTienDoTai(null);
    }
  }

  if (baiHocVuaTao) {
    return <FormThemCauHoi maBaiHoc={baiHocVuaTao.maBaiHoc} onXongTatCa={onXong} />;
  }

  return (
    <form onSubmit={xuLySubmit} className="card sm" style={{ background: 'var(--surface)', marginTop: 8 }}>
      {loi && <div className="alert alert-danger">{loi}</div>}
      <div className="form-group">
        <label className="form-label">Tiêu đề bài học</label>
        <input className="form-input" value={tieuDe} onChange={(e) => setTieuDe(e.target.value)} required />
      </div>
      <div className="grid-2">
        <div className="form-group">
          <label className="form-label">Loại bài học</label>
          <select className="form-input" value={loai} onChange={(e) => setLoai(e.target.value)}>
            <option value="VIDEO">Video (Max 100MB)</option>
            <option value="PDF">PDF</option>
            <option value="DOC">Tài liệu (DOC)</option>
            <option value="QUIZ">Quiz (Bài kiểm tra)</option>
          </select>
        </div>
        <div className="form-group" style={{ display: 'flex', alignItems: 'flex-end', gap: 8 }}>
          <button type="button" className={`toggle ${xemThu ? 'on' : ''}`} onClick={() => setXemThu(!xemThu)} />
          <span style={{ fontSize: 12, color: 'var(--text-2)' }}>Cho phép xem thử miễn phí</span>
        </div>
      </div>

      {loai === 'QUIZ' ? (
        <div className="grid-2">
          <div className="form-group">
            <label className="form-label">Điểm tối thiểu để đạt (%)</label>
            <input className="form-input" type="number" min="0" max="100" value={diemToiThieu} onChange={(e) => setDiemToiThieu(e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Thời gian làm bài (phút, để trống nếu không giới hạn)</label>
            <input className="form-input" type="number" min="1" value={thoiGian} onChange={(e) => setThoiGian(e.target.value)} />
          </div>
        </div>
      ) : (
        <div className="form-group">
          <label className="form-label">Tệp {loai === 'VIDEO' ? 'video' : 'tài liệu'}</label>
          <div className="upload-zone" onClick={() => document.getElementById(`tep-${maChuong}`).click()}>
            <i className="ti ti-upload" style={{ fontSize: 22 }} aria-hidden="true"></i>
            <div style={{ marginTop: 6 }}>{file ? file.name : 'Bấm để chọn tệp hoặc kéo thả vào đây'}</div>
          </div>
          <input
            id={`tep-${maChuong}`}
            type="file"
            style={{ display: 'none' }}
            accept={loai === 'VIDEO' ? 'video/*' : '.pdf,.doc,.docx'}
            onChange={(e) => setFile(e.target.files?.[0] || null)}
          />
        </div>
      )}

      {dangTai && loai !== 'QUIZ' && (
        <ThanhTienDoTaiLen
          phanTram={tienDoTai}
          nhan={tienDoTai == null ? 'Đang xử lý trên máy chủ...' : 'Đang tải lên...'}
        />
      )}

      <div className="btn-row">
        <button type="submit" className="btn btn-primary btn-sm" disabled={dangTai}>
          {dangTai ? 'Đang tải lên...' : loai === 'QUIZ' ? 'Tạo bài kiểm tra' : 'Tải lên & thêm bài học'}
        </button>
        <button type="button" className="btn btn-secondary btn-sm" onClick={onHuy} disabled={dangTai}>
          Hủy
        </button>
      </div>
    </form>
  );
}

/** Thêm câu hỏi trắc nghiệm cho bài học loại QUIZ vừa tạo, có thể thêm nhiều câu liên tiếp. */
function FormThemCauHoi({ maBaiHoc, onXongTatCa }) {
  const [noiDung, setNoiDung] = useState('');
  const [loaiCauHoi, setLoaiCauHoi] = useState('MOT_DAP_AN');
  const [dapAn, setDapAn] = useState([
    { id: 1, noiDung: '', dapAnDung: false },
    { id: 2, noiDung: '', dapAnDung: false },
  ]);
  const [soCauDaThem, setSoCauDaThem] = useState(0);
  const [dangXuLy, setDangXuLy] = useState(false);
  const [dangHuy, setDangHuy] = useState(false);
  const [loi, setLoi] = useState('');

  function capNhatDapAn(i, truong, giaTri) {
    setDapAn(dapAn.map((d, idx) => (idx === i ? { ...d, [truong]: giaTri } : d)));
  }

  function themDongDapAn() {
    setDapAn([...dapAn, { id: dapAn.length + 1, noiDung: '', dapAnDung: false }]);
  }

  async function xuLyThemCauHoi(e) {
    e.preventDefault();
    if (!noiDung.trim() || dapAn.some((d) => !d.noiDung.trim())) {
      setLoi('Vui lòng nhập đầy đủ nội dung câu hỏi và các phương án');
      return;
    }
    if (!dapAn.some((d) => d.dapAnDung)) {
      setLoi('Cần chọn ít nhất 1 đáp án đúng');
      return;
    }
    const soDapAnDung = dapAn.filter((d) => d.dapAnDung).length;
    if (loaiCauHoi === 'MOT_DAP_AN' && soDapAnDung > 1) {
      setLoi('Câu hỏi "1 đáp án đúng" chỉ được đánh dấu đúng cho đúng 1 phương án. Nếu muốn nhiều đáp án đúng, đổi loại câu hỏi thành "Nhiều đáp án đúng".');
      return;
    }
    setLoi('');
    setDangXuLy(true);
    try {
      await themCauHoi(maBaiHoc, { noiDung, viTri: soCauDaThem + 1, loaiCauHoi, dapAn });
      setSoCauDaThem(soCauDaThem + 1);
      setNoiDung('');
      setLoaiCauHoi('MOT_DAP_AN');
      setDapAn([
        { id: 1, noiDung: '', dapAnDung: false },
        { id: 2, noiDung: '', dapAnDung: false },
      ]);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLy(false);
    }
  }

  async function xuLyThoatKhongCauHoi() {
    // Yêu cầu: KHÔNG cho lưu bài kiểm tra rỗng vào giáo trình — thay vì lưu
    // kèm cảnh báo, tự động xóa hẳn khung Quiz vừa tạo (chưa có câu hỏi nào
    // nên chắc chắn chưa có ai làm bài, xóa cứng an toàn tuyệt đối).
    setDangHuy(true);
    setLoi('');
    try {
      await xoaBaiHoc(maBaiHoc);
      onXongTatCa();
    } catch (err) {
      // Trường hợp hiếm: backend chặn xóa (VD: khóa học từng có học viên sở
      // hữu). Báo rõ cho giảng viên biết thay vì lặng lẽ để lại Quiz rỗng.
      setLoi(layThongBaoLoi(err) + ' Vui lòng thêm ít nhất 1 câu hỏi rồi mới thoát.');
    } finally {
      setDangHuy(false);
    }
  }

  return (
    <div className="card sm" style={{ background: 'var(--surface)', marginTop: 8 }}>
      <div className="card-sub" style={{ marginBottom: 10 }}>
        Đã thêm {soCauDaThem} câu hỏi. Thêm câu hỏi cho bài kiểm tra (đánh dấu phương án đúng bằng cách bấm vào nó):
        {soCauDaThem === 0 && (
          <div style={{ color: 'var(--danger)', marginTop: 4, fontSize: 12 }}>
            Bài kiểm tra chưa có câu hỏi nào sẽ <strong>không được lưu</strong> - nhấn "Xong" lúc này sẽ tự hủy bài kiểm tra.
          </div>
        )}
      </div>
      {loi && <div className="alert alert-danger">{loi}</div>}
      <form onSubmit={xuLyThemCauHoi}>
        <div className="form-group">
          <input className="form-input" placeholder="Nội dung câu hỏi" value={noiDung} onChange={(e) => setNoiDung(e.target.value)} />
        </div>
        <div className="form-group">
          <label className="form-label">Loại câu hỏi</label>
          <select className="form-input" value={loaiCauHoi} onChange={(e) => setLoaiCauHoi(e.target.value)}>
            <option value="MOT_DAP_AN">1 đáp án đúng (chọn 1)</option>
            <option value="NHIEU_DAP_AN">Nhiều đáp án đúng (chọn nhiều)</option>
            <option value="DUNG_SAI">Đúng / Sai</option>
          </select>
        </div>
        {dapAn.map((d, i) => (
          <div key={d.id} style={{ display: 'flex', gap: 8, marginBottom: 6, alignItems: 'center' }}>            <input
              type="checkbox"
              checked={d.dapAnDung}
              onChange={(e) => capNhatDapAn(i, 'dapAnDung', e.target.checked)}
              title="Đánh dấu là đáp án đúng"
            />
            <input
              className="form-input"
              placeholder={`Phương án ${i + 1}`}
              value={d.noiDung}
              onChange={(e) => capNhatDapAn(i, 'noiDung', e.target.value)}
            />
          </div>
        ))}
        <div className="btn-row">
          <button type="button" className="btn btn-secondary btn-sm" onClick={themDongDapAn}>
            + Thêm phương án
          </button>
          <button type="submit" className="btn btn-primary btn-sm" disabled={dangXuLy}>
            {dangXuLy ? 'Đang lưu...' : 'Lưu câu hỏi & thêm câu tiếp theo'}
          </button>
          <button
            type="button"
            className="btn btn-success btn-sm"
            disabled={dangHuy}
            onClick={() => soCauDaThem === 0 ? xuLyThoatKhongCauHoi() : onXongTatCa()}
          >
            {dangHuy ? 'Đang hủy bài kiểm tra rỗng...' : 'Xong, quay lại giáo trình'}
          </button>
        </div>
      </form>
    </div>
  );
}