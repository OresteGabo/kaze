-- Kaze development seed data.
--
-- Static seed only: no SQL loops or runtime generators.
-- Run this manually after the schema exists:
--   psql "postgresql://postgres:YOUR_PASSWORD@localhost:5432/kaze" -f server/src/main/resources/db/dev_seed.sql
-- Do not run it against production data.

BEGIN;

TRUNCATE TABLE
    access_passes,
    event_invitations,
    event_memberships,
    venue_reservations,
    events,
    auth_one_time_login_tokens,
    auth_refresh_tokens,
    oauth_login_attempts,
    user_auth_providers,
    app_users,
    service_requests,
    late_checkout_requests,
    map_nodes,
    map_floors,
    maps,
    amenity_statuses,
    amenity_highlights,
    scheduled_experiences,
    event_days,
    itinerary_items,
    stays,
    guests,
    place_services,
    hotel_buildings,
    hotels,
    service_places
RESTART IDENTITY CASCADE;

INSERT INTO app_users (id, email, display_name, username, phone_number, password_hash, roles, disabled, last_login_at) VALUES
    ('user_jean_paul', 'jeanpaul.habimana@kaze.dev', 'Jean-Paul Habimana', 'jeanpaul.h', '+250788100101', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['CUSTOMER']::TEXT[], FALSE, '2026-04-19 07:30:00+00'),
    ('user_ange_uwase', 'ange.uwase@kaze.dev', 'Ange Uwase', 'ange.uwase', '+250788100102', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['CUSTOMER']::TEXT[], FALSE, '2026-04-22 18:12:00+00'),
    ('user_claudine_m', 'claudine.mukamana@kaze.dev', 'Claudine Mukamana', 'claudine.m', '+250788100103', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['STAFF']::TEXT[], FALSE, '2026-04-23 05:55:00+00'),
    ('user_david_k', 'david.karemera@kaze.dev', 'David Karemera', 'david.k', '+250788100104', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['CUSTOMER']::TEXT[], FALSE, '2026-04-21 11:40:00+00'),
    ('user_diane_i', 'diane.ingabire@kaze.dev', 'Diane Ingabire', 'diane.i', '+250788100105', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['CUSTOMER']::TEXT[], FALSE, '2026-04-20 08:25:00+00'),
    ('user_kevin_n', 'kevin.nshuti@kaze.dev', 'Kevin Nshuti', 'kevin.n', '+250788100106', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['CUSTOMER']::TEXT[], FALSE, '2026-04-18 16:05:00+00'),
    ('user_grace_u', 'grace.umutoni@kaze.dev', 'Grace Umutoni', 'grace.u', '+250788100107', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['CUSTOMER']::TEXT[], FALSE, '2026-04-23 13:18:00+00'),
    ('user_patrick_b', 'patrick.bosco@kaze.dev', 'Patrick Bosco', 'patrick.b', '+250788100108', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['STAFF']::TEXT[], FALSE, '2026-04-24 06:42:00+00'),
    ('user_sonia_t', 'sonia.tuyisenge@kaze.dev', 'Sonia Tuyisenge', 'sonia.t', '+250788100109', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['CUSTOMER']::TEXT[], FALSE, '2026-04-21 09:12:00+00'),
    ('user_brian_r', 'brian.rukundo@kaze.dev', 'Brian Rukundo', 'brian.r', '+250788100110', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['CUSTOMER']::TEXT[], FALSE, '2026-04-17 19:48:00+00'),
    ('user_lilian_s', 'lilian.serukiza@kaze.dev', 'Lilian Serukiza', 'lilian.s', '+250788100111', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['STAFF']::TEXT[], FALSE, '2026-04-24 04:20:00+00'),
    ('user_admin_oreste', 'oreste.admin@kaze.dev', 'Oreste Gabo', 'oreste.admin', '+250788100112', '$2a$12$8IPt1WZUYaKu74LaLzdx3uvSIkqMmt4PLvLAxTHs4dld0nI5tbFoG', ARRAY['ADMIN','STAFF']::TEXT[], FALSE, '2026-04-24 07:15:00+00');

INSERT INTO user_auth_providers (id, user_id, provider, provider_subject, email, email_verified, display_name, avatar_url) VALUES
    ('provider_jean_password', 'user_jean_paul', 'PASSWORD', 'jeanpaul.habimana@kaze.dev', 'jeanpaul.habimana@kaze.dev', TRUE, 'Jean-Paul Habimana', NULL),
    ('provider_ange_google', 'user_ange_uwase', 'GOOGLE', 'google-ange-uwase', 'ange.uwase@kaze.dev', TRUE, 'Ange Uwase', NULL),
    ('provider_claudine_password', 'user_claudine_m', 'PASSWORD', 'claudine.mukamana@kaze.dev', 'claudine.mukamana@kaze.dev', TRUE, 'Claudine Mukamana', NULL),
    ('provider_david_apple', 'user_david_k', 'APPLE', 'apple-david-karemera', 'david.karemera@kaze.dev', TRUE, 'David Karemera', NULL),
    ('provider_diane_google', 'user_diane_i', 'GOOGLE', 'google-diane-ingabire', 'diane.ingabire@kaze.dev', TRUE, 'Diane Ingabire', NULL),
    ('provider_kevin_password', 'user_kevin_n', 'PASSWORD', 'kevin.nshuti@kaze.dev', 'kevin.nshuti@kaze.dev', TRUE, 'Kevin Nshuti', NULL),
    ('provider_grace_google', 'user_grace_u', 'GOOGLE', 'google-grace-umutoni', 'grace.umutoni@kaze.dev', TRUE, 'Grace Umutoni', NULL),
    ('provider_patrick_password', 'user_patrick_b', 'PASSWORD', 'patrick.bosco@kaze.dev', 'patrick.bosco@kaze.dev', TRUE, 'Patrick Bosco', NULL),
    ('provider_sonia_apple', 'user_sonia_t', 'APPLE', 'apple-sonia-tuyisenge', 'sonia.tuyisenge@kaze.dev', TRUE, 'Sonia Tuyisenge', NULL),
    ('provider_brian_google', 'user_brian_r', 'GOOGLE', 'google-brian-rukundo', 'brian.rukundo@kaze.dev', TRUE, 'Brian Rukundo', NULL),
    ('provider_lilian_password', 'user_lilian_s', 'PASSWORD', 'lilian.serukiza@kaze.dev', 'lilian.serukiza@kaze.dev', TRUE, 'Lilian Serukiza', NULL),
    ('provider_oreste_google', 'user_admin_oreste', 'GOOGLE', 'google-oreste-admin', 'oreste.admin@kaze.dev', TRUE, 'Oreste Gabo', NULL);

INSERT INTO oauth_login_attempts (id, provider, state_hash, code_verifier_hash, code_verifier, nonce_hash, nonce, app_redirect_uri, expires_at, consumed_at, created_at) VALUES
    ('oauth_attempt_google_001', 'GOOGLE', 'state-hash-google-001', 'verifier-hash-google-001', 'verifier-google-001', 'nonce-hash-google-001', 'nonce-google-001', 'kaze://auth/callback', '2026-04-24 09:35:00+00', NULL, '2026-04-24 09:15:00+00'),
    ('oauth_attempt_apple_001', 'APPLE', 'state-hash-apple-001', 'verifier-hash-apple-001', 'verifier-apple-001', 'nonce-hash-apple-001', 'nonce-apple-001', 'kaze://auth/callback', '2026-04-24 10:10:00+00', '2026-04-24 09:58:00+00', '2026-04-24 09:40:00+00'),
    ('oauth_attempt_google_002', 'GOOGLE', 'state-hash-google-002', 'verifier-hash-google-002', 'verifier-google-002', 'nonce-hash-google-002', 'nonce-google-002', 'kaze://auth/callback', '2026-04-24 11:05:00+00', NULL, '2026-04-24 10:45:00+00');

INSERT INTO auth_one_time_login_tokens (id, user_id, token_hash, expires_at, consumed_at, created_at) VALUES
    ('one_time_login_001', 'user_ange_uwase', 'login-token-hash-ange-001', '2026-04-24 10:02:00+00', NULL, '2026-04-24 10:00:00+00'),
    ('one_time_login_002', 'user_grace_u', 'login-token-hash-grace-001', '2026-04-24 10:11:00+00', '2026-04-24 10:07:00+00', '2026-04-24 10:01:00+00');

INSERT INTO auth_refresh_tokens (id, user_id, token_hash, family_id, device_id, device_label, expires_at, revoked_at, replaced_by_token_id, created_at, last_used_at) VALUES
    ('refresh_token_001', 'user_jean_paul', 'refresh-hash-001', 'family-001', 'iphone-15-jean', 'Jean-Paul iPhone', '2026-06-24 08:00:00+00', NULL, NULL, '2026-04-20 08:00:00+00', '2026-04-24 06:55:00+00'),
    ('refresh_token_002', 'user_ange_uwase', 'refresh-hash-002', 'family-002', 'pixel-ange', 'Ange Pixel', '2026-06-24 09:00:00+00', NULL, NULL, '2026-04-21 09:00:00+00', '2026-04-24 07:05:00+00'),
    ('refresh_token_003', 'user_claudine_m', 'refresh-hash-003', 'family-003', 'ipad-claudine', 'Claudine iPad', '2026-06-24 10:00:00+00', NULL, NULL, '2026-04-21 10:00:00+00', '2026-04-24 05:50:00+00'),
    ('refresh_token_004', 'user_admin_oreste', 'refresh-hash-004', 'family-004', 'macbook-oreste', 'Oreste MacBook', '2026-06-24 11:00:00+00', NULL, NULL, '2026-04-22 11:00:00+00', '2026-04-24 07:10:00+00');

INSERT INTO service_places (id, name, kind, city, country_code, address_label, map_id) VALUES
    ('rw-kgl-marriott', 'Kigali Marriott Hotel', 'HOTEL', 'Kigali', 'RW', 'KN 3 Avenue, Nyarugenge, Kigali', 'map_marriott_main'),
    ('rw-kgl-serena', 'Kigali Serena Hotel', 'HOTEL', 'Kigali', 'RW', 'KN 3 Avenue, Kigali City Centre', 'map_serena_main'),
    ('rw-kgl-radisson', 'Radisson Blu Hotel & Convention Centre, Kigali', 'HOTEL', 'Kigali', 'RW', 'KG 2 Roundabout, Kimihurura, Kigali', 'map_radisson_convention'),
    ('rw-kgl-four-points', 'Four Points by Sheraton Kigali', 'HOTEL', 'Kigali', 'RW', 'KN 3 Avenue, Nyarugenge District, Kigali', NULL),
    ('rw-kgl-mille-collines', 'Hotel des Mille Collines', 'HOTEL', 'Kigali', 'RW', '2 KN 6 Avenue, Kigali', 'map_mille_collines'),
    ('rw-kgl-convention-centre', 'Kigali Convention Centre', 'CONFERENCE_VENUE', 'Kigali', 'RW', 'KG 2 Roundabout, Kimihurura, Kigali', NULL),
    ('rw-rebero-umucyo-gardens', 'Umucyo Garden Venue', 'WEDDING_VENUE', 'Kigali', 'RW', 'Rebero Ridge, Kigali', NULL),
    ('rw-kigali-intare-arena', 'Intare Conference Arena', 'CONFERENCE_VENUE', 'Kigali', 'RW', 'Rusororo, Gasabo, Kigali', NULL),
    ('rw-musanze-gorillas-nest', 'One&Only Gorilla''s Nest', 'HOTEL', 'Musanze', 'RW', 'Kinigi, Musanze, near Volcanoes National Park', NULL),
    ('rw-musanze-kwitonda', 'Singita Kwitonda Lodge', 'HOTEL', 'Musanze', 'RW', 'Edge of Volcanoes National Park, Musanze', NULL);

INSERT INTO hotels (id, slug, market, timezone_id, display_name, primary_hex, secondary_hex, accent_hex, surface_hex, background_hex, logo_asset, wordmark_asset, heading_scale, body_scale, label_scale, supported_locales, default_currency_code, active_experiences) VALUES
    ('rw-kgl-marriott', 'kigali-marriott', 'LUXURY_HOTEL', 'Africa/Kigali', 'Kigali Marriott', '#2F6970', '#B4874F', '#D8C6A3', '#FCF8F1', '#F3EEE5', 'branding/rw-kgl-marriott/logo.svg', 'branding/rw-kgl-marriott/wordmark.svg', 1.05, 1.00, 0.96, ARRAY['en','fr'], 'RWF', ARRAY['STAY','EVENT','EXPLORE','SERVICE_REQUESTS']::TEXT[]),
    ('rw-kgl-serena', 'kigali-serena', 'LUXURY_HOTEL', 'Africa/Kigali', 'Kigali Serena Hotel', '#6D3F2F', '#C18D55', '#E6D1B3', '#FFF8F0', '#F4ECE2', 'branding/rw-kgl-serena/logo.svg', 'branding/rw-kgl-serena/wordmark.svg', 1.03, 1.00, 0.96, ARRAY['en','fr'], 'RWF', ARRAY['STAY','EVENT','EXPLORE']::TEXT[]),
    ('rw-kgl-radisson', 'radisson-blu-kigali', 'BUSINESS_HOTEL', 'Africa/Kigali', 'Radisson Blu Kigali', '#1F4C7A', '#9AB7D3', '#DCE9F5', '#F8FBFF', '#EEF4FA', 'branding/rw-kgl-radisson/logo.svg', 'branding/rw-kgl-radisson/wordmark.svg', 1.02, 1.00, 0.97, ARRAY['en','fr'], 'RWF', ARRAY['STAY','EVENT','EXPLORE']::TEXT[]),
    ('rw-kgl-four-points', 'four-points-kigali', 'BUSINESS_HOTEL', 'Africa/Kigali', 'Four Points by Sheraton Kigali', '#3A4E68', '#8FA8C3', '#DDE7F1', '#F8FAFC', '#EDF2F7', 'branding/rw-kgl-fourpoints/logo.svg', 'branding/rw-kgl-fourpoints/wordmark.svg', 1.01, 1.00, 0.97, ARRAY['en','fr'], 'RWF', ARRAY['STAY','EVENT','EXPLORE']::TEXT[]),
    ('rw-kgl-mille-collines', 'mille-collines', 'BOUTIQUE_HOTEL', 'Africa/Kigali', 'Hotel des Mille Collines', '#355B4C', '#B38B59', '#D9C5A0', '#FBF8F2', '#F1ECE3', 'branding/rw-kgl-mille/logo.svg', 'branding/rw-kgl-mille/wordmark.svg', 1.02, 1.00, 0.97, ARRAY['en','fr'], 'RWF', ARRAY['STAY','EVENT','EXPLORE']::TEXT[]),
    ('rw-musanze-gorillas-nest', 'one-and-only-gorillas-nest', 'LUXURY_LODGE', 'Africa/Kigali', 'One&Only Gorilla''s Nest', '#4C5A49', '#B68B57', '#E5D4BA', '#FBF8F3', '#F2EDE5', 'branding/rw-gorillas-nest/logo.svg', 'branding/rw-gorillas-nest/wordmark.svg', 1.03, 1.00, 0.97, ARRAY['en','fr'], 'RWF', ARRAY['STAY','EXPLORE']::TEXT[]),
    ('rw-musanze-kwitonda', 'singita-kwitonda-lodge', 'LUXURY_LODGE', 'Africa/Kigali', 'Singita Kwitonda Lodge', '#6C5A4A', '#B99A74', '#E7D7C4', '#FAF7F2', '#F1EBE4', 'branding/rw-kwitonda/logo.svg', 'branding/rw-kwitonda/wordmark.svg', 1.03, 1.00, 0.97, ARRAY['en','fr'], 'RWF', ARRAY['STAY','EXPLORE']::TEXT[]);

INSERT INTO hotel_buildings (id, hotel_id, name, floors) VALUES
    ('building_marriott_main', 'rw-kgl-marriott', 'Main Tower', ARRAY['B1','L1','L2','L3','L4','L5','L6','L7','L8','L9']),
    ('building_serena_main', 'rw-kgl-serena', 'Main Wing', ARRAY['L1','L2','L3','L4','L5']),
    ('building_radisson_hotel', 'rw-kgl-radisson', 'Hotel Tower', ARRAY['L1','L2','L3','L4','L5','L6','L7']),
    ('building_radisson_convention', 'rw-kgl-radisson', 'Convention Centre', ARRAY['L1','L2','L3']),
    ('building_fourpoints_main', 'rw-kgl-four-points', 'Main Tower', ARRAY['L1','L2','L3','L4','L5','L6']),
    ('building_mille_collines_main', 'rw-kgl-mille-collines', 'Historic Wing', ARRAY['L1','L2','L3','L4']),
    ('building_gorillas_nest_main', 'rw-musanze-gorillas-nest', 'Forest Suites', ARRAY['L1']),
    ('building_kwitonda_main', 'rw-musanze-kwitonda', 'Main Lodge', ARRAY['L1']);

INSERT INTO place_services (id, place_id, title, description, category, pricing_kind, amount_label, requestable) VALUES
    ('svc_marriott_airport', 'rw-kgl-marriott', 'Airport pickup', 'Private airport pickup arranged through the concierge desk for arriving guests and event speakers.', 'TRANSPORT', 'QUOTE', NULL, TRUE),
    ('svc_marriott_boardroom', 'rw-kgl-marriott', 'Boardroom reservation', 'Half-day and full-day boardroom booking with screen, coffee station, and support staff.', 'MEETING_SPACE', 'FROM', 'From RWF 450,000', TRUE),
    ('svc_marriott_photo', 'rw-kgl-marriott', 'Conference photography', 'Professional event coverage for plenaries, networking sessions, and speaker portraits.', 'MEDIA', 'FROM', 'From RWF 650,000', TRUE),
    ('svc_serena_bridal', 'rw-kgl-serena', 'Bridal preparation suite', 'Private getting-ready suite with breakfast setup, makeup mirrors, and host support.', 'WEDDING', 'FROM', 'From RWF 380,000', TRUE),
    ('svc_serena_private_dinner', 'rw-kgl-serena', 'Private dinner setup', 'Chef-led plated dinner with floral styling for executive dinners or family celebrations.', 'DINING', 'QUOTE', NULL, TRUE),
    ('svc_radisson_expo_hall', 'rw-kgl-radisson', 'Expo hall package', 'Convention floor package with registration desks, ushers, wifi, and LED signage.', 'CONFERENCE', 'QUOTE', NULL, TRUE),
    ('svc_radisson_translation', 'rw-kgl-radisson', 'Interpretation booth support', 'Simultaneous interpretation equipment and booth staffing for regional summits.', 'CONFERENCE', 'FROM', 'From RWF 900,000', TRUE),
    ('svc_fourpoints_simba', 'rw-kgl-four-points', 'Simba ballroom package', 'Meeting and event package built around the Simba Ballroom with AV support and catering coordination.', 'CONFERENCE', 'FROM', 'From RWF 520,000', TRUE),
    ('svc_fourpoints_room_block', 'rw-kgl-four-points', 'Conference room block', 'Business room block with meeting support for organizers staying near central Kigali.', 'STAY_GROUP', 'QUOTE', NULL, TRUE),
    ('svc_mille_breakfast', 'rw-kgl-mille-collines', 'Breakfast meeting table', 'Reserved breakfast table with quiet service for founders, investors, or press interviews.', 'MEETING', 'FROM', 'From RWF 120,000', TRUE),
    ('svc_kcc_auditorium', 'rw-kgl-convention-centre', 'Convention auditorium booking', 'Large-format conference venue booking for plenaries, exhibitions, and public business forums.', 'CONFERENCE', 'QUOTE', NULL, TRUE),
    ('svc_kcc_delegate_flow', 'rw-kgl-convention-centre', 'Delegate arrival and wayfinding', 'Registration desks, delegate signage, queue support, and access coordination for major conferences.', 'CONFERENCE', 'QUOTE', NULL, TRUE),
    ('svc_umucyo_garden', 'rw-rebero-umucyo-gardens', 'Garden ceremony layout', 'Ceremony seating, aisle styling, umbrellas, and garden guest flow setup.', 'WEDDING', 'QUOTE', NULL, TRUE),
    ('svc_umucyo_photo', 'rw-rebero-umucyo-gardens', 'Photo and film package', 'Golden-hour photography and short-form wedding film delivered by a Kigali production crew.', 'MEDIA', 'FROM', 'From RWF 1,400,000', TRUE),
    ('svc_intare_mainhall', 'rw-kigali-intare-arena', 'Main hall conference package', 'Large-scale auditorium package with registration, ushering, and branded wayfinding.', 'CONFERENCE', 'QUOTE', NULL, TRUE),
    ('svc_intare_streaming', 'rw-kigali-intare-arena', 'Hybrid streaming production', 'Stage switching, LED feed capture, and multilingual livestream operations.', 'MEDIA', 'FROM', 'From RWF 2,300,000', TRUE),
    ('svc_gorillas_nest_retreat', 'rw-musanze-gorillas-nest', 'Private retreat buyout', 'Luxury forest retreat setup for leadership groups, destination celebrations, or intimate hosted weekends.', 'RETREAT', 'QUOTE', NULL, TRUE),
    ('svc_gorillas_nest_dinner', 'rw-musanze-gorillas-nest', 'Forest dinner experience', 'Private outdoor dinner with lodge styling and premium service in a secluded forest setting.', 'WEDDING', 'QUOTE', NULL, TRUE),
    ('svc_kwitonda_buyout', 'rw-musanze-kwitonda', 'Executive lodge buyout', 'High-end lodge buyout for founders, leadership teams, and bespoke hosted experiences near Volcanoes National Park.', 'RETREAT', 'QUOTE', NULL, TRUE),
    ('svc_kwitonda_campfire', 'rw-musanze-kwitonda', 'Volcano-view fireside dinner', 'Private fireside dining and storytelling setup for premium guest groups and intimate celebrations.', 'WEDDING', 'FROM', 'From RWF 1,800,000', TRUE);

INSERT INTO guests (id, hotel_id, user_id, full_name) VALUES
    ('guest_jean_paul', 'rw-kgl-marriott', 'user_jean_paul', 'Jean-Paul Habimana'),
    ('guest_ange', 'rw-kgl-marriott', 'user_ange_uwase', 'Ange Uwase'),
    ('guest_diane', 'rw-kgl-serena', 'user_diane_i', 'Diane Ingabire'),
    ('guest_sonia', 'rw-kgl-radisson', 'user_sonia_t', 'Sonia Tuyisenge'),
    ('guest_brian', 'rw-kgl-mille-collines', 'user_brian_r', 'Brian Rukundo');

INSERT INTO stays (id, hotel_id, guest_id, room_id, start_iso_utc, end_iso_utc, status) VALUES
    ('stay_marriott_jean', 'rw-kgl-marriott', 'guest_jean_paul', '814', '2026-04-23 13:00:00+00', '2026-04-26 10:00:00+00', 'ACTIVE'),
    ('stay_marriott_ange', 'rw-kgl-marriott', 'guest_ange', '917', '2026-04-24 12:00:00+00', '2026-04-27 10:00:00+00', 'ACTIVE'),
    ('stay_serena_diane', 'rw-kgl-serena', 'guest_diane', '305', '2026-04-22 14:00:00+00', '2026-04-25 10:00:00+00', 'ACTIVE'),
    ('stay_radisson_sonia', 'rw-kgl-radisson', 'guest_sonia', '512', '2026-04-24 09:00:00+00', '2026-04-25 18:00:00+00', 'ACTIVE'),
    ('stay_mille_brian', 'rw-kgl-mille-collines', 'guest_brian', '204', '2026-04-24 07:00:00+00', '2026-04-24 15:00:00+00', 'ACTIVE');

INSERT INTO itinerary_items (id, stay_id, title, category, status, start_iso_utc, end_iso_utc, venue_node_id, venue_floor_id, venue_label, notes) VALUES
    ('itinerary_jean_001', 'stay_marriott_jean', 'Registration and badge pickup', 'EVENT', 'CONFIRMED', '2026-04-24 07:45:00+00', '2026-04-24 08:15:00+00', 'node_marriott_l1_registration', 'floor_marriott_l1', 'Marriott registration foyer', 'Proceed with QR code ready.'),
    ('itinerary_jean_002', 'stay_marriott_jean', 'Investor breakfast roundtable', 'MEETING', 'CONFIRMED', '2026-04-24 08:30:00+00', '2026-04-24 09:30:00+00', 'node_marriott_l1_salon', 'floor_marriott_l1', 'Akagera Salon', 'Hosted by Kaze partners and invited investors.'),
    ('itinerary_ange_001', 'stay_marriott_ange', 'Women in fintech panel', 'EVENT', 'CONFIRMED', '2026-04-24 10:00:00+00', '2026-04-24 11:15:00+00', 'node_marriott_l9_ballroom', 'floor_marriott_l9', 'Great Rift Ballroom', NULL),
    ('itinerary_diane_001', 'stay_serena_diane', 'Organizer briefing', 'MEETING', 'CONFIRMED', '2026-04-24 09:00:00+00', '2026-04-24 09:45:00+00', NULL, NULL, 'Serena boardroom', 'Final hospitality review before guests arrive.'),
    ('itinerary_sonia_001', 'stay_radisson_sonia', 'Speaker rehearsal', 'EVENT', 'CONFIRMED', '2026-04-24 12:30:00+00', '2026-04-24 13:15:00+00', NULL, NULL, 'Convention Centre stage', 'AV team to check slides and lav mic.'),
    ('itinerary_brian_001', 'stay_mille_brian', 'Founder breakfast', 'EVENT', 'CONFIRMED', '2026-04-24 08:00:00+00', '2026-04-24 09:30:00+00', NULL, NULL, 'Poolside terrace', 'Investor introductions begin at 08:20.');

INSERT INTO event_days (id, hotel_id, label, date_iso) VALUES
    ('day_marriott_20260424', 'rw-kgl-marriott', 'Thu 24 Apr', '2026-04-24'),
    ('day_marriott_20260425', 'rw-kgl-marriott', 'Fri 25 Apr', '2026-04-25'),
    ('day_marriott_20260426', 'rw-kgl-marriott', 'Sat 26 Apr', '2026-04-26'),
    ('day_serena_20260426', 'rw-kgl-serena', 'Sat 26 Apr', '2026-04-26');

INSERT INTO scheduled_experiences (id, hotel_id, day_id, title, description, start_iso, end_iso, venue_label, host_label) VALUES
    ('session_marriott_001', 'rw-kgl-marriott', 'day_marriott_20260424', 'East Africa Fintech Forum opening session', 'Regional founders, operators, and regulators open the forum with a Kigali market outlook and product showcase.', '2026-04-24 08:45:00+00', '2026-04-24 10:00:00+00', 'Great Rift Ballroom', 'Kaze x Rwanda Fintech Community'),
    ('session_marriott_002', 'rw-kgl-marriott', 'day_marriott_20260424', 'Payments infrastructure roundtable', 'Closed-door conversation on interoperability, merchant onboarding, and settlement pain points in East Africa.', '2026-04-24 11:00:00+00', '2026-04-24 12:00:00+00', 'Akagera Salon', 'Patrick Bosco'),
    ('session_marriott_003', 'rw-kgl-marriott', 'day_marriott_20260425', 'Product design clinic', 'Hands-on critique sessions for startup teams preparing live demos and investor meetings.', '2026-04-25 09:30:00+00', '2026-04-25 11:00:00+00', 'Virunga Room', 'Grace Umutoni'),
    ('session_marriott_004', 'rw-kgl-marriott', 'day_marriott_20260426', 'Closing breakfast with investors', 'Final networking breakfast with investors, ecosystem builders, and invited founders.', '2026-04-26 07:30:00+00', '2026-04-26 09:00:00+00', 'Muryango Terrace', 'Jean-Paul Habimana'),
    ('session_serena_001', 'rw-kgl-serena', 'day_serena_20260426', 'Wedding welcome dinner', 'Family welcome dinner with live acoustic music and seated service ahead of the garden ceremony weekend.', '2026-04-26 17:30:00+00', '2026-04-26 20:30:00+00', 'Serena courtyard', 'David Karemera');

INSERT INTO amenity_highlights (id, hotel_id, title, description, location_label, availability_label, category_label, access_label, action_label) VALUES
    ('amenity_marriott_001', 'rw-kgl-marriott', 'Quiet breakfast corner', 'A calmer breakfast area usually preferred by early speakers, sponsors, and press guests.', 'Iriba restaurant', '06:30-09:00', 'Dining', 'Included', 'Open amenity'),
    ('amenity_marriott_002', 'rw-kgl-marriott', 'Delegate coffee point', 'Fast coffee and tea service near the registration foyer for event guests between sessions.', 'Registration foyer', '08:00-16:00', 'Event support', 'Event guests', 'Open amenity map'),
    ('amenity_serena_001', 'rw-kgl-serena', 'Garden portrait corner', 'Styled portrait spot for family photos before the wedding dinner and ceremony.', 'Upper garden', 'By booking', 'Wedding', 'Reserved guests', 'Open venue map'),
    ('amenity_radisson_001', 'rw-kgl-radisson', 'Speaker green room', 'Private hospitality room for keynote speakers and moderators before stage sessions.', 'Convention backstage', 'By pass', 'Conference', 'Speaker access', 'Open amenity');

INSERT INTO amenity_statuses (id, hotel_id, title, location_label, status_label, hours_label, open_now) VALUES
    ('amenity_status_marriott_pool', 'rw-kgl-marriott', 'Pool deck', 'Level 1 leisure wing', 'Open', '06:00-21:00', TRUE),
    ('amenity_status_marriott_gym', 'rw-kgl-marriott', 'Fitness studio', 'Level 2 wellness wing', 'Open', '24 hours', TRUE),
    ('amenity_status_serena_spa', 'rw-kgl-serena', 'Maisha spa', 'Ground floor', 'By appointment', '08:00-20:00', TRUE),
    ('amenity_status_radisson_lounge', 'rw-kgl-radisson', 'Executive lounge', 'Hotel tower level 7', 'Restricted access', '06:30-22:00', TRUE);

INSERT INTO maps (id, hotel_id, name, source_format) VALUES
    ('map_marriott_main', 'rw-kgl-marriott', 'Kigali Marriott guest and event map', 'SVG'),
    ('map_serena_main', 'rw-kgl-serena', 'Kigali Serena hospitality map', 'SVG'),
    ('map_radisson_convention', 'rw-kgl-radisson', 'Radisson convention and hotel map', 'SVG');

INSERT INTO map_floors (id, map_id, building_id, label, level_index, width, height) VALUES
    ('floor_marriott_l1', 'map_marriott_main', 'building_marriott_main', 'Ground floor', 1, 1600, 900),
    ('floor_marriott_l9', 'map_marriott_main', 'building_marriott_main', 'Ballroom level', 9, 1600, 900),
    ('floor_serena_l1', 'map_serena_main', 'building_serena_main', 'Garden level', 1, 1200, 760),
    ('floor_radisson_l1', 'map_radisson_convention', 'building_radisson_convention', 'Convention level', 1, 1800, 980);

INSERT INTO map_nodes (id, floor_id, label, kind, x, y) VALUES
    ('node_marriott_l1_registration', 'floor_marriott_l1', 'Registration foyer', 'REGISTRATION', 260, 410),
    ('node_marriott_l1_salon', 'floor_marriott_l1', 'Akagera Salon', 'MEETING_ROOM', 620, 340),
    ('node_marriott_l1_restaurant', 'floor_marriott_l1', 'Iriba restaurant', 'DINING', 1100, 520),
    ('node_marriott_l9_ballroom', 'floor_marriott_l9', 'Great Rift Ballroom', 'BALLROOM', 760, 430),
    ('node_marriott_l9_virunga', 'floor_marriott_l9', 'Virunga Room', 'BREAKOUT_ROOM', 1180, 300),
    ('node_serena_garden', 'floor_serena_l1', 'Upper garden', 'GARDEN', 740, 280),
    ('node_serena_boardroom', 'floor_serena_l1', 'Executive boardroom', 'MEETING_ROOM', 330, 400),
    ('node_radisson_stage', 'floor_radisson_l1', 'Convention stage', 'AUDITORIUM', 910, 360);

INSERT INTO late_checkout_requests (id, hotel_id, guest_id, stay_id, room_id, checkout_time_iso, fee_amount_minor, currency_code, payment_preference, follow_up_preference, status, note, created_at) VALUES
    ('late_checkout_001', 'rw-kgl-marriott', 'guest_jean_paul', 'stay_marriott_jean', '814', '2026-04-26 14:00:00+00', 5500000, 'RWF', 'CHARGE_TO_ROOM', 'CONFIRM_IN_APP', 'PENDING', 'Requested after investor breakfast because departure is in the afternoon.', '2026-04-24 07:10:00+00'),
    ('late_checkout_002', 'rw-kgl-serena', 'guest_diane', 'stay_serena_diane', '305', '2026-04-25 13:00:00+00', 3500000, 'RWF', 'PAY_AT_RECEPTION', 'CALL_ROOM', 'APPROVED', 'Organizer asked for extra prep time before family departure.', '2026-04-24 06:50:00+00');

INSERT INTO service_requests (id, hotel_id, guest_id, stay_id, room_id, type, status, note, created_at) VALUES
    ('service_request_001', 'rw-kgl-marriott', 'guest_ange', 'stay_marriott_ange', '917', 'HOUSEKEEPING', 'PENDING', 'Fresh towels before returning from the keynote sessions.', '2026-04-24 08:05:00+00'),
    ('service_request_002', 'rw-kgl-radisson', 'guest_sonia', 'stay_radisson_sonia', '512', 'CONCIERGE', 'IN_PROGRESS', 'Need a quick printout of revised speaker notes before rehearsal.', '2026-04-24 11:40:00+00'),
    ('service_request_003', 'rw-kgl-mille-collines', 'guest_brian', 'stay_mille_brian', '204', 'DINING', 'COMPLETED', 'Coffee service for a breakfast meeting of four on the terrace.', '2026-04-24 07:15:00+00');

INSERT INTO events (id, place_id, organizer_user_id, slug, title, event_type, lifecycle_status, visibility, summary, starts_at, ends_at) VALUES
    ('event_fintech_forum_2026', 'rw-kgl-marriott', 'user_patrick_b', 'east-africa-fintech-forum-2026', 'East Africa Fintech Forum 2026', 'CONFERENCE', 'LIVE', 'PRIVATE', 'A Kigali forum for founders, product leaders, regulators, and payment operators building financial products across East Africa.', '2026-04-24 08:30:00+00', '2026-04-26 09:30:00+00'),
    ('event_umucyo_wedding', 'rw-rebero-umucyo-gardens', 'user_david_k', 'umutoni-karemera-wedding', 'Umutoni & Karemera Wedding Weekend', 'WEDDING', 'LIVE', 'PRIVATE', 'Ceremony, family dinner, and evening reception hosted across Rebero and central Kigali.', '2026-04-26 14:00:00+00', '2026-04-27 22:00:00+00'),
    ('event_founders_breakfast', 'rw-kgl-mille-collines', 'user_jean_paul', 'kigali-founders-breakfast-april', 'Kigali Founders Breakfast', 'BUSINESS_EVENT', 'LIVE', 'PRIVATE', 'An invite-only breakfast conversation for founders, operators, and investors meeting in Kigali.', '2026-04-24 08:00:00+00', '2026-04-24 09:30:00+00'),
    ('event_women_in_trade', 'rw-kigali-intare-arena', 'user_grace_u', 'women-in-trade-africa-2026', 'Women In Trade Africa 2026', 'CONFERENCE', 'SCHEDULED', 'PRIVATE', 'A regional summit focused on logistics, trade finance, digital commerce, and women-led cross-border businesses.', '2026-05-08 09:00:00+00', '2026-05-09 18:00:00+00'),
    ('event_leadership_retreat', 'rw-kgl-serena', 'user_claudine_m', 'private-leadership-retreat-april', 'Private Leadership Retreat', 'RETREAT', 'SCHEDULED', 'PRIVATE', 'A closed executive retreat with facilitated sessions, dinner, and hospitality support.', '2026-04-30 15:00:00+00', '2026-05-01 13:00:00+00'),
    ('event_product_night', 'rw-kgl-marriott', 'user_ange_uwase', 'kigali-product-night-april', 'Kigali Product Night', 'NETWORKING_EVENT', 'SCHEDULED', 'PRIVATE', 'A smaller evening gathering for product managers, designers, founders, and ecosystem partners meeting in Kigali.', '2026-04-25 18:30:00+00', '2026-04-25 22:00:00+00'),
    ('event_hospitality_briefing', 'rw-kgl-serena', 'user_lilian_s', 'hospitality-briefing-april', 'Hospitality And Guest Flow Briefing', 'OPERATIONS_EVENT', 'SCHEDULED', 'PRIVATE', 'An operations briefing for event hosts, venue staff, and guest-experience coordinators before a high-touch weekend of events.', '2026-04-25 09:00:00+00', '2026-04-25 11:30:00+00'),
    ('event_creator_roundtable', 'rw-kgl-four-points', 'user_ange_uwase', 'kigali-creator-roundtable-may', 'Kigali Creator Roundtable', 'BUSINESS_EVENT', 'SCHEDULED', 'PRIVATE', 'A hosted evening roundtable for product creators, founders, and operators comparing distribution, storytelling, and community-building tactics in Kigali.', '2026-05-13 18:00:00+00', '2026-05-13 21:15:00+00'),
    ('event_musanze_executive_escape', 'rw-musanze-kwitonda', 'user_claudine_m', 'musanze-executive-escape-june', 'Musanze Executive Escape', 'RETREAT', 'SCHEDULED', 'PRIVATE', 'A premium two-day hosted retreat in Musanze for leaders who want a quieter setting for strategy sessions, fireside dinners, and private guest hosting.', '2026-06-06 14:30:00+00', '2026-06-08 11:00:00+00'),
    ('event_musanze_retreat_2025', 'rw-musanze-gorillas-nest', 'user_claudine_m', 'musanze-leadership-retreat-2025', 'Musanze Leadership Retreat 2025', 'RETREAT', 'COMPLETED', 'PRIVATE', 'A premium offsite for operators and founders combining working sessions with hosted dinners near Volcanoes National Park.', '2025-11-14 14:00:00+00', '2025-11-16 12:30:00+00'),
    ('event_garden_wedding_2025', 'rw-kgl-serena', 'user_david_k', 'ingabire-family-garden-wedding-2025', 'Ingabire Family Garden Wedding', 'WEDDING', 'COMPLETED', 'PRIVATE', 'A past family wedding weekend hosted between the Serena garden and a downtown dinner reception.', '2025-08-09 13:00:00+00', '2025-08-10 23:00:00+00'),
    ('event_kcc_trade_dinner_2025', 'rw-kgl-convention-centre', 'user_grace_u', 'trade-partners-dinner-2025', 'Trade Partners Dinner 2025', 'BUSINESS_EVENT', 'COMPLETED', 'PRIVATE', 'A hosted Kigali dinner for sponsors, partners, and selected delegates after a regional trade forum.', '2025-10-03 18:30:00+00', '2025-10-03 22:30:00+00');

INSERT INTO venue_reservations (id, reservation_code, requester_user_id, place_id, service_id, event_id, event_name, preferred_date_label, guest_count, package_label, add_ons, payment_method, note, status, created_at, updated_at) VALUES
    ('reservation_product_night', 'KAZE-PRODUCT1', 'user_ange_uwase', 'rw-kgl-marriott', 'svc_marriott_boardroom', 'event_product_night', 'Kigali Product Night', '25 Apr 2026 evening', 120, 'Evening launch package', ARRAY['Conference photography','Delegate arrival and wayfinding']::TEXT[], 'Pay deposit after venue confirmation', 'Need a quiet registration point and sponsor table near the room entrance.', 'PENDING_CONFIRMATION', '2026-04-22 09:15:00+00', '2026-04-22 09:15:00+00'),
    ('reservation_umucyo_wedding', 'KAZE-UMUCYO1', 'user_david_k', 'rw-rebero-umucyo-gardens', 'svc_umucyo_garden', 'event_umucyo_wedding', 'Umutoni & Karemera Wedding Weekend', '26 Apr 2026 afternoon', 450, 'Garden ceremony and reception', ARRAY['Photo and film package','Wedding Guest Shuttle']::TEXT[], 'Venue invoice', 'Family arrival should separate elders, vendors, and general guests.', 'CONFIRMED', '2026-04-18 12:30:00+00', '2026-04-20 08:45:00+00'),
    ('reservation_women_trade', 'KAZE-WITA261', 'user_grace_u', 'rw-kigali-intare-arena', 'svc_intare_mainhall', 'event_women_in_trade', 'Women In Trade Africa 2026', '8 May 2026 full day', 1200, 'Main hall conference package', ARRAY['Hybrid streaming production','Delegate arrival and wayfinding']::TEXT[], 'Pay deposit after venue confirmation', 'Need speaker green room, media table, and two registration flows.', 'PENDING_CONFIRMATION', '2026-04-24 10:05:00+00', '2026-04-24 10:05:00+00');

INSERT INTO event_memberships (id, event_id, user_id, membership_role, membership_status, invited_by_user_id) VALUES
    ('membership_001', 'event_fintech_forum_2026', 'user_patrick_b', 'ORGANIZER', 'ACTIVE', NULL),
    ('membership_002', 'event_fintech_forum_2026', 'user_jean_paul', 'ATTENDEE', 'ACTIVE', 'user_patrick_b'),
    ('membership_003', 'event_fintech_forum_2026', 'user_ange_uwase', 'ATTENDEE', 'ACTIVE', 'user_patrick_b'),
    ('membership_004', 'event_fintech_forum_2026', 'user_kevin_n', 'SPEAKER', 'ACTIVE', 'user_patrick_b'),
    ('membership_005', 'event_umucyo_wedding', 'user_david_k', 'ORGANIZER', 'ACTIVE', NULL),
    ('membership_006', 'event_umucyo_wedding', 'user_diane_i', 'FAMILY', 'ACTIVE', 'user_david_k'),
    ('membership_007', 'event_umucyo_wedding', 'user_sonia_t', 'GUEST', 'ACTIVE', 'user_david_k'),
    ('membership_008', 'event_founders_breakfast', 'user_jean_paul', 'ORGANIZER', 'ACTIVE', NULL),
    ('membership_009', 'event_founders_breakfast', 'user_brian_r', 'ATTENDEE', 'ACTIVE', 'user_jean_paul'),
    ('membership_010', 'event_women_in_trade', 'user_grace_u', 'ORGANIZER', 'ACTIVE', NULL),
    ('membership_011', 'event_women_in_trade', 'user_ange_uwase', 'ATTENDEE', 'ACTIVE', 'user_grace_u'),
    ('membership_012', 'event_leadership_retreat', 'user_claudine_m', 'ORGANIZER', 'ACTIVE', NULL),
    ('membership_013', 'event_leadership_retreat', 'user_admin_oreste', 'HOST', 'ACTIVE', 'user_claudine_m'),
    ('membership_014', 'event_product_night', 'user_ange_uwase', 'ORGANIZER', 'ACTIVE', NULL),
    ('membership_015', 'event_product_night', 'user_kevin_n', 'SPEAKER', 'ACTIVE', 'user_ange_uwase'),
    ('membership_016', 'event_product_night', 'user_jean_paul', 'ATTENDEE', 'ACTIVE', 'user_ange_uwase'),
    ('membership_017', 'event_product_night', 'user_brian_r', 'ATTENDEE', 'ACTIVE', 'user_ange_uwase'),
    ('membership_018', 'event_hospitality_briefing', 'user_lilian_s', 'ORGANIZER', 'ACTIVE', NULL),
    ('membership_019', 'event_hospitality_briefing', 'user_claudine_m', 'HOST', 'ACTIVE', 'user_lilian_s'),
    ('membership_020', 'event_hospitality_briefing', 'user_patrick_b', 'VENUE_PARTNER', 'ACTIVE', 'user_lilian_s'),
    ('membership_021', 'event_hospitality_briefing', 'user_sonia_t', 'GUEST', 'ACTIVE', 'user_lilian_s'),
    ('membership_022', 'event_hospitality_briefing', 'user_david_k', 'HOST', 'ACTIVE', 'user_lilian_s'),
    ('membership_023', 'event_women_in_trade', 'user_diane_i', 'ATTENDEE', 'ACTIVE', 'user_grace_u'),
    ('membership_024', 'event_founders_breakfast', 'user_admin_oreste', 'ATTENDEE', 'ACTIVE', 'user_jean_paul'),
    ('membership_025', 'event_musanze_retreat_2025', 'user_claudine_m', 'ORGANIZER', 'ACTIVE', NULL),
    ('membership_026', 'event_musanze_retreat_2025', 'user_admin_oreste', 'HOST', 'ACTIVE', 'user_claudine_m'),
    ('membership_027', 'event_musanze_retreat_2025', 'user_jean_paul', 'ATTENDEE', 'ACTIVE', 'user_claudine_m'),
    ('membership_028', 'event_musanze_retreat_2025', 'user_kevin_n', 'ATTENDEE', 'ACTIVE', 'user_claudine_m'),
    ('membership_029', 'event_garden_wedding_2025', 'user_david_k', 'ORGANIZER', 'ACTIVE', NULL),
    ('membership_030', 'event_garden_wedding_2025', 'user_diane_i', 'FAMILY', 'ACTIVE', 'user_david_k'),
    ('membership_031', 'event_garden_wedding_2025', 'user_sonia_t', 'GUEST', 'ACTIVE', 'user_david_k'),
    ('membership_032', 'event_garden_wedding_2025', 'user_grace_u', 'GUEST', 'ACTIVE', 'user_david_k'),
    ('membership_033', 'event_kcc_trade_dinner_2025', 'user_grace_u', 'ORGANIZER', 'ACTIVE', NULL),
    ('membership_034', 'event_kcc_trade_dinner_2025', 'user_ange_uwase', 'ATTENDEE', 'ACTIVE', 'user_grace_u'),
    ('membership_035', 'event_kcc_trade_dinner_2025', 'user_brian_r', 'ATTENDEE', 'ACTIVE', 'user_grace_u');

INSERT INTO event_invitations (id, event_id, invited_user_id, sent_by_user_id, invite_code, invited_email, invited_phone_number, invitation_status, access_tier, note, expires_at, responded_at, accepted_at, declined_at) VALUES
    ('invite_fintech_jean', 'event_fintech_forum_2026', 'user_jean_paul', 'user_patrick_b', 'EAFF26-JP', 'jeanpaul.habimana@kaze.dev', '+250788100101', 'ACCEPTED', 'ATTENDEE', 'Seat reserved for the opening breakfast and investor roundtable.', '2026-04-24 08:00:00+00', '2026-04-22 18:15:00+00', '2026-04-22 18:15:00+00', NULL),
    ('invite_fintech_ange', 'event_fintech_forum_2026', 'user_ange_uwase', 'user_patrick_b', 'EAFF26-AU', 'ange.uwase@kaze.dev', '+250788100102', 'ACCEPTED', 'ATTENDEE', 'Access includes keynote seating and partner lunch.', '2026-04-24 08:00:00+00', '2026-04-23 07:10:00+00', '2026-04-23 07:10:00+00', NULL),
    ('invite_fintech_kevin', 'event_fintech_forum_2026', 'user_kevin_n', 'user_patrick_b', 'EAFF26-KN', 'kevin.nshuti@kaze.dev', '+250788100106', 'ACCEPTED', 'SPEAKER', 'Speaker access includes backstage and media room support.', '2026-04-24 08:00:00+00', '2026-04-21 16:20:00+00', '2026-04-21 16:20:00+00', NULL),
    ('invite_fintech_patrick', 'event_fintech_forum_2026', 'user_patrick_b', 'user_admin_oreste', 'EAFF26-PB', 'patrick.bosco@kaze.dev', '+250788100108', 'ACCEPTED', 'HOST', 'Operations host pass for the main forum days.', '2026-04-24 08:00:00+00', '2026-04-20 07:50:00+00', '2026-04-20 07:50:00+00', NULL),
    ('invite_wedding_diane', 'event_umucyo_wedding', 'user_diane_i', 'user_david_k', 'UMUCYO-DI', 'diane.ingabire@kaze.dev', '+250788100105', 'ACCEPTED', 'FAMILY', 'Family dinner and ceremony seating are both reserved.', '2026-04-26 14:00:00+00', '2026-04-20 12:40:00+00', '2026-04-20 12:40:00+00', NULL),
    ('invite_wedding_sonia', 'event_umucyo_wedding', 'user_sonia_t', 'user_david_k', 'UMUCYO-ST', 'sonia.tuyisenge@kaze.dev', '+250788100109', 'PENDING', 'STANDARD', 'Reception invite shared with digital directions and parking support.', '2026-04-26 14:00:00+00', NULL, NULL, NULL),
    ('invite_wedding_grace', 'event_umucyo_wedding', 'user_grace_u', 'user_david_k', 'UMUCYO-GU', 'grace.umutoni@kaze.dev', '+250788100107', 'ACCEPTED', 'GUEST', 'Reserved for the ceremony and evening reception.', '2026-04-26 14:00:00+00', '2026-04-21 15:15:00+00', '2026-04-21 15:15:00+00', NULL),
    ('invite_wedding_lilian', 'event_umucyo_wedding', 'user_lilian_s', 'user_david_k', 'UMUCYO-LS', 'lilian.serukiza@kaze.dev', '+250788100111', 'SENT', 'STAFF', 'Guest-flow and family arrival support request.', '2026-04-26 14:00:00+00', NULL, NULL, NULL),
    ('invite_breakfast_brian', 'event_founders_breakfast', 'user_brian_r', 'user_jean_paul', 'FOUNDERS-BR', 'brian.rukundo@kaze.dev', '+250788100110', 'ACCEPTED', 'STANDARD', 'Breakfast table reserved on the terrace.', '2026-04-24 07:45:00+00', '2026-04-23 19:50:00+00', '2026-04-23 19:50:00+00', NULL),
    ('invite_breakfast_oreste', 'event_founders_breakfast', 'user_admin_oreste', 'user_jean_paul', 'FOUNDERS-OG', 'oreste.admin@kaze.dev', '+250788100112', 'ACCEPTED', 'STANDARD', 'Reserved seat for an operator and investor breakfast table.', '2026-04-24 07:45:00+00', '2026-04-22 20:05:00+00', '2026-04-22 20:05:00+00', NULL),
    ('invite_breakfast_david', 'event_founders_breakfast', 'user_david_k', 'user_jean_paul', 'FOUNDERS-DK', 'david.karemera@kaze.dev', '+250788100104', 'SENT', 'STANDARD', 'Hold a seat if the wedding planning morning allows attendance.', '2026-04-24 07:45:00+00', NULL, NULL, NULL),
    ('invite_trade_ange', 'event_women_in_trade', 'user_ange_uwase', 'user_grace_u', 'WITA26-AU', 'ange.uwase@kaze.dev', '+250788100102', 'ACCEPTED', 'ATTENDEE', 'Regional summit pass with both main hall days included.', '2026-05-08 08:30:00+00', '2026-04-18 09:25:00+00', '2026-04-18 09:25:00+00', NULL),
    ('invite_trade_diane', 'event_women_in_trade', 'user_diane_i', 'user_grace_u', 'WITA26-DI', 'diane.ingabire@kaze.dev', '+250788100105', 'ACCEPTED', 'ATTENDEE', 'Included in the women founders and trade operators cohort.', '2026-05-08 08:30:00+00', '2026-04-19 10:00:00+00', '2026-04-19 10:00:00+00', NULL),
    ('invite_trade_sonia', 'event_women_in_trade', 'user_sonia_t', 'user_grace_u', 'WITA26-ST', 'sonia.tuyisenge@kaze.dev', '+250788100109', 'SENT', 'STANDARD', 'Shared with a breakout-track focus on speaker communication.', '2026-05-08 08:30:00+00', NULL, NULL, NULL),
    ('invite_retreat_lilian', 'event_leadership_retreat', 'user_lilian_s', 'user_claudine_m', 'RETREAT-LS', 'lilian.serukiza@kaze.dev', '+250788100111', 'SENT', 'STAFF', 'Hospitality coordination access for executive arrivals.', '2026-04-30 13:00:00+00', NULL, NULL, NULL),
    ('invite_retreat_oreste', 'event_leadership_retreat', 'user_admin_oreste', 'user_claudine_m', 'RETREAT-OG', 'oreste.admin@kaze.dev', '+250788100112', 'ACCEPTED', 'HOST', 'Host-level access for retreat operations review.', '2026-04-30 13:00:00+00', '2026-04-23 08:35:00+00', '2026-04-23 08:35:00+00', NULL),
    ('invite_retreat_claudine', 'event_leadership_retreat', 'user_claudine_m', 'user_admin_oreste', 'RETREAT-CM', 'claudine.mukamana@kaze.dev', '+250788100103', 'ACCEPTED', 'HOST', 'Lead operations host for the private retreat.', '2026-04-30 13:00:00+00', '2026-04-22 06:20:00+00', '2026-04-22 06:20:00+00', NULL),
    ('invite_product_kevin', 'event_product_night', 'user_kevin_n', 'user_ange_uwase', 'PRODUCT-KN', 'kevin.nshuti@kaze.dev', '+250788100106', 'ACCEPTED', 'SPEAKER', 'Product talk slot and speaker hospitality included.', '2026-04-25 18:00:00+00', '2026-04-22 15:35:00+00', '2026-04-22 15:35:00+00', NULL),
    ('invite_product_jean', 'event_product_night', 'user_jean_paul', 'user_ange_uwase', 'PRODUCT-JH', 'jeanpaul.habimana@kaze.dev', '+250788100101', 'ACCEPTED', 'STANDARD', 'Reserved networking and fireside conversation seat.', '2026-04-25 18:00:00+00', '2026-04-23 05:30:00+00', '2026-04-23 05:30:00+00', NULL),
    ('invite_product_brian', 'event_product_night', 'user_brian_r', 'user_ange_uwase', 'PRODUCT-BR', 'brian.rukundo@kaze.dev', '+250788100110', 'ACCEPTED', 'STANDARD', 'Founders and operators guest list access.', '2026-04-25 18:00:00+00', '2026-04-23 20:10:00+00', '2026-04-23 20:10:00+00', NULL),
    ('invite_product_patrick', 'event_product_night', 'user_patrick_b', 'user_ange_uwase', 'PRODUCT-PB', 'patrick.bosco@kaze.dev', '+250788100108', 'SENT', 'VENUE_PARTNER', 'Requested for host coordination and venue support.', '2026-04-25 18:00:00+00', NULL, NULL, NULL),
    ('invite_briefing_claudine', 'event_hospitality_briefing', 'user_claudine_m', 'user_lilian_s', 'BRIEF-CM', 'claudine.mukamana@kaze.dev', '+250788100103', 'ACCEPTED', 'HOST', 'Hospitality lead coordination session.', '2026-04-25 08:30:00+00', '2026-04-22 07:45:00+00', '2026-04-22 07:45:00+00', NULL),
    ('invite_briefing_patrick', 'event_hospitality_briefing', 'user_patrick_b', 'user_lilian_s', 'BRIEF-PB', 'patrick.bosco@kaze.dev', '+250788100108', 'ACCEPTED', 'VENUE_PARTNER', 'Venue and guest-flow support discussion for the upcoming weekend.', '2026-04-25 08:30:00+00', '2026-04-22 08:10:00+00', '2026-04-22 08:10:00+00', NULL),
    ('invite_briefing_sonia', 'event_hospitality_briefing', 'user_sonia_t', 'user_lilian_s', 'BRIEF-ST', 'sonia.tuyisenge@kaze.dev', '+250788100109', 'ACCEPTED', 'GUEST', 'Speaker services and arrivals coordination briefing.', '2026-04-25 08:30:00+00', '2026-04-23 11:20:00+00', '2026-04-23 11:20:00+00', NULL),
    ('invite_briefing_david', 'event_hospitality_briefing', 'user_david_k', 'user_lilian_s', 'BRIEF-DK', 'david.karemera@kaze.dev', '+250788100104', 'ACCEPTED', 'HOST', 'Wedding host operations handoff before the weekend.', '2026-04-25 08:30:00+00', '2026-04-23 13:45:00+00', '2026-04-23 13:45:00+00', NULL),
    ('invite_creator_oreste', 'event_creator_roundtable', 'user_admin_oreste', 'user_ange_uwase', 'CREATOR-OG', 'oreste.admin@kaze.dev', '+250788100112', 'PENDING', 'HOST', 'Ange asked you to join as a host-participant and help shape the evening conversation.', '2026-05-13 17:30:00+00', NULL, NULL, NULL),
    ('invite_musanze_oreste', 'event_musanze_executive_escape', 'user_admin_oreste', 'user_claudine_m', 'MUSANZE-OG', 'oreste.admin@kaze.dev', '+250788100112', 'PENDING', 'HOST', 'Claudine wants you on the retreat host list for guest-flow, rooms, and strategy session coordination.', '2026-06-05 17:00:00+00', NULL, NULL, NULL),
    ('invite_fintech_external', 'event_fintech_forum_2026', NULL, 'user_patrick_b', 'EAFF26-GUEST', 'delegate.guest@example.com', '+250788221199', 'SENT', 'STANDARD', 'Seat held for an external guest not yet registered on Kaze.', '2026-04-24 08:00:00+00', NULL, NULL, NULL),
    ('invite_retreat_jean_2025', 'event_musanze_retreat_2025', 'user_jean_paul', 'user_claudine_m', 'RETREAT25-JH', 'jeanpaul.habimana@kaze.dev', '+250788100101', 'ACCEPTED', 'ATTENDEE', 'Accepted for the full retreat and fireside dinner.', '2025-11-10 16:00:00+00', '2025-11-02 12:10:00+00', '2025-11-02 12:10:00+00', NULL),
    ('invite_retreat_kevin_2025', 'event_musanze_retreat_2025', 'user_kevin_n', 'user_claudine_m', 'RETREAT25-KN', 'kevin.nshuti@kaze.dev', '+250788100106', 'DECLINED', 'ATTENDEE', 'Declined because of travel overlap with a product launch week.', '2025-11-10 16:00:00+00', '2025-11-03 09:40:00+00', NULL, '2025-11-03 09:40:00+00'),
    ('invite_retreat_oreste_2025', 'event_musanze_retreat_2025', 'user_admin_oreste', 'user_claudine_m', 'RETREAT25-OG', 'oreste.admin@kaze.dev', '+250788100112', 'ACCEPTED', 'HOST', 'Host access for rooms, transport, and guest-flow coordination.', '2025-11-10 16:00:00+00', '2025-11-01 10:20:00+00', '2025-11-01 10:20:00+00', NULL),
    ('invite_retreat_brian_2025', 'event_musanze_retreat_2025', 'user_brian_r', 'user_claudine_m', 'RETREAT25-BR', 'brian.rukundo@kaze.dev', '+250788100110', 'EXPIRED', 'STANDARD', 'Invite was sent, but registration closed before confirmation.', '2025-11-10 16:00:00+00', NULL, NULL, NULL),
    ('invite_wedding_diane_2025', 'event_garden_wedding_2025', 'user_diane_i', 'user_david_k', 'GWED25-DI', 'diane.ingabire@kaze.dev', '+250788100105', 'ACCEPTED', 'FAMILY', 'Family seating and dinner table placement were confirmed early.', '2025-08-05 18:00:00+00', '2025-07-21 14:00:00+00', '2025-07-21 14:00:00+00', NULL),
    ('invite_wedding_sonia_2025', 'event_garden_wedding_2025', 'user_sonia_t', 'user_david_k', 'GWED25-ST', 'sonia.tuyisenge@kaze.dev', '+250788100109', 'DECLINED', 'STANDARD', 'Declined because she was traveling outside Kigali that weekend.', '2025-08-05 18:00:00+00', '2025-07-28 17:20:00+00', NULL, '2025-07-28 17:20:00+00'),
    ('invite_wedding_grace_2025', 'event_garden_wedding_2025', 'user_grace_u', 'user_david_k', 'GWED25-GU', 'grace.umutoni@kaze.dev', '+250788100107', 'ACCEPTED', 'STANDARD', 'Accepted for ceremony and evening reception access.', '2025-08-05 18:00:00+00', '2025-07-30 09:25:00+00', '2025-07-30 09:25:00+00', NULL),
    ('invite_wedding_lilian_2025', 'event_garden_wedding_2025', 'user_lilian_s', 'user_david_k', 'GWED25-LS', 'lilian.serukiza@kaze.dev', '+250788100111', 'CANCELLED', 'STAFF', 'Staff hold was cancelled after guest-flow support moved in-house.', '2025-08-05 18:00:00+00', NULL, NULL, NULL),
    ('invite_trade_ange_2025', 'event_kcc_trade_dinner_2025', 'user_ange_uwase', 'user_grace_u', 'TRADE25-AU', 'ange.uwase@kaze.dev', '+250788100102', 'ACCEPTED', 'PARTNER', 'Partner dinner invitation with hosted seating near the sponsor table.', '2025-09-30 18:00:00+00', '2025-09-11 08:15:00+00', '2025-09-11 08:15:00+00', NULL),
    ('invite_trade_brian_2025', 'event_kcc_trade_dinner_2025', 'user_brian_r', 'user_grace_u', 'TRADE25-BR', 'brian.rukundo@kaze.dev', '+250788100110', 'ACCEPTED', 'STANDARD', 'Investor guest access for the evening dinner.', '2025-09-30 18:00:00+00', '2025-09-18 17:45:00+00', '2025-09-18 17:45:00+00', NULL),
    ('invite_trade_patrick_2025', 'event_kcc_trade_dinner_2025', 'user_patrick_b', 'user_grace_u', 'TRADE25-PB', 'patrick.bosco@kaze.dev', '+250788100108', 'DECLINED', 'STANDARD', 'Declined after being assigned to another hosted event on the same date.', '2025-09-30 18:00:00+00', '2025-09-16 15:10:00+00', NULL, '2025-09-16 15:10:00+00'),
    ('invite_trade_external_2025', 'event_kcc_trade_dinner_2025', NULL, 'user_grace_u', 'TRADE25-EXT', 'regional.partner@example.com', '+250788229944', 'EXPIRED', 'PARTNER', 'External partner invite expired before the guest completed registration.', '2025-09-30 18:00:00+00', NULL, NULL, NULL);

INSERT INTO access_passes (id, event_id, user_id, invitation_id, pass_code, title, pass_status, qr_payload, valid_from, valid_until) VALUES
    ('pass_fintech_jean', 'event_fintech_forum_2026', 'user_jean_paul', 'invite_fintech_jean', 'PASS-EAFF26-JP', 'East Africa Fintech Forum attendee pass', 'ACTIVE', 'kaze-pass://event_fintech_forum_2026/user_jean_paul', '2026-04-24 07:30:00+00', '2026-04-26 10:00:00+00'),
    ('pass_fintech_ange', 'event_fintech_forum_2026', 'user_ange_uwase', 'invite_fintech_ange', 'PASS-EAFF26-AU', 'East Africa Fintech Forum attendee pass', 'ACTIVE', 'kaze-pass://event_fintech_forum_2026/user_ange_uwase', '2026-04-24 07:30:00+00', '2026-04-26 10:00:00+00'),
    ('pass_fintech_kevin', 'event_fintech_forum_2026', 'user_kevin_n', 'invite_fintech_kevin', 'PASS-EAFF26-KN', 'East Africa Fintech Forum speaker pass', 'ACTIVE', 'kaze-pass://event_fintech_forum_2026/user_kevin_n', '2026-04-24 07:00:00+00', '2026-04-26 10:00:00+00'),
    ('pass_fintech_patrick', 'event_fintech_forum_2026', 'user_patrick_b', 'invite_fintech_patrick', 'PASS-EAFF26-PB', 'East Africa Fintech Forum host pass', 'ACTIVE', 'kaze-pass://event_fintech_forum_2026/user_patrick_b', '2026-04-24 06:45:00+00', '2026-04-26 10:00:00+00'),
    ('pass_wedding_diane', 'event_umucyo_wedding', 'user_diane_i', 'invite_wedding_diane', 'PASS-UMUCYO-DI', 'Umutoni & Karemera family pass', 'ACTIVE', 'kaze-pass://event_umucyo_wedding/user_diane_i', '2026-04-26 13:30:00+00', '2026-04-27 23:00:00+00'),
    ('pass_wedding_grace', 'event_umucyo_wedding', 'user_grace_u', 'invite_wedding_grace', 'PASS-UMUCYO-GU', 'Umutoni & Karemera guest pass', 'ACTIVE', 'kaze-pass://event_umucyo_wedding/user_grace_u', '2026-04-26 13:30:00+00', '2026-04-27 23:00:00+00'),
    ('pass_breakfast_brian', 'event_founders_breakfast', 'user_brian_r', 'invite_breakfast_brian', 'PASS-FOUNDERS-BR', 'Kigali Founders Breakfast pass', 'ACTIVE', 'kaze-pass://event_founders_breakfast/user_brian_r', '2026-04-24 07:30:00+00', '2026-04-24 10:00:00+00'),
    ('pass_breakfast_oreste', 'event_founders_breakfast', 'user_admin_oreste', 'invite_breakfast_oreste', 'PASS-FOUNDERS-OG', 'Kigali Founders Breakfast pass', 'ACTIVE', 'kaze-pass://event_founders_breakfast/user_admin_oreste', '2026-04-24 07:30:00+00', '2026-04-24 10:00:00+00'),
    ('pass_trade_ange', 'event_women_in_trade', 'user_ange_uwase', 'invite_trade_ange', 'PASS-WITA26-AU', 'Women In Trade Africa delegate pass', 'ACTIVE', 'kaze-pass://event_women_in_trade/user_ange_uwase', '2026-05-08 08:00:00+00', '2026-05-09 19:00:00+00'),
    ('pass_trade_diane', 'event_women_in_trade', 'user_diane_i', 'invite_trade_diane', 'PASS-WITA26-DI', 'Women In Trade Africa delegate pass', 'ACTIVE', 'kaze-pass://event_women_in_trade/user_diane_i', '2026-05-08 08:00:00+00', '2026-05-09 19:00:00+00'),
    ('pass_retreat_oreste', 'event_leadership_retreat', 'user_admin_oreste', 'invite_retreat_oreste', 'PASS-RETREAT-OG', 'Leadership Retreat host pass', 'ACTIVE', 'kaze-pass://event_leadership_retreat/user_admin_oreste', '2026-04-30 12:00:00+00', '2026-05-01 14:00:00+00'),
    ('pass_retreat_claudine', 'event_leadership_retreat', 'user_claudine_m', 'invite_retreat_claudine', 'PASS-RETREAT-CM', 'Leadership Retreat host pass', 'ACTIVE', 'kaze-pass://event_leadership_retreat/user_claudine_m', '2026-04-30 12:00:00+00', '2026-05-01 14:00:00+00'),
    ('pass_product_kevin', 'event_product_night', 'user_kevin_n', 'invite_product_kevin', 'PASS-PRODUCT-KN', 'Kigali Product Night speaker pass', 'ACTIVE', 'kaze-pass://event_product_night/user_kevin_n', '2026-04-25 17:45:00+00', '2026-04-25 22:30:00+00'),
    ('pass_product_jean', 'event_product_night', 'user_jean_paul', 'invite_product_jean', 'PASS-PRODUCT-JH', 'Kigali Product Night guest pass', 'ACTIVE', 'kaze-pass://event_product_night/user_jean_paul', '2026-04-25 17:45:00+00', '2026-04-25 22:30:00+00'),
    ('pass_product_brian', 'event_product_night', 'user_brian_r', 'invite_product_brian', 'PASS-PRODUCT-BR', 'Kigali Product Night guest pass', 'ACTIVE', 'kaze-pass://event_product_night/user_brian_r', '2026-04-25 17:45:00+00', '2026-04-25 22:30:00+00'),
    ('pass_briefing_claudine', 'event_hospitality_briefing', 'user_claudine_m', 'invite_briefing_claudine', 'PASS-BRIEF-CM', 'Hospitality briefing host pass', 'ACTIVE', 'kaze-pass://event_hospitality_briefing/user_claudine_m', '2026-04-25 08:15:00+00', '2026-04-25 12:00:00+00'),
    ('pass_briefing_patrick', 'event_hospitality_briefing', 'user_patrick_b', 'invite_briefing_patrick', 'PASS-BRIEF-PB', 'Hospitality briefing venue partner pass', 'ACTIVE', 'kaze-pass://event_hospitality_briefing/user_patrick_b', '2026-04-25 08:15:00+00', '2026-04-25 12:00:00+00'),
    ('pass_briefing_sonia', 'event_hospitality_briefing', 'user_sonia_t', 'invite_briefing_sonia', 'PASS-BRIEF-ST', 'Hospitality briefing guest pass', 'ACTIVE', 'kaze-pass://event_hospitality_briefing/user_sonia_t', '2026-04-25 08:15:00+00', '2026-04-25 12:00:00+00'),
    ('pass_briefing_david', 'event_hospitality_briefing', 'user_david_k', 'invite_briefing_david', 'PASS-BRIEF-DK', 'Hospitality briefing host pass', 'ACTIVE', 'kaze-pass://event_hospitality_briefing/user_david_k', '2026-04-25 08:15:00+00', '2026-04-25 12:00:00+00'),
    ('pass_retreat_jean_2025', 'event_musanze_retreat_2025', 'user_jean_paul', 'invite_retreat_jean_2025', 'PASS-RETREAT25-JH', 'Musanze Leadership Retreat attendee pass', 'USED', 'kaze-pass://event_musanze_retreat_2025/user_jean_paul', '2025-11-14 12:30:00+00', '2025-11-16 13:00:00+00'),
    ('pass_retreat_oreste_2025', 'event_musanze_retreat_2025', 'user_admin_oreste', 'invite_retreat_oreste_2025', 'PASS-RETREAT25-OG', 'Musanze Leadership Retreat host pass', 'USED', 'kaze-pass://event_musanze_retreat_2025/user_admin_oreste', '2025-11-14 11:00:00+00', '2025-11-16 13:30:00+00'),
    ('pass_wedding_diane_2025', 'event_garden_wedding_2025', 'user_diane_i', 'invite_wedding_diane_2025', 'PASS-GWED25-DI', 'Garden Wedding family pass', 'USED', 'kaze-pass://event_garden_wedding_2025/user_diane_i', '2025-08-09 12:30:00+00', '2025-08-10 23:30:00+00'),
    ('pass_wedding_grace_2025', 'event_garden_wedding_2025', 'user_grace_u', 'invite_wedding_grace_2025', 'PASS-GWED25-GU', 'Garden Wedding guest pass', 'USED', 'kaze-pass://event_garden_wedding_2025/user_grace_u', '2025-08-09 12:30:00+00', '2025-08-10 23:30:00+00'),
    ('pass_trade_ange_2025', 'event_kcc_trade_dinner_2025', 'user_ange_uwase', 'invite_trade_ange_2025', 'PASS-TRADE25-AU', 'Trade Partners Dinner partner pass', 'USED', 'kaze-pass://event_kcc_trade_dinner_2025/user_ange_uwase', '2025-10-03 17:45:00+00', '2025-10-03 23:00:00+00'),
    ('pass_trade_brian_2025', 'event_kcc_trade_dinner_2025', 'user_brian_r', 'invite_trade_brian_2025', 'PASS-TRADE25-BR', 'Trade Partners Dinner guest pass', 'USED', 'kaze-pass://event_kcc_trade_dinner_2025/user_brian_r', '2025-10-03 17:45:00+00', '2025-10-03 23:00:00+00'),
    ('pass_trade_patrick_void_2025', 'event_kcc_trade_dinner_2025', 'user_patrick_b', NULL, 'PASS-TRADE25-PB-VOID', 'Trade Partners Dinner unused hold', 'VOID', 'kaze-pass://event_kcc_trade_dinner_2025/user_patrick_b/void', '2025-10-03 17:45:00+00', '2025-10-03 23:00:00+00');

COMMIT;
