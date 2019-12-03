import java.text.SimpleDateFormat
import java.lang.String

//ver 1.8 - Added multiple branch with build trigger to simulate the immutable artifact solution

node {
    def shortCommit
    def pom
    //docker.withTool('/usr/local/bin/docker')
  try {

        environment {
            registry = "docker-test"
            registryCredential = 'dockerhub'
            dockerImage = ''
        }

if (env.BRANCH_NAME == 'develop'|| env.BRANCH_NAME == 'release' || env.BRANCH_NAME == 'dev')
{
       //Step #1. checkout the files
        stage ("Code checkout")  {
                checkout scm
                // credentialsId: 'dinesh'
                // echo 'Checking out the files from repo...' + env.BRANCH_NAME
                // git 'https://github.com/OctoDevOps/codelab.git'
                 }
//        dir('SpringLab') {
        def mvnHome = tool 'M3'
        def targetVersion = "${env.BUILD_NUMBER}" //getDevVersion()

        sh "/usr/local/bin/docker kill  dkautomation || true"
        sh "/usr/local/bin/docker rm -f  dkautomation || true"
        sh "/usr/local/bin/docker rmi  dkautomation || true"
        sh "/usr/local/bin/docker system prune || true"

        // timeout(5) {
        //     waitUntil {
        //         script {
        //             def result = sh script: '/usr/local/bin/docker ps -f "name=dkautomation" --format "{{.ID}}"'
        //             print result
        //             return result == null;
        //         }
        //     }
        // }

        //Step #2. Build with unit testing
        stage("Build with Unit Testing ")
        {
                    print 'target build version...'
                    print targetVersion
                    withSonarQubeEnv('localhost_sonarqube') {
                        sh "pwd;'${mvnHome}/bin/mvn' -Dintegration-tests.skip=true -Dbuild.number=${targetVersion} clean compile test sonar:sonar"
                    }
                }

            // No need to occupy a node
            stage("Quality Gate"){
                withSonarQubeEnv('localhost_sonarqube') {
                    timeout(time: 1, unit: 'HOURS') { // Just in case something goes wrong, pipeline will be killed after a timeout
                        def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
                        if (qg.status != 'OK') {
                        error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                        else{
                            sh "pwd;'${mvnHome}/bin/mvn' -Dintegration-tests.skip=true -Dbuild.number=${targetVersion} package"
                            pom = readMavenPom file: 'pom.xml'
                            // get the current development version
                            developmentArtifactVersion = "${pom.version}-${targetVersion}"
                            print 'Build Artifact version:'
                            print developmentArtifactVersion
                            // execute the unit testing and collect the reports
                            //junit '**//*target/unit-testing-reports/TEST-*.xml'
                            archiveArtifacts 'target*//*.jar'
                        }
                    }
                }
            }

            // stage("Publishing Unit Testing Metris to Confluence")
            // {
            //     withSonarQubeEnv('localhost_sonarqube') {
                    
            //     }

            // }


        stage("Stop, Deploy and Restart"){
            echo "Build package is ready to deploy"
            // shutdown
            //sh 'curl -X POST http://localhost:9090/shutdown || true'
            // copy file to target location, and start the application
        // sh 'cp target/*.jar /Users/dineshganesan/codelab/buildrun/;' --gd
        //withEnv(['JENKINS_NODE_COOKIE=dontkill']) {
        //     sh 'nohup java -Dserver.port=9090 -jar /Users/dineshganesan/codelab/buildrun/*.jar &'
        //}
            // sh "'${mvnHome}/bin/mvn' package"
            //dockerImage = sh "/usr/local/bin/docker build -t imgautomation ." -- gd
            pom = readMavenPom file: 'pom.xml'
            // get the current development version
            developmentArtifactVersion = "${pom.artifactId}_${pom.version}"
            echo "Artifact Name ---> $developmentArtifactVersion"

            dockerImage = docker.build("services/$developmentArtifactVersion")
            sh "/usr/local/bin/docker run --name dkautomation -d -p 9090:8080 services/$developmentArtifactVersion"

            //dockerImage = docker.build('services/imgautomation') -- gd
           // sh "/usr/local/bin/docker run --name dkautomation -d -p 9090:8080 services/imgautomation" -- gd

            echo "Successfully launched the app"
            // wait for application to respond
            //sh 'while ! httping -qc1 http://localhost:8081 ; do sleep 1 ; done'
            }
}

if (env.BRANCH_NAME == 'release')
{
        stage("Push Image to Repo "){
            echo "Publishing docker image to docker registry repo"


                docker.withRegistry('http://localhost:8090', 'nexus_access_id') {
                    dockerImage.push('snapshot')
                }

                def userInput = true
                def didTimeout = false
                try {
                    timeout(time: 60, unit: 'SECONDS') { // change to a convenient timeout for you
                        userInput = input(
                        ok:'Yes', message: 'Please confirm if you are satisfied with the QA results', parameters: [
                        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Certified']
                        ])
                    }
                } catch(err) { // timeout reached or input false
                    def user = err.getCauses()[0].getUser()
                    if('SYSTEM' == user.toString()) { // SYSTEM means timeout.
                        didTimeout = true
                    } else {
                        userInput = false
                        echo "Aborted by: [${user}]"
                    }
                }

                node {
                    if (didTimeout) {
                        // do something on timeout
                        echo "no input was received before timeout"
                    } else if (userInput == true) {
                        // do something
                        echo "Admin has approved the build and it's ready to release now"
                        docker.withRegistry('http://localhost:8091', 'nexus_access_id') {

                            stage ('Label RC'){
                                // sh 'docker run -d -p 8080:8080 -t safe/gs-spring-boot-docker --name safe-ws'
                                // app.push("${shortCommit}")
                                dockerImage.push('release')
                            }
                        }
                    } else {
                        // do something else
                        echo "Admin has rejected the release deployment"
                        currentBuild.result = 'SUCCESS'
                    } 
                }

            // } 


            // dockerImage = docker.build registry + ":$BUILD_NUMBER"
            // docker.withRegistry( '', registryCredential ) {dockerImage.push()
            // prepare docker build context
            //associateTag nexusInstanceId: 'iae_artifact_repo', search: [[key: 'name', value: 'iae_artifact_repo']], tagName: 'iae_automation_snapshot'
            //archiveArtifacts allowEmptyArchive: true, artifacts: 'imgautomation', onlyIfSuccessful: true
        }
}

    if (env.BRANCH_NAME == 'master')
    {
        stage ('Deploy and Launch RC'){

            pom = readMavenPom file: 'pom.xml'
            // get the current development version
            releaseArtifactName = "${pom.artifactId}_${pom.version}"
            echo "Artifact Name ... $releaseArtifactName"

            releaseArtifactLiveName = "${pom.artifactId}_${pom.version}_rc"
            echo "Artifact Live Name ==> $releaseArtifactLiveName"

            sh "/usr/local/bin/docker kill  $releaseArtifactLiveName || true"
            sh "/usr/local/bin/docker rm  $releaseArtifactLiveName || true"
            timeout(5) {
                waitUntil {
                    script {
                        def result = sh script: '/usr/local/bin/docker ps -f "name=$releaseArtifactVersion" --format "{{.ID}}"'
                        print result
                        return result == null;
                    }
                }
            }

            //docker.image("localhost:8091/services/imgautomation:release").run('-p 9091:8080 -h 0.0.0.0 --name imgautomation_rc')
            docker.image("localhost:8091/services/releaseArtifactName:release").run('-p 9091:8080 -h 0.0.0.0 --name releaseArtifactLiveName')
        }
        
    }


                // stage("Update JIRA"){
                //         updateJira("${env.BUILD_NUMBER}")
                //         //jiraComment(issueKey: "${ISSUE_ID}", body: "Job '${env.JOB_NAME}' (${env.BUILD_NUMBER}) was successful. Please go to ${env.BUILD_URL}.")
                //         print 'Updated JIRA sucessfully!'

                //         //setBuildStatus("ci/jenkins/build-status", "Build # ${env.BUILD_NUMBER} is pass", 'FAILURE')
                //         setBuildStatus("ci/jenkins/build-status", "Build # ${env.BUILD_NUMBER} is pass", 'SUCCESS')
                //     }

                // stage("Integration Testing"){
                // //    dir('/Users/dineshganesan/codelab/QA_Scripts') {
                //         notifyBuild("Integration Testing Started..")

                //         //running test for jenkins admin console    
                //         sh 'pwd;newman run ../QA_Scripts/JPT_Automation_Verison_Mgmt.postman_collection.json -d "../QA_Scripts/getRel_testdata.csv" --reporters cli,junit,html --reporter-junit-export var/reports/newman/junit/newman.xml --reporter-html-export var/reports/newman/html/index.html'
                        
                //         //running test to generate report for confluence
                //         sh 'pwd;newman run ../QA_Scripts/JPT_Automation_Verison_Mgmt.postman_collection.json -d "../QA_Scripts/getRel_testdata.csv" --reporters cli,json,html --reporter-json-export var/reports/newman/json/testreport.json --reporter-html-export var/reports/newman/html/rpt_int_testing.html --reporter-html-template "../QA_Scripts/conf_api_test_rpt.hbs"'
                //         print 'Integration testing is compelte'
                //         //publishing the result in HTML format
                //         //publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'SpringLab/var/reports/newman/html', reportFiles: 'index.html', reportName: 'JPT - API Test Report', reportTitles: 'Integration Testing'])
                //         //publishing the file to confluence
                //         //publishConfluence editorList: [confluenceWritePage(confluenceFile('index.html'))], labels: 'Automation Test', pageName: 'API Integration Testing', replaceAttachments: true, siteName: 'localhost', spaceName: 'JA'

                        
                //     }

              /*  
            stage('Update Confluence Page') {
                        steps {
                            sh '''
                                #!/bin/bash
                                curl -u ${CONFLUENCE_PAGE_CREDS} 'http://localhost/rest/api/content/'${PAGE_ID}'?expand=version' | python -mjson.tool > version.txt
                                PAGE_VERSION=$(grep -Po '(?<="number": )[0-9]+' version.txt)
                                rm version.txt
                                PAGE_VERSION=$((PAGE_VERSION+1))
                                curl -u ${CONFLUENCE_PAGE_CREDS} 'https://YOURDOMAIN.atlassian.net/wiki/rest/api/content/'${PAGE_ID}'?expand=body.storage' | python -mjson.tool > body.txt
                                more body.txt
                                PAGE_BODY="$(grep -Po '(?<="value": ")[^"]+' body.txt)"
                                rm body.txt
                                TEXT='<p>The content to append</p>'
                                TEXT=$PAGE_BODY$TEXT
                                echo '{"id":"'${PAGE_ID}'","type":"page","title":"NEW PAGE","space":{"key":"TR"},"body":{"storage":{"value":"'$TEXT'","representation":"storage"}},"version":{"number":'$PAGE_VERSION'}}' > update.json
                                curl -u ${CONFLUENCE_PAGE_CREDS} -X PUT -H 'Content-Type: application/json' -d '@update.json' https://YOURDOMAIN.atlassian.net/wiki/rest/api/content/${PAGE_ID} | python -mjson.tool
                                rm update.json
                            '''
                        }
                    }*/
//                } 
   } catch(err){
       //bitbucketStatusNotify(buildState: 'FAILED')
       //jiraComment(issueKey: "${ISSUE_ID}", body: "Job '${env.JOB_NAME}' (${env.BUILD_NUMBER}) was failed. Please go to ${env.BUILD_URL}.")
       currentBuild.result = "FAILURE"
       print 'error ' + err
       throw err
   } 
   finally{
    //     print 'Generating HTML Repot ...'
    //     notifyBuild("Integration test result is available now.")
    //     publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'SpringLab/var/reports/newman/html', reportFiles: 'index.html', reportName: 'JPT - API Test Report', reportTitles: 'Integration Testing'])
    //     //junit '/Users/dineshganesan/codelab/QA_Scripts/**/*.xml' 
    //     print 'Publishing the HTML Repot to Confluence...'
    //     //def pom = readMavenPom file: 'pom.xml'
    //     def project_name = "Rel.${pom.version}"+" API Test Report "+"${env.BUILD_NUMBER}"
        
    //    // def project_build_status =  currentBuild.result
    //    // currentBuild.result = hudson.model.Result.SUCCESS

    //     print 'Publishing the'+project_name+'HTML Repot to Confluence...'
    //     publishConfluence editorList: [confluenceWritePage(confluenceFile('SpringLab/var/reports/newman/html/rpt_int_testing.html'))], labels: 'Automation Test', pageName: project_name,  parentId: 65609, replaceAttachments: false, siteName: 'localhost', spaceName: 'JA'

        //currentBuild.result = project_build_status
        //publishConfluence attachArchivedArtifacts: true, pageName: 'API Integration Testing', siteName: 'localhost', spaceName: 'JA'
   }
}
    /**
    * Updates Jira Fixed in build field with the build number and adds a comment in each related Jira issue
    * This requires the Jenkins JIRA Pipeline Steps plugin https://jenkinsci.github.io/jira-steps-plugin/getting-started/
    * @param build Build number that will be entered in the "Fixed in Build" Jira field
    */
  
    def updateJira(build) {
        def jiraServer = 'JIRA_LOCAL' // Define a Jira server entry in the Jenkins Jira Steps configuration named JIRA-PROD
        def jiraIssues = jiraIssueSelector(issueSelector: [$class: 'DefaultIssueSelector'])
        //print 'Total # of Jira issues included in this build:' + jiraIssues.length
        jiraIssues.each { issue ->
            print 'JIRA #' + issue
            jiraAddComment comment: "{panel:bgColor=#97FF94}{code}Code was added to address this issue in build ${build}{code} {panel}", idOrKey: issue, site: jiraServer
            //def fixedInBuild = [fields: [customfield_10121: build]] // This is a custom field named "Fixed in Build"
            //jiraEditIssue idOrKey: issue, issue: fixedInBuild, site: jiraServer
        }
    }

    def setBuildStatus(context, message, state) {
    // partially hard coded URL because of https://issues.jenkins-ci.org/browse/JENKINS-36961, adjust to your own GitHub instance
    print "Publishing the build status"
    
        step([
            $class: "GitHubCommitStatusSetter",
            //contextSource: [$class: "ManuallyEnteredCommitContextSource", context: context],
            reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/OctoDevOps/codelab.git"],
            errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
            statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
        ]);
    }

    def getChangeString() {
        MAX_MSG_LEN = 100
        def changeString = ""

        echo "Gathering SCM changes"
        def changeLogSets = currentBuild.changeSets
        for (int i = 0; i < changeLogSets.size(); i++) {
            def entries = changeLogSets[i].items
            for (int j = 0; j < entries.length; j++) {
                def entry = entries[j]
                truncated_msg = entry.msg.take(MAX_MSG_LEN)
                changeString += " - ${truncated_msg} [${entry.author}]\n"
            }
        }

        if (!changeString) {
            changeString = " - No new changes"
        }
        return changeString
    }

def notifyBuild(String buildStatus = 'STARTED') {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESSFUL'

  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def summary = "${subject} (${env.BUILD_URL})"
  def details = """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
  } else if (buildStatus == 'SUCCESSFUL') {
    color = 'GREEN'
    colorCode = '#00FF00'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
  }

  // Send notifications
  slackSend (color: colorCode, message: summary)

  emailext(
      subject: subject,
      body: details,
      recipientProviders: [[$class: 'DevelopersRecipientProvider']]
    )
}

/* to remove the build history from jenkinsfiles
    def jobName = "JPT_Automation"
    def job = Jenkins.instance.getItem(jobName)
    job.getBuilds().each { it.delete() }
    job.nextBuildNumber = 1
    job.save()
*/

 /* to create a confluence page
 pipeline {
    agent any
    environment {
      CONFLUENCE_PAGE_CREDS = credentials('confluence-creds')
      PAGE_ID = credentials('confluence-page-id')
    }
    stages {
        stage('Update Confluence Page') {
            steps {
                sh '''
                    #!/bin/bash
                    TEXT='<p>New page</p>'
                    echo '{"type":"page","title":"New page","ancestors":[{"id":"'${PAGE_ID}'"}],"space":{"key":"TR"},"body":{"storage":{"value":"'$TEXT'","representation":"storage"}}}' > update.json
                    curl -u ${CONFLUENCE_PAGE_CREDS} -X POST -H 'Content-Type: application/json' -d '@update.json' https://YOURDOMAIN.atlassian.net/wiki/rest/api/content/ | python -mjson.tool
                    rm update.json
                '''
            }
        }
    }
}
*/