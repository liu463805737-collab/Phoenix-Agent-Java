FROM cimg/openjdk:21.0

LABEL maintainer="军哥 463805737@qq.com"
WORKDIR /app


COPY ./target/phoenix-admin.jar /app/phoenix-admin.jar
RUN mkdir -p /app/logs
EXPOSE 8066
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/phoenix-admin.jar -Dlog.path=/data/applogs/phoenix"]