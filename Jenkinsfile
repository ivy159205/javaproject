pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/ivy159205/javaproject.git'
            }
        }

        stage('Build') {
            steps {
                bat 'D:/ApacheTomcat/apache-maven-3.9.9/bin/mvn clean package'
            }
        }

        stage('Deploy to Tomcat on Port 8082 & 8083') {
            steps {
                script {
                    def warFile = 'target/web-crud-app-1.0-SNAPSHOT.war'
                    def tomcatDirs = [
                        'D:\\ApacheTomcat\\apache-tomcat-11.0.7-8082\\webapps',
                        'D:\\ApacheTomcat\\apache-tomcat-11.0.7-8083\\webapps'
                    ]

                    if (fileExists(warFile)) {
                        echo "Found WAR file: ${warFile}"

                        for (dir in tomcatDirs) {
                            echo "Deploying to Tomcat at ${dir}..."
                            bat "if not exist ${dir} mkdir ${dir}"
                            bat "copy /Y ${warFile} ${dir}\\web-crud-app.war"
                        }
                    } else {
                        error "WAR file not found: ${warFile}"
                    }
                }
            }
        }

        stage('Restart Tomcat (Optional)') {
            when {
                expression {
                    return false // sửa lại nếu bạn muốn restart thật
                }
            }
            steps {
                echo 'Restarting Tomcat...'
                // bat 'path_to_restart_script.bat' (nếu có)
            }
        }
    }
}
