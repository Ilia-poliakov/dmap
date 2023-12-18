FROM bellsoft/liberica-openjdk-alpine:21.0.1-16-x86_64
COPY dmap-node/target/dmap-node.jar /opt/
EXPOSE ${PORT_TO_EXPOSE} 9090
ENTRYPOINT [ "java",\
             "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",\
             "--add-opens=java.base/java.nio=ALL-UNNAMED",\
             "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",\
             "-Dio.netty.tryReflectionSetAccessible=true",\
             "-XX:+UseZGC",\
             "-Xlog:gc:file=/var/log/gc.log:utctime,pid,tags:filecount=32,filesize=64m",\
             "-XX:+HeapDumpOnOutOfMemoryError",\
             "-XX:HeapDumpPath=/var/dmap/",\
             "-jar", "/opt/dmap-node.jar"\
]