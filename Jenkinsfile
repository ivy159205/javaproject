pipeline {
    agent any
    
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
        
        // Secondary Tomcat path
        SECONDARY_TOMCAT = 'D:\\ApacheTomcat\\apache-tomcat-11.0.7-secondary'
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
                script {
                    try {
                        git branch: 'main', url: 'https://github.com/ivy159205/javaproject.git'
                        echo 'Git checkout completed successfully'
                    } catch (Exception e) {
                        echo "Git checkout failed: ${e.getMessage()}"
                        echo 'Falling back to local copy...'
                        bat """
                            if exist "${PROJECT_PATH}" (
                                echo Copying project from ${PROJECT_PATH}
                                xcopy "${PROJECT_PATH}" "${WORKSPACE}\\\\" /E /I /Y
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
                        if (fileExists('target/*.war')) {
                            archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                            echo 'WAR file archived successfully'
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
                            xcopy "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}" "${BACKUP_DIR}\\${DEPLOY_NAME}-${timestamp}\\\\" /E /I /Y
                            echo Backup created at ${BACKUP_DIR}\\${DEPLOY_NAME}-${timestamp}
                        ) else (
                            echo No existing deployment to backup
                        )
                    """
                }
            }
        }
        
        stage('Stop Tomcat') {
            steps {
                echo 'Stopping Tomcat servers...'
                script {
                    // Stop primary Tomcat
                    try {
                        bat """
                            cd /d "${TOMCAT_HOME}\\bin"
                            call shutdown.bat
                        """
                        sleep(time: 10, unit: 'SECONDS')
                        echo "Primary Tomcat shutdown command sent"
                    } catch (Exception e) {
                        echo "Warning: Could not gracefully stop primary Tomcat: ${e.getMessage()}"
                    }
                    
                    // Stop secondary Tomcat if exists
                    try {
                        bat """
                            if exist "${SECONDARY_TOMCAT}\\bin\\shutdown.bat" (
                                cd /d "${SECONDARY_TOMCAT}\\bin"
                                call shutdown.bat
                            )
                        """
                        sleep(time: 10, unit: 'SECONDS')
                        echo "Secondary Tomcat shutdown command sent"
                    } catch (Exception e) {
                        echo "Warning: Could not gracefully stop secondary Tomcat: ${e.getMessage()}"
                    }
                    
                    // Force kill any remaining Java processes
                    try {
                        bat '''
                            for /f "tokens=2 delims=," %%i in ('tasklist /fi "imagename eq java.exe" /fo csv /nh 2^>nul') do (
                                if not "%%i"=="" (
                                    echo Killing Java process %%i
                                    taskkill /F /PID %%i 2>nul
                                )
                            )
                            echo Java process cleanup completed
                        '''
                        sleep(time: 5, unit: 'SECONDS')
                    } catch (Exception e) {
                        echo "Java process cleanup completed: ${e.getMessage()}"
                    }
                }
            }
        }
        
        stage('Deploy to Tomcat') {
            steps {
                script {
                    // Deploy to primary instance (8082)
                    echo 'Deploying to primary Tomcat instance (port 8082)...'
                    bat """
                        cd /d "${WORKSPACE}"
                        
                        REM Clean up existing deployment
                        if exist "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}" (
                            rmdir /s /q "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}"
                        )
                        if exist "${TOMCAT_WEBAPPS}\\${WAR_FILE}" (
                            del /f "${TOMCAT_WEBAPPS}\\${WAR_FILE}"
                        )
                        
                        REM Deploy new WAR file
                        copy "target\\${WAR_FILE}" "${TOMCAT_WEBAPPS}\\\\"
                        echo Primary deployment completed
                    """
                    
                    // Setup secondary instance (8083)
                    echo 'Setting up secondary Tomcat instance (port 8083)...'
                    bat """
                        cd /d "${WORKSPACE}"
                        
                        REM Create secondary Tomcat instance if not exists
                        if not exist "${SECONDARY_TOMCAT}" (
                            echo Creating secondary Tomcat instance...
                            xcopy "${TOMCAT_HOME}" "${SECONDARY_TOMCAT}\\\\" /E /I /Y /Q
                            echo Secondary Tomcat instance created
                        )
                        
                        REM Clean up existing deployment in secondary
                        if exist "${SECONDARY_TOMCAT}\\webapps\\${DEPLOY_NAME}" (
                            rmdir /s /q "${SECONDARY_TOMCAT}\\webapps\\${DEPLOY_NAME}"
                        )
                        if exist "${SECONDARY_TOMCAT}\\webapps\\${WAR_FILE}" (
                            del /f "${SECONDARY_TOMCAT}\\webapps\\${WAR_FILE}"
                        )
                        
                        REM Deploy to secondary instance
                        copy "target\\${WAR_FILE}" "${SECONDARY_TOMCAT}\\webapps\\\\"
                        echo Secondary deployment completed
                    """
                    
                    // Update server.xml for secondary instance
                    echo 'Configuring secondary Tomcat ports...'
                    bat '''
                        cd /d "''' + env.WORKSPACE + '''"
                        
                        REM Create PowerShell script to update server.xml
                        echo # PowerShell script to update server.xml > update_server.ps1
                        echo $ErrorActionPreference = 'Continue' >> update_server.ps1
                        echo try { >> update_server.ps1
                        echo     $xmlPath = ''' + "'${SECONDARY_TOMCAT}\\conf\\server.xml'" + ''' >> update_server.ps1
                        echo     Write-Host "Updating server.xml at: $xmlPath" >> update_server.ps1
                        echo     [xml]$xml = Get-Content $xmlPath >> update_server.ps1
                        echo     # Update shutdown port >> update_server.ps1
                        echo     $xml.Server.SetAttribute('port', '8006') >> update_server.ps1
                        echo     # Find and update HTTP connector >> update_server.ps1
                        echo     foreach ($connector in $xml.Server.Service.Connector) { >> update_server.ps1
                        echo         if ($connector.protocol -eq 'HTTP/1.1' -or $connector.port -eq '8080') { >> update_server.ps1
                        echo             $connector.SetAttribute('port', '8083') >> update_server.ps1
                        echo             Write-Host "Updated HTTP connector to port 8083" >> update_server.ps1
                        echo             break >> update_server.ps1
                        echo         } >> update_server.ps1
                        echo     } >> update_server.ps1
                        echo     # Find and update AJP connector >> update_server.ps1
                        echo     foreach ($connector in $xml.Server.Service.Connector) { >> update_server.ps1
                        echo         if ($connector.protocol -eq 'AJP/1.3' -or $connector.port -eq '8009') { >> update_server.ps1
                        echo             $connector.SetAttribute('port', '8010') >> update_server.ps1
                        echo             Write-Host "Updated AJP connector to port 8010" >> update_server.ps1
                        echo             break >> update_server.ps1
                        echo         } >> update_server.ps1
                        echo     } >> update_server.ps1
                        echo     $xml.Save($xmlPath) >> update_server.ps1
                        echo     Write-Host 'Server.xml updated successfully' >> update_server.ps1
                        echo } catch { >> update_server.ps1
                        echo     Write-Host "Error updating server.xml: $($_.Exception.Message)" >> update_server.ps1
                        echo     Write-Host 'Continuing with deployment...' >> update_server.ps1
                        echo } >> update_server.ps1
                        
                        REM Execute PowerShell script
                        powershell -ExecutionPolicy Bypass -File update_server.ps1
                        
                        REM Clean up
                        del update_server.ps1 2>nul
                    '''
                }
            }
        }
        
        stage('Start Tomcat Instances') {
            steps {
                echo 'Starting Tomcat instances...'
                script {
                    // Start primary Tomcat (8082)
                    echo 'Starting primary Tomcat instance...'
                    bat """
                        cd /d "${TOMCAT_HOME}\\bin"
                        start "Primary Tomcat 8082" /min startup.bat
                        echo Primary Tomcat starting on port 8082...
                    """
                    
                    // Wait a bit before starting secondary
                    sleep(time: 15, unit: 'SECONDS')
                    
                    // Start secondary Tomcat (8083)
                    echo 'Starting secondary Tomcat instance...'
                    bat """
                        cd /d "${SECONDARY_TOMCAT}\\bin"
                        start "Secondary Tomcat 8083" /min startup.bat
                        echo Secondary Tomcat starting on port 8083...
                    """
                }
                
                echo 'Waiting for Tomcat instances to fully start...'
                sleep(time: 60, unit: 'SECONDS')
            }
        }
        
        stage('Health Check') {
            steps {
                echo 'Performing health checks...'
                script {
                    def healthCheckPassed = false
                    def maxRetries = 8
                    def retryCount = 0
                    
                    while (!healthCheckPassed && retryCount < maxRetries) {
                        retryCount++
                        echo "Health check attempt ${retryCount}/${maxRetries}..."
                        
                        def healthCheckResult = bat returnStatus: true, script: '''
                            cd /d "''' + env.WORKSPACE + '''"
                            
                            REM Create PowerShell health check script
                            echo # Health check script > health_check.ps1
                            echo $ErrorActionPreference = 'Stop' >> health_check.ps1
                            echo try { >> health_check.ps1
                            echo     Write-Host 'Checking primary instance (8082)...' >> health_check.ps1
                            echo     $response8082 = Invoke-WebRequest -Uri 'http://localhost:8082/''' + env.DEPLOY_NAME + '''/' -TimeoutSec 30 -UseBasicParsing >> health_check.ps1
                            echo     Write-Host "Primary instance status: $($response8082.StatusCode)" >> health_check.ps1
                            echo. >> health_check.ps1
                            echo     Write-Host 'Checking secondary instance (8083)...' >> health_check.ps1
                            echo     $response8083 = Invoke-WebRequest -Uri 'http://localhost:8083/''' + env.DEPLOY_NAME + '''/' -TimeoutSec 30 -UseBasicParsing >> health_check.ps1
                            echo     Write-Host "Secondary instance status: $($response8083.StatusCode)" >> health_check.ps1
                            echo. >> health_check.ps1
                            echo     if ($response8082.StatusCode -eq 200 -and $response8083.StatusCode -eq 200) { >> health_check.ps1
                            echo         Write-Host 'SUCCESS: Both instances are healthy' >> health_check.ps1
                            echo         exit 0 >> health_check.ps1
                            echo     } else { >> health_check.ps1
                            echo         Write-Host 'FAILED: One or both instances are not responding correctly' >> health_check.ps1
                            echo         exit 1 >> health_check.ps1
                            echo     } >> health_check.ps1
                            echo } catch { >> health_check.ps1
                            echo     Write-Host "Health check error: $($_.Exception.Message)" >> health_check.ps1
                            echo     exit 1 >> health_check.ps1
                            echo } >> health_check.ps1
                            
                            REM Execute health check
                            powershell -ExecutionPolicy Bypass -File health_check.ps1
                            set HEALTH_RESULT=%ERRORLEVEL%
                            
                            REM Clean up
                            del health_check.ps1 2>nul
                            
                            REM Return the actual result
                            exit %HEALTH_RESULT%
                        '''
                        
                        if (healthCheckResult == 0) {
                            healthCheckPassed = true
                            echo "✓ Health check passed on attempt ${retryCount}"
                        } else {
                            echo "✗ Health check failed on attempt ${retryCount}"
                            if (retryCount < maxRetries) {
                                echo "Waiting 25 seconds before retry..."
                                sleep(time: 25, unit: 'SECONDS')
                            }
                        }
                    }
                    
                    if (!healthCheckPassed) {
                        echo "Health check failed after ${maxRetries} attempts. Checking individual instances..."
                        
                        // Check individual instances for better error reporting
                        bat '''
                            echo Checking individual instances:
                            echo.
                            echo Testing Primary (8082):
                            powershell -Command "try { $r = Invoke-WebRequest 'http://localhost:8082/''' + env.DEPLOY_NAME + '''/' -UseBasicParsing -TimeoutSec 10; Write-Host 'Primary Status:' $r.StatusCode } catch { Write-Host 'Primary Error:' $_.Exception.Message }"
                            
                            echo.
                            echo Testing Secondary (8083):
                            powershell -Command "try { $r = Invoke-WebRequest 'http://localhost:8083/''' + env.DEPLOY_NAME + '''/' -UseBasicParsing -TimeoutSec 10; Write-Host 'Secondary Status:' $r.StatusCode } catch { Write-Host 'Secondary Error:' $_.Exception.Message }"
                        '''
                        
                        error("Health check failed after ${maxRetries} attempts")
                    }
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                echo 'Verifying deployment status...'
                bat '''
                    echo.
                    echo ================================
                    echo DEPLOYMENT VERIFICATION
                    echo ================================
                    
                    cd /d "''' + env.WORKSPACE + '''"
                    
                    REM Create verification script
                    echo # Deployment verification script > verify.ps1
                    echo Write-Host 'Checking Tomcat processes...' >> verify.ps1
                    echo Get-Process ^| Where-Object {$_.ProcessName -like '*java*'} ^| Select-Object ProcessName, Id, StartTime ^| Format-Table -AutoSize >> verify.ps1
                    echo. >> verify.ps1
                    echo Write-Host 'Checking listening ports...' >> verify.ps1
                    echo netstat -an ^| findstr "8082\\|8083" >> verify.ps1
                    echo. >> verify.ps1
                    echo Write-Host 'Checking deployed applications...' >> verify.ps1
                    echo if (Test-Path ''' + "'${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}'" + ''') { >> verify.ps1
                    echo     Write-Host '✓ Primary instance (8082): Application deployed and extracted' >> verify.ps1
                    echo } elseif (Test-Path ''' + "'${TOMCAT_WEBAPPS}\\${WAR_FILE}'" + ''') { >> verify.ps1
                    echo     Write-Host '⚠ Primary instance (8082): WAR file present but not extracted yet' >> verify.ps1
                    echo } else { >> verify.ps1
                    echo     Write-Host '✗ Primary instance (8082): Application NOT found' >> verify.ps1
                    echo } >> verify.ps1
                    echo. >> verify.ps1
                    echo if (Test-Path ''' + "'${SECONDARY_TOMCAT}\\webapps\\${DEPLOY_NAME}'" + ''') { >> verify.ps1
                    echo     Write-Host '✓ Secondary instance (8083): Application deployed and extracted' >> verify.ps1
                    echo } elseif (Test-Path ''' + "'${SECONDARY_TOMCAT}\\webapps\\${WAR_FILE}'" + ''') { >> verify.ps1
                    echo     Write-Host '⚠ Secondary instance (8083): WAR file present but not extracted yet' >> verify.ps1
                    echo } else { >> verify.ps1
                    echo     Write-Host '✗ Secondary instance (8083): Application NOT found' >> verify.ps1
                    echo } >> verify.ps1
                    
                    REM Execute verification
                    powershell -ExecutionPolicy Bypass -File verify.ps1
                    
                    REM Clean up
                    del verify.ps1 2>nul
                '''
            }
        }
        
        stage('Final Status Check') {
            steps {
                echo 'Performing final application accessibility test...'
                bat '''
                    echo ================================
                    echo FINAL APPLICATION STATUS
                    echo ================================
                    
                    echo Testing application accessibility:
                    echo.
                    echo Primary instance (8082):
                    powershell -Command "try { $response = Invoke-WebRequest 'http://localhost:8082/''' + env.DEPLOY_NAME + '''/' -UseBasicParsing -TimeoutSec 15; Write-Host '✓ ACCESSIBLE - Status Code:' $response.StatusCode; Write-Host '  URL: http://localhost:8082/''' + env.DEPLOY_NAME + '''/' } catch { Write-Host '✗ NOT ACCESSIBLE - Error:' $_.Exception.Message }"
                    
                    echo.
                    echo Secondary instance (8083):
                    powershell -Command "try { $response = Invoke-WebRequest 'http://localhost:8083/''' + env.DEPLOY_NAME + '''/' -UseBasicParsing -TimeoutSec 15; Write-Host '✓ ACCESSIBLE - Status Code:' $response.StatusCode; Write-Host '  URL: http://localhost:8083/''' + env.DEPLOY_NAME + '''/' } catch { Write-Host '✗ NOT ACCESSIBLE - Error:' $_.Exception.Message }"
                    
                    echo.
                    echo ================================
                '''
            }
        }
    }
    
    post {
        success {
            echo '================================'
            echo 'DEPLOYMENT COMPLETED SUCCESSFULLY!'
            echo '================================'
            echo 'Application should be available at:'
            echo "- http://localhost:8082/${DEPLOY_NAME}/ (Primary)"
            echo "- http://localhost:8083/${DEPLOY_NAME}/ (Secondary)"
            echo '- GitHub Repository: https://github.com/ivy159205/javaproject'
            echo '================================'
            echo 'If applications are not accessible, check:'
            echo '1. Tomcat processes are running'
            echo '2. Ports 8082 and 8083 are not blocked'
            echo '3. WAR files have been extracted'
            echo '4. Check Tomcat logs for errors'
            echo '================================'
        }
        
        failure {
            echo '================================'
            echo 'DEPLOYMENT FAILED!'
            echo '================================'
            echo 'Check the logs above for error details.'
            echo 'Common issues:'
            echo '- Port conflicts (8082/8083 already in use)'
            echo '- Java processes not terminating properly'
            echo '- WAR file corruption or build issues'
            echo '- Tomcat configuration problems'
            
            script {
                try {
                    echo 'Collecting failure information...'
                    bat """
                        echo Current Java processes:
                        tasklist /fi "imagename eq java.exe" /fo table 2>nul
                        echo.
                        echo Port usage:
                        netstat -an | findstr "8082\\|8083" 2>nul
                        echo.
                        echo Available backups:
                        if exist "${BACKUP_DIR}" (
                            dir "${BACKUP_DIR}" /b 2>nul
                        ) else (
                            echo No backup directory found
                        )
                    """
                } catch (Exception e) {
                    echo "Could not collect failure information: ${e.getMessage()}"
                }
            }
        }
        
        always {
            echo 'Pipeline execution completed.'
            echo "Build timestamp: ${new Date()}"
        }
    }
}