# Changelog: User API

**1.0.0 → 2.0.0**

*Generated: 2025-12-25 04:30:25*

---

## Summary

| Metric | Value |
|--------|-------|
| Total changes | 7 |
| Breaking changes | 3 |
| Risk level | CRITICAL |
| Risk score | 90/100 |
| Recommended version bump | **MAJOR** |

## 🔴 Breaking Changes

### `/users`

- **Type:** REMOVED
- **Category:** ENDPOINT
- **Description:** Endpoint removed: POST /users
- **Impact Score:** 100/100
- **Migration:** Update all API consumers to stop using the removed endpoint. Consider using an alternative endpoint if available.

### `/users`

- **Type:** REMOVED
- **Category:** ENDPOINT
- **Description:** Endpoint removed: GET /users
- **Impact Score:** 100/100
- **Migration:** Update all API consumers to stop using the removed endpoint. Consider using an alternative endpoint if available.

### `/users/{id}`

- **Type:** REMOVED
- **Category:** ENDPOINT
- **Description:** Endpoint removed: GET /users/{id}
- **Impact Score:** 100/100
- **Migration:** Update all API consumers to stop using the removed endpoint. Consider using an alternative endpoint if available.

## 🟢 Additions

- **/api/users**: New endpoint added: POST /api/users *(ADDED)*
- **/api/users/{userId}**: New endpoint added: GET /api/users/{userId} *(ADDED)*
- **/api/users**: New endpoint added: GET /api/users *(ADDED)*
- **/api/users/{userId}/profile**: New endpoint added: GET /api/users/{userId}/profile *(ADDED)*

