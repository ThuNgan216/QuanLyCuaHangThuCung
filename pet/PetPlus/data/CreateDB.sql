/* ============================================================
   PETPLUS DATABASE
   HE THONG QUAN LY CUA HANG THU CUNG

   19 THUC THE + DU LIEU MAU
   SQL Server

   LUU Y: Script nay XOA database PETPLUS cu va tao lai tu dau.
   Hay sao luu du lieu cu truoc khi chay neu can giu lai.
   ============================================================ */

USE master;
GO

/* 1. XOA DATABASE CU NEU TON TAI */
IF DB_ID(N'PETPLUS') IS NOT NULL
BEGIN
    ALTER DATABASE PETPLUS SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE PETPLUS;
END;
GO

/* 2. TAO DATABASE MOI */
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
    trangThai   NVARCHAR(50)   NOT NULL,
    CONSTRAINT PK_TaiKhoan PRIMARY KEY (maTK),
    CONSTRAINT UQ_TaiKhoan_TenDangNhap UNIQUE (tenDangNhap)
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
    CONSTRAINT PK_NhanVien PRIMARY KEY (maNV),
    CONSTRAINT UQ_NhanVien_CCCD UNIQUE (cccd),
    CONSTRAINT UQ_NhanVien_MaTK UNIQUE (maTK),
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK)
);
GO

/* ============================================================
   3. CA LAM VIEC
   ============================================================ */
CREATE TABLE CaLamViec
(
    maCa        VARCHAR(20)    NOT NULL,
    gioBatDau   TIME           NOT NULL,
    gioKetThuc  TIME           NOT NULL,
    tenCa       NVARCHAR(100)  NOT NULL,
    CONSTRAINT PK_CaLamViec PRIMARY KEY (maCa),
    CONSTRAINT CK_CaLamViec_ThoiGian CHECK (gioKetThuc > gioBatDau)
);
GO

/* ============================================================
   4. LICH LAM VIEC
   ============================================================ */
CREATE TABLE LichLamViec
(
    maLichLamViec VARCHAR(20) NOT NULL,
    ngayLamViec   DATE        NOT NULL,
    maNV          VARCHAR(20) NOT NULL,
    maCa          VARCHAR(20) NOT NULL,
    CONSTRAINT PK_LichLamViec PRIMARY KEY (maLichLamViec),
    CONSTRAINT FK_LichLamViec_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_LichLamViec_CaLamViec FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa),
    CONSTRAINT UQ_LichLamViec UNIQUE (ngayLamViec, maNV, maCa)
);
GO

/* ============================================================
   5. KHACH HANG
   Da khai bao tenKH truc tiep trong cau truc bang.
   ============================================================ */
CREATE TABLE KhachHang
(
    maKH           VARCHAR(20)    NOT NULL,
    tenKH          NVARCHAR(100)  NOT NULL,
    sdt            VARCHAR(15)    NULL,
    diaChi         NVARCHAR(255)  NULL,
    hangThanhVien  INT            NULL,
    diemTichLuy    INT            NOT NULL CONSTRAINT DF_KhachHang_Diem DEFAULT 0,
    CONSTRAINT PK_KhachHang PRIMARY KEY (maKH),
    CONSTRAINT CK_KhachHang_Diem CHECK (diemTichLuy >= 0)
);
GO

/* ============================================================
   6. THU CUNG
   ============================================================ */
CREATE TABLE ThuCung
(
    maThuCung    VARCHAR(20)    NOT NULL,
    tenThuCung   NVARCHAR(100)  NOT NULL,
    loai         NVARCHAR(50)   NOT NULL,
    giong        NVARCHAR(100)  NULL,
    gioiTinh     NVARCHAR(20)   NULL,
    canNang      FLOAT          NULL,
    ngaySinh     DATE           NULL,
    tinhTrangSK  NVARCHAR(255)  NULL,
    tiemChung    NVARCHAR(255)  NULL,
    maKH         VARCHAR(20)    NOT NULL,
    CONSTRAINT PK_ThuCung PRIMARY KEY (maThuCung),
    CONSTRAINT FK_ThuCung_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT CK_ThuCung_CanNang CHECK (canNang IS NULL OR canNang >= 0)
);
GO

/* ============================================================
   7. SAN PHAM
   ============================================================ */
CREATE TABLE SanPham
(
    maSP        VARCHAR(20)     NOT NULL,
    tenSP       NVARCHAR(150)   NOT NULL,
    giaBan      FLOAT           NOT NULL,
    soLuongTon  INT             NOT NULL CONSTRAINT DF_SanPham_SoLuong DEFAULT 0,
    CONSTRAINT PK_SanPham PRIMARY KEY (maSP),
    CONSTRAINT CK_SanPham_GiaBan CHECK (giaBan >= 0),
    CONSTRAINT CK_SanPham_SoLuong CHECK (soLuongTon >= 0)
);
GO

/* ============================================================
   8. MA VACH
   ============================================================ */
CREATE TABLE MaVach
(
    maVach VARCHAR(50) NOT NULL,
    maSP   VARCHAR(20) NOT NULL,
    CONSTRAINT PK_MaVach PRIMARY KEY (maVach),
    CONSTRAINT UQ_MaVach_MaSP UNIQUE (maSP),
    CONSTRAINT FK_MaVach_SanPham FOREIGN KEY (maSP) REFERENCES SanPham(maSP)
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
    CONSTRAINT PK_NhaCungCap PRIMARY KEY (maNCC)
);
GO

/* ============================================================
   10. PHIEU NHAP
   ============================================================ */
CREATE TABLE PhieuNhap
(
    maPN      VARCHAR(20) NOT NULL,
    ngayNhap  DATE        NOT NULL,
    maNV      VARCHAR(20) NOT NULL,
    maNCC     VARCHAR(20) NOT NULL,
    CONSTRAINT PK_PhieuNhap PRIMARY KEY (maPN),
    CONSTRAINT FK_PhieuNhap_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PhieuNhap_NhaCungCap FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC)
);
GO

/* ============================================================
   11. CHI TIET PHIEU NHAP
   ============================================================ */
CREATE TABLE ChiTietPhieuNhap
(
    soLuongNhap  INT             NOT NULL,
    donGiaNhap   DECIMAL(18,2)   NOT NULL,
    hanSuDung    DATE            NULL,
    maPN         VARCHAR(20)     NOT NULL,
    maSP         VARCHAR(20)     NOT NULL,
    CONSTRAINT FK_ChiTietPhieuNhap_PhieuNhap FOREIGN KEY (maPN) REFERENCES PhieuNhap(maPN),
    CONSTRAINT FK_ChiTietPhieuNhap_SanPham FOREIGN KEY (maSP) REFERENCES SanPham(maSP),
    CONSTRAINT UQ_ChiTietPhieuNhap UNIQUE (maPN, maSP),
    CONSTRAINT CK_ChiTietPhieuNhap_SoLuong CHECK (soLuongNhap > 0),
    CONSTRAINT CK_ChiTietPhieuNhap_DonGia CHECK (donGiaNhap >= 0)
);
GO

/* ============================================================
   12. LICH HEN
   ============================================================ */
CREATE TABLE LichHen
(
    maLichHen    VARCHAR(20)   NOT NULL,
    thoiGianHen  DATETIME2     NOT NULL,
    trangThai    NVARCHAR(50)  NOT NULL,
    maNV         VARCHAR(20)   NOT NULL,
    maKH         VARCHAR(20)   NOT NULL,
    maThuCung    VARCHAR(20)   NOT NULL,
    CONSTRAINT PK_LichHen PRIMARY KEY (maLichHen),
    CONSTRAINT FK_LichHen_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_LichHen_ThuCung FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung)
);
GO

/* ============================================================
   13. DICH VU
   ============================================================ */
CREATE TABLE DichVu
(
    maDichVu   VARCHAR(20)     NOT NULL,
    tenDichVu  NVARCHAR(150)   NOT NULL,
    donGia     DECIMAL(18,2)   NOT NULL,
    moTa       NVARCHAR(500)   NULL,
    trangThai  NVARCHAR(20)    NOT NULL,
    maLichHen  VARCHAR(20)     NOT NULL,
    CONSTRAINT PK_DichVu PRIMARY KEY (maDichVu),
    CONSTRAINT FK_DichVu_LichHen FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen),
    CONSTRAINT CK_DichVu_DonGia CHECK (donGia >= 0)
);
GO

/* ============================================================
   14. KHUYEN MAI
   ============================================================ */
CREATE TABLE KhuyenMai
(
    maKhuyenMai     VARCHAR(20)    NOT NULL,
    tenKhuyenMai    NVARCHAR(150)  NOT NULL,
    phanTramGiam    FLOAT          NOT NULL,
    dieuKienApDung  NVARCHAR(500)  NULL,
    ngayBatDau      DATE           NOT NULL,
    ngayKetThuc     DATE           NOT NULL,
    CONSTRAINT PK_KhuyenMai PRIMARY KEY (maKhuyenMai),
    CONSTRAINT CK_KhuyenMai_PhanTram CHECK (phanTramGiam >= 0 AND phanTramGiam <= 100),
    CONSTRAINT CK_KhuyenMai_Ngay CHECK (ngayKetThuc >= ngayBatDau)
);
GO
/* ============================================================
   15. PHUONG THUC THANH TOAN
   ============================================================ */
CREATE TABLE PhuongThucThanhToan
(
    maPTTT   VARCHAR(20)    NOT NULL,
    tenPTTT  NVARCHAR(100)  NOT NULL,
    CONSTRAINT PK_PhuongThucThanhToan PRIMARY KEY (maPTTT),
    CONSTRAINT UQ_PhuongThucThanhToan UNIQUE (tenPTTT)
);
GO

/* ============================================================
   16. HOA DON
   ============================================================ */
CREATE TABLE HoaDon
(
    maHoaDon     VARCHAR(20) NOT NULL,
    ngayLapHD    DATE        NOT NULL,
    maNV         VARCHAR(20) NOT NULL,
    maKH         VARCHAR(20) NOT NULL,
    maKhuyenMai  VARCHAR(20) NULL,
    maPTTT       VARCHAR(20) NULL,
    laTiepNhan   BIT NOT NULL CONSTRAINT DF_HoaDon_LaTiepNhan DEFAULT (0),
    maLichHen    VARCHAR(20) NULL,
    maThuCung    VARCHAR(20) NULL,
    maNVTiepNhan VARCHAR(20) NULL,
    thoiGianTiepNhan DATETIME2 NULL,
    gioHenTra    DATETIME2 NULL,
    dichVuYeuCau NVARCHAR(500) NULL,
    tinhTrangBanGiao NVARCHAR(1000) NULL,
    ghiChuTiepNhan NVARCHAR(1000) NULL,
    phiDuKien    DECIMAL(18,2) NULL,
    trangThaiTiepNhan NVARCHAR(50) NULL,
    CONSTRAINT PK_HoaDon PRIMARY KEY (maHoaDon),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai),
    CONSTRAINT FK_HoaDon_PhuongThucThanhToan FOREIGN KEY (maPTTT) REFERENCES PhuongThucThanhToan(maPTTT),
    CONSTRAINT FK_HoaDon_LichHen FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen),
    CONSTRAINT FK_HoaDon_ThuCung FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung),
    CONSTRAINT FK_HoaDon_NVTiepNhan FOREIGN KEY (maNVTiepNhan) REFERENCES NhanVien(maNV),
    CONSTRAINT CK_HoaDon_PhiDuKien CHECK (phiDuKien IS NULL OR phiDuKien >= 0),
    CONSTRAINT CK_HoaDon_ThoiGianTiepNhan CHECK (
        laTiepNhan = 0 OR (thoiGianTiepNhan IS NOT NULL AND gioHenTra IS NOT NULL
            AND gioHenTra > thoiGianTiepNhan
            AND maThuCung IS NOT NULL AND maNVTiepNhan IS NOT NULL AND trangThaiTiepNhan IS NOT NULL))
);
GO

SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;
GO
CREATE UNIQUE INDEX UX_HoaDon_TiepNhan_LichHen
    ON HoaDon(maLichHen) WHERE laTiepNhan = 1 AND maLichHen IS NOT NULL;
GO

/* ============================================================
   17. CHI TIET HOA DON
   ============================================================ */
CREATE TABLE ChiTietHoaDon
(
    maHoaDon  VARCHAR(20)     NOT NULL,
    maDichVu  VARCHAR(20)      NULL,
    maSP VARCHAR(20)      NULL,
    soLuong   INT             NOT NULL,
    donGia    DECIMAL(18,2)   NOT NULL,
    tienKhachDua DECIMAL(18,2)   NULL,
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maHoaDon),
    CONSTRAINT FK_ChiTietHoaDon_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_ChiTietHoaDon_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),
    CONSTRAINT CK_ChiTietHoaDon_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_ChiTietHoaDon_DonGia CHECK (donGia >= 0)
);
GO

/* ============================================================
   18. KHUYEN MAI SAN PHAM
   ============================================================ */
CREATE TABLE KhuyenMaiSanPham
(
    maKhuyenMaiMaSanPham VARCHAR(50)    NOT NULL,
    mucGiam              DECIMAL(18,2)  NOT NULL,
    maSP                 VARCHAR(20)    NOT NULL,
    maKhuyenMai          VARCHAR(20)    NOT NULL,
    maHoaDon             VARCHAR(20)    NOT NULL,
    CONSTRAINT PK_KhuyenMaiSanPham PRIMARY KEY (maKhuyenMaiMaSanPham),
    CONSTRAINT FK_KhuyenMaiSanPham_SanPham FOREIGN KEY (maSP) REFERENCES SanPham(maSP),
    CONSTRAINT FK_KhuyenMaiSanPham_KhuyenMai FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai),
    CONSTRAINT FK_KhuyenMaiSanPham_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT CK_KhuyenMaiSanPham_MucGiam CHECK (mucGiam >= 0)
);
GO

/* ============================================================
   19. TICH DIEM
   ============================================================ */
CREATE TABLE TichDiem
(
    maTichDiem   VARCHAR(20) NOT NULL,
    diemTichLuy  INT         NOT NULL,
    ngayCapNhat  DATE        NOT NULL CONSTRAINT DF_TichDiem_NgayCapNhat DEFAULT (CONVERT(DATE, SYSDATETIME())),
    maKH         VARCHAR(20) NOT NULL,
    maHoaDon     VARCHAR(20) NOT NULL,
    CONSTRAINT PK_TichDiem PRIMARY KEY (maTichDiem),
    CONSTRAINT FK_TichDiem_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_TichDiem_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT UQ_TichDiem_HoaDon UNIQUE (maHoaDon),
    CONSTRAINT CK_TichDiem_Diem CHECK (diemTichLuy >= 0)
);
GO
ALTER TABLE ChiTietHoaDon DROP CONSTRAINT PK_ChiTietHoaDon;

ALTER TABLE ChiTietHoaDon ADD maChiTiet INT IDENTITY(1,1) NOT NULL;
ALTER TABLE ChiTietHoaDon ADD CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maChiTiet);
/* ============================================================
   INDEX
   ============================================================ */
CREATE INDEX IX_LichLamViec_MaNV ON LichLamViec(maNV);
CREATE INDEX IX_LichLamViec_Ngay ON LichLamViec(ngayLamViec);
CREATE INDEX IX_ThuCung_MaKH ON ThuCung(maKH);
CREATE INDEX IX_PhieuNhap_MaNV ON PhieuNhap(maNV);
CREATE INDEX IX_PhieuNhap_MaNCC ON PhieuNhap(maNCC);
CREATE INDEX IX_PhieuNhap_NgayNhap ON PhieuNhap(ngayNhap);
CREATE INDEX IX_LichHen_MaKH ON LichHen(maKH);
CREATE INDEX IX_LichHen_MaThuCung ON LichHen(maThuCung);
CREATE INDEX IX_LichHen_MaNV ON LichHen(maNV);
CREATE INDEX IX_LichHen_ThoiGianHen ON LichHen(thoiGianHen);
CREATE INDEX IX_HoaDon_MaKH ON HoaDon(maKH);
CREATE INDEX IX_HoaDon_MaNV ON HoaDon(maNV);
CREATE INDEX IX_HoaDon_NgayLapHD ON HoaDon(ngayLapHD);
GO
USE PETPLUS;
GO
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

    -- THÊM DỮ LIỆU TÀI KHOẢN
INSERT INTO TaiKhoan (maTK, tenDangNhap, matKhau, vaiTro, trangThai) VALUES
    ('TK001', N'NV01', N'123456', N'QUAN_LY', N'Hoạt động'),
    ('TK002', N'nguyenvana', N'123456', N'BAN_HANG', N'Hoạt động'),
    ('TK003', N'tranthib', N'123456', N'CHAM_SOC', N'Hoạt động'),
    ('TK004', N'levanc', N'123456', N'BAN_HANG', N'Hoạt động'),
    ('TK005', N'phamthid', N'123456', N'CHAM_SOC', N'Hoạt động');

INSERT INTO NhanVien (maNV, tenNV, sdt, cccd, diaChi, email, chucVu, maTK) VALUES
    ('NV001', N'Nguyễn Văn An', '0901000001', '079201000001',
     N'Quận 12, TP.HCM', 'an@petplus.vn', N'Quản lý', 'TK001'),
    ('NV002', N'Trần Thị Bình', '0901000002', '079201000002',
     N'Gò Vấp, TP.HCM', 'binh@petplus.vn', N'Bán hàng / Thu ngân', 'TK002'),
    ('NV003', N'Lê Văn Cường', '0901000003', '079201000003',
     N'Tân Bình, TP.HCM', 'cuong@petplus.vn', N'Chăm sóc thú cưng', 'TK003'),
    ('NV004', N'Phạm Thị Dung', '0901000004', '079201000004',
     N'Thủ Đức, TP.HCM', 'dung@petplus.vn', N'Bán hàng / Thu ngân', 'TK004'),
    ('NV005', N'Võ Minh Đức', '0901000005', '079201000005',
     N'Bình Thạnh, TP.HCM', 'duc@petplus.vn', N'Chăm sóc thú cưng', 'TK005');

    INSERT INTO CaLamViec (maCa, gioBatDau, gioKetThuc, tenCa) VALUES
    ('CA001', '07:00', '12:00', N'Ca sáng'),
    ('CA002', '12:00', '17:00', N'Ca chiều'),
    ('CA003', '17:00', '22:00', N'Ca tối');

    INSERT INTO LichLamViec (maLichLamViec, ngayLamViec, maNV, maCa) VALUES
    ('LLV001', '2026-10-13', 'NV001', 'CA001'),
    ('LLV002', '2026-10-13', 'NV002', 'CA002'),
    ('LLV003', '2026-10-13', 'NV003', 'CA002'),
    ('LLV004', '2026-10-13', 'NV004', 'CA001'),
    ('LLV005', '2026-10-13', 'NV005', 'CA003'),
    ('LLV006', '2026-10-14', 'NV001', 'CA001'),
    ('LLV007', '2026-10-14', 'NV002', 'CA002');

    INSERT INTO KhachHang (maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy) VALUES
    ('KH001', N'Nguyễn Minh Anh', '0912000001', N'Quận 1, TP.HCM', 1, 120),
    ('KH002', N'Trần Quốc Bảo', '0912000002', N'Gò Vấp, TP.HCM', 2, 250),
    ('KH003', N'Lê Ngọc Chi', '0912000003', N'Tân Phú, TP.HCM', 1, 80),
    ('KH004', N'Phạm Gia Hân', '0912000004', N'Thủ Đức, TP.HCM', 3, 560),
    ('KH005', N'Võ Thành Nam', '0912000005', N'Bình Thạnh, TP.HCM', 1, 40),
    ('KH006', N'Đặng Thu Trang', '0912000006', N'Quận 7, TP.HCM', 2, 310),
    ('KH007', N'Bùi Hoàng Long', '0912000007', N'Quận 10, TP.HCM', 1, 0),
    ('KH008', N'Phan Khánh Linh', '0912000008', N'Bình Tân, TP.HCM', 2, 175);

    INSERT INTO ThuCung (maThuCung, tenThuCung, loai, giong, gioiTinh, canNang, ngaySinh, tinhTrangSK, tiemChung, maKH) VALUES
    ('TC001', N'Milo', N'Chó', N'Poodle', N'Đực', 4.5, '2023-04-12', N'Khỏe mạnh', N'Đã tiêm vaccine cơ bản', 'KH001'),
    ('TC002', N'Miu', N'Mèo', N'Anh lông ngắn', N'Cái', 3.8, '2024-01-20', N'Khỏe mạnh', N'Đã tiêm vaccine 3 bệnh', 'KH002'),
    ('TC003', N'Bông', N'Chó', N'Phốc sóc', N'Cái', 3.2, '2022-09-08', N'Cần vệ sinh tai định kỳ', N'Đã tiêm phòng dại', 'KH003'),
    ('TC004', N'Lucky', N'Chó', N'Golden Retriever', N'Đực', 22.0, '2021-06-15', N'Khỏe mạnh', N'Đã tiêm đủ vaccine', 'KH004'),
    ('TC005', N'Sữa', N'Mèo', N'Mèo ta', N'Cái', 3.1, '2023-11-02', N'Khỏe mạnh', N'Đã tiêm vaccine cơ bản', 'KH005'),
    ('TC006', N'Cốm', N'Thỏ', N'Thỏ Hà Lan', N'Đực', 1.7, '2024-05-10', N'Khỏe mạnh', N'Chưa ghi nhận', 'KH006'),
    ('TC007', N'Đốm', N'Chó', N'Chó ta', N'Đực', 12.5, '2020-08-22', N'Cần kiểm tra da', N'Đã tiêm phòng dại', 'KH007'),
    ('TC008', N'Bắp', N'Mèo', N'Munchkin', N'Đực', 3.4, '2024-03-17', N'Khỏe mạnh', N'Đã tiêm vaccine cơ bản', 'KH008');

    INSERT INTO SanPham (maSP, tenSP, giaBan, soLuongTon) VALUES
    ('SP001', N'Hạt chó trưởng thành 2kg', 185000, 45),
    ('SP002', N'Hạt mèo vị cá hồi 1.5kg', 155000, 38),
    ('SP003', N'Pate chó lon 400g', 35000, 80),
    ('SP004', N'Pate mèo lon 400g', 32000, 75),
    ('SP005', N'Vòng cổ thú cưng', 65000, 25),
    ('SP006', N'Dây dắt chó', 95000, 20),
    ('SP007', N'Cát vệ sinh mèo 5L', 89000, 42),
    ('SP008', N'Bình nước thú cưng', 55000, 30),
    ('SP009', N'Đồ chơi bóng cao su', 25000, 60),
    ('SP010', N'Sữa tắm chó mèo 500ml', 115000, 18);

    INSERT INTO MaVach (maVach, maSP) VALUES
    ('8938501000011', 'SP001'), ('8938501000028', 'SP002'),
    ('8938501000035', 'SP003'), ('8938501000042', 'SP004'),
    ('8938501000059', 'SP005'), ('8938501000066', 'SP006'),
    ('8938501000073', 'SP007'), ('8938501000080', 'SP008'),
    ('8938501000097', 'SP009'), ('8938501000103', 'SP010');

    INSERT INTO NhaCungCap (maNCC, tenNCC, soDienThoai, diaChi, email) VALUES
    ('NCC001', N'Công ty Thức ăn PetFood', '02838000001', N'Quận 5, TP.HCM', 'sales@petfood.vn'),
    ('NCC002', N'Nhà phân phối HappyPet', '02838000002', N'Bình Tân, TP.HCM', 'contact@happypet.vn'),
    ('NCC003', N'Công ty phụ kiện thú cưng Việt', '02838000003', N'Tân Bình, TP.HCM', 'info@phukienpet.vn');

    INSERT INTO PhieuNhap (maPN, ngayNhap, maNV, maNCC) VALUES
    ('PN001', '2026-10-01', 'NV001', 'NCC001'),
    ('PN002', '2026-10-04', 'NV004', 'NCC002'),
    ('PN003', '2026-10-08', 'NV001', 'NCC003');

    INSERT INTO ChiTietPhieuNhap (soLuongNhap, donGiaNhap, hanSuDung, maPN, maSP) VALUES
    (30, 135000, '2027-10-01', 'PN001', 'SP001'),
    (25, 112000, '2027-09-01', 'PN001', 'SP002'),
    (100, 22000, '2027-06-01', 'PN001', 'SP003'),
    (80, 20000, '2027-06-01', 'PN001', 'SP004'),
    (20, 42000, NULL, 'PN002', 'SP005'),
    (15, 65000, NULL, 'PN002', 'SP006'),
    (40, 58000, '2028-01-01', 'PN003', 'SP007'),
    (20, 72000, '2028-01-01', 'PN003', 'SP010');

    INSERT INTO LichHen (maLichHen, thoiGianHen, trangThai, maNV, maKH, maThuCung) VALUES
    ('LH001', '2026-10-13T09:00:00', N'Đã xác nhận', 'NV003', 'KH001', 'TC001'),
    ('LH002', '2026-10-13T10:30:00', N'Hoàn thành', 'NV005', 'KH002', 'TC002'),
    ('LH003', '2026-10-13T14:00:00', N'Đã xác nhận', 'NV003', 'KH003', 'TC003'),
    ('LH004', '2026-10-14T09:30:00', N'Chờ xác nhận', 'NV005', 'KH004', 'TC004'),
    ('LH005', '2026-10-14T15:00:00', N'Đã xác nhận', 'NV003', 'KH005', 'TC005'),
    ('LH006', '2026-10-15T10:00:00', N'Chờ xác nhận', 'NV005', 'KH006', 'TC006');

    INSERT INTO DichVu (maDichVu, tenDichVu, donGia, moTa, trangThai, maLichHen) VALUES
    ('DV001', N'Tắm và sấy chó nhỏ', 120000, N'Tắm, sấy và chải lông cơ bản', N'Hoàn thành', 'LH001'),
    ('DV002', N'Vệ sinh tai mèo', 80000, N'Vệ sinh tai và kiểm tra sơ bộ', N'Hoàn thành', 'LH002'),
    ('DV003', N'Cắt tỉa lông', 180000, N'Cắt tỉa theo yêu cầu khách hàng', N'Đã đặt', 'LH003'),
    ('DV004', N'Khám sức khỏe tổng quát', 150000, N'Kiểm tra sức khỏe tổng quát', N'Đã đặt', 'LH004'),
    ('DV005', N'Cắt móng thú cưng', 50000, N'Cắt móng và vệ sinh bàn chân', N'Đã đặt', 'LH005'),
    ('DV006', N'Tắm mèo', 140000, N'Tắm và sấy dành cho mèo', N'Đã đặt', 'LH006');

    INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, phanTramGiam, dieuKienApDung, ngayBatDau, ngayKetThuc) VALUES
    ('KM000', N'Không áp dụng khuyến mãi', 0, N'Áp dụng khi không có chương trình giảm giá', '2025-01-01', '2030-12-31'),
    ('KM001', N'Ưu đãi khách hàng mới', 10, N'Giảm 10% cho hóa đơn từ 100000 đồng', '2026-01-01', '2026-12-31'),
    ('KM002', N'Tháng yêu thú cưng', 15, N'Giảm 15% dịch vụ chăm sóc', '2026-10-01', '2026-10-31'),
    ('KM003', N'Ưu đãi thành viên', 5, N'Giảm 5% cho khách hàng thành viên', '2026-01-01', '2026-12-31');

    INSERT INTO PhuongThucThanhToan (maPTTT, tenPTTT) VALUES
    ('PT001', N'Tiền mặt'),
    ('PT002', N'Chuyển khoản'),
    ('PT003', N'Ví điện tử'),
    ('PT004', N'Thẻ ngân hàng');

    INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, maKhuyenMai, maPTTT) VALUES
    ('HD001', '2026-10-02', 'NV002', 'KH001', 'KM001', 'PT001'),
    ('HD002', '2026-10-03', 'NV002', 'KH002', 'KM000', 'PT002'),
    ('HD003', '2026-10-05', 'NV004', 'KH003', 'KM003', 'PT001'),
    ('HD004', '2026-10-07', 'NV002', 'KH004', 'KM002', 'PT003'),
    ('HD005', '2026-10-09', 'NV004', 'KH005', 'KM000', 'PT001'),
    ('HD006', '2026-10-10', 'NV002', 'KH006', 'KM001', 'PT004');

    /*
      Schema hiện tại đặt PRIMARY KEY chỉ trên maHoaDon trong ChiTietHoaDon,
      nên mỗi hóa đơn chỉ có thể có một dòng chi tiết. Dữ liệu bên dưới tuân thủ
      đúng cấu trúc đó. Cột maSP không có FK trong schema đã gửi.
    */
    INSERT INTO ChiTietHoaDon (maHoaDon, maDichVu, maSP, soLuong, donGia, tienKhachDua) VALUES
    ('HD001', NULL, 'SP001', 1, 185000, 200000),
    ('HD002', 'DV002', NULL, 1, 80000, 80000),
    ('HD003', NULL, 'SP007', 2, 89000, 180000),
    ('HD004', 'DV004', NULL, 1, 150000, 150000),
    ('HD005', NULL, 'SP003', 3, 35000, 110000),
    ('HD006', 'DV005', NULL, 1, 50000, 50000);

    INSERT INTO KhuyenMaiSanPham (maKhuyenMaiMaSanPham, mucGiam, maSP, maKhuyenMai, maHoaDon) VALUES
    ('KMSP001', 18500, 'SP001', 'KM001', 'HD001'),
    ('KMSP002', 0, 'SP007', 'KM000', 'HD003'),
    ('KMSP003', 5250, 'SP003', 'KM003', 'HD005');

    INSERT INTO TichDiem (maTichDiem, diemTichLuy, ngayCapNhat, maKH, maHoaDon) VALUES
    ('TD001', 17, '2026-10-02', 'KH001', 'HD001'),
    ('TD002', 8, '2026-10-03', 'KH002', 'HD002'),
    ('TD003', 17, '2026-10-05', 'KH003', 'HD003'),
    ('TD004', 15, '2026-10-07', 'KH004', 'HD004'),
    ('TD005', 10, '2026-10-09', 'KH005', 'HD005'),
    ('TD006', 5, '2026-10-10', 'KH006', 'HD006');

    COMMIT TRANSACTION;
    PRINT N'Đã thêm dữ liệu mẫu PETPLUS thành công.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

/* Kiểm tra số bản ghi của 19 bảng */
SELECT N'TaiKhoan' AS TenBang, COUNT(*) AS SoBanGhi FROM TaiKhoan
UNION ALL SELECT N'NhanVien', COUNT(*) FROM NhanVien
UNION ALL SELECT N'CaLamViec', COUNT(*) FROM CaLamViec
UNION ALL SELECT N'LichLamViec', COUNT(*) FROM LichLamViec
UNION ALL SELECT N'KhachHang', COUNT(*) FROM KhachHang
UNION ALL SELECT N'ThuCung', COUNT(*) FROM ThuCung
UNION ALL SELECT N'SanPham', COUNT(*) FROM SanPham
UNION ALL SELECT N'MaVach', COUNT(*) FROM MaVach
UNION ALL SELECT N'NhaCungCap', COUNT(*) FROM NhaCungCap
UNION ALL SELECT N'PhieuNhap', COUNT(*) FROM PhieuNhap
UNION ALL SELECT N'ChiTietPhieuNhap', COUNT(*) FROM ChiTietPhieuNhap
UNION ALL SELECT N'LichHen', COUNT(*) FROM LichHen
UNION ALL SELECT N'DichVu', COUNT(*) FROM DichVu
UNION ALL SELECT N'KhuyenMai', COUNT(*) FROM KhuyenMai
UNION ALL SELECT N'PhuongThucThanhToan', COUNT(*) FROM PhuongThucThanhToan
UNION ALL SELECT N'HoaDon', COUNT(*) FROM HoaDon
UNION ALL SELECT N'ChiTietHoaDon', COUNT(*) FROM ChiTietHoaDon
UNION ALL SELECT N'KhuyenMaiSanPham', COUNT(*) FROM KhuyenMaiSanPham
UNION ALL SELECT N'TichDiem', COUNT(*) FROM TichDiem;
GO


USE PETPLUS;
GO

