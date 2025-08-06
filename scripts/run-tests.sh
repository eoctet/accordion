#!/bin/bash

# Accordion Test Runner Script
# This script runs all tests and generates reports

set -e

echo "🪗 Accordion Test Runner"
echo "========================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed or not in PATH"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

print_status "Java version check passed: $JAVA_VERSION"

# Clean previous builds
print_status "Cleaning previous builds..."
mvn clean -q

# Compile the project
print_status "Compiling project..."
if mvn compile -q; then
    print_success "Compilation successful"
else
    print_error "Compilation failed"
    exit 1
fi

# Compile test sources
print_status "Compiling test sources..."
if mvn test-compile -q; then
    print_success "Test compilation successful"
else
    print_error "Test compilation failed"
    exit 1
fi

# Run unit tests
print_status "Running unit tests..."
if mvn test -Dtest="!*IntegrationTest" -q; then
    print_success "Unit tests passed"
else
    print_error "Unit tests failed"
    exit 1
fi

# Run integration tests
print_status "Running integration tests..."
if mvn test -Dtest="*IntegrationTest" -q; then
    print_success "Integration tests passed"
else
    print_error "Integration tests failed"
    exit 1
fi

# Run all tests with coverage (if available)
print_status "Running all tests..."
if mvn test -q; then
    print_success "All tests passed"
else
    print_error "Some tests failed"
    exit 1
fi

# Generate test report
print_status "Generating test reports..."
if [ -d "target/surefire-reports" ]; then
    TEST_COUNT=$(find target/surefire-reports -name "*.xml" -exec grep -l "testcase" {} \; | wc -l)
    print_success "Test reports generated. Found $TEST_COUNT test files."
    
    # Show test summary
    if [ -f "target/surefire-reports/TEST-*.xml" ]; then
        TOTAL_TESTS=$(grep -h "tests=" target/surefire-reports/TEST-*.xml | sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
        FAILED_TESTS=$(grep -h "failures=" target/surefire-reports/TEST-*.xml | sed 's/.*failures="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
        SKIPPED_TESTS=$(grep -h "skipped=" target/surefire-reports/TEST-*.xml | sed 's/.*skipped="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
        
        echo ""
        echo "📊 Test Summary:"
        echo "   Total Tests: ${TOTAL_TESTS:-0}"
        echo "   Failed Tests: ${FAILED_TESTS:-0}"
        echo "   Skipped Tests: ${SKIPPED_TESTS:-0}"
        echo "   Passed Tests: $((${TOTAL_TESTS:-0} - ${FAILED_TESTS:-0} - ${SKIPPED_TESTS:-0}))"
    fi
else
    print_warning "No test reports found"
fi

# Package the project
print_status "Packaging project..."
if mvn package -DskipTests -q; then
    print_success "Packaging successful"
    
    if [ -f "target/accordion-*.jar" ]; then
        JAR_FILE=$(ls target/accordion-*.jar | head -n 1)
        JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
        print_success "Generated JAR: $JAR_FILE ($JAR_SIZE)"
    fi
else
    print_error "Packaging failed"
    exit 1
fi

echo ""
print_success "🎉 All tests completed successfully!"
echo ""
echo "📁 Generated artifacts:"
echo "   - JAR file: target/accordion-*.jar"
echo "   - Test reports: target/surefire-reports/"
echo "   - Compiled classes: target/classes/"
echo ""
echo "🚀 Ready for deployment!"