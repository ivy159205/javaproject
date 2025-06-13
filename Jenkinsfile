pipeline {
    agent any

    environment {
        CATALINA_HOME = 'D:/ApacheTomcat/apache-tomcat-11.0.7'
    }

    stages {
        stage('Prepare') {
            steps {
                echo '✅ Create deployment folder if not exist...'
                bat '''
                    if not exist "%CATALINA_HOME%\\webapps" mkdir "%CATALINA_HOME%\\webapps"
                '''
            }
        }

        stage('Build') {
            steps {
                echo '🏗️ Building project using Maven...'
                bat 'mvn clean package'
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                echo '🚀 Deploying WAR to Tomcat...'
                bat 'copy /Y target\\*.war "%CATALINA_HOME%\\webapps\\"'
            }
        }

        stage('Restart Tomcat') {
            steps {
                echo '🔁 Restarting Tomcat...'
                bat '''
                    echo 🛑 Killing Tomcat only (not Jenkins)...
                    for /f "tokens=2 delims==;" %%a in (
                        'wmic process where "CommandLine like '%%tomcat%%'" get ProcessId /format:list ^| find "ProcessId"'
                    ) do (
                        taskkill /F /PID %%a
                    )

                    timeout /T 3 >nul

                    echo 🚀 Starting Tomcat...
                    call "%CATALINA_HOME%\\bin\\startup.bat"
                '''
            }
        }
    }
}
