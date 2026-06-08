for f in /etc/epics/env.d/*.env; do
    [ -r "$f" ] && set -a && . "$f" && set +a
done
unset f
