---
name: AgileWorkflow
description: Guide and templates for following Agile methodology and MVP principles in Kaimera Tablet development.
---

# Agile Workflow & MVP Guide

This skill provides a structured approach to delivering value quickly and iteratively.

## Core Principles

1.  **MVP (Minimum Viable Product)**: Build the smallest thing that delivers value. Don't gold-plate.
2.  **Iterative Delivery**: Release small, working increments (Sprints) rather than one big bang.
3.  **User-Centric**: Every feature must solve a user problem or delight the user.

## Workflow

### 1. Planning (Sprint Start)
- **Review Backlog**: Pick high-priority items from `docs/developer/feature_roadmap.md`.
- **Define Sprint Goal**: What is the "theme" of this sprint? (e.g., "Foundation", "Visuals").
- **Task Breakdown**: Break features into small, testable tasks in `task.md`.

### 2. Execution (Daily)
- **Pick a Task**: Work on one item at a time.
- **Implement**: Write code.
- **Verify**: Test locally.
- **Update Status**: Mark progress in `task.md`.

### 3. Review (Sprint End)
- **Demo**: Verify the features work as intended.
- **Release**: Tag a new version (e.g., v0.0.X) using the `AndroidDevelopment` release script.
- **Retrospective**: Update the roadmap with what was learned.

## Templates

### Feature Request Template (for `feature_roadmap.md`)
```markdown
### [Feature Name]
- **Value**: Why does the user need this?
- **MVP Scope**: What is the bare minimum version?
- **Future Scope**: What can wait for later?
```

### Sprint Checklist (for `task.md`)
```markdown
## Sprint [X]: [Goal Name]
- [ ] [Feature A]: Implementation
- [ ] [Feature A]: Verification
- [ ] [Feature B]: Implementation
- [ ] [Feature B]: Verification
- [ ] Release v0.X.X
```

## Definitions of Done (DoD)
A feature is "Done" when:
1.  Code is written and compiles.
2.  Unit/UI tests pass (if applicable).
3.  Manual verification on device/emulator confirms functionality.
4.  Documentation (`docs/`) is updated.
