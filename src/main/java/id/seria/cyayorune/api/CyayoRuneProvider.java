package id.seria.cyayorune.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class CyayoRuneProvider {

    private static CyayoRuneAPI instance = null;

    private CyayoRuneProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Mendapatkan instance API CyayoRune.
     * 
     * @return CyayoRuneAPI instance
     * @throws IllegalStateException jika API belum didaftarkan (plugin belum aktif)
     */
    @NotNull
    public static CyayoRuneAPI get() {
        if (instance == null) {
            throw new IllegalStateException("CyayoRune API is not registered yet!");
        }
        return instance;
    }

    @ApiStatus.Internal
    public static void register(@NotNull CyayoRuneAPI api) {
        instance = api;
    }

    @ApiStatus.Internal
    public static void unregister() {
        instance = null;
    }
}
