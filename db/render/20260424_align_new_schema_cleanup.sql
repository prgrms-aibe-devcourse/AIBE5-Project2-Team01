-- Align the live Render PostgreSQL schema with the current MeetBall ERD.
-- This script removes legacy tables that are no longer referenced by runtime code
-- and drops legacy columns that were left behind on mixed tables during the migration.

begin;

drop table if exists application cascade;
drop table if exists attachments cascade;
drop table if exists bookmarks cascade;
drop table if exists catalog_position cascade;
drop table if exists catalog_tech_stack cascade;
drop table if exists comment cascade;
drop table if exists project_member cascade;
drop table if exists project_position cascade;
drop table if exists project_reads cascade;
drop table if exists reviews cascade;
drop table if exists user_tech_stack cascade;
drop table if exists users cascade;

alter table if exists project
    drop column if exists id cascade,
    drop column if exists created_date cascade,
    drop column if exists current_recruitment cascade,
    drop column if exists leader_avatar_url cascade,
    drop column if exists leader_name cascade,
    drop column if exists leader_role cascade,
    drop column if exists recruitment_deadline cascade,
    drop column if exists summary cascade,
    drop column if exists total_recruitment cascade,
    drop column if exists closed cascade,
    drop column if exists progress_method cascade,
    drop column if exists project_end_at cascade,
    drop column if exists project_start_at cascade,
    drop column if exists recruitment_count cascade,
    drop column if exists recruitment_end_at cascade,
    drop column if exists recruitment_start_at cascade,
    drop column if exists completed cascade,
    drop column if exists position cascade,
    drop column if exists tech_stack_csv cascade;

alter table if exists project
    alter column start_date drop not null,
    alter column bookmark_count set default 0,
    alter column view_count set default 0,
    alter column is_deleted set default false,
    alter column is_public set default true,
    alter column created_at set default current_timestamp;

update project
set bookmark_count = 0
where bookmark_count is null;

update project
set view_count = 0
where view_count is null;

update project
set is_deleted = false
where is_deleted is null;

update project
set is_public = true
where is_public is null;

alter table if exists project_tech_stack
    drop column if exists id cascade,
    drop column if exists sort_order cascade,
    drop column if exists tech_stack_name cascade;

alter table if exists project_tech_stack
    alter column project_id set not null,
    alter column tech_stack_id set not null;

alter table if exists project_resource
    alter column tab_type set default 'RECRUIT',
    alter column display_order set default 0,
    alter column created_at set default current_timestamp;

update project_recruit_position
set approved_user = 0
where approved_user is null;

alter table if exists project_recruit_position
    alter column approved_user set default 0;

alter table if exists bookmarked_project
    alter column created_at set default current_timestamp;

alter table if exists view_history
    alter column viewed_at set default current_timestamp;

alter table if exists project_comment
    alter column is_deleted set default false,
    alter column created_at set default current_timestamp;

alter table if exists peer_review
    alter column created_at set default current_timestamp;

alter table if exists project_review
    alter column created_at set default current_timestamp;

commit;
