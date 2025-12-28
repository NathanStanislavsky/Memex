# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

API_BASE="http://localhost:8080"

echo -e "${BLUE}=== Memex API Test Script ===${NC}\n"

# Test 1: Search endpoint
echo -e "${YELLOW}1. Testing search endpoint...${NC}"
echo "   Query: 'test'"
curl -s "${API_BASE}/api/search?q=test" | jq '.' || echo "   Response received (may be empty array if no documents)"
echo ""

# Test 2: Search with different query
echo -e "${YELLOW}2. Testing search with different query...${NC}"
echo "   Query: 'document'"
curl -s "${API_BASE}/api/search?q=document" | jq '.' || echo "   Response received"
echo ""

# Test 3: Create a test file and upload it
echo -e "${YELLOW}3. Testing upload endpoint...${NC}"
TEST_FILE="/tmp/memex-test-$(date +%s).txt"
echo "This is a test document for Memex ingestion." > "$TEST_FILE"
echo "It contains some sample text that can be searched later." >> "$TEST_FILE"
echo "   Uploading test file: $TEST_FILE"
RESPONSE=$(curl -s -X POST -F "files=@${TEST_FILE}" "${API_BASE}/api/upload")
echo "   Response: $RESPONSE"
rm -f "$TEST_FILE"
echo ""

# Test 4: Search again to see if uploaded document appears (with delay)
echo -e "${YELLOW}4. Waiting 3 seconds for document processing, then searching...${NC}"
sleep 3
echo "   Query: 'test document'"
curl -s "${API_BASE}/api/search?q=test%20document" | jq '.' || echo "   Response received"
echo ""

# Test 5: Multiple rapid requests to generate some load
echo -e "${YELLOW}5. Generating load with multiple rapid requests...${NC}"
for i in {1..10}; do
    echo -n "   Request $i... "
    curl -s -o /dev/null -w "Status: %{http_code}, Time: %{time_total}s\n" "${API_BASE}/api/search?q=load${i}"
    sleep 0.2
done
echo ""

echo -e "${GREEN}=== Test completed! ===${NC}"
echo ""
echo "Check Grafana at http://localhost:3000 to see metrics:"
echo "  - Latency spikes: http_server_requests_seconds_max"
echo "  - Queue depth: rabbitmq_queue_messages_ready"

