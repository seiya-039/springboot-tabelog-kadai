-- rolesテーブル
INSERT IGNORE INTO roles (id, name) VALUES
(1, 'ROLE_FREE_MEMBER'),
(2, 'ROLE_PAID_MEMBER'),
(3, 'ROLE_ADMIN');

-- usersテーブル
INSERT IGNORE INTO users (id, name, furigana, postal_code, address, phone_number, birthday, occupation, email, password, role_id, enabled) VALUES
(1, '侍 太郎', 'サムライ タロウ', '1010022', '東京都千代田区テスト', '09012345678', '1990-01-01', 'エンジニア', 'taro.samurai@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 1, true),
(2, '侍 次郎', 'サムライ ジロウ', '1010022', '東京都千代田区テスト', '09012345678', '1990-02-02', 'デザイナー', 'jiro.samurai@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 1, true),
(3, '侍 花子', 'サムライ ハナコ', '1010022', '東京都千代田区テスト', '09012345678', '1990-03-03', 'マーケティング', 'hanako.samurai@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 3, true);

-- restaurantsテーブル
INSERT IGNORE INTO restaurants (id, name, image, description, lowest_price, highest_price, postal_code, address, opening_time, closing_time, seating_capacity) VALUES
(1, 'バーガー', 'dummy.jpg', 'テストデータ', 3000, 4000, '4500000', '愛知県名古屋市中区栄テスト', '10:00:00', '20:00:00', 50),
(2, '焼肉', 'dummy.jpg', 'テストデータ', 4000, 5000, '4500000', '愛知県名古屋市中区栄テスト', '13:00:00', '23:00:00', 60),
(3, 'お寿司', 'dummy.jpg', 'テストデータ', 2000, 4000, '4500000', '愛知県名古屋市中区栄テスト', '11:00:00', '20:30:00', 70),
(4, 'そば', 'dummy.jpg', 'テストデータ', 5000, 6000, '4500000', '愛知県名古屋市中区栄テスト', '9:30:00', '22:00:00', 80),
(5, 'にく', 'dummy.jpg', 'テストデータ', 3000, 4000, '4500000', '愛知県名古屋市中区栄テスト', '13:00:00', '23:00:00', 90),
(6, 'おとうふ', 'dummy.jpg', 'テストデータ', 4000, 5000, '4500000', '愛知県名古屋市中区栄テスト', '13:00:00', '23:00:00', 100),
(7, '焼き鳥', 'dummy.jpg', 'テストデータ', 2000, 4000, '4500000', '愛知県名古屋市中区栄テスト', '9:30:00', '22:00:00', 110),
(8, 'すき焼き', 'dummy.jpg', 'テストデータ', 5000, 6000, '4500000', '愛知県名古屋市中区栄テスト', '13:00:00', '23:00:00', 120),
(9, '焼肉屋さん', 'dummy.jpg', 'テストデータ', 3000, 4000, '4500000', '愛知県名古屋市中区栄テスト', '11:00:00', '20:30:00', 130),
(10, 'みすじ', 'dummy.jpg', 'テストデータ', 4000, 5000, '4500000', '愛知県名古屋市中区栄テスト', '10:00:00', '20:00:00', 140);

-- regular_holidaysテーブル
INSERT IGNORE INTO regular_holidays (id, day, day_index) VALUES
(1, '月', 1),
(2, '火', 2),
(3, '水', 3),
(4, '木', 4),
(5, '金', 5),
(6, '土', 6),
(7, '日', 0),
(8, '不定休', null);

-- regular_holiday_restaurantテーブル
INSERT IGNORE INTO regular_holiday_restaurant (id, restaurant_id, regular_holiday_id) VALUES
(1, 1, 1),
(2, 1, 3),
(3, 3, 8),
(4, 4, 1),
(5, 4, 5),
(6, 6, 7),
(7, 7, 1),
(8, 7, 2),
(9, 7, 6),
(10, 8, 3),
(11, 10, 2),
(12, 10, 5),
(13, 10, 7);

-- categoriesテーブル
INSERT IGNORE INTO categories (id, name) VALUES
(1, '居酒屋'),
(2, '焼肉'),
(3, '寿司'),
(4, '定食'),
(5, 'カレー'),
(6, '喫茶店'),
(7, 'ステーキ'),
(8, 'ハンバーグ'),
(9, 'ハンバーガー'),
(10, 'そば'),
(11, '焼き鳥');

-- category_restaurantテーブル
INSERT IGNORE INTO category_restaurant (id, restaurant_id, category_id) VALUES
(1,1,1),
(2,2,2),
(3,3,1),
(4,3,3),
(5,4,5),
(6,5,6),
(7,6,7),
(8,7,8),
(9,8,9),
(10,9,10),
(11,10,11);

-- companiesテーブル
INSERT IGNORE INTO companies (id, name, postal_code, address, representative, establishment_date, capital, business, number_of_employees) VALUES
(1, 'NAGOYAMESHI', '1010022', '東京都千代田区テスト', '侍 太郎', '2000年3月19日', '11,000円', '飲食店サービス', '80名');

 -- favoritesテーブル						
 INSERT IGNORE INTO favorites (id, restaurant_id, user_id) VALUES (1, 1, 1);						
 INSERT IGNORE INTO favorites (id, restaurant_id, user_id) VALUES (2, 2, 1);						
 INSERT IGNORE INTO favorites (id, restaurant_id, user_id) VALUES (3, 3, 1);						
 INSERT IGNORE INTO favorites (id, restaurant_id, user_id) VALUES (4, 4, 1);						
 INSERT IGNORE INTO favorites (id, restaurant_id, user_id) VALUES (5, 5, 1);						
 INSERT IGNORE INTO favorites (id, restaurant_id, user_id) VALUES (6, 6, 1);						
 INSERT IGNORE INTO favorites (id, restaurant_id, user_id) VALUES (7, 7, 1);						
 INSERT IGNORE INTO favorites (id, restaurant_id, user_id) VALUES (8, 8, 1);						
 INSERT IGNORE INTO favorites (id, restaurant_id, user_id) VALUES (9, 9, 1);						
 INSERT IGNORE INTO favorites (id, restaurant_id, user_id) VALUES (10, 10, 1);						