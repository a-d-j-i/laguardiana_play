ALTER TABLE public.lg_deposit ADD column canceled bool NOT NULL default false;
update public.lg_deposit set canceled = ( finish_date is null );

ALTER TABLE lg_event ALTER COLUMN message TYPE varchar(512);

ALTER TABLE public.lg_user DROP dtype;




