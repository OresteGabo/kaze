-- Kaze development schema.
--
-- Run this manually before the seed script:
--   psql "postgresql://postgres:YOUR_PASSWORD@localhost:5432/kaze" -f server/src/main/resources/db/dev_schema.sql
--   psql "postgresql://postgres:YOUR_PASSWORD@localhost:5432/kaze" -f server/src/main/resources/db/dev_seed.sql
--
-- This script creates all current Kaze application tables and indexes.

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS app_users (
    id VARCHAR(120) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    email VARCHAR(320) NOT NULL UNIQUE,
    display_name VARCHAR(240),
    username VARCHAR(80),
    phone_number VARCHAR(32),
    password_hash TEXT,
    roles TEXT[] NOT NULL DEFAULT ARRAY['CUSTOMER']::TEXT[],
    disabled BOOLEAN NOT NULL DEFAULT false,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_auth_providers (
    id VARCHAR(120) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    user_id VARCHAR(120) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    provider VARCHAR(40) NOT NULL,
    provider_subject VARCHAR(320) NOT NULL,
    email VARCHAR(320) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    display_name VARCHAR(240),
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (provider, provider_subject)
);

CREATE TABLE IF NOT EXISTS oauth_login_attempts (
    id VARCHAR(120) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    provider VARCHAR(40) NOT NULL,
    state_hash TEXT NOT NULL UNIQUE,
    code_verifier_hash TEXT NOT NULL,
    code_verifier TEXT NOT NULL,
    nonce_hash TEXT,
    nonce TEXT,
    app_redirect_uri TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS auth_one_time_login_tokens (
    id VARCHAR(120) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    user_id VARCHAR(120) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
    id VARCHAR(120) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    user_id VARCHAR(120) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE,
    family_id VARCHAR(120) NOT NULL,
    device_id VARCHAR(240),
    device_label VARCHAR(240),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_token_id VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS service_places (
    id VARCHAR(120) PRIMARY KEY,
    name VARCHAR(240) NOT NULL,
    kind VARCHAR(64) NOT NULL,
    city VARCHAR(120) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    address_label VARCHAR(240),
    map_id VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS events (
    id VARCHAR(120) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    place_id VARCHAR(120) REFERENCES service_places(id) ON DELETE SET NULL,
    organizer_user_id VARCHAR(120) REFERENCES app_users(id) ON DELETE SET NULL,
    slug VARCHAR(160) UNIQUE,
    title VARCHAR(240) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    lifecycle_status VARCHAR(64) NOT NULL DEFAULT 'DRAFT',
    visibility VARCHAR(64) NOT NULL DEFAULT 'PRIVATE',
    summary TEXT,
    starts_at TIMESTAMPTZ,
    ends_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS event_memberships (
    id VARCHAR(120) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    event_id VARCHAR(120) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id VARCHAR(120) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    membership_role VARCHAR(64) NOT NULL,
    membership_status VARCHAR(64) NOT NULL DEFAULT 'ACTIVE',
    invited_by_user_id VARCHAR(120) REFERENCES app_users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (event_id, user_id)
);

CREATE TABLE IF NOT EXISTS event_invitations (
    id VARCHAR(120) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    event_id VARCHAR(120) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    invited_user_id VARCHAR(120) REFERENCES app_users(id) ON DELETE SET NULL,
    sent_by_user_id VARCHAR(120) REFERENCES app_users(id) ON DELETE SET NULL,
    invite_code VARCHAR(64) NOT NULL UNIQUE,
    invited_email VARCHAR(320),
    invited_phone_number VARCHAR(32),
    invitation_status VARCHAR(64) NOT NULL DEFAULT 'PENDING',
    access_tier VARCHAR(64) NOT NULL DEFAULT 'STANDARD',
    note TEXT,
    expires_at TIMESTAMPTZ,
    responded_at TIMESTAMPTZ,
    accepted_at TIMESTAMPTZ,
    declined_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (invited_user_id IS NOT NULL OR invited_email IS NOT NULL OR invited_phone_number IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS access_passes (
    id VARCHAR(120) PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    event_id VARCHAR(120) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id VARCHAR(120) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    invitation_id VARCHAR(120) REFERENCES event_invitations(id) ON DELETE SET NULL,
    pass_code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(240) NOT NULL,
    pass_status VARCHAR(64) NOT NULL DEFAULT 'ACTIVE',
    qr_payload TEXT,
    valid_from TIMESTAMPTZ,
    valid_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (event_id, user_id)
);

CREATE TABLE IF NOT EXISTS hotels (
    id VARCHAR(120) PRIMARY KEY REFERENCES service_places(id) ON DELETE CASCADE,
    slug VARCHAR(160) NOT NULL UNIQUE,
    market VARCHAR(64) NOT NULL,
    timezone_id VARCHAR(80) NOT NULL,
    display_name VARCHAR(240) NOT NULL,
    primary_hex VARCHAR(16) NOT NULL,
    secondary_hex VARCHAR(16) NOT NULL,
    accent_hex VARCHAR(16) NOT NULL,
    surface_hex VARCHAR(16) NOT NULL,
    background_hex VARCHAR(16) NOT NULL,
    logo_asset VARCHAR(240) NOT NULL,
    wordmark_asset VARCHAR(240) NOT NULL,
    heading_scale REAL NOT NULL DEFAULT 1,
    body_scale REAL NOT NULL DEFAULT 1,
    label_scale REAL NOT NULL DEFAULT 1,
    supported_locales TEXT[] NOT NULL DEFAULT ARRAY['en'],
    default_currency_code VARCHAR(8) NOT NULL DEFAULT 'USD',
    active_experiences TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[]
);

CREATE TABLE IF NOT EXISTS hotel_buildings (
    id VARCHAR(120) PRIMARY KEY,
    hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    name VARCHAR(240) NOT NULL,
    floors TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[]
);

CREATE TABLE IF NOT EXISTS place_services (
    id VARCHAR(120) PRIMARY KEY,
    place_id VARCHAR(120) NOT NULL REFERENCES service_places(id) ON DELETE CASCADE,
    title VARCHAR(240) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(64) NOT NULL,
    pricing_kind VARCHAR(64) NOT NULL,
    amount_label VARCHAR(80),
    requestable BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS guests (
    id VARCHAR(120) PRIMARY KEY,
    hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    user_id VARCHAR(120) REFERENCES app_users(id) ON DELETE SET NULL,
    full_name VARCHAR(240) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS stays (
    id VARCHAR(120) PRIMARY KEY,
    hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    guest_id VARCHAR(120) NOT NULL REFERENCES guests(id) ON DELETE CASCADE,
    room_id VARCHAR(120),
    start_iso_utc TIMESTAMPTZ NOT NULL,
    end_iso_utc TIMESTAMPTZ NOT NULL,
    status VARCHAR(64) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS itinerary_items (
    id VARCHAR(120) PRIMARY KEY,
    stay_id VARCHAR(120) NOT NULL REFERENCES stays(id) ON DELETE CASCADE,
    title VARCHAR(240) NOT NULL,
    category VARCHAR(80) NOT NULL,
    status VARCHAR(80) NOT NULL,
    start_iso_utc TIMESTAMPTZ NOT NULL,
    end_iso_utc TIMESTAMPTZ NOT NULL,
    venue_node_id VARCHAR(120),
    venue_floor_id VARCHAR(120),
    venue_label VARCHAR(240),
    notes TEXT
);

CREATE TABLE IF NOT EXISTS event_days (
    id VARCHAR(120) PRIMARY KEY,
    hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    label VARCHAR(120) NOT NULL,
    date_iso DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS scheduled_experiences (
    id VARCHAR(120) PRIMARY KEY,
    hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    day_id VARCHAR(120) NOT NULL REFERENCES event_days(id) ON DELETE CASCADE,
    title VARCHAR(240) NOT NULL,
    description TEXT NOT NULL,
    start_iso TIMESTAMPTZ NOT NULL,
    end_iso TIMESTAMPTZ NOT NULL,
    venue_label VARCHAR(240) NOT NULL,
    host_label VARCHAR(240)
);

CREATE TABLE IF NOT EXISTS amenity_highlights (
    id VARCHAR(120) PRIMARY KEY,
    hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    title VARCHAR(240) NOT NULL,
    description TEXT NOT NULL,
    location_label VARCHAR(240) NOT NULL,
    availability_label VARCHAR(160) NOT NULL,
    category_label VARCHAR(160) NOT NULL,
    access_label VARCHAR(160) NOT NULL,
    action_label VARCHAR(160) NOT NULL
);

CREATE TABLE IF NOT EXISTS amenity_statuses (
    id VARCHAR(120) PRIMARY KEY,
    hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    title VARCHAR(240) NOT NULL,
    location_label VARCHAR(240) NOT NULL,
    status_label VARCHAR(160) NOT NULL,
    hours_label VARCHAR(240) NOT NULL,
    open_now BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS maps (
    id VARCHAR(120) PRIMARY KEY,
    hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    name VARCHAR(240) NOT NULL,
    source_format VARCHAR(80)
);

CREATE TABLE IF NOT EXISTS map_floors (
    id VARCHAR(120) PRIMARY KEY,
    map_id VARCHAR(120) NOT NULL REFERENCES maps(id) ON DELETE CASCADE,
    building_id VARCHAR(120) NOT NULL,
    label VARCHAR(160) NOT NULL,
    level_index INTEGER NOT NULL,
    width REAL NOT NULL,
    height REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS map_nodes (
    id VARCHAR(120) PRIMARY KEY,
    floor_id VARCHAR(120) NOT NULL REFERENCES map_floors(id) ON DELETE CASCADE,
    label VARCHAR(240) NOT NULL,
    kind VARCHAR(80) NOT NULL,
    x REAL NOT NULL,
    y REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS late_checkout_requests (
    id VARCHAR(120) PRIMARY KEY,
    hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    guest_id VARCHAR(120) NOT NULL REFERENCES guests(id) ON DELETE CASCADE,
    stay_id VARCHAR(120) REFERENCES stays(id) ON DELETE SET NULL,
    room_id VARCHAR(120),
    checkout_time_iso TIMESTAMPTZ NOT NULL,
    fee_amount_minor BIGINT NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    payment_preference VARCHAR(80) NOT NULL,
    follow_up_preference VARCHAR(80) NOT NULL,
    status VARCHAR(80) NOT NULL,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS service_requests (
    id VARCHAR(120) PRIMARY KEY,
    hotel_id VARCHAR(120) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    guest_id VARCHAR(120) NOT NULL REFERENCES guests(id) ON DELETE CASCADE,
    stay_id VARCHAR(120) REFERENCES stays(id) ON DELETE SET NULL,
    room_id VARCHAR(120),
    type VARCHAR(80) NOT NULL,
    status VARCHAR(80) NOT NULL,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS guests_hotel_id_idx ON guests(hotel_id);
CREATE INDEX IF NOT EXISTS guests_user_id_idx ON guests(user_id);
CREATE INDEX IF NOT EXISTS app_users_email_idx ON app_users(lower(email));
CREATE UNIQUE INDEX IF NOT EXISTS app_users_username_idx ON app_users(username) WHERE username IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS app_users_phone_number_idx ON app_users(phone_number) WHERE phone_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS user_auth_providers_user_id_idx ON user_auth_providers(user_id);
CREATE INDEX IF NOT EXISTS auth_refresh_tokens_user_id_idx ON auth_refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS auth_refresh_tokens_family_id_idx ON auth_refresh_tokens(family_id);
CREATE INDEX IF NOT EXISTS events_place_id_idx ON events(place_id);
CREATE INDEX IF NOT EXISTS events_organizer_user_id_idx ON events(organizer_user_id);
CREATE INDEX IF NOT EXISTS event_memberships_user_id_idx ON event_memberships(user_id);
CREATE INDEX IF NOT EXISTS event_memberships_event_id_idx ON event_memberships(event_id);
CREATE INDEX IF NOT EXISTS event_invitations_event_id_idx ON event_invitations(event_id);
CREATE INDEX IF NOT EXISTS event_invitations_invited_user_id_idx ON event_invitations(invited_user_id);
CREATE INDEX IF NOT EXISTS event_invitations_invited_email_idx ON event_invitations(lower(invited_email)) WHERE invited_email IS NOT NULL;
CREATE INDEX IF NOT EXISTS event_invitations_invited_phone_number_idx ON event_invitations(invited_phone_number) WHERE invited_phone_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS access_passes_user_id_idx ON access_passes(user_id);
CREATE INDEX IF NOT EXISTS access_passes_event_id_idx ON access_passes(event_id);
CREATE INDEX IF NOT EXISTS oauth_login_attempts_expires_at_idx ON oauth_login_attempts(expires_at);
CREATE INDEX IF NOT EXISTS stays_guest_id_idx ON stays(guest_id);
CREATE INDEX IF NOT EXISTS event_days_hotel_id_idx ON event_days(hotel_id);
CREATE INDEX IF NOT EXISTS scheduled_experiences_day_id_idx ON scheduled_experiences(day_id);
CREATE INDEX IF NOT EXISTS late_checkout_requests_guest_id_idx ON late_checkout_requests(guest_id);
CREATE INDEX IF NOT EXISTS service_requests_guest_id_idx ON service_requests(guest_id);

COMMIT;
