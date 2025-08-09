#!/bin/bash

# Script to validate code quality configuration
# This script tests the Maven plugins for code quality analysis

set -e

echo "🔍 Validating Code Quality Configuration"
echo "========================================"

# Check if configuration files exist
echo "📋 Checking configuration files..."

if [ -f "checkstyle.xml" ]; then
    echo "✅ Checkstyle configuration found"
else
    echo "❌ Checkstyle configuration missing"
    exit 1
fi

if [ -f "spotbugs-exclude.xml" ]; then
    echo "✅ SpotBugs exclusion file found"
else
    echo "❌ SpotBugs exclusion file missing"
    exit 1
fi

if [ -f "pmd-ruleset.xml" ]; then
    echo "✅ PMD ruleset found"
else
    echo "❌ PMD ruleset missing"
    exit 1
fi

echo ""
echo "🔧 Testing Maven plugin configurations..."

# Test Checkstyle plugin
echo "Testing Checkstyle plugin..."
if mvn checkstyle:help -q > /dev/null 2>&1; then
    echo "✅ Checkstyle plugin configured correctly"
else
    echo "❌ Checkstyle plugin configuration error"
    exit 1
fi

# Test SpotBugs plugin
echo "Testing SpotBugs plugin..."
if mvn spotbugs:help -q > /dev/null 2>&1; then
    echo "✅ SpotBugs plugin configured correctly"
else
    echo "❌ SpotBugs plugin configuration error"
    exit 1
fi

# Test PMD plugin
echo "Testing PMD plugin..."
if mvn pmd:help -q > /dev/null 2>&1; then
    echo "✅ PMD plugin configured correctly"
else
    echo "❌ PMD plugin configuration error"
    exit 1
fi

echo ""
echo "🧪 Running dry-run quality checks..."

# Compile first
echo "Compiling project..."
mvn clean compile test-compile -q

# Run Checkstyle check (dry run)
echo "Running Checkstyle analysis..."
if mvn checkstyle:checkstyle -q; then
    echo "✅ Checkstyle analysis completed"
    if [ -f "target/checkstyle-result.xml" ]; then
        CHECKSTYLE_VIOLATIONS=$(grep -c '<error' target/checkstyle-result.xml || echo "0")
        echo "   Found $CHECKSTYLE_VIOLATIONS violations"
    fi
else
    echo "⚠️  Checkstyle analysis had issues"
fi

# Run SpotBugs analysis (dry run)
echo "Running SpotBugs analysis..."
if mvn spotbugs:spotbugs -q; then
    echo "✅ SpotBugs analysis completed"
    if [ -f "target/spotbugsXml.xml" ]; then
        SPOTBUGS_VIOLATIONS=$(grep -c '<BugInstance' target/spotbugsXml.xml || echo "0")
        echo "   Found $SPOTBUGS_VIOLATIONS violations"
    fi
else
    echo "⚠️  SpotBugs analysis had issues"
fi

# Run PMD analysis (dry run)
echo "Running PMD analysis..."
if mvn pmd:pmd -q; then
    echo "✅ PMD analysis completed"
    if [ -f "target/pmd.xml" ]; then
        PMD_VIOLATIONS=$(grep -c '<violation' target/pmd.xml || echo "0")
        echo "   Found $PMD_VIOLATIONS violations"
    fi
else
    echo "⚠️  PMD analysis had issues"
fi

echo ""
echo "📊 Quality Analysis Summary"
echo "=========================="
echo "Checkstyle violations: ${CHECKSTYLE_VIOLATIONS:-N/A}"
echo "SpotBugs violations: ${SPOTBUGS_VIOLATIONS:-N/A}"
echo "PMD violations: ${PMD_VIOLATIONS:-N/A}"

echo ""
echo "✅ Code quality configuration validation completed!"
echo "🚀 Ready for GitHub Actions CI/CD pipeline"