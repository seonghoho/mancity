pipeline {
    agent any
    tools {
        gradle 'Gradle 8.5'
    }
    environment {
        DOCKER_IMAGE_NAME = 'joonseong/mancity-user'
        DOCKERFILE_PATH = './user/Dockerfile'
        CONTAINER_NAME = 'mancity-user'
        REGISTRY_CREDENTIAL = 'dockerhub_IdPwd'
        DOCKER_IMAGE = ''
        DOCKER_IMAGE_TAG = 'latest'
    }
    stages {
        stage('GitLab Clone') {
            steps {
                git branch : 'develop-be-user', credentialsId: 'gitlab_access_token', url: 'https://lab.ssafy.com/s10-ai-image-sub2/S10P22C201.git'
            }
        }

        stage('Add Env') {
            steps {
                dir('./user') {
                    withCredentials([file(credentialsId: 'skey', variable: 'skey')]) {
                        sh 'chmod -R a=rwx src/main/resources'
                        sh 'cp ${skey} src/main/resources/application-skey.yml'
                    }
                }
            }
        }

        stage('Gradle Build') {
            steps {
                echo 'Building..'
                dir('./user') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean bootjar'
                }
            }
        }
        stage('Docker Build Image') {
            steps {
                dir('./user') {
                    script {
                        DOCKER_IMAGE = docker.build("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}", "-f Dockerfile .")
                    }
                }
            }
        }
        stage('Push Image to DockerHub') {
            steps {
                script {
                    docker.withRegistry('', REGISTRY_CREDENTIAL) {
                        DOCKER_IMAGE.push()
                    }
                }
            }
        }
        
        stage('Delete Previous user Docker Container'){
            steps {
                script {
                    // 컨테이너가 실행중이 아니거나 중지되어 있는 경우 아무런 동작하지 않고 넘어가도록
                    sh "docker stop ${CONTAINER_NAME} || true"
                }
            }
        }

        stage('Prune Docker Object'){
            steps {
                echo '##### delete stopped containers, networks, volumes, images, cache... #####'
                script {
                    sh "docker system prune --volumes -f"
                }
            }
        }

        stage('Pull from DockerHub') {
            steps {
                script {
                    sh 'docker pull ${DOCKER_IMAGE_NAME}'
                }
            }
        }

        stage('Run Docker Container') {
            steps {
                script {
                    sh 'docker run -d --name ${CONTAINER_NAME} -p 8081:8081 ${DOCKER_IMAGE_NAME}'
                }
            }
        }
    }
}
