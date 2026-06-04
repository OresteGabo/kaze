# Kaze Environments

## Purpose

Kaze does not treat every push to `main` as a production release.

The goal is to separate local work, development testing, staging validation, and production users so mistakes are easier to catch before they affect real customers.

## Current Environment Setup

The repository now defines four operating environments:
- local
- development
- staging
- production

Each deployed environment uses its own tracked reference file and an ignored local secrets file.

### Local

Use this on your laptop.

Purpose:
- fast development
- experiments
- local debugging
- no accidental Cloud Run deploy

Files:
- `.env.server.reference`
- `.env.server.local`

Run:

```sh
./run-server.sh
```

By default, local startup does not deploy Cloud Run. To intentionally sync Cloud Run before local startup, set:

```sh
SYNC_CLOUD_ON_RUN=1 ./run-server.sh
```

Use that only when you really mean it.

### Development

Use this for cloud testing before staging.

Cloud Run service:

```text
kaze-api-dev
```

Files:
- `.env.server.dev.reference`
- `.env.server.dev.local`

Deploy:

```sh
ENV_FILE=.env.server.dev.local ./deploy-cloudrun.sh
```

Use development for:
- testing backend changes in the cloud
- checking OAuth redirect behavior
- testing with a disposable database
- trying risky changes before staging

### Staging

Use this as the production rehearsal environment.

Cloud Run service:

```text
kaze-api-staging
```

Files:
- `.env.server.staging.reference`
- `.env.server.staging.local`

Deploy:

```sh
ENV_FILE=.env.server.staging.local ./deploy-cloudrun.sh
```

Use staging for:
- final testing before production
- testing mobile builds against a real backend
- verifying payments, OAuth, reservations, and invitation flows before launch
- demos with trusted testers

### Production

Use this for real users only.

Cloud Run service:

```text
kaze-api
```

Files:
- `.env.server.production.reference`
- `.env.server.production.local`

Deploy:

```sh
ENV_FILE=.env.server.production.local ./deploy-cloudrun.sh
```

Use production for:
- real customers
- real venues
- real service providers
- real event data

Production deploys are manual until the release process is mature.

## Branch Strategy

Current branch flow:

```text
feature/... -> develop -> staging test -> main -> production deploy
```

Branch meanings:
- `feature/...`: one focused change
- `develop`: active development branch
- `main`: production-ready code only

Do not treat every push to `main` as an automatic production deploy while Kaze is still early.

## Database Rule

Each environment has its own database.

Implemented split:

```text
kaze_dev
kaze_staging
kaze_production
```

Never use the production database for local development or staging tests.

## Secrets Rule

Never commit real secrets.

Ignored local files:
- `.env.server.local`
- `.env.server.dev.local`
- `.env.server.staging.local`
- `.env.server.production.local`
- `export-config.sh`

Tracked reference files:
- `.env.server.reference`
- `.env.server.dev.reference`
- `.env.server.staging.reference`
- `.env.server.production.reference`

If a real database URL, token, or secret was ever pushed to GitHub, rotate it immediately in the provider dashboard.

## Payment And OAuth Rule

Each deployed environment has its own callback URLs.

Examples:
- `https://dev.api.kazerwanda.com/api/v1/auth/google/callback`
- `https://staging.api.kazerwanda.com/api/v1/auth/google/callback`
- `https://api.kazerwanda.com/api/v1/auth/google/callback`

The same applies to Apple and Facebook callback URLs.

If custom domains are not mapped yet, use the Cloud Run `run.app` URL for the callback temporarily and include that same host in `KAZE_CORS_ALLOWED_HOSTS`.

Payment providers are configured by environment. Do not test payment flows against production accounts until the checkout and reconciliation rules are ready.

## Cloud Run Safety

Cloud Run sets `K_SERVICE`, so the backend treats any Cloud Run service as production-like for safety checks.

That means development and staging Cloud Run services still need:
- `KAZE_JWT_REQUIRE_FOR_API=true`
- a strong `KAZE_JWT_SECRET`
- non-destructive `KAZE_DB_SCHEMA_MODE`
- explicit non-local `KAZE_CORS_ALLOWED_HOSTS`

This is intentional. A cloud environment is treated as safer and stricter than a laptop.

## Setup Checklist

1. Create separate dev, staging, and production databases.
2. Copy each tracked reference file to its matching ignored local file.
3. Fill each ignored local file with real values.
4. Deploy development first.
5. Deploy staging after development works.
6. Deploy production only after staging is verified.
7. Add GitHub branch protection for `main`.
8. Later, add GitHub Environments with required approval for production deploys.
