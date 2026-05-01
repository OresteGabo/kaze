-- Kaze development reset script.
--
-- Run this manually before recreating the schema:
--   psql "postgresql://postgres:YOUR_PASSWORD@localhost:5432/kaze" -f server/src/main/resources/db/dev_drop_all.sql
--   psql "postgresql://postgres:YOUR_PASSWORD@localhost:5432/kaze" -f server/src/main/resources/db/dev_schema.sql
--   psql "postgresql://postgres:YOUR_PASSWORD@localhost:5432/kaze" -f server/src/main/resources/db/dev_seed.sql
--
-- This script drops all current Kaze application tables in dependency-safe order.

BEGIN;

DROP TABLE IF EXISTS access_passes CASCADE;
DROP TABLE IF EXISTS event_invitations CASCADE;
DROP TABLE IF EXISTS event_memberships CASCADE;
DROP TABLE IF EXISTS venue_reservations CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS auth_one_time_login_tokens CASCADE;
DROP TABLE IF EXISTS auth_refresh_tokens CASCADE;
DROP TABLE IF EXISTS oauth_login_attempts CASCADE;
DROP TABLE IF EXISTS user_auth_providers CASCADE;
DROP TABLE IF EXISTS app_users CASCADE;
DROP TABLE IF EXISTS service_requests CASCADE;
DROP TABLE IF EXISTS late_checkout_requests CASCADE;
DROP TABLE IF EXISTS map_nodes CASCADE;
DROP TABLE IF EXISTS map_floors CASCADE;
DROP TABLE IF EXISTS maps CASCADE;
DROP TABLE IF EXISTS amenity_statuses CASCADE;
DROP TABLE IF EXISTS amenity_highlights CASCADE;
DROP TABLE IF EXISTS scheduled_experiences CASCADE;
DROP TABLE IF EXISTS event_days CASCADE;
DROP TABLE IF EXISTS itinerary_items CASCADE;
DROP TABLE IF EXISTS stays CASCADE;
DROP TABLE IF EXISTS guests CASCADE;
DROP TABLE IF EXISTS place_services CASCADE;
DROP TABLE IF EXISTS hotel_buildings CASCADE;
DROP TABLE IF EXISTS hotels CASCADE;
DROP TABLE IF EXISTS service_places CASCADE;

COMMIT;
