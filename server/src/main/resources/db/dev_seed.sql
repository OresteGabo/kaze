-- Kaze development seed data.
--
-- Run this manually after the schema exists:
--   psql "postgresql://postgres:YOUR_PASSWORD@localhost:5432/kaze" -f server/src/main/resources/db/dev_seed.sql
--
-- This script resets Kaze application tables before inserting the development dataset.
-- Do not run it against production data.

BEGIN;

TRUNCATE TABLE
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

INSERT INTO app_users (
    id,
    email,
    display_name,
    password_hash,
    roles
) VALUES (
    'user_aline',
    'aline@example.com',
    'Aline Uwase',
    '$2a$12$idtV1xjR/gPX0yHZRg9x..v.0GF3F/gQl/2LDfggOfPhI/LTqNPIm',
    ARRAY['CUSTOMER']::TEXT[]
),
(
    'user_michael',
    'michael@example.com',
    'Michael Nshuti',
    '$2a$12$idtV1xjR/gPX0yHZRg9x..v.0GF3F/gQl/2LDfggOfPhI/LTqNPIm',
    ARRAY['CUSTOMER']::TEXT[]
),
(
    'user_staff_claudine',
    'claudine.staff@example.com',
    'Claudine Mukamana',
    '$2a$12$idtV1xjR/gPX0yHZRg9x..v.0GF3F/gQl/2LDfggOfPhI/LTqNPIm',
    ARRAY['STAFF']::TEXT[]
),
(
    'user_admin_orest',
    'admin@example.com',
    'Kaze Admin',
    '$2a$12$idtV1xjR/gPX0yHZRg9x..v.0GF3F/gQl/2LDfggOfPhI/LTqNPIm',
    ARRAY['ADMIN', 'STAFF']::TEXT[]
);

INSERT INTO user_auth_providers (
    user_id,
    provider,
    provider_subject,
    email
) VALUES (
    'user_aline',
    'PASSWORD',
    'aline@example.com',
    'aline@example.com'
),
(
    'user_michael',
    'PASSWORD',
    'michael@example.com',
    'michael@example.com'
),
(
    'user_staff_claudine',
    'PASSWORD',
    'claudine.staff@example.com',
    'claudine.staff@example.com'
),
(
    'user_admin_orest',
    'PASSWORD',
    'admin@example.com',
    'admin@example.com'
);

INSERT INTO service_places (
    id,
    name,
    kind,
    city,
    country_code,
    address_label,
    map_id
) VALUES (
    'rw-kgl-marriott',
    'Kigali Marriott by Kaze',
    'HOTEL',
    'Kigali',
    'RW',
    'Kigali Marriott',
    'temporary-svg-venue'
);

INSERT INTO hotels (
    id,
    slug,
    market,
    timezone_id,
    display_name,
    primary_hex,
    secondary_hex,
    accent_hex,
    surface_hex,
    background_hex,
    logo_asset,
    wordmark_asset,
    heading_scale,
    body_scale,
    label_scale,
    supported_locales,
    default_currency_code,
    active_experiences
) VALUES (
    'rw-kgl-marriott',
    'kigali-marriott',
    'LUXURY_HOTEL',
    'Africa/Kigali',
    'Kigali Marriott',
    '#2F6970',
    '#B4874F',
    '#D8C6A3',
    '#FCF8F1',
    '#F3EEE5',
    'k_logo.svg',
    'k_logo.svg',
    1.05,
    1.0,
    0.96,
    ARRAY['en', 'fr']::TEXT[],
    'RWF',
    ARRAY['STAY', 'EVENT', 'EXPLORE', 'SERVICE_REQUESTS']::TEXT[]
);

INSERT INTO hotel_buildings (
    id,
    hotel_id,
    name,
    floors
) VALUES (
    'main-tower',
    'rw-kgl-marriott',
    'Main Tower',
    ARRAY['l1', 'l9']::TEXT[]
);

INSERT INTO place_services (
    id,
    place_id,
    title,
    description,
    category,
    pricing_kind,
    amount_label,
    requestable
) VALUES
    (
        'svc_room_key',
        'rw-kgl-marriott',
        'Room access',
        'Use the active pass to confirm guest access to the room and hotel services.',
        'ACCESS',
        'INCLUDED',
        NULL,
        FALSE
    ),
    (
        'svc_late_checkout',
        'rw-kgl-marriott',
        'Late checkout',
        'Ask reception for a later checkout time when room availability allows.',
        'CHECKOUT',
        'REQUIRES_CONFIRMATION',
        NULL,
        TRUE
    ),
    (
        'svc_towels',
        'rw-kgl-marriott',
        'Fresh towels',
        'Request towels or simple room-care support from housekeeping.',
        'ROOM_CARE',
        'INCLUDED',
        NULL,
        TRUE
    ),
    (
        'svc_water',
        'rw-kgl-marriott',
        'Bottled water',
        'Request drinking water for the room or event table.',
        'DRINK',
        'FREE',
        NULL,
        TRUE
    ),
    (
        'svc_breakfast',
        'rw-kgl-marriott',
        'Breakfast',
        'Access breakfast according to the guest stay or event package.',
        'FOOD',
        'REQUIRES_CONFIRMATION',
        NULL,
        TRUE
    ),
    (
        'svc_spa',
        'rw-kgl-marriott',
        'Signature spa treatment',
        'Reserve a wellness session at Ubumwe Spa.',
        'ADD_ON',
        'PAID',
        'From RWF 55,000',
        TRUE
    );

INSERT INTO guests (
    id,
    hotel_id,
    full_name
) VALUES
    ('guest_aline', 'rw-kgl-marriott', 'Aline Uwase'),
    ('guest_michael', 'rw-kgl-marriott', 'Michael Nshuti');

INSERT INTO stays (
    id,
    hotel_id,
    guest_id,
    room_id,
    start_iso_utc,
    end_iso_utc,
    status
) VALUES
    (
        'stay_001',
        'rw-kgl-marriott',
        'guest_aline',
        'room_906',
        '2026-04-03T10:00:00Z',
        '2026-04-06T10:00:00Z',
        'ACTIVE'
    ),
    (
        'stay_002',
        'rw-kgl-marriott',
        'guest_michael',
        'room_512',
        '2026-04-03T10:00:00Z',
        '2026-04-06T10:00:00Z',
        'ACTIVE'
    );

INSERT INTO itinerary_items (
    id,
    stay_id,
    title,
    category,
    status,
    start_iso_utc,
    end_iso_utc,
    venue_node_id,
    venue_floor_id,
    venue_label,
    notes
) VALUES
    (
        'itinerary_aline_spa',
        'stay_001',
        'Signature massage',
        'SPA',
        'CONFIRMED',
        '2026-04-04T12:00:00Z',
        '2026-04-04T13:15:00Z',
        'registration',
        'l1',
        'Ubumwe Spa',
        NULL
    ),
    (
        'itinerary_aline_keynote',
        'stay_001',
        'Opening keynote',
        'EVENT_SESSION',
        'CONFIRMED',
        '2026-04-04T08:00:00Z',
        '2026-04-04T09:15:00Z',
        'keynote-room',
        'l9',
        'Great Rift Ballroom',
        NULL
    ),
    (
        'itinerary_michael_keynote',
        'stay_002',
        'Opening keynote',
        'EVENT_SESSION',
        'CONFIRMED',
        '2026-04-04T08:00:00Z',
        '2026-04-04T09:15:00Z',
        'keynote-room',
        'l9',
        'Great Rift Ballroom',
        NULL
    );

INSERT INTO event_days (
    id,
    hotel_id,
    label,
    date_iso
) VALUES
    ('day1', 'rw-kgl-marriott', 'Fri 3 Apr', '2026-04-03'),
    ('day2', 'rw-kgl-marriott', 'Sat 4 Apr', '2026-04-04'),
    ('day3', 'rw-kgl-marriott', 'Sun 5 Apr', '2026-04-05');

INSERT INTO scheduled_experiences (
    id,
    hotel_id,
    day_id,
    title,
    description,
    start_iso,
    end_iso,
    venue_label,
    host_label
) VALUES
    (
        'session_welcome',
        'rw-kgl-marriott',
        'day1',
        'Welcome reception',
        'Arrival gathering for summit delegates with lounge music and light bites.',
        '2026-04-03T16:00:00Z',
        '2026-04-03T17:00:00Z',
        'Sky Lobby',
        'Guest Relations'
    ),
    (
        'session_keynote',
        'rw-kgl-marriott',
        'day2',
        'Opening keynote',
        'Main plenary session in the Great Rift Ballroom, with map route and speaker details available from the card.',
        '2026-04-04T08:00:00Z',
        '2026-04-04T09:15:00Z',
        'Great Rift Ballroom',
        'Finance Summit'
    ),
    (
        'session_roundtable',
        'rw-kgl-marriott',
        'day2',
        'Private investor roundtable',
        'Invitation-only gathering with live occupancy and room lookup support.',
        '2026-04-04T11:00:00Z',
        '2026-04-04T12:00:00Z',
        'Virunga Room',
        'Executive Office'
    ),
    (
        'session_brunch',
        'rw-kgl-marriott',
        'day3',
        'Farewell brunch',
        'Closing brunch for delegates and hotel guests who opted into the event program.',
        '2026-04-05T10:00:00Z',
        '2026-04-05T11:30:00Z',
        'Kivu Terrace',
        'Events Team'
    );

INSERT INTO amenity_highlights (
    id,
    hotel_id,
    title,
    description,
    location_label,
    availability_label,
    category_label,
    access_label,
    action_label
) VALUES
    (
        'amenity_pool',
        'rw-kgl-marriott',
        'Infinity pool quiet hours',
        'A calmer pool deck period curated for business travelers between meetings.',
        'Pool Deck',
        '06:00 - 09:00',
        'Amenity',
        'Included',
        'Open amenity'
    ),
    (
        'amenity_lobby',
        'rw-kgl-marriott',
        'Lobby art walk',
        'A short self-guided route through the hotel featured Rwandan artists.',
        'Grand Lobby',
        'All day',
        'Self-guided',
        'Complimentary',
        'Start route'
    ),
    (
        'amenity_jazz',
        'rw-kgl-marriott',
        'Evening jazz set',
        'Soft live music in the bar, recommended for summit delegates after sessions.',
        'Panorama Bar',
        '20:00',
        'Evening experience',
        'Extra charge',
        'Reserve table'
    );

INSERT INTO amenity_statuses (
    id,
    hotel_id,
    title,
    location_label,
    status_label,
    hours_label,
    open_now
) VALUES
    (
        'kitchen',
        'rw-kgl-marriott',
        'Hotel kitchen',
        'Back-of-house culinary service',
        'Open now',
        'Open daily until 22:30',
        TRUE
    ),
    (
        'restaurant',
        'rw-kgl-marriott',
        'Kivu Dining',
        'Ground floor',
        'Open now',
        'Breakfast 06:30 - 10:30, lunch and dinner until 22:30',
        TRUE
    ),
    (
        'spa',
        'rw-kgl-marriott',
        'Ubumwe Spa',
        'Wellness level',
        'Open now',
        'Open daily from 09:00 to 21:00',
        TRUE
    ),
    (
        'pool',
        'rw-kgl-marriott',
        'Infinity pool',
        'Pool Deck',
        'Open now',
        'Open daily from 06:00 to 20:00',
        TRUE
    );

INSERT INTO maps (
    id,
    hotel_id,
    name,
    source_format
) VALUES (
    'temporary-svg-venue',
    'rw-kgl-marriott',
    'Temporary SVG Venue Map',
    'SVG'
);

INSERT INTO map_floors (
    id,
    map_id,
    label,
    level_index,
    width,
    height
) VALUES
    ('l1', 'temporary-svg-venue', 'Ground floor', 0, 1200.0, 1200.0),
    ('l9', 'temporary-svg-venue', 'First floor', 1, 1200.0, 1200.0);

INSERT INTO map_nodes (
    id,
    floor_id,
    label,
    kind,
    x,
    y
) VALUES
    ('auditorium-1', 'l1', 'Auditorium, Room 1', 'BALLROOM', 252.0, 924.0),
    ('lightning-room', 'l1', 'Room 5, Lightning talks', 'ROOM', 444.0, 192.0),
    ('room-2', 'l1', 'Room 2', 'ROOM', 360.0, 420.0),
    ('room-3', 'l1', 'Room 3', 'ROOM', 420.0, 420.0),
    ('party', 'l1', 'Party', 'LANDMARK', 816.0, 342.0),
    ('registration', 'l1', 'Registration', 'CONCIERGE', 552.0, 792.0),
    ('room-11b', 'l9', 'Room 11b', 'ROOM', 360.0, 396.0),
    ('room-11a', 'l9', 'Room 11a', 'ROOM', 360.0, 444.0),
    ('room-12b', 'l9', 'Room 12b', 'ROOM', 540.0, 396.0),
    ('room-12a', 'l9', 'Room 12a', 'ROOM', 540.0, 444.0),
    ('room-13a', 'l9', 'Room 13a', 'ROOM', 396.0, 252.0),
    ('room-13b', 'l9', 'Room 13b', 'ROOM', 492.0, 252.0),
    ('keynote-room', 'l9', 'Keynote, Room 14', 'BALLROOM', 864.0, 324.0);

INSERT INTO late_checkout_requests (
    id,
    hotel_id,
    guest_id,
    stay_id,
    room_id,
    checkout_time_iso,
    fee_amount_minor,
    currency_code,
    payment_preference,
    follow_up_preference,
    status,
    note
) VALUES (
    'late_guest_aline_seed_1',
    'rw-kgl-marriott',
    'guest_aline',
    'stay_001',
    'room_906',
    '2026-04-06T14:00:00Z',
    55000,
    'RWF',
    'CHARGE_TO_ROOM',
    'CONFIRM_IN_APP',
    'PENDING',
    'Approval will appear quietly in the app.'
);

INSERT INTO service_requests (
    id,
    hotel_id,
    guest_id,
    stay_id,
    room_id,
    type,
    status,
    note
) VALUES (
    'service_towels_guest_aline_seed_1',
    'rw-kgl-marriott',
    'guest_aline',
    'stay_001',
    'room_906',
    'TOWELS',
    'PENDING',
    'The hotel team has received the request.'
);

COMMIT;
