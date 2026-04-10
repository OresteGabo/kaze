#!/bin/sh
# Author: Oreste Gabo
# Purpose: Remove LaTeX temporary artifacts while keeping generated PDFs.

set -eu

# Resolve paths relative to this script so cleanup works from any cwd.
SCRIPT_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
PDF_DIR="${1:-$SCRIPT_DIR/out}"
BUILD_DIR="${2:-$SCRIPT_DIR/.latex-build}"
ROOT_OUT_DIR="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)/out"

# Use colored status labels only in an interactive terminal.
if [ -t 1 ]; then
  COLOR_RESET="$(printf '\033[0m')"
  COLOR_ACCENT="$(printf '\033[1;38;5;66m')"
  COLOR_LABEL="$(printf '\033[38;5;180m')"
  COLOR_MUTED="$(printf '\033[38;5;245m')"
  COLOR_OK="$(printf '\033[38;5;29m')"
else
  COLOR_RESET=""
  COLOR_ACCENT=""
  COLOR_LABEL=""
  COLOR_MUTED=""
  COLOR_OK=""
fi

printf '%s◆%s %sCleaning generated docs%s\n' \
  "$COLOR_ACCENT" "$COLOR_RESET" "$COLOR_LABEL" "$COLOR_RESET"

# Keep final PDFs in `out`, but remove the temporary LaTeX working directory.
rm -rf "$BUILD_DIR"

# Some manual or IDE builds may write temporary LaTeX files into the project
# root `out/` folder. Delete only the disposable artifacts there, never PDFs.
if [ -d "$ROOT_OUT_DIR" ]; then
  find "$ROOT_OUT_DIR" -type f \( \
    -name '*.aux' -o \
    -name '*.log' -o \
    -name '*.fls' -o \
    -name '*.fdb_latexmk' -o \
    -name '*.out' -o \
    -name '*.toc' -o \
    -name '*.synctex.gz' -o \
    -name '*.listing' \
  \) -delete
fi

printf '%s■%s %sPDF output kept%s\n' \
  "$COLOR_OK" "$COLOR_RESET" "$COLOR_LABEL" "$COLOR_RESET"
printf '%s■%s %sBuild artifacts removed%s\n' \
  "$COLOR_OK" "$COLOR_RESET" "$COLOR_LABEL" "$COLOR_RESET"
if [ -d "$ROOT_OUT_DIR" ]; then
  printf '%s■%s %sStray root out artifacts removed%s\n' \
    "$COLOR_OK" "$COLOR_RESET" "$COLOR_LABEL" "$COLOR_RESET"
fi
printf '%s◆%s %sClean complete%s\n' \
  "$COLOR_ACCENT" "$COLOR_RESET" "$COLOR_LABEL" "$COLOR_RESET"
