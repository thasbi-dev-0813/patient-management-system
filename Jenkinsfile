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

stage('Deploy') {
    steps {
        sh '''
            echo "Deploying Patient Management System..."

            cp target/PatientManagementSystem-0.0.1-SNAPSHOT.jar \
               /opt/patient-management/PatientManagementSystem.jar

            echo "JAR deployed successfully"
        '''
    }
}
    }
}