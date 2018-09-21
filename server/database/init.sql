create TABLE `commands` (
  `command_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `command` VARCHAR(45) NULL,
  `status` BOOL NOT NULL,
  `create_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`command_id`));
create TABLE `installs` (
  `install_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `token` VARCHAR(45) NULL,
  `path` VARCHAR(45) NOT NULL,
  `create_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`install_id`));
