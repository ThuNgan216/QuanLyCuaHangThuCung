/* =========================================================
   DATABASE: QUAN LY CUA HANG / PET CARE
   SQL SERVER
   ========================================================= */

USE master;
GO

IF DB_ID('PetCareDB') IS NOT NULL
BEGIN
    ALTER DATABASE PetCareDB
    SET SINGLE_USER WITH ROLLBACK IMMEDIATE;

    DROP DATABASE PetCareDB;
END
GO

CREATE DATABASE PetCareDB;
GO

USE PetCareDB;
GO

/* =========================================================
   1. TAI KHOAN
   ========================================================= */

CREATE TABLE TaiKhoan
(
    MaTK            VARCHAR(20) NOT NULL,
    TenDangNhap     VARCHAR(50) NOT NULL,
    MatKhau         VARCHAR(255) NOT NULL,
    VaiTro          NVARCHAR(30) NOT NULL,
    TrangThai       BIT NOT NULL DEFAULT 1,

    CONSTRAINT PK_TaiKhoan
        PRIMARY KEY (MaTK),

    CONSTRAINT UQ_TaiKhoan_TenDangNhap
        UNIQUE (TenDangNhap),

    CONSTRAINT CK_TaiKhoan_VaiTro
        CHECK (VaiTro IN
        (
            N'ADMIN',
            N'QUANLY',
            N'NHANVIEN'
        ))
);
GO


/* =========================================================
   2. NHAN VIEN
   ========================================================= */

CREATE TABLE NhanVien
(
    MaNV            VARCHAR(20) NOT NULL,
    TaiKhoanMaTK    VARCHAR(20) NOT NULL,
    TenNV           NVARCHAR(100) NULL,
    SDT             VARCHAR(15) NULL,
    CCCD            VARCHAR(20) NULL,
    DiaChi          NVARCHAR(255) NULL,
    Email           VARCHAR(100) NULL,
    ChucVu          NVARCHAR(50) NULL,

    CONSTRAINT PK_NhanVien
        PRIMARY KEY (MaNV),

    CONSTRAINT FK_NhanVien_TaiKhoan
        FOREIGN KEY (TaiKhoanMaTK)
        REFERENCES TaiKhoan(MaTK),

    CONSTRAINT UQ_NhanVien_TaiKhoan
        UNIQUE (TaiKhoanMaTK),

    CONSTRAINT UQ_NhanVien_CCCD
        UNIQUE (CCCD),

    CONSTRAINT UQ_NhanVien_Email
        UNIQUE (Email)
);
GO


/* =========================================================
   3. BANG LUONG
   ========================================================= */

CREATE TABLE BangLuong
(
    MaBangLuong     VARCHAR(20) NOT NULL,
    NhanVienMaNV    VARCHAR(20) NOT NULL,
    Thang           TINYINT NOT NULL,
    Nam             SMALLINT NOT NULL,
    LuongCoBan      DECIMAL(18,2) NOT NULL DEFAULT 0,
    PhuCap          DECIMAL(18,2) NOT NULL DEFAULT 0,
    Thuong          DECIMAL(18,2) NOT NULL DEFAULT 0,
    KhauTru         DECIMAL(18,2) NOT NULL DEFAULT 0,

    CONSTRAINT PK_BangLuong
        PRIMARY KEY (MaBangLuong),

    CONSTRAINT FK_BangLuong_NhanVien
        FOREIGN KEY (NhanVienMaNV)
        REFERENCES NhanVien(MaNV),

    CONSTRAINT UQ_BangLuong_NhanVien_Thang_Nam
        UNIQUE (NhanVienMaNV, Thang, Nam),

    CONSTRAINT CK_BangLuong_Thang
        CHECK (Thang BETWEEN 1 AND 12),

    CONSTRAINT CK_BangLuong_Nam
        CHECK (Nam >= 2000),

    CONSTRAINT CK_BangLuong_Luong
        CHECK
        (
            LuongCoBan >= 0
            AND PhuCap >= 0
            AND Thuong >= 0
            AND KhauTru >= 0
        )
);
GO


/* =========================================================
   4. LICH LAM VIEC
   ========================================================= */

CREATE TABLE LichLamViec
(
    MaLichLamViec  VARCHAR(20) NOT NULL,
    NhanVienMaNV   VARCHAR(20) NOT NULL,
    ThoiGianBatDau DATETIME2 NOT NULL,
    ThoiGianKetThuc DATETIME2 NOT NULL,

    CONSTRAINT PK_LichLamViec
        PRIMARY KEY (MaLichLamViec),

    CONSTRAINT FK_LichLamViec_NhanVien
        FOREIGN KEY (NhanVienMaNV)
        REFERENCES NhanVien(MaNV),

    CONSTRAINT CK_LichLamViec_ThoiGian
        CHECK (ThoiGianKetThuc > ThoiGianBatDau)
);
GO


/* =========================================================
   5. KHACH HANG
   ========================================================= */

CREATE TABLE KhachHang
(
    MaKH            VARCHAR(20) NOT NULL,
    SDT              VARCHAR(15) NULL,
    DiaChi           NVARCHAR(255) NULL,
    HangThanhVien    NVARCHAR(30) NULL,
    DiemTichLuy      INT NOT NULL DEFAULT 0,

    CONSTRAINT PK_KhachHang
        PRIMARY KEY (MaKH),

    CONSTRAINT CK_KhachHang_Diem
        CHECK (DiemTichLuy >= 0)
);
GO


/* =========================================================
   6. THU CUNG
   ========================================================= */

CREATE TABLE ThuCung
(
    MaThuCung       VARCHAR(20) NOT NULL,
    KhachHangMaKH   VARCHAR(20) NOT NULL,
    TenThuCung      NVARCHAR(100) NULL,
    Loai            NVARCHAR(50) NULL,
    Giong           NVARCHAR(100) NULL,
    GioiTinh        NVARCHAR(20) NULL,
    CanNang         DECIMAL(10,2) NOT NULL,
    NgaySinh        DATE NULL,
    TinhTrangSK     NVARCHAR(255) NULL,
    TiemChung       NVARCHAR(255) NULL,

    CONSTRAINT PK_ThuCung
        PRIMARY KEY (MaThuCung),

    CONSTRAINT FK_ThuCung_KhachHang
        FOREIGN KEY (KhachHangMaKH)
        REFERENCES KhachHang(MaKH),

    CONSTRAINT CK_ThuCung_CanNang
        CHECK (CanNang >= 0)
);
GO


/* =========================================================
   7. LOAI SAN PHAM
   ========================================================= */

CREATE TABLE LoaiSanPham
(
    MaLoai         VARCHAR(20) NOT NULL,
    TenLoai        NVARCHAR(100) NULL,

    CONSTRAINT PK_LoaiSanPham
        PRIMARY KEY (MaLoai),

    CONSTRAINT UQ_LoaiSanPham_TenLoai
        UNIQUE (TenLoai)
);
GO


/* =========================================================
   8. SAN PHAM
   ========================================================= */

CREATE TABLE SanPham
(
    MaSP                VARCHAR(20) NOT NULL,
    LoaiSanPhamMaLoai   VARCHAR(20) NOT NULL,
    TenSP               NVARCHAR(150) NULL,
    GiaBan              DECIMAL(18,2) NOT NULL,
    SoLuongTon          INT NOT NULL DEFAULT 0,

    CONSTRAINT PK_SanPham
        PRIMARY KEY (MaSP),

    CONSTRAINT FK_SanPham_LoaiSanPham
        FOREIGN KEY (LoaiSanPhamMaLoai)
        REFERENCES LoaiSanPham(MaLoai),

    CONSTRAINT CK_SanPham_GiaBan
        CHECK (GiaBan >= 0),

    CONSTRAINT CK_SanPham_SoLuongTon
        CHECK (SoLuongTon >= 0)
);
GO


/* =========================================================
   9. MA VACH
   ========================================================= */

CREATE TABLE MaVach
(
    MaVach          VARCHAR(50) NOT NULL,
    SanPhamMaSP     VARCHAR(20) NOT NULL,

    CONSTRAINT PK_MaVach
        PRIMARY KEY (MaVach),

    CONSTRAINT FK_MaVach_SanPham
        FOREIGN KEY (SanPhamMaSP)
        REFERENCES SanPham(MaSP),

    CONSTRAINT UQ_MaVach_SanPham
        UNIQUE (SanPhamMaSP)
);
GO


/* =========================================================
   10. KHUYEN MAI
   ========================================================= */

CREATE TABLE KhuyenMai
(
    MaKhuyenMai     VARCHAR(20) NOT NULL,
    TenKhuyenMai    NVARCHAR(150) NULL,
    PhanTramGiam    DECIMAL(5,2) NOT NULL,
    DieuKienApDung  NVARCHAR(255) NULL,
    NgayBatDau      DATE NULL,
    NgayKetThuc     DATE NULL,

    CONSTRAINT PK_KhuyenMai
        PRIMARY KEY (MaKhuyenMai),

    CONSTRAINT CK_KhuyenMai_PhanTram
        CHECK (PhanTramGiam BETWEEN 0 AND 100),

    CONSTRAINT CK_KhuyenMai_Ngay
        CHECK
        (
            NgayKetThuc IS NULL
            OR NgayBatDau IS NULL
            OR NgayKetThuc >= NgayBatDau
        )
);
GO


/* =========================================================
   11. KHUYEN MAI - SAN PHAM
   ========================================================= */

CREATE TABLE KhuyenMaiSanPham
(
    KhuyenMaiMaKhuyenMai   VARCHAR(20) NOT NULL,
    SanPhamMaSP            VARCHAR(20) NOT NULL,
    MucGiam                DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_KhuyenMaiSanPham
        PRIMARY KEY
        (
            KhuyenMaiMaKhuyenMai,
            SanPhamMaSP
        ),

    CONSTRAINT FK_KhuyenMaiSanPham_KhuyenMai
        FOREIGN KEY (KhuyenMaiMaKhuyenMai)
        REFERENCES KhuyenMai(MaKhuyenMai),

    CONSTRAINT FK_KhuyenMaiSanPham_SanPham
        FOREIGN KEY (SanPhamMaSP)
        REFERENCES SanPham(MaSP),

    CONSTRAINT CK_KhuyenMaiSanPham_MucGiam
        CHECK (MucGiam >= 0)
);
GO


/* =========================================================
   12. NHA CUNG CAP
   ========================================================= */

CREATE TABLE NhaCungCap
(
    MaNCC          VARCHAR(20) NOT NULL,
    TenNCC         NVARCHAR(150) NULL,
    SoDienThoai    VARCHAR(15) NULL,
    DiaChi         NVARCHAR(255) NULL,
    Email          VARCHAR(100) NULL,

    CONSTRAINT PK_NhaCungCap
        PRIMARY KEY (MaNCC)
);
GO


/* =========================================================
   13. PHIEU NHAP
   ========================================================= */

CREATE TABLE PhieuNhap
(
    MaPN            VARCHAR(20) NOT NULL,
    NhanVienMaNV    VARCHAR(20) NOT NULL,
    NhaCungCapMaNCC VARCHAR(20) NOT NULL,
    NgayNhap        DATETIME2 NULL,

    CONSTRAINT PK_PhieuNhap
        PRIMARY KEY (MaPN),

    CONSTRAINT FK_PhieuNhap_NhanVien
        FOREIGN KEY (NhanVienMaNV)
        REFERENCES NhanVien(MaNV),

    CONSTRAINT FK_PhieuNhap_NhaCungCap
        FOREIGN KEY (NhaCungCapMaNCC)
        REFERENCES NhaCungCap(MaNCC)
);
GO


/* =========================================================
   14. CHI TIET PHIEU NHAP
   ========================================================= */

CREATE TABLE ChiTietPhieuNhap
(
    PhieuNhapMaPN   VARCHAR(20) NOT NULL,
    SanPhamMaSP     VARCHAR(20) NOT NULL,
    SoLuongNhap     INT NOT NULL,
    DonGiaNhap      DECIMAL(18,2) NOT NULL,
    HanSuDung       DATE NULL,
    ThanhTien       DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_ChiTietPhieuNhap
        PRIMARY KEY
        (
            PhieuNhapMaPN,
            SanPhamMaSP
        ),

    CONSTRAINT FK_ChiTietPhieuNhap_PhieuNhap
        FOREIGN KEY (PhieuNhapMaPN)
        REFERENCES PhieuNhap(MaPN),

    CONSTRAINT FK_ChiTietPhieuNhap_SanPham
        FOREIGN KEY (SanPhamMaSP)
        REFERENCES SanPham(MaSP),

    CONSTRAINT CK_ChiTietPhieuNhap_SoLuong
        CHECK (SoLuongNhap > 0),

    CONSTRAINT CK_ChiTietPhieuNhap_DonGia
        CHECK (DonGiaNhap >= 0),

    CONSTRAINT CK_ChiTietPhieuNhap_ThanhTien
        CHECK (ThanhTien >= 0)
);
GO


/* =========================================================
   15. HOA DON
   ========================================================= */

CREATE TABLE HoaDon
(
    MaHoaDon        VARCHAR(20) NOT NULL,
    NgayLapHD       DATETIME2 NULL,
    TinhTienGiam    DECIMAL(18,2) NOT NULL DEFAULT 0,
    TongTien        DECIMAL(18,2) NOT NULL DEFAULT 0,
    ThanhTien       DECIMAL(18,2) NOT NULL DEFAULT 0,

    CONSTRAINT PK_HoaDon
        PRIMARY KEY (MaHoaDon),

    CONSTRAINT CK_HoaDon_Tien
        CHECK
        (
            TinhTienGiam >= 0
            AND TongTien >= 0
            AND ThanhTien >= 0
        )
);
GO


/* =========================================================
   16. DON HANG
   ========================================================= */

CREATE TABLE DonHang
(
    MaDonHang       VARCHAR(20) NOT NULL,
    KhachHangMaKH   VARCHAR(20) NOT NULL,
    HoaDonID        VARCHAR(20) NOT NULL,
    NhanVienMaNV    VARCHAR(20) NOT NULL,
    NgayTao         DATETIME2 NULL,
    TrangThai       NVARCHAR(50) NULL,

    CONSTRAINT PK_DonHang
        PRIMARY KEY (MaDonHang),

    CONSTRAINT FK_DonHang_KhachHang
        FOREIGN KEY (KhachHangMaKH)
        REFERENCES KhachHang(MaKH),

    CONSTRAINT FK_DonHang_HoaDon
        FOREIGN KEY (HoaDonID)
        REFERENCES HoaDon(MaHoaDon),

    CONSTRAINT FK_DonHang_NhanVien
        FOREIGN KEY (NhanVienMaNV)
        REFERENCES NhanVien(MaNV),

    CONSTRAINT UQ_DonHang_HoaDon
        UNIQUE (HoaDonID)
);
GO


/* =========================================================
   17. CHI TIET DON HANG
   ========================================================= */

CREATE TABLE ChiTietDonHang
(
    DonHangMaDonHang    VARCHAR(20) NOT NULL,
    SanPhamMaSP         VARCHAR(20) NOT NULL,
    SoLuong             INT NOT NULL,
    DonGia              DECIMAL(18,2) NOT NULL,
    ThanhTien           DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_ChiTietDonHang
        PRIMARY KEY
        (
            DonHangMaDonHang,
            SanPhamMaSP
        ),

    CONSTRAINT FK_ChiTietDonHang_DonHang
        FOREIGN KEY (DonHangMaDonHang)
        REFERENCES DonHang(MaDonHang),

    CONSTRAINT FK_ChiTietDonHang_SanPham
        FOREIGN KEY (SanPhamMaSP)
        REFERENCES SanPham(MaSP),

    CONSTRAINT CK_ChiTietDonHang_SoLuong
        CHECK (SoLuong > 0),

    CONSTRAINT CK_ChiTietDonHang_DonGia
        CHECK (DonGia >= 0),

    CONSTRAINT CK_ChiTietDonHang_ThanhTien
        CHECK (ThanhTien >= 0)
);
GO


/* =========================================================
   18. PHUONG THUC THANH TOAN
   ========================================================= */

CREATE TABLE PhuongThucThanhToan
(
    MaPTTT          VARCHAR(20) NOT NULL,
    TenPTTT         NVARCHAR(100) NULL,

    CONSTRAINT PK_PhuongThucThanhToan
        PRIMARY KEY (MaPTTT),

    CONSTRAINT UQ_PhuongThucThanhToan_Ten
        UNIQUE (TenPTTT)
);
GO


/* =========================================================
   19. THANH TOAN
   ========================================================= */

CREATE TABLE ThanhToan
(
    MaTT                        VARCHAR(20) NOT NULL,
    DonHangMaDonHang            VARCHAR(20) NOT NULL,
    KhuyenMaiMaKhuyenMai        VARCHAR(20) NULL,
    PhuongThucThanhToanMaPTTT   VARCHAR(20) NOT NULL,
    HoaDonID                    VARCHAR(20) NOT NULL,
    SoTien                      DECIMAL(18,2) NOT NULL,
    NgayThanhToan               DATETIME2 NULL,
    TrangThai                   NVARCHAR(50) NULL,
    TienKhachDua                DECIMAL(18,2) NOT NULL DEFAULT 0,
    TienThoi                    DECIMAL(18,2) NOT NULL DEFAULT 0,

    CONSTRAINT PK_ThanhToan
        PRIMARY KEY (MaTT),

    CONSTRAINT FK_ThanhToan_DonHang
        FOREIGN KEY (DonHangMaDonHang)
        REFERENCES DonHang(MaDonHang),

    CONSTRAINT FK_ThanhToan_KhuyenMai
        FOREIGN KEY (KhuyenMaiMaKhuyenMai)
        REFERENCES KhuyenMai(MaKhuyenMai),

    CONSTRAINT FK_ThanhToan_PhuongThuc
        FOREIGN KEY (PhuongThucThanhToanMaPTTT)
        REFERENCES PhuongThucThanhToan(MaPTTT),

    CONSTRAINT FK_ThanhToan_HoaDon
        FOREIGN KEY (HoaDonID)
        REFERENCES HoaDon(MaHoaDon),

    CONSTRAINT CK_ThanhToan_SoTien
        CHECK (SoTien >= 0),

    CONSTRAINT CK_ThanhToan_TienKhachDua
        CHECK (TienKhachDua >= 0),

    CONSTRAINT CK_ThanhToan_TienThoi
        CHECK (TienThoi >= 0)
);
GO


/* =========================================================
   20. TICH DIEM
   ========================================================= */

CREATE TABLE TichDiem
(
    MaTichDiem      VARCHAR(20) NOT NULL,
    HoaDonID         VARCHAR(20) NOT NULL,
    KhachHangMaKH    VARCHAR(20) NOT NULL,
    DiemTichLuy      INT NOT NULL,
    NgayCapNhat      DATETIME2 NULL,
    QuyDoiDiem       INT NOT NULL,

    CONSTRAINT PK_TichDiem
        PRIMARY KEY (MaTichDiem),

    CONSTRAINT FK_TichDiem_HoaDon
        FOREIGN KEY (HoaDonID)
        REFERENCES HoaDon(MaHoaDon),

    CONSTRAINT FK_TichDiem_KhachHang
        FOREIGN KEY (KhachHangMaKH)
        REFERENCES KhachHang(MaKH),

    CONSTRAINT CK_TichDiem_Diem
        CHECK (DiemTichLuy >= 0),

    CONSTRAINT CK_TichDiem_QuyDoi
        CHECK (QuyDoiDiem >= 0)
);
GO


/* =========================================================
   21. DICH VU
   ========================================================= */

CREATE TABLE DichVu
(
    MaDichVu       VARCHAR(20) NOT NULL,
    TenDichVu      NVARCHAR(150) NULL,
    DonGia         DECIMAL(18,2) NOT NULL,
    MoTa           NVARCHAR(255) NULL,
    TrangThai      BIT NOT NULL DEFAULT 1,

    CONSTRAINT PK_DichVu
        PRIMARY KEY (MaDichVu),

    CONSTRAINT CK_DichVu_DonGia
        CHECK (DonGia >= 0)
);
GO


/* =========================================================
   22. LICH HEN
   ========================================================= */

CREATE TABLE LichHen
(
    MaLichHen       VARCHAR(20) NOT NULL,
    ThuCungMaThuCung VARCHAR(20) NOT NULL,
    KhachHangMaKH   VARCHAR(20) NOT NULL,
    NhanVienMaNV    VARCHAR(20) NOT NULL,
    ThoiGianHen     DATETIME2 NULL,
    TrangThai       NVARCHAR(50) NULL,

    CONSTRAINT PK_LichHen
        PRIMARY KEY (MaLichHen),

    CONSTRAINT FK_LichHen_ThuCung
        FOREIGN KEY (ThuCungMaThuCung)
        REFERENCES ThuCung(MaThuCung),

    CONSTRAINT FK_LichHen_KhachHang
        FOREIGN KEY (KhachHangMaKH)
        REFERENCES KhachHang(MaKH),

    CONSTRAINT FK_LichHen_NhanVien
        FOREIGN KEY (NhanVienMaNV)
        REFERENCES NhanVien(MaNV)
);
GO


/* =========================================================
   23. CHI TIET DICH VU
   ========================================================= */

CREATE TABLE ChiTietDichVu
(
    DichVuMaDichVu      VARCHAR(20) NOT NULL,
    LichHenMaLichHen    VARCHAR(20) NOT NULL,
    DonHangMaDonHang    VARCHAR(20) NOT NULL,
    PhuThu               DECIMAL(18,2) NOT NULL DEFAULT 0,

    CONSTRAINT PK_ChiTietDichVu
        PRIMARY KEY
        (
            DichVuMaDichVu,
            LichHenMaLichHen,
            DonHangMaDonHang
        ),

    CONSTRAINT FK_ChiTietDichVu_DichVu
        FOREIGN KEY (DichVuMaDichVu)
        REFERENCES DichVu(MaDichVu),

    CONSTRAINT FK_ChiTietDichVu_LichHen
        FOREIGN KEY (LichHenMaLichHen)
        REFERENCES LichHen(MaLichHen),

    CONSTRAINT FK_ChiTietDichVu_DonHang
        FOREIGN KEY (DonHangMaDonHang)
        REFERENCES DonHang(MaDonHang),

    CONSTRAINT CK_ChiTietDichVu_PhuThu
        CHECK (PhuThu >= 0)
);
GO


/* =========================================================
   INDEX
   ========================================================= */

CREATE INDEX IX_NhanVien_TaiKhoan
ON NhanVien(TaiKhoanMaTK);
GO

CREATE INDEX IX_ThuCung_KhachHang
ON ThuCung(KhachHangMaKH);
GO

CREATE INDEX IX_SanPham_Loai
ON SanPham(LoaiSanPhamMaLoai);
GO

CREATE INDEX IX_DonHang_KhachHang
ON DonHang(KhachHangMaKH);
GO

CREATE INDEX IX_DonHang_NhanVien
ON DonHang(NhanVienMaNV);
GO

CREATE INDEX IX_DonHang_NgayTao
ON DonHang(NgayTao);
GO

CREATE INDEX IX_ChiTietDonHang_SanPham
ON ChiTietDonHang(SanPhamMaSP);
GO

CREATE INDEX IX_PhieuNhap_NhaCungCap
ON PhieuNhap(NhaCungCapMaNCC);
GO

CREATE INDEX IX_ChiTietPhieuNhap_SanPham
ON ChiTietPhieuNhap(SanPhamMaSP);
GO

CREATE INDEX IX_LichHen_ThoiGian
ON LichHen(ThoiGianHen);
GO

CREATE INDEX IX_LichHen_NhanVien
ON LichHen(NhanVienMaNV);
GO

CREATE INDEX IX_LichLamViec_NhanVien
ON LichLamViec(NhanVienMaNV);
GO

CREATE INDEX IX_ThanhToan_Ngay
ON ThanhToan(NgayThanhToan);
GO


/* =========================================================
   HOAN TAT
   ========================================================= */

PRINT N'==============================================';
PRINT N'DATABASE PetCareDB DA DUOC TAO THANH CONG';
PRINT N'==============================================';
GO