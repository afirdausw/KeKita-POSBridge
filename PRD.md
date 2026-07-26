# PRD.md — Product Requirements Document
# KeKita POS Bridge Android

**Versi:** 1.0
**Terakhir diperbarui:** Juli 2026
**Status:** In Development

---

## 1. Ringkasan Produk

**KeKita POS Bridge** adalah aplikasi Android native yang berfungsi sebagai
**jembatan POS (Point of Sale) offline-first** untuk jaringan outlet klien KeKita.

Satu aplikasi dapat melayani **berbagai outlet berbeda** hanya dengan mengganti
kode akses — setiap outlet punya server API-nya sendiri, logo, nama, dan
konfigurasi struk yang unik.

### Tagline
> *"KeKita POS Bridge — Satu Aplikasi, Banyak Outlet"*

---

## 2. Masalah yang Dipecahkan

| Masalah | Solusi |
|---|---|
| Koneksi internet tidak stabil di lokasi outlet | Transaksi tersimpan lokal dulu, sync saat online |
| Banyak outlet dengan server berbeda-beda | Dynamic API domain dari kode akses |
| Kasir berganti-ganti di satu device | 2-step login: outlet config permanen, sesi kasir sementara |
| Struk berbeda tiap outlet | Header/footer struk dikonfigurasi dari server pusat |

---

## 3. Target Pengguna

| Peran | Deskripsi |
|---|---|
| **Administrator** | Memberikan kode akses outlet, mengelola user di server |
| **Kasir** | Login dengan username/password, melakukan transaksi |
| **Waiter** | (opsional) Mencatat pesanan meja |

---

## 4. Alur Penggunaan

### 4.1 Setup Awal (Sekali)
```
Install Aplikasi
    ↓
Masukkan Kode Akses Outlet (dari administrator)
    ↓
Aplikasi fetch konfigurasi dari Central API
    ↓
Konfigurasi tersimpan permanen di device
```

### 4.2 Login Harian
```
Buka Aplikasi (outlet sudah terkonfigurasi)
    ↓
Login dengan username + password kasir
    ↓
Masuk ke Home / POS
```

### 4.3 Proses Transaksi
```
Pilih Produk → Tambah ke Keranjang
    ↓
Review Order
    ↓
Proses Pembayaran
    ↓
Cetak Struk (Bluetooth Printer)
    ↓
Transaksi tersimpan lokal (sync background)
```

### 4.4 Logout
```
Logout Akun   → hapus sesi kasir, outlet tetap → kembali ke Login
Logout Sistem → hapus sesi + outlet config → kembali ke Kode Akses
```

---

## 5. Fitur

### 5.1 Sudah Diimplementasi ✅

#### Auth — 2 Step Login
- **Step 1: Kode Akses Outlet**
  - Input kode akses dari administrator
  - Fetch konfigurasi dari Central API (`GET verify.php?kode=XXXX`)
  - Simpan: nama outlet, API domain, logo, header/footer struk
  - Konfigurasi persisten di SharedPreferences

- **Step 2: Login Kasir**
  - Username + password kasir
  - Auth ke outlet API (`POST api/login_user` — form-urlencoded)
  - Simpan sesi: nama, username, peran
  - Sesi terpisah dari konfigurasi outlet

#### Home Screen
- Info sesi user aktif (nama, username, peran)
- Info konfigurasi outlet (nama, API domain, header/footer struk)
- Logout Akun (sesi saja) dengan konfirmasi dialog
- Logout Sistem (sesi + outlet config) dengan konfirmasi dialog

#### Infrastructure
- Room Database dengan sync_status tracking
- WorkManager untuk background sync
- Retrofit dengan dynamic base URL (per outlet)
- Network Security Config (HTTP lokal diizinkan untuk dev)
- Light/Dark theme support dengan brand color KeKita
- Navigasi 3-state: AccessCode → Login → Home

---

### 5.2 Akan Diimplementasi 🔲

#### POS — Transaksi
- [ ] Daftar produk dari Room DB (sync dari outlet API)
- [ ] Keranjang belanja (cart management)
- [ ] Pilih metode pembayaran (cash / non-cash)
- [ ] Hitung kembalian
- [ ] Simpan transaksi ke Room DB (offline-first)
- [ ] Sync transaksi ke outlet API via WorkManager

#### Produk & Kategori
- [ ] Daftar produk dengan kategori
- [ ] Filter dan search produk
- [ ] Gambar produk (load dari URL outlet)
- [ ] Stok realtime (ambil dari Room, sync dari API)

#### Struk / Receipt
- [ ] Preview struk di layar
- [ ] Cetak ke Bluetooth thermal printer
- [ ] Format struk: header outlet, item, total, footer
- [ ] Logo outlet pada struk (dari `settings.logo_print`)

#### Laporan
- [ ] Laporan penjualan harian (dari Room DB)
- [ ] Export ke PDF / share
- [ ] Rekap per kasir

#### Pengaturan
- [ ] Info versi aplikasi
- [ ] Status sinkronisasi (berapa transaksi pending)
- [ ] Force sync manual
- [ ] Ganti outlet (Logout Sistem)

---

## 6. Spesifikasi Teknis

### Stack Teknologi

| Kategori | Teknologi |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Repository Pattern |
| Local Storage | Room Database |
| Background Job | WorkManager |
| Network | Retrofit 2 + OkHttp + Gson |
| Async | Kotlin Coroutines + StateFlow |
| Navigation | Navigation Compose |
| Config Storage | SharedPreferences (OutletManager + SessionManager) |

### Struktur Package

```
devyana.kekita.posbridge
├── data/
│   ├── local/
│   │   ├── dao/         (Room DAO interfaces)
│   │   ├── database/    (AppDatabase)
│   │   └── entity/      (Room entities)
│   ├── remote/
│   │   ├── api/         (Retrofit interfaces)
│   │   ├── model/       (Request/Response data classes)
│   │   └── network/     (RetrofitClient)
│   └── repository/      (Repository classes)
├── ui/
│   ├── accesscode/      (Step 1: kode akses)
│   ├── login/           (Step 2: login kasir)
│   ├── home/            (Home screen)
│   ├── navigation/      (AppNavHost, Screen routes)
│   └── theme/           (Color, Theme, Typography)
├── utils/
│   ├── Constants.kt
│   ├── OutletManager.kt
│   └── SessionManager.kt
└── worker/              (WorkManager sync workers)
```

### API Integration

#### Central API (Tetap)
```
Base URL : Constants.CENTRAL_API_URL
Endpoint : GET verify.php?kode={KODE}
Format   : application/json
```

**Response sukses:**
```json
{
  "status": "success",
  "client": "Nama Outlet",
  "settings": {
    "url": "https://outlet.domain.com/api/",
    "logo": "https://...",
    "logo_print": "https://...",
    "header_text": "Alamat\nTelp",
    "footer_text": "Terima Kasih"
  }
}
```

#### Outlet API (Dinamis per outlet)
```
Base URL : OutletManager.getApiDomain()
```

| Endpoint | Method | Format | Keterangan |
|---|---|---|---|
| `api/login_user` | POST | form-urlencoded | Login kasir |
| `api/produk` | GET | - | Daftar produk (rencana) |
| `api/transaksi` | POST | JSON | Simpan transaksi (rencana) |

**Login request fields:** `username`, `password`

**Login response:**
```json
{
  "status": true,
  "data": {
    "isLogin": true,
    "user": {
      "id": "1",
      "name": "Nama Lengkap",
      "username": "kasir01",
      "level": "kasir",
      "photo": "url_foto"
    }
  }
}
```

### Database Schema (Room)

#### Tabel `products` (sudah ada)
| Kolom | Tipe | Keterangan |
|---|---|---|
| id | Long | PK autoGenerate |
| uuid | String | Unique, dari server |
| name | String | Nama produk |
| description | String | Deskripsi |
| price | Double | Harga |
| stock | Int | Stok lokal |
| category | String | Kategori |
| image_url | String | URL gambar |
| is_active | Boolean | Aktif/nonaktif |
| is_deleted | Boolean | Soft delete |
| sync_status | SyncStatus | PENDING/SYNCED/FAILED |
| created_at | Long | Timestamp ms |
| updated_at | Long | Timestamp ms |

---

## 7. Desain & Branding

### Brand Identity
- **Nama:** KeKita POS Bridge
- **Highlight:** **KeKita** (biru bold) + " POS Bridge" (regular)
- **Logo:** `ic_launcher_foreground` dengan rounded corners 20dp

### Palet Warna

| Token | Light | Dark | Keterangan |
|---|---|---|---|
| Primary | `#1D4ED8` | `#60A5FA` | Biru brand utama |
| Background | `#F8F8F8` | `#0F172A` | Latar halaman |
| Surface | `#FFFFFF` | `#1E293B` | Card, input |
| onSurfaceVariant | `#6B7280` | `#94A3B8` | Label, subtitle |
| Error | `#DC2626` | `#F87171` | Error, logout |

### Aturan UI
- Tidak ada placeholder/hint pada input — hanya `label`
- Semua warna via `MaterialTheme.colorScheme.*`
- `dynamicColor = false` (warna brand tidak berubah di Android 12+)
- Dark mode: shadow logo dimatikan (`elevation = 0.dp`)

---

## 8. Non-Functional Requirements

| Aspek | Requirement |
|---|---|
| **Offline** | Semua fitur utama POS berjalan tanpa internet |
| **Performance** | UI tetap 60fps, tidak ada ANR |
| **Data Safety** | Tidak ada transaksi yang hilang |
| **Security** | Tidak ada credential yang di-log / di-expose |
| **Compatibility** | Android 10+ (API 29), tablet-first |
| **Sync** | Retry otomatis, exponential backoff |

---

## 9. Batasan Saat Ini

- Tidak ada role-based access control di sisi Android (bergantung server)
- Satu device = satu outlet (multi-outlet butuh Logout Sistem)
- Tidak ada push notification
- Tidak ada fitur split bill

---

*Dokumen ini akan diperbarui seiring perkembangan fitur.*
