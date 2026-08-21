pipeline {
    agent any

    tools {
        maven 'Maven-3.9.9'
        jdk 'JDK-21'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                        -Dsonar.projectKey=PatientManagementSystem
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    echo "Building Docker image..."

                    docker build \
                        -t patient-management-system:1.0 .

                    echo "Docker image built successfully"
                '''
            }
        }

        stage('Docker Deploy') {
            steps {
                sh '''
                    echo "Deploying Patient Management System..."

                    docker stop patient-management-container || true
                    docker rm patient-management-container || true

                    docker run -d \
                        --name patient-management-container \
                        --add-host=host.docker.internal:host-gateway \
                        -p 8082:8081 \
                        patient-management-system:1.0

                    echo "Docker container deployed successfully"

                    docker ps
                '''
            }
        }
    }
}