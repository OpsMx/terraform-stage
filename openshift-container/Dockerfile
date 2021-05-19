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

#RUN groupadd -g 999 terraspin && \
#    useradd -r -u 999 -g terraspin terraspin && \
#    mkdir -p /home/builder && \
#    chown terraspin /home/builder

USER root

RUN chmod 666 /etc/passwd && mkdir -p /home/builder/.ssh .local .cache  && chmod -R 777 /home/builder && chmod -R 777 .local .cache && export PATH="$PATH:/.local/bin"
#RUN mkdir -p  /home/builder && chmod 666 /etc/passwd && chmod 777 -R /home/builder
RUN mkdir -p /home/builder/artifact /home/builder/extra /home/builder/overridefilerepodir /home/builder/tfstatefilerepodir /home/builder/opsmx/app/config /home/builder/.ssh /home/builder/tfstatefilerepodir/terraform-state-tf-bucket/pipelineId-spinPipeId && \
    touch /home/builder/artifact/terraspin.log && \
    chmod 777 /home/builder/artifact/terraspin.log

RUN chmod -R 777 /home/builder/

WORKDIR /home/builder

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

#USER terraspin

COPY /id_rsa.pub /home/builder/.ssh/id_rsa.pub

COPY /PlanRun.sh  /usr/local/bin/PlanRun.sh

COPY /ApplyRun.sh  /usr/local/bin/ApplyRun.sh

COPY /DestroyRun.sh  /usr/local/bin/DestroyRun.sh

COPY /TerraSpin.jar  /home/builder/artifact/TerraSpin.jar
#COPY entrypoint.sh /entrypoint.sh
#RUN chmod 777 /entrypoint.sh
#ENTRYPOINT ["sh", "/entrypoint.sh"]
