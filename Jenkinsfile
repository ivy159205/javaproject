pipeline {
    agent any
    
    environment {
        // Tomcat and Maven paths
        TOMCAT_HOME = 'D:\\ApacheTomcat\\apache-tomcat-11.0.7'
        MAVEN_HOME = 'D:\\ApacheTomcat\\apache-maven-3.9.9'
        TOMCAT_WEBAPPS = "${TOMCAT_HOME}\\webapps"
        
        // Project configuration
        PROJECT_PATH = 'C:\\Users\\casto\\OneDrive\\Desktop\\web-crud-app'
        GIT_REPO = 'https://github.com/ivy159205/javaproject.git'
        GIT_BRANCH = 'main'
        
        // Application configuration
        PROJECT_NAME = 'web-crud-app'
        WAR_FILE = "web-crud-app-1.0-SNAPSHOT.war"
        DEPLOY_NAME = 'web-crud-app'
        
        // Deployment ports
        PRIMARY_PORT = '8082'
        SECONDARY_PORT = '8083'
        
        // Backup and secondary Tomcat
        BACKUP_DIR = 'D:\\deployment-backup'
        SECONDARY_TOMCAT = 'D:\\ApacheTomcat\\apache-tomcat-11.0.7-secondary'
        
        // Build configuration
        JAVA_OPTS = '-Xmx512m -Xms256m'
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=256m'
    }
    
    options {
        // Keep only last 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Timeout after 30 minutes
        timeout(time: 30, unit: 'MINUTES')
        // Retry on failure
        retry(2)
    }
    
    stages {
        stage('Pre-build Setup') {
            steps {
                echo 'Setting up build environment...'
                script {
                    // Clean workspace
                    cleanWs()
                    
                    // Verify Java and Maven installation
                    bat '''
                        echo Verifying Java installation:
                        java -version
                        echo.
                        echo Verifying Maven installation:
                        set MAVEN_HOME=%MAVEN_HOME%
                        set PATH=%MAVEN_HOME%\\bin;%PATH%
                        mvn -version
                    '''
                }
            }
        }
        
        stage('Source Code Checkout') {
            steps {
                echo 'Fetching source code from GitHub...'
                script {
                    try {
                        // Primary: Git checkout
                        git branch: "${GIT_BRANCH}", url: "${GIT_REPO}"
                        echo '✓ Git checkout successful'
                        
                        // Verify project structure
                        bat '''
                            echo Verifying project structure:
                            if exist "pom.xml" (
                                echo ✓ Maven POM file found
                            ) else (
                                echo ✗ Maven POM file not found
                                exit 1
                            )
                            
                            if exist "src\\main\\java" (
                                echo ✓ Java source directory found
                            ) else (
                                echo ✗ Java source directory not found
                                exit 1
                            )
                        '''
                        
                    } catch (Exception e) {
                        echo "Git checkout failed: ${e.getMessage()}"
                        echo 'Attempting fallback to local copy...'
                        
                        bat """
                            if exist "${PROJECT_PATH}" (
                                echo Copying from local path: ${PROJECT_PATH}
                                xcopy "${PROJECT_PATH}" "${WORKSPACE}\\\\" /E /I /Y /Q
                                echo ✓ Local copy successful
                            ) else (
                                echo ✗ Both Git and local fallback failed
                                exit 1
                            )
                        """
                    }
                }
            }
        }
        
        stage('Build & Test') {
            parallel {
                stage('Compile & Package') {
                    steps {
                        echo 'Compiling and packaging application...'
                        bat """
                            set MAVEN_HOME=${MAVEN_HOME}
                            set MAVEN_OPTS=${MAVEN_OPTS}
                            set PATH=%MAVEN_HOME%\\bin;%PATH%
                            cd /d "${WORKSPACE}"
                            
                            echo Starting Maven build...
                            mvn clean compile package -DskipTests -q
                            
                            echo Verifying build artifacts...
                            if exist "target\\${WAR_FILE}" (
                                echo ✓ WAR file created successfully
                                dir target\\*.war
                            ) else (
                                echo ✗ WAR file not found
                                exit 1
                            )
                        """
                    }
                }
                
                stage('Run Tests') {
                    steps {
                        echo 'Running unit tests...'
                        bat """
                            set MAVEN_HOME=${MAVEN_HOME}
                            set PATH=%MAVEN_HOME%\\bin;%PATH%
                            cd /d "${WORKSPACE}"
                            mvn test -q
                        """
                    }
                    post {
                        always {
                            script {
                                // Publish test results if available
                                if (fileExists('**/target/surefire-reports/*.xml')) {
                                    junit '**/target/surefire-reports/*.xml'
                                    echo '✓ Test results published'
                                } else {
                                    echo 'ℹ No test results found'
                                }
                            }
                        }
                    }
                }
            }
        }
        
        stage('Pre-deployment Preparation') {
            steps {
                echo 'Preparing for deployment...'
                script {
                    // Create backup
                    def timestamp = new Date().format('yyyyMMdd-HHmmss')
                    bat """
                        echo Creating deployment backup...
                        if not exist "${BACKUP_DIR}" mkdir "${BACKUP_DIR}"
                        
                        if exist "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}" (
                            xcopy "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}" "${BACKUP_DIR}\\${DEPLOY_NAME}-${timestamp}\\\\" /E /I /Y /Q
                            echo ✓ Backup created: ${BACKUP_DIR}\\${DEPLOY_NAME}-${timestamp}
                        ) else (
                            echo ℹ No existing deployment to backup
                        )
                    """
                    
                    // Archive WAR file
                    if (fileExists('target/*.war')) {
                        archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                        echo '✓ WAR file archived'
                    }
                }
            }
        }
        
        stage('Service Management') {
            steps {
                echo 'Managing Tomcat services...'
                script {
                    // Stop existing instances
                    try {
                        echo 'Stopping Tomcat instances...'
                        bat """
                            echo Stopping primary Tomcat...
                            cd /d "${TOMCAT_HOME}\\bin"
                            call shutdown.bat
                        """
                        sleep(time: 10, unit: 'SECONDS')
                    } catch (Exception e) {
                        echo "Primary Tomcat was not running: ${e.getMessage()}"
                    }
                    
                    try {
                        bat """
                            if exist "${SECONDARY_TOMCAT}\\bin\\shutdown.bat" (
                                echo Stopping secondary Tomcat...
                                cd /d "${SECONDARY_TOMCAT}\\bin"
                                call shutdown.bat
                            )
                        """
                        sleep(time: 10, unit: 'SECONDS')
                    } catch (Exception e) {
                        echo "Secondary Tomcat was not running: ${e.getMessage()}"
                    }
                    
                    // Force cleanup Java processes
                    bat '''
                        echo Cleaning up Java processes...
                        for /f "tokens=2 delims=," %%i in ('tasklist /fi "imagename eq java.exe" /fo csv /nh 2^>nul') do (
                            if not "%%i"=="" (
                                echo Terminating Java process %%i
                                taskkill /F /PID %%i 2>nul || echo Process %%i already terminated
                            )
                        )
                        echo ✓ Java process cleanup completed
                    '''
                }
            }
        }
        
        stage('Application Deployment') {
            steps {
                echo 'Deploying application to Tomcat instances...'
                script {
                    // Deploy to primary instance
                    bat """
                        echo Deploying to primary instance (port ${PRIMARY_PORT})...
                        
                        REM Clean existing deployment
                        if exist "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}" rmdir /s /q "${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}"
                        if exist "${TOMCAT_WEBAPPS}\\${WAR_FILE}" del /f "${TOMCAT_WEBAPPS}\\${WAR_FILE}"
                        
                        REM Deploy new WAR
                        copy "target\\${WAR_FILE}" "${TOMCAT_WEBAPPS}\\\\"
                        echo ✓ Primary deployment completed
                    """
                    
                    // Setup and deploy to secondary instance
                    bat """
                        echo Setting up secondary instance (port ${SECONDARY_PORT})...
                        
                        REM Create secondary instance if needed
                        if not exist "${SECONDARY_TOMCAT}" (
                            echo Creating secondary Tomcat instance...
                            xcopy "${TOMCAT_HOME}" "${SECONDARY_TOMCAT}\\\\" /E /I /Y /Q
                            echo ✓ Secondary instance created
                        )
                        
                        REM Clean secondary deployment
                        if exist "${SECONDARY_TOMCAT}\\webapps\\${DEPLOY_NAME}" rmdir /s /q "${SECONDARY_TOMCAT}\\webapps\\${DEPLOY_NAME}"
                        if exist "${SECONDARY_TOMCAT}\\webapps\\${WAR_FILE}" del /f "${SECONDARY_TOMCAT}\\webapps\\${WAR_FILE}"
                        
                        REM Deploy to secondary
                        copy "target\\${WAR_FILE}" "${SECONDARY_TOMCAT}\\webapps\\\\"
                        echo ✓ Secondary deployment completed
                    """
                    
                    // Configure secondary instance ports
                    echo 'Configuring secondary instance ports...'
                    bat """
                        cd /d "${WORKSPACE}"
                        
                        REM Create PowerShell script for port configuration
                        (
                            echo try {
                            echo     \\$xmlPath = '${SECONDARY_TOMCAT}\\conf\\server.xml'
                            echo     [xml]\\$xml = Get-Content \\$xmlPath
                            echo     
                            echo     # Update shutdown port
                            echo     \\$xml.Server.SetAttribute('port', '8006'^)
                            echo     
                            echo     # Update HTTP connector port
                            echo     foreach (\\$connector in \\$xml.Server.Service.Connector^) {
                            echo         if (\\$connector.protocol -eq 'HTTP/1.1' -or \\$connector.port -eq '8080'^) {
                            echo             \\$connector.SetAttribute('port', '${SECONDARY_PORT}'^)
                            echo             break
                            echo         }
                            echo     }
                            echo     
                            echo     # Update AJP connector port
                            echo     foreach (\\$connector in \\$xml.Server.Service.Connector^) {
                            echo         if (\\$connector.protocol -eq 'AJP/1.3' -or \\$connector.port -eq '8009'^) {
                            echo             \\$connector.SetAttribute('port', '8010'^)
                            echo             break
                            echo         }
                            echo     }
                            echo     
                            echo     \\$xml.Save(\\$xmlPath^)
                            echo     Write-Host '✓ Server.xml configured successfully'
                            echo } catch {
                            echo     Write-Host "✗ Error: \\$_"
                            echo     exit 1
                            echo }
                        ) > configure_ports.ps1
                        
                        powershell -ExecutionPolicy Bypass -File configure_ports.ps1
                        del configure_ports.ps1
                    """
                }
            }
        }
        
        stage('Service Startup') {
            steps {
                echo 'Starting Tomcat instances...'
                script {
                    // Start primary instance
                    bat """
                        echo Starting primary Tomcat instance...
                        cd /d "${TOMCAT_HOME}\\bin"
                        start "Primary-Tomcat-${PRIMARY_PORT}" /min startup.bat
                        echo ✓ Primary Tomcat starting on port ${PRIMARY_PORT}
                    """
                    
                    // Start secondary instance
                    bat """
                        echo Starting secondary Tomcat instance...
                        cd /d "${SECONDARY_TOMCAT}\\bin"
                        start "Secondary-Tomcat-${SECONDARY_PORT}" /min startup.bat
                        echo ✓ Secondary Tomcat starting on port ${SECONDARY_PORT}
                    """
                    
                    // Wait for startup
                    echo 'Waiting for services to initialize...'
                    sleep(time: 45, unit: 'SECONDS')
                }
            }
        }
        
        stage('Health Verification') {
            steps {
                echo 'Performing comprehensive health checks...'
                script {
                    def healthCheckSuccess = false
                    def maxAttempts = 6
                    def currentAttempt = 0
                    
                    while (!healthCheckSuccess && currentAttempt < maxAttempts) {
                        currentAttempt++
                        echo "Health check attempt ${currentAttempt}/${maxAttempts}..."
                        
                        try {
                            bat """
                                cd /d "${WORKSPACE}"
                                
                                (
                                    echo try {
                                    echo     Write-Host 'Testing primary instance (${PRIMARY_PORT})...'
                                    echo     \\$primary = Invoke-WebRequest -Uri 'http://localhost:${PRIMARY_PORT}/${DEPLOY_NAME}/' -TimeoutSec 15 -UseBasicParsing
                                    echo     Write-Host "Primary status: \\$^(\\$primary.StatusCode^)"
                                    echo     
                                    echo     Write-Host 'Testing secondary instance (${SECONDARY_PORT})...'
                                    echo     \\$secondary = Invoke-WebRequest -Uri 'http://localhost:${SECONDARY_PORT}/${DEPLOY_NAME}/' -TimeoutSec 15 -UseBasicParsing
                                    echo     Write-Host "Secondary status: \\$^(\\$secondary.StatusCode^)"
                                    echo     
                                    echo     if (\\$primary.StatusCode -eq 200 -and \\$secondary.StatusCode -eq 200^) {
                                    echo         Write-Host '✓ SUCCESS: Both instances are healthy'
                                    echo         exit 0
                                    echo     } else {
                                    echo         Write-Host '✗ FAILED: Health check failed'
                                    echo         exit 1
                                    echo     }
                                    echo } catch {
                                    echo     Write-Host "✗ Health check error: \\$^(\\$_.Exception.Message^)"
                                    echo     exit 1
                                    echo }
                                ) > health_check.ps1
                                
                                powershell -ExecutionPolicy Bypass -File health_check.ps1
                                del health_check.ps1
                            """
                            healthCheckSuccess = true
                            echo "✓ Health check passed on attempt ${currentAttempt}"
                            
                        } catch (Exception e) {
                            echo "✗ Health check attempt ${currentAttempt} failed: ${e.getMessage()}"
                            if (currentAttempt < maxAttempts) {
                                echo "Retrying in 20 seconds..."
                                sleep(time: 20, unit: 'SECONDS')
                            }
                        }
                    }
                    
                    if (!healthCheckSuccess) {
                        error("✗ Health verification failed after ${maxAttempts} attempts")
                    }
                }
            }
        }
        
        stage('Deployment Validation') {
            steps {
                echo 'Validating deployment status...'
                bat """
                    echo.
                    echo ========================================
                    echo DEPLOYMENT VALIDATION REPORT
                    echo ========================================
                    
                    cd /d "${WORKSPACE}"
                    
                    (
                        echo Write-Host 'Checking Java processes...'
                        echo Get-Process ^| Where-Object {\\$_.ProcessName -like '*java*'} ^| Select-Object ProcessName, Id, StartTime ^| Format-Table -AutoSize
                        echo 
                        echo Write-Host 'Validating application deployments...'
                        echo if ^(Test-Path '${TOMCAT_WEBAPPS}\\${DEPLOY_NAME}'^) {
                        echo     Write-Host '✓ Primary (${PRIMARY_PORT}): Application deployed and extracted'
                        echo } elseif ^(Test-Path '${TOMCAT_WEBAPPS}\\${WAR_FILE}'^) {
                        echo     Write-Host '⚠ Primary (${PRIMARY_PORT}): WAR present but not extracted'
                        echo } else {
                        echo     Write-Host '✗ Primary (${PRIMARY_PORT}): Application NOT found'
                        echo }
                        echo 
                        echo if ^(Test-Path '${SECONDARY_TOMCAT}\\webapps\\${DEPLOY_NAME}'^) {
                        echo     Write-Host '✓ Secondary (${SECONDARY_PORT}): Application deployed and extracted'
                        echo } elseif ^(Test-Path '${SECONDARY_TOMCAT}\\webapps\\${WAR_FILE}'^) {
                        echo     Write-Host '⚠ Secondary (${SECONDARY_PORT}): WAR present but not extracted'
                        echo } else {
                        echo     Write-Host '✗ Secondary (${SECONDARY_PORT}): Application NOT found'
                        echo }
                        echo 
                        echo Write-Host 'Deployment validation completed.'
                    ) > validation.ps1
                    
                    powershell -ExecutionPolicy Bypass -File validation.ps1
                    del validation.ps1
                """
            }
        }
    }
    
    post {
        success {
            echo '''
            ========================================
            🎉 DEPLOYMENT COMPLETED SUCCESSFULLY! 🎉
            ========================================
            '''
            script {
                def deploymentInfo = """
                Application URLs:
                • Primary Instance:   http://localhost:${PRIMARY_PORT}/${DEPLOY_NAME}/
                • Secondary Instance: http://localhost:${SECONDARY_PORT}/${DEPLOY_NAME}/
                
                Source Repository: ${GIT_REPO}
                Build Timestamp: ${new Date().format('yyyy-MM-dd HH:mm:ss')}
                WAR File: ${WAR_FILE}
                """
                echo deploymentInfo
                
                // Send success notification (if configured)
                // emailext subject: "✅ Deployment Success: ${PROJECT_NAME}", 
                //          body: deploymentInfo,
                //          to: "team@company.com"
            }
        }
        
        failure {
            echo '''
            ========================================
            ❌ DEPLOYMENT FAILED!
            ========================================
            '''
            script {
                try {
                    echo 'Attempting automatic rollback...'
                    bat """
                        echo Checking available backups...
                        if exist "${BACKUP_DIR}" (
                            echo Available backups:
                            dir "${BACKUP_DIR}" /b
                            echo.
                            echo Rollback can be performed manually using the latest backup.
                        ) else (
                            echo No backup directory found.
                        )
                    """
                } catch (Exception e) {
                    echo "Rollback check failed: ${e.getMessage()}"
                }
                
                // Send failure notification (if configured)
                // emailext subject: "❌ Deployment Failed: ${PROJECT_NAME}", 
                //          body: "Check Jenkins logs for details: ${BUILD_URL}",
                //          to: "team@company.com"
            }
        }
        
        unstable {
            echo 'Build completed with warnings. Please review the logs.'
        }
        
        always {
            echo 'Cleaning up temporary files...'
            script {
                // Clean up any temporary PowerShell scripts
                bat '''
                    cd /d "%WORKSPACE%"
                    del *.ps1 2>nul || echo No PowerShell scripts to clean
                '''
            }
            echo "Pipeline execution completed at ${new Date().format('yyyy-MM-dd HH:mm:ss')}"
        }
    }
}