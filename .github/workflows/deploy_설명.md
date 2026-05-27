name: backend deploy

on:
  push:
    branches: [ main ]   # main 브랜치에(계정) push 발생 시 자동 배포 실행

# 깃허브 리눅스로 ubuntu 를 사용하겠다는 의미
jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:

      # 1) 깃허브 리눅스 컴퓨터 이름(고정)
      - uses: actions/checkout@v4

      # 2) Java 설치: 테무린 자바 17 설치
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      # 3) Spring Boot WAR 빌드1(전에 파일 실행권한 부여)
      - name: Grant execute permission
        run: chmod +x simpledmsreact/gradlew

      # 3) Spring Boot WAR 빌드2: 결과물: xx.war
      - name: build
        run: |
          cd simpledmsreact
          ./gradlew bootWar

      # 4) SSH key 생성: 인증키: was_key01 복사 -> was.pem
      - name: SSH key
        run: |
          echo "${{ secrets.EC2_SSH_KEY }}" > was.pem
          chmod 600 was.pem

      # 5) WAR 업로드: 깃허브 리눅스 -> aws 리눅스 전송(.war)
      # 배포위치: ~ : /home/ubuntu 폴더를 의미합니다.
      - name: upload war
        run: |
          scp -o StrictHostKeyChecking=no -i was.pem \
          simpledmsreact/build/libs/*.war \
          ${{ secrets.EC2_USER }}@${{ secrets.EC2_HOST }}:~