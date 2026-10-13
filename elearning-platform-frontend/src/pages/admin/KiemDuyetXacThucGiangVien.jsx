import { useCallback, useEffect, useState } from 'react';
import DashboardShell, { layNavTheoVaiTro } from '../../components/DashboardShell';
import {
  layDanhSachXacThucChoAdmin,
  duyetXacThuc,
  taiFileMinhChung,
} from '../../services/xacThucGiangVienService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';

const TABS = [
  { value: 'CHO_DUYET', label: 'Chờ duyệt' },
  { value: 'DA_DUYET', label: 'Đã duyệt' },
  { value: 'TU_CHOI', label: 'Đã từ chối' },
];
const NHAN = {
  CHO_DUYET: ['Chờ duyệt', 'badge-amber'],
  DA_DUYET: ['Đã duyệt', 'badge-green'],
  TU_CHOI: ['Từ chối', 'badge-red'],
};

function dinhDangNgay(ngay) {
  return ngay ? new Date(ngay).toLocaleDateString('vi-VN') : '—';
}

export default function KiemDuyetXacThucGiangVien() {
  const { navItems, roleLabel } = layNavTheoVaiTro();
  const [trangThai, setTrangThai] = useState('CHO_DUYET');
  const [trang, setTrang] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0 });
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [dangXuLy, setDangXuLy] = useState(null);
  const [lyDo, setLyDo] = useState('');

  const taiDanhSach = useCallback((page = 0, filter = trangThai) => {
    setDangTai(true);
    setLoi('');
    layDanhSachXacThucChoAdmin({ trangThai: filter, page, size: 10 })
      .then(setTrang)
      .catch(err => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, [trangThai]);

  useEffect(() => { taiDanhSach(0, trangThai); }, [taiDanhSach, trangThai]);

  async function xuLyDuyet(yeuCau, chapDuyet) {
    if (!chapDuyet && !lyDo.trim()) {
      setLoi('Vui lòng nhập lý do khi từ chối hồ sơ.');
      return;
    }
    setDangXuLy(yeuCau.maXacThuc);
    setLoi('');
    try {
      await duyetXacThuc(yeuCau.maXacThuc, { chapDuyet, lyDoTuChoi: chapDuyet ? '' : lyDo.trim() });
      setLyDo('');
      taiDanhSach(trang.number, trangThai);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLy(null);
    }
  }

  async function xuLyTaiFile(yeuCau) {
    try {
      await taiFileMinhChung(yeuCau.maXacThuc, yeuCau.tenFileGoc);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    }
  }

  return (
    <DashboardShell roleLabel={roleLabel} navItems={navItems} crumb="Xác thực giảng viên" actions={<span className="badge badge-amber">{trang.totalElements} hồ sơ</span>}>
      <div className="ds-page-head">
        <div><h1>Xác thực giảng viên</h1><p>Kiểm tra hồ sơ chuyên môn trước khi cho phép tạo khóa học.</p></div>
      </div>
      {loi && <div className="alert alert-danger">{loi}</div>}
      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 14 }}>
        {TABS.map(tab => <button key={tab.value} className={`btn btn-sm ${trangThai === tab.value ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTrangThai(tab.value)}>{tab.label}</button>)}
      </div>
      {dangTai ? <p style={{ color: 'var(--text-3)' }}>Đang tải hồ sơ...</p> : trang.content.length === 0 ? (
        <div className="empty-state"><i className="ti ti-inbox" style={{ fontSize: 32 }} aria-hidden="true"></i><p style={{ marginTop: 8 }}>Không có hồ sơ trong trạng thái này.</p></div>
      ) : (
        <div className="card" style={{ overflowX: 'auto' }}>
          <table className="data-table">
            <thead><tr><th>Người dùng</th><th>Ngày sinh</th><th>Chuyên môn</th><th>File minh chứng</th><th>Ngày gửi</th><th>Trạng thái</th><th style={{ minWidth: 280 }}>Xử lý</th></tr></thead>
            <tbody>{trang.content.map(yeuCau => {
              const [nhan, lop] = NHAN[yeuCau.trangThai] || [yeuCau.trangThai, 'badge-gray'];
              const dangCho = yeuCau.trangThai === 'CHO_DUYET';
              return <tr key={yeuCau.maXacThuc}>
                <td><strong>{yeuCau.hoTenNguoiDung || `Người dùng #${yeuCau.maNguoiDung}`}</strong><div style={{ fontSize: 11, color: 'var(--text-3)' }}>{yeuCau.emailNguoiDung || '—'}</div></td>
                <td>{dinhDangNgay(yeuCau.ngaySinh)}<div style={{ fontSize: 11, color: 'var(--text-3)' }}>{yeuCau.tuoi} tuổi</div></td>
                <td style={{ maxWidth: 220, whiteSpace: 'pre-wrap' }}>{yeuCau.bangCap}</td>
                <td><button className="btn btn-secondary btn-sm" onClick={() => xuLyTaiFile(yeuCau)}><i className="ti ti-download" aria-hidden="true"></i> Tải file</button></td>
                <td>{dinhDangNgay(yeuCau.ngayGui)}</td>
                <td><span className={`badge ${lop}`}>{nhan}</span></td>
                <td>{dangCho ? <div><div style={{ display: 'flex', gap: 6 }}><button className="btn btn-primary btn-sm" disabled={dangXuLy === yeuCau.maXacThuc} onClick={() => xuLyDuyet(yeuCau, true)}>Duyệt</button><button className="btn btn-danger btn-sm" disabled={dangXuLy === yeuCau.maXacThuc} onClick={() => xuLyDuyet(yeuCau, false)}>Từ chối</button></div><textarea className="form-input" rows="2" placeholder="Lý do từ chối (bắt buộc khi từ chối)" value={lyDo} onChange={e => setLyDo(e.target.value)} style={{ marginTop: 7, height: 'auto', padding: 7, resize: 'vertical' }} /></div> : yeuCau.lyDoTuChoi || '—'}</td>
              </tr>;
            })}</tbody>
          </table>
        </div>
      )}
      {trang.totalPages > 1 && <div style={{ display: 'flex', justifyContent: 'center', gap: 6, marginTop: 20 }}><button className="btn btn-secondary btn-sm" disabled={trang.number === 0} onClick={() => taiDanhSach(trang.number - 1)}>‹</button>{Array.from({ length: trang.totalPages }, (_, index) => <button key={index} className={`btn btn-sm ${index === trang.number ? 'btn-primary' : 'btn-secondary'}`} onClick={() => taiDanhSach(index)}>{index + 1}</button>)}<button className="btn btn-secondary btn-sm" disabled={trang.number >= trang.totalPages - 1} onClick={() => taiDanhSach(trang.number + 1)}>›</button></div>}
    </DashboardShell>
  );
}
