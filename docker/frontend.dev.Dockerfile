# Dev image - the source is bind-mounted at /build and node_modules lives in a named volume so
# the host's install (possibly a different OS or architecture) does not shadow the in-container
# one. Runs the Vite dev server with HMR; chokidar polls because inotify events from a bind
# mount are not reliable across every host filesystem.
FROM node:24-alpine

WORKDIR /build

EXPOSE 5173

ENV CHOKIDAR_USEPOLLING=true

# The install runs against the bind-mounted source the first time the container starts;
# afterwards the named-volume node_modules survives restarts. The stamp is what makes that safe:
# a volume filled weeks ago holds whatever package.json wanted then, so a dependency added since
# is simply missing and Vite fails at startup. Recording the lock file it was installed from, and
# installing again when that no longer matches, keeps the volume honest without paying for an
# install on every start.
CMD ["sh", "-c", "stamp=node_modules/.lock-stamp; want=$(md5sum package-lock.json | cut -d' ' -f1); [ \"$(cat $stamp 2>/dev/null)\" = \"$want\" ] || { npm ci && printf %s \"$want\" > $stamp; }; exec npm run dev -- --host 0.0.0.0"]
