###############################################################
# $Author: pmarschik $
# $Revision: 18119 $
# $Date: 2011-04-12 17:48:48 +0200 (Di, 12 Apr 2011) $
###############################################################

# ATTENTION: do not add other sql statements than the CREATE TABLE statement

CREATE TABLE itemtype (
  tenantId INT(11) unsigned NOT NULL,
  name VARCHAR(50) NOT NULL,
  id INT(11) NOT NULL,
  profileSchema MEDIUMBLOB,
  profileMatcher MEDIUMBLOB,
  visible BIT(1) NOT NULL DEFAULT b'1',
  UNIQUE KEY (tenantId, name),
  UNIQUE KEY (tenantId, id)
) COMMENT='Table containing itemtypes';
