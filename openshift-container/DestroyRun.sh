#!/bin/bash
if [ `id -u` -ge 10000 ]; then

    cp /etc/passwd /tmp/passwd
    echo "builder:x:`id -u`:`id -g`:,,,:/home/builder:/bin/bash" >> /tmp/passwd
    cat /tmp/passwd > /etc/passwd
    rm /tmp/passwd
fi

echo "I am builder"
export HOME=/home/builder

echo -e "Executing Terraform Destroy ...."
java -jar /home/builder/artifact/TerraSpin.jar 2>&1 > /home/builder/artifact/builder.log

RETURN_CODE=$?

if [ $RETURN_CODE -eq 0 ]; then

    echo -e "Terraform Destroy Execution completed Successfully"
    echo -e '\n\n \t\t ================================ Terraform Destroy Output ====================================== \t\t\n\n'

    DESTROYSTATUS=`jq -r .status /home/builder/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/destroyStatus | tr -d '\n'`

    echo -e 'Terraform Destroy Status:' $DESTROYSTATUS "\n"

    if [ $DESTROYSTATUS != "SUCCESS" ]; then
	echo "Failed while executing Terraform Destroy stage\n\n\n"
        cat /home/builder/artifact/builder.log
        exit 1
    fi

    jq -r .output /home/builder/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/destroyStatus | grep -E "Destroy complete! Resources: "

    exit 0 
else
    ## Error while executing terraform plan
    echo -e "Error encountered while executing Terraform Destroy\n\n\n"
    cat /home/builder/artifact/builder.log
    exit $RETURN_CODE
fi


