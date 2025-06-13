pipeline {
    agent any

    environment {
        MAVEN_HOME = 'D:/ApacheTomcat/apache-maven-3.9.9'
        JAVA_HOME = 'C:/Program Files/Java/jdk-23'
        WAR_OUTPUT_DIR = 'target'
        DEPLOY_PATH_1 = 'D:/ApacheTomcat/apache-tomcat-11.0.7-8082/webapps'
        DEPLOY_PATH_2 = 'D:/ApacheTomcat/apache-tomcat-11.0.7-8083/webapps'
        DEPLOY_WAR_NAME = 'web-crud-app.war' // Tên mong muốn khi deploy
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/ivy159205/javaproject.git'
            }
        }

        stage('Build') {
            steps {
                bat "${env.MAVEN_HOME}/bin/mvn clean package"
            }
        }

        stage('Deploy to Tomcat on Port 8082 & 8083') {
            steps {
                script {
                    def warFiles = findFiles(glob: "${env.WAR_OUTPUT_DIR}/*.war")
                    if (warFiles.length == 0) {
                        error "WAR file not found in ${env.WAR_OUTPUT_DIR}/ folder."
                    }

                    def warFileName = warFiles[0].name
                    echo "Found WAR file: ${warFileName}"

                    // Deploy to 8082
                    echo "Deploying to Tomcat on port 8082..."
                    bat "copy /Y ${env.WAR_OUTPUT_DIR}\\${warFileName} \"${env.DEPLOY_PATH_1}\\${env.DEPLOY_WAR_NAME}\""

                    // Deploy to 8083
                    echo "Deploying to Tomcat on port 8083..."
                    bat "copy /Y ${env.WAR_OUTPUT_DIR}\\${warFileName} \"${env.DEPLOY_PATH_2}\\${env.DEPLOY_WAR_NAME}\""
                }
            }
        }

        stage('Restart Tomcat (Optional)') {
            steps {
                echo 'Nếu Tomcat không tự động reload WAR, hãy thêm lệnh restart tại đây.'
            }
        }
    }
}
