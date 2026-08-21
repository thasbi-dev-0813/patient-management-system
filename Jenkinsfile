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

        stage('Deploy') {
    steps {
        sh '''
            echo "Deploying Patient Management System..."

            # Stop old container if running
            docker stop patient-management-container || true

            # Remove old container
            docker rm patient-management-container || true

            # Build new Docker image
            docker build -t patient-management-system:1.0 .

            # Start new container
            docker run -d \
  --name patient-management-container \
  --add-host=host.docker.internal:host-gateway \
  -p 8081:8081 \
  patient-management-system:1.0

            echo "Patient Management System deployed successfully"
        '''
    }
}

stage('Verify Deployment') {
    steps {
        sh '''
            echo "Waiting for application to start..."
            sleep 10

            echo "Checking container status..."
            docker ps --filter "name=patient-management-container"

            echo "Checking application..."
            curl -f http://localhost:8081/patients/getAllPatients

            echo "Application is running successfully!"
        '''
    }
}
    }
}