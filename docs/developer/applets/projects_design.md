# Projects Applet Design & Architecture

**Goal**: To build a professional-grade Project Management tool within the Kaimera ecosystem, capable of handling complex workflows, hierarchical structures, and team-like entity management (even for single users).

## 1. Architecture Overview

The Projects Applet will be the first "Heavy" applet in the Kaimera ecosystem, requiring robust data persistence and complex relationships.

*   **Persistence**: **Room Database** (SQLite abstraction).
*   **Architecture Pattern**: MVVM + Repository Pattern.
*   **DI**: Hilt for dependency injection.
*   **Navigation**: Tree-based hierarchy (Spaces > Projects > Phases > Tasks).

---

## 2. Data Model (Room Entities)

We will use a relational model to support deep nesting and flexible categorization.

### Core Entities

1.  **ProjectSpace (Root Container)**
    *   `id`: Long (PK)
    *   `name`: String ("Personal", "Work", "Side Hustle")
    *   `icon`: String (Material Icon ID)
    *   `color`: Int (Hex)

2.  **Project**
    *   `id`: Long (PK)
    *   `spaceId`: Long (FK -> ProjectSpace)
    *   `name`: String
    *   `description`: String?
    *   `status`: String (Customizable, defaults to "Active")
    *   `deadline`: Long? (Timestamp)
    *   `progress`: Int (0-100, or calculated)

3.  **Task**
    *   `id`: Long (PK)
    *   `projectId`: Long (FK -> Project)
    *   `parentId`: Long? (Self-referencing FK for Subtasks)
    *   `title`: String
    *   `description`: String (Markdown support)
    *   `status`: String (Customizable, defaults to "Todo")
    *   `priority`: Enum (LOW, MEDIUM, HIGH, CRITICAL)
    *   `assignedTo`: Long? (FK -> User)
    *   `dueDate`: Long?
    *   `estimatedDuration`: Long? (Minutes)

4.  **StatusDefinition** (New - for Custom Workflows)
    *   `id`: Long (PK)
    *   `spaceId`: Long? (FK -> ProjectSpace, Null = Global)
    *   `name`: String ("To Do", "In QA", "Done")
    *   `type`: Enum (PROJECT, TASK)
    *   `sortOrder`: Int
    *   `color`: Int

5.  **User** (Mock "Team Members" for planning)
    *   `id`: Long (PK)
    *   `name`: String
    *   `avatarUri`: String?
    *   `role`: String ("Admin", "Editor", "Viewer")

6.  **Team**
    *   `id`: Long (PK)
    *   `name`: String ("Engineering", "Design", "Marketing")
    *   `description`: String?

7.  **TeamMember** (Junction)
    *   `userId`: Long (FK -> User)
    *   `teamId`: Long (FK -> Team)
    *   `role`: String ("Lead", "Member")

8.  **ProjectTeam** (Junction - Assign Teams to Projects)
    *   `projectId`: Long (FK -> Project)
    *   `teamId`: Long (FK -> Team)
    *   `permissions`: String ("Read", "Write", "Admin")
    *   `startDate`: Long? (Timestamp)
    *   `endDate`: Long? (Timestamp)

    *   `endDate`: Long? (Timestamp)

9.  **MediaCollection** (e.g., "Site Photos", "Blueprints")
    *   `id`: Long (PK)
    *   `projectId`: Long (FK -> Project)
    *   `name`: String
    *   `description`: String?

10. **MediaItem**
    *   `id`: Long (PK)
    *   `collectionId`: Long (FK -> MediaCollection)
    *   `uri`: String (File URI)
    *   `type`: Enum (IMAGE, VIDEO, DOC)
    *   `caption`: String?

### 3.5 Data Management
*   **Import/Export**:
    *   **Formats**: Support JSON (for Kaimera backup) and CSV (for Excel interoperability).
    *   **SQL Dump**: Developer option to export the raw SQLite database for debugging.
    *   **Import**: Wizard to map CSV columns to Task fields.

11. **Category / Tag**
    *   `id`: Long (PK)
    *   `name`: String
    *   `color`: Int
    *   `scope`: Enum (GLOBAL, PROJECT_SPECIFIC)

### Junction Tables (Many-to-Many)
*   **TaskTags**: `taskId` + `tagId`
*   **ProjectTeam**: `projectId` + `teamId`

---

## 3. Key Features

### 3.1 Hierarchical Tree Panel
*   **Visual**: A collapsible sidebar (similar to Files/Notes) allowing deep traversal.
*   **Structure**:
    *   Spaces (Roots)
        *   Projects (Nodes)
            *   Milestones/Phases (Optional grouping)
                *   Tasks (Leafs)

### 3.2 Task Views
1.  **List View**: Standard outline view with indentation for subtasks.
2.  **Kanban Board**: Columns by Status (Todo -> Doing -> Done). Drag-and-drop support.
3.  **Gantt / Timeline**: (Future) Visual timeline based on start/due dates.

### 3.3 Dashboard
A unified "Home" view for the applet:
*   **"My Tasks"**: Aggregated list of tasks assigned to 'Me' across all projects, sorted by Due Date.
*   **"Overdue"**: High-visibility alert section.
*   **"Recent Activity"**: Log of last touched tasks.

### 3.4 Resource Integration
*   **Attachments**: Link existing Kaimera assets to tasks.
    *   Link a **Note** (from Notes applet) as a spec doc.
    *   Link a **File** (PDF/Image from Files applet) as a deliverable.
    *   Link a **URL** (from Browser) as research.

---

## 4. Implementation Plan

### Phase A: Foundation (v0.0.51)
*   [ ] Add Room dependencies to `build.gradle.kts`.
*   [ ] Define Entities (`Project`, `Task`, `Tag`) and DAOs.
*   [ ] Create `ProjectsDatabase` and Hilt modules.
*   [ ] Build basic `ProjectRepository`.

### Phase B: Structure & CRUD (v0.0.53)
*   [ ] Implement **Tree Panel** UI for Spaces & Projects.
*   [ ] Create UI to Add/Edit/Delete Projects.
*   [ ] Create UI to Add/Edit/Delete Tasks (List View only).

### Phase C: Task Board & Logic (v0.0.55 - MVP)
*   [ ] Implement **Kanban Board** logic (drag-and-drop state changes).
*   [ ] Add Filter/Sort logic (by Tag, Priority).
*   [ ] Implement "Dashboard" view.

### Phase D: Polish (v0.0.60)
*   [ ] User/Team mock implementation.
*   [ ] Inter-applet linking (Attach File to Task).
*   [ ] Implement Import/Export (JSON/CSV) logic.
