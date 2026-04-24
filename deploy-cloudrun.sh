#!/bin/sh

set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

LOCAL_ENV_FILE="${ENV_FILE:-$ROOT_DIR/.env.server.local}"

if [ -f "$LOCAL_ENV_FILE" ]; then
  # shellcheck disable=SC1090
  . "$LOCAL_ENV_FILE"
fi

SERVICE_NAME="${SERVICE_NAME:-kaze-api}"
REGION="${REGION:-europe-west1}"
PROJECT_ID="${PROJECT_ID:-}"

if [ -z "$PROJECT_ID" ]; then
  echo "Missing PROJECT_ID"
  echo "Set PROJECT_ID in $LOCAL_ENV_FILE or export it before running ./deploy-cloudrun.sh."
  exit 1
fi

if [ -z "${DATABASE_URL:-}" ]; then
  echo "Missing DATABASE_URL"
  echo "Set DATABASE_URL in $LOCAL_ENV_FILE or export it before deploying."
  exit 1
fi

if [ -z "${KAZE_JWT_SECRET:-}" ]; then
  echo "Missing KAZE_JWT_SECRET"
  echo "Set KAZE_JWT_SECRET in $LOCAL_ENV_FILE or export it before deploying."
  exit 1
fi

ENV_FILE="$(mktemp)"
trap 'rm -f "$ENV_FILE"' EXIT

yaml_quote() {
  printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'
}

cat > "$ENV_FILE" <<EOF
DATABASE_URL: "$(yaml_quote "${DATABASE_URL}")"
KAZE_DB_SCHEMA_MODE: "$(yaml_quote "${KAZE_DB_SCHEMA_MODE:-none}")"
KAZE_JWT_SECRET: "$(yaml_quote "${KAZE_JWT_SECRET}")"
KAZE_JWT_ISSUER: "$(yaml_quote "${KAZE_JWT_ISSUER:-kaze-api}")"
KAZE_JWT_AUDIENCE: "$(yaml_quote "${KAZE_JWT_AUDIENCE:-kaze-mobile}")"
KAZE_AUTH_APP_DEEP_LINK_REDIRECT: "$(yaml_quote "${KAZE_AUTH_APP_DEEP_LINK_REDIRECT:-kaze://auth/callback}")"
GOOGLE_OAUTH_CLIENT_ID: "$(yaml_quote "${GOOGLE_OAUTH_CLIENT_ID:-}")"
GOOGLE_OAUTH_CLIENT_SECRET: "$(yaml_quote "${GOOGLE_OAUTH_CLIENT_SECRET:-}")"
GOOGLE_OAUTH_REDIRECT_URI: "$(yaml_quote "${GOOGLE_OAUTH_REDIRECT_URI:-}")"
APPLE_SERVICE_ID: "$(yaml_quote "${APPLE_SERVICE_ID:-}")"
APPLE_TEAM_ID: "$(yaml_quote "${APPLE_TEAM_ID:-}")"
APPLE_KEY_ID: "$(yaml_quote "${APPLE_KEY_ID:-}")"
APPLE_PRIVATE_KEY_PEM: "$(yaml_quote "${APPLE_PRIVATE_KEY_PEM:-}")"
APPLE_REDIRECT_URI: "$(yaml_quote "${APPLE_REDIRECT_URI:-}")"
FACEBOOK_APP_ID: "$(yaml_quote "${FACEBOOK_APP_ID:-}")"
FACEBOOK_APP_SECRET: "$(yaml_quote "${FACEBOOK_APP_SECRET:-}")"
FACEBOOK_REDIRECT_URI: "$(yaml_quote "${FACEBOOK_REDIRECT_URI:-}")"
KAZE_CORS_ALLOWED_HOSTS: "$(yaml_quote "${KAZE_CORS_ALLOWED_HOSTS:-api.kazerwanda.com,www.kazerwanda.com,kazerwanda.com,localhost:8080,10.0.2.2:8080}")"
EOF

gcloud config set project "$PROJECT_ID"

gcloud run deploy "$SERVICE_NAME" \
  --source . \
  --region "$REGION" \
  --platform managed \
  --allow-unauthenticated \
  --env-vars-file "$ENV_FILE" \
  --memory "${CLOUD_RUN_MEMORY:-1Gi}" \
  --cpu "${CLOUD_RUN_CPU:-1}" \
  --min-instances "${CLOUD_RUN_MIN_INSTANCES:-0}" \
  --max-instances "${CLOUD_RUN_MAX_INSTANCES:-5}" \
  --port 8080
