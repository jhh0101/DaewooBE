/* 초기 설정: 외래키 체크 해제 및 인코딩 설정 */
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET NAMES utf8mb4;

-- 1. Location (지역)
DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
                            `location_id` bigint(20) NOT NULL AUTO_INCREMENT,
                            `location_name` varchar(255) DEFAULT NULL,
                            PRIMARY KEY (`location_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Amenities (편의시설 항목)
DROP TABLE IF EXISTS `amenities`;
CREATE TABLE `amenities` (
                             `am_id` bigint(20) NOT NULL AUTO_INCREMENT,
                             `am_category` varchar(255) DEFAULT NULL,
                             `icon_name` varchar(255) DEFAULT NULL,
                             PRIMARY KEY (`am_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Room Type (객실 타입 정의)
DROP TABLE IF EXISTS `room_type`;
CREATE TABLE `room_type` (
                             `room_type_id` bigint(20) NOT NULL AUTO_INCREMENT,
                             `room_type_name` varchar(255) DEFAULT NULL,
                             `parlor_image` varchar(255) DEFAULT NULL,
                             PRIMARY KEY (`room_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. User Table (사용자)
DROP TABLE IF EXISTS `user_tbl`;
CREATE TABLE `user_tbl` (
                            `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
                            `username` varchar(255) DEFAULT NULL,
                            `email` varchar(255) DEFAULT NULL,
                            `password` varchar(255) DEFAULT NULL,
                            `user_phone` varchar(255) DEFAULT NULL,
                            `user_address` varchar(255) DEFAULT NULL,
                            `user_birth` date DEFAULT NULL,
                            `role` varchar(255) DEFAULT NULL,
                            `image_url` varchar(255) DEFAULT NULL,
                            `oauth_id` varchar(255) DEFAULT NULL,
                            `registration_id` varchar(255) DEFAULT NULL,
                            PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Accommodation (숙소)
DROP TABLE IF EXISTS `accommodation`;
CREATE TABLE `accommodation` (
                                 `com_id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `com_title` varchar(255) DEFAULT NULL,
                                 `com_description` varchar(1000) DEFAULT NULL,
                                 `com_address` varchar(255) DEFAULT NULL,
                                 `review_avg` decimal(38,1) DEFAULT 0.0,
                                 `review_count` int(11) DEFAULT 0,
                                 `star` int(11) DEFAULT NULL,
                                 `location_id` bigint(20) DEFAULT NULL,
                                 `category` varchar(20) DEFAULT NULL,
                                 `discount_rate` decimal(5,2) DEFAULT 0.00,
                                 `check_in_time` time DEFAULT NULL,
                                 `check_out_time` time DEFAULT NULL,
                                 PRIMARY KEY (`com_id`),
                                 KEY `accommodation_location_FK` (`location_id`),
                                 CONSTRAINT `accommodation_location_FK` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Acc Room Type (숙소별 객실 타입 매핑 및 가격)
DROP TABLE IF EXISTS `acc_room_type`;
CREATE TABLE `acc_room_type` (
                                 `acc_id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `price` int(11) DEFAULT NULL,
                                 `max_room` int(11) DEFAULT NULL,
                                 `com_id` bigint(20) DEFAULT NULL,
                                 `room_type_id` bigint(20) DEFAULT NULL,
                                 PRIMARY KEY (`acc_id`),
                                 KEY `acc_room_type_accommodation_FK` (`com_id`),
                                 KEY `acc_room_type_room_type_FK` (`room_type_id`),
                                 CONSTRAINT `acc_room_type_accommodation_FK` FOREIGN KEY (`com_id`) REFERENCES `accommodation` (`com_id`) ON DELETE CASCADE,
                                 CONSTRAINT `acc_room_type_room_type_FK` FOREIGN KEY (`room_type_id`) REFERENCES `room_type` (`room_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Accommodation Amenity (숙소 편의시설 매핑)
DROP TABLE IF EXISTS `accommodation_amenity`;
CREATE TABLE `accommodation_amenity` (
                                         `com_id` bigint(20) NOT NULL,
                                         `am_id` bigint(20) NOT NULL,
                                         PRIMARY KEY (`com_id`,`am_id`),
                                         KEY `accommodation_amenity_amenities_FK` (`am_id`),
                                         CONSTRAINT `accommodation_amenity_accommodation_FK` FOREIGN KEY (`com_id`) REFERENCES `accommodation` (`com_id`) ON DELETE CASCADE,
                                         CONSTRAINT `accommodation_amenity_amenities_FK` FOREIGN KEY (`am_id`) REFERENCES `amenities` (`am_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Card (결제 카드)
DROP TABLE IF EXISTS `card`;
CREATE TABLE `card` (
                        `card_id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `card_number` varchar(255) DEFAULT NULL,
                        `exp_date` varchar(255) DEFAULT NULL,
                        `cvc` int(11) DEFAULT NULL,
                        `name` varchar(255) DEFAULT NULL,
                        `country` varchar(255) DEFAULT NULL,
                        `user_id` bigint(20) DEFAULT NULL,
                        PRIMARY KEY (`card_id`),
                        KEY `card_user_tbl_FK` (`user_id`),
                        CONSTRAINT `card_user_tbl_FK` FOREIGN KEY (`user_id`) REFERENCES `user_tbl` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Com Image (숙소 이미지)
DROP TABLE IF EXISTS `com_image`;
CREATE TABLE `com_image` (
                             `com_image_id` bigint(20) NOT NULL AUTO_INCREMENT,
                             `image_url` varchar(255) DEFAULT NULL,
                             `is_main` bit(1) DEFAULT NULL,
                             `com_id` bigint(20) DEFAULT NULL,
                             PRIMARY KEY (`com_image_id`),
                             KEY `com_image_accommodation_FK` (`com_id`),
                             CONSTRAINT `com_image_accommodation_FK` FOREIGN KEY (`com_id`) REFERENCES `accommodation` (`com_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Parlor (구체적인 호실 정보)
DROP TABLE IF EXISTS `parlor`;
CREATE TABLE `parlor` (
                          `par_id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `par_content` varchar(255) DEFAULT NULL,
                          `com_id` bigint(20) DEFAULT NULL,
                          `acc_id` bigint(20) DEFAULT NULL,
                          PRIMARY KEY (`par_id`),
                          KEY `parlor_accommodation_FK` (`com_id`),
                          KEY `parlor_acc_room_type_FK` (`acc_id`),
                          CONSTRAINT `parlor_acc_room_type_FK` FOREIGN KEY (`acc_id`) REFERENCES `acc_room_type` (`acc_id`),
                          CONSTRAINT `parlor_accommodation_FK` FOREIGN KEY (`com_id`) REFERENCES `accommodation` (`com_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Reservation (예약)
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
                               `reservation_id` bigint(20) NOT NULL AUTO_INCREMENT,
                               `check_in` date DEFAULT NULL,
                               `check_out` date DEFAULT NULL,
                               `user_id` bigint(20) DEFAULT NULL,
                               `par_id` bigint(20) DEFAULT NULL,
                               `order_id` varchar(500) DEFAULT NULL,
                               `payment_key` varchar(500) DEFAULT NULL,
                               `amount` bigint(20) DEFAULT NULL,
                               PRIMARY KEY (`reservation_id`),
                               KEY `reservation_user_tbl_FK` (`user_id`),
                               KEY `reservation_parlor_FK` (`par_id`),
                               CONSTRAINT `reservation_parlor_FK` FOREIGN KEY (`par_id`) REFERENCES `parlor` (`par_id`) ON DELETE CASCADE,
                               CONSTRAINT `reservation_user_tbl_FK` FOREIGN KEY (`user_id`) REFERENCES `user_tbl` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Review (리뷰)
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
                          `review_id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `title` varchar(255) DEFAULT NULL,
                          `content` varchar(255) DEFAULT NULL,
                          `score` decimal(38,1) DEFAULT NULL,
                          `com_id` bigint(20) DEFAULT NULL,
                          `user_id` bigint(20) DEFAULT NULL,
                          PRIMARY KEY (`review_id`),
                          KEY `review_accommodation_FK` (`com_id`),
                          KEY `review_user_tbl_FK` (`user_id`),
                          CONSTRAINT `review_accommodation_FK` FOREIGN KEY (`com_id`) REFERENCES `accommodation` (`com_id`) ON DELETE CASCADE,
                          CONSTRAINT `review_user_tbl_FK` FOREIGN KEY (`user_id`) REFERENCES `user_tbl` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Wish (찜/위시리스트)
DROP TABLE IF EXISTS `wish`;
CREATE TABLE `wish` (
                        `wish_id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `user_id` bigint(20) DEFAULT NULL,
                        `com_id` bigint(20) DEFAULT NULL,
                        PRIMARY KEY (`wish_id`),
                        KEY `wish_user_tbl_FK` (`user_id`),
                        KEY `wish_parlor_FK` (`com_id`),
                        CONSTRAINT `wish_accommodation_FK` FOREIGN KEY (`com_id`) REFERENCES `accommodation` (`com_id`) ON DELETE CASCADE,
                        CONSTRAINT `wish_user_tbl_FK` FOREIGN KEY (`user_id`) REFERENCES `user_tbl` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 외래키 체크 복구 */
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;