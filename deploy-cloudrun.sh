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
BUILD_REGION="${BUILD_REGION:-global}"
ARTIFACT_REPOSITORY="${ARTIFACT_REPOSITORY:-kaze-images}"

if [ -z "$PROJECT_ID" ]; then
  echo "Missing PROJECT_ID"
  echo "Set PROJECT_ID in $LOCAL_ENV_FILE or export it before running ./deploy-cloudrun.sh."
  exit 1
fi

IMAGE_REPOSITORY="${IMAGE_REPOSITORY:-$REGION-docker.pkg.dev/$PROJECT_ID/$ARTIFACT_REPOSITORY/$SERVICE_NAME}"
IMAGE_TAG="${IMAGE_TAG:-$(date -u +"%Y%m%d-%H%M%S")}"
IMAGE_URI="${IMAGE_URI:-$IMAGE_REPOSITORY:$IMAGE_TAG}"

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

stream_build_logs() {
  if [ "${RAW_BUILD_LOGS:-0}" = "1" ]; then
    gcloud builds log "$BUILD_ID" \
      --region "$BUILD_REGION" \
      --stream
    return
  fi

  gcloud builds log "$BUILD_ID" \
    --region "$BUILD_REGION" \
    --stream | awk '
      /^gcloud builds log --stream only displays logs from Cloud Storage\./ { next }
      /^gcloud beta builds log --stream$/ { next }
      /^Waiting for build to complete\./ { next }
      /^-+ REMOTE BUILD OUTPUT -+$/ { next }
      /^Fetching storage object:/ { next }
      /^Copying gs:\/\// { next }
      /^Operation completed over / { next }
      /^Already have image \(with digest\):/ { next }
      /^Sending build context to Docker daemon/ { next }
      /^[0-9a-f]+: (Pulling fs layer|Waiting|Verifying Checksum|Download complete|Pull complete|Preparing)$/ { next }
      /^Removing intermediate container / { next }
      /^ ---> [0-9a-f]+$/ { next }
      /^Status: Downloaded newer image for / { next }
      /^Digest: sha256:/ { next }
      /^Welcome to Gradle / { next }
      /^Here are the highlights of this release:$/ { next }
      /^ - / { next }
      /^For more details see https:\/\/docs\.gradle\.org\// { next }
      /^To honour the JVM settings for this build / { next }
      /^Daemon will be stopped at the end of the build$/ { next }
      /^Calculating task graph as no cached configuration is available/ { next }
      /^Type-safe project accessors is an incubating feature\.$/ { next }
      /^\[Incubating\] Problems report is available at:/ { next }
      /^Configuration cache entry stored\.$/ { next }
      /^WARNING: The option setting '\''android\./ { next }
      /^The current default is '\''(true|false)'\''\.$/ { next }
      /^It will be removed in version 10\.0 of the Android Gradle plugin\.$/ { next }
      /^WARNING: The property android\.dependency\.excludeLibraryComponentsFromConstraints/ { next }
      /^To suppress this warning, add android\.generateSyncIssueWhenLibraryConstraintsAreEnabled=false to gradle\.properties$/ { next }
      /^w: file:\/\/\/workspace\/shared\/build\.gradle\.kts:40:1: / { next }
      /^w: ⚠️ The '\''org\.jetbrains\.kotlin\.multiplatform'\'' plugin deprecated compatibility/ { next }
      /^The '\''org\.jetbrains\.kotlin\.multiplatform'\'' plugin is not compatible/ { next }
      /^Solution: Please use the '\''com\.android\.kotlin\.multiplatform\.library'\'' plugin instead of '\''com\.android\.library'\''\.$/ { next }
      /^See https:\/\/kotl\.in\/gradle\/agp-new-kmp for more details\.$/ { next }
      /^Step [0-9]+\/[0-9]+ : / {
        print "";
        print $0;
        next;
      }
      /^(FETCHSOURCE|BUILD|PUSH|DONE|ERROR)$/ {
        print "";
        print "== " $0 " ==";
        next;
      }
      /^BUILD SUCCESSFUL in / {
        print "";
        print $0;
        next;
      }
      /^Successfully built / { print $0; next }
      /^Successfully tagged / { print $0; next }
      /^[a-z0-9]+: Pushed$/ { next }
      /^[0-9]{8}-[0-9]{6}: digest: sha256:/ { print $0; next }
      /^Created \[https:\/\/cloudbuild.googleapis.com\// { next }
      /^Logs are available at \[/ { next }
      { print }
    '
}

cat > "$ENV_FILE" <<EOF
DATABASE_URL: "$(yaml_quote "${DATABASE_URL}")"
KAZE_ENV: "production"
KAZE_DB_SCHEMA_MODE: "$(yaml_quote "${KAZE_DB_SCHEMA_MODE:-none}")"
KAZE_DB_MAXIMUM_POOL_SIZE: "$(yaml_quote "${KAZE_DB_MAXIMUM_POOL_SIZE:-5}")"
KAZE_JWT_SECRET: "$(yaml_quote "${KAZE_JWT_SECRET}")"
KAZE_JWT_REQUIRE_FOR_API: "$(yaml_quote "${KAZE_JWT_REQUIRE_FOR_API:-true}")"
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
KAZE_CORS_ALLOWED_HOSTS: "$(yaml_quote "${KAZE_CORS_ALLOWED_HOSTS:-api.kazerwanda.com,www.kazerwanda.com,kazerwanda.com}")"
EOF

echo "==> Selecting Google Cloud project: $PROJECT_ID"
gcloud config set project "$PROJECT_ID"

echo "==> Ensuring Artifact Registry repository exists"
if gcloud artifacts repositories describe "$ARTIFACT_REPOSITORY" \
  --location "$REGION" >/dev/null 2>&1; then
  echo "    Using existing repository: $ARTIFACT_REPOSITORY"
else
  echo "    Creating repository: $ARTIFACT_REPOSITORY"
  gcloud artifacts repositories create "$ARTIFACT_REPOSITORY" \
    --location "$REGION" \
    --repository-format docker \
    --description "Container images for $SERVICE_NAME"
fi

echo "==> Building container image"
echo "    Image: $IMAGE_URI"
BUILD_ID="$(gcloud builds submit . \
  --tag "$IMAGE_URI" \
  --region "$BUILD_REGION" \
  --async \
  --format='value(id)')"

if [ -z "$BUILD_ID" ]; then
  echo "Could not determine Cloud Build ID."
  exit 1
fi

echo "    Build ID: $BUILD_ID"
echo "    Console: https://console.cloud.google.com/cloud-build/builds/$BUILD_ID?project=$PROJECT_ID"
echo "==> Streaming Cloud Build logs"
stream_build_logs

BUILD_STATUS="$(gcloud builds describe "$BUILD_ID" \
  --region "$BUILD_REGION" \
  --format='value(status)')"

if [ "$BUILD_STATUS" != "SUCCESS" ]; then
  echo "Cloud Build finished with status: $BUILD_STATUS"
  exit 1
fi

echo "==> Deploying image to Cloud Run"
gcloud run deploy "$SERVICE_NAME" \
  --image "$IMAGE_URI" \
  --region "$REGION" \
  --platform managed \
  --allow-unauthenticated \
  --env-vars-file "$ENV_FILE" \
  --memory "${CLOUD_RUN_MEMORY:-1Gi}" \
  --cpu "${CLOUD_RUN_CPU:-1}" \
  --concurrency "${CLOUD_RUN_CONCURRENCY:-40}" \
  --min-instances "${CLOUD_RUN_MIN_INSTANCES:-1}" \
  --max-instances "${CLOUD_RUN_MAX_INSTANCES:-10}" \
  --port 8080
