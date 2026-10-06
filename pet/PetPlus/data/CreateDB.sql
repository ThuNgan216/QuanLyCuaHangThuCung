/* ============================================================
   PETPLUS DATABASE
   HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG

   BÁM THEO CLASS DIAGRAM: 19 THỰC THỂ
   ============================================================ */

USE master;
GO

/* ============================================================
   XÓA DATABASE CŨ
   ============================================================ */

IF DB_ID('PETPLUS') IS NOT NULL
BEGIN
    ALTER DATABASE PETPLUS
    SET SINGLE_USER
    WITH ROLLBACK IMMEDIATE;

    DROP DATABASE PETPLUS;
END;
GO


/* ============================================================
   TẠO DATABASE MỚI
   ============================================================ */

CREATE DATABASE PETPLUS;
GO

USE PETPLUS;
GO


/* ============================================================
   1. TAI KHOAN
   ============================================================ */

CREATE TABLE TaiKhoan
(
    maTK        VARCHAR(20)    NOT NULL,
    tenDangNhap NVARCHAR(50)   NOT NULL,
    matKhau     NVARCHAR(255)  NOT NULL,
    vaiTro      NVARCHAR(50)   NOT NULL,
    trangThai   NVARCHAR(50)   NOT NULL ,

    CONSTRAINT PK_TaiKhoan
        PRIMARY KEY (maTK),

    CONSTRAINT UQ_TaiKhoan_TenDangNhap
        UNIQUE (tenDangNhap)
);
GO


/* ============================================================
   2. NHAN VIEN

   TaiKhoan 1 ----- 1 NhanVien
   ============================================================ */

CREATE TABLE NhanVien
(
    maNV    VARCHAR(20)    NOT NULL,
    tenNV   NVARCHAR(100)  NOT NULL,
    sdt     VARCHAR(15)    NULL,
    cccd    VARCHAR(20)    NULL,
    diaChi  NVARCHAR(255)  NULL,
    email   VARCHAR(100)   NULL,
    chucVu  NVARCHAR(100)  NULL,

    maTK    VARCHAR(20)    NOT NULL,

    CONSTRAINT PK_NhanVien
        PRIMARY KEY (maNV),

    CONSTRAINT UQ_NhanVien_CCCD
        UNIQUE (cccd),

    CONSTRAINT UQ_NhanVien_MaTK
        UNIQUE (maTK),

    CONSTRAINT FK_NhanVien_TaiKhoan
        FOREIGN KEY (maTK)
        REFERENCES TaiKhoan(maTK)
);
GO


/* ============================================================
   3. CA LAM VIEC

   LocalTime -> TIME
   ============================================================ */

CREATE TABLE CaLamViec
(
    maCa        VARCHAR(20)    NOT NULL,
    gioBatDau   Time           NOT NULL,
    gioKetThuc  Time           NOT NULL,
    tenCa       NVARCHAR(100)  NOT NULL,

    CONSTRAINT PK_CaLamViec
        PRIMARY KEY (maCa),

    CONSTRAINT CK_CaLamViec_ThoiGian
        CHECK (gioKetThuc > gioBatDau)
);
GO


/* ============================================================
   4. LICH LAM VIEC

   ngayLamViec : LocalDate -> DATE

   NhanVien 1 ----- 0..* LichLamViec
   CaLamViec 1 ---- 0..* LichLamViec
   ============================================================ */

CREATE TABLE LichLamViec
(
    maLichLamViec VARCHAR(20) NOT NULL,
    ngayLamViec   DATE        NOT NULL,

    maNV          VARCHAR(20) NOT NULL,
    maCa          VARCHAR(20) NOT NULL,

    CONSTRAINT PK_LichLamViec
        PRIMARY KEY (maLichLamViec),

    CONSTRAINT FK_LichLamViec_NhanVien
        FOREIGN KEY (maNV)
        REFERENCES NhanVien(maNV),

    CONSTRAINT FK_LichLamViec_CaLamViec
        FOREIGN KEY (maCa)
        REFERENCES CaLamViec(maCa),

    CONSTRAINT UQ_LichLamViec
        UNIQUE (ngayLamViec, maNV, maCa)
);
GO


/* ============================================================
   5. KHACH HANG
   ============================================================ */

CREATE TABLE KhachHang
(
    maKH           VARCHAR(20)    NOT NULL,
    sdt            VARCHAR(15)    NULL,
    diaChi         NVARCHAR(255)  NULL,
    hangThanhVien  INT            NULL,
    diemTichLuy    INT            NOT NULL DEFAULT 0,

    CONSTRAINT PK_KhachHang
        PRIMARY KEY (maKH),

    CONSTRAINT CK_KhachHang_Diem
        CHECK (diemTichLuy >= 0)
);
GO


/* ============================================================
   6. THU CUNG

   ngaySinh -> DATE
   canNang -> DECIMAL(6,2)

   KhachHang 1 ----- 1..* ThuCung
   ============================================================ */

CREATE TABLE ThuCung
(
    maThuCung    VARCHAR(20)    NOT NULL,
    tenThuCung   NVARCHAR(100)  NOT NULL,
    loai         NVARCHAR(50)   NOT NULL,
    giong        NVARCHAR(100)  NULL,
    gioiTinh     NVARCHAR(20)   NULL,

    canNang      float   NULL,
    ngaySinh     DATE           NULL,

    tinhTrangSK  NVARCHAR(255)  NULL,
    tiemChung    NVARCHAR(255)  NULL,

    maKH         VARCHAR(20)    NOT NULL,

    CONSTRAINT PK_ThuCung
        PRIMARY KEY (maThuCung),

    CONSTRAINT FK_ThuCung_KhachHang
        FOREIGN KEY (maKH)
        REFERENCES KhachHang(maKH),

    CONSTRAINT CK_ThuCung_CanNang
        CHECK (canNang IS NULL OR canNang >= 0)
);
GO


/* ============================================================
   7. SAN PHAM

   Giá tiền -> DECIMAL
   ============================================================ */

CREATE TABLE SanPham
(
    maSP        VARCHAR(20)     NOT NULL,
    tenSP       NVARCHAR(150)   NOT NULL,
    giaBan      float   NOT NULL,
    soLuongTon  INT             NOT NULL DEFAULT 0,

    CONSTRAINT PK_SanPham
        PRIMARY KEY (maSP),

    CONSTRAINT CK_SanPham_GiaBan
        CHECK (giaBan >= 0),

    CONSTRAINT CK_SanPham_SoLuong
        CHECK (soLuongTon >= 0)
);
GO


/* ============================================================
   8. MA VACH

   SanPham 1 ----- 1 MaVach
   ============================================================ */

CREATE TABLE MaVach
(
    maVach VARCHAR(50) NOT NULL,
    maSP   VARCHAR(20) NOT NULL,

    CONSTRAINT PK_MaVach
        PRIMARY KEY (maVach),

    CONSTRAINT UQ_MaVach_MaSP
        UNIQUE (maSP),

    CONSTRAINT FK_MaVach_SanPham
        FOREIGN KEY (maSP)
        REFERENCES SanPham(maSP)
);
GO


/* ============================================================
   9. NHA CUNG CAP
   ============================================================ */

CREATE TABLE NhaCungCap
(
    maNCC       VARCHAR(20)    NOT NULL,
    tenNCC      NVARCHAR(150)  NOT NULL,
    soDienThoai VARCHAR(15)    NULL,
    diaChi      NVARCHAR(255)  NULL,
    email       VARCHAR(100)   NULL,

    CONSTRAINT PK_NhaCungCap
        PRIMARY KEY (maNCC)
);
GO


/* ============================================================
   10. PHIEU NHAP

   ngayNhap là ngày nhập hàng -> DATETIME2

   NhanVien -> PhieuNhap
   NhaCungCap -> PhieuNhap
   ============================================================ */

CREATE TABLE PhieuNhap
(
    maPN      VARCHAR(20) NOT NULL,
    ngayNhap  DATE  NOT NULL ,

    maNV      VARCHAR(20) NOT NULL,
    maNCC     VARCHAR(20) NOT NULL,

    CONSTRAINT PK_PhieuNhap
        PRIMARY KEY (maPN),

    CONSTRAINT FK_PhieuNhap_NhanVien
        FOREIGN KEY (maNV)
        REFERENCES NhanVien(maNV),

    CONSTRAINT FK_PhieuNhap_NhaCungCap
        FOREIGN KEY (maNCC)
        REFERENCES NhaCungCap(maNCC)
);
GO


/* ============================================================
   11. CHI TIET PHIEU NHAP

   hanSuDung -> DATE
   donGiaNhap -> DECIMAL
   thanhTien là thuộc tính dẫn xuất
   ============================================================ */

CREATE TABLE ChiTietPhieuNhap
(
    ID           INT IDENTITY(1,1) NOT NULL,

    soLuongNhap  INT               NOT NULL,
    donGiaNhap   DECIMAL(18,2)     NOT NULL,
    hanSuDung    DATE              NULL,

    maPN         VARCHAR(20)       NOT NULL,
    maSP         VARCHAR(20)       NOT NULL,

    thanhTien AS
    (
        CONVERT
        (
            DECIMAL(18,2),
            soLuongNhap * donGiaNhap
        )
    ) PERSISTED,

    CONSTRAINT PK_ChiTietPhieuNhap
        PRIMARY KEY (ID),

    CONSTRAINT FK_ChiTietPhieuNhap_PhieuNhap
        FOREIGN KEY (maPN)
        REFERENCES PhieuNhap(maPN),

    CONSTRAINT FK_ChiTietPhieuNhap_SanPham
        FOREIGN KEY (maSP)
        REFERENCES SanPham(maSP),

    CONSTRAINT UQ_ChiTietPhieuNhap
        UNIQUE (maPN, maSP),

    CONSTRAINT CK_ChiTietPhieuNhap_SoLuong
        CHECK (soLuongNhap > 0),

    CONSTRAINT CK_ChiTietPhieuNhap_DonGia
        CHECK (donGiaNhap >= 0)
);
GO


/* ============================================================
   12. LICH HEN

   thoiGianHen : LocalDateTime -> DATETIME2

   NhanVien -> LichHen
   KhachHang -> LichHen
   ThuCung -> LichHen
   ============================================================ */

CREATE TABLE LichHen
(
    maLichHen    VARCHAR(20)   NOT NULL,
    thoiGianHen  DATETIME2     NOT NULL,
    trangThai    NVARCHAR(50)  NOT NULL,

    maNV         VARCHAR(20)   NOT NULL,
    maKH         VARCHAR(20)   NOT NULL,
    maThuCung    VARCHAR(20)   NOT NULL,

    CONSTRAINT PK_LichHen
        PRIMARY KEY (maLichHen),

    CONSTRAINT FK_LichHen_NhanVien
        FOREIGN KEY (maNV)
        REFERENCES NhanVien(maNV),

    CONSTRAINT FK_LichHen_KhachHang
        FOREIGN KEY (maKH)
        REFERENCES KhachHang(maKH),

    CONSTRAINT FK_LichHen_ThuCung
        FOREIGN KEY (maThuCung)
        REFERENCES ThuCung(maThuCung)
);
GO


/* ============================================================
   13. DICH VU

   donGia -> DECIMAL
   trangThai -> BIT

   Theo sơ đồ:
   LichHen 1 ----- 1..* DichVu
   ============================================================ */

CREATE TABLE DichVu
(
    maDichVu   VARCHAR(20)     NOT NULL,
    tenDichVu  NVARCHAR(150)   NOT NULL,
    donGia     DECIMAL(18,2)   NOT NULL,
    moTa       NVARCHAR(500)   NULL,
    trangThai  BIT             NOT NULL DEFAULT 1,

    maLichHen  VARCHAR(20)     NOT NULL,

    CONSTRAINT PK_DichVu
        PRIMARY KEY (maDichVu),

    CONSTRAINT FK_DichVu_LichHen
        FOREIGN KEY (maLichHen)
        REFERENCES LichHen(maLichHen),

    CONSTRAINT CK_DichVu_DonGia
        CHECK (donGia >= 0)
);
GO


/* ============================================================
   14. KHUYEN MAI

   ngayBatDau -> DATE
   ngayKetThuc -> DATE
   % giảm -> DECIMAL
   ============================================================ */

CREATE TABLE KhuyenMai
(
    maKhuyenMai     VARCHAR(20)    NOT NULL,
    tenKhuyenMai    NVARCHAR(150)  NOT NULL,

    phanTramGiam    DECIMAL(5,2)   NOT NULL,

    dieuKienApDung  NVARCHAR(500)  NULL,

    ngayBatDau      DATE           NOT NULL,
    ngayKetThuc     DATE           NOT NULL,

    CONSTRAINT PK_KhuyenMai
        PRIMARY KEY (maKhuyenMai),

    CONSTRAINT CK_KhuyenMai_PhanTram
        CHECK
        (
            phanTramGiam >= 0
            AND phanTramGiam <= 100
        ),

    CONSTRAINT CK_KhuyenMai_Ngay
        CHECK (ngayKetThuc >= ngayBatDau)
);
GO


/* ============================================================
   15. PHUONG THUC THANH TOAN
   ============================================================ */

CREATE TABLE PhuongThucThanhToan
(
    maPTTT   VARCHAR(20)    NOT NULL,
    tenPTTT  NVARCHAR(100)  NOT NULL,

    CONSTRAINT PK_PhuongThucThanhToan
        PRIMARY KEY (maPTTT),

    CONSTRAINT UQ_PhuongThucThanhToan
        UNIQUE (tenPTTT)
);
GO


/* ============================================================
   16. HOA DON

   ngayLapHD -> DATETIME2

   NhanVien -> HoaDon
   KhachHang -> HoaDon
   KhuyenMai -> HoaDon
   PhuongThucThanhToan -> HoaDon

   /tinhTienGiam và /tongTien là derived
   => không lưu trực tiếp.
   ============================================================ */

CREATE TABLE HoaDon
(
    maHoaDon     VARCHAR(20) NOT NULL,

    ngayLapHD    DATETIME2   NOT NULL
                  DEFAULT SYSDATETIME(),

    maNV         VARCHAR(20) NOT NULL,
    maKH         VARCHAR(20) NOT NULL,
    maKhuyenMai  VARCHAR(20) NOT NULL,
    maPTTT       VARCHAR(20) NOT NULL,

    CONSTRAINT PK_HoaDon
        PRIMARY KEY (maHoaDon),

    CONSTRAINT FK_HoaDon_NhanVien
        FOREIGN KEY (maNV)
        REFERENCES NhanVien(maNV),

    CONSTRAINT FK_HoaDon_KhachHang
        FOREIGN KEY (maKH)
        REFERENCES KhachHang(maKH),

    CONSTRAINT FK_HoaDon_KhuyenMai
        FOREIGN KEY (maKhuyenMai)
        REFERENCES KhuyenMai(maKhuyenMai),

    CONSTRAINT FK_HoaDon_PhuongThucThanhToan
        FOREIGN KEY (maPTTT)
        REFERENCES PhuongThucThanhToan(maPTTT)
);
GO


/* ============================================================
   17. CHI TIET HOA DON

   HoaDon 1 ----- 1..* ChiTietHoaDon

   Theo sơ đồ class hiện tại:
   ChiTietHoaDon liên kết với DichVu.

   thanhTien là derived.
   ============================================================ */

CREATE TABLE ChiTietHoaDon
(
    maHoaDon  VARCHAR(20)     NOT NULL,
    maDichVu  VARCHAR(20)     NOT NULL,

    soLuong   INT             NOT NULL,
    donGia    DECIMAL(18,2)   NOT NULL,

    thanhTien AS
    (
        CONVERT
        (
            DECIMAL(18,2),
            soLuong * donGia
        )
    ) PERSISTED,

    CONSTRAINT PK_ChiTietHoaDon
        PRIMARY KEY (maHoaDon, maDichVu),

    CONSTRAINT FK_ChiTietHoaDon_HoaDon
        FOREIGN KEY (maHoaDon)
        REFERENCES HoaDon(maHoaDon),

    CONSTRAINT FK_ChiTietHoaDon_DichVu
        FOREIGN KEY (maDichVu)
        REFERENCES DichVu(maDichVu),

    CONSTRAINT CK_ChiTietHoaDon_SoLuong
        CHECK (soLuong > 0),

    CONSTRAINT CK_ChiTietHoaDon_DonGia
        CHECK (donGia >= 0)
);
GO


/* ============================================================
   18. KHUYEN MAI SAN PHAM

   Liên kết:
   SanPham
   KhuyenMai
   HoaDon

   mucGiam -> DECIMAL
   ============================================================ */

CREATE TABLE KhuyenMaiSanPham
(
    maKhuyenMaiMaSanPham VARCHAR(50)    NOT NULL,
    mucGiam              DECIMAL(18,2)  NOT NULL,

    maSP                 VARCHAR(20)    NOT NULL,
    maKhuyenMai          VARCHAR(20)    NOT NULL,
    maHoaDon             VARCHAR(20)    NOT NULL,

    CONSTRAINT PK_KhuyenMaiSanPham
        PRIMARY KEY (maKhuyenMaiMaSanPham),

    CONSTRAINT FK_KhuyenMaiSanPham_SanPham
        FOREIGN KEY (maSP)
        REFERENCES SanPham(maSP),

    CONSTRAINT FK_KhuyenMaiSanPham_KhuyenMai
        FOREIGN KEY (maKhuyenMai)
        REFERENCES KhuyenMai(maKhuyenMai),

    CONSTRAINT FK_KhuyenMaiSanPham_HoaDon
        FOREIGN KEY (maHoaDon)
        REFERENCES HoaDon(maHoaDon),

    CONSTRAINT CK_KhuyenMaiSanPham_MucGiam
        CHECK (mucGiam >= 0)
);
GO


/* ============================================================
   19. TICH DIEM

   ngayCapNhat -> DATETIME2

   KhachHang 1 ----- 0..* TichDiem
   HoaDon 1 -------- 0..1 TichDiem

   /quyDoiDiem không lưu trực tiếp.
   ============================================================ */

CREATE TABLE TichDiem
(
    maTichDiem   VARCHAR(20) NOT NULL,
    diemTichLuy  INT         NOT NULL,

    ngayCapNhat  DATETIME2   NOT NULL
                  DEFAULT SYSDATETIME(),

    maKH         VARCHAR(20) NOT NULL,
    maHoaDon     VARCHAR(20) NOT NULL,

    CONSTRAINT PK_TichDiem
        PRIMARY KEY (maTichDiem),

    CONSTRAINT FK_TichDiem_KhachHang
        FOREIGN KEY (maKH)
        REFERENCES KhachHang(maKH),

    CONSTRAINT FK_TichDiem_HoaDon
        FOREIGN KEY (maHoaDon)
        REFERENCES HoaDon(maHoaDon),

    CONSTRAINT UQ_TichDiem_HoaDon
        UNIQUE (maHoaDon),

    CONSTRAINT CK_TichDiem_Diem
        CHECK (diemTichLuy >= 0)
);
GO


/* ============================================================
   INDEX
   ============================================================ */

CREATE INDEX IX_LichLamViec_MaNV
ON LichLamViec(maNV);
GO

CREATE INDEX IX_LichLamViec_Ngay
ON LichLamViec(ngayLamViec);
GO

CREATE INDEX IX_ThuCung_MaKH
ON ThuCung(maKH);
GO

CREATE INDEX IX_PhieuNhap_MaNV
ON PhieuNhap(maNV);
GO

CREATE INDEX IX_PhieuNhap_MaNCC
ON PhieuNhap(maNCC);
GO

CREATE INDEX IX_PhieuNhap_NgayNhap
ON PhieuNhap(ngayNhap);
GO

CREATE INDEX IX_LichHen_MaKH
ON LichHen(maKH);
GO

CREATE INDEX IX_LichHen_MaThuCung
ON LichHen(maThuCung);
GO

CREATE INDEX IX_LichHen_MaNV
ON LichHen(maNV);
GO

CREATE INDEX IX_LichHen_ThoiGianHen
ON LichHen(thoiGianHen);
GO

CREATE INDEX IX_HoaDon_MaKH
ON HoaDon(maKH);
GO

CREATE INDEX IX_HoaDon_MaNV
ON HoaDon(maNV);
GO

CREATE INDEX IX_HoaDon_NgayLapHD
ON HoaDon(ngayLapHD);
GO


/* ============================================================
   KIỂM TRA SỐ BẢNG
   ============================================================ */

SELECT
    TABLE_NAME AS TenBang
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
GO


/* ============================================================
   KIỂM TRA KIỂU DỮ LIỆU CÁC CỘT NGÀY
   ============================================================ */

SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE COLUMN_NAME IN
(
    'ngaySinh',
    'ngayLamViec',
    'ngayNhap',
    'hanSuDung',
    'thoiGianHen',
    'ngayBatDau',
    'ngayKetThuc',
    'ngayLapHD',
    'ngayCapNhat'
)
ORDER BY TABLE_NAME, COLUMN_NAME;
GO


PRINT N'================================================';
PRINT N'PETPLUS ĐÃ ĐƯỢC TẠO THÀNH CÔNG';
PRINT N'19 BẢNG - ĐÃ SỬA ĐÚNG KIỂU DỮ LIỆU';
PRINT N'================================================';
GO