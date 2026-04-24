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
export DATABASE_URL="${DATABASE_URL:-jdbc:postgresql://localhost:5432/kaze}"
export KAZE_DB_SCHEMA_MODE="${KAZE_DB_SCHEMA_MODE:-none}"
export GOOGLE_OAUTH_CLIENT_ID="${GOOGLE_OAUTH_CLIENT_ID:-}"
export GOOGLE_OAUTH_CLIENT_SECRET="${GOOGLE_OAUTH_CLIENT_SECRET:-}"
export GOOGLE_OAUTH_REDIRECT_URI="${GOOGLE_OAUTH_REDIRECT_URI:-https://kaze-api-338266348516.europe-west1.run.app/api/v1/auth/google/callback}"

SYNC_CLOUD_ON_RUN="${SYNC_CLOUD_ON_RUN:-1}"

if [ "$SYNC_CLOUD_ON_RUN" = "1" ]; then
  if [ -n "${PROJECT_ID:-}" ] && [ -n "${KAZE_JWT_SECRET:-}" ] && [ -x "$ROOT_DIR/deploy-cloudrun.sh" ]; then
    echo "Syncing Cloud Run before local startup..."
    /bin/sh "$ROOT_DIR/deploy-cloudrun.sh"
  else
    echo "Cloud sync skipped. Add PROJECT_ID and KAZE_JWT_SECRET in $ENV_FILE to enable automatic deploy."
  fi
fi

echo "Starting local Kaze server..."
exec ./gradlew :server:run --no-daemon
