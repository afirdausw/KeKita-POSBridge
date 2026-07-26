# RULES.md — KeKita POS Bridge Android

> Dokumen ini adalah **aturan wajib** yang harus diikuti oleh semua kontributor
> dan AI assistant saat mengerjakan project ini.
> Semua aturan berlaku kecuali ada instruksi eksplisit sebaliknya.

---

## 1. Prinsip Utama

```
Reliability > Features
Data Integrity > Speed
Offline Capability > Realtime Capability
```

Aplikasi **wajib tetap berfungsi** meski:
- Internet tidak tersedia
- Local network down
- Central server mati
- Outlet server mati

---

## 2. Arsitektur

### Pola Wajib: MVVM + Repository

```
Screen (Composable)
  ↓
ViewModel (StateFlow)
  ↓
Repository
  ↙         ↘
Room DB    Retrofit API
  ↓
WorkManager (Sync)
```

### Aturan Tiap Layer

| Layer | Tanggung Jawab | Dilarang |
|---|---|---|
| **Screen** | Tampilkan UI, kirim event ke ViewModel | Logic bisnis, akses DB langsung |
| **ViewModel** | Kelola state UI, panggil Repository | Akses DB/API langsung |
| **Repository** | Koordinasi Room + API | Logic UI, akses SharedPreferences |
| **Room DAO** | Query database saja | Logic bisnis apapun |
| **API Service** | Definisi endpoint saja | Parsing manual, logic |

---

## 3. Aturan Database (Room)

### Setiap tabel Room WAJIB memiliki kolom berikut:

```kotlin
val id: Long           // PrimaryKey autoGenerate
val uuid: String       // UUID unik untuk sync (unique index)
val syncStatus: SyncStatus  // PENDING | SYNCED | FAILED
val createdAt: Long    // System.currentTimeMillis()
val updatedAt: Long    // System.currentTimeMillis()
```

### SyncStatus Enum

```kotlin
enum class SyncStatus { PENDING, SYNCED, FAILED }
```

### Aturan Delete
- **Gunakan soft delete**: kolom `isDeleted: Boolean = false`
- Jangan hapus record yang belum `SYNCED`
- Jangan pernah hapus record yang `FAILED` sebelum berhasil sync

### Index Wajib
```kotlin
Index(value = ["uuid"], unique = true)
Index(value = ["sync_status"])
Index(value = ["is_deleted"])
```

---

## 4. Alur Transaksi (Wajib Offline-First)

```
User Action
    ↓
Validasi Input (ViewModel)
    ↓
Simpan ke Room DB (syncStatus = PENDING)
    ↓
Tampilkan sukses ke user SEGERA
    ↓
Enqueue WorkManager job
    ↓
[Background] Kirim ke API
    ↓
Update syncStatus = SYNCED / FAILED
```

**User tidak boleh menunggu response API untuk menyelesaikan transaksi.**

---

## 5. Aturan Sinkronisasi

- Retry **tanpa batas** menggunakan `WorkManager` dengan constraints network
- Gunakan exponential backoff
- Jangan pernah hapus record `PENDING` atau `FAILED`
- Jangan overwrite data lokal dengan data server secara blind
- Log setiap kegagalan sync (minimal ke Logcat dengan tag `SYNC`)
- Constraint: hanya sync saat ada koneksi internet (`NetworkType.CONNECTED`)

---

## 6. Aturan Kode

### Naming Convention

| Jenis | Konvensi | Contoh |
|---|---|---|
| Class / Object | PascalCase | `ProductRepository` |
| Function / Variable | camelCase | `getActiveProducts()` |
| Constant | SCREAMING_SNAKE | `CONNECT_TIMEOUT_SECONDS` |
| Composable | PascalCase | `HomeScreen()` |
| ViewModel state | camelCase + UiState suffix | `LoginUiState` |
| File package | lowercase | `devyana.kekita.posbridge` |

### Aturan Wajib Kode

- Tidak ada **magic number** — semua konstanta di `Constants.kt`
- Tidak ada **hardcoded URL** — URL outlet dari `OutletManager`, central dari `Constants.CENTRAL_API_URL`
- Tidak ada **duplikasi logic** — buat fungsi / extension jika sama
- Semua exception **harus ditangkap eksplisit** — minimal `IOException` dan `Exception`
- Semua network call menggunakan `suspend fun` + `try-catch`
- Tidak ada blocking call di Main Thread

### Aturan SharedPreferences

| Data | Manager | Catatan |
|---|---|---|
| Konfigurasi outlet | `OutletManager` | Permanen, hanya reset oleh "Logout Sistem" |
| Sesi user (kasir) | `SessionManager` | Dihapus saat "Logout Akun" |

Jangan menyimpan data outlet di `SessionManager` atau sebaliknya.

---

## 7. Aturan UI / Compose

### Warna
- Semua warna **harus** menggunakan `MaterialTheme.colorScheme.*`
- Tidak ada hardcoded hex di composable — definisikan di `Color.kt` dahulu, lalu map ke `Theme.kt`
- `dynamicColor = false` — warna brand KeKita selalu konsisten

### Input Field
- Tidak ada `placeholder` / `hint` — hanya gunakan `label`
- Semua field punya `label` yang deskriptif

### Logo
- Gunakan `R.drawable.ic_launcher_foreground` (bukan `mipmap`)
- Selalu bungkus dalam `Box` ter-clip untuk memotong transparent padding adaptive icon

### Dark/Light Theme
- Semua screen **wajib** responsif terhadap sistem dark/light
- Gunakan `val colorScheme = MaterialTheme.colorScheme` di awal composable
- `isSystemInDarkTheme()` hanya untuk keputusan non-colorScheme (misal shadow elevation)

---

## 8. Aturan Auth (2-Step)

### Step 1 — Kode Akses Outlet
- Endpoint: `GET {CENTRAL_API_URL}verify.php?kode=XXXX`
- Berhasil jika `response.status == "success"`
- Simpan semua `settings` dari response ke `OutletManager`
- Kode akses selalu di-uppercase sebelum dikirim

### Step 2 — Login Kasir
- Endpoint: `POST {outlet_api_domain}api/login_user`
- Format: `@FormUrlEncoded` (`application/x-www-form-urlencoded`)
- Field: `username`, `password`
- Berhasil jika `response.status == true` (Boolean)
- Simpan `username`, `nama_lengkap`, `peran` ke `SessionManager`

### Navigasi Auth
```
OutletManager.isOutletConfigured() == false → AccessCodeScreen
SessionManager.isLoggedIn() == false        → LoginScreen
Keduanya true                               → HomeScreen
```

---

## 9. Aturan Network

### Cleartext (HTTP)
- Diizinkan **hanya** untuk IP lokal (development) via `network_security_config.xml`
- Produksi wajib HTTPS

### Retrofit
- Central API: satu instance static untuk `CENTRAL_API_URL`
- Outlet API: instance dinamis berdasarkan `OutletManager.getApiDomain()`
- Timeout: `CONNECT_TIMEOUT_SECONDS = 30`, `READ_TIMEOUT_SECONDS = 30`

---

## 10. Target Device

- Platform: Android Native (Kotlin + Jetpack Compose)
- Minimum SDK: Android 10 (API 29)
- Target: Android Tablet, minimal 4GB RAM
- Orientasi: mendukung landscape (POS biasanya tablet landscape)

---

## 11. Dilarang Keras

1. Menyimpan data ke API **sebelum** menyimpan ke Room
2. Menghapus record `PENDING` atau `FAILED` dari Room
3. Hardcode URL outlet di kode
4. Akses SharedPreferences langsung dari Composable / ViewModel
5. Blocking call (`Thread.sleep`, `runBlocking`) di Main Thread
6. Menghapus kode yang tidak diminta
7. Mengubah nilai variabel / konstanta tanpa instruksi eksplisit

---

*Terakhir diperbarui: Juli 2026*
