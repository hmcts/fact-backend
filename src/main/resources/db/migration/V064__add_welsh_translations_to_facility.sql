ALTER TABLE ONLY search_facility ADD name_cy varchar(255);
UPDATE search_facility SET name_cy = 'Cŵn cymorth' WHERE name = 'Assistance dogs';
UPDATE search_facility SET name_cy = 'cyfleuster newid cewynnau babanod' WHERE name = 'Baby changing facility';
UPDATE search_facility SET name_cy = 'Ystafell aros plant' WHERE name = 'Children''s waiting room';
UPDATE search_facility SET name_cy = 'Mynediad i bobl anabl' WHERE name = 'Disabled access';
UPDATE search_facility SET name_cy = 'Toiled anabl' WHERE name = 'Disabled toilet';
UPDATE search_facility SET name_cy = 'Ffilmio a llogi lleoliad' WHERE name = 'Filming and venue hire';
UPDATE search_facility SET name_cy = 'Cymorth Cyntaf' WHERE name = 'First Aid';
UPDATE search_facility SET name_cy = 'Dolen glyw ''T''' WHERE name = 'Hearing loop ''T''';
UPDATE search_facility SET name_cy = 'Ystafell gyfweld' WHERE name = 'Interview room';
UPDATE search_facility SET name_cy = 'Lifft' WHERE name = 'Lift';
UPDATE search_facility SET name_cy = 'Dolen glyw' WHERE name = 'Loop Hearing';
UPDATE search_facility SET name_cy = 'Dim parcio' WHERE name = 'No parking';
UPDATE search_facility SET name_cy = 'Parcio' WHERE name = 'Parking';
UPDATE search_facility SET name_cy = 'Ystafell weddio/dawel' WHERE name = 'Prayer / Quiet room';
UPDATE search_facility SET name_cy = 'Ffôn cyhoeddus' WHERE name = 'Public pay phone';
UPDATE search_facility SET name_cy = 'Toiledau cyhoeddus' WHERE name = 'Public toilets';
UPDATE search_facility SET name_cy = 'Lluniaeth' WHERE name = 'Refreshments';
UPDATE search_facility SET name_cy = 'Bwa diogelwch' WHERE name = 'Security arch';
UPDATE search_facility SET name_cy = 'cyfleusterau fideo' WHERE name = 'Video facilities';
UPDATE search_facility SET name_cy = 'Ystafell aros' WHERE name = 'Waiting Room';
UPDATE search_facility SET name_cy = 'cysylltiad rhwydwaith di-wifr' WHERE name = 'Wireless network connection';
UPDATE search_facility SET name_cy = 'Gwasanaeth gofal' WHERE name = 'Witness care';
UPDATE search_facility SET name_cy = 'Gwasanaeth tystion' WHERE name = 'Witness service';
