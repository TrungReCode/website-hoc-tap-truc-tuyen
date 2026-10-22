import { nhanLoaiCauHoi } from '../../utils/cauHoi';
import './quiz.css';

/**
 * Phần phương án/đáp án của 1 câu hỏi ở chế độ XEM (không phải làm bài) — dùng cho
 * giảng viên, quản trị viên và cả bản xem trước dành cho học viên/khách.
 *
 * Tự nhận biết đáp án có được tiết lộ hay không: backend chỉ gửi cờ dapAnDung (khác
 * null) cho giảng viên/quản trị; với học viên/khách, backend đã che hết (dapAnDung = null,
 * và với TRA_LOI_NGAN/SAP_XEP/GHEP_CAP còn che cả chính cấu trúc dữ liệu). Frontend
 * KHÔNG được tự suy ra đáp án khi chưa được tiết lộ.
 */
export default function DapAnXemTruoc({ cauHoi }) {
  const loai = cauHoi.loaiCauHoi || 'MOT_DAP_AN';
  const dapAn = cauHoi.dapAn || [];
  const daLo = dapAn.some((d) => d.dapAnDung !== null && d.dapAnDung !== undefined);
  const dangMoi = loai === 'TRA_LOI_NGAN' || loai === 'SAP_XEP' || loai === 'GHEP_CAP';

  return (
    <>
      {dangMoi && <div className="qz-tip">Dạng câu hỏi: {nhanLoaiCauHoi(loai)}</div>}
      {loai === 'TRA_LOI_NGAN' && <TraLoiNgan dapAn={dapAn} daLo={daLo} />}
      {loai === 'SAP_XEP' && <SapXep dapAn={dapAn} daLo={daLo} />}
      {loai === 'GHEP_CAP' && <GhepCap dapAn={dapAn} daLo={daLo} luaChonGhep={cauHoi.luaChonGhep} />}
      {!dangMoi &&
        dapAn.map((d) => (
          <div key={d.id} className={`preview-quiz-option ${d.dapAnDung ? 'correct' : ''}`}>
            {d.dapAnDung && <i className="ti ti-check" aria-hidden="true"></i>} {d.noiDung}
          </div>
        ))}
    </>
  );
}

function TraLoiNgan({ dapAn, daLo }) {
  if (!daLo) {
    return <div className="preview-quiz-option">Học viên tự gõ câu trả lời.</div>;
  }
  return (
    <div className="preview-quiz-option correct">
      <i className="ti ti-check" aria-hidden="true"></i> Đáp án chấp nhận: {dapAn.map((d) => d.noiDung).join('  ·  ')}
    </div>
  );
}

function SapXep({ dapAn, daLo }) {
  return (
    <>
      {!daLo && <div className="qz-tip">Học viên sắp xếp các mục sau theo đúng thứ tự.</div>}
      {dapAn.map((d, i) => (
        <div key={d.id} className={`preview-quiz-option ${daLo ? 'correct' : ''}`}>
          {daLo ? `${i + 1}. ` : ''}{d.noiDung}
        </div>
      ))}
    </>
  );
}

function GhepCap({ dapAn, daLo, luaChonGhep }) {
  if (!daLo) {
    return (
      <>
        <div className="qz-tip">Học viên ghép từng mục bên trái với một mục ở vế phải.</div>
        {dapAn.map((d) => (
          <div key={d.id} className="preview-quiz-option">{d.noiDung}</div>
        ))}
        {luaChonGhep?.length > 0 && (
          <div className="qz-tip">Vế phải: {luaChonGhep.join('  ·  ')}</div>
        )}
      </>
    );
  }
  return dapAn.map((d) => (
    <div key={d.id} className="preview-quiz-option correct">
      {d.noiDung} <i className="ti ti-arrow-right" aria-hidden="true"></i> {d.noiDungGhep}
    </div>
  ));
}
