###############################################################
# $Author: szavrel $
# $Revision: 14846 $
# $Date: 2009-10-21 16:38:35 +0200 (Mi, 21 Okt 2009) $
###############################################################

# ATTENTION: do not add other sql statements than the CREATE TABLE statement

CREATE TABLE action (
  id INT(11) unsigned NOT NULL AUTO_INCREMENT,
  tenantId INT(11) NOT NULL,
  userId INT(11),
  sessionId VARCHAR(50),
  ip VARCHAR(45),
  itemId INT(11),
  itemTypeId INT(11) NOT NULL,
  actionTypeId INT(11) NOT NULL,
  ratingValue INT(11),
  description VARCHAR(500) CHARACTER SET utf8,
  actionTime DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY action_reader (tenantId,userId,actionTypeId,itemTypeId)
) COMMENT='Table containing user actions';
    
