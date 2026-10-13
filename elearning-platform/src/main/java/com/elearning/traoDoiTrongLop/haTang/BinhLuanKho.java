package com.elearning.traoDoiTrongLop.haTang;

import com.elearning.traoDoiTrongLop.mienDomain.BinhLuan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BinhLuanKho extends JpaRepository<BinhLuan, Long> {

    /** Tất cả bình luận gốc (maCha = NULL) của một bài đăng, sắp xếp cũ nhất trước. */
    List<BinhLuan> findByMaBaiDangAndMaChaIsNullOrderByNgayTaoAsc(Long maBaiDang);

    /** Các reply của một bình luận gốc. */
    List<BinhLuan> findByMaChaOrderByNgayTaoAsc(Long maCha);

    long countByMaBaiDang(Long maBaiDang);

    /**
     * FIX: đếm số bình luận cho NHIỀU bài đăng cùng lúc bằng 1 query duy
     * nhất (GROUP BY) — dùng khi hiển thị danh sách bài đăng (BaiDangDichVuImpl.
     * layDanhSach) để tránh N+1: trước đây mỗi bài đăng trong trang kết quả
     * gọi riêng countByMaBaiDang(), tức N query đếm cho N bài đăng dù đã
     * batch-load thông tin người dùng bằng đúng kỹ thuật này rồi. Bài đăng
     * chưa có bình luận nào sẽ KHÔNG xuất hiện trong kết quả trả về — nơi
     * gọi cần tự suy ra 0 cho các mã bài đăng vắng mặt.
     */
    @Query("SELECT b.maBaiDang, COUNT(b) FROM BinhLuan b WHERE b.maBaiDang IN :dsMaBaiDang GROUP BY b.maBaiDang")
    List<Object[]> demTheoDanhSachBaiDang(@Param("dsMaBaiDang") List<Long> dsMaBaiDang);

    /** Tất cả bình luận thuộc bài đăng (dùng khi xoá bài đăng theo cascade). */
    List<BinhLuan> findByMaBaiDang(Long maBaiDang);

    /** Bài nộp (bình luận gốc) hiện có của một học viên cho một bài tập — dùng để phát hiện nộp lại. */
    Optional<BinhLuan> findByMaBaiDangAndMaNguoiDungAndMaChaIsNull(Long maBaiDang, Long maNguoiDung);
}