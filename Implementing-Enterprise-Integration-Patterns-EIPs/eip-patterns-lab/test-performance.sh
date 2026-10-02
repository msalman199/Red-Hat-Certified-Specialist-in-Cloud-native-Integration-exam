#!/bin/bash

echo "Starting performance test..."

for i in {1..10}
do
  echo "Sending order $i"
  curl -X POST http://localhost:8080/api/orders/process \
    -H "Content-Type: application/json" \
    -d "{
      \"orderId\": \"PERF-TEST-$i\",
      \"customerType\": \"PREMIUM\",
      \"amount\": $((RANDOM % 2000 + 100)),
      \"priority\": \"HIGH\",
      \"productCategory\": \"ELECTRONICS\"
    }" &
done

wait
echo "Performance test completed"


