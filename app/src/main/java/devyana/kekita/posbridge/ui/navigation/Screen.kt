package devyana.kekita.posbridge.ui.navigation

sealed class Screen(val route: String) {

    /** Step 1: Konfigurasi outlet via kode akses */
    data object AccessCode : Screen("access_code")

    /** Step 2: Login kasir/waiter */
    data object Login : Screen("login")

    /** Halaman utama POS setelah login berhasil */
    data object Home : Screen("home")

    /** Halaman dummy transaksi (Riwayat) */
    data object Transaction : Screen("transaction")

    /** Halaman dummy daftar produk */
    data object Product : Screen("product")

    /** Halaman dummy laporan */
    data object Report : Screen("report")

    /** Halaman dummy pembayaran */
    data object Payment : Screen("payment")

    /** Halaman dummy checker kitchen/bar */
    data object Checker : Screen("checker")

    /** Halaman pengaturan (settings) */
    data object Settings : Screen("settings")
}
