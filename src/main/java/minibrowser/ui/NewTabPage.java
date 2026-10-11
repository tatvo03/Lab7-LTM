package minibrowser.ui;

public class NewTabPage {

    public static String getHtml(boolean isDarkMode, String searchEngineUrl) {
        String bgColor = isDarkMode ? "#121212" : "#ffffff";
        String cardBg = isDarkMode ? "#1e1e1e" : "#f8fafc";
        String cardHover = isDarkMode ? "#282828" : "#f1f5f9";
        String textColor = isDarkMode ? "#ffffff" : "#000000";
        String subTextColor = isDarkMode ? "#a1a1aa" : "#4b5563";
        String borderColor = isDarkMode ? "#2e2e2e" : "#e5e7eb";
        String searchBg = isDarkMode ? "#1e1e1e" : "#f8fafc";
        String searchBorder = isDarkMode ? "#3a3a3a" : "#d1d5db";

        return """
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MiniBrowser - Trang chủ</title>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Inter", Helvetica, Arial, sans-serif;
        }
        body {
            background-color: %s;
            color: %s;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            padding: 24px;
            transition: background-color 0.3s ease;
        }
        .container {
            display: flex;
            flex-direction: column;
            align-items: center;
            max-width: 760px;
            width: 100%%;
            margin-top: -30px;
        }
        /* Live Clock & Greeting */
        .time-box {
            text-align: center;
            margin-bottom: 24px;
            user-select: none;
        }
        .clock {
            font-size: 54px;
            font-weight: 300;
            letter-spacing: -1px;
            color: %s;
        }
        .greeting {
            font-size: 15px;
            color: %s;
            margin-top: 4px;
            font-weight: 400;
        }

        /* MiniBrowser Brand Logo */
        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 30px;
            user-select: none;
        }
        .brand-icon {
            width: 48px;
            height: 48px;
            border-radius: 14px;
            background: linear-gradient(135deg, #4f46e5 0%%, #06b6d4 100%%);
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 14px rgba(79, 70, 229, 0.35);
        }
        .brand-icon svg {
            width: 28px;
            height: 28px;
            fill: white;
        }
        .brand-name {
            font-size: 28px;
            font-weight: 700;
            letter-spacing: -0.5px;
            background: linear-gradient(135deg, #4f46e5 0%%, #06b6d4 100%%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        /* Search Engine Selector & Search Form */
        .search-wrapper {
            width: 100%%;
            max-width: 650px;
            margin-bottom: 36px;
        }
        .search-engine-tabs {
            display: flex;
            gap: 8px;
            margin-bottom: 10px;
            justify-content: center;
        }
        .engine-tab {
            font-size: 12px;
            padding: 4px 12px;
            border-radius: 20px;
            border: 1px solid %s;
            background: transparent;
            color: %s;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .engine-tab.active {
            background: #4f46e5;
            color: #ffffff;
            border-color: #4f46e5;
        }
        .search-bar {
            width: 100%%;
            display: flex;
            align-items: center;
            background: %s;
            border: 1.5px solid %s;
            border-radius: 28px;
            padding: 10px 20px;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
            transition: all 0.2s ease;
        }
        .search-bar:focus-within {
            border-color: #4f46e5;
            box-shadow: 0 4px 20px rgba(79, 70, 229, 0.18);
        }
        .search-icon {
            font-size: 17px;
            color: %s;
            margin-right: 14px;
        }
        .search-input {
            flex: 1;
            background: transparent;
            border: none;
            outline: none;
            font-size: 15px;
            color: %s;
        }
        .search-input::placeholder {
            color: %s;
        }

        /* Speed Dial Shortcuts Grid */
        .shortcuts-title {
            align-self: flex-start;
            font-size: 13px;
            font-weight: 600;
            color: %s;
            margin-bottom: 14px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .shortcuts-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 16px;
            width: 100%%;
        }
        .shortcut-card {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-decoration: none;
            background: %s;
            border: 1px solid %s;
            border-radius: 14px;
            padding: 16px 12px;
            transition: all 0.2s ease;
        }
        .shortcut-card:hover {
            background: %s;
            transform: translateY(-3px);
            box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
            border-color: #4f46e5;
        }
        .icon-circle {
            width: 44px;
            height: 44px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            font-weight: bold;
            color: white;
            margin-bottom: 10px;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
        }
        .shortcut-label {
            font-size: 13px;
            font-weight: 500;
            color: %s;
            text-align: center;
            max-width: 110px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        /* Footer */
        .footer {
            margin-top: 40px;
            font-size: 12px;
            color: %s;
            display: flex;
            gap: 16px;
            align-items: center;
        }
        .footer a {
            color: #4f46e5;
            text-decoration: none;
        }
        .footer a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Live Clock -->
        <div class="time-box">
            <div class="clock" id="liveClock">--:--</div>
            <div class="greeting" id="greetingText">Chào mừng bạn đến với MiniBrowser</div>
        </div>

        <!-- Logo -->
        <div class="brand">
            <div class="brand-icon">
                <svg viewBox="0 0 24 24">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
                </svg>
            </div>
            <div class="brand-name">MiniBrowser</div>
        </div>

        <!-- Search Engine Area -->
        <div class="search-wrapper">
            <div class="search-engine-tabs">
                <button type="button" class="engine-tab active" onclick="setEngine('bing')">Bing</button>
                <button type="button" class="engine-tab" onclick="setEngine('google')">Google</button>
                <button type="button" class="engine-tab" onclick="setEngine('duckduckgo')">DuckDuckGo</button>
                <button type="button" class="engine-tab" onclick="setEngine('wikipedia')">Wikipedia</button>
            </div>

            <form class="search-bar" id="searchForm" action="https://www.bing.com/search" method="GET">
                <span class="search-icon">&#128269;</span>
                <input class="search-input" id="searchInput" type="text" name="q" placeholder="Nhập từ khóa tìm kiếm hoặc đường dẫn URL..." autofocus autocomplete="off" />
            </form>
        </div>

        <!-- Shortcuts -->
        <div class="shortcuts-title">Lối tắt truy cập nhanh</div>
        <div class="shortcuts-grid">
            <a class="shortcut-card" href="https://www.google.com">
                <div class="icon-circle" style="background: linear-gradient(135deg, #4285F4, #34A853);">G</div>
                <div class="shortcut-label">Google</div>
            </a>
            <a class="shortcut-card" href="https://www.youtube.com">
                <div class="icon-circle" style="background: linear-gradient(135deg, #FF0000, #CC0000);">&#9658;</div>
                <div class="shortcut-label">YouTube</div>
            </a>
            <a class="shortcut-card" href="https://github.com">
                <div class="icon-circle" style="background: linear-gradient(135deg, #24292e, #4f46e5);">GH</div>
                <div class="shortcut-label">GitHub</div>
            </a>
            <a class="shortcut-card" href="https://vi.wikipedia.org">
                <div class="icon-circle" style="background: linear-gradient(135deg, #64748b, #475569);">W</div>
                <div class="shortcut-label">Wikipedia</div>
            </a>
            <a class="shortcut-card" href="https://vnexpress.net">
                <div class="icon-circle" style="background: linear-gradient(135deg, #9333ea, #7e22ce);">VnE</div>
                <div class="shortcut-label">VnExpress</div>
            </a>
            <a class="shortcut-card" href="https://translate.google.com">
                <div class="icon-circle" style="background: linear-gradient(135deg, #2563eb, #38bdf8);">&#127760;</div>
                <div class="shortcut-label">Google Dịch</div>
            </a>
            <a class="shortcut-card" href="https://chatgpt.com">
                <div class="icon-circle" style="background: linear-gradient(135deg, #10a37f, #059669);">AI</div>
                <div class="shortcut-label">ChatGPT</div>
            </a>
            <a class="shortcut-card" href="https://dantri.com.vn">
                <div class="icon-circle" style="background: linear-gradient(135deg, #0284c7, #0369a1);">DT</div>
                <div class="shortcut-label">Dân Trí</div>
            </a>
        </div>

        <div class="footer">
            <span>MiniBrowser v1.0 • Lab 7 Lập trình mạng</span>
        </div>
    </div>

    <script>
        function updateClock() {
            const now = new Date();
            const hours = String(now.getHours()).padStart(2, '0');
            const minutes = String(now.getMinutes()).padStart(2, '0');
            document.getElementById('liveClock').textContent = hours + ':' + minutes;

            const hour = now.getHours();
            let greeting = 'Chào bạn!';
            if (hour >= 5 && hour < 12) {
                greeting = 'Chào buổi sáng! Chúc bạn ngày mới tốt lành.';
            } else if (hour >= 12 && hour < 18) {
                greeting = 'Chào buổi chiều! Làm việc hiệu quả nhé.';
            } else {
                greeting = 'Chào buổi tối! Thư giãn sau một ngày dài.';
            }
            document.getElementById('greetingText').textContent = greeting;
        }

        setInterval(updateClock, 1000);
        updateClock();

        function setEngine(engine) {
            const form = document.getElementById('searchForm');
            const input = document.getElementById('searchInput');
            const tabs = document.querySelectorAll('.engine-tab');

            tabs.forEach(t => t.classList.remove('active'));
            event.target.classList.add('active');

            if (engine === 'google') {
                form.action = 'https://www.google.com/search';
                input.name = 'q';
            } else if (engine === 'bing') {
                form.action = 'https://www.bing.com/search';
                input.name = 'q';
            } else if (engine === 'duckduckgo') {
                form.action = 'https://duckduckgo.com/';
                input.name = 'q';
            } else if (engine === 'wikipedia') {
                form.action = 'https://vi.wikipedia.org/w/index.php';
                input.name = 'search';
            }
            input.focus();
        }
    </script>
</body>
</html>
        """.formatted(
                bgColor, textColor, textColor, subTextColor,
                borderColor, subTextColor,
                searchBg, searchBorder, subTextColor, textColor, subTextColor,
                subTextColor, cardBg, borderColor, cardHover, textColor, subTextColor
        );
    }
}
