#!/bin/bash

# Accordion commit message format checking script
# Based on Conventional Commits specification
# https://www.conventionalcommits.org/

set -e

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Help information
show_help() {
    echo "Usage: $0 [OPTIONS] <commit-message>"
    echo ""
    echo "Check if commit message complies with Conventional Commits specification"
    echo ""
    echo "Options:"
    echo "  -h, --help     Show help information"
    echo "  -f, --file     Read commit message from file"
    echo "  -v, --verbose  Show verbose information"
    echo ""
    echo "Examples:"
    echo "  $0 'feat(auth): add JWT token refresh mechanism'"
    echo "  $0 -f .git/COMMIT_EDITMSG"
    echo ""
    echo "Supported commit types:"
    echo "  feat     - New feature"
    echo "  fix      - Bug fix"
    echo "  docs     - Documentation update"
    echo "  style    - Code style adjustment"
    echo "  refactor - Code refactoring"
    echo "  test     - Test related"
    echo "  chore    - Build process or auxiliary tool changes"
    echo "  perf     - Performance optimization"
    echo "  ci       - CI/CD related"
}

# Log functions
log_error() {
    echo -e "${RED}❌ ERROR: $1${NC}" >&2
}

log_success() {
    echo -e "${GREEN}✅ SUCCESS: $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}"
}

log_info() {
    echo -e "${BLUE}ℹ️  INFO: $1${NC}"
}

# Check commit message format
check_commit_message() {
    local message="$1"
    local verbose="$2"

    if [ -z "$message" ]; then
        log_error "Commit message cannot be empty"
        return 1
    fi

    # Remove leading and trailing whitespace
    message=$(echo "$message" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')

    if [ "$verbose" = "true" ]; then
        log_info "Checking commit message: '$message'"
    fi

    # Define allowed commit types
    local valid_types="feat|fix|docs|style|refactor|test|chore|perf|ci"

    # Conventional Commits regular expression
    # Format: <type>[optional scope]: <description>
    local regex="^(${valid_types})(\([a-zA-Z0-9_-]+\))?: .{1,100}$"

    if [[ ! "$message" =~ $regex ]]; then
        log_error "Incorrect commit message format"
        echo ""
        echo "Correct format: <type>[optional scope]: <description>"
        echo ""
        echo "Examples:"
        echo "  feat(auth): add JWT token refresh mechanism"
        echo "  fix(api): handle null pointer in user service"
        echo "  docs(readme): update installation instructions"
        echo ""
        echo "Requirements:"
        echo "  - Type must be: ${valid_types//|/, }"
        echo "  - Description length: 1-100 characters"
        echo "  - Format: type(optional scope): description"
        echo ""
        return 1
    fi

    # Extract type and description
    local type=$(echo "$message" | sed -E "s/^(${valid_types})(\([^)]+\))?: .*/\1/")
    local scope=""
    local description=""

    # Extract scope (if exists)
    if echo "$message" | grep -q '('; then
        scope=$(echo "$message" | sed -E 's/^[^(]*\(([^)]+)\).*/\1/')
    fi

    description=$(echo "$message" | sed -E "s/^(${valid_types})(\([^)]+\))?: (.*)/\3/")

    if [ "$verbose" = "true" ]; then
        log_info "Type: $type"
        if [ -n "$scope" ]; then
            log_info "Scope: $scope"
        fi
        log_info "Description: $description"
    fi

    # Check if description starts with lowercase letter
    if [[ "$description" =~ ^[A-Z] ]]; then
        log_warning "Recommend starting description with lowercase letter"
    fi

    # Check if description ends with period
    if [[ "$description" =~ \.$$ ]]; then
        log_warning "Description should not end with period"
    fi

    # Check for common irregular words
    local bad_words=("fixed" "added" "updated" "changed")
    for word in "${bad_words[@]}"; do
        if [[ "$description" =~ ^$word ]]; then
            log_warning "Recommend using infinitive form instead of past tense: '$word' -> '${word%ed}'"
        fi
    done

    log_success "Commit message format is correct"
    return 0
}

# Main function
main() {
    local commit_message=""
    local from_file=false
    local verbose=false

    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -f|--file)
                from_file=true
                shift
                if [[ $# -gt 0 ]]; then
                    commit_message="$1"
                else
                    log_error "Option -f requires file path"
                    exit 1
                fi
                ;;
            -v|--verbose)
                verbose=true
                ;;
            -*)
                log_error "Unknown option: $1"
                show_help
                exit 1
                ;;
            *)
                if [ -z "$commit_message" ]; then
                    commit_message="$1"
                else
                    log_error "Can only specify one commit message"
                    exit 1
                fi
                ;;
        esac
        shift
    done

    # If reading from file
    if [ "$from_file" = true ]; then
        if [ ! -f "$commit_message" ]; then
            log_error "File does not exist: $commit_message"
            exit 1
        fi
        commit_message=$(head -n 1 "$commit_message")
    fi

    # If no commit message provided, try to read from stdin
    if [ -z "$commit_message" ]; then
        if [ -t 0 ]; then
            log_error "Please provide commit message"
            show_help
            exit 1
        else
            commit_message=$(head -n 1)
        fi
    fi

    # Check commit message
    if check_commit_message "$commit_message" "$verbose"; then
        exit 0
    else
        exit 1
    fi
}

# If script is executed directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi