#!/bin/bash

echo "=== Testing Dynamic Routing with Camel ==="
echo

echo "1. Category-based Routing (Electronics):"
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD001", "category": "ELECTRONICS", "name": "Laptop"}'

echo
echo "2. Category-based Routing (Unknown category, should hit default):"
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD099", "category": "TOYS", "name": "Puzzle"}'

echo
echo "3. Complex Order Routing (High Value):"
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST http://localhost:8080/complex-orders \
  -H "Content-Type: application/json" \
  -d @test-data/high-value-order.json

echo
echo "4. Complex Order Routing (Standard Order):"
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST http://localhost:8080/complex-orders \
  -H "Content-Type: application/json" \
  -d @test-data/medium-priority-order.json

echo
echo "5. Complex Order Routing (High Priority):"
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST http://localhost:8080/complex-orders \
  -H "Content-Type: application/json" \
  -d @test-data/high-priority-order.json

echo
echo "=== All tests completed ==="
