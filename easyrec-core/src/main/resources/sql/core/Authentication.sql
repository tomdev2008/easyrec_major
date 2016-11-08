###############################################################
# $Author: szavrel $ 
# $Revision: 14846 $ 
# $Date: 2009-10-21 16:38:35 +0200 (Mi, 21 Okt 2009) $ 
###############################################################

# ATTENTION: do not add other sql statements than the CREATE TABLE statement

CREATE TABLE authentication (
  tenantId INT(11) unsigned NOT NULL,
  domainURL VARCHAR(250) NOT NULL DEFAULT '',
  UNIQUE KEY unique_authentication (tenantId,domainURL)
) COMMENT='Table containing valid access domains for tenants';