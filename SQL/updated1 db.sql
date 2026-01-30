-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.42 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for cinema
CREATE DATABASE IF NOT EXISTS `cinema` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `cinema`;

-- Dumping structure for table cinema.movie
CREATE TABLE IF NOT EXISTS `movie` (
  `movie_id` int NOT NULL,
  `movie_name` varchar(100) DEFAULT NULL,
  `age_rate` int DEFAULT NULL,
  `language` enum('ENGLISH','ARABIC') DEFAULT NULL,
  PRIMARY KEY (`movie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table cinema.movie: ~6 rows (approximately)
INSERT INTO `movie` (`movie_id`, `movie_name`, `age_rate`, `language`) VALUES
	(1, '15', 12, 'ENGLISH'),
	(2, '15', 33, 'ENGLISH'),
	(3, '2222', 10, 'ENGLISH'),
	(5, 'fr44', 0, 'ENGLISH'),
	(12, '459899', 11, 'ENGLISH'),
	(33, 'www', 12, 'ENGLISH'),
	(100, 'cars', 17, 'ARABIC');

-- Dumping structure for table cinema.seat
CREATE TABLE IF NOT EXISTS `seat` (
  `seat_id` int NOT NULL,
  `seat_type` enum('STANDARD','VIP') DEFAULT NULL,
  `seat_status` enum('EMPTY','OCCUPIED') NOT NULL DEFAULT 'EMPTY',
  `theater_id` int NOT NULL,
  PRIMARY KEY (`seat_id`,`theater_id`),
  KEY `fk_Seat_Theater1_idx` (`theater_id`),
  CONSTRAINT `fk_Seat_Theater1` FOREIGN KEY (`theater_id`) REFERENCES `theater` (`theater_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table cinema.seat: ~4 rows (approximately)
INSERT INTO `seat` (`seat_id`, `seat_type`, `seat_status`, `theater_id`) VALUES
	(1, 'STANDARD', 'EMPTY', 1),
	(2, 'STANDARD', 'OCCUPIED', 1),
	(3, 'STANDARD', 'EMPTY', 2),
	(10, 'VIP', 'EMPTY', 2);

-- Dumping structure for table cinema.shows
CREATE TABLE IF NOT EXISTS `shows` (
  `movie_id` int NOT NULL,
  `theater_id` int NOT NULL,
  `start_datetime` datetime NOT NULL,
  `end_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`movie_id`,`theater_id`,`start_datetime`),
  KEY `fk_Movie_has_Theater_Theater1_idx` (`theater_id`),
  KEY `fk_Movie_has_Theater_Movie1_idx` (`movie_id`),
  CONSTRAINT `fk_Movie_has_Theater_Movie1` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`movie_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_Movie_has_Theater_Theater1` FOREIGN KEY (`theater_id`) REFERENCES `theater` (`theater_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table cinema.shows: ~3 rows (approximately)
INSERT INTO `shows` (`movie_id`, `theater_id`, `start_datetime`, `end_datetime`) VALUES
	(1, 1, '2002-04-21 13:40:33', '2002-04-21 13:40:33'),
	(2, 3, '2002-04-21 13:40:33', '2002-04-21 14:40:33'),
	(33, 1, '2003-12-09 12:50:00', '2003-12-09 13:50:00'),
	(33, 2, '2003-12-09 12:50:00', '2003-12-09 16:50:00');

-- Dumping structure for table cinema.theater
CREATE TABLE IF NOT EXISTS `theater` (
  `theater_id` int NOT NULL,
  `theater_name` varchar(50) DEFAULT NULL,
  `number_of_seats` int DEFAULT NULL,
  PRIMARY KEY (`theater_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table cinema.theater: ~3 rows (approximately)
INSERT INTO `theater` (`theater_id`, `theater_name`, `number_of_seats`) VALUES
	(1, 'amc', 50),
	(2, 'VOX', 20),
	(3, 'x', 33);

-- Dumping structure for table cinema.ticket
CREATE TABLE IF NOT EXISTS `ticket` (
  `ticket_id` int NOT NULL AUTO_INCREMENT,
  `price` decimal(8,2) NOT NULL,
  `reservation_date` datetime DEFAULT NULL,
  `username` varchar(20) NOT NULL,
  `seat_id` int NOT NULL,
  `theater_id` int NOT NULL,
  PRIMARY KEY (`ticket_id`),
  KEY `fk_Ticket_viewer_idx` (`username`),
  KEY `fk_Ticket_Seat1_idx` (`seat_id`,`theater_id`),
  CONSTRAINT `fk_Ticket_Seat1` FOREIGN KEY (`seat_id`, `theater_id`) REFERENCES `seat` (`seat_id`, `theater_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_Ticket_viewer` FOREIGN KEY (`username`) REFERENCES `viewer` (`username`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_price_nonneg` CHECK ((`price` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb3;

-- Dumping data for table cinema.ticket: ~2 rows (approximately)

-- Dumping structure for table cinema.viewer
CREATE TABLE IF NOT EXISTS `viewer` (
  `username` varchar(20) NOT NULL,
  `Fname` varchar(20) DEFAULT NULL,
  `Lname` varchar(20) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  `phone_number` int DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table cinema.viewer: ~2 rows (approximately)
INSERT INTO `viewer` (`username`, `Fname`, `Lname`, `email`, `phone_number`, `date_of_birth`) VALUES
	('ali22', 'ali', 'albadr', 'ali@gmail.com', 1234567890, '2009-11-21');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
