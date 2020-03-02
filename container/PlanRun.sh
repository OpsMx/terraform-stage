#!/bin/bash

#nohup java -Dspring.config.location=/home/terraspin/opsmx/app/config/application.properties  -jar /home/terraspin/artifact/TerraSpin.jar > /home/terraspin/artifact/terraspin.log 2>&1 &

#java -jar /home/terraspin/artifact/TerraSpin.jar --application.iscontainer.env=true
java -jar /home/terraspin/artifact/TerraSpin.jar

# For Debugging, Docker should alive! Uncommment while portation to keep containe live
# while :; do echo '*print*'; sleep 5; done

if [ $? -eq 0 ]; then
    echo -e '\n\n \t\t ================================ Terraform Plan Output ====================================== \t\t\n\n'
    jq .output /home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/planStatus | xargs -0 echo -e
    #while :; do echo '*print*'; sleep 5; done
    exit 0
else
    #while :; do echo '*print*'; sleep 5; done
    exit 127
fi



