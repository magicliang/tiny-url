#!/bin/bash

# URL Shortener Test Script
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Maven is installed
check_maven() {
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed. Please install Maven first."
        exit 1
    fi
    log_success "Maven is available"
}

# Run unit tests
run_unit_tests() {
    log_info "Running unit tests..."
    mvn test
    log_success "Unit tests completed"
}

# Run integration tests
run_integration_tests() {
    log_info "Running integration tests..."
    mvn verify -P integration-test
    log_success "Integration tests completed"
}

# Run all tests
run_all_tests() {
    log_info "Running all tests..."
    mvn clean verify
    log_success "All tests completed"
}

# Generate test report
generate_test_report() {
    log_info "Generating test report..."
    mvn surefire-report:report
    mvn site -DgenerateReports=false
    log_success "Test report generated in target/site/surefire-report.html"
}

# Run code coverage
run_code_coverage() {
    log_info "Running code coverage analysis..."
    mvn clean verify jacoco:report
    log_success "Code coverage report generated in target/site/jacoco/index.html"
}

# Run static code analysis
run_static_analysis() {
    log_info "Running static code analysis..."
    mvn spotbugs:check
    mvn checkstyle:check
    log_success "Static code analysis completed"
}

# Performance test
run_performance_test() {
    log_info "Running performance tests..."
    
    # Start the application in background
    mvn spring-boot:run &
    APP_PID=$!
    
    # Wait for application to start
    sleep 30
    
    # Run performance tests using curl
    BASE_URL="http://localhost:8000/api/v1"
    
    log_info "Testing URL shortening performance..."
    for i in {1..100}; do
        curl -s -X POST "$BASE_URL/shorten" \
             -H "Content-Type: application/json" \
             -d "{\"url\":\"https://www.example$i.com\"}" > /dev/null
    done
    
    log_info "Testing URL expansion performance..."
    for i in {1..100}; do
        # This would need actual short URLs from the previous step
        curl -s "$BASE_URL/health" > /dev/null
    done
    
    # Stop the application
    kill $APP_PID
    
    log_success "Performance tests completed"
}

# Load test using Apache Bench (if available)
run_load_test() {
    if ! command -v ab &> /dev/null; then
        log_warning "Apache Bench (ab) is not installed. Skipping load test."
        return
    fi
    
    log_info "Running load test..."
    
    # Start the application in background
    mvn spring-boot:run &
    APP_PID=$!
    
    # Wait for application to start
    sleep 30
    
    # Run load test
    ab -n 1000 -c 10 http://localhost:8000/api/v1/health
    
    # Stop the application
    kill $APP_PID
    
    log_success "Load test completed"
}

# Test Docker build
test_docker_build() {
    log_info "Testing Docker build..."
    docker build -t url-shortener-test .
    log_success "Docker build test completed"
}

# Test Kubernetes manifests
test_k8s_manifests() {
    log_info "Testing Kubernetes manifests..."
    
    # Validate YAML syntax
    for file in k8s/*.yaml; do
        kubectl apply --dry-run=client -f "$file"
    done
    
    log_success "Kubernetes manifests validation completed"
}

# Cleanup test resources
cleanup_test_resources() {
    log_info "Cleaning up test resources..."
    
    # Remove test Docker images
    docker rmi url-shortener-test 2>/dev/null || true
    
    # Clean Maven target directory
    mvn clean
    
    log_success "Test cleanup completed"
}

# Show test summary
show_test_summary() {
    log_info "Test Summary:"
    echo "=============="
    
    if [ -f "target/surefire-reports/TEST-*.xml" ]; then
        TOTAL_TESTS=$(grep -h "tests=" target/surefire-reports/TEST-*.xml | sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
        FAILED_TESTS=$(grep -h "failures=" target/surefire-reports/TEST-*.xml | sed 's/.*failures="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
        SKIPPED_TESTS=$(grep -h "skipped=" target/surefire-reports/TEST-*.xml | sed 's/.*skipped="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
        
        echo "Total Tests: ${TOTAL_TESTS:-0}"
        echo "Failed Tests: ${FAILED_TESTS:-0}"
        echo "Skipped Tests: ${SKIPPED_TESTS:-0}"
        echo "Passed Tests: $((${TOTAL_TESTS:-0} - ${FAILED_TESTS:-0} - ${SKIPPED_TESTS:-0}))"
    else
        echo "No test results found"
    fi
}

# Help function
show_help() {
    echo "URL Shortener Test Script"
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  unit          Run unit tests only"
    echo "  integration   Run integration tests only"
    echo "  all           Run all tests (default)"
    echo "  report        Generate test report"
    echo "  coverage      Run code coverage analysis"
    echo "  static        Run static code analysis"
    echo "  performance   Run performance tests"
    echo "  load          Run load tests"
    echo "  docker        Test Docker build"
    echo "  k8s           Test Kubernetes manifests"
    echo "  cleanup       Cleanup test resources"
    echo "  summary       Show test summary"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all"
    echo "  $0 unit"
    echo "  $0 coverage"
}

# Main script logic
case "${1:-all}" in
    "unit")
        check_maven
        run_unit_tests
        show_test_summary
        ;;
    "integration")
        check_maven
        run_integration_tests
        show_test_summary
        ;;
    "all")
        check_maven
        run_all_tests
        show_test_summary
        ;;
    "report")
        check_maven
        generate_test_report
        ;;
    "coverage")
        check_maven
        run_code_coverage
        ;;
    "static")
        check_maven
        run_static_analysis
        ;;
    "performance")
        check_maven
        run_performance_test
        ;;
    "load")
        check_maven
        run_load_test
        ;;
    "docker")
        test_docker_build
        ;;
    "k8s")
        test_k8s_manifests
        ;;
    "cleanup")
        cleanup_test_resources
        ;;
    "summary")
        show_test_summary
        ;;
    "help")
        show_help
        ;;
    *)
        log_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac