#!/bin/sh
# Author: Oreste Gabo
# Purpose: Build every standalone LaTeX document in this folder, show friendly
# terminal progress, and then clean temporary artifacts while keeping final PDFs.

set -eu

# Resolve paths relative to this script so the command works from any cwd.
SCRIPT_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
PDF_DIR="${1:-$SCRIPT_DIR/out}"
BUILD_DIR="${2:-$SCRIPT_DIR/.latex-build}"
LOG_DIR="$BUILD_DIR/logs"

# Keep the companion cleanup script executable for normal `./clean-docs.sh` use.
chmod +x "$SCRIPT_DIR/clean-docs.sh" 2>/dev/null || true

# Use colors and animated progress only when stdout is a real terminal.
if [ -t 1 ]; then
  COLOR_RESET="$(printf '\033[0m')"
  COLOR_FILLED="$(printf '\033[38;5;29m')"
  COLOR_EMPTY="$(printf '\033[38;5;245m')"
  COLOR_LABEL="$(printf '\033[38;5;180m')"
  COLOR_COUNT="$(printf '\033[1;38;5;66m')"
  USE_DYNAMIC_BAR=1
else
  COLOR_RESET=""
  COLOR_FILLED=""
  COLOR_EMPTY=""
  COLOR_LABEL=""
  COLOR_COUNT=""
  USE_DYNAMIC_BAR=0
fi

mkdir -p "$PDF_DIR" "$BUILD_DIR" "$LOG_DIR"

# Draw a compact block-based bar for the current document.
build_bar() {
  filled="$1"
  width=24
  empty=$((width - filled))

  filled_bar=""
  i=0
  while [ "$i" -lt "$filled" ]; do
    filled_bar="${filled_bar}■"
    i=$((i + 1))
  done

  empty_bar=""
  i=0
  while [ "$i" -lt "$empty" ]; do
    empty_bar="${empty_bar}□"
    i=$((i + 1))
  done

  printf '%s[%s%s%s%s]%s' \
    "$COLOR_EMPTY" \
    "$COLOR_FILLED" "$filled_bar" \
    "$COLOR_EMPTY" "$empty_bar" \
    "$COLOR_RESET"
}

# Print the final settled line for one completed document.
print_done_step() {
  current="$1"
  total="$2"
  label="$3"
  bar="$(build_bar 24)"
  printf '%s  %s100%%%s  %s%s/%s%s  %s%s%s\n' \
    "$bar" \
    "$COLOR_COUNT" "$COLOR_RESET" \
    "$COLOR_COUNT" "$current" "$total" "$COLOR_RESET" \
    "$COLOR_LABEL" "$label" "$COLOR_RESET"
}

# Run one build command quietly, animate progress while it is running, and
# print the saved log only if the command fails.
run_with_progress() {
  current="$1"
  total="$2"
  label="$3"
  log_file="$4"
  shift 4

  "$@" >"$log_file" 2>&1 &
  cmd_pid=$!

  if [ "$USE_DYNAMIC_BAR" -eq 1 ]; then
    frame=0
    while kill -0 "$cmd_pid" 2>/dev/null; do
      filled=$(( (frame % 18) + 4 ))
      bar="$(build_bar "$filled")"
      printf '\r%s  %s...%s  %s%s/%s%s  %s%s%s' \
        "$bar" \
        "$COLOR_COUNT" "$COLOR_RESET" \
        "$COLOR_COUNT" "$current" "$total" "$COLOR_RESET" \
        "$COLOR_LABEL" "$label" "$COLOR_RESET"
      frame=$((frame + 1))
      sleep 0.12
    done
    wait "$cmd_pid" || {
      printf '\nBuild failed for %s\n' "$label" >&2
      printf 'Log: %s\n\n' "$log_file" >&2
      cat "$log_file" >&2
      exit 1
    }
    printf '\r'
    print_done_step "$current" "$total" "$label"
  else
    if ! wait "$cmd_pid"; then
      printf '\nBuild failed for %s\n' "$label" >&2
      printf 'Log: %s\n\n' "$log_file" >&2
      cat "$log_file" >&2
      exit 1
    fi
    print_done_step "$current" "$total" "$label"
  fi
}

# Auto-discover buildable docs by looking for real LaTeX entry files.
# Helper/theme files like `kaze-theme.tex` are ignored automatically.
discover_docs() {
  found=0
  for tex_file in "$SCRIPT_DIR"/*.tex; do
    [ -e "$tex_file" ] || continue
    if grep -q '\\documentclass' "$tex_file"; then
      found=1
      basename "$tex_file" .tex
    fi
  done

  if [ "$found" -eq 0 ]; then
    printf 'No standalone LaTeX documents found in %s\n' "$SCRIPT_DIR" >&2
    exit 1
  fi
}

DOCS="$(discover_docs)"
DOC_COUNT="$(printf '%s\n' "$DOCS" | wc -l | tr -d ' ')"

# Replace the previous PDF set with a fresh build.
rm -f "$PDF_DIR"/*.pdf

# Preferred path: latexmk handles references and reruns cleanly.
build_with_latexmk() {
  current="$1"
  total="$2"
  doc="$3"
  log_file="$LOG_DIR/$doc.log"
  run_with_progress "$current" "$total" "$doc.pdf" "$log_file" \
    latexmk \
    -cd \
    -pdf \
    -silent \
    -interaction=nonstopmode \
    -halt-on-error \
    -outdir="$BUILD_DIR" \
    -auxdir="$BUILD_DIR" \
    "$SCRIPT_DIR/$doc.tex"
  mv "$BUILD_DIR/$doc.pdf" "$PDF_DIR/$doc.pdf"
}

# Fallback path when latexmk is not installed.
build_with_pdflatex() {
  current="$1"
  total="$2"
  doc="$3"
  log_file="$LOG_DIR/$doc.log"
  run_with_progress "$current" "$total" "$doc.pdf" "$log_file" \
    sh -c "cd \"$SCRIPT_DIR\" && pdflatex -interaction=nonstopmode -halt-on-error -output-directory=\"$BUILD_DIR\" \"$doc.tex\" && pdflatex -interaction=nonstopmode -halt-on-error -output-directory=\"$BUILD_DIR\" \"$doc.tex\""
  mv "$BUILD_DIR/$doc.pdf" "$PDF_DIR/$doc.pdf"
}

step=0
if command -v latexmk >/dev/null 2>&1; then
  for doc in $DOCS; do
    step=$((step + 1))
    build_with_latexmk "$step" "$DOC_COUNT" "$doc"
  done
else
  for doc in $DOCS; do
    step=$((step + 1))
    build_with_pdflatex "$step" "$DOC_COUNT" "$doc"
  done
fi

printf '\n'
# Clean temporary artifacts immediately after the build, but keep final PDFs.
"$SCRIPT_DIR/clean-docs.sh" "$PDF_DIR" "$BUILD_DIR"
