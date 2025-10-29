pipeline {
    agent any

    environment {
        APP_NAME = "smartdoc"
        DOCKER_COMPOSE_FILE = "docker-compose.yml"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                echo "Building Spring Boot JAR..."
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image using docker-compose..."
                sh "docker-compose -f ${DOCKER_COMPOSE_FILE} build app"
            }
        }

        stage('Deploy Stack') {
            steps {
                echo "Deploying stack with docker-compose..."
                sh "docker-compose -f ${DOCKER_COMPOSE_FILE} down"
                sh "docker-compose -f ${DOCKER_COMPOSE_FILE} up -d"
            }
        }
    }

    post {
        success {
            echo "SmartDoc successfully built and deployed!"
        }
        failure {
            echo "Build or deployment failed. Check Jenkins logs."
        }
    }
}
