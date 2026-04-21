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
    artists/
    followings/
    donate/
    share/
  components/
    layout/
    feedback/
    marketing/
  lib/
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

- Add feature-local hooks or mappers next to `followings`, `share`, and `donate` as those areas grow.
- Keep shared components in the grouped subfolders and do not add new files back to the flat `components/` root.
- Consider moving large route-only view logic from `page.tsx` into feature modules once each route gains more interactions.