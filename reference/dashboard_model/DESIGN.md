---
name: Telemetry Operations Console
colors:
  surface: '#031427'
  surface-dim: '#031427'
  surface-bright: '#2a3a4f'
  surface-container-lowest: '#000f21'
  surface-container-low: '#0b1c30'
  surface-container: '#102034'
  surface-container-high: '#1b2b3f'
  surface-container-highest: '#26364a'
  on-surface: '#d3e4fe'
  on-surface-variant: '#c6c6cd'
  inverse-surface: '#d3e4fe'
  inverse-on-surface: '#213145'
  outline: '#909097'
  outline-variant: '#45464d'
  surface-tint: '#bec6e0'
  primary: '#bec6e0'
  on-primary: '#283044'
  primary-container: '#0f172a'
  on-primary-container: '#798098'
  inverse-primary: '#565e74'
  secondary: '#b4c5ff'
  on-secondary: '#002a78'
  secondary-container: '#0053db'
  on-secondary-container: '#cdd7ff'
  tertiary: '#4edea3'
  on-tertiary: '#003824'
  tertiary-container: '#001c10'
  on-tertiary-container: '#009365'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#dae2fd'
  primary-fixed-dim: '#bec6e0'
  on-primary-fixed: '#131b2e'
  on-primary-fixed-variant: '#3f465c'
  secondary-fixed: '#dbe1ff'
  secondary-fixed-dim: '#b4c5ff'
  on-secondary-fixed: '#00174b'
  on-secondary-fixed-variant: '#003ea8'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#031427'
  on-background: '#d3e4fe'
  surface-variant: '#26364a'
typography:
  headline-xl:
    fontFamily: Space Grotesk
    fontSize: 2rem
    fontWeight: '700'
    lineHeight: 2.25rem
    letterSpacing: -0.03em
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 1.5rem
    fontWeight: '600'
    lineHeight: 1.875rem
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Space Grotesk
    fontSize: 1.25rem
    fontWeight: '600'
    lineHeight: 1.625rem
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Space Grotesk
    fontSize: 1rem
    fontWeight: '600'
    lineHeight: 1.375rem
    letterSpacing: 0em
  body-lg:
    fontFamily: Geist
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: 1.5rem
  body-md:
    fontFamily: Geist
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: 1.25rem
  body-sm:
    fontFamily: Geist
    fontSize: 0.75rem
    fontWeight: '400'
    lineHeight: 1rem
  label-lg:
    fontFamily: Geist
    fontSize: 0.875rem
    fontWeight: '500'
    lineHeight: 1.25rem
    letterSpacing: 0.01em
  label-md:
    fontFamily: Geist
    fontSize: 0.75rem
    fontWeight: '600'
    lineHeight: 1rem
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Geist
    fontSize: 0.6875rem
    fontWeight: '700'
    lineHeight: 0.875rem
    letterSpacing: 0.08em
  telemetry-num:
    fontFamily: Space Grotesk
    fontSize: 1.125rem
    fontWeight: '700'
    lineHeight: 1.25rem
    letterSpacing: 0.02em
spacing:
  gutter: 1rem
  margin: 1.25rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system delivers a high-density, mission-critical operations environment engineered for high-throughput desktop supervision. Drawing aesthetic and structural cues from industrial control rooms and aerospace telemetry terminals, the UI prioritizes instantaneous status legibility, rigid spatial predictability, and absolute visual precision.

The visual execution is uncompromising: strict zero-radius planar geometries, crisp monoline structural separators, and an intense high-contrast dark telemetry theme rooted in `#0F172A`. Dynamic states (parking bay allocations, ticketing events, sensor readings, and transactional totals in Ariary) cut sharply through deep oceanic slates using luminous signal accents. Every pixel serves a functional duty: eliminating ambiguity, accelerating decision velocity for operational dispatchers, and maintaining legibility during extended, multi-monitor surveillance shifts.

## Colors

The color palette establishes an uncompromising operations console hierarchy calibrated for deep slate backgrounds with high-visibility signal lights:

- **Canvas & Structural Bases**:
  - `bg-terminal`: `#0B0F19` (Root viewport, frame backdrops)
  - `surface-base`: `#0F172A` (Primary canvas panels, table viewports, modal viewports)
  - `surface-elevated`: `#1E293B` (Command sidebars, card containers, dialog toolbars)
  - `surface-highlight`: `#334155` (Hover registers, active cell highlights, table header rows)

- **Accents & Action Signals**:
  - `primary-action`: `#2563EB` (Primary interactions, selected menu tiles, focused control perimeters)
  - `action-hover`: `#1D4ED8` (Depressed/hovered states)
  - `action-subtle`: `rgba(37, 99, 235, 0.12)` (Selection indicator fills, active row backdrops)

- **Monoline Separators**:
  - `border-subtle`: `#1E293B` (Internal data splits, gridlines)
  - `border-strong`: `#334155` (Card perimeter frames, dock boundaries)
  - `border-focus`: `#38BDF8` (Active input and focused selector frames)

- **Telemetry & Status Indicators**:
  - `status-success`: `#10B981` / `#16A34A` (Bay Available / "Disponible", Transaction Complete / "Terminé")
  - `status-warning`: `#F59E0B` / `#D97706` (Occupied / "Occupée", Pending / "En cours")
  - `status-danger`: `#EF4444` / `#DC2626` (Fault / "Erreur", Revocation / "Suppression")
  - `status-info`: `#38BDF8` (Sensor stream active, audit log note)

- **Text & Numeric Hierarchy**:
  - `text-primary`: `#F8FAFC` (High-contrast operational values, headers, monoline telemetry)
  - `text-secondary`: `#94A3B8` (Metadata labels, table headers, currency denominator `Ar`)
  - `text-muted`: `#64748B` (Inactive switches, placeholder indicators)

## Typography

Typography functions as an analytical instrument. Space Grotesk commands structural headlines, key metrics, and financial figures, imparting a machined, modernist presence. Geist handles all analytical data tables, system dialogs, state logs, and component controls, ensuring optimal horizontal character tracking and high glyph distinctiveness across dense matrices.

- **Tabular Figures & Metrics**: All values representing parking capacity counts, time stamps, durations, and currency amounts (`Ar`) must utilize proportional tabular lining to prevent layout stutter during real-time repaints.
- **Micro-Labels**: Operational labels (e.g., `ZONE ID`, `RATE/HR`, `BAY STATUS`) use `label-sm` rendered exclusively in uppercase with tracking at `0.08em` for instantaneous peripheral recognition.
- **Desktop Swing Font Rendering**: Anti-aliasing must be forced via `RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON` with fractional metrics enabled.

## Layout & Spacing

The spatial architecture is founded on a 4px/8px modular telemetry grid tailored for workstation-class displays (1080p, 1440p, 4K):

- **Dock Layout Architecture**:
  - **Navigation Master Rail / Sidebar**: Fixed at `240px` (standard) or collapsible to `64px` (dense monitoring mode).
  - **Telemetry Canvas**: Fluid workspace hosting multi-split panels (`JSplitPane`) divided by 1px hairline gutters.
  - **Status & Control Dock**: Top horizontal bar locked at `48px` height containing system clock, connectivity beacon, operator ID, and real-time revenue tallies.
- **Density Classes**:
  - `space-xs` (4px): Micro gaps between status pills and metric units.
  - `space-sm` (8px): Form input padding, table cell vertical offsets, card header spacing.
  - `space-md` (12px): Standard desktop component spacing, card inner padding.
  - `space-lg` (16px): Layout grid column separation, primary panel margins.
  - `space-xl` (24px): Boundary separations between major modular docks.

## Elevation & Depth

This system avoids blurred drop shadows and skeuomorphic illusions in favor of **Monoline Tonal Stratification**. Depth is communicated exclusively through surface luminance variation and 1px rectilinear structural borders:

- **Level 0 (Base Canvas)**: `#0B0F19` — Deepest structural base.
- **Level 1 (Card & Module Deck)**: `#0F172A` with a 1px solid border of `#1E293B`.
- **Level 2 (Active Toolbars, Floating Modals & Flyouts)**: `#1E293B` surrounded by a continuous 1px solid border of `#334155`.
- **Level 3 (Focused Overlays & Alert Notices)**: `#1E293B` reinforced with a 1px solid accent border (`#2563EB` or `#EF4444`).
- **Focus & Selection**: Outlines do not expand outward via fuzzy drop shadows; they are drawn as crisp, high-contrast interior inset borders or contiguous 1px contrast rings.

## Shapes

The design system enforces **strictly sharp geometries** with zero corner rounding (`0px`).

Every boundary, container, button, table header, status tag, and dialog corner terminates at a razor-sharp 90-degree angle. This geometric discipline reinforces industrial instrumentation standards, maximizes visible information density, eliminates wasted corner sub-pixels, and maps 1:1 to native desktop raster engines without edge blurring.

## Components

### Buttons & Trigger Controls
- **Primary Action**: Solid `#2563EB` fill, `#F8FAFC` text, `0px` radius, `0 12px` horizontal padding, height `32px` (compact) or `36px` (standard). Hover: `#1D4ED8`. Active: `#1E40AF`.
- **Secondary / Peripheral Action**: Transparent background, 1px solid `#334155` border, `#F8FAFC` text. Hover: `#1E293B` background with `#475569` border.
- **Destructive Trigger**: 1px solid `#DC2626` outline or solid `#DC2626` background, `#F8FAFC` text.
- **Icon Actions**: 32x32px square buttons, 1px `#1E293B` border, centered crisp glyph.

### Status Telemetry Badges & Chips
- Sharp 0px rectangular pill replacement: height `20px`, padding `0 6px`.
- Structure: 1px hairline perimeter with matching 10% tinted alpha background.
- **Disponible / Terminé**: Background `rgba(16, 185, 129, 0.12)`, border `#10B981`, text `#34D399`. Includes a 4x4px solid `#10B981` square indicator.
- **Occupée / En cours**: Background `rgba(245, 158, 11, 0.12)`, border `#F59E0B`, text `#FBBF24`. Includes a 4x4px solid `#F59E0B` square indicator.
- **Erreur / Suppression**: Background `rgba(239, 68, 68, 0.12)`, border `#EF4444`, text `#F87171`. Includes a 4x4px solid `#EF4444` square indicator.

### Data Tables (`JTable`)
- **Header**: Height `32px`, background `#1E293B`, bottom border 1px solid `#334155`, text `label-md` in `#94A3B8`.
- **Row Grid**: Height `36px` (default) or `28px` (dense telemetry mode), background alternate alternating between `#0F172A` and `#131D31`.
- **Gridlines**: Crisp horizontal border 1px `#1E293B`; no vertical dividers.
- **Row Selected**: Background `rgba(37, 99, 235, 0.20)`, left edge highlighted by a 2px vertical solid bar in `#2563EB`.
- **Numeric / Currency Alignment**: Right-aligned, formatted as `12 500 Ar` using `Space Grotesk`.

### Form Controls & Text Fields (`JTextField`, `JComboBox`)
- Height `32px`, background `#0B0F19`, 1px solid border `#334155`, text `#F8FAFC`, caret color `#38BDF8`.
- Focused state: 1px continuous border `#2563EB`, background `#0F172A`.
- Disabled state: Background `#1E293B`, border `#334155`, text `#64748B`.

### Checkboxes & Radio Controls
- Checkbox: 14x14px square, 0px radius, 1px border `#475569`, background `#0B0F19`. Checked: solid `#2563EB` with `#FFFFFF` inner square.
- Radio: 14x14px diamond (45-degree rotated square) or sharp square with 4x4px inner dot indicator to honor the zero-radius ethos.

### Cards, Panels & Modal Dialogs
- **Card**: Background `#0F172A`, 1px solid border `#1E293B`. Card title banner separated by a 1px solid `#1E293B` rule.
- **Modal Viewport**: Root `#1E293B`, framed in 1px `#334155`. Header bar locked at `36px` with draggable telemetry handle and prominent close button (`#EF4444` hover).