#!/bin/bash

REPORT_FILE="error-handling-test-report.txt"

echo "=== CAMEL ERROR HANDLING LAB TEST REPORT ===" > $REPORT_FILE
echo "Generated on: $(date)" >> $REPORT_FILE
echo "=========================================" >> $REPORT_FILE
echo >> $REPORT_FILE

echo "=== CONFIGURATION SUMMARY ===" >> $REPORT_FILE
echo "- Retry Strategy: Max 3 retries, 2 second delay" >> $REPORT_FILE
echo "- DLQ Strategy: Max 2 retries, 1 second delay" >> $REPORT_FILE
echo "- Fallback Strategy: Immediate fallback on failure" >> $REPORT_FILE
echo "- Advanced Handling: Exception-specific routing" >> $REPORT_FILE
echo >> $REPORT_FILE

echo "=== PROCESSING RESULTS ===" >> $REPORT_FILE
./analyze-performance.sh >> $REPORT_FILE
echo >> $REPORT_FILE

echo "=== ERROR PATTERNS ANALYSIS ===" >> $REPORT_FILE
echo -n "Retry attempts found: " >> $REPORT_FILE
grep -c -i "retry" application.log 2>/dev/null >> $REPORT_FILE || echo "0" >> $REPORT_FILE
echo -n "DLQ routing events: " >> $REPORT_FILE
grep -c -i "DLQ" application.log 2>/dev/null >> $REPORT_FILE || echo "0" >> $REPORT_FILE
echo -n "Fallback processing events: " >> $REPORT_FILE
grep -c -i "Fallback" application.log 2>/dev/null >> $REPORT_FILE || echo "0" >> $REPORT_FILE
echo >> $REPORT_FILE

echo "=== SAMPLE ERROR MESSAGES ===" >> $REPORT_FILE
echo "Recent error log entries:" >> $REPORT_FILE
tail -50 application.log 2>/dev/null | grep -i error >> $REPORT_FILE || echo "No recent errors found" >> $REPORT_FILE
echo >> $REPORT_FILE

echo "=== RECOMMENDATIONS ===" >> $REPORT_FILE
echo "1. Monitor DLQ messages regularly for business impact" >> $REPORT_FILE
echo "2. Adjust retry delays based on downstream system capabilities" >> $REPORT_FILE
echo "3. Implement alerting for high failure rates" >> $REPORT_FILE
echo "4. Consider circuit breaker patterns for external dependencies" >> $REPORT_FILE
echo >> $REPORT_FILE

echo "Test report generated: $REPORT_FILE"
