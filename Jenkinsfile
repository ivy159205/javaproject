pipeline {
    agent any

    environment {
        MAVEN_HOME = 'D:/ApacheTomcat/apache-maven-3.9.9' // hoặc dùng tool 'Maven 3.9.9' từ Jenkins Global Tools
        JAVA_HOME = 'C:/Program Files/Java/jdk-23'
        WAR_NAME = 'web-crud-app.war'
        DEPLOY_PATH_1 = 'D:/ApacheTomcat/apache-tomcat-11.0.7-8082/webapps'
        DEPLOY_PATH_2 = 'D:/ApacheTomcat/apache-tomcat-11.0.7-8083/webapps'
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/ivy159205/javaproject.git'
            }
        }

        stage('Build') {
            steps {
                bat "${MAVEN_HOME}/bin/mvn clean package"
            }
        }

        stage('Deploy to Tomcat on Port 8082 & 8083') {
            steps {
                script {
                    def warFile = findFiles(glob: 'target/*.war')[0]
                    echo "Found WAR file: ${warFile.name}"

                    echo "Deploying to Tomcat on port 8082..."
                    bat "copy /Y target\\${warFile.name} \"${DEPLOY_PATH_1}\\${WAR_NAME}\""

                    echo "Deploying to Tomcat on port 8083..."
                    bat "copy /Y target\\${warFile.name} \"${DEPLOY_PATH_2}\\${WAR_NAME}\""
                }
            }
        }

        stage('Restart Tomcat (Optional)') {
            steps {
                echo 'Bạn có thể thêm lệnh stop/start Tomcat nếu cần, hoặc để Tomcat tự reload .war file.'
            }
        }
    }
}
