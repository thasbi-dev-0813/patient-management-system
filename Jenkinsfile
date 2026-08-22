pipeline {
    agent any
    
    triggers {
        githubPush()
    }
    
    environment {
    DOCKER_HUB_USERNAME = 'thasbidocker'
}

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

            docker build -t patient-management-system:build-${BUILD_NUMBER} .
            docker tag patient-management-system:build-${BUILD_NUMBER} ${DOCKER_HUB_USERNAME}/patient-management-system:build-${BUILD_NUMBER}
            docker tag patient-management-system:build-${BUILD_NUMBER} ${DOCKER_HUB_USERNAME}/patient-management-system:latest

            echo "Docker image built successfully"
        '''
    }
}

        stage('Docker Push') {
    steps {
        withCredentials([usernamePassword(
            credentialsId: 'dockerhub-credentials',
            usernameVariable: 'DOCKER_USERNAME',
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {
            sh '''
                echo "Logging in to Docker Hub..."

                echo "$DOCKER_PASSWORD" | docker login \
                    -u "$DOCKER_USERNAME" \
                    --password-stdin

                echo "Pushing build image..."
                docker push thasbidocker/patient-management-system:build-${BUILD_NUMBER}

                echo "Pushing latest image..."
                docker push thasbidocker/patient-management-system:latest

                docker logout

                echo "Docker images pushed successfully"
            '''
        }
    }
}
        
        stage('Docker Deploy') {
    steps {
        sh '''
            docker stop patient-management-container || true
            docker rm patient-management-container || true

            docker run -d \
              --name patient-management-container \
              --add-host=host.docker.internal:host-gateway \
              -p 8081:8081 \
              patient-management-system:build-${BUILD_NUMBER}
        '''
    }
}


        stage('Deployment Verification') {
    steps {
        sh '''
            echo "Waiting for application to start..."

            for i in {1..12}
            do
                echo "Attempt $i: Checking application..."

                if curl --fail --silent http://localhost:8081/patients/getAllPatients > /tmp/patient-response.json
                then
                    echo "Application is UP!"
                    cat /tmp/patient-response.json
                    break
                fi

                if [ $i -eq 12 ]
                then
                    echo "Application failed to start."
                    docker logs patient-management-container
                    exit 1
                fi

                sleep 5
            done

            echo "Deployment verification successful."
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
            echo "Waiting for Patient Management System to start..."

            for i in $(seq 1 12); do
                echo "Health check attempt $i/12..."

                if curl -f --max-time 5 http://localhost:8081/patients/getAllPatients; then
                    echo ""
                    echo "Patient Management System is running successfully!"
                    exit 0
                fi

                echo "Application not ready yet. Waiting 5 seconds..."
                sleep 5
            done

            echo "Application failed to respond after 60 seconds."
            echo "Container status:"
            docker ps -a --filter name=patient-management-container

            echo "Container logs:"
            docker logs patient-management-container

            exit 1
        '''
    }
}
    }
}