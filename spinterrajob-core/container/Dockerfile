FROM opsmx11/java:ubuntu16_java8

MAINTAINER OpsMx

RUN apt-get update && apt-get install -y \
    git \
    curl \
    openssl\
    jq \
    wget \
    unzip \
    vim


RUN curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.deb.sh | bash

RUN apt-get install -y \
    git-lfs 

#RUN git lfs install

RUN groupadd -g 999 terraspin && \
    useradd -r -u 999 -g terraspin terraspin && \
    mkdir -p /home/terraspin && \
    chown terraspin /home/terraspin

USER terraspin

RUN mkdir -p /home/terraspin/artifact /home/terraspin/extra /home/terraspin/overridefilerepodir /home/terraspin/tfstatefilerepodir /home/terraspin/opsmx/app/config /home/terraspin/.ssh && \
    touch /home/terraspin/artifact/terraspin.log && \
    chmod 777 /home/terraspin/artifact/terraspin.log

WORKDIR /home/terraspin

RUN wget https://releases.hashicorp.com/terraform/0.12.28/terraform_0.12.28_linux_amd64.zip && \
    unzip -a terraform_0.12.28_linux_amd64.zip && \
    chmod +x terraform &&\
    rm -rf terraform_0.12.28_linux_amd64.zip

RUN wget -O vault.zip https://releases.hashicorp.com/vault/1.7.0/vault_1.7.0_linux_amd64.zip && \
    unzip vault.zip && \
    chmod +x vault && \ 
    rm -rf vault.zip

RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" 

RUN chmod +x kubectl

USER root

RUN mv vault /usr/local/bin/vault

RUN mv terraform /usr/local/bin/terraform

RUN mv kubectl /usr/local/bin/kubectl

USER terraspin

COPY /id_rsa.pub /home/terraspin/.ssh/id_rsa.pub

COPY /PlanRun.sh  /usr/local/bin/PlanRun.sh

COPY /ApplyRun.sh  /usr/local/bin/ApplyRun.sh

COPY /DestroyRun.sh  /usr/local/bin/DestroyRun.sh

COPY /TerraSpin.jar  /home/terraspin/artifact/TerraSpin.jar

#CMD run.sh
