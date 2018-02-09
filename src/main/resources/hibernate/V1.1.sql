-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema NanoDefiner
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema NanoDefiner
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `NanoDefiner` DEFAULT CHARACTER SET utf8 COLLATE utf8_swedish_ci ;
USE `NanoDefiner` ;

-- -----------------------------------------------------
-- Table `NanoDefiner`.`User`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `NanoDefiner`.`User` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `hash_function` VARCHAR(10) NOT NULL,
  `hash_salt` VARCHAR(10) NOT NULL,
  `registration_date` DATETIME NOT NULL DEFAULT NOW(),
  `activation_date` DATETIME NULL,
  `activation_code` VARCHAR(10) NULL,
  `forename` VARCHAR(50) NOT NULL,
  `surname` VARCHAR(50) NOT NULL,
  `title` VARCHAR(10) NULL,
  `email` VARCHAR(50) NOT NULL,
  `current_session` VARCHAR(50) NULL,
  `last_session` VARCHAR(50) NULL,
  `last_login` DATETIME NULL,
  `last_ip` VARCHAR(50) NULL,
  `reminded` TINYINT(1) NOT NULL DEFAULT 0,
  `access` VARCHAR(255) NULL,
  `locale` VARCHAR(5) NOT NULL DEFAULT 'en_US',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `subject_principal_UNIQUE` (`username` ASC),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `NanoDefiner`.`Dossier`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `NanoDefiner`.`Dossier` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `fk_id_subject` INT NOT NULL,
  `creation_date` DATETIME NOT NULL DEFAULT NOW(),
  `change_date` DATETIME NOT NULL DEFAULT NOW(),
  `name` VARCHAR(255) NOT NULL,
  `comment` TEXT NULL,
  `internal_comment` TEXT NULL,
  `sample_name` VARCHAR(255) NULL,
  `multiconstituent` TINYINT(1) NOT NULL DEFAULT 0,
  `purpose` VARCHAR(50) NOT NULL DEFAULT 'reach',
  PRIMARY KEY (`id`),
  INDEX `dossier_fk_id_subject_idx` (`fk_id_subject` ASC),
  CONSTRAINT `dossier_fk_id_subject`
    FOREIGN KEY (`fk_id_subject`)
    REFERENCES `NanoDefiner`.`User` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `NanoDefiner`.`Technique`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `NanoDefiner`.`Technique` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `signifier` VARCHAR(50) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `comment` TEXT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `NanoDefiner`.`Material`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `NanoDefiner`.`Material` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `id_fk_dossier` INT NOT NULL,
  `signifier` VARCHAR(50) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `comment` TEXT NULL,
  `template` TINYINT(1) NOT NULL DEFAULT 0,
  `reference` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `material_fk_id_dossier_idx` (`id_fk_dossier` ASC),
  CONSTRAINT `material_fk_id_dossier`
    FOREIGN KEY (`id_fk_dossier`)
    REFERENCES `NanoDefiner`.`Dossier` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `NanoDefiner`.`Report`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `NanoDefiner`.`Report` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `id_fk_dossier` INT NOT NULL,
  `creation_date` DATETIME NOT NULL DEFAULT NOW(),
  `change_date` DATETIME NOT NULL DEFAULT NOW(),
  `name` VARCHAR(255) NOT NULL,
  `report_file` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  INDEX `report_fk_id_dossier_idx` (`id_fk_dossier` ASC),
  CONSTRAINT `report_fk_id_dossier`
    FOREIGN KEY (`id_fk_dossier`)
    REFERENCES `NanoDefiner`.`Dossier` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `NanoDefiner`.`Method`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `NanoDefiner`.`Method` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `fk_id_dossier` INT NOT NULL,
  `fk_id_technique` INT NOT NULL,
  `creation_date` DATETIME NOT NULL DEFAULT NOW(),
  `change_date` DATETIME NOT NULL DEFAULT NOW(),
  `name` VARCHAR(255) NOT NULL,
  `comment` TEXT NULL,
  `tier` VARCHAR(5) NOT NULL,
  `data_file` VARCHAR(255) NULL,
  `data_file_format` VARCHAR(255) NULL,
  `result` VARCHAR(10) NULL,
  PRIMARY KEY (`id`),
  INDEX `method_fk_id_dossier_idx` (`fk_id_dossier` ASC),
  INDEX `method_fk_id_technique_idx` (`fk_id_technique` ASC),
  CONSTRAINT `method_fk_id_technique`
    FOREIGN KEY (`fk_id_technique`)
    REFERENCES `NanoDefiner`.`Technique` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `method_fk_id_dossier`
    FOREIGN KEY (`fk_id_dossier`)
    REFERENCES `NanoDefiner`.`Dossier` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `NanoDefiner`.`Parameter`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `NanoDefiner`.`Parameter` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `creation_date` DATETIME NOT NULL DEFAULT NOW(),
  `change_date` DATETIME NOT NULL DEFAULT NOW(),
  `name` VARCHAR(255) NOT NULL,
  `comment` TEXT NULL,
  `type` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NULL,
  `previous_value` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `NanoDefiner`.`Profile`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `NanoDefiner`.`Profile` (
  `id_fk_subject` INT NOT NULL,
  `id_fk_technique` INT NOT NULL,
  PRIMARY KEY (`id_fk_subject`, `id_fk_technique`),
  INDEX `profile_fk_id_technique_idx` (`id_fk_technique` ASC),
  INDEX `profile_fk_id_subject_idx` (`id_fk_subject` ASC),
  CONSTRAINT `profile_fk_id_user`
    FOREIGN KEY (`id_fk_subject`)
    REFERENCES `NanoDefiner`.`User` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `profile_fk_id_technique`
    FOREIGN KEY (`id_fk_technique`)
    REFERENCES `NanoDefiner`.`Technique` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `NanoDefiner`.`MaterialCriterion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `NanoDefiner`.`MaterialCriterion` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `id_material` INT NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `material_criterion_fk_id_material_idx` (`id_material` ASC),
  CONSTRAINT `material_criterion_fk_id_material`
    FOREIGN KEY (`id_material`)
    REFERENCES `NanoDefiner`.`Material` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
