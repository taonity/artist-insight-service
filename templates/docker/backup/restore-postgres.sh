#!/bin/sh
# Restore to an explicitly named, separate database; never drop the application database.
set -eu
: "${RESTORE_DATABASE:?Set RESTORE_DATABASE to a new, empty database name}"
: "${PGDATABASE:?Set PGDATABASE to the original application database}"
[ "$RESTORE_DATABASE" != "$PGDATABASE" ] || { echo "Use a separate restore database." >&2; exit 2; }
export_dir="${1:?Pass the completed postgres export directory}"
(cd "$export_dir" && sha256sum -c checksums.sha256)
pg_restore --list "$export_dir/database.dump" >/dev/null
createdb -- "$RESTORE_DATABASE"
pg_restore --exit-on-error --no-owner --no-privileges --dbname="$RESTORE_DATABASE" "$export_dir/database.dump"
psql --dbname="$RESTORE_DATABASE" --set=ON_ERROR_STOP=1 --command='SELECT count(*) AS restored_users FROM spotify_user;'
echo "Restore check complete in $RESTORE_DATABASE. Review and remove this temporary database manually."
