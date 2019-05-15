FROM ubuntu:18.04

ARG IP_ADDRESS

RUN apt-get update && \
    apt-get install -y maradns sed && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY provisions/etc/mararc /etc/mararc
RUN  sed -i "s|IP_ADDRESS|$IP_ADDRESS|g" /etc/mararc

COPY provisions/etc/maradns/databases.c2s /etc/maradns/databases.c2s
RUN  sed -i "s|IP_ADDRESS|$IP_ADDRESS|g" /etc/maradns/databases.c2s

CMD [ "maradns" ]