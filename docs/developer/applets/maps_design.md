# Maps Applet Design & Architecture

**Goal**: To provide a robust, private, and capable mapping and navigation tool within the Kaimera ecosystem, leveraging OpenStreetMap (OSM) for offline-capable, keyless mapping.

## 1. Architecture Overview

The Maps Applet will utilize `osmdroid` for rendering, ensuring independence from paid API keys (like Google Maps) while offering a "pro" feel with custom tile sources.

*   **Map Engine**: **osmdroid** (Android implementation of OpenStreetMap).
*   **Persistence**: **Room Database** for Saved Places and History.
*   **Architecture Pattern**: MVVM + Repository Pattern.
*   **DI**: Hilt for dependency injection.
*   **Location**: Android Framework LocationManager (or FusedLocationProvider if available).

---

## 2. Data Model (Room Entities)

We will use a relational model to store user data like saved places ("Favorites") and search history.

### Core Entities

1.  **SavedPlace**
    *   `id`: Long (PK)
    *   `name`: String ("Home", "Office", "Favorite Cafe")
    *   `latitude`: Double
    *   `longitude`: Double
    *   `address`: String?
    *   `categoryId`: Long? (FK -> PlaceCategory)
    *   `notes`: String?
    *   `addedAt`: Long (Timestamp)

2.  **PlaceCategory**
    *   `id`: Long (PK)
    *   `name`: String ("Food", "Work", "Travel")
    *   `color`: Int (Hex)
    *   `icon`: String (Material Icon ID)

3.  **SearchHistory**
    *   `id`: Long (PK)
    *   `query`: String
    *   `timestamp`: Long

---

## 3. Key Features

### 3.1 Interactive Map view
*   **Vector/Tile Rendering**: High-performance map rendering.
*   **Multi-Touch**: Pinch-to-zoom, two-finger rotation.
*   **Scale Bar**: Visual indicator of distance.
*   **Compass overlay**: Orients the user.

### 3.2 Location Services
*   **"My Location"**: One-tap signaling to center the map on the user's current coordinates.
*   **Follow Mode**: Map updates as the user moves.
*   **Compass Mode**: Map rotates with user's specific bearing.

### 3.3 Tree Panel Integration
*   **Visual**: A collapsible sidebar for managing "My Places".
*   **Structure**:
    *   Favorites (List of SavedPlace)
    *   Categories (Folder-like grouping)
    *   History (Recent searches)

### 3.4 Search & Geocoding
*   **Search Bar**: Floating search bar over the map.
*   **Nominatim Integration**: Use OSM's Nominatim API for address search and reverse geocoding (finding address from point).

---

## 4. Implementation Plan

### Phase A: Foundation & Map Rendering (v0.1.0)
*   [ ] Add `osmdroid-android` dependency.
*   [ ] Request Internet and Location permissions in Manifest.
*   [ ] Create `MapsViewModel` and `MapsScreen` scaffold.
*   [ ] Implement basic `AndroidView` wrapping `MapView` from osmdroid.
*   [ ] **Deliverable**: A screen showing the world map that can be panned and zoomed.

### Phase B: Location & Controls (v0.1.1)
*   [ ] Implement Permission handling (Runtime permissions for Location).
*   [ ] Add "My Location" Overlay to the map.
*   [ ] Implement UI controls (Zoom In/Out buttons, Recenter button).
*   [ ] Persist last map center/zoom level in `UserPreferences`.

### Phase C: Places & Persistence (v0.1.2)
*   [ ] Create Room Entities (`SavedPlace`, `PlaceCategory`).
*   [ ] Implement "Long-press to drop pin" interaction.
*   [ ] Create "Add Place" bottom sheet/dialog.
*   [ ] Implement **Tree Panel** to list Favorites.

### Phase D: Search & Navigation Basics (v0.1.3+)
*   [ ] Implement Nominatim Search API client (Retrofit).
*   [ ] Display search results on Map.
*   [ ] Basic "Draw Route" functionality using OSRM or similar public routing API (optional/future).
