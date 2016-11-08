###############################################################
# $Author: szavrel $
# $Revision: 14846 $
# $Date: 2009-10-21 16:38:35 +0200 (Mi, 21 Okt 2009) $
###############################################################

# ATTENTION: do not add other sql statements than the CREATE TABLE statement

CREATE TABLE aggregatetype (
  tenantId INT(11) unsigned NOT NULL,
  name VARCHAR(50) NOT NULL,
  id INT(11) NOT NULL,
  UNIQUE KEY (tenantId, name),
  UNIQUE KEY (tenantId, id)
) COMMENT='Table containing aggregatetypes';
