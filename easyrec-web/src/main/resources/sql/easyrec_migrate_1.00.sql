-- ALTER TABLE action DROP COLUMN searchSucceeded;
-- ALTER TABLE action DROP COLUMN numberOfFoundItems;
-- ALTER TABLE action CHANGE COLUMN description actionInfo VARCHAR(500) CHARACTER SET utf8;

DROP TABLE IF EXISTS actionarch;
CREATE TABLE actionarch (
  id int(11) unsigned NOT NULL,
  tenantId int(11) NOT NULL,
  userId int(11) default NULL,
  sessionId varchar(50) default NULL,
  ip varchar(45) default NULL,
  itemId int(11) default NULL,
  itemTypeId int(11) NOT NULL,
  actionTypeId int(11) NOT NULL,
  ratingValue int(11) default NULL,
  actionInfo varchar(500) CHARACTER SET utf8 default NULL,
  actionTime datetime NOT NULL,
  PRIMARY KEY  (id)
) ENGINE=InnoDb DEFAULT CHARSET=latin1 COMMENT='Table containing archived actions';

-- ALTER TABLE backtracking ADD COLUMN itemFromTypeId int(11) NOT NULL AFTER itemFromId;
-- ALTER TABLE backtracking ADD COLUMN itemToTypeId int(11) NOT NULL AFTER itemToId;
-- ALTER TABLE backtracking CHANGE COLUMN TIMESTAMP actionTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
-- ALTER TABLE backtracking CHANGE COLUMN assocType recType INT(11) UNSIGNED NOT NULL;
-- ALTER TABLE backtracking DROP INDEX assoc;
-- ALTER TABLE backtracking ADD INDEX assoc(tenantId, itemFromId, itemFromTypeId, recType, itemToId, itemToTypeId);

-- ALTER TABLE actiontype ADD COLUMN weight INT(11) NOT NULL DEFAULT 1;

-- strange string operations necessary because SqlScriptParser interprets double / as comment
UPDATE plugin_configuration SET pluginVersion="1.0" WHERE SUBSTRING(pluginId,8)="www.easyrec.org/plugins/ARM" AND pluginVersion="0.98";
UPDATE plugin_configuration SET pluginVersion="1.0" WHERE SUBSTRING(pluginId,8)="www.easyrec.org/plugins/slopeone" AND pluginVersion="0.98";
UPDATE plugin_configuration SET pluginVersion="1.0" WHERE SUBSTRING(pluginId,8)="www.easyrec.org/plugins/UPA" AND pluginVersion="0.98";

UPDATE plugin_log SET pluginVersion="1.0" WHERE SUBSTRING(pluginId,8)="www.easyrec.org/plugins/ARM" AND pluginVersion="0.98";
UPDATE plugin_log SET pluginVersion="1.0" WHERE SUBSTRING(pluginId,8)="www.easyrec.org/plugins/slopeone" AND pluginVersion="0.98";
UPDATE plugin_log SET pluginVersion="1.0" WHERE SUBSTRING(pluginId,8)="www.easyrec.org/plugins/UPA" AND pluginVersion="0.98";
UPDATE plugin_log SET pluginVersion="1.0" WHERE SUBSTRING(pluginId,8)="www.easyrec.org/internal/Archive" AND pluginVersion="0.96";
UPDATE plugin_log SET pluginVersion="1.0" WHERE SUBSTRING(pluginId,8)="www.easyrec.org/internal/SessionToUserMapping" AND pluginVersion="0.98";

UPDATE sourcetype SET NAME=CONCAT(SUBSTRING_INDEX(NAME,"/",5),"/1.0") WHERE SUBSTRING(NAME,8)="www.easyrec.org/plugins/ARM/0.98";
UPDATE sourcetype SET NAME=CONCAT(SUBSTRING_INDEX(NAME,"/",5),"/1.0") WHERE SUBSTRING(NAME,8)="www.easyrec.org/plugins/slopeone/0.98";
UPDATE sourcetype SET NAME=CONCAT(SUBSTRING_INDEX(NAME,"/",5),"/1.0") WHERE SUBSTRING(NAME,8)="www.easyrec.org/plugins/UPA/0.98";

-- update database version
TRUNCATE TABLE easyrec;
INSERT INTO easyrec (version) VALUES (1.0);