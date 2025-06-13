pipeline {
    agent any

    environment {
        DEPLOY_DIR_1 = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps8082"
        DEPLOY_DIR_2 = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps8083"
        TOMCAT_BIN = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/bin"
    }

    stages {
        stage('Prepare') {
            steps {
                echo '✅ Create deployment folders if not exist...'
                bat """
                    if not exist "${DEPLOY_DIR_1}" mkdir "${DEPLOY_DIR_1}"
                    if not exist "${DEPLOY_DIR_2}" mkdir "${DEPLOY_DIR_2}"
                """
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
                bat """
                    copy /Y target\\*.war "${DEPLOY_DIR_1}"
                    copy /Y target\\*.war "${DEPLOY_DIR_2}"
                """
            }
        }

        stage('Restart Tomcat (Optional)') {
            steps {
                echo '🔁 Restarting Tomcat using .bat scripts...'
                bat """
                    call "${TOMCAT_BIN}\\shutdown.bat"
                    timeout /T 5 >nul
                    call "${TOMCAT_BIN}\\startup.bat"
                """
            }
        }
    }
}
