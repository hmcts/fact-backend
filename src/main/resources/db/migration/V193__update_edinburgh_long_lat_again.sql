-- FACT-1405_2
-- OLD 55.93194563254789, -3.2511289046474214
-- NEW 55.952014793762594, -3.2052462569620377

UPDATE search_court sc
SET lat = 55.952014793762594, lon = -3.2052462569620377
WHERE "name" = 'Edinburgh Social Security and Child Support Tribunal';