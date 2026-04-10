# Kaze LaTeX Cheatsheet

This cheatsheet shows how to create a new `.tex` document that uses the current Kaze PDF theme.

## Fast Start

1. Create a new `.tex` file in `docs/latex`
2. Start from [`kaze-template.tex`](kaze-template.tex)
3. Update the cover content
4. Add sections and callout boxes
5. Build with:

```sh
cd docs/latex
sh build-docs.sh
```

The build script automatically discovers every standalone `.tex` document in this folder.

## Minimum Structure

```tex
\documentclass[11pt,a4paper]{article}
\input{kaze-theme.tex}

\begin{document}

\KazeCover
  {Document Title}
  {Short descriptive subtitle}
  {April 2026}
  {Audience or document type}

\KazePageTitle{Document Scope}
\KazeDocumentNotice

\section{First Section}

Your content here.

\end{document}
```

## Main Theme Commands

### `\KazeCover`

Use it once at the top of the document.

```tex
\KazeCover
  {Document Title}
  {Short descriptive subtitle}
  {April 2026}
  {Investor summary}
```

Arguments:
- title
- subtitle
- date
- audience or document descriptor

### `\KazePageTitle`

Use it near the start of the main body to label the document scope.

```tex
\KazePageTitle{Investor Brief}
```

### `\KazeDocumentNotice`

Adds the standard confidentiality and restricted-use message.

```tex
\KazeDocumentNotice
```

## Callout Boxes

### Highlight Box

Use for important observations, strategic notes, or key messages.

```tex
\begin{kazehighlightbox}
\textbf{Strategic note:} Kaze can reuse venue-map infrastructure across hotels, conferences, and weddings.
\end{kazehighlightbox}
```

### Callout Box With Title

Use for structured notes, summary panels, or boxed explanations.

```tex
\begin{kazecalloutbox}[Why This Matters]
This document helps future developers understand the architecture before reading the codebase.
\end{kazecalloutbox}
```

## Typical Content Patterns

### Normal section

```tex
\section{Executive Summary}

Kaze is a venue experience, access, and commerce platform.
```

### Subsection

```tex
\subsection{Payments}

Kaze can support MTN MoMo, Airtel Money, and other local payment rails.
```

### Itemized list

```tex
\begin{itemize}
    \item hotels
    \item conference venues
    \item wedding venues
\end{itemize}
```

### Table-style content

```tex
\begin{longtable}{p{0.28\textwidth} p{0.64\textwidth}}
\textbf{Area} & \textbf{Why It Matters} \\
Hotels & strong starting market for guest flows and services \\
Conference venues & good fit for bookings and access control \\
\end{longtable}
```

## Naming Suggestions

Use clear file names such as:
- `kaze-investor-memo.tex`
- `kaze-business-plan.tex`
- `kaze-architecture-spec.tex`
- `kaze-partner-brief.tex`

Avoid:
- `doc1.tex`
- `notes.tex`
- `final-final.tex`

## What Not To Do

- do not edit `kaze-theme.tex` unless you are intentionally changing the global document design
- do not hardcode machine-specific paths
- do not create another manual build list in the script
- do not put temporary files in version control

## Recommended Workflow

- duplicate [`kaze-template.tex`](kaze-template.tex)
- rename it
- edit the cover and content
- run `sh build-docs.sh`
- review the PDF in `docs/latex/out`

