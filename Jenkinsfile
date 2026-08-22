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
            set -e

            CONTAINER_NAME="patient-management-container"
            IMAGE_NAME="thasbidocker/patient-management-system"
            NEW_IMAGE="${IMAGE_NAME}:build-${BUILD_NUMBER}"
            BACKUP_FILE="/tmp/patient_previous_image.txt"

            echo "========================================"
            echo "Docker Deployment"
            echo "========================================"

            echo "New image: ${NEW_IMAGE}"

            if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"
            then
                PREVIOUS_IMAGE=$(docker inspect ${CONTAINER_NAME} \
                    --format='{{.Config.Image}}')

                echo "Previous image: ${PREVIOUS_IMAGE}"

                echo "${PREVIOUS_IMAGE}" > ${BACKUP_FILE}
            else
                echo "No previous container found."
                rm -f ${BACKUP_FILE}
            fi

            echo "Stopping old container..."

            docker stop ${CONTAINER_NAME} || true

            echo "Removing old container..."

            docker rm ${CONTAINER_NAME} || true

            echo "Pulling build image..."

            docker pull ${NEW_IMAGE}

            echo "Starting new container..."

            docker run -d \
                --name ${CONTAINER_NAME} \
                --add-host=host.docker.internal:host-gateway \
                -p 8081:8081 \
                ${NEW_IMAGE}

            echo "========================================"
            echo "Deployment completed"
            echo "Deployed: ${NEW_IMAGE}"
            echo "========================================"
        '''
    }
}
        

       stage('Deployment Verification') {

    steps {

        sh '''
            CONTAINER_NAME="patient-management-container"
            HEALTH_URL="http://localhost:8081/patients/getAllPatients"
            BACKUP_FILE="/tmp/patient_previous_image.txt"

            echo "========================================"
            echo "Deployment Verification"
            echo "========================================"

            echo "Checking container status..."

            docker ps --filter "name=${CONTAINER_NAME}"

            echo "Waiting for application to start..."

            DEPLOYMENT_SUCCESS=false

            for i in {1..12}
            do

                echo "Attempt $i: Checking application..."

                if curl --fail --silent ${HEALTH_URL} > /tmp/patient-response.json
                then

                    echo "Application is UP!"

                    cat /tmp/patient-response.json

                    DEPLOYMENT_SUCCESS=true

                    break

                fi

                echo "Application is not ready yet."

                sleep 5

            done


            if [ "$DEPLOYMENT_SUCCESS" = true ]
            then

                echo "========================================"
                echo "Deployment verification successful."
                echo "========================================"

                exit 0

            fi


            echo "========================================"
            echo "DEPLOYMENT FAILED"
            echo "========================================"

            echo "New container logs:"

            docker logs ${CONTAINER_NAME} || true


            if [ -f "${BACKUP_FILE}" ]
            then

                PREVIOUS_IMAGE=$(cat ${BACKUP_FILE})

                echo "========================================"
                echo "Starting Rollback"
                echo "========================================"

                echo "Previous image: ${PREVIOUS_IMAGE}"

                echo "Stopping failed container..."

                docker stop ${CONTAINER_NAME} || true

                echo "Removing failed container..."

                docker rm ${CONTAINER_NAME} || true

                echo "Pulling previous image..."

                docker pull ${PREVIOUS_IMAGE}

                echo "Starting previous version..."

                docker run -d \
                    --name ${CONTAINER_NAME} \
                    --add-host=host.docker.internal:host-gateway \
                    -p 8081:8081 \
                    ${PREVIOUS_IMAGE}

                echo "Rollback container started."

                echo "Waiting for rollback application..."

                ROLLBACK_SUCCESS=false

                for i in {1..12}
                do

                    echo "Rollback verification attempt $i..."

                    if curl --fail --silent ${HEALTH_URL} > /tmp/rollback-response.json
                    then

                        echo "========================================"
                        echo "ROLLBACK SUCCESSFUL"
                        echo "========================================"

                        cat /tmp/rollback-response.json

                        ROLLBACK_SUCCESS=true

                        break

                    fi

                    sleep 5

                done


                if [ "$ROLLBACK_SUCCESS" = true ]
                then

                    echo "Previous version successfully restored."

                else

                    echo "Rollback verification FAILED."

                    docker logs ${CONTAINER_NAME}

                    exit 1

                fi

            else

                echo "No previous image available."
                echo "Rollback cannot be performed."

                exit 1

            fi


            exit 1
        '''
    }
}

stage('Deployment Information') {

    steps {

        sh '''
            echo "========================================"
            echo "DEPLOYMENT INFORMATION"
            echo "========================================"

            echo "Jenkins Build:"
            echo "${BUILD_NUMBER}"

            echo "Git Commit:"
            git rev-parse --short HEAD

            echo "Docker Image:"
            docker inspect patient-management-container \
                --format='{{.Config.Image}}'

            echo "Container:"
            docker ps --filter "name=patient-management-container"

            echo "Application URL:"
            echo "http://localhost:8081/patients/getAllPatients"

            echo "========================================"
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
    
    post {

    success {

        echo '========================================'
        echo 'CI/CD PIPELINE COMPLETED SUCCESSFULLY'
        echo '========================================'

        sh '''
            echo "Running container:"
            docker ps --filter "name=patient-management-container"

            echo "Deployed image:"
            docker inspect patient-management-container \
                --format='{{.Config.Image}}'
        '''
    }

    failure {

        echo '========================================'
        echo 'CI/CD PIPELINE FAILED'
        echo '========================================'

        sh '''
            echo "Container status:"
            docker ps -a --filter "name=patient-management-container"
        '''
    }

    always {

        echo 'Pipeline execution completed.'
    }
}
}