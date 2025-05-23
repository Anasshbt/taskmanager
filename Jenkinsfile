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

        stage('Debug Application Properties') {
            steps {
                script {
                    echo "Vérification du fichier application.properties..."

                    // Vérification que le fichier existe
                    bat 'dir src\\main\\resources\\application.properties'

                    // Affichage du contenu (masquer les mots de passe)
                    bat 'type src\\main\\resources\\application.properties'

                    // Vérification de l'encodage du fichier
                    bat 'chcp'
                }
            }
        }

        stage('Debug Maven & Network') {
            steps {
                script {
                    echo "Diagnostic Maven et connectivité réseau..."

                    // Vérification de Maven
                    bat 'mvnw.cmd --version'

                    // Test de connectivité Maven Central
                    bat 'ping -n 4 repo.maven.apache.org'

                    // Vérification des répertoires Maven
                    bat 'echo %USERPROFILE%\\.m2'
                    bat 'dir "%USERPROFILE%\\.m2" 2>nul || echo "Répertoire .m2 n\'existe pas"'

                    // Test de téléchargement simple
                    bat 'curl -I https://repo.maven.apache.org/maven2/ || echo "Curl failed"'
                }
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

                        // Tentative 1: Build normal avec refresh des dépendances
                        script {
                            try {
                                bat '''
                                    echo "=== TENTATIVE 1: Build standard avec refresh ==="
                                    mvnw.cmd clean compile -U ^
                                        -Dproject.build.sourceEncoding=UTF-8 ^
                                        -Dproject.reporting.outputEncoding=UTF-8 ^
                                        -Dfile.encoding=UTF-8

                                    echo "Packaging de l'application..."
                                    mvnw.cmd package -DskipTests ^
                                        -Dproject.build.sourceEncoding=UTF-8 ^
                                        -Dproject.reporting.outputEncoding=UTF-8 ^
                                        -Dfile.encoding=UTF-8
                                '''
                            } catch (Exception e) {
                                echo "❌ Tentative 1 échouée: ${e.getMessage()}"
                                echo "🔄 Essai avec nettoyage du cache Maven..."

                                // Tentative 2: Nettoyage complet du cache
                                bat '''
                                    echo "=== TENTATIVE 2: Nettoyage cache et rebuild ==="
                                    echo "Suppression du cache Maven local..."
                                    if exist "%USERPROFILE%\\.m2\\repository" rmdir /s /q "%USERPROFILE%\\.m2\\repository"

                                    echo "Build avec téléchargement forcé..."
                                    mvnw.cmd clean compile -U -o false ^
                                        -Dproject.build.sourceEncoding=UTF-8 ^
                                        -Dproject.reporting.outputEncoding=UTF-8 ^
                                        -Dfile.encoding=UTF-8

                                    mvnw.cmd package -DskipTests ^
                                        -Dproject.build.sourceEncoding=UTF-8 ^
                                        -Dproject.reporting.outputEncoding=UTF-8 ^
                                        -Dfile.encoding=UTF-8
                                '''
                            }
                        }
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
                        bat 'mvnw.cmd test -Dproject.build.sourceEncoding=UTF-8 -Dfile.encoding=UTF-8'
                    }
                }
            }
            post {
                always {
                    // Publication des résultats de tests Maven - CORRIGÉ
                    junit testResultsPattern: '**/target/surefire-reports/*.xml', allowEmptyResults: true
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
                        bat 'mvnw.cmd spring-boot:repackage -Dproject.build.sourceEncoding=UTF-8 -Dfile.encoding=UTF-8'
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

                    // Affichage des informations sur le JAR créé (Windows)
                    bat 'dir target\\*.jar'
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

            // Archive du JAR même en cas de succès
            script {
                if (fileExists('target/*.jar')) {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    echo "🎉 JAR archivé avec succès dans Jenkins !"
                } else {
                    echo "⚠️ Aucun JAR trouvé dans target/"
                }
            }
        }

        failure {
            echo "❌ Build échoué. Vérifiez les logs pour plus de détails."
            echo "Vérifiez la connexion à Vault et à PostgreSQL"
        }
    }
}