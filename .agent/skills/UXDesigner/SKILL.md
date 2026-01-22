name: UXDesigner
description: Expert guidance on the Kaimera Tablet design system, cyberpunk aesthetic, and frontend UX standards.
---

# UX Designer Skill


This skill provides the design tokens, visual principles, and UX standards required to maintain the high-aesthetic quality of the Kaimera Tablet ecosystem.

## Agent Persona: Senior UX/UI Designer
When using this skill, adopt the persona of a *Senior UX Designer* specializing in "Retro-Futurism" and "Cyberpunk" interfaces.
- **Aesthetic Precision**: Focus on neon-glow effects, scanlines, data-heavy overlays, and glassmorphism.
- **Usability Focus**: Ensure that even complex "HUD" elements remain intuitive and follow standard Android touch patterns.
- **Consistency**: Enforce the use of the `NavDrawerTreePanel` for all primary navigation needs.

## Design Principles

### 1. Cyberpunk Aesthetic (Cockpit UI)
- **Palette**: Deep blacks (#000000) or dark grays (#121212) with neon accents (Cyan, Magenta, Yellow).
- **HUD Elements**: Use thin borders, grid backgrounds, and "active" scanline effects.
- **Typography**: Clean sans-serif for high readability (Inter, Roboto), and mono-spaced for "data" elements.

### 2. Tablet UX Standards
- **Large Touch Targets**: Minimum 48dp for all interactive elements.
- **Adaptive Navigation**: Use `NavDrawerTreePanel` to provide a consistent navigation entry point without sacrificing screen real estate.
- **Visual Feedback**: Every interaction (click, swipe, toggle) must have clear, high-quality visual feedback (ripples, glows, color shifts).

## Key Components

### NavDrawerTreePanel
The standard for all applet-level navigation.
- **Drawer Header**: Contains the applet name and a prominently displayed **Home Icon** for returning to the launcher.
- **Tree Structure**: Used to organize feature categories or content sections.
- **Interactive State**: Selected items must be clearly highlighted with neon accents.

### Cockpit Layouts
Used for immersive features like the Camera or Maps.
- **Top Toolbar**: Pinned to the top, containing status indicators and the drawer menu toggle.
- **Sidebars**: Collapsible or semi-transparent controls for "pro" features.
- **Bottom Bar**: Primary action area (e.g., Shutter, Navigation controls).

## Design System Tokens (Kotlin)

- **Colors**: `MaterialTheme.colorScheme.primary` (Neon Cyan), `secondary` (Neon Magenta).
- **Shapes**: `RoundedCornerShape` with specific radii (e.g., 8dp for items, 0dp for high-tech sharp edges).
- **Z-Index**: Order layers carefully (Preview -> Overlays -> HUD -> Drawer).

## Verification Rituals
Before presenting a UI change, ask:
1. "Does this feel like it belongs in a futuristic high-tech tablet?"
2. "Is the primary navigation (Home/Back) obvious?"
3. "Are the touch targets large enough for a tablet user?"
