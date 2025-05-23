pipeline {
    agent any

    environment {
        // Configuration Maven
        MAVEN_OPTS = '-Xmx1024m -Dfile.encoding=UTF-8'
        // Propriétés Maven pour l'encodage
        MAVEN_RESOURCE_ENCODING = 'UTF-8'
        PROJECT_BUILD_SOURCEENCODING = 'UTF-8'
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
                    // Récupération des secrets depuis Vault
                    def secrets = [
                        [
                            path: 'kv/myapp',
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
                    withEnv([
                        "SPRING_DB_USER=${env.DATABASE_USER}",
                        "SPRING_DB_PASS=${env.DATABASE_PASS}",
                        "SPRING_PROFILES_ACTIVE=prod"
                    ]) {

                        echo "Building Spring Boot app with Maven..."
                        echo "Database URL: jdbc:postgresql://pfe-ocp-group.c.aivencloud.com:10688/defaultdb"
                        echo "Database User: ${env.SPRING_DB_USER}"

                        // Build complet avec génération du JAR Spring Boot
                        bat '''
                            echo "=== BUILD COMPLET SPRING BOOT ==="
                            mvnw.cmd clean package -DskipTests ^
                                -Dproject.build.sourceEncoding=UTF-8 ^
                                -Dproject.reporting.outputEncoding=UTF-8 ^
                                -Dfile.encoding=UTF-8

                            echo "=== VÉRIFICATION DU JAR GÉNÉRÉ ==="
                            dir target\\*.jar

                            echo "=== INFORMATIONS SUR LE JAR ==="
                            for %%f in (target\\*.jar) do echo Fichier JAR: %%f
                        '''
                    }
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                script {
                    // Archive du JAR généré par Maven
                    echo "Archivage des artifacts..."

                    // Vérification que le JAR existe avant archivage
                    bat '''
                        echo "=== CONTENU DU RÉPERTOIRE TARGET ==="
                        dir target

                        echo "=== RECHERCHE DES FICHIERS JAR ==="
                        dir target\\*.jar 2>nul || echo "Aucun fichier JAR trouvé"
                    '''

                    // Archive si le JAR existe
                    if (fileExists('target/*.jar')) {
                        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                        echo "🎉 JAR archivé avec succès !"
                    } else {
                        error "❌ Aucun JAR trouvé à archiver"
                    }
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
            echo "🎉 JAR généré et archivé avec succès !"
        }

        failure {
            echo "❌ Build échoué. Vérifiez les logs pour plus de détails."
            echo "Vérifiez la connexion à Vault et à PostgreSQL"
        }
    }
}