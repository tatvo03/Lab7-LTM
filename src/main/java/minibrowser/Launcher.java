package minibrowser;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Launcher {

    // Giữ strong reference để tránh bị Garbage Collector thu hồi cấu hình Logger
    private static final List<Logger> PINNED_LOGGERS = new ArrayList<>();
    private static boolean silenced = false;

    static {
        silenceWebKitMediaWarnings();
    }

    public static synchronized void silenceWebKitMediaWarnings() {
        if (silenced) return;
        silenced = true;

        try {
            // 1. Tắt log từ các gói WebKit media của JavaFX
            String[] loggerNames = {
                    "com.sun.javafx.webkit.prism.WCMediaPlayerImpl",
                    "com.sun.javafx.webkit",
                    "javafx.scene.web.WebEngine"
            };
            for (String name : loggerNames) {
                Logger l = Logger.getLogger(name);
                l.setLevel(Level.OFF);
                PINNED_LOGGERS.add(l);
            }

            // 2. Thiết lập Filter cho Root Logger và các Handler đang có
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            if (rootLogger != null) {
                for (Handler handler : rootLogger.getHandlers()) {
                    handler.setFilter(record -> {
                        String msg = record.getMessage();
                        String src = record.getSourceClassName();
                        String logName = record.getLoggerName();
                        if (msg != null && (msg.contains("ERROR_MEDIA_INVALID") || msg.contains("WCMediaPlayer"))) {
                            return false;
                        }
                        if (src != null && src.contains("WCMediaPlayer")) {
                            return false;
                        }
                        if (logName != null && logName.contains("WCMediaPlayer")) {
                            return false;
                        }
                        return true;
                    });
                }
            }

            // 3. Lọc trực tiếp System.err để chặn triệt để thông báo đỏ trong IntelliJ IDEA
            PrintStream origErr = System.err;
            System.setErr(new PrintStream(origErr) {
                @Override
                public void println(String x) {
                    if (isMediaWarning(x)) return;
                    super.println(x);
                }

                @Override
                public void print(String s) {
                    if (isMediaWarning(s)) return;
                    super.print(s);
                }

                private boolean isMediaWarning(String str) {
                    if (str == null) return false;
                    return str.contains("WCMediaPlayerImpl")
                            || str.contains("ERROR_MEDIA_INVALID")
                            || str.contains("errCode=260");
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) {
        silenceWebKitMediaWarnings();
        minibrowser.ui.Main.main(args);
    }
}
