import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layLopHocVien } from '../../services/lopHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';

export default function LopHocDaMua() {
  const navigate = useNavigate();

  const [danhSach, setDanhSach] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    let daHuy = false;

    layLopHocVien()
      .then((ds) => {
        if (!daHuy) setDanhSach(ds || []);
      })
      .catch((err) => {
        if (!daHuy) setLoi(layThongBaoLoi(err));
      })
      .finally(() => {
        if (!daHuy) setDangTai(false);
      });

    return () => {
      daHuy = true;
    };
  }, []);

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Lớp học của tôi</h1>
        <p>Danh sách các lớp học bạn đã tham gia.</p>
      </div>

      {dangTai && <p>Đang tải dữ liệu...</p>}
      {loi && <div className="alert alert-danger">{loi}</div>}

      {!dangTai && !loi && danhSach.length === 0 && (
        <div className="card empty-state">
          <p>Bạn chưa tham gia lớp học nào.</p>
        </div>
      )}

      {!dangTai && !loi && danhSach.length > 0 && (
        <div className="tc-card-grid">
          {danhSach.map((lop) => (
            <div
              key={lop.maLopHoc}
              className="card sm tc-learn-card"
              onClick={() => navigate(`/lop-hoc/${lop.maLopHoc}`)}
              style={{ cursor: 'pointer' }}
            >
              {lop.anhBiaKhoaHoc && (
                <img
                  src={lop.anhBiaKhoaHoc}
                  alt={lop.tenKhoaHoc}
                  className="tc-learn-thumb"
                />
              )}

              <div className="tc-learn-body">
                <h4 className="tc-learn-title">
                  {lop.tenKhoaHoc}
                </h4>

                <p className="tc-learn-meta">
                  GV: {lop.tenGiangVien || '—'} · Mã lớp: {lop.maLop}
                </p>

                <button className="btn btn-primary btn-sm">
                  Vào lớp
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
