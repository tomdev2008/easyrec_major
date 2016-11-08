###############################################################
# $Author: szavrel $
# $Revision: 18633 $
# $Date: 2011-11-04 16:32:45 +0100 (Fr, 04 Nov 2011) $
###############################################################

# ATTENTION: do not add other sql statements than the CREATE TABLE statement

CREATE TABLE actiontype (
  tenantId INT(11) unsigned NOT NULL,
  name VARCHAR(50) NOT NULL,
  id INT(11) NOT NULL,
  hasvalue BIT(1) NOT NULL DEFAULT b'0',
  UNIQUE KEY (tenantId, name),
  UNIQUE KEY (tenantId, id)
) COMMENT='Table containing actiontypes';
