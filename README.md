# MiniKasir – Mini Android POS & Sync App

Aplikasi **Android Native (Java)** berorientasi transaksi kasir (Point of Sale) sederhana dengan kemampuan **Offline-First Storage**, **FIFO Sync Queue**, dan **Background WorkManager Synchronization** menuju Mock API.

Proyek ini dibangun sebagai bagian dari *Practical Assignment (Task 4)* seleksi posisi **Lead Mobile-Desktop** di **Kasir Pintar**.

---

## 🏛️ Arsitektur Aplikasi

Aplikasi dirancang menggunakan prinsip **Clean Architecture** dipadukan dengan pola **MVVM (Model-View-ViewModel)** dan **Repository Pattern**:

```
+----------------------------------------------------------------------------+
|                          PRESENTATION LAYER (UI)                           |
|   ProductListActivity   |   TransactionActivity   |   SyncStatusActivity   |
|   ProductViewModel      |   TransactionViewModel  |   SyncStatusViewModel  |
+----------------------------------------------------------------------------+
                                      │
                                      ▼
+----------------------------------------------------------------------------+
|                            DOMAIN LAYER (UseCases)                         |
|         CreateTransactionUseCase      │      SyncTransactionsUseCase       |
+----------------------------------------------------------------------------+
                                      │
                                      ▼
+----------------------------------------------------------------------------+
|                             DATA LAYER                                     |
|     ProductRepository   │   TransactionRepository   │   SyncRepository     |
|   ──────────────────────┴───────────────────────────┴───────────────────   |
|     LOCAL (Room Database / SQLite)      │    REMOTE (Retrofit + OkHttp)    |
|     - ProductDao                        │    - KasirApiService             |
|     - TransactionDao                    │    - ApiClient                   |
|     - SyncQueueDao                      │    - Mock Models / Payloads      |
+----------------------------------------------------------------------------+
                                      ▲
                                      │
+----------------------------------------------------------------------------+
|                     BACKGROUND WORKER (WorkManager)                        |
|                                SyncWorker                                  |
|     - Periodic Sync (setiap 15 menit jika ada koneksi internet)            |
|     - One-Time Sync Trigger (tombol "Sync Sekarang" / Swipe-to-refresh)    |
+----------------------------------------------------------------------------+
```

### Flow Transaksi & Sinkronisasi
```
[User / Kasir] 
     │
     ▼ (Pilih Produk & Checkout)
[CreateTransactionUseCase] 
     │
     ▼ (Atomic ACID write)
[Room Local Database] ──► Tabel `transactions` (Status: PENDING)
     │                ──► Tabel `transaction_items`
     │                ──► Tabel `sync_queue` (Status: PENDING)
     │
     ▼ (Background trigger)
[SyncWorker / WorkManager] (Requires: Network Connected)
     │
     ▼ (POST /api/transactions/sync)
[Mock Server / Retrofit]
     │
     ├─► [SUCCESS] ──► Update Local DB (Status: SYNCED, set synced_at)
     └─► [FAIL]    ──► Increment Retry Count (Max 3x, fallback: FAILED)
```

---

## 🛠️ Tech Stack & Dependencies

| Komponen | Library / Tool | Versi |
|---|---|---|
| **Bahasa Pemrograman** | Java 17 | 17 |
| **Minimum SDK / Target SDK** | minSdk 24 (Android 7.0) / targetSdk 34 (Android 14) | 24 / 34 |
| **Architecture / UI** | AndroidX ViewModel, LiveData, ViewBinding | 2.8.3 |
| **Material Components** | Material Design 3 (`com.google.android.material`) | 1.12.0 |
| **Local Database** | AndroidX Room ORM (SQLite) | 2.6.1 |
| **Background Processing** | AndroidX WorkManager | 2.9.0 |
| **HTTP / REST Client** | Retrofit 2 + OkHttp 4 + Gson Converter | 2.11.0 / 4.12.0 |
| **JSON Serialization** | Google Gson | 2.11.0 |
| **Unit Testing** | JUnit 4, Mockito, AndroidX Core Testing | 4.13.2 / 5.12.0 |
| **Instrumented Testing** | AndroidX JUnit Runner, Espresso, Room Test Helper | 1.2.1 / 3.6.1 |

---

## ✨ Fitur-Fitur Utama

1. **Katalog Produk (CRUD)**:
   - Menampilkan daftar produk dengan harga (format Rupiah) dan stok.
   - Pencarian produk real-time via `SearchView` toolbar.
   - Tambah produk baru & edit/hapus produk yang sudah ada.
   - Pre-population 5 item produk kuliner default saat aplikasi baru pertama kali dibuka.

2. **Transaksi Kasir (POS Cart & Checkout)**:
   - Keranjang belanja interaktif dengan tombol penyesuaian kuantitas (+ / - / hapus).
   - Rak *quick-pick* produk di bagian bawah untuk mempercepat penambahan item.
   - Kalkulasi otomatis subtotal per item dan total pembayaran secara instan.
   - Dialog konfirmasi checkout dengan kolom input catatan kasir opsional.

3. **Offline-First Local Persistence**:
   - Seluruh transaksi dan item langsung tersimpan di database lokal Room.
   - Setiap transaksi otomatis mendapatkan kode unik (`TRX-<timestamp>-<uuid>`) dan status awal `PENDING`.
   - Transaksi dapat dilakukan tanpa bergantung pada koneksi internet.

4. **Sync Queue & WorkManager**:
   - Outbox pattern: data payload transaksi diantrekan di tabel `sync_queue`.
   - `SyncWorker` terjadwal otomatis secara periodik (15 menit) dengan constraint `NetworkType.CONNECTED`.
   - Tombol manual "Sync Sekarang" dan *swipe-to-refresh* untuk trigger instan.
   - Penanganan retry hingga maksimal 3 kali jika terjadi kegagalan jaringan sebelum ditandai `FAILED`.

5. **Sync Status Dashboard**:
   - Tampilan visual kartu indikator jumlah transaksi `PENDING`, `SYNCED`, dan `FAILED`.
   - Detail antrean sinkronisasi beserta log error jika ada.

---

## 🚀 Cara Menjalankan Aplikasi

### Prasyarat
- **Android Studio** Hedgehog (2023.1.1) atau versi lebih baru.
- **JDK 17** terpasang dan disetel sebagai Gradle JDK.
- Perangkat Android fisik atau Emulator dengan Android OS versi 7.0+ (API level 24+).

### Langkah Menjalankan
1. Clone / buka folder proyek:
   ```bash
   cd MiniKasir
   ```
2. Buka folder `MiniKasir` di Android Studio (*File → Open...*).
3. Tunggu hingga proses **Gradle Sync** selesai secara otomatis.
4. Pilih target perangkat/emulator, lalu klik tombol **Run 'app'** (`Shift + F10`).

> **Catatan mengenai Mock API**:  
> Secara default, endpoint diarahkan ke `http://localhost:8080/`. Dalam mode offline, seluruh fitur transaksi dan antrean lokal tetap berfungsi penuh. Untuk menguji sinkronisasi sukses ke server sungguhan atau mock server lokal, URL API dapat disesuaikan pada `Constants.DEFAULT_BASE_URL` atau via `ApiClient.setBaseUrl(...)`.

---

## 🧪 Menjalankan Pengujian (Testing)

### Unit Tests
Menjalankan pengujian logika UseCase, Repository, dan Formatting Utils:
```bash
./gradlew testDebugUnitTest
```

### Instrumented Tests (Perangkat / Emulator Aktif)
Menjalankan pengujian Room Database in-memory DAO dan WorkManager:
```bash
./gradlew connectedDebugAndroidTest
```

---

## ⚠️ Known Limitations & Rekomendasi Pengembangan Lanjut

1. **Mock Server Endpoint**: Sinkronisasi saat ini mengasumsikan endpoint REST mock standar (`/api/transactions/sync`). Di lingkungan produksi, dapat ditambahkan kontrak auth token (OAuth2 / JWT Bearer).
2. **Conflict Resolution Strategy**: Saat ini menggunakan strategi *Client-Wins* berbasis *Idempotent Transaction Code*. Untuk arsitektur multi-kasir di toko yang sama, dapat ditambahkan *Server-Assigned Version Vector* untuk sinkronisasi mutasi stok produk.
3. **Receipt Printing**: Protokol cetak struk via Bluetooth thermal printer ESC/POS (58mm/80mm) dapat diintegrasikan langsung pada callback sukses UseCase `CreateTransactionUseCase`.

---

## 🤖 AI-Assisted SDLC Disclosure

Sesuai panduan teknis Kasir Pintar yang memperbolehkan dan mendorong pemanfaatan AI dalam siklus pengembangan:
* **Tool AI yang digunakan**: Google Antigravity / Gemini Assistant.
* **Pemanfaatan**:
  - Scaffolding struktur direktori MVVM dan boilerplate Room Entities/DAOs.
  - Pembuatan template Unit Test awal (Mockito stubbing & JUnit assertions).
  - Penyusunan draft dokumentasi arsitektur.
* **Review & Koreksi Manual oleh Engineer**:
  - Mengubah `TransactionDao` menjadi abstract class dengan anotasi `@Transaction` untuk memastikan persistensi header transaksi dan line items berjalan dalam 1 blok ACID atomik.
  - Menetapkan relasi `ForeignKey.RESTRICT` dan `CASCADE` pada entitas Room agar riwayat transaksi masa lalu tidak korup saat master produk diubah/dihapus.
  - Menambahkan validasi integritas data bisnis (kuantitas `<= 0`, pencegahan overflow kalkulasi `long`, dan format kode transaksi unik).
  - Mengonfigurasi WorkManager lifecycle constraints (`NetworkType.CONNECTED`) agar hemat baterai.
