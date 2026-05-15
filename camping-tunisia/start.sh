#!/bin/bash
# CampingTN — Quick Start Script
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}"
echo "  ⛺  CampingTN — Smart Camping Platform"
echo "  ======================================${NC}"
echo ""

# Check prerequisites
check_cmd() {
  if ! command -v "$1" &>/dev/null; then
    echo -e "${RED}❌ $1 is required but not installed.${NC}"
    exit 1
  fi
  echo -e "${GREEN}✅ $1 found${NC}"
}

echo -e "${BLUE}Checking prerequisites...${NC}"
check_cmd java
check_cmd mvn
check_cmd python3
check_cmd mongod

echo ""
echo -e "${BLUE}Starting MongoDB...${NC}"
if ! pgrep mongod > /dev/null; then
  mkdir -p /tmp/camping_mongodb
  mongod --dbpath /tmp/camping_mongodb --port 27017 --fork --logpath /tmp/mongod.log
  sleep 2
  echo -e "${GREEN}✅ MongoDB started${NC}"
else
  echo -e "${GREEN}✅ MongoDB already running${NC}"
fi

echo ""
echo -e "${BLUE}Starting ML API (Python Flask)...${NC}"
cd ml-notebooks/ml-api
if [ ! -d "venv" ]; then
  python3 -m venv venv
  source venv/bin/activate
  pip install -r requirements.txt -q
else
  source venv/bin/activate
fi
nohup python app.py > /tmp/ml_api.log 2>&1 &
ML_PID=$!
echo -e "${GREEN}✅ ML API started (PID: $ML_PID, Port: 5000)${NC}"
cd ../..

sleep 3

echo ""
echo -e "${BLUE}Starting Spring Boot application...${NC}"
mvn spring-boot:run -q &
APP_PID=$!
echo -e "${GREEN}✅ Spring Boot starting (PID: $APP_PID)${NC}"

echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}🚀 CampingTN is starting up!"
echo ""
echo -e "   🌐 Web App:   http://localhost:8080"
echo -e "   🤖 ML API:    http://localhost:5000/health"
echo -e "   🍃 MongoDB:   mongodb://localhost:27017/camping_tunisia"
echo ""
echo -e "   🔑 Admin:     admin@camping.tn / Admin@2024${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "Press Ctrl+C to stop all services"

cleanup() {
  echo ""
  echo -e "${YELLOW}Stopping services...${NC}"
  kill $APP_PID 2>/dev/null || true
  kill $ML_PID 2>/dev/null || true
  pkill mongod 2>/dev/null || true
  echo -e "${GREEN}✅ All services stopped${NC}"
  exit 0
}

trap cleanup INT TERM
wait $APP_PID
