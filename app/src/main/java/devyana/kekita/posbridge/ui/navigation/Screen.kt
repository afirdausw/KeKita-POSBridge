package devyana.kekita.posbridge.ui.navigation

sealed class Screen(val route: String) {

    /** Step 1: Konfigurasi outlet via kode akses */
    data object AccessCode : Screen("access_code")

    /** Step 2: Login kasir/waiter */
    data object Login : Screen("login")

    /** Halaman utama setelah login berhasil */
    data object Home : Screen("home")
}
