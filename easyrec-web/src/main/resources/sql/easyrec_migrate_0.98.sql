-- easyrec database migration statements from 0.97 to 0.98

ALTER TABLE itemtype DROP COLUMN profileSchema;
ALTER TABLE itemtype DROP COLUMN profileMatcher;

-- move the columns from the profile table to the item table
ALTER TABLE item ADD profileData text CHARACTER SET utf8;
ALTER TABLE item ADD changedate DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00';

-- change the field size of description in the actionarchive table
-- to match the description in the action table
ALTER TABLE actionarchive MODIFY description VARCHAR(500) CHARACTER SET utf8;

-- the profile table will be dropped by the migration code


-- update database version
TRUNCATE TABLE easyrec;
INSERT INTO easyrec (version) VALUES (0.98);


