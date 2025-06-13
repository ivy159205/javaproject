pipeline {
    agent any

    environment {
        DEPLOY_DIR_8082 = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps8082"
        DEPLOY_DIR_8083 = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps8083"
    }

    stages {
        stage('Prepare') {
            steps {
                echo '✅ Create deployment folders if not exist...'
                bat '''
                    if not exist "%DEPLOY_DIR_8082%" mkdir "%DEPLOY_DIR_8082%"
                    if not exist "%DEPLOY_DIR_8083%" mkdir "%DEPLOY_DIR_8083%"
                '''
            }
        }

        stage('Build') {
            steps {
                echo '🏗️ Building project using Maven...'
                bat 'mvn clean package'
            }
        }

        stage('Deploy to Tomcat on Port 8082 & 8083') {
            steps {
                echo '🚀 Deploying to Tomcat...'
                bat '''
                    copy /Y target\\*.war "%DEPLOY_DIR_8082%"
                    copy /Y target\\*.war "%DEPLOY_DIR_8083%"
                '''
            }
        }

        stage('Restart Tomcat (Optional)') {
            steps {
                echo '🔁 Restarting Tomcat services...'
                bat '''
                    net stop Tomcat9-8082
                    net start Tomcat9-8082
                    net stop Tomcat9-8083
                    net start Tomcat9-8083
                '''
            }
        }
    }
}
