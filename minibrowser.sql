-- ===================================================
-- CSDL MINIBROWSER (MySQL / phpMyAdmin / XAMPP)
-- ===================================================

CREATE DATABASE IF NOT EXISTS `minibrowser`
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `minibrowser`;

-- 1. Bảng lưu trữ Dấu trang (Bookmarks)
CREATE TABLE IF NOT EXISTS `bookmarks` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `url` VARCHAR(500) NOT NULL UNIQUE,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng lưu trữ Lịch sử duyệt web (History)
CREATE TABLE IF NOT EXISTS `history` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255),
    `url` VARCHAR(1000) NOT NULL,
    `visited_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng lưu trữ Cài đặt ứng dụng (Settings)
CREATE TABLE IF NOT EXISTS `settings` (
    `key` VARCHAR(100) PRIMARY KEY,
    `value` TEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dữ liệu mặc định cho bookmarks
INSERT IGNORE INTO `bookmarks` (`title`, `url`) VALUES
('Google', 'https://www.google.com'),
('YouTube', 'https://www.youtube.com'),
('GitHub', 'https://github.com'),
('Wikipedia', 'https://vi.wikipedia.org'),
('VnExpress', 'https://vnexpress.net');

-- Dữ liệu mặc định cho settings
INSERT IGNORE INTO `settings` (`key`, `value`) VALUES
('homepage', 'https://www.google.com'),
('search_engine', 'https://www.bing.com/search?q='),
('show_bookmarks_bar', 'true'),
('theme', 'light');
