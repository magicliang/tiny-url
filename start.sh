#!/bin/bash

# URL Shortener Service Startup Script

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    URL Shortener Service Starting     ${NC}"
echo -e "${BLUE}========================================${NC}"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed${NC}"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed${NC}"
    exit 1
fi

# Set environment variables
export DB_HOST=${DB_HOST:-11.142.154.110}
export DB_PORT=${DB_PORT:-3306}
export DB_NAME=${DB_NAME:-6n9pyl60}
export DB_USERNAME=${DB_USERNAME:-with_racdjgzrtmhgtadh}
export DB_PASSWORD=${DB_PASSWORD:-K#2C@Y3pTOpOJu}

echo -e "${YELLOW}Environment Configuration:${NC}"
echo -e "Database Host: ${DB_HOST}"
echo -e "Database Port: ${DB_PORT}"
echo -e "Database Name: ${DB_NAME}"
echo -e "Database User: ${DB_USERNAME}"
echo ""

# Build the application if jar doesn't exist
if [ ! -f "target/url-shortener-1.0.0.jar" ]; then
    echo -e "${YELLOW}Building application...${NC}"
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo -e "${RED}Build failed!${NC}"
        exit 1
    fi
    echo -e "${GREEN}Build completed successfully!${NC}"
fi

# Start the application
echo -e "${YELLOW}Starting URL Shortener Service...${NC}"
echo -e "${BLUE}Access the application at: http://localhost:8000${NC}"
echo -e "${BLUE}API Documentation: http://localhost:8000/api/v1/health${NC}"
echo ""

java -jar target/url-shortener-1.0.0.jar