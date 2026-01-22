# New Applets Development Plan: Browser, Downloads, & Notes

This document outlines the expansion of the Kaimera Tablet ecosystem with three new essential applets. Following our Agile methodology, each applet will be delivered as an MVP first, followed by iterative enhancements.

## 1. Browser Applet
*Goal: Provide a lightweight, secure web browsing experience integrated within the tablet UI.*

### MVP Features (Sprint 5.1)
- **Address Bar**: Input field for URLs and search queries.
- **WebView Integration**: Core browsing engine.
- **Navigation Controls**: Back, Forward, Refresh, and Home buttons.
- **Loading Progress**: Visual indicator for page load status.
- **Context Menu**: Long-press support for Links, Images, and Videos (v0.0.36).
- **Download Integration**: Seamlessly hand off downloads to the Downloads applet (v0.0.35).

### Activated Web Features
- **JavaScript Enabled**: Full support for modern interactive web content and video players.
- **DOM Storage**: Enabled `localStorage` and `sessionStorage` for site persistence.
- **Persistent State**: Automatically remembers and reloads the last visited URL.
- **Download Management**: Direct integration with system `DownloadManager` for files and media.

### Future Enhancements
- **Bookmarks**: Save and manage favorite sites.
- **Incognito Mode**: Private browsing without history.
- **Multiple Tabs**: Support for browsing multiple sites simultaneously.

---

## 2. Downloads Applet
*Goal: A centralized hub for managing all files downloaded via the Browser or other services.*

### MVP Features (Sprint 5.2)
- **Downloads List**: Vertical list showing file name, size, and date added.
- **File Actions**:
    - **Open**: Launch files using Android system intents.
    - **Delete**: Remove files from local storage.
- **Manual Download**: Dialog to paste a URL and start a download manually.
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
- **Folders/Tags**: Organize notes into categories via a hierarchical **Tree Panel**.
- **Attachments**: Link photos from the Camera or files from the Files applet.
- **Export**: Share notes as PDF or plain text.

---

## 4. Files Applet
*Goal: A comprehensive file manager for organizing documents, media, and system files.*

### MVP Features (Sprint 5.4)
- **File Explorer**: Browse local storage with list and grid views.
- **Tree Panel**: Collapsible sidebar for hierarchical folder navigation.
- **Dynamic Operations**: Rename, Delete, and Move actions.
- **Media Preview**: Integrated thumbnails for images and videos.

### Future Enhancements
- **Cloud Integration**: Access Google Drive, Dropbox, and S3.
- **Archive Support**: Compression and extraction of ZIP/RAR files.
- **Advanced Search**: Metadata-based filtering.

---

## 5. Calendar Applet
*Goal: A centralized scheduling tool for managing events, tasks, and deadlines.*

### MVP Features (Sprint 7.1)
- **Month/Week Views**: Fluid transitions between different time scales.
- **Event Management**: Create, edit, and delete calendar events.
- **Tree Panel**: Navigation sidebar for switching between "Personal", "Work", and "Shared" calendars.
- **Quick Add**: Simple interface for rapid event entry.

### Future Enhancements
- **Task Integration**: Sync with Projects applet to display deadlines.
- **Reminders**: System notifications for upcoming events.

---

## 6. Projects Applet (Sprint 7.2 - v0.0.55)
*Goal: A hierarchical project management tool for tracking complex workflows.*

**[Detailed Design Plan](applets/projects_design.md)**

### MVP Features
- **Persistence**: Built on **Room Database** for robust relation management.
- **Hierarchical Structure**: Organize work into Spaces -> Projects -> Tasks.
- **Tree Panel**: Deeply nested navigation for sub-projects and categories.
- **Task Board**: Basic Kanban (Todo/Doing/Done) or List view.
- **Status Tracking**: Visual indicators for project progress.

### Future Enhancements
- **Timeline/Gantt**: Visual representation of project schedules.
- **Resource Linking**: Attach Notes, Files, and Calendar events to projects.
- **Team Simulation**: Assign tasks to virtual users/roles.

---

## Agile Roadmap: Phase 5 & 6

### Phase 5: Multi-Utility Expansion (v0.1.0+)
- **v0.0.36**: Browser MVP implementation (Completed).
- **v0.0.35**: Downloads MVP implementation and Browser integration (Completed).
- **v0.0.37**: Manual Download feature in Downloads applet (In Progress).
- **v0.0.40**: Notes MVP implementation.

### Phase 6: System Synergy (v0.2.0+)
- **Hierarchical Tree Component**: Unified navigation panel for Files, Notes, and Settings (v0.0.43).
- **Deep Linking**: Allow applets to transition between each other (e.g., Camera -> Notes for logging).
- **Unified Search**: Search across Browser history, Downloads, and Notes from the Launcher.
- **Cloud Sync**: Optional background syncing for Notes and Bookmarks.

### Phase 7: Productivity Powerhouse (v0.3.0+)
- **v0.0.50**: Calendar MVP implementation.
- **v0.0.55**: Projects MVP implementation with hierarchical TreePanel navigation.
- **Inter-Applet Sync**: Deadline syncing between Projects and Calendar.
