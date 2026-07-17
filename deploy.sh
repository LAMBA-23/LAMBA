#!/bin/bash
# Deploy script for LAMBA backend
# Run this on the server (186.246.27.211) or via SSH

set -e

echo "=== LAMBA Backend Deploy ==="
echo "Server: 186.246.27.211"
echo ""

# Check if we're on the server
if [ ! -f "docker-compose.yml" ]; then
    echo "ERROR: docker-compose.yml not found. Run this from the LAMBA directory on the server."
    echo ""
    echo "To deploy via SSH:"
    echo "  ssh user@186.246.27.211"
    echo "  cd /path/to/LAMBA"
    echo "  bash deploy.sh"
    exit 1
fi

echo "1. Pulling latest code..."
git pull origin main

echo "2. Stopping containers..."
docker compose down

echo "3. Rebuilding and starting..."
docker compose up --build -d

echo "4. Waiting for backend to start..."
sleep 5

echo "5. Checking health..."
if curl -s http://localhost:8000/health | grep -q '"status":"ok"'; then
    echo "   Backend is healthy!"
else
    echo "   WARNING: Backend may not be ready yet. Check manually."
fi

echo ""
echo "=== Deploy complete ==="
echo "Backend URL: http://186.246.27.211:8000"
