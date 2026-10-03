#!/bin/bash

echo "=== Testing Camel REST API ==="
echo

# Test GET all users
echo "1. Testing GET all users:"
curl -s -X GET http://localhost:8080/api/users | jq '.'
echo
echo

# Test GET user by ID
echo "2. Testing GET user by ID (1):"
curl -s -X GET http://localhost:8080/api/users/1 | jq '.'
echo
echo

# Test POST create user
echo "3. Testing POST create user:"
curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "age": 25
  }' | jq '.'
echo
echo

# Test GET all users again to see the new user
echo "4. Testing GET all users (after creation):"
curl -s -X GET http://localhost:8080/api/users | jq '.'
echo
echo

# Test PUT update user
echo "5. Testing PUT update user (ID 1):"
curl -s -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "email": "john.updated@example.com",
    "age": 32
  }' | jq '.'
echo
echo

# Test DELETE user
echo "6. Testing DELETE user (ID 2):"
curl -s -X DELETE http://localhost:8080/api/users/2 -w "HTTP Status: %{http_code}\n"
echo
echo

# Test GET all users after deletion
echo "7. Testing GET all users (after deletion):"
curl -s -X GET http://localhost:8080/api/users | jq '.'
echo
echo

# Test error cases
echo "8. Testing GET non-existent user (404):"
curl -s -X GET http://localhost:8080/api/users/999 | jq '.'
echo
echo

echo "=== API Testing Complete ==="
