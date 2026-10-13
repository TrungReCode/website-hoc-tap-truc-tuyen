import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import TrangDangKy from '../pages/TrangDangKy';
import TrangDangNhap from '../pages/TrangDangNhap';
import TrangKichHoat from '../pages/TrangKichHoat';
import TrangQuenMatKhau from '../pages/TrangQuenMatKhau';
import TrangDatLaiMatKhau from '../pages/TrangDatLaiMatKhau';
import TrangChu from '../pages/TrangChu';
import ChoKhoaHoc from '../pages/ChoKhoaHoc';
import DanhSachLopHoc from '../pages/DanhSachLopHoc';
import ChiTietKhoaHoc from '../pages/ChiTietKhoaHoc';
import KhoaHocDaMua from '../pages/khoaHoc/KhoaHocDaMua';
import HocKhoaHoc from '../pages/khoaHoc/HocKhoaHoc';
import LamBaiKiemTraTuHoc from '../pages/khoaHoc/LamBaiKiemTraTuHoc';
import KetQuaKiemTraTuHoc from '../pages/khoaHoc/KetQuaKiemTraTuHoc';
import ChungChiCuaToi from '../pages/ChungChiCuaToi';
import XacThucGiayXacNhan from '../pages/XacThucGiayXacNhan';
import ChiTietKhoaHocGiangVien from '../pages/giangVien/ChiTietKhoaHocGiangVien';
import BangDieuKhienGiangVien from '../pages/giangVien/BangDieuKhienGiangVien';
import DoanhThuGiangVien from '../pages/giangVien/DoanhThuGiangVien';
import DanhSachKhoaHocGiangVien from '../pages/giangVien/DanhSachKhoaHocGiangVien';
import TaoSuaKhoaHoc from '../pages/giangVien/TaoSuaKhoaHoc';
import DanhSachChoDuyet from '../pages/admin/DanhSachChoDuyet';
import KiemDuyetKhoaHoc from '../pages/admin/KiemDuyetKhoaHoc';
import KiemDuyetDanhGia from '../pages/admin/KiemDuyetDanhGia';
import KiemDuyetDanhGiaKhoaHoc from '../pages/admin/KiemDuyetDanhGiaKhoaHoc';
import ThamGiaLopHoc from '../pages/lopHoc/ThamGiaLopHoc';
import GiaoDienLopHoc from '../pages/lopHoc/GiaoDienLopHoc';
import XemBaiGiang from '../pages/lopHoc/XemBaiGiang';
import LamBaiKiemTra from '../pages/lopHoc/LamBaiKiemTra';
import KetQuaKiemTra from '../pages/lopHoc/KetQuaKiemTra';
import DanhSachLopGiangVien from '../pages/lopHoc/giangVien/DanhSachLopGiangVien';
import TaoLopHoc from '../pages/lopHoc/giangVien/TaoLopHoc';
import GiaoTrinhLaiGiangVien from '../pages/lopHoc/giangVien/GiaoTrinhLaiGiangVien';
import MoiHocVien from '../pages/lopHoc/giangVien/MoiHocVien';
import QuanLyLopHoc from '../pages/lopHoc/giangVien/QuanLyLopHoc';
import KetQuaThanhToan from '../pages/KetQuaThanhToan';
import LichSuGiaoDich from '../pages/LichSuGiaoDich';
import HoSoCaNhan from '../pages/HoSoCaNhan';
import XacThucGiangVien from '../pages/XacThucGiangVien';
import KiemDuyetXacThucGiangVien from '../pages/admin/KiemDuyetXacThucGiangVien';
import LopHocDaMua from '../pages/lopHoc/LopHocDaMua';
// ── Admin mới ──
import BangDieuKhienCaoCap from '../pages/admin/BangDieuKhienCaoCap';
import BangDieuKhienNoiDung from '../pages/admin/BangDieuKhienNoiDung';
import BangDieuKhienTaiChinh from '../pages/admin/BangDieuKhienTaiChinh';
import TaoTaiKhoanAdmin from '../pages/admin/TaoTaiKhoanAdmin';
import CauHinhHeThong from '../pages/admin/CauHinhHeThong';
import NhatKyHeThong from '../pages/admin/NhatKyHeThong';
import QuanLyNguoiDung from '../pages/admin/QuanLyNguoiDung';
import QuanLyDanhMuc from '../pages/admin/QuanLyDanhMuc';
import QuanLyLopHocAdmin from '../pages/admin/QuanLyLopHocAdmin';
// ── Trao đổi + Thông báo ──
import DienDanLopHoc from '../pages/lopHoc/DienDanLopHoc';
import TrangThongBao from '../pages/TrangThongBao';
import TuyenDuongRieng from './TuyenDuongRieng';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<TrangChu />} />
        <Route path="/dang-ky" element={<TrangDangKy />} />
        <Route path="/dang-nhap" element={<TrangDangNhap />} />
        <Route path="/kich-hoat" element={<TrangKichHoat />} />
        <Route path="/quen-mat-khau" element={<TrangQuenMatKhau />} />
        <Route path="/dat-lai-mat-khau" element={<TrangDatLaiMatKhau />} />
        <Route path="/trang-chu" element={<TrangChu />} />

        {/* ── Chợ Khóa Học ── công khai */}
        <Route path="/cho-khoa-hoc" element={<ChoKhoaHoc />} />
        <Route path="/khoa-hoc/:maKhoaHoc" element={<ChiTietKhoaHoc />} />

        {/* ── Danh sách lớp học đang hoạt động ── công khai */}
        <Route path="/danh-sach-lop-hoc" element={<DanhSachLopHoc />} />
        <Route path="/lop-hoc-da-mua" element={<TuyenDuongRieng><LopHocDaMua /></TuyenDuongRieng>}/>

        {/* ── Học viên — Khóa học đã mua (choKhoaHoc) ── */}
        <Route path="/khoa-hoc-cua-toi/da-mua" element={<TuyenDuongRieng><KhoaHocDaMua /></TuyenDuongRieng>} />
        <Route path="/khoa-hoc-cua-toi/:maKhoaHoc/hoc" element={<TuyenDuongRieng><HocKhoaHoc /></TuyenDuongRieng>} />
        <Route path="/khoa-hoc-cua-toi/:maKhoaHoc/quiz/:maBaiHoc" element={<TuyenDuongRieng><LamBaiKiemTraTuHoc /></TuyenDuongRieng>} />
        <Route path="/khoa-hoc-cua-toi/:maKhoaHoc/quiz/:maBaiHoc/ket-qua" element={<TuyenDuongRieng><KetQuaKiemTraTuHoc /></TuyenDuongRieng>} />
        <Route path="/chung-chi-cua-toi" element={<TuyenDuongRieng><ChungChiCuaToi /></TuyenDuongRieng>} />

        {/* Công khai — không bọc TuyenDuongRieng, khớp whitelist /api/chung-chi/xac-thuc/**
            trong CauHinhBaoMat.java. Đúng URL đã in trong mã QR trên giấy PDF. */}
        <Route path="/xac-thuc-giay/:maXacThuc" element={<XacThucGiayXacNhan />} />

        {/* ── Giảng viên (choKhoaHoc) ── */}
        <Route path="/giang-vien" element={<TuyenDuongRieng><BangDieuKhienGiangVien /></TuyenDuongRieng>} />
        <Route path="/giang-vien/doanh-thu" element={<TuyenDuongRieng><DoanhThuGiangVien /></TuyenDuongRieng>} />
        <Route path="/giang-vien/khoa-hoc" element={<TuyenDuongRieng><DanhSachKhoaHocGiangVien /></TuyenDuongRieng>} />
        <Route path="/giang-vien/khoa-hoc/moi" element={<TuyenDuongRieng><TaoSuaKhoaHoc /></TuyenDuongRieng>} />
        {/* ĐÃ CẬP NHẬT ROUTE SỬA Ở ĐÂY */}
        <Route path="/giang-vien/khoa-hoc/:maKhoaHoc" element={<TuyenDuongRieng><ChiTietKhoaHocGiangVien /></TuyenDuongRieng>} />
        <Route path="/giang-vien/khoa-hoc/sua/:maKhoaHoc" element={<TuyenDuongRieng><TaoSuaKhoaHoc /></TuyenDuongRieng>} />

        {/* ── Admin — QTV_CAO_CAP ── */}
        <Route path="/admin" element={<TuyenDuongRieng chiCaoCap><BangDieuKhienCaoCap /></TuyenDuongRieng>} />
        <Route path="/admin/tai-khoan-admin" element={<TuyenDuongRieng chiCaoCap><TaoTaiKhoanAdmin /></TuyenDuongRieng>} />
        <Route path="/admin/cau-hinh-he-thong" element={<TuyenDuongRieng chiCaoCap><CauHinhHeThong /></TuyenDuongRieng>} />
        <Route path="/admin/nhat-ky" element={<TuyenDuongRieng chiCaoCap><NhatKyHeThong /></TuyenDuongRieng>} />

        {/* ── Admin — QTV_NOI_DUNG (+ QTV_CAO_CAP) ── */}
        <Route path="/admin/noi-dung" element={<TuyenDuongRieng chiNoiDung><BangDieuKhienNoiDung /></TuyenDuongRieng>} />
        <Route path="/admin/kiem-duyet" element={<TuyenDuongRieng chiNoiDung><DanhSachChoDuyet /></TuyenDuongRieng>} />
        <Route path="/admin/kiem-duyet/:maKhoaHoc" element={<TuyenDuongRieng chiNoiDung><KiemDuyetKhoaHoc /></TuyenDuongRieng>} />
        <Route path="/admin/kiem-duyet-danh-gia" element={<TuyenDuongRieng chiNoiDung><KiemDuyetDanhGia /></TuyenDuongRieng>} />
        <Route path="/admin/kiem-duyet-danh-gia/:maKhoaHoc" element={<TuyenDuongRieng chiNoiDung><KiemDuyetDanhGiaKhoaHoc /></TuyenDuongRieng>} />
        <Route path="/admin/nguoi-dung" element={<TuyenDuongRieng chiNoiDung><QuanLyNguoiDung /></TuyenDuongRieng>} />
        <Route path="/admin/danh-muc" element={<TuyenDuongRieng chiNoiDung><QuanLyDanhMuc /></TuyenDuongRieng>} />
        <Route path="/admin/lop-hoc" element={<TuyenDuongRieng chiNoiDung><QuanLyLopHocAdmin /></TuyenDuongRieng>} />

        {/* ── Admin — QTV_TAI_CHINH (+ QTV_CAO_CAP) ── */}
        <Route path="/admin/tai-chinh" element={<TuyenDuongRieng chiTaiChinh><BangDieuKhienTaiChinh /></TuyenDuongRieng>} />

        {/* ── Lớp học (lopHocLMS) ── Học viên */}
        {/* Đã gộp trang Tham gia lớp học + Thanh toán phí lớp học vào 1 component
            (ThamGiaLopHoc) dùng tab để chuyển chế độ, chia sẻ chung state mã lớp
            nên bấm "Thanh toán phí lớp học" sẽ đổ sẵn mã lớp, không cần nhập lại. */}
        <Route path="/lop-hoc/tham-gia" element={<TuyenDuongRieng><ThamGiaLopHoc /></TuyenDuongRieng>} />
        <Route path="/lop-hoc/thanh-toan" element={<TuyenDuongRieng><ThamGiaLopHoc /></TuyenDuongRieng>} />
        <Route path="/lop-hoc/:maLopHoc" element={<TuyenDuongRieng><GiaoDienLopHoc /></TuyenDuongRieng>} />
        <Route path="/lop-hoc/:maLopHoc/xem/:loaiNguon/:maThamChieu" element={<TuyenDuongRieng><XemBaiGiang /></TuyenDuongRieng>} />
        <Route path="/lop-hoc/:maLopHoc/quiz/:loaiNguon/:maThamChieu" element={<TuyenDuongRieng><LamBaiKiemTra /></TuyenDuongRieng>} />
        <Route path="/lop-hoc/:maLopHoc/quiz/:loaiNguon/:maThamChieu/ket-qua" element={<TuyenDuongRieng><KetQuaKiemTra /></TuyenDuongRieng>} />
        <Route path="/lop-hoc/:maLopHoc/dien-dan" element={<TuyenDuongRieng><DienDanLopHoc /></TuyenDuongRieng>} />

        {/* ── Lớp học (lopHocLMS) ── Giảng viên */}
        <Route path="/lop-hoc/giang-vien" element={<TuyenDuongRieng><DanhSachLopGiangVien /></TuyenDuongRieng>} />
        <Route path="/lop-hoc/tao-lop" element={<TuyenDuongRieng><TaoLopHoc /></TuyenDuongRieng>} />
        <Route path="/lop-hoc/:maLopHoc/quan-ly" element={<TuyenDuongRieng><QuanLyLopHoc /></TuyenDuongRieng>} />
        <Route path="/lop-hoc/:maLopHoc/giao-trinh-lai" element={<TuyenDuongRieng><GiaoTrinhLaiGiangVien /></TuyenDuongRieng>} />
        <Route path="/lop-hoc/:maLopHoc/moi-hoc-vien" element={<TuyenDuongRieng><MoiHocVien /></TuyenDuongRieng>} />

        {/* ── Thông báo ── */}
        <Route path="/thong-bao" element={<TuyenDuongRieng><TrangThongBao /></TuyenDuongRieng>} />

        <Route path="/thanh-toan/ket-qua" element={<KetQuaThanhToan />} />
        <Route path="/thanh-toan/lich-su" element={<TuyenDuongRieng><LichSuGiaoDich /></TuyenDuongRieng>} />
        <Route path="/ho-so" element={<TuyenDuongRieng><HoSoCaNhan /></TuyenDuongRieng>} />
        <Route path="/xac-thuc-giang-vien" element={<TuyenDuongRieng><XacThucGiangVien /></TuyenDuongRieng>} />
        <Route path="/admin/xac-thuc-giang-vien" element={<TuyenDuongRieng chiNoiDung><KiemDuyetXacThucGiangVien /></TuyenDuongRieng>} />

        <Route path="*" element={<Navigate to="/trang-chu" replace />} />
      </Routes>
    </BrowserRouter>
  );
}