pipeline {
  agent any

  environment {

    SPRING_DB_USER = vault path: 'secret/myapp', key: 'SPRING_DB_USER'
    SPRING_DB_PASS = vault path: 'secret/myapp', key: 'SPRING_DB_PASS'
  }

  stages {

    stage('Clone Repository') {
      steps {
        git branch: 'master', url: 'https://github.com/Anasshbt/taskmanager.git'
      }
    }

    stage('Build') {
      steps {
        sh './mvnw clean package -Dspring.datasource.username=$SPRING_DB_USER -Dspring.datasource.password=$SPRING_DB_PASS'
      }
    }

    stage('Deploy') {
      steps {
        echo "Ready to deploy with DB user: $SPRING_DB_USER"
        // Deploy logic here (Ansible, SCP, etc.)
      }
    }
  }
}
