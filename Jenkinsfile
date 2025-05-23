pipeline {
    agent any

    environment {
        // Configuration Maven
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {
        stage('Checkout') {
            steps {

                checkout scm
            }
        }

        stage('Get Secrets from Vault') {
            steps {
                script {
                    // Récupération des secrets depuis Vault
                    def secrets = [
                        [
                            path: 'kv/data/myapp', // Chemin dans Vault
                            engineVersion: 2,
                            secretValues: [
                                [envVar: 'SPRING_DB_USER', vaultKey: 'SPRING_DB_USER'],
                                [envVar: 'SPRING_DB_PASS', vaultKey: 'SPRING_DB_PASS']
                            ]
                        ]
                    ]

                    // Utilisation du plugin Vault
                    withVault([
                        configuration: [
                            vaultUrl: 'http://5.189.152.120:8200', // URL de votre serveur Vault
                            vaultCredentialId: 'vault-secret', // ID des credentials Vault dans Jenkins
                            engineVersion: 2
                        ],
                        vaultSecrets: secrets
                    ]) {
                        // Les variables sont maintenant disponibles
                        echo "Secrets récupérés avec succès depuis Vault"
                        echo "DB User: ${env.SPRING_DB_USER}"
                        // N'affichez jamais le mot de passe dans les logs !
                        echo "DB Password: [MASKED]"

                        // Variables disponibles pour les étapes suivantes
                        env.DATABASE_USER = env.SPRING_DB_USER
                        env.DATABASE_PASS = env.SPRING_DB_PASS
                    }
                }
            }
        }

        stage('Build Spring Boot App') {
            steps {
                script {
                    // Configuration des variables d'environnement pour Spring Boot
                    // Les variables SPRING_DB_USER et SPRING_DB_PASS sont utilisées directement
                    // dans votre application.properties
                    withEnv([
                        "SPRING_DB_USER=${env.DATABASE_USER}",
                        "SPRING_DB_PASS=${env.DATABASE_PASS}",
                        "SPRING_PROFILES_ACTIVE=prod"
                    ]) {

                        echo "Building Spring Boot app with Maven..."
                        echo "Database URL: jdbc:postgresql://pfe-ocp-group.c.aivencloud.com:10688/defaultdb"
                        echo "Database User: ${env.SPRING_DB_USER}"

                        // Build avec Maven
                        sh '''
                            echo "Compilation du projet..."
                            ./mvnw clean compile

                            echo "Packaging de l'application..."
                            ./mvnw package -DskipTests
                        '''
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    withEnv([
                        "SPRING_DATASOURCE_USERNAME=${env.DB_USERNAME}",
                        "SPRING_DATASOURCE_PASSWORD=${env.DB_PASSWORD}",
                        "SPRING_PROFILES_ACTIVE=test"
                    ]) {
                        if (fileExists('pom.xml')) {
                            sh './mvnw test'
                        } else {
                            sh './gradlew test'
                        }
                    }
                }
            }
            post {
                always {
                    // Publication des résultats de tests
                    publishTestResults testResultsPattern: '**/target/surefire-reports/*.xml'
                    publishTestResults testResultsPattern: '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Package Application') {
            steps {
                script {
                    if (fileExists('pom.xml')) {
                        sh './mvnw spring-boot:repackage'
                    } else {
                        sh './gradlew bootJar'
                    }
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                script {
                    // Archive du JAR généré
                    if (fileExists('target/*.jar')) {
                        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    }
                    if (fileExists('build/libs/*.jar')) {
                        archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                    }
                }
            }
        }
    }

    post {
        always {
            // Nettoyage des variables sensibles
            script {
                env.DB_USERNAME = null
                env.DB_PASSWORD = null
                env.SPRING_DB_USER = null
                env.SPRING_DB_PASS = null
            }

            // Nettoyage de l'workspace si nécessaire
            cleanWs()
        }

        success {
            echo "Build réussi ! Application Spring Boot compilée avec succès."
        }

        failure {
            echo "Build échoué. Vérifiez les logs pour plus de détails."
        }
    }
}