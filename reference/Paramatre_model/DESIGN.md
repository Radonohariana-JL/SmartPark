---
name: SmartPark Operations Console
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
  display-lg:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  display-md:
    fontFamily: Space Grotesk
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Space Grotesk
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0em
  headline-sm:
    fontFamily: Space Grotesk
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  body-lg:
    fontFamily: Geist
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: 0em
  body-md:
    fontFamily: Geist
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0em
  body-sm:
    fontFamily: Geist
    fontSize: 11px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Space Grotesk
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Space Grotesk
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.08em
  telemetry-plate:
    fontFamily: Space Grotesk
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 22px
    letterSpacing: 0.1em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 0.75rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system establishes a high-contrast dark operations console tailored for 24/7 mission-control parking facilities, automated barrier systems, and high-velocity Automatic Number Plate Recognition (ANPR) monitoring. 

The aesthetic is technical, exacting, and low-fatigue. It rejects consumer fluff in favor of dense data displays, visible alignment grids, technical telemetry, and rapid spatial recognition under low-light control room environments. The visual language blends structural brutalism with sleek mission-critical instrumentation: razor-thin precise borders, high-contrast data callouts, monospaced tabular readings, and deliberate chromatic cues that draw immediate focus to gate stalls, sensor alerts, and occupancy thresholds.

## Colors

The palette operates on a calibrated dark slate infrastructure designed for maximum legibility and reduced ocular strain during continuous operation shifts:

- **Surface Base (`#0B0F19`)**: Ground canvas for application window backing, viewport gutters, and frame outlines.
- **Surface Elevation 1 (`#0F172A`)**: Primary container background for telemetry modules, data grids, toolbars, and primary navigation sidebars.
- **Surface Elevation 2 (`#1E293B`)**: Intermediary surface for cards, floating inspectors, search fields, and popover panels.
- **Border / Grid Line (`#334155`)**: Crisp, 1px structural dividing borders enforcing high visual separation across dense tool panels.
- **Action & Accent (`#2563EB` / Active: `#1D4ED8`)**: Focused interactive triggers, selected operational bays, active ANPR recognition bounding boxes, and system states.
- **Telemetry Emerald (`#10B981`)**: Vacancy indicators, automated barrier open status, and authenticated whitelist license plates.
- **Telemetry Amber (`#F59E0B`)**: Congestion warnings, maintenance diagnostics, and manual review queues.
- **Telemetry Danger (`#EF4444`)**: Tailgating flags, unregistered entry attempts, offline sensor nodes, and emergency barrier locks.
- **Text Primary (`#F8FAFC`)**: High-contrast operational readouts, vehicle plates, and counter telemetry.
- **Text Secondary (`#94A3B8`)**: Field descriptors, timestamps, and column headers.
- **Text Muted (`#64748B`)**: Structural metadata, grid coordinates, and inactive state cues.

## Typography

The typography couples the sharp, industrial geometry of **Space Grotesk** with the surgical utility of **Geist**.

- **Space Grotesk** is reserved for high-impact terminal telemetry, live barrier status banners, metric callouts, uppercase categorization tags, and license plate displays. Its engineered letterforms preserve legibility at brief glances.
- **Geist** governs continuous data density: multi-row data tables, system transaction logs, diagnostic traces, and dense configuration forms.
- Data tables and real-time ANPR plates must always enforce tabular numbers (`tnum`) to eliminate layout jitter when real-time vehicle counters, durations, and timestamps fluctuate.
- All technical micro-labels (`label-sm`, `label-md`) must be styled in uppercase with expanded letter-spacing to ensure legibility across multi-monitor control desk configurations.

## Layout & Spacing

This design system uses a modular, panel-oriented layout model optimized for desktop viewports and Java Swing split panes (`JSplitPane`), toolbars, and docking frames.

- **Grid Architecture**: Multi-pane docking grid. Desktop workspaces feature a fixed 240px or collapsible 64px left operations rail, a central multi-viewport camera/lane grid (2x2, 3x3, or 1x4 horizontal), and a contextual right-hand inspector panel (320px).
- **Rhythm**: Spacing is calibrated to a tight 4px baseline rhythm (`0.25rem`). Padding inside dense table cells and telemetry feeds defaults to `space-xs` (vertical) and `space-sm` (horizontal) to maximize the visible information envelope without screen clutter.
- **Adaptability**: Dynamic resize handles collapse secondary telemetry into icon-only status markers when horizontal resolution drops below 1280px. High-density 1080p and 4K control monitors expand card bays horizontally without increasing text size, displaying more active camera feeds and data columns simultaneously.

## Elevation & Depth

Visual hierarchy is communicated strictly through tonal layers and 1px borders. Ambient drop shadows are eliminated to reflect a precise instrumentation cockpit and ensure smooth, artifact-free painting in Java Swing runtimes.

- **Level 0 (Canvas Base)**: `#0B0F19`. Backdrop for window frames, status bars, and docking gutters.
- **Level 1 (Structural Surfaces)**: `#0F172A` encased by a 1px border of `#334155`. Used for primary workspace modules, live stream wrappers, and navigation bars.
- **Level 2 (Active Modules & Inputs)**: `#1E293B` encased by a 1px border of `#475569`. Applied to active cards, form fields, and dropdown selections.
- **Level 3 (Overlays & Inspectors)**: `#1E293B` overlaid directly onto Level 1 with an accentuated 1px border of `#2563EB` or `#64748B`. Modals and contextual flyout inspectors require no blur, maintaining sharp demarcation across window layers.

## Shapes

The design system standardizes on crisp, controlled corners (`roundedness: 1`), conveying structural rigor and technical efficiency:

- Standard controls (buttons, inputs, status tags, camera viewport containers) feature a base corner radius of `4px` (`0.25rem`).
- Secondary parent panels, modals, and primary application windows utilize `8px` (`0.5rem`).
- Inner status lights, sensor connection indicators, and live ANPR camera tally dots are pure circles (`9999px`).
- Sharp geometric boundaries prevent optical blur and anti-aliasing artifacts on industrial control room monitors.

## Components

### Buttons & Action Controls
- **Primary Action (Barrier Trigger / Override)**: Background `#2563EB`, text `#FFFFFF`, border 1px `#3B82F6`, radius 4px. Hover: `#1D4ED8`. Active: `#1E40AF`.
- **Secondary Action (Filter / Export)**: Background `#1E293B`, text `#F8FAFC`, border 1px `#334155`. Hover: `#334155` border `#64748B`.
- **Danger Override (Emergency Gate Lock / Alert)**: Background `#7F1D1D`, text `#FEE2E2`, border 1px `#DC2626`. Hover: `#991B1B`.

### ANPR Plate Display Chips
- High-visibility pill containing recognized license numbers. Background `#020617`, border 1px `#2563EB` (or `#10B981` on verified entry), padding 2px 8px, font `Space Grotesk` Bold (`telemetry-plate`), text `#F8FAFC`.

### Data Tables & Event Logs
- Dense alternating rows: base `#0F172A`, alternating `#131C31`. Row borders: 1px `#1E293B`.
- Table header: uppercase `Space Grotesk` (`label-sm`), background `#0B0F19`, text `#94A3B8`, height 28px.
- Selected row: `#1E293B` with a 2px left border accent of `#2563EB`.

### Status Badges & Chips
- **Vacant / Open**: Background `#064E3B`, text `#6EE7B7`, border 1px `#059669`.
- **Occupied / Processing**: Background `#1E293B`, text `#94A3B8`, border 1px `#475569`.
- **Warning / Tailgating**: Background `#78350F`, text `#FDE68A`, border 1px `#D97706`.
- **Violations / Error**: Background `#450A0A`, text `#FCA5A5`, border 1px `#DC2626`.

### Inputs & Search Bars
- Background `#0B0F19`, border 1px `#334155`, text `#F8FAFC`, placeholder `#64748B`, height 32px, radius 4px. Focus: border 1px `#2563EB` with zero glow.

### Live Camera Stream Containers
- Solid `#000000` backing, 1px border `#334155`. Embedded upper-left HUD overlay featuring camera ID, FPS, and bit-rate in `Space Grotesk` (`label-sm`), background `rgba(15, 23, 42, 0.85)`. Real-time vehicle recognition bounding boxes rendered in 1.5px `#10B981` or `#EF4444`.