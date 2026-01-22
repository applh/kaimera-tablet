# Design Exploration: Camera Control Panel UX/UI

**Date:** 2026-01-22
**Status:** Draft
**Related Feature**: Phase 4 - Adaptive & Responsive UI

## 1. Introduction

The Kaimera Tablet Camera Applet is evolving into a professional-grade tool. As we introduce "Liquid Layouts" and support various window sizes (split-screen, floating, desktop), the `CameraControlPanel` must adapt.

This document challenges the traditional linear/rectangular layout by exploring two distinct paradigms for the control panel:
1.  **The Grid Layout:** An evolution of the current linear lists into structured, responsive grids.
2.  **The Circular Layout:** A thumb-centric, radial interface inspired by physical camera dials and ergonomic reachability.

## 2. Option A: The Grid Layout (Structured & Dense)

### Concept
The Grid Layout organizes controls into a structured matrix of cells. It is "content-aware" and "window-aware," filling the available rectangular space efficiently.

### Visual Descriptions
- **Portrait/Sidebar:** A 2xN or 3xN grid of buttons on the side.
- **Landscape/Bottom:** A Nx2 or Nx3 grid at the bottom.
- **Groupings:** Logical groups (Toggles, Pro Controls, Actions) are separated by spacing or dividers within the grid.

### Pros
- **Density:** Can fit many controls in a compact area.
- **Familiarity:** Users are accustomed to toolbars and grids (standard Android UI).
- **Scalability:** Easy to "reflow" cells based on window width (e.g., changes from 1 column to 2 columns seamlessly).
- **Predictability:** Touch targets are uniform in size and shape.

### Cons
- **Reachability:** On large tablets, the top-left or top-right cells of a sidebar grid might be hard to reach with a thumb while holding the device.
- **Aesthetics:** Can feel utilitarian or "mobile-first" rather than a custom pro-camera interface.

### Draft Implementation Idea
```kotlin
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 64.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    item { FlashToggle() }
    item { TorchToggle() }
    // ...
}
```

## 3. Option B: The Circular Layout (Ergonomic & Dial-Based)

### Concept
The Circular Layout places primary controls along an arc or circle, radiating from the user's thumb position (usually near the Shutter button). It mimics the physical command dials of DSLR/Mirrorless cameras.

### Visual Descriptions
- **The "Fan" Menu:** Tapping a "Settings" FAB expands controls in a fan arc (quarter circle).
- **Rotary Dials:** Instead of linear sliders for EV and Zoom, use curved sliders or rotating wheels.
- **Floating HUD:** Controls "orbit" the shutter button.

### Pros
- **Ergonomics:** Heavily optimized for handheld tablet use. All controls are within the "thumb sweep" radius.
- **Differentiation:** Looks futuristic, premium, and distinct from generic camera apps.
- **Muscle Memory:** Learning the "position on the clock" allows for eyes-free operation.

### Cons
- **Space Efficiency:** Circles leave "dead space" in rectangular corners.
- **Complexity:** Harder to adapt to extremely narrow split-screen windows (circles get clipped).
- **Learning Curve:** Non-standard interaction patterns may confuse casual users.

### Draft Implementation Idea
A main `ShutterButton` anchored at the bottom-right/center. Secondary controls (Flash, Lens, Mode) are positioned absolutely at `(r * cos(theta), r * sin(theta))` relative to the anchor.

## 4. Variant: The Scrollable Grid Overlay (User Proposal)

### Concept
Place **all** controls into a unified grid that overlays the preview or sits adjacent to it. If the window is too small to fit the grid, the grid becomes scrollable.

### Pros
- **Review:** "One bucket" for all features. No hidden menus.
- **Responsiveness:** `LazyVerticalGrid` provides scrolling automatically. It never breaks; it just scrolls.
- **Simplicity:** Extremely easy to implement and maintain.

### Cons
- **Friction:** Scrolling is effectively "hiding" controls. In a fast-paced camera environment, having to scroll to find the "Video" toggle or "Flash" is a significant UX friction point.
- **Preview Blockage:** If it's an overlay, it obscures the subject.
- **Hierarchy:** Treating "Shutter" and "Grid Lines" with equal weight (just cells in a grid) dilutes the priority of primary actions.

### Verdict
Good for **Secondary/Settings** menus (like a "Tools" drawer), but **risky for Primary Controls** (Shutter, Zoom, Mode) which should always be visible and stable.

## 5. Variant: The Quad-Border "Cockpit" (User Proposal)

### Concept
A professional, "IDE-style" layout that utilizes all four borders of the screen, leaving the center clear for the viewport (or an overlay grid).

**Layout Priority:**
1.  **Bottom Bar (Priority 1):** Primary Actions (Shutter, Mode).
2.  **Right Bar (Priority 2):** Primary Scalers (Zoom Slider - Vertical).
3.  **Left Bar (Priority 3):** Secondary Scalers (Exposure/Focus - Vertical).
4.  **Top Bar (Priority 4):** Status & Toggles (Flash, Settings - Horizontal).

**Central Overflow:**
A scrollable grid panel in the center handles overflow or detailed settings when window size is small.

### Pros
- **Professionalism:** Mimics high-end creative tools (Editor, IDE, Flight Simulator).
- **Ergonomics:** Distributes controls to where thumbs naturally rest (Bottom/Sides) while moving static info to the Top.
- **Responsiveness:** The "Priority System" is brilliant for resizing. As the window shrinks, you drop borders:
    - *Shrink 1:* Drop Top Bar -> Move icons to Side.
    - *Shrink 2:* Drop Left Bar -> Move sliders to Scrollable Grid.
    - *Shrink 3:* Keep only Bottom Bar.

### Verdict
**Strongest contender for a "Pro" Tablet interface.** It maximizes screen real estate usage while providing a clear logic for degradation on smaller screens.

## 6. Comparative Analysis

| Feature | Grid Layout | Circular Layout |
| :--- | :--- | :--- |
| **Space Efficiency** | High (Fills rectangles) | Medium (Leaves corners) |
| **Reachability** | Medium (Depends on height) | High (Thumb-centric) |
| **Responsiveness** | Excellent (Reflows easily) | Challenging (Requires smart scaling) |
| **"Pro" Feel** | Standard Software | Hardware/Mechanical Vibe |
| **Dev Effort** | Low (Standard Compose) | High (Custom Layouts + Trig) |

## 7. Recommendation: The "Quad-Border Cockpit"

### Selected Strategy
We will implement **Option D (The Quad-Border Cockpit)** as it best aligns with the "Pro" tablet vision while offering a robust fallback strategy for smaller windows.

### Layout Topology
1.  **Bottom (Action Bar):** Shutter, Mode Switcher, Gallery, Lens.
2.  **Right (Zoom Bar):** Vertical Zoom Slider.
3.  **Left (Control Bar):** Vertical Exposure Slider & Manual Focus.
4.  **Top (Status Bar):** Flash, Timer, Settings Icon.
5.  **Center Overlay:** A `LazyVerticalGrid` (Option C) triggered by a "More" button to handle overflow settings that don't fit in the borders.

### Proposed Timeline
- **Phase 4.1:** Refactor current `CameraControlPanel` into 4 discrete Composables (`TopBar`, `BottomBar`, `SideBarLeft`, `SideBarRight`).
- **Phase 4.2:** Implement the "Priority Degradation" logic to hide bars as width reduces.
- **Phase 4.3:** Create the central `LazyVerticalGrid` overlay for overflow items.
