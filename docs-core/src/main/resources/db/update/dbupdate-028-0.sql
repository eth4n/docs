create table T_IFTTT_RULE(IFTTT_ID_C         varchar(36)   not null,IFTTT_NAME_C       varchar(50)   not null, IFTTT_RULE_C        varchar(5000) not null,IFTTT_CREATEDATE_D datetime      not null,IFTTT_DELETEDATE_D datetime,primary key (IFTTT_ID_C));
create table T_IFTTT_TRIGGER(IFTTTT_ID_C         varchar(36)   not null,IFTTT_NAME_C       varchar(50)   not null unique, primary key (IFTTTT_ID_C) );
create table T_IFTTT_RULE_TRIGGER(IFTTTRT_ID_C         varchar(36)   not null,IFTTT_ID_C         varchar(36)   not null,IFTTTT_ID_C         varchar(36)   not null, primary key (IFTTTRT_ID_C) );
alter table T_IFTTT_RULE_TRIGGER add constraint FK_IFTTT_ID_C foreign key (IFTTT_ID_C) references T_IFTTT_RULE (IFTTT_ID_C) on delete cascade;
alter table T_IFTTT_RULE_TRIGGER add constraint FK_IFTTTT_ID_C foreign key (IFTTTT_ID_C) references T_IFTTT_TRIGGER (IFTTTT_ID_C) on delete restrict;
update T_CONFIG set CFG_VALUE_C = '28' where CFG_ID_C = 'DB_VERSION';
