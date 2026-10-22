// Quy tắc dùng chung phía giao diện cho mọi dạng câu hỏi của quiz — khớp với
// QuyTacCauHoi.java ở backend (backend vẫn là nơi quyết định cuối cùng; đây chỉ
// để báo lỗi sớm và dựng/đọc dữ liệu đúng hình dạng).

export const LOAI_CAU_HOI = [
  { ma: 'MOT_DAP_AN', nhan: '1 đáp án đúng (chọn 1)' },
  { ma: 'NHIEU_DAP_AN', nhan: 'Nhiều đáp án đúng (chọn nhiều)' },
  { ma: 'DUNG_SAI', nhan: 'Đúng / Sai' },
  { ma: 'TRA_LOI_NGAN', nhan: 'Trả lời ngắn (điền từ)' },
  { ma: 'SAP_XEP', nhan: 'Sắp xếp thứ tự' },
  { ma: 'GHEP_CAP', nhan: 'Ghép cặp' },
];

export function nhanLoaiCauHoi(ma) {
  return LOAI_CAU_HOI.find((l) => l.ma === (ma || 'MOT_DAP_AN'))?.nhan || ma;
}

// Dạng mà mọi phần tử đều là "đúng" (không có khái niệm chọn đúng/sai từng dòng).
const LOAI_MOI_DONG_DEU_DUNG = ['TRA_LOI_NGAN', 'SAP_XEP', 'GHEP_CAP'];

// ───────────────────────── Bài làm của học viên ─────────────────────────
// Trạng thái trả lời của 1 câu: ids (chọn / thứ tự đã sắp), vanBan (trả lời ngắn),
// capGhep { [maDapAn vế trái]: nội dung vế phải đã chọn }.

export function traLoiTrong() {
  return { ids: [], vanBan: '', capGhep: {} };
}

export function daTraLoi(cauHoi, traLoi) {
  if (!traLoi) return false;
  switch (cauHoi.loaiCauHoi) {
    case 'TRA_LOI_NGAN':
      return (traLoi.vanBan || '').trim().length > 0;
    case 'GHEP_CAP':
      return cauHoi.dapAn.length > 0 && cauHoi.dapAn.every((d) => !!traLoi.capGhep?.[d.id]);
    case 'SAP_XEP':
      return (traLoi.ids || []).length === cauHoi.dapAn.length && cauHoi.dapAn.length > 0;
    default:
      return (traLoi.ids || []).length > 0;
  }
}

/** Dựng phần tử của YeuCauCauTraLoi (backend) cho 1 câu từ trạng thái trả lời. */
export function taoYeuCauTraLoi(cauHoi, traLoi) {
  const t = traLoi || traLoiTrong();
  const yeuCau = { maCauHoi: cauHoi.maCauHoi, dsMaDapAnDaChon: [] };
  switch (cauHoi.loaiCauHoi) {
    case 'TRA_LOI_NGAN':
      yeuCau.traLoiVanBan = t.vanBan || '';
      break;
    case 'GHEP_CAP':
      yeuCau.capGhep = cauHoi.dapAn
        .filter((d) => t.capGhep?.[d.id])
        .map((d) => ({ maDapAn: d.id, noiDungGhep: t.capGhep[d.id] }));
      break;
    default:
      yeuCau.dsMaDapAnDaChon = t.ids || [];
  }
  return yeuCau;
}

// ───────────────────────── Soạn câu hỏi (giảng viên) ─────────────────────────

/** Danh sách phương án khởi tạo khi chọn 1 dạng câu hỏi. */
export function dapAnMacDinh(loai) {
  switch (loai) {
    case 'DUNG_SAI':
      return [
        { noiDung: 'Đúng', dapAnDung: true },
        { noiDung: 'Sai', dapAnDung: false },
      ];
    case 'TRA_LOI_NGAN':
      return [{ noiDung: '', dapAnDung: true }];
    case 'SAP_XEP':
      return [
        { noiDung: '', dapAnDung: true },
        { noiDung: '', dapAnDung: true },
        { noiDung: '', dapAnDung: true },
      ];
    case 'GHEP_CAP':
      return [
        { noiDung: '', noiDungGhep: '', dapAnDung: true },
        { noiDung: '', noiDungGhep: '', dapAnDung: true },
        { noiDung: '', noiDungGhep: '', dapAnDung: true },
      ];
    default:
      return [
        { noiDung: '', dapAnDung: false },
        { noiDung: '', dapAnDung: false },
      ];
  }
}

export function cauHoiMoi(loai = 'MOT_DAP_AN') {
  return { noiDung: '', loaiCauHoi: loai, dapAn: dapAnMacDinh(loai) };
}

const chuanHoa = (s) => (s || '').normalize('NFC').trim().replace(/\s+/g, ' ').toLowerCase();

function khacNhau(giaTri) {
  const daGap = new Set();
  for (const g of giaTri) {
    const c = chuanHoa(g);
    if (!c || daGap.has(c)) return false;
    daGap.add(c);
  }
  return true;
}

/** Trả về thông báo lỗi (chuỗi) nếu câu hỏi chưa hợp lệ, hoặc chuỗi rỗng nếu ổn. */
export function kiemTraSoanThao({ noiDung, loaiCauHoi, dapAn }) {
  if (!noiDung.trim()) return 'Vui lòng nhập nội dung câu hỏi';
  const soDung = dapAn.filter((d) => d.dapAnDung).length;
  switch (loaiCauHoi) {
    case 'MOT_DAP_AN':
      if (dapAn.length < 2) return 'Cần ít nhất 2 phương án';
      if (dapAn.some((d) => !d.noiDung.trim())) return 'Vui lòng nhập đầy đủ nội dung các phương án';
      if (soDung !== 1) return 'Hãy đánh dấu đúng 1 đáp án đúng';
      return '';
    case 'DUNG_SAI':
      if (dapAn.length !== 2) return 'Câu hỏi Đúng/Sai phải có đúng 2 phương án';
      if (dapAn.some((d) => !d.noiDung.trim())) return 'Vui lòng nhập đầy đủ nội dung các phương án';
      if (soDung !== 1) return 'Hãy đánh dấu đúng 1 đáp án đúng';
      return '';
    case 'NHIEU_DAP_AN':
      if (dapAn.length < 2) return 'Cần ít nhất 2 phương án';
      if (dapAn.some((d) => !d.noiDung.trim())) return 'Vui lòng nhập đầy đủ nội dung các phương án';
      if (soDung < 1) return 'Cần chọn ít nhất 1 đáp án đúng';
      return '';
    case 'TRA_LOI_NGAN':
      if (dapAn.length < 1 || dapAn.some((d) => !d.noiDung.trim())) {
        return 'Nhập ít nhất 1 đáp án được chấp nhận và không để dòng nào trống';
      }
      return '';
    case 'SAP_XEP':
      if (dapAn.length < 2) return 'Câu hỏi sắp xếp cần ít nhất 2 mục';
      if (!khacNhau(dapAn.map((d) => d.noiDung))) return 'Các mục cần sắp xếp không được để trống hoặc trùng nhau';
      return '';
    case 'GHEP_CAP':
      if (dapAn.length < 2) return 'Câu hỏi ghép cặp cần ít nhất 2 cặp';
      if (!khacNhau(dapAn.map((d) => d.noiDung))) return 'Các mục vế trái không được để trống hoặc trùng nhau';
      if (!khacNhau(dapAn.map((d) => d.noiDungGhep))) return 'Các mục vế phải không được để trống hoặc trùng nhau';
      return '';
    default:
      return 'Loại câu hỏi không hợp lệ';
  }
}

/** Dựng body gửi backend (YeuCauTaoCauHoi / YeuCauTaoCauHoiBoSung) từ dữ liệu form. */
export function taoPayloadCauHoi({ noiDung, loaiCauHoi, dapAn }, viTri) {
  const moiDongDeuDung = LOAI_MOI_DONG_DEU_DUNG.includes(loaiCauHoi);
  return {
    noiDung: noiDung.trim(),
    viTri,
    loaiCauHoi,
    // id đánh lại liên tục 1..n lúc gửi để luôn duy nhất dù giảng viên đã xóa/thêm dòng
    dapAn: dapAn.map((d, i) => ({
      id: i + 1,
      noiDung: d.noiDung.trim(),
      dapAnDung: moiDongDeuDung ? true : !!d.dapAnDung,
      ...(loaiCauHoi === 'GHEP_CAP' ? { noiDungGhep: (d.noiDungGhep || '').trim() } : {}),
    })),
  };
}

/** Chuẩn hóa giống backend (NFC, bỏ khoảng trắng thừa, không phân biệt hoa/thường) — dùng để tô đúng/sai khi xem lại. */
export const chuanHoaVanBan = chuanHoa;
