import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  guiYeuCauXacThuc,
  layTrangThaiXacThucCuaToi,
} from '../services/xacThucGiangVienService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';
import ThanhTienDoTaiLen from '../components/ThanhTienDoTaiLen';

const NHAN_TRANG_THAI = {
  CHO_DUYET: { nhan: 'Đang chờ duyệt', lop: 'badge-amber' },
  DA_DUYET: { nhan: 'Đã được xác thực', lop: 'badge-green' },
  TU_CHOI: { nhan: 'Cần gửi lại', lop: 'badge-red' },
};

function dinhDangNgay(ngay) {
  return ngay ? new Date(ngay).toLocaleDateString('vi-VN') : '—';
}

export default function XacThucGiangVien() {
  const navigate = useNavigate();
  const fileRef = useRef(null);
  const [trangThai, setTrangThai] = useState(null);
  const [ngaySinh, setNgaySinh] = useState('');
  const [bangCap, setBangCap] = useState('');
  const [file, setFile] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [dangGui, setDangGui] = useState(false);
  const [tienDo, setTienDo] = useState(null);
  const [loi, setLoi] = useState('');
  const [thanhCong, setThanhCong] = useState('');

  useEffect(() => {
    let daHuy = false;
    layTrangThaiXacThucCuaToi()
      .then(ketQua => { if (!daHuy) setTrangThai(ketQua); })
      .catch(err => { if (!daHuy) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
  }, []);

  function chonFile(event) {
    const fileMoi = event.target.files?.[0];
    setLoi('');
    setThanhCong('');
    if (!fileMoi) return;
    if (fileMoi.size > 10 * 1024 * 1024) {
      setFile(null);
      setLoi('File minh chứng không được vượt quá 10 MB.');
      return;
    }
    setFile(fileMoi);
  }

  async function guiYeuCau(event) {
    event.preventDefault();
    setLoi('');
    setThanhCong('');
    if (!file) {
      setLoi('Vui lòng chọn file minh chứng.');
      return;
    }
    setDangGui(true);
    setTienDo(0);
    try {
      const ketQua = await guiYeuCauXacThuc({ ngaySinh, bangCap: bangCap.trim(), file }, setTienDo);
      setTrangThai(ketQua);
      setNgaySinh('');
      setBangCap('');
      setFile(null);
      if (fileRef.current) fileRef.current.value = '';
      setThanhCong('Đã gửi yêu cầu xác thực. Quản trị viên sẽ xem xét hồ sơ của bạn.');
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
      setTienDo(null);
    }
  }

  const coTheGui = !trangThai || trangThai.trangThai === 'TU_CHOI';
  const thongTinTrangThai = trangThai && NHAN_TRANG_THAI[trangThai.trangThai];

  return (
    <div style={{ minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title"><i className="ti ti-certificate" aria-hidden="true"></i> Xác thực giảng viên</span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
      </div>
      <main style={{ maxWidth: 760, width: '100%', margin: '0 auto', padding: 20 }}>
        <div className="ds-page-head">
          <div>
            <h1>Đăng ký trở thành giảng viên</h1>
            <p>Cung cấp thông tin chuyên môn để bắt đầu tạo khóa học.</p>
          </div>
        </div>
        {loi && <div className="alert alert-danger">{loi}</div>}
        {thanhCong && <div className="alert alert-success">{thanhCong}</div>}
        {dangTai ? <p style={{ color: 'var(--text-3)' }}>Đang tải trạng thái...</p> : (
          <>
            {trangThai && (
              <div className="card" style={{ marginBottom: 18 }}>
                <div className="card-title">Trạng thái hồ sơ <span className={`badge ${thongTinTrangThai?.lop || 'badge-gray'}`}>{thongTinTrangThai?.nhan || trangThai.trangThai}</span></div>
                <div className="verify-summary">
                  <span>Ngày sinh: <strong>{dinhDangNgay(trangThai.ngaySinh)}</strong></span>
                  <span>Tuổi: <strong>{trangThai.tuoi ?? '—'}</strong></span>
                  <span>Ngày gửi: <strong>{dinhDangNgay(trangThai.ngayGui)}</strong></span>
                </div>
                {trangThai.trangThai === 'TU_CHOI' && <div className="alert alert-warning" style={{ marginTop: 14, marginBottom: 0 }}><span>Lý do từ chối: {trangThai.lyDoTuChoi || 'Vui lòng bổ sung hồ sơ và gửi lại.'}</span></div>}
                {trangThai.trangThai === 'DA_DUYET' && <p style={{ color: 'var(--success)', marginTop: 12 }}>Bạn đã có thể tạo và quản lý khóa học.</p>}
              </div>
            )}

            {coTheGui && (
              <div className="card">
                <div className="card-title">{trangThai?.trangThai === 'TU_CHOI' ? 'Gửi lại hồ sơ' : 'Thông tin xác thực'}</div>
                <p className="form-hint">Bạn phải đủ 18 tuổi và gửi bằng cấp hoặc chứng chỉ chuyên môn kèm file minh chứng.</p>
                <form onSubmit={guiYeuCau}>
                  <div className="form-group">
                    <label className="form-label" htmlFor="ngay-sinh">Ngày sinh</label>
                    <input id="ngay-sinh" className="form-input" type="date" value={ngaySinh} onChange={e => setNgaySinh(e.target.value)} required />
                  </div>
                  <div className="form-group">
                    <label className="form-label" htmlFor="bang-cap">Bằng cấp / chứng chỉ chuyên môn</label>
                    <textarea id="bang-cap" className="form-input" rows="3" style={{ height: 'auto', padding: '9px 10px', resize: 'vertical' }} maxLength={2000} value={bangCap} onChange={e => setBangCap(e.target.value)} placeholder="Ví dụ: Thạc sĩ Công nghệ thông tin, chứng chỉ..." required />
                  </div>
                  <div className="form-group">
                    <label className="form-label" htmlFor="file-minh-chung">File minh chứng</label>
                    <input id="file-minh-chung" ref={fileRef} className="form-input" type="file" accept=".pdf,.png,.jpg,.jpeg,.webp,.doc,.docx" onChange={chonFile} required />
                    <div className="form-hint">PDF, Word hoặc ảnh; tối đa 10 MB. File chỉ được quản trị viên có quyền xem.</div>
                    {file && <div className="file-name"><i className="ti ti-paperclip" aria-hidden="true"></i> {file.name}</div>}
                  </div>
                  {dangGui && <ThanhTienDoTaiLen phanTram={tienDo} nhan={tienDo == null ? 'Đang xử lý...' : 'Đang tải minh chứng...'} />}
                  <button className="btn btn-primary" type="submit" disabled={dangGui}>{dangGui ? 'Đang gửi...' : 'Gửi yêu cầu xác thực'}</button>
                </form>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
