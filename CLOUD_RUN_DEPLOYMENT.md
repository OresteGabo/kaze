# Cloud Run Deployment

This backend is ready to deploy to Google Cloud Run.

## What was added

- `Dockerfile`
- `.dockerignore`
- `deploy-cloudrun.sh`

Cloud Run can build directly from source, and because this repo now has a `Dockerfile`, Cloud Run will use that file when you run `gcloud run deploy --source .`.

This project now uses a slightly more explicit deploy flow in `deploy-cloudrun.sh`:

1. build the container image with Cloud Build
2. stream the Cloud Build logs live
3. deploy the built image to Cloud Run

That flow is easier to understand than the default `gcloud run deploy --source .` spinner, which often just repeats "Building Container" without much detail.

## Recommended region

Use `europe-west1`.

Why:

- good Europe/Africa latency balance
- supported by Cloud Run custom domain mapping preview
- simple for first deployment

## Before first deploy

Enable these APIs in your Google Cloud project:

- Cloud Run Admin API
- Cloud Build API
- Artifact Registry API

## Required environment variables

Minimum:

- `PROJECT_ID`
- `DATABASE_URL`
- `KAZE_JWT_SECRET`

For Google login:

- `GOOGLE_OAUTH_CLIENT_ID`
- `GOOGLE_OAUTH_CLIENT_SECRET`
- `GOOGLE_OAUTH_REDIRECT_URI`

## First deploy

```sh
export PROJECT_ID="kaze-backend"
export DATABASE_URL="postgresql://USER:PASSWORD@HOST/DB?sslmode=require"
export KAZE_JWT_SECRET="replace-with-a-long-random-secret"
export GOOGLE_OAUTH_CLIENT_ID="..."
export GOOGLE_OAUTH_CLIENT_SECRET="..."
export GOOGLE_OAUTH_REDIRECT_URI="https://api.kazerwanda.com/api/v1/auth/google/callback"

/bin/sh ./deploy-cloudrun.sh
```

Optional deploy variables:

- `BUILD_REGION` to control Cloud Build region, default `global`
- `ARTIFACT_REPOSITORY` to control the Artifact Registry Docker repository name, default `kaze-images`
- `IMAGE_REPOSITORY` to override the pushed image repository
- `IMAGE_TAG` to override the generated timestamp tag
- `IMAGE_URI` to override the full image path directly

By default, the deploy script now pushes to Artifact Registry using a repository like:

- `europe-west1-docker.pkg.dev/PROJECT_ID/kaze-images/kaze-api:TIMESTAMP`

This is usually more reliable than relying on `gcr.io/...` create-on-push behavior.

After deployment, Cloud Run gives you a public `run.app` URL.

Test it first before mapping your domain.

## Custom domain

Recommended by Google for production: use a global external Application Load Balancer in front of Cloud Run.

Reference:

- https://docs.cloud.google.com/run/docs/mapping-custom-domains

For a fast dev setup, you can still first test with the default Cloud Run URL, then later map:

- `api.kazerwanda.com`

## Google OAuth note

Your Google OAuth redirect URI must exactly match the deployed backend callback URL. If you deploy first on the default Cloud Run URL, you may want to temporarily add that callback URL in Google Cloud Console too.

## Suggested order

1. Deploy backend to Cloud Run
2. Test with the default `run.app` URL
3. Add Google OAuth callback for the deployed URL
4. Verify login works
5. Map `api.kazerwanda.com`
6. Update OAuth callback to the final custom domain
