package org.ipoliakov.dmap.common;

import java.util.Locale;

public class OS {

    private static final boolean IS_WINDOWS;

    static {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        IS_WINDOWS = osName.startsWith("windows");
    }

    public static boolean isWindows() {
        return IS_WINDOWS;
    }
}
