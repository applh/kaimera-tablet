# New Applets Development Plan: Browser, Downloads, & Notes

This document outlines the expansion of the Kaimera Tablet ecosystem with three new essential applets. Following our Agile methodology, each applet will be delivered as an MVP first, followed by iterative enhancements.

## 1. Browser Applet
*Goal: Provide a lightweight, secure web browsing experience integrated within the tablet UI.*

### MVP Features (Sprint 5.1)
- **Address Bar**: Input field for URLs and search queries.
- **WebView Integration**: Core browsing engine.
- **Navigation Controls**: Back, Forward, Refresh, and Home buttons.
- **Loading Progress**: Visual indicator for page load status.

### Future Enhancements
- **Bookmarks**: Save and manage favorite sites.
- **Incognito Mode**: Private browsing without history.
- **Multiple Tabs**: Support for browsing multiple sites simultaneously.
- **Download Integration**: Seamlessly hand off downloads to the Downloads applet.

---

## 2. Downloads Applet
*Goal: A centralized hub for managing all files downloaded via the Browser or other services.*

### MVP Features (Sprint 5.2)
- **Downloads List**: Vertical list showing file name, size, and date added.
- **File Actions**:
    - **Open**: Launch files using Android system intents.
    - **Delete**: Remove files from local storage.
- **Storage Tracking**: Show remaining device storage space.

### Future Enhancements
- **Category Filtering**: Filter by Images, Videos, Documents, and Others.
- **Search**: Quickly find specific downloaded files.
- **Parallel Downloads**: Support and UI for multiple simultaneous downloads.
- **Pause/Resume**: Control over active downloads.

---

## 3. Notes Applet
*Goal: A distraction-free environment for quick ideation and long-form writing.*

### MVP Features (Sprint 5.3)
- **Note Management**: Create, view, edit, and delete notes.
- **Auto-Save**: Ensure no data is lost during composition.
- **Simple UI**: Clean, typography-focused interface.
- **Search**: Local search through note titles and content.

### Future Enhancements
- **Markdown Support**: Rich text formatting using Markdown syntax.
- **Folders/Tags**: Organize notes into categories.
- **Attachments**: Link photos from the Camera or files from the Files applet.
- **Export**: Share notes as PDF or plain text.

---

## Agile Roadmap: Phase 5 & 6

### Phase 5: Multi-Utility Expansion (v0.1.0+)
- **v0.0.31**: Browser MVP implementation (Completed).
- **v0.0.35**: Downloads MVP implementation and Browser integration (Completed).
- **v0.0.40**: Notes MVP implementation.

### Phase 6: System Synergy (v0.2.0+)
- **Deep Linking**: Allow applets to transition between each other (e.g., Camera -> Notes for logging).
- **Unified Search**: Search across Browser history, Downloads, and Notes from the Launcher.
- **Cloud Sync**: Optional background syncing for Notes and Bookmarks.
