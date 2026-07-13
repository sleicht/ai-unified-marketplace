# Requirements Catalog: CollabSpace Team Collaboration Portal

## Background

The product team at CollabSpace has drafted an initial vision document for a new team collaboration platform. The document has been shared with you as `docs/vision.md`. Leadership needs a formal requirements catalog before development can begin, and the project has been handed to you to produce it.

Your job is to translate the vision into a structured requirements catalog at `docs/requirements.html`. The catalog must cover functional requirements (what the system will do), non-functional requirements (measurable quality attributes), constraints (boundaries imposed on the solution), and documented review notes.

The vision document was written quickly by a non-technical stakeholder. You may find that some quality goals are expressed loosely, that there are tensions between different parts of the document, or that certain information about who will use the system is underspecified. Document every issue you encounter and the decision you made to resolve it — because the engineering and QA teams will need to review your reasoning before sign-off.

## Output Specification

Produce the following files:

1. **`docs/requirements.html`** — The complete canonical HTML catalog. Use semantic tables in the stable `functional-requirements`, `non-functional-requirements`, and `constraints` sections; include one `use-case-diagram` Mermaid slot.

2. **`requirements-notes` section in `docs/requirements.html`** — Record:
   - Any quality goals or NFRs from the vision that were vague or ambiguous, showing how you rewrote each one as a measurable requirement and what threshold you assumed.
   - Any conflicts or contradictions you discovered between requirements, identifying which specific requirements are in tension and how you resolved them for the catalog.
   - Any information that appeared to be missing or underspecified (such as stakeholder roles), and what defaults or assumptions you used in its place.

Do not create a separate Markdown requirements or notes file.
