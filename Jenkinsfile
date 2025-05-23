pipeline {
    agent any

    environment {
        // Configuration Maven
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {
        stage('Checkout') {
            steps {
                // Récupération du code source
                checkout scm
            }
        }

        stage('Get Secrets from Vault') {
            steps {
                script {
                    // Version alternative avec chemin simplifié
                    def secrets = [
                        [
                            path: 'kv/myapp', // Essayez sans /data/
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
                            vaultUrl: 'http://5.189.152.120:8200',
                            vaultCredentialId: 'vault-secret',
                            engineVersion: 2
                        ],
                        vaultSecrets: secrets
                    ]) {
                        echo "✅ Secrets récupérés avec succès depuis Vault"
                        echo "DB User: ${env.SPRING_DB_USER}"
                        echo "DB Password: [MASKED]"

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
                    // Tests avec les variables d'environnement correctes
                    withEnv([
                        "SPRING_DB_USER=${env.DATABASE_USER}",
                        "SPRING_DB_PASS=${env.DATABASE_PASS}",
                        "SPRING_PROFILES_ACTIVE=test"
                    ]) {
                        echo "Exécution des tests Maven..."
                        sh './mvnw test'
                    }
                }
            }
            post {
                always {
                    // Publication des résultats de tests Maven
                    publishTestResults testResultsPattern: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package Application') {
            steps {
                script {
                    withEnv([
                        "SPRING_DB_USER=${env.DATABASE_USER}",
                        "SPRING_DB_PASS=${env.DATABASE_PASS}"
                    ]) {
                        echo "Création du JAR Spring Boot..."
                        sh './mvnw spring-boot:repackage'
                    }
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                script {
                    // Archive du JAR généré par Maven
                    echo "Archivage des artifacts..."
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true

                    // Affichage des informations sur le JAR créé
                    sh 'ls -la target/*.jar'
                }
            }
        }
    }

    post {
        always {
            // Nettoyage des variables sensibles
            script {
                env.DATABASE_USER = null
                env.DATABASE_PASS = null
                env.SPRING_DB_USER = null
                env.SPRING_DB_PASS = null
            }

            echo "Nettoyage terminé"
        }

        success {
            echo "✅ Build réussi ! Application Spring Boot PostgreSQL compilée avec succès."
            echo "JAR disponible dans target/"
        }

        failure {
            echo "❌ Build échoué. Vérifiez les logs pour plus de détails."
            echo "Vérifiez la connexion à Vault et à PostgreSQL"
        }
    }
}