#!/bin/sh
 echo "INFO --- Checking User settings"
 if [ `id -u` -ge 10000 ]; then 
    cp /etc/passwd /tmp/passwd 
    echo "builder:x:`id -u`:`id -g`:,,,:/home/builder:/bin/bash" >> /tmp/passwd
    cat /tmp/passwd > /etc/passwd
    rm /tmp/passwd
    export HOME=/home/builder
 fi

