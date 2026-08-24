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
        
        stage('Unit Tests') {
    steps {
        sh '''
            echo "========================================"
            echo "Running Unit Tests"
            echo "========================================"

            mvn test

            echo "========================================"
            echo "Unit Tests Completed Successfully"
            echo "========================================"
        '''
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
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

            # Check whether an existing container exists
            if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then

                PREVIOUS_IMAGE=$(docker inspect ${CONTAINER_NAME} --format='{{.Config.Image}}')

                echo "Previous image: ${PREVIOUS_IMAGE}"

                # Save previous image for rollback
                echo "${PREVIOUS_IMAGE}" > ${BACKUP_FILE}

                echo "Stopping old container..."
                docker stop ${CONTAINER_NAME}

                echo "Removing old container..."
                docker rm ${CONTAINER_NAME}

            else
                echo "No previous container found."
                rm -f ${BACKUP_FILE}
            fi

            echo "Pulling new image..."
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

       stage('API Smoke Test') { 
		steps { 
			echo '========================================'
			echo 'Running API Smoke Test'
			echo '========================================'
			
			sh '''
			echo "Checking Patient Management API..." 
			
			RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" \ http://localhost:8081/patients/getAllPatients)
			
			echo "HTTP Response Code: $RESPONSE" 
			
			if [ "$RESPONSE" -ge 200 ] && [ "$RESPONSE" -lt 300 ]; then
			     echo "API Smoke Test PASSED"
			
			else
			     echo "API Smoke Test FAILED" exit 1 fi ''' 
			     
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

            docker ps --filter name=${CONTAINER_NAME}

            echo "Waiting for application to start..."

            DEPLOYMENT_SUCCESS=false

            for i in $(seq 1 12); do

                echo "Attempt $i: Checking application..."

                if curl --fail --silent ${HEALTH_URL}; then
                    DEPLOYMENT_SUCCESS=true
                    echo ""
                    echo "Application is healthy!"
                    break
                fi

                echo "Application is not ready yet."
                sleep 5
            done

            if [ "$DEPLOYMENT_SUCCESS" = "true" ]; then

                echo "========================================"
                echo "DEPLOYMENT SUCCESSFUL"
                echo "========================================"

            else

                echo "========================================"
                echo "DEPLOYMENT FAILED"
                echo "========================================"

                echo "New container logs:"
                docker logs ${CONTAINER_NAME}

                if [ -f "${BACKUP_FILE}" ]; then

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

                    echo "Starting previous container..."

                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        --add-host=host.docker.internal:host-gateway \
                        -p 8081:8081 \
                        ${PREVIOUS_IMAGE}

                    echo "Rollback container started."

                    sleep 10

                    echo "Checking rollback..."

                    if curl --fail --silent ${HEALTH_URL}; then

                        echo ""
                        echo "========================================"
                        echo "ROLLBACK SUCCESSFUL"
                        echo "========================================"
                        echo "Restored image: ${PREVIOUS_IMAGE}"

                    else

                        echo ""
                        echo "========================================"
                        echo "ROLLBACK FAILED"
                        echo "========================================"

                        docker logs ${CONTAINER_NAME}

                        exit 1
                    fi

                else

                    echo "No previous image available for rollback."
                    exit 1

                fi
            fi
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