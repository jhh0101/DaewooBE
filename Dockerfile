FROM eclipse-temurin:21-jdk


WORKDIR /home/ubuntu

COPY build/libs/Daewoo.jar Daewoo.jar

COPY ./uploads/ ./uploads/
#COPY ./uploads/hotelimage /app/uploads/hotelimage
#COPY ./uploads/parlorimage /app/uploads/parlorimage
#COPY ./uploads/userimage /app/uploads/userimage

RUN chown -R ubuntu:ubuntu ./uploads/userimage

#RUN groupadd ubuntu && useradd -m -g ubuntu ubuntu
USER ubuntu

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "Daewoo.jar", "--spring.profiles.active=prod"]
