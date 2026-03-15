FROM eclipse-temurin:21-jdk


WORKDIR /home/ubuntu

COPY build/libs/Daewoo.jar Daewoo.jar

COPY ./uploads/ ./uploads/
#COPY ./uploads/hotelimage /app/uploads/hotelimage
#COPY ./uploads/parlorimage /app/uploads/parlorimage
#COPY ./uploads/userimage /app/uploads/userimage

# 디렉토리가 없을 수 있으므로 먼저 생성 후 권한 변경
RUN mkdir -p ./uploads/userimage ./uploads/hotelimage ./uploads/parlorimage && \
    chown -R ubuntu:ubuntu ./uploads

#RUN groupadd ubuntu && useradd -m -g ubuntu ubuntu
USER ubuntu

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "Daewoo.jar", "--spring.profiles.active=prod"]
