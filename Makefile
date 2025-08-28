# URL Shortener Service Makefile

# Variables
APP_NAME := url-shortener
VERSION := 1.0.0
DOCKER_IMAGE := $(APP_NAME):$(VERSION)
DOCKER_IMAGE_LATEST := $(APP_NAME):latest
NAMESPACE := url-shortener
MAVEN_OPTS := -Dmaven.test.skip=false

# Colors
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[1;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

.PHONY: help build test clean package docker-build docker-run k8s-deploy k8s-cleanup dev-setup

# Default target
.DEFAULT_GOAL := help

## Help
help: ## Show this help message
	@echo "$(BLUE)URL Shortener Service$(NC)"
	@echo "====================="
	@echo ""
	@echo "Available targets:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)

## Development
dev-setup: ## Setup development environment
	@echo "$(BLUE)Setting up development environment...$(NC)"
	@chmod +x scripts/*.sh
	@mvn dependency:resolve
	@echo "$(GREEN)Development environment ready!$(NC)"

clean: ## Clean build artifacts
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	@mvn clean
	@docker system prune -f
	@echo "$(GREEN)Clean completed!$(NC)"

compile: ## Compile the application
	@echo "$(BLUE)Compiling application...$(NC)"
	@mvn compile
	@echo "$(GREEN)Compilation completed!$(NC)"

## Testing
test: ## Run all tests
	@echo "$(BLUE)Running all tests...$(NC)"
	@./scripts/test.sh all

test-unit: ## Run unit tests only
	@echo "$(BLUE)Running unit tests...$(NC)"
	@./scripts/test.sh unit

test-integration: ## Run integration tests only
	@echo "$(BLUE)Running integration tests...$(NC)"
	@./scripts/test.sh integration

test-coverage: ## Generate test coverage report
	@echo "$(BLUE)Generating test coverage report...$(NC)"
	@./scripts/test.sh coverage
	@echo "$(GREEN)Coverage report: target/site/jacoco/index.html$(NC)"

## Building
build: clean compile test ## Build the application
	@echo "$(BLUE)Building application...$(NC)"
	@mvn package -DskipTests
	@echo "$(GREEN)Build completed!$(NC)"

package: ## Package the application
	@echo "$(BLUE)Packaging application...$(NC)"
	@mvn package
	@echo "$(GREEN)Package created: target/$(APP_NAME)-$(VERSION).jar$(NC)"

## Docker
docker-build: ## Build Docker image
	@echo "$(BLUE)Building Docker image...$(NC)"
	@docker build -t $(DOCKER_IMAGE) -t $(DOCKER_IMAGE_LATEST) .
	@echo "$(GREEN)Docker image built: $(DOCKER_IMAGE)$(NC)"

docker-run: docker-build ## Run application in Docker
	@echo "$(BLUE)Running application in Docker...$(NC)"
	@docker-compose up -d
	@echo "$(GREEN)Application running at http://localhost:8000$(NC)"

docker-stop: ## Stop Docker containers
	@echo "$(BLUE)Stopping Docker containers...$(NC)"
	@docker-compose down
	@echo "$(GREEN)Docker containers stopped!$(NC)"

docker-logs: ## Show Docker logs
	@docker-compose logs -f

## Local Development
run: ## Run application locally
	@echo "$(BLUE)Starting application locally...$(NC)"
	@mvn spring-boot:run -Dspring.profiles.active=dev
	
run-prod: ## Run application in production mode
	@echo "$(BLUE)Starting application in production mode...$(NC)"
	@mvn spring-boot:run -Dspring.profiles.active=prod

debug: ## Run application in debug mode
	@echo "$(BLUE)Starting application in debug mode...$(NC)"
	@mvn spring-boot:run -Dspring.profiles.active=dev -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

## Kubernetes
k8s-deploy: docker-build ## Deploy to Kubernetes
	@echo "$(BLUE)Deploying to Kubernetes...$(NC)"
	@./scripts/deploy.sh deploy
	@echo "$(GREEN)Deployment completed!$(NC)"

k8s-status: ## Show Kubernetes deployment status
	@echo "$(BLUE)Kubernetes deployment status:$(NC)"
	@./scripts/deploy.sh status

k8s-logs: ## Show Kubernetes logs
	@./scripts/deploy.sh logs

k8s-cleanup: ## Cleanup Kubernetes resources
	@echo "$(BLUE)Cleaning up Kubernetes resources...$(NC)"
	@./scripts/deploy.sh cleanup
	@echo "$(GREEN)Cleanup completed!$(NC)"

k8s-port-forward: ## Port forward to Kubernetes service
	@echo "$(BLUE)Port forwarding to Kubernetes service...$(NC)"
	@kubectl port-forward service/url-shortener-service 8000:80 -n $(NAMESPACE)

## Database
db-init: ## Initialize database with sample data
	@echo "$(BLUE)Initializing database...$(NC)"
	@mysql -h 11.142.154.110 -P 3306 -u with_racdjgzrtmhgtadh -p'K#2C@Y3pTOpOJu' 6n9pyl60 < scripts/init.sql
	@echo "$(GREEN)Database initialized!$(NC)"

db-connect: ## Connect to database
	@echo "$(BLUE)Connecting to database...$(NC)"
	@mysql -h 11.142.154.110 -P 3306 -u with_racdjgzrtmhgtadh -p'K#2C@Y3pTOpOJu' 6n9pyl60

## Quality Assurance
lint: ## Run code linting
	@echo "$(BLUE)Running code linting...$(NC)"
	@mvn checkstyle:check
	@mvn spotbugs:check
	@echo "$(GREEN)Linting completed!$(NC)"

format: ## Format code
	@echo "$(BLUE)Formatting code...$(NC)"
	@mvn fmt:format
	@echo "$(GREEN)Code formatted!$(NC)"

security-scan: ## Run security scan
	@echo "$(BLUE)Running security scan...$(NC)"
	@mvn org.owasp:dependency-check-maven:check
	@echo "$(GREEN)Security scan completed!$(NC)"

## Performance
perf-test: ## Run performance tests
	@echo "$(BLUE)Running performance tests...$(NC)"
	@./scripts/test.sh performance

load-test: ## Run load tests
	@echo "$(BLUE)Running load tests...$(NC)"
	@./scripts/test.sh load

## Monitoring
health-check: ## Check application health
	@echo "$(BLUE)Checking application health...$(NC)"
	@curl -f http://localhost:8000/api/v1/health || echo "$(RED)Application is not healthy$(NC)"

metrics: ## Show application metrics
	@echo "$(BLUE)Application metrics:$(NC)"
	@curl -s http://localhost:8000/actuator/metrics | jq .

## Documentation
docs: ## Generate documentation
	@echo "$(BLUE)Generating documentation...$(NC)"
	@mvn javadoc:javadoc
	@echo "$(GREEN)Documentation generated: target/site/apidocs/index.html$(NC)"

api-docs: ## Generate API documentation
	@echo "$(BLUE)Generating API documentation...$(NC)"
	@mvn spring-boot:run -Dspring.profiles.active=dev &
	@sleep 30
	@curl -s http://localhost:8000/v3/api-docs > api-docs.json
	@pkill -f spring-boot:run
	@echo "$(GREEN)API documentation: api-docs.json$(NC)"

## Release
release: clean test build docker-build ## Create a release
	@echo "$(BLUE)Creating release $(VERSION)...$(NC)"
	@git tag -a v$(VERSION) -m "Release version $(VERSION)"
	@echo "$(GREEN)Release $(VERSION) created!$(NC)"
	@echo "$(YELLOW)Don't forget to push the tag: git push origin v$(VERSION)$(NC)"

## Utilities
logs: ## Show application logs
	@tail -f logs/url-shortener.log

watch-logs: ## Watch application logs
	@watch -n 1 'tail -20 logs/url-shortener.log'

ps: ## Show running processes
	@ps aux | grep -E "(java|spring-boot|url-shortener)" | grep -v grep

kill: ## Kill running application processes
	@pkill -f "url-shortener" || echo "No processes to kill"

## Environment
env-check: ## Check environment setup
	@echo "$(BLUE)Checking environment...$(NC)"
	@echo "Java version:"
	@java -version
	@echo ""
	@echo "Maven version:"
	@mvn -version
	@echo ""
	@echo "Docker version:"
	@docker --version
	@echo ""
	@echo "Kubernetes version:"
	@kubectl version --client
	@echo "$(GREEN)Environment check completed!$(NC)"

## All-in-one commands
dev: clean compile run ## Full development workflow

ci: clean test build docker-build ## Continuous integration workflow

cd: ci k8s-deploy ## Continuous deployment workflow

full-test: clean test-unit test-integration test-coverage lint security-scan ## Full test suite