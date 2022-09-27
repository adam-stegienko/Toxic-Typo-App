pipeline {
    agent any
    environment {
        GITLAB = credentials('a31843c7-9aa6-4723-95ff-87a1feb934a1')
        AWS_CREDS = credentials('aws-adam-iam')
    }
    tools {
        maven 'Maven 3.6.2'
        jdk 'JDK 8'
    }
    stages {
        stage('Parameters Set-up') {
            steps {
                script {
                    properties([
                        disableConcurrentBuilds(), 
                        gitLabConnection(gitLabConnection: 'GitLab API Connection', jobCredentialId: ''), 
                        [$class: 'GitlabLogoProperty', repositoryName: 'adam/suggest-lib'], 
                        parameters([
                            validatingString(
                                description: 'Put "feature" and a 2-digit value meaning the branch you would like to build your version on, as in the following examples: "feature/1.0", "feature/1.1", "feature/1.2", etc. Parameter also available for master branch.',
                                name: 'Version', 
                                regex: '^master$|^feature\\/[0-9]{1,}\\.[0-9]{1,}$'
                            )
                        ]), 
                    ])
                }
            }
        }
        stage('Feature branch versioning') {
            when { branch "feature/*" }
            steps{
                script {
                    BRANCH = params.Version
                    sh"""
                        git checkout master
                        git remote set-url origin http://\"$GITLAB\"@ec2-3-125-51-254.eu-central-1.compute.amazonaws.com/adam/toxic-typo.git
                        git pull --rebase
                    """
                    BRANCH_EXISTING = sh(
                        script: "(git ls-remote -q | grep -w $BRANCH) || BRANCH_EXISTING=False",
                        returnStdout: true,
                    )
                        if (BRANCH_EXISTING) {
                            echo "The $BRANCH branch is already existing."
                            sh """
                            git checkout $BRANCH
                            git pull origin $BRANCH --rebase
                            git fetch --tags
                            """
                        } else {
                            echo "The $BRANCH branch is not exsiting yet and needs to be created."
                            sh"""
                            git branch $BRANCH
                            git checkout $BRANCH
                            git remote set-url origin http://\"$GITLAB\"@ec2-3-125-51-254.eu-central-1.compute.amazonaws.com/adam/toxic-typo.git
                            git fetch --tags
                            """
                        }
                    MINOR_VERSION = BRANCH.split("/")[1]
                    LATEST_TAG = sh(
                        script: "git describe --tags --abbrev=0 | grep -E '^$MINOR_VERSION' || true",
                        returnStdout: true,
                    )
                    echo "This is $LATEST_TAG"
                        if (LATEST_TAG) {
                            NEW_PATCH = (LATEST_TAG.tokenize(".")[2].toInteger() + 1).toString()
                        } else {
                            NEW_PATCH = "0"
                        }
                    NEW_TAG = MINOR_VERSION + "." + NEW_PATCH
                    echo "The new tag for feature commit is $NEW_TAG"
                    VERSION_TAG = "latest"
                    }
                }
            }
        stage('Master branch versioning') {
            when { branch "master" }
            steps{
                script {
                    BRANCH = env.BRANCH_NAME
                    sh"""
                        git checkout master
                        git remote set-url origin http://\"$GITLAB\"@ec2-3-125-51-254.eu-central-1.compute.amazonaws.com/adam/toxic-typo.git
                        git pull --rebase
                        git fetch --tags
                    """
                    LATEST_TAG = sh(
                        script: "git describe --tags --abbrev=0 || true",
                        returnStdout: true,
                    ).toString()
                    echo "This is $LATEST_TAG"
                        if (LATEST_TAG == "+.+.+") {
                            NEW_TAG = LATEST_TAG
                        } else if (LATEST_TAG == "+.+") {
                            NEW_TAG = LATEST_TAG + ".0"
                        }
                    echo "The new tag for release on master branch is $NEW_TAG"
                    VERSION_TAG = NEW_TAG
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
                    docker rm -f toxic_typo python_test
                    docker run -d --name=toxic_typo -p 8000:8080 adam-toxictypo:$VERSION_TAG > test_logs.txt
                    docker build -t python_tester:latest -f Dockerfile.python .
                    docker run -it --name=python_test python_tester:latest
                    sleep 3
                    docker rm -f toxic_typo python_test
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
                    docker tag adam-toxictypo:latest 644435390668.dkr.ecr.eu-central-1.amazonaws.com/adam-toxictypo:latest
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
                    ssh -t -i "/home/.ssh/adam-lab.pem" ubuntu@18.192.58.176
                    aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 644435390668.dkr.ecr.eu-central-1.amazonaws.com
                    docker pull 644435390668.dkr.ecr.eu-central-1.amazonaws.com/adam-toxictypo:latest
                    docker rm -f toxic_typo
                    docker run --name=toxic_typo -d -p 8000:8080 644435390668.dkr.ecr.eu-central-1.amazonaws.com/adam-toxictypo:latest
                    """
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
                    git tag -a $NEW_TAG -m \"New $NEW_TAG tag added to latest commit on branch $BRANCH\"
                    git push origin $BRANCH --tag
                    """
                }
            }
        }
    }
    post {
        when { branch "feature/*"}
        failure {  
             mail 
             bcc: '', 
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