pipeline {
    agent any
    environment {
        GITLAB = credentials('a31843c7-9aa6-4723-95ff-87a1feb934a1')
        AWS_CREDS = credentials('aws-adam-iam')
    }
    stages {
        stage('Properties set-up') {
            steps {
                script {
                    properties([
                        disableConcurrentBuilds(), 
                        gitLabConnection(gitLabConnection: 'GitLab API Connection', jobCredentialId: ''), 
                        [$class: 'GitlabLogoProperty', repositoryName: 'adam/suggest-lib']
                    ])
                }
            }
        }
        stage('Cleaning') {
            steps {
                script {
                    deleteDir()
                    checkout scm
                }
            }
        }
        stage('Feature branch versioning') {
            when { branch "feature/*" }
            steps{
                script {
                    sh"""
                        git checkout master
                        git remote set-url origin http://\"$GITLAB\"@gitlab_repo/adam/toxic-typo.git
                        git pull --rebase
                    """
                    BRANCH_EXISTING = sh(
                        script: "(git ls-remote -q | grep -w $BRANCH_NAME) || BRANCH_EXISTING=False",
                        returnStdout: true,
                    )
                        if (BRANCH_EXISTING) {
                            echo "The $BRANCH_NAME branch is already existing."
                            sh """
                            git checkout $BRANCH_NAME
                            git pull origin $BRANCH_NAME --rebase
                            git fetch --tags
                            """
                        } else {
                            echo "The $BRANCH_NAME branch is not exsiting yet and needs to be created."
                            sh"""
                            git branch $BRANCH_NAME
                            git checkout $BRANCH_NAME
                            git remote set-url origin http://\"$GITLAB\"@gitlab_repo/adam/toxic-typo.git
                            git fetch --tags
                            """
                        }
                    MINOR_VERSION = BRANCH_NAME.split("/")[1]
                    LATEST_TAG = sh(
                        script: "git tag | sort -V | grep '^$MINOR_VERSION' | tail -1  || true",
                        returnStdout: true,
                    ).toString()
                        if (LATEST_TAG == "*.0") {
                            NEW_PATCH = "1"
                        } else if (LATEST_TAG) {
                            NEW_PATCH = (LATEST_TAG.tokenize(".")[2].toInteger() + 1).toString()
                        } else {
                            NEW_PATCH = "0"
                    }
                    NEW_TAG = MINOR_VERSION + "." + NEW_PATCH
                    echo "The new tag for feature commit is $NEW_TAG"
                    VERSION_TAG = "latest"
                    echo "The tag used for pipeline operations will be '$VERSION_TAG'"
                    }
                }
            }
        stage('Master branch versioning') {     
            when { branch "master" }
            steps{
                script {
                    sh"""
                        git checkout master
                        git remote set-url origin http://\"$GITLAB\"@gitlab_repo/adam/toxic-typo.git
                        git pull --rebase
                        git fetch --tags
                    """
                    LATEST_TAG = sh(
                        script: "git tag | sort -V | tail -1 || true",
                        returnStdout: true,
                    ).toString()
                    NEW_PATCH = (LATEST_TAG.tokenize(".")[2].toInteger() + 1).toString()
                    NEW_TAG = LATEST_TAG.tokenize(".")[0].toString() + "." + LATEST_TAG.tokenize(".")[1].toString() + "." + NEW_PATCH
                    echo "The new tag for release on master branch is $NEW_TAG"
                    VERSION_TAG = NEW_TAG
                    echo "The tag used for pipeline operations will be '$VERSION_TAG'"
                }
            }    
        }
        stage('ToxicTypo Build') {
            steps {
                script {
                    sh """
                    docker build -t adam-toxictypo:$VERSION_TAG -f Dockerfile.java .
                    """
                    echo "ToxicTypo image version $VERSION_TAG has been built successfully."
                }
            }
        }
        stage('ToxicTypo E2E Test') {
            steps {
                script {
                    sh"""
                    docker rm -f toxic_typo
                    docker run -d --name=toxic_typo -p 8000:8080 adam-toxictypo:$VERSION_TAG
                    docker build -t python_tester:latest -f Dockerfile.python .
                    docker run --rm --name=python_test python_tester:latest
                    docker rmi python_tester:latest
                    docker rm -f toxic_typo
                    """
                }
            }
        }
        stage('ToxicTypo Image Publishing to AWS ECR Repo') {
            when { branch "master" }
            steps {
                script {
                    sh"""
                    aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 644435390668.dkr.ecr.eu-central-1.amazonaws.com
                    docker tag adam-toxictypo:$VERSION_TAG 644435390668.dkr.ecr.eu-central-1.amazonaws.com/adam-toxictypo:$VERSION_TAG
                    docker push 644435390668.dkr.ecr.eu-central-1.amazonaws.com/adam-toxictypo:$VERSION_TAG
                    docker tag adam-toxictypo:$VERSION_TAG 644435390668.dkr.ecr.eu-central-1.amazonaws.com/adam-toxictypo:latest
                    docker push 644435390668.dkr.ecr.eu-central-1.amazonaws.com/adam-toxictypo:latest
                    echo "ToxicTypo image version latest and version $VERSION_TAG has been successfully pushed to remote repo."
                    """
                }
            }
        }
        stage('Deploy to PROD Instance') {
            when { branch "master" }
            steps {
                script {
                    sh"""
                    ssh -i "/var/jenkins_home/adam-lab.pem" ubuntu@3.126.120.246
                    aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 644435390668.dkr.ecr.eu-central-1.amazonaws.com
                    docker rmi -f 644435390668.dkr.ecr.eu-central-1.amazonaws.com/adam-toxictypo:latest
                    docker pull 644435390668.dkr.ecr.eu-central-1.amazonaws.com/adam-toxictypo:latest
                    sleep 5
                    docker rm -f toxic_typo
                    docker run --name=toxic_typo -d -p 8001:8080 644435390668.dkr.ecr.eu-central-1.amazonaws.com/adam-toxictypo:latest
                    """
                    sh "sleep 5"
                    sh "curl http://3.126.120.246:8001"
                }
            }
        }
        stage('Tagging and Pushing to GitLab Repository') {
            steps {
                script {
                    sh"""
                    git config --global user.email "adam.stegienko1@gmail.com"
                    git config --global user.name "Adam Stegienko"
                    git clean -f -x
                    git tag -a $NEW_TAG -m \"New $NEW_TAG tag added to latest commit on branch $BRANCH_NAME\"
                    git push origin $BRANCH_NAME --tag
                    """
                    echo "All new tags have been pushed to GitLab repo."
                }
            }
        }
    }
    post {
        failure {  
             mail bcc: '', 
             body: "<b>Build Failed</b><br>Project: $env.JOB_NAME <br>Build Number: $env.BUILD_NUMBER <br> Build's URL: $env.BUILD_URL", 
             cc: '', 
             charset: 'UTF-8', 
             from: '', 
             mimeType: 'text/html', 
             replyTo: '', 
             subject: "ERROR CI: Project name -> $env.JOB_NAME", 
             to: "adam.stegienko1@gmail.com";
        }
    }
}
