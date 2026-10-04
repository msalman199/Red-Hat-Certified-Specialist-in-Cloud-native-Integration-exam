#!/bin/bash

echo "=== Error Handling Performance Analysis ==="
echo "Analysis timestamp: $(date)"
echo

retry_count=$(ls output/retry/ 2>/dev/null | wc -l)
dlq_success_count=$(ls output/dlq/success/ 2>/dev/null | wc -l)
dlq_failed_count=$(ls output/dlq/failed/ 2>/dev/null | wc -l)
fallback_primary_count=$(ls output/fallback/primary/ 2>/dev/null | wc -l)
fallback_fallback_count=$(ls output/fallback/fallback/ 2>/dev/null | wc -l)
advanced_success_count=$(ls output/advanced/success/ 2>/dev/null | wc -l)
advanced_validation_count=$(ls output/advanced/validation-errors/ 2>/dev/null | wc -l)
advanced_runtime_count=$(ls output/advanced/runtime-errors/ 2>/dev/null | wc -l)
advanced_general_count=$(ls output/advanced/general-errors/ 2>/dev/null | wc -l)

echo "Retry Strategy:"
echo "  Successfully processed: $retry_count messages"
echo

echo "Dead Letter Queue Strategy:"
echo "  Successfully processed: $dlq_success_count messages"
echo "  Failed (sent to DLQ): $dlq_failed_count messages"
if [ $((dlq_success_count + dlq_failed_count)) -gt 0 ]; then
    success_rate=$(echo "scale=2; $dlq_success_count * 100 / ($dlq_success_count + $dlq_failed_count)" | bc -l)
    echo "  Success rate: ${success_rate}%"
fi
echo

echo "Fallback Strategy:"
echo "  Primary processing: $fallback_primary_count messages"
echo "  Fallback processing: $fallback_fallback_count messages"
if [ $((fallback_primary_count + fallback_fallback_count)) -gt 0 ]; then
    primary_rate=$(echo "scale=2; $fallback_primary_count * 100 / ($fallback_primary_count + $fallback_fallback_count)" | bc -l)
    echo "  Primary success rate: ${primary_rate}%"
fi
echo

echo "Advanced Error Handling:"
echo "  Successful processing: $advanced_success_count messages"
echo "  Validation errors: $advanced_validation_count messages"
echo "  Runtime errors: $advanced_runtime_count messages"
echo "  General errors: $advanced_general_count messages"
echo

total_processed=$((retry_count + dlq_success_count + dlq_failed_count + fallback_primary_count + fallback_fallback_count + advanced_success_count + advanced_validation_count + advanced_runtime_count + advanced_general_count))
echo "Total messages processed across all strategies: $total_processed"
