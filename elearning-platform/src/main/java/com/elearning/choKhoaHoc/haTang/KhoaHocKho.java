package com.elearning.choKhoaHoc.haTang;

import com.elearning.choKhoaHoc.mienDomain.KhoaHoc;
import com.elearning.choKhoaHoc.mienDomain.enumDomain.TrangThaiKhoaHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * PUBLIC: lý do kỹ thuật — Service (package ungDung) khác package với
 * Repository (package haTang) nên Spring Data JPA cần Repository public
 * để proxy. Ranh giới module vẫn giữ vì Repository này không được khai
 * báo trong bất kỳ import nào ở ngoài package choKhoaHoc theo quy ước.
 */
public interface KhoaHocKho extends JpaRepository<KhoaHoc, Long> {

  List<KhoaHoc> findByMaTacGia(Long maTacGia);

  Page<KhoaHoc> findByTrangThai(TrangThaiKhoaHoc trangThai, Pageable pageable);

  /**
   * LƯU Ý: trangThaiCongKhai PHẢI truyền qua bind parameter, KHÔNG được viết
   * literal enum trực tiếp trong chuỗi JPQL (kiểu "k.trangThai =
   * com.elearning...TrangThaiKhoaHoc.CONG_KHAI"). TrangThaiKhoaHoc dùng
   * @JdbcTypeCode(SqlTypes.NAMED_ENUM) ánh xạ tới type PostgreSQL riêng
   * (trang_thai_kh_enum) — khi enum là literal trong JPQL thay vì bind
   * parameter, Hibernate không tra được type Postgres thật, tự sinh cast
   * theo TÊN CLASS JAVA (::TrangThaiKhoaHoc) gây lỗi "type does not exist"
   * lúc chạy.
   */
  @Query("SELECT k FROM KhoaHoc k " +
      "WHERE k.trangThai = :trangThaiCongKhai " +
      // Khớp danh mục theo 1 trong 2 trường hợp:
      //  1) k.danhMuc.maDanhMuc = :maDanhMuc         -> khóa học gắn TRỰC TIẾP với
      //     danh mục được chọn (áp dụng khi chọn 1 danh mục con cụ thể, hoặc 1
      //     danh mục cha không có con nào).
      //  2) k.danhMuc.mucCha.maDanhMuc = :maDanhMuc  -> khóa học gắn với 1 danh
      //     mục CON thuộc về danh mục cha đang chọn. Thiếu nhánh này thì lọc
      //     theo cha gần như luôn ra rỗng, vì khóa học thường được gắn thẳng ở
      //     danh mục con (lá), hiếm khi gắn trực tiếp ở danh mục cha.
      "AND (:maDanhMuc IS NULL OR k.danhMuc.maDanhMuc = :maDanhMuc OR k.danhMuc.mucCha.maDanhMuc = :maDanhMuc) " +
      "AND (CAST(:tuKhoa AS String) IS NULL OR LOWER(k.tieuDe) LIKE LOWER(CONCAT('%', CAST(:tuKhoa AS String), '%'))) " +
      "AND (CAST(:caNhanLoc AS String) IS NULL " +
      "     OR (CAST(:caNhanLoc AS String) = 'MIEN_PHI' AND k.gia = 0) " +
      "     OR (CAST(:caNhanLoc AS String) = 'CO_PHI' AND k.gia > 0)) " +
      "AND (CAST(:dayHocLoc AS String) IS NULL " +
      "     OR (CAST(:dayHocLoc AS String) = 'MIEN_PHI' AND k.phiDayHoc = 0) " +
      "     OR (CAST(:dayHocLoc AS String) = 'CO_PHI' AND k.phiDayHoc > 0))")
  Page<KhoaHoc> timKiemCongKhai(@Param("maDanhMuc") Long maDanhMuc,
      @Param("tuKhoa") String tuKhoa,
      @Param("caNhanLoc") String caNhanLoc,
      @Param("dayHocLoc") String dayHocLoc,
      @Param("trangThaiCongKhai") TrangThaiKhoaHoc trangThaiCongKhai,
      Pageable pageable);

  /**
   * NÂNG CẤP (Full-Text Search): dùng khi tuKhoa CÓ giá trị thật — xếp hạng
   * theo ts_rank() thay vì theo ngày tạo, tìm được cả trong mieu_ta chứ
   * không chỉ tieu_de. Đây là NATIVE QUERY (SQL thật, không phải JPQL) vì
   * JPQL không hỗ trợ toán tử @@ / hàm ts_rank của Postgres. Giữ nguyên
   * timKiemCongKhai() ở trên cho trường hợp duyệt danh sách KHÔNG tìm kiếm
   * (tuKhoa rỗng) — ở đó không cần xếp hạng, JPQL hiện tại đã đủ dùng và an
   * toàn hơn (không phải lặp lại toàn bộ logic lọc danh mục/giá bằng SQL thô).
   *
   * websearch_to_tsquery (thay vì plainto_tsquery/to_tsquery) tự xử lý cụm
   * nhiều từ theo kiểu người dùng gõ tìm kiếm thông thường (không cần đúng
   * cú pháp toán tử &/|), và không ném lỗi khi tuKhoa chứa ký tự đặc biệt.
   *
   * countQuery bắt buộc phải khai báo riêng vì Spring Data không tự suy ra
   * được câu COUNT từ native query có ORDER BY ts_rank (ts_rank không hợp lệ
   * trong ngữ cảnh COUNT).
   */
  @Query(value = "SELECT k.* FROM khoa_hoc k " +
      "LEFT JOIN danh_muc dm ON dm.ma_danh_muc = k.ma_danh_muc " +
      "WHERE k.trang_thai = CAST(:trangThaiCongKhai AS trang_thai_kh_enum) " +
      "AND (:maDanhMuc IS NULL OR k.ma_danh_muc = :maDanhMuc OR dm.muc_cha = :maDanhMuc) " +
      "AND (:caNhanLoc IS NULL " +
      "     OR (:caNhanLoc = 'MIEN_PHI' AND k.gia = 0) " +
      "     OR (:caNhanLoc = 'CO_PHI' AND k.gia > 0)) " +
      "AND (:dayHocLoc IS NULL " +
      "     OR (:dayHocLoc = 'MIEN_PHI' AND k.phi_day_hoc = 0) " +
      "     OR (:dayHocLoc = 'CO_PHI' AND k.phi_day_hoc > 0)) " +
      "AND k.tsv_tim_kiem @@ websearch_to_tsquery('simple', :tuKhoa) " +
      "ORDER BY ts_rank(k.tsv_tim_kiem, websearch_to_tsquery('simple', :tuKhoa)) DESC",
      countQuery = "SELECT count(*) FROM khoa_hoc k " +
          "LEFT JOIN danh_muc dm ON dm.ma_danh_muc = k.ma_danh_muc " +
          "WHERE k.trang_thai = CAST(:trangThaiCongKhai AS trang_thai_kh_enum) " +
          "AND (:maDanhMuc IS NULL OR k.ma_danh_muc = :maDanhMuc OR dm.muc_cha = :maDanhMuc) " +
          "AND (:caNhanLoc IS NULL " +
          "     OR (:caNhanLoc = 'MIEN_PHI' AND k.gia = 0) " +
          "     OR (:caNhanLoc = 'CO_PHI' AND k.gia > 0)) " +
          "AND (:dayHocLoc IS NULL " +
          "     OR (:dayHocLoc = 'MIEN_PHI' AND k.phi_day_hoc = 0) " +
          "     OR (:dayHocLoc = 'CO_PHI' AND k.phi_day_hoc > 0)) " +
          "AND k.tsv_tim_kiem @@ websearch_to_tsquery('simple', :tuKhoa)",
      nativeQuery = true)
  Page<KhoaHoc> timKiemCongKhaiTheoDoLienQuan(@Param("maDanhMuc") Long maDanhMuc,
      @Param("tuKhoa") String tuKhoa,
      @Param("caNhanLoc") String caNhanLoc,
      @Param("dayHocLoc") String dayHocLoc,
      @Param("trangThaiCongKhai") String trangThaiCongKhai,
      Pageable pageable);

  /**
   * LƯU Ý: so sánh bằng CAST(k.trangThai AS string) = :trangThai (String, không
   * phải enum) để tránh lỗi Postgres "could not determine data type of parameter"
   * (SQLState 42P18) — driver không tự suy ra kiểu cho tham số enum native khi nó
   * chỉ xuất hiện trong nhánh "IS NULL OR ...". Cùng cách xử lý với :tuKhoa bên dưới.
   */
  @Query("SELECT k FROM KhoaHoc k " +
      "WHERE (CAST(:trangThai AS string) IS NULL OR CAST(k.trangThai AS string) = :trangThai) " +
      "AND (CAST(:tuKhoa AS String) IS NULL OR LOWER(k.tieuDe) LIKE LOWER(CONCAT('%', CAST(:tuKhoa AS String), '%')))")
  Page<KhoaHoc> timKiemChoAdmin(@Param("trangThai") String trangThai,
      @Param("tuKhoa") String tuKhoa,
      Pageable pageable);

  Optional<KhoaHoc> findByMaKhoaHocAndTrangThai(Long maKhoaHoc, TrangThaiKhoaHoc trangThai);

  /** Dùng bởi lopHocLMS (qua TimKiemKhoaHocDichVu) để tìm lớp học theo tên khóa học.
   *  trangThaiCongKhai truyền qua bind parameter — lý do xem ghi chú ở timKiemCongKhai(). */
  @Query("SELECT k.maKhoaHoc FROM KhoaHoc k " +
      "WHERE k.trangThai = :trangThaiCongKhai " +
      "AND LOWER(k.tieuDe) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))")
  List<Long> timMaKhoaHocCongKhaiKhopTuKhoa(@Param("tuKhoa") String tuKhoa,
      @Param("trangThaiCongKhai") TrangThaiKhoaHoc trangThaiCongKhai);

  long countByMaTacGia(Long maTacGia);

  long countByDanhMuc_MaDanhMuc(Long maDanhMuc);
}