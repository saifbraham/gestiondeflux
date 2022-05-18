pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
    }
    
    tools {
        maven 'Maven 3.3.9'
    }
    
    stages {
    	stage('initialize') {
    		steps {
    			sh '''
    				echo "PATH = ${PATH}"
    				echo "M2_HOME = ${M2_HOME}"
    			'''
    		}
    	}
        stage('build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
            post{
                success{
                    script{
                        if(env.BRANCH_NAME == "develop"){
                            echo 'deploy develop to DEVDEV'
                            sh 'mvn deploy -DskipTests -Pdevdev-deploy'
                        }else{
                            echo 'this branch isnt develop so we dont deploy to DEVDEV'
                        }
                    }
                }
            }
        }
		stage('publish') {
			steps {
				step([$class: 'hudson.plugins.checkstyle.CheckStylePublisher', checkstyle: 'target/checkstyle-result.xml'])
				step([$class: 'hudson.plugins.pmd.PmdPublisher', checkstyle: 'target/pmd.xml'])
				step([$class: 'FindBugsPublisher', pattern: '**/findbugsXml.xml', unstableTotalAll:'20'])
			}
		}        
		stage('Clover') {
			steps {
				sh 'mvn clean generate-sources clover:setup test clover:aggregate clover:save-history clover:clover --fail-never -Pjenkins'
				step([$class: 'CloverPublisher',
					cloverReportDir: 'target/site/clover',
					cloverReportFileName: 'clover.xml',
					healthyTarget: [methodCoverage: 10, conditionalCoverage: 10, statementCoverage: 10], // optional, default is: method=70, conditional=80, statement=80
					unhealthyTarget: [methodCoverage: 10, conditionalCoverage: 10, statementCoverage: 10], // optional, default is none
					failingTarget: [methodCoverage: 0, conditionalCoverage: 0, statementCoverage: 0]	 // optional, default is none
					])
			}
		}
    }
    post {
        unstable {
            step(
                [$class: 'Mailer',
                    subject: "Unstable Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is wrong with ${env.BUILD_URL}.",
                    notifyEveryUnstableBuild: true,
                    recipients: emailextrecipients([
                        [$class: 'CulpritsRecipientProvider'],
                        [$class: 'RequesterRecipientProvider'],
                        [$class: 'FailingTestSuspectsRecipientProvider'],
                    ])
                ]
            )
        }
        failure {
            step(
                [$class: 'Mailer',
                    subject: "Broken Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is broken in ${env.BUILD_URL}.",
                    notifyEveryUnstableBuild: true,
                    recipients: emailextrecipients([
                        [$class: 'CulpritsRecipientProvider'],
                        [$class: 'RequesterRecipientProvider'],
                        [$class: 'FirstFailingBuildSuspectsRecipientProvider']
                    ])
                ]
            )
        }
    }
}