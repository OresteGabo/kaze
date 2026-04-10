# Kaze LaTeX Docs

This folder contains printable LaTeX documentation for Kaze.

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
cd /Users/muhirwagabooreste/AndroidStudioProjects/kaze/docs/latex
pdflatex kaze-investor-brief.tex
pdflatex kaze-technical-brief.tex
pdflatex kaze-product-dossier.tex
```

If you want, these can later be split into:
- a formal business plan
- an investor memo
- a systems architecture spec
- a payment/compliance strategy brief
