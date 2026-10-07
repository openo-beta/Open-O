-- Appointment settings: Item Style on lookup list items, Location Code on appointments.
-- Safe to run more than once.

-- Item Style: an icon and a colour per lookup list item, #rrggbb or #rrggbbaa when see-through.
ALTER TABLE LookupListItem
  ADD COLUMN IF NOT EXISTS icon varchar(255) NULL AFTER label,
  ADD COLUMN IF NOT EXISTS colour varchar(9) NULL AFTER icon;

-- A narrower existing colour column (UBC's was varchar(6), an earlier run of this script's varchar(7)) is widened too.
ALTER TABLE LookupListItem MODIFY colour varchar(9) NULL;

-- Location Code: the Location List item chosen at booking. The location text keeps
-- a snapshot of the item's label, so it widens to fit item labels.
ALTER TABLE appointment
  ADD COLUMN IF NOT EXISTS locationCode int(11) NULL AFTER location,
  MODIFY location varchar(80) NULL DEFAULT NULL;

ALTER TABLE appointmentArchive
  ADD COLUMN IF NOT EXISTS locationCode int(11) NULL AFTER location,
  MODIFY location varchar(80) NULL DEFAULT NULL;

-- The Location List ships empty: booking screens keep free-text locations
-- until it has an active item.
INSERT INTO LookupList (name, listTitle, description, categoryId, active, createdBy, dateCreated)
SELECT 'appointmentLocationCode', 'Appointment Locations', 'Places where an appointment can be booked are added here.', NULL, 1, 'oscar', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM LookupList WHERE name = 'appointmentLocationCode');

-- Status colours may be see-through (#rrggbbaa); a status icon may be a glyph icon (longest name 32 characters).
ALTER TABLE appointment_status
  MODIFY color char(9) NOT NULL DEFAULT '#cccccc',
  MODIFY icon char(40) NOT NULL DEFAULT '''''';