--- Create a table containing the single point of entry information for courts
--- If a row exists in this table, then that means there is a single point of entry that exists
--- for the given court based on the area of law provided by the id
---
--- Sources of data:
--- Rebecca C's spreadsheet = the childcare data (superseding the previous data in the migrations)
--- Every other bit of existing data with the exception of previous childcare data should be in the new table.
---
--- New table:
---
CREATE TABLE public.search_courtareaoflawspoe (
   id integer NOT NULL,
   court_id integer NOT NULL,
   area_of_law_id integer NOT NULL
);

CREATE SEQUENCE public.search_courtareaoflawspoe_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.search_courtareaoflawspoe_id_seq OWNED BY public.search_courtareaoflawspoe.id;
ALTER TABLE ONLY public.search_courtareaoflawspoe ALTER COLUMN id SET DEFAULT nextval('public.search_courtareaoflawspoe_id_seq'::regclass);

--- Childcare data based on the C100 spreadsheet provided 10/08/2021
INSERT INTO public.search_courtareaoflawspoe(court_id, area_of_law_id)
SELECT scaol.court_id, scaol.area_of_law_id
FROM public.search_court sc
JOIN public.search_courtareaoflaw scaol
ON sc.id = scaol.court_id
WHERE sc.slug IN (
    'barrow-in-furness-county-court-and-family-court', 'basingstoke-county-court-and-family-court',
    'birmingham-civil-and-family-justice-centre', 'bournemouth-and-poole-county-court-and-family-court',
    'brighton-county-court', 'bristol-civil-and-family-justice-centre',
    'caernarfon-justice-centre', 'cardiff-civil-and-family-justice-centre',
    'carlisle-combined-court', 'central-family-court', 'chelmsford-magistrates-court-and-family-court',
    'coventry-combined-court-centre', 'derby-combined-court-centre', 'east-london-family-court',
    'exeter-combined-court-centre', 'gloucester-and-cheltenham-county-and-family-court',
    'guildford-county-court-and-family-court', 'kingston-upon-hull-combined-court-centre',
    'leeds-combined-court-centre', 'leicester-county-court-and-family-court',
    'lincoln-county-court-and-family-court', 'liverpool-civil-and-family-court',
    'luton-justice-centre', 'manchester-civil-justice-centre-civil-and-family-courts',
    'medway-county-court-and-family-court', 'middlesbrough-county-court-at-teesside-combined-court',
    'milton-keynes-county-court-and-family-court', 'newcastle-civil-family-courts-and-tribunals-centre',
    'newport-south-wales-county-court-and-family-court', 'northampton-crown-county-and-family-court',
    'norwich-combined-court-centre', 'nottingham-county-court-and-family-court',
    'oxford-combined-court-centre', 'peterborough-combined-court-centre', 'plymouth-combined-court',
    'portsmouth-combined-court-centre', 'lancaster-courthouse',
    --- Note that both Slough and Reading use the same slug here for the spoe
    'reading-county-court-and-family-court',
    'sheffield-combined-court-centre', 'southampton-combined-court-centre',
    'stoke-on-trent-combined-court', 'swansea-civil-justice-centre', 'swindon-combined-court',
    'truro-county-court-and-family-court', 'watford-county-court-and-family-court',
    'west-london-family-court', 'wolverhampton-combined-court-centre', 'worcester-combined-court',
    'wrexham-county-and-family-court', 'york-county-court-and-family-court',
    'torquay-and-newton-abbot-county-court-and-family-court', 'taunton-crown-county-and-family-court'
)
AND scaol.area_of_law_id = (
    SELECT id
    FROM public.search_areaoflaw saol
    WHERE saol.name = 'Children'
);

--- Existing data migrated across to new table; with exception of childcare areas of law
INSERT INTO public.search_courtareaoflawspoe(court_id, area_of_law_id)
SELECT scaol.court_id, scaol.area_of_law_id
FROM public.search_court sc
JOIN public.search_courtareaoflaw scaol
ON sc.id = scaol.court_id
WHERE scaol.single_point_of_entry = true
AND scaol.area_of_law_id != (
    SELECT id
    FROM public.search_areaoflaw
    WHERE name = 'Children'
);

--- Remove existing column on the search_courtareaoflaw table
ALTER TABLE public.search_courtareaoflaw
DROP COLUMN single_point_of_entry;
