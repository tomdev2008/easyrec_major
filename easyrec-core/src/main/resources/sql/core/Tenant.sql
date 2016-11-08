###############################################################
# $Author: dmann $
# $Revision: 17756 $
# $Date: 2011-02-24 16:13:43 +0100 (Do, 24 Feb 2011) $
###############################################################

# ATTENTION: do not add other sql statements than the CREATE TABLE statement

CREATE TABLE tenant (
  id INT(11) unsigned NOT NULL,
  stringId VARCHAR(100) NOT NULL,
  description VARCHAR(250),
  ratingRangeMin INT(11) unsigned,
  ratingRangeMax INT(11) unsigned,
  ratingRangeNeutral DOUBLE,
  active TINYINT(1) NOT NULL DEFAULT '1',
  operatorid varchar(250) DEFAULT NULL,
  url varchar(250) DEFAULT NULL,
  creationdate datetime DEFAULT NULL,
  tenantConfig mediumblob,
  tenantStatistic mediumblob,
  PRIMARY KEY (id),
  UNIQUE KEY (stringId) 
)ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Table containing tenants';
