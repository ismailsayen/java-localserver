#!/bin/bash

# LocalServer Audit Test Suite
# Tests all requirements from the audit checklist

SERVER_URL="http://127.0.0.2:8081"
TEST_RESULTS=0

echo "=========================================="
echo "LocalServer Audit Test Suite"
echo "=========================================="
echo ""

# Helper function to test HTTP method and status code (with file upload support)
test_http_method() {
    local method=$1
    local url=$2
    local expected_status=$3
    local description=$4
    local file=$5   # optional file path

    echo "Testing: $description"

    if [ -n "$file" ]; then
        # Send file as multipart/form-data
        response=$(curl -s -w "\n%{http_code}" -X "$method" -F "file=@$file" "$url")
    else
        # Send empty request
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url")
    fi

    status=$(echo "$response" | tail -n1)

    if [ "$status" = "$expected_status" ]; then
        echo "  ✓ PASS: Got status $status (expected $expected_status)"
    else
        echo "  ✗ FAIL: Got status $status (expected $expected_status)"
        TEST_RESULTS=$((TEST_RESULTS + 1))
    fi
    echo ""
}


# Test 1: GET requests
echo "=== TEST 1: GET Requests ==="
test_http_method "GET" "$SERVER_URL/" "200" "GET request to root"
test_http_method "GET" "$SERVER_URL/nonexistent" "404" "GET request to non-existent file"

# Test 2: POST requests
echo "=== TEST 2: POST Requests ==="
test_http_method "POST" "$SERVER_URL/upload" "201" "POST upload image" "www/uploads/image.jpg"

# Test 3: DELETE requests
echo "=== TEST 3: DELETE Requests ==="
test_http_method "DELETE" "$SERVER_URL/uploads/none" "404" "DELETE non-existent file"

# Test 4: Method Not Allowed (405)
echo "=== TEST 4: Method Not Allowed ==="
# First create a file in /api route which only allows GET
test_http_method "DELETE" "$SERVER_URL/" "405" "DELETE method on GET-only route"

# Test 5: Sessions and Cookies
echo "=== TEST 5: Sessions and Cookies ==="
response=$(curl -s -i "$SERVER_URL/")
if echo "$response" | grep -q "SESSION_ID: LOCALSERVER_SESSION"; then
    echo "  ✓ PASS: Session cookie is being set"
else
    echo "  ✗ FAIL: Session cookie not found"
    TEST_RESULTS=$((TEST_RESULTS + 1))
fi
echo ""

# Test 6: Error Page Status Codes
echo "=== TEST 6: Error Page Status Codes ==="
test_http_method "GET" "$SERVER_URL/forbidden" "404" "Testing 404 error page"

# Test 7: Request Headers
echo "=== TEST 7: HTTP Headers ==="
response=$(curl -s -i "$SERVER_URL/")
if echo "$response" | grep -q "HTTP/1.1"; then
    echo "  ✓ PASS: HTTP/1.1 response line present"
else
    echo "  ✗ FAIL: HTTP/1.1 response line not found"
    TEST_RESULTS=$((TEST_RESULTS + 1))
fi
echo ""

# Test 8: Connection header
if echo "$response" | grep -q "Connection: close"; then
    echo "  ✓ PASS: Connection: close header present"
else
    echo "  ✗ FAIL: Connection: close header missing"
    TEST_RESULTS=$((TEST_RESULTS + 1))
fi
echo ""

# Test 9: Content-Type header
if echo "$response" | grep -q "Content-Type:"; then
    echo "  ✓ PASS: Content-Type header present"
else
    echo "  ✗ FAIL: Content-Type header missing"
    TEST_RESULTS=$((TEST_RESULTS + 1))
fi
echo ""

echo "=========================================="
echo "Test Summary"
echo "=========================================="
if [ $TEST_RESULTS -eq 0 ]; then
    echo "✓ All tests passed!"
else
    echo "✗ $TEST_RESULTS test(s) failed"
fi
echo ""