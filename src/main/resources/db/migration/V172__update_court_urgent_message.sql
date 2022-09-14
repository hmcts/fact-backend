ALTER TABLE public.search_court ALTER COLUMN alert TYPE varchar(500);
ALTER TABLE public.search_court ALTER COLUMN alert_cy TYPE varchar(500);

UPDATE public.search_court
SET alert = 'Following the death of Her Majesty The Queen, most court and tribunal hearings will not take place on 19 September 2022, the day of the State Funeral. <a href="https://www.gov.uk/government/news/courts-and-tribunals-arrangements-for-the-queens-state-funeral" rel="nofollow" data-mce-href="https://www.gov.uk/government/news/courts-and-tribunals-arrangements-for-the-queens-state-funeral">Find out more information about court and tribunal arrangements at this time</a>',
alert_cy = 'Yn dilyn marwolaeth Ei Mawrhydi Y Frenhines, ni fydd y rhan fwyaf o wrandawiadau llys a thribiwnlys yn cael eu cynnal ar 19 Medi 2022, sef diwrnod yr Angladd Wladol. <a href="https://www.gov.uk/government/news/courts-and-tribunals-arrangements-for-the-queens-state-funeral" rel="nofollow" data-mce-href="https://www.gov.uk/government/news/courts-and-tribunals-arrangements-for-the-queens-state-funeral">Rhagor o wybodaeth am drefniadaur llys ar tribiwnlys yn ystod y cyfnod hwn.</a>';
