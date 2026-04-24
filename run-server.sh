#!/bin/sh

set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

ENV_FILE="${ENV_FILE:-$ROOT_DIR/.env.server.local}"

if [ -f "$ENV_FILE" ]; then
  # shellcheck disable=SC1090
  . "$ENV_FILE"
fi

if [ -z "${JAVA_HOME:-}" ] && [ -x "/usr/libexec/java_home" ]; then
  JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
  export JAVA_HOME
fi

if [ -z "${JAVA_HOME:-}" ] && ! command -v java >/dev/null 2>&1; then
  echo "JAVA_HOME is not set and no 'java' command was found on PATH."
  echo "Install Java 21 or export JAVA_HOME in $ENV_FILE."
  exit 1
fi

SYNC_CLOUD_ON_RUN="${SYNC_CLOUD_ON_RUN:-1}"

if [ "$SYNC_CLOUD_ON_RUN" = "1" ]; then
  if [ -n "${PROJECT_ID:-}" ] && [ -n "${DATABASE_URL:-}" ] && [ -n "${KAZE_JWT_SECRET:-}" ] && [ -x "$ROOT_DIR/deploy-cloudrun.sh" ]; then
    echo "Syncing Cloud Run before local startup..."
    /bin/sh "$ROOT_DIR/deploy-cloudrun.sh"
  else
    echo "Cloud sync skipped. Add PROJECT_ID, DATABASE_URL, and KAZE_JWT_SECRET in $ENV_FILE to enable automatic deploy."
  fi
fi

export DATABASE_URL="${DATABASE_URL:-jdbc:postgresql://localhost:5432/kaze}"
export KAZE_DB_SCHEMA_MODE="${KAZE_DB_SCHEMA_MODE:-none}"
export KAZE_JWT_SECRET="${KAZE_JWT_SECRET:-}"
export KAZE_JWT_ISSUER="${KAZE_JWT_ISSUER:-kaze-api}"
export KAZE_JWT_AUDIENCE="${KAZE_JWT_AUDIENCE:-kaze-mobile}"
export KAZE_AUTH_APP_DEEP_LINK_REDIRECT="${KAZE_AUTH_APP_DEEP_LINK_REDIRECT:-kaze://auth/callback}"
export GOOGLE_OAUTH_CLIENT_ID="${GOOGLE_OAUTH_CLIENT_ID:-}"
export GOOGLE_OAUTH_CLIENT_SECRET="${GOOGLE_OAUTH_CLIENT_SECRET:-}"
export GOOGLE_OAUTH_REDIRECT_URI="${GOOGLE_OAUTH_REDIRECT_URI:-https://kaze-api-338266348516.europe-west1.run.app/api/v1/auth/google/callback}"
export APPLE_SERVICE_ID="${APPLE_SERVICE_ID:-}"
export APPLE_TEAM_ID="${APPLE_TEAM_ID:-}"
export APPLE_KEY_ID="${APPLE_KEY_ID:-}"
export APPLE_PRIVATE_KEY_PEM="${APPLE_PRIVATE_KEY_PEM:-}"
export APPLE_REDIRECT_URI="${APPLE_REDIRECT_URI:-}"
export FACEBOOK_APP_ID="${FACEBOOK_APP_ID:-}"
export FACEBOOK_APP_SECRET="${FACEBOOK_APP_SECRET:-}"
export FACEBOOK_REDIRECT_URI="${FACEBOOK_REDIRECT_URI:-}"
export KAZE_CORS_ALLOWED_HOSTS="${KAZE_CORS_ALLOWED_HOSTS:-api.kazerwanda.com,www.kazerwanda.com,kazerwanda.com,localhost:8080,10.0.2.2:8080}"

echo "Starting local Kaze server..."
exec ./gradlew :server:run --no-daemon
