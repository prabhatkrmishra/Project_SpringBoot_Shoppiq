#!/usr/bin/env bash
set -euo pipefail

# ─── Configuration ───────────────────────────────────────────────────────────
REPO="prabhatkrmishra/Project_SpringBoot_Shoppiq"
DEPLOY_DIR="$HOME/shoppiq"
JAR_NAME="shoppiq-*.jar"
VERSION_FILE="$DEPLOY_DIR/.current_version"
TMUX_SESSION="shoppiq"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
JAVA_OPTS="--enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow -Xms256m -Xmx512m -DORT_NUM_THREADS=1"

# ─── Check latest release from GitHub ────────────────────────────────────────
LATEST_TAG=$(curl -s "https://api.github.com/repos/$REPO/releases/latest" \
  | grep '"tag_name"' | head -1 | sed 's/.*: "\(.*\)".*/\1/')

if [ -z "$LATEST_TAG" ]; then
  echo "[$(date)] Could not fetch latest release"
  exit 1
fi

CURRENT_VERSION=""
if [ -f "$VERSION_FILE" ]; then
  CURRENT_VERSION=$(cat "$VERSION_FILE")
fi

if [ "$LATEST_TAG" = "$CURRENT_VERSION" ]; then
  echo "[$(date)] Already running $LATEST_TAG — nothing to do"
  exit 0
fi

echo "[$(date)] New release found: $LATEST_TAG (current: ${CURRENT_VERSION:-none})"

# ─── Download JAR ────────────────────────────────────────────────────────────
JAR_URL="https://github.com/$REPO/releases/download/$LATEST_TAG/$JAR_NAME"

mkdir -p "$DEPLOY_DIR"
cd "$DEPLOY_DIR"

echo "[$(date)] Downloading $JAR_URL"
curl -fSL -o "${JAR_NAME}.new" "$JAR_URL"

# ─── Stop old process ────────────────────────────────────────────────────────
if tmux has-session -t "$TMUX_SESSION" 2>/dev/null; then
  echo "[$(date)] Stopping old process in tmux session '$TMUX_SESSION'"
  tmux send-keys -t "$TMUX_SESSION" C-c
  sleep 2
  tmux kill-session -t "$TMUX_SESSION"
fi

# ─── Swap JAR and start ─────────────────────────────────────────────────────
mv "${JAR_NAME}.new" "$JAR_NAME"
echo "$LATEST_TAG" > "$VERSION_FILE"

echo "[$(date)] Starting $JAR_NAME in tmux session '$TMUX_SESSION' (profile: $SPRING_PROFILE)"
tmux new-session -d -s "$TMUX_SESSION" \
  "cd $DEPLOY_DIR && java $JAVA_OPTS -jar $JAR_NAME --spring.profiles.active=$SPRING_PROFILE; echo 'Process exited'; sleep 10"

echo "[$(date)] Deployment complete — running $LATEST_TAG"
