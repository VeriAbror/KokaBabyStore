# KokaBabyStore - Android Application

Aplikasi Android **KokaBabyStore** adalah sistem manajemen toko dan e-commerce produk bayi yang dibangun menggunakan arsitektur modern berbasis Kotlin, didukung oleh Firebase Firestore untuk database cloud serta Supabase Storage untuk penyimpanan media gambar.

## 🚀 Fitur Utama & Arsitektur Repository

Aplikasi ini membagi fungsionalitas datanya ke dalam beberapa lapisan repository utama:

- **Admin Management (`AdminRepository`)**:
  - Mengelola aliran data produk secara real-time.
  - Operasi CRUD produk (Simpan/Update dan Hapus produk beserta pembersihan gambar terkait di Supabase Storage).
  - Unggah gambar produk ke penyimpanan Supabase (`uploadProductImage`).
  - Pengelolaan pesanan masuk, pembaruan status pesanan (`updateOrderStatus`), serta sinkronisasi stok otomatis menggunakan Firestore Transactions (`updateProductStock`).

- **Customer E-Commerce (`CustomerRepository`)**:
  - Pengelolaan keranjang belanja pengguna (Tambah item, ubah jumlah, hapus item, dan mengosongkan keranjang).
  - Pembuatan pesanan baru (`placeOrder`) dan pemantauan riwayat pesanan pelanggan secara real-time.

- **Product Catalog (`ProductRepository`)**:
  - Penyedia aliran data daftar produk untuk ditampilkan ke pengguna.

## 🛠 Teknologi yang Digunakan

* **Bahasa**: Kotlin
* **Asinkronus**: Kotlin Coroutines & Flows (`Flow`, `callbackFlow`)
* **Backend / Database**: Firebase Firestore (NoSQL Cloud Database)
* **Storage**: Supabase Storage (untuk pengelolaan file gambar produk)

---

## 📥 Cara Instalasi & Menjalankan Proyek

Ikuti langkah-langkah di bawah ini untuk mengonfigurasi dan menjalankan proyek di komputer lokal:

### 1. Clone Repository
Buka terminal/Git Bash lalu jalankan perintah berikut:
git clone https://github.com/VeriAbror/KokaBabyStore.git

### 2. Buka Proyek di Android Studio
- Buka aplikasi **Android Studio**.
- Pilih menu **File > Open**, lalu arahkan ke folder tempat kamu melakukan clone (`KokaBabyStore`).
- Tunggu beberapa saat hingga Android Studio selesai memuat proyek.

### 3. Konfigurasi Kredensial & Backend (Penting)
Karena proyek ini terhubung ke layanan cloud, pastikan file konfigurasi berikut sudah terpasang dengan benar di dalam direktori proyek lokalmu:
- **Firebase**: Pastikan file `google-services.json` sudah berada di dalam folder `app/`.
- **Supabase & Service Account**: Pastikan file konfigurasi kredensial tambahan seperti `service_account.json` (atau file local properties lainnya yang dibutuhkan) sudah diletakkan sesuai struktur direktori lokal proyek (file ini diabaikan oleh Git demi keamanan).

### 4. Sinkronisasi Gradle & Build
- Jika muncul notifikasi *Gradle Sync*, klik **Sync Now**.
- Tunggu proses download dependencies dan sinkronisasi selesai.

### 5. Jalankan Aplikasi
- Sambungkan perangkat Android fisik (dengan USB Debugging aktif) atau jalankan **Android Emulator**.
- Klik tombol **Run** (ikon panah hijau / `Shift + F10`) di Android Studio.
