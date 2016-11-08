###############################################################
# $Author: szavrel $
# $Revision: 14846 $
# $Date: 2009-10-21 16:38:35 +0200 (Mi, 21 Okt 2009) $
###############################################################

# ATTENTION: do not add other sql statements than the CREATE TABLE statement

CREATE TABLE recommendeditem (
  id INT(11) unsigned NOT NULL AUTO_INCREMENT,
  itemId INT(11) NOT NULL,
  itemTypeId INT(11) NOT NULL,
  recommendationId INT(11) NOT NULL,
  predictionValue DOUBLE NOT NULL DEFAULT '0',
  itemAssocId INT(11),
  explanation VARCHAR(255),
  PRIMARY KEY (id),
  UNIQUE KEY unique_recommended_item (itemId, itemTypeId, recommendationId)
) COMMENT='Table containing all recommended items (ever)';
