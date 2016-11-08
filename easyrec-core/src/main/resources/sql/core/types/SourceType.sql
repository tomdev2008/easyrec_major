###############################################################
# $Author: szavrel $
# $Revision: 16885 $
# $Date: 2010-09-30 16:17:47 +0200 (Do, 30 Sep 2010) $
###############################################################

# ATTENTION: do not add other sql statements than the CREATE TABLE statement

CREATE TABLE sourcetype (
  tenantId INT(11) unsigned NOT NULL,
  name VARCHAR(250) NOT NULL,
  id INT(11) NOT NULL,
  UNIQUE KEY (tenantId, name),
  UNIQUE KEY (tenantId, id)
) COMMENT='Table containing sourcetypes';
