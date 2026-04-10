# Kaze LaTeX Docs

This folder contains printable LaTeX documentation for Kaze.

Helpful starter files:
- [CHEATSHEET.md](CHEATSHEET.md)
- [kaze-cheatsheet.tex](kaze-cheatsheet.tex)
- [kaze-template.tex](kaze-template.tex)

Current documents:
- `kaze-investor-brief.tex`
  - investor and partner-facing overview
  - useful for fundraising, partnerships, and strategic discussions
- `kaze-technical-brief.tex`
  - architecture and platform overview for future developers and technical stakeholders
  - useful before diving into the codebase
- `kaze-product-dossier.tex`
  - a fuller internal product dossier combining product, platform, and revenue direction

Suggested uses for LaTeX/PDF docs in this project:
- investor decks turned into printable briefs
- technical onboarding packs for future hires
- architecture review documents
- partner/sales leave-behinds
- pilot proposals for hotels, venues, and government clients
- procurement responses
- internal design and product strategy memos
- due-diligence material for grants or accelerators

Example compile commands:

```sh
cd docs/latex
pdflatex kaze-investor-brief.tex
pdflatex kaze-technical-brief.tex
pdflatex kaze-product-dossier.tex
```

Recommended scripted build:

```sh
cd docs/latex
./build-docs.sh
```

Immediate cleanup after a build:

```sh
./clean-docs.sh
```

The cleanup script removes temporary LaTeX artifacts while keeping the final PDFs in `docs/latex/out`.
It also removes stray LaTeX temporary files that some manual/IDE builds may write into the project-root `out/` folder, while leaving PDF files there untouched.
The build script already calls the cleanup step automatically after a successful build.

The script deletes the current generated PDFs first, then rebuilds fresh copies into the output folder.
It automatically builds every standalone `.tex` document in this folder, so you do not need to maintain a manual file list in the script.
It also keeps terminal output clean by showing per-document progress instead of raw TeX engine internals.
If a document fails, the script prints the saved log for that document.

This writes the generated files into:

```text
docs/latex/out
```

Temporary LaTeX build artifacts such as `.aux`, `.log`, `.fls`, `.fdb_latexmk`, and `.out` are written into:

```text
docs/latex/.latex-build
```

Per-document build logs are written into:

```text
docs/latex/.latex-build/logs
```

If you want to remove temporary LaTeX artifacts without touching the generated PDFs, use:

```sh
./clean-docs.sh
```

You can also choose a different output folder:

```sh
sh build-docs.sh out
```

Or choose both PDF and artifact directories:

```sh
sh build-docs.sh out .latex-build
```

The build script is the source of truth for output paths. If your IDE still writes PDFs to a project-level `out/` directory, that usually means it is using its own LaTeX runner instead of [build-docs.sh](build-docs.sh).

If you want, these can later be split into:
- a formal business plan
- an investor memo
- a systems architecture spec
- a payment/compliance strategy brief
