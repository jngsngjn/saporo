#!/bin/bash

APP_NAME="saporo-1.0.0.jar"
BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
JAR_FILE="$BASE_DIR/lib/$APP_NAME"
RESOURCE_DIR="$BASE_DIR/resource"
LOG_DIR="$BASE_DIR/log"

# 로그 디렉터리 없으면 생성
mkdir -p "$LOG_DIR"

# 현재 실행 중인 프로세스 확인
get_pid() {
  pgrep -f "$JAR_FILE"
}

# 실행
start_app() {
  PID=$(get_pid)
  if [ -n "$PID" ]; then
    echo "App is already running (PID: $PID). Restarting..."
    kill "$PID"
    sleep 1
  fi

  echo "Starting app..."
  nohup java -Xms300m -Xmx300m -Dspring.config.location="$RESOURCE_DIR/application.yml" \
             -Dlogging.config="$RESOURCE_DIR/logback-spring.xml" \
             -jar "$JAR_FILE" > "$LOG_DIR/app.out" 2>&1 &
  echo "Started."
}

# 종료
stop_app() {
  PID=$(get_pid)
  if [ -z "$PID" ]; then
    echo "App is not running."
  else
    echo "Stopping app (PID: $PID)..."
    kill "$PID"
    echo "Stopped."
  fi
}

# 인자 처리
case "$1" in
  start)
    start_app
    ;;
  stop)
    stop_app
    ;;
  *)
    echo "Usage: $0 {start|stop}"
    exit 1
    ;;
esac