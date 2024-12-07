#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if the API is available
check_api() {
    echo -e "${BLUE}Checking API availability...${NC}"
    local response=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/api/podcasts")
    
    if [ "$response" -eq 200 ]; then
        echo -e "${GREEN}API is available${NC}"
        return 0
    else
        echo -e "${RED}API is not available (HTTP status: $response)${NC}"
        echo "Please ensure the application is running and accessible at http://localhost:8080"
        return 1
    fi
}

# Function to delete a podcast
delete_podcast() {
    local id=$1
    echo -e "${BLUE}Attempting to delete podcast with ID: $id${NC}"
    
    # Make the DELETE request and capture both the HTTP status code and response body
    local temp_file=$(mktemp)
    local response=$(curl -s -w "%{http_code}" -X DELETE "http://localhost:8080/api/podcasts/$id" -o "$temp_file")
    local body=$(cat "$temp_file")
    rm "$temp_file"
    
    case $response in
        204|200)
            echo -e "${GREEN}Successfully deleted podcast $id${NC}"
            return 0
            ;;
        404)
            echo -e "${YELLOW}Podcast $id not found - skipping${NC}"
            return 0
            ;;
        *)
            echo -e "${RED}Failed to delete podcast $id (HTTP status: $response)${NC}"
            if [ ! -z "$body" ]; then
                echo -e "${RED}Error response: $body${NC}"
            fi
            return 1
            ;;
    esac
}

# Main script
echo -e "${BLUE}=== Podcast Cleanup Script ===${NC}"
echo -e "${BLUE}This script will delete all podcasts with IDs from 1 to 89${NC}"
echo -e "${YELLOW}Press CTRL+C to cancel or ENTER to continue...${NC}"
read

# Check if API is available before proceeding
if ! check_api; then
    exit 1
fi

echo -e "\n${BLUE}Starting deletion of podcasts with IDs < 90${NC}"
echo -e "${BLUE}----------------------------------------${NC}"

failures=0
success=0
skipped=0
start_time=$(date +%s)

for id in {1..89}
do
    if delete_podcast $id; then
        if [[ $(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/api/podcasts/$id") == "404" ]]; then
            ((success++))
        else
            ((skipped++))
        fi
    else
        ((failures++))
    fi
    # Add a small delay to prevent overwhelming the server
    sleep 0.2
done

end_time=$(date +%s)
duration=$((end_time - start_time))

echo -e "\n${BLUE}----------------------------------------${NC}"
echo -e "${BLUE}Deletion process complete${NC}"
echo -e "Time taken: ${duration} seconds"
echo -e "Successfully deleted: ${GREEN}$success${NC}"
echo -e "Skipped/Not found: ${YELLOW}$skipped${NC}"
if [ $failures -eq 0 ]; then
    echo -e "${GREEN}All operations completed successfully${NC}"
else
    echo -e "${RED}Failed operations: $failures${NC}"
fi
