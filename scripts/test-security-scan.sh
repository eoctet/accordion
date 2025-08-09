#!/bin/bash

# Test script for OWASP Dependency Check
# This script tests the security scan configuration locally

set -e

echo "🔍 Testing OWASP Dependency Check Configuration"
echo "=============================================="

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH"
    exit 1
fi

echo "✅ Maven is available"
mvn --version

# Check if pom.xml exists
if [ ! -f "pom.xml" ]; then
    echo "❌ pom.xml not found"
    exit 1
fi

echo "✅ pom.xml found"

# Check if suppressions file exists
if [ ! -f "owasp-suppressions.xml" ]; then
    echo "❌ owasp-suppressions.xml not found"
    exit 1
fi

echo "✅ OWASP suppressions file found"

# Clean and compile the project
echo "🔨 Compiling project..."
mvn clean compile -q

# Run dependency check
echo "🔍 Running OWASP Dependency Check..."
mvn dependency-check:check -B

# Check if reports were generated
echo "📊 Checking generated reports..."

if [ -f "target/dependency-check-report.html" ]; then
    echo "✅ HTML report generated"
else
    echo "⚠️ HTML report not found"
fi

if [ -f "target/dependency-check-report.xml" ]; then
    echo "✅ XML report generated"
else
    echo "⚠️ XML report not found"
fi

if [ -f "target/dependency-check-report.json" ]; then
    echo "✅ JSON report generated"
else
    echo "⚠️ JSON report not found"
fi

echo ""
echo "🎉 OWASP Dependency Check test completed successfully!"
echo "Reports are available in the target/ directory"