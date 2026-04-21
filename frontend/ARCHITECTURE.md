# Frontend Structure

This frontend should follow a Next.js App Router structure where routing stays in `src/app` and product logic is grouped by feature outside of it.

## Target structure

```text
src/
  app/
    (public)/
    (app)/
    (legal)/
    api/
  features/
    auth/
    followings/
    share/
    user/
  components/
    layout/
    feedback/
    ui/
  lib/
    api/
    browser/
    config/
  types/
  utils/
  styles/
```

## Rules

- Keep `src/app` for routes, layouts, metadata, and route handlers only.
- Move feature-specific UI, hooks, and data mappers into `src/features/<feature>`.
- Keep `src/components` for reusable cross-feature components only.
- Keep `src/lib` for infrastructure helpers like backend fetch wrappers, runtime config, cookies, and browser integration.
- Keep shared domain types in `src/types`; do not export reusable types from component files.
- Avoid generic file names like `utils.tsx`; name files after the behavior they provide.
- Use `.ts` for non-JSX files and `.tsx` only for React components.

## Suggested next moves

- Create route groups: `(app)` for authenticated screens, `(public)` for login, `(legal)` for privacy and terms.
- Move `ArtistList`, `AdvisoryCards`, and related followings logic into `src/features/followings/`.
- Move share-page table logic into `src/features/share/`.
- Split global components into `components/layout`, `components/feedback`, and `components/ui`.
- Move logger into `src/lib` if it remains application-wide infrastructure.