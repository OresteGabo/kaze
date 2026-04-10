#!/bin/sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
PDF_DIR="${1:-$SCRIPT_DIR/out}"
BUILD_DIR="${2:-$SCRIPT_DIR/.latex-build}"

mkdir -p "$PDF_DIR" "$BUILD_DIR"

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

rm -f "$PDF_DIR"/*.pdf

build_with_latexmk() {
  doc="$1"
  latexmk \
    -pdf \
    -interaction=nonstopmode \
    -halt-on-error \
    -outdir="$BUILD_DIR" \
    -auxdir="$BUILD_DIR" \
    "$SCRIPT_DIR/$doc.tex"
  mv "$BUILD_DIR/$doc.pdf" "$PDF_DIR/$doc.pdf"
}

build_with_pdflatex() {
  doc="$1"
  pdflatex -interaction=nonstopmode -halt-on-error -output-directory="$BUILD_DIR" "$SCRIPT_DIR/$doc.tex"
  pdflatex -interaction=nonstopmode -halt-on-error -output-directory="$BUILD_DIR" "$SCRIPT_DIR/$doc.tex"
  mv "$BUILD_DIR/$doc.pdf" "$PDF_DIR/$doc.pdf"
}

if command -v latexmk >/dev/null 2>&1; then
  for doc in $DOCS; do
    build_with_latexmk "$doc"
  done
else
  for doc in $DOCS; do
    build_with_pdflatex "$doc"
  done
fi

printf 'Built PDFs in %s\n' "$PDF_DIR"
printf 'Build artifacts in %s\n' "$BUILD_DIR"
