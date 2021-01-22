FROM java:8
VOLUME /tmp
ADD moquette-0.12.1.zip push.zip
RUN apt-get install unzip
RUN unzip push.zip
EXPOSE 1883
CMD /moquette-0.12.1/bin/moquette.sh
