#!/bin/bash
if [ `id -u` -ge 10000 ]; then

    cp /etc/passwd /tmp/passwd
    echo "builder:x:`id -u`:`id -g`:,,,:/home/builder:/bin/bash" >> /tmp/passwd
    cat /tmp/passwd > /etc/passwd
    rm /tmp/passwd
fi

echo "I am builder"
export HOME=/home/builder

echo -e "Executing Terraform Apply ...."
java -jar /home/builder/artifact/TerraSpin.jar 2>&1 > /home/builder/artifact/builder.log
RETURN_CODE=$?

if [ $RETURN_CODE -eq 0 ]; then

    echo -e "Terraform Apply Execution completed Successfully"
    echo -e '\n\n \t ================================ Terraform Apply Output ====================================== \t\t\n\n'

    APPLYSTATUS=`jq -r .status /home/builder/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/applyStatus | tr -d '\n'`

    echo -e 'Terraform Apply Status:' $APPLYSTATUS "\n"

    if [ $APPLYSTATUS != "SUCCESS" ]; then
	echo -e "Failed while executing Terraform Apply stage\n\n\n"
        cat /home/builder/artifact/builder.log
        echo -e "Printing Plan Status...\n\n"
        jq -r .output /home/builder/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/applyStatus
        exit 1
    fi

    jq -r .output /home/builder/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/applyStatus | grep -E "Apply complete! Resources: "

    echo -e '\n\n \t =================================== Additional Info ========================================= \t\t\n\n'

    # Run terraform init to download plugins associated with provider
    cd $HOME/state_dir
    terraform init > /dev/null
    terraform show -no-color terraform.tfstate
    cd $HOME

    # Get the SPINNAKER_PROPERTY variable already set by the java program and display on the console
    cat /home/builder/artifact/builder.log | grep 'SPINNAKER_PROPERTY_'
    echo 'SPINNAKER_PROPERTY_APPLYSTATUS='$APPLYSTATUS

    exit 0

else
    ## Error while executing terraform plan
    echo -e "Error encountered while executing Terraform Apply\n\n\n"
    cat /home/builder/artifact/builder.log
    exit $RETURN_CODE
fi
