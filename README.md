# KokaBabyStore - Android Application

Aplikasi Android **KokaBabyStore** adalah sistem manajemen toko dan e-commerce produk bayi yang dibangun menggunakan arsitektur modern berbasis Kotlin, didukung oleh Firebase Firestore untuk database cloud serta Supabase Storage untuk penyimpanan media gambar.

## 🚀 Fitur Utama & Arsitektur Repository

Aplikasi ini membagi fungsionalitas datanya ke dalam beberapa lapisan repository utama:

- **Admin Management (`AdminRepository`)**:
  - Mengelola aliran data produk secara real-time[cite: 9, 10].
  - Operasi CRUD produk (Simpan/Update dan Hapus produk beserta pembersihan gambar terkait di Supabase Storage)[cite: 9, 10].
  - Unggah gambar produk ke penyimpanan Supabase (`uploadProductImage`)[cite: 9, 10].
  - Pengelolaan pesanan masuk, pembaruan status pesanan (`updateOrderStatus`), serta sinkronisasi stok otomatis menggunakan Firestore Transactions (`updateProductStock`)[cite: 9, 10].

- **Customer E-Commerce (`CustomerRepository`)**:
  - Pengelolaan keranjang belanja pengguna (Tambah item, ubah jumlah, hapus item, dan mengosongkan keranjang)[cite: 11, 12].
  - Pembuatan pesanan baru (`placeOrder`) dan pemantauan riwayat pesanan pelanggan secara *real-time*[cite: 11, 12].

- **Product Catalog (`ProductRepository`)**:
  - Penyedia aliran data daftar produk untuk ditampilkan ke pengguna[cite: 13, 14].

## 🛠 Teknologi yang Digunakan

* **Bahasa**: Kotlin[cite: 9, 10, 11, 12, 13, 14]
* **Asinkronus**: Kotlin Coroutines & Flows (`Flow`, `callbackFlow`)[cite: 9, 10, 11, 12, 13, 14]
* **Backend / Database**: Firebase Firestore (NoSQL Cloud Database)[cite: 10, 12, 14]
* **Storage**: Supabase Storage (untuk pengelolaan file gambar produk)[cite: 10]

---

## 📥 Cara Instalasi

1. **Clone Repository**:
   ```bash
   git clone [https://github.com/VeriAbror/KokaBabyStore.git](https://github.com/VeriAbror/KokaBabyStore.git)
