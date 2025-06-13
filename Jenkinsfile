pipeline {
    agent any
    
    // tools block removed - using environment variables instead
    
    environment {
        // Đường dẫn Tomcat và Maven
        TOMCAT_HOME = 'D:\\ApacheTomcat\\apache-tomcat-11.0.7'
        MAVEN_HOME = 'D:\\ApacheTomcat\\apache-maven-3.9.9'
        TOMCAT_WEBAPPS = "${TOMCAT_HOME}\\webapps"
        
        // Đường dẫn project (backup nếu Git fail)
        PROJECT_PATH = 'C:\\Users\\casto\\OneDrive\\Desktop\\web-crud-app'
        
        // GitHub repository info
        GIT_REPO = 'https://github.com/ivy159205/javaproject.git'
        GIT_BRANCH = 'main'
        
        // Tên project - based on actual Maven build output
        PROJECT_NAME = 'web-crud-app'
        WAR_FILE = "web-crud-app-1.0-SNAPSHOT.war"
        DEPLOY_NAME = 'web-crud-app'
        
        // Ports cho dual deployment
        TOMCAT_PORT_8082 = '8082'
        TOMCAT_PORT_8083 = '8083'
        
        // Backup directory
        BACKUP_DIR = 'D:\\deployment-backup'
    }
    
    stages {
        stage('Clean Workspace') {
            steps {
                echo 'Cleaning workspace...'
                cleanWs()
            }
        }
        
        stage('Checkout') {
            steps {
                echo 'Checking out source code from GitHub...'
                git branch: 'main', url: 'https://github.com/ivy159205/javaproject.git'
                
                // Backup: Copy từ local path nếu Git fail
                script {
                    try {
                        echo 'Git checkout completed successfully'
                    } catch (Exception e) {
                        echo "Git checkout failed: ${e.getMessage()}"
                        echo 'Falling back to local copy...'
                        bat """
                            if exist "${PROJECT_PATH}" (
                                echo Copying project from ${PROJECT_PATH}
                                xcopy "${PROJECT_PATH}" "${WORKSPACE}\\" /E /I /Y
                                echo Project copied successfully from local path
                            ) else (
                                echo ERROR: Both Git and local path failed!
                                exit 1
                            )
                        """
                    }
                }
            }
        }
        
        stage('Compile') {
            steps {
                echo 'Compiling Java project...'
                bat """
                    set MAVEN_HOME=${MAVEN_HOME}
                    set PATH=%MAVEN_HOME%\\bin;%PATH%
                    cd /d "${WORKSPACE}"
                    mvn clean compile
                """
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running unit tests...'
                bat """
                    set MAVEN_HOME=${MAVEN_HOME}
                    set PATH=%MAVEN_HOME%\\bin;%PATH%
                    cd /d "${WORKSPACE}"
                    mvn test
                """
            }
            post {
                always {
                    // Publish test results
                    script {
                        if (fileExists('**/target/surefire-reports/*.xml')) {
                            junit '**/target/surefire-reports/*.xml'
                        } else {
                            echo 'No test results found'
                        }
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging WAR file...'
                bat """
                    set MAVEN_HOME=${MAVEN_HOME}
                    set PATH=%MAVEN_HOME%\\bin;%PATH%
                    cd /d "${WORKSPACE}"
                    mvn clean package -DskipTests
                """
            }
            post {
                success {
                    echo 'Archiving WAR file...'
                    script {
                        if (fileExists('**/target/*.war')) {
                            archiveArtifacts artifacts: '**/target/*.war', fingerprint: true
                        } else {
                            echo 'No WAR file found to archive'
                        }
                    }
                }
            }
        }
        
        stage('Backup Current Deployment') {
            steps {
                echo 'Creating backup of current deployment...'
                script {
                    def timestamp = new Date().format('yyyyMMdd-HHmmss')
                        bat """
                            if not exist "${BACKUP_DIR}" mkdir "${BACKUP_DIR}"
                            if exist "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}" (
                                xcopy "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}" "${BACKUP_DIR}\\${DEPLOY_NAME}-${timestamp}\\" /E /I /Y
                                echo Backup created at ${BACKUP_DIR}\\${DEPLOY_NAME}-${timestamp}
                            )
                        """
                }
            }
        }
        
        stage('Stop Tomcat') {
            steps {
                echo 'Stopping Tomcat server...'
                script {
                    try {
                        bat """
                            cd /d "${TOMCAT_HOME}\\bin"
                            startup.bat
                            timeout /t 5 /nobreak
                            shutdown.bat
                            timeout /t 10 /nobreak
                        """
                    } catch (Exception e) {
                        echo "Warning: Could not gracefully stop Tomcat: ${e.getMessage()}"
                        // Force kill Tomcat processes
                        bat '''
                            for /f "tokens=2" %%i in ('tasklist /fi "imagename eq java.exe" /fo csv ^| find "java.exe"') do (
                                taskkill /F /PID %%i 2>nul
                            )
                            echo Tomcat processes terminated
                        '''
                    }
                }
            }
        }
        
        stage('Deploy to Tomcat') {
            steps {
                script {
                    // Deploy to primary instance (8082)
                    echo 'Deploying to Tomcat port 8082...'
                    bat """
                        cd /d "${WORKSPACE}"
                        if exist "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}" (
                            rmdir /s /q "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}"
                        )
                        if exist "${TOMCAT_WEBAPPS}\\${WAR_FILE}" (
                            del /f "${TOMCAT_WEBAPPS}\\${WAR_FILE}"
                        )
                        copy "target\\${WAR_FILE}" "${TOMCAT_WEBAPPS}\\"
                        echo Deployed WAR file to ${TOMCAT_WEBAPPS}
                    """
                    
                    // Setup secondary instance (8083)
                    echo 'Setting up secondary Tomcat instance on port 8083...'
                    def secondaryTomcat = 'D:\\ApacheTomcat\\apache-tomcat-11.0.7-secondary'
                    bat """
                        cd /d "${WORKSPACE}"
                        if not exist "${secondaryTomcat}" (
                            xcopy "${TOMCAT_HOME}" "${secondaryTomcat}\\" /E /I /Y
                            echo Secondary Tomcat instance created
                        )
                        
                        REM Update server.xml for port 8083
                        powershell -Command "
                            \\$content = Get-Content '${secondaryTomcat}\\conf\\server.xml'
                            \\$content = \\$content -replace 'port=\"8080\"', 'port=\"8083\"'
                            \\$content = \\$content -replace 'port=\"8005\"', 'port=\"8006\"'
                            \\$content = \\$content -replace 'port=\"8009\"', 'port=\"8010\"'
                            Set-Content '${secondaryTomcat}\\conf\\server.xml' \\$content
                        "
                        
                        REM Deploy to secondary instance
                        if exist "${secondaryTomcat}\\webapps\\${DEPLOY_NAME}" (
                            rmdir /s /q "${secondaryTomcat}\\webapps\\${DEPLOY_NAME}"
                        )
                        if exist "${secondaryTomcat}\\webapps\\${WAR_FILE}" (
                            del /f "${secondaryTomcat}\\webapps\\${WAR_FILE}"
                        )
                        copy "target\\${WAR_FILE}" "${secondaryTomcat}\\webapps\\"
                    """
                }
            }
        }
        
        stage('Start Tomcat Instances') {
            steps {
                echo 'Starting Tomcat instances...'
                script {
                    // Start primary Tomcat (8082)
                    bat """
                        cd /d "${TOMCAT_HOME}\\bin"
                        start /b startup.bat
                        echo Primary Tomcat starting on port 8082...
                    """
                    
                    // Start secondary Tomcat (8083)
                    bat """
                        cd /d "D:\\ApacheTomcat\\apache-tomcat-11.0.7-secondary\\bin"
                        start /b startup.bat
                        echo Secondary Tomcat starting on port 8083...
                    """
                }
                
                echo 'Waiting for Tomcat instances to start...'
                bat 'timeout /t 30 /nobreak'
            }
        }
        
        stage('Health Check') {
            steps {
                echo 'Performing health checks...'
                script {
                    def healthCheckPassed = false
                    def maxRetries = 5
                    def retryCount = 0
                    
                    while (!healthCheckPassed && retryCount < maxRetries) {
                        try {
                            bat """
                                powershell -Command "
                                    try {
                                        \\$response8082 = Invoke-WebRequest -Uri 'http://localhost:8082/${DEPLOY_NAME}' -TimeoutSec 10
                                        Write-Host 'Port 8082 Status Code: ' \\$response8082.StatusCode
                                        
                                        \\$response8083 = Invoke-WebRequest -Uri 'http://localhost:8083/${DEPLOY_NAME}' -TimeoutSec 10
                                        Write-Host 'Port 8083 Status Code: ' \\$response8083.StatusCode
                                        
                                        if (\\$response8082.StatusCode -eq 200 -and \\$response8083.StatusCode -eq 200) {
                                            Write-Host 'Health check passed for both instances'
                                            exit 0
                                        } else {
                                            Write-Host 'Health check failed'
                                            exit 1
                                        }
                                    } catch {
                                        Write-Host 'Health check error: ' \\$_.Exception.Message
                                        exit 1
                                    }
                                "
                            """
                            healthCheckPassed = true
                        } catch (Exception e) {
                            retryCount++
                            echo "Health check attempt ${retryCount} failed: ${e.getMessage()}"
                            if (retryCount < maxRetries) {
                                echo "Retrying in 15 seconds..."
                                bat 'timeout /t 15 /nobreak'
                            }
                        }
                    }
                    
                    if (!healthCheckPassed) {
                        error("Health check failed after ${maxRetries} attempts")
                    }
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                echo 'Verifying deployment status...'
                bat """
                    echo.
                    echo ================================
                    echo DEPLOYMENT VERIFICATION
                    echo ================================
                    
                    powershell -Command "
                        Write-Host 'Checking Tomcat processes...'
                        Get-Process | Where-Object {\\$_.ProcessName -like '*java*' -and \\$_.MainWindowTitle -like '*Tomcat*'} | Format-Table ProcessName, Id, MainWindowTitle -AutoSize
                        
                        Write-Host 'Checking deployed applications...'
                        if (Test-Path '${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}') {
                            Write-Host '✓ Primary instance (8082): Application deployed'
                        } else {
                            Write-Host '✗ Primary instance (8082): Application NOT found'
                        }
                        
                        if (Test-Path 'D:\\ApacheTomcat\\apache-tomcat-11.0.7-secondary\\webapps\\${DEPLOY_NAME}') {
                            Write-Host '✓ Secondary instance (8083): Application deployed'
                        } else {
                            Write-Host '✗ Secondary instance (8083): Application NOT found'
                        }
                    "
                """
            }
        }
    }
    
    post {
        success {
            echo '================================'
            echo 'DEPLOYMENT COMPLETED SUCCESSFULLY!'
            echo '================================'
            echo 'Application is available at:'
            echo "- http://localhost:8082/${DEPLOY_NAME} (Primary)"
            echo "- http://localhost:8083/${DEPLOY_NAME} (Secondary)"
            echo '- GitHub Repository: https://github.com/ivy159205/javaproject'
            echo '================================'
            
            // Send success notification (uncomment if needed)
            // emailext (
            //     subject: "✅ Deployment Success: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
            //     body: "Deployment completed successfully!\n\nJob: ${env.JOB_NAME}\nBuild: ${env.BUILD_NUMBER}\nBranch: ${env.BRANCH_NAME}",
            //     to: "your-email@company.com"
            // )
        }
        
        failure {
            echo '================================'
            echo 'DEPLOYMENT FAILED!'
            echo '================================'
            echo 'Check the logs above for error details.'
            
            // Rollback on failure
            script {
                try {
                    echo 'Attempting rollback...'
                    // Add rollback logic here if needed
                } catch (Exception e) {
                    echo "Rollback failed: ${e.getMessage()}"
                }
            }
            
            // Send failure notification (uncomment if needed)
            // emailext (
            //     subject: "❌ Deployment Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
            //     body: "Deployment failed!\n\nJob: ${env.JOB_NAME}\nBuild: ${env.BUILD_NUMBER}\nBranch: ${env.BRANCH_NAME}\n\nCheck Jenkins for details.",
            //     to: "your-email@company.com"
            // )
        }
        
        always {
            echo 'Cleaning up temporary files...'
            // Keep workspace for debugging in case of failure
            // cleanWs()
        }
    }
}