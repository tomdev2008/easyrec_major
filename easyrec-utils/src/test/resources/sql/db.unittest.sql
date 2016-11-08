# $Author: pmarschik $
# $Date: 2011-05-04 10:53:37 +0200 (Mi, 04 Mai 2011) $
# $Revision: 18244 $
drop table if exists testtable;
CREATE TABLE testtable (
  id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  name varchar(100),
  PRIMARY KEY  (id)
);
