#!/bin/bash

# URL Shortener Kubernetes Deployment Script
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="url-shortener"
APP_NAME="url-shortener"
IMAGE_NAME="url-shortener:latest"

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

# Check if kubectl is installed
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed. Please install kubectl first."
        exit 1
    fi
    log_success "kubectl is available"
}

# Check if Docker is installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    log_success "Docker is available"
}

# Build Docker image
build_image() {
    log_info "Building Docker image..."
    docker build -t $IMAGE_NAME .
    log_success "Docker image built successfully"
}

# Create namespace
create_namespace() {
    log_info "Creating namespace..."
    kubectl apply -f k8s/namespace.yaml
    log_success "Namespace created/updated"
}

# Deploy ConfigMap
deploy_configmap() {
    log_info "Deploying ConfigMap..."
    kubectl apply -f k8s/configmap.yaml
    log_success "ConfigMap deployed"
}

# Deploy Secret
deploy_secret() {
    log_info "Deploying Secret..."
    kubectl apply -f k8s/secret.yaml
    log_success "Secret deployed"
}

# Deploy application
deploy_app() {
    log_info "Deploying application..."
    kubectl apply -f k8s/deployment.yaml
    log_success "Application deployed"
}

# Deploy Ingress
deploy_ingress() {
    log_info "Deploying Ingress..."
    kubectl apply -f k8s/ingress.yaml
    log_success "Ingress deployed"
}

# Deploy HPA
deploy_hpa() {
    log_info "Deploying HPA..."
    kubectl apply -f k8s/hpa.yaml
    log_success "HPA deployed"
}

# Wait for deployment to be ready
wait_for_deployment() {
    log_info "Waiting for deployment to be ready..."
    kubectl wait --for=condition=available --timeout=300s deployment/url-shortener-deployment -n $NAMESPACE
    log_success "Deployment is ready"
}

# Show deployment status
show_status() {
    log_info "Deployment Status:"
    echo "===================="
    kubectl get pods -n $NAMESPACE
    echo ""
    kubectl get services -n $NAMESPACE
    echo ""
    kubectl get ingress -n $NAMESPACE
    echo ""
    kubectl get hpa -n $NAMESPACE
}

# Get service URL
get_service_url() {
    log_info "Service Information:"
    echo "===================="
    
    # Check if ingress is available
    INGRESS_HOST=$(kubectl get ingress url-shortener-ingress -n $NAMESPACE -o jsonpath='{.spec.rules[0].host}' 2>/dev/null || echo "")
    
    if [ -n "$INGRESS_HOST" ]; then
        echo "External URL: http://$INGRESS_HOST"
        echo "Add this to your /etc/hosts file:"
        echo "127.0.0.1 $INGRESS_HOST"
    else
        log_warning "Ingress not configured. Using port-forward for access."
        echo "Run: kubectl port-forward service/url-shortener-service 8000:80 -n $NAMESPACE"
        echo "Then access: http://localhost:8000"
    fi
}

# Cleanup function
cleanup() {
    log_warning "Cleaning up resources..."
    kubectl delete -f k8s/ --ignore-not-found=true
    log_success "Cleanup completed"
}

# Main deployment function
deploy() {
    log_info "Starting URL Shortener deployment..."
    
    check_kubectl
    check_docker
    build_image
    create_namespace
    deploy_configmap
    deploy_secret
    deploy_app
    deploy_ingress
    deploy_hpa
    wait_for_deployment
    show_status
    get_service_url
    
    log_success "Deployment completed successfully!"
}

# Help function
show_help() {
    echo "URL Shortener Deployment Script"
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  deploy    Deploy the application to Kubernetes"
    echo "  cleanup   Remove all resources from Kubernetes"
    echo "  status    Show current deployment status"
    echo "  logs      Show application logs"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 deploy"
    echo "  $0 cleanup"
    echo "  $0 status"
}

# Show logs
show_logs() {
    log_info "Showing application logs..."
    kubectl logs -f deployment/url-shortener-deployment -n $NAMESPACE
}

# Main script logic
case "${1:-deploy}" in
    "deploy")
        deploy
        ;;
    "cleanup")
        cleanup
        ;;
    "status")
        show_status
        ;;
    "logs")
        show_logs
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