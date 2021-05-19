#!/bin/bash
if [ `id -u` -ge 10000 ]; then

    cp /etc/passwd /tmp/passwd
    echo "builder:x:`id -u`:`id -g`:,,,:/home/builder:/bin/bash" >> /tmp/passwd
    cat /tmp/passwd > /etc/passwd
    rm /tmp/passwd
fi


export HOME=/home/builder
echo -e "Executing Terraform Plan ...."
java -jar /home/builder/artifact/TerraSpin.jar 2>&1 > /home/builder/artifact/terraspin.log

RETURN_CODE=$?

if [ $RETURN_CODE -eq 0 ]; then

    echo -e "Terraform Plan Execution completed Successfully"
    echo -e '\n\n \t ================================ Terraform Plan Output ====================================== \t\t\n\n'

    PLANSTATUS=`jq -r .status /home/builder/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/planStatus | tr -d '\n'`

    echo -e 'Terraform Plan Status:' $PLANSTATUS "\n"

    if [ $PLANSTATUS != "SUCCESS" ]; then
	echo "Failed while executing Terraform Plan stage\n\n\n"
        cat /home/builder/artifact/terraspin.log
        jq -r .output /home/builder/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/planStatus
        exit 1
    fi

    jq -r .output /home/builder/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/planStatus | grep -E "Plan: "

    echo -e '\n\n \t =================================== Additional Info ========================================= \t\t\n\n'

    # Run terraform init to download plugins associated with provider
    cd $HOME/state_dir/
    terraform init > /dev/null

    # Show the output of terraform plan
    terraform show -no-color $HOME/state_dir/planOut
    cd $HOME

    exit 0

else
    ## Error while executing terraform plan
    echo -e "Error encountered while executing Terraform Plan\n"
    cat /home/builder/artifact/terraspin.log
    exit $RETURN_CODE
fi
