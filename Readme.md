# Terraspin -- :whale:

A microservice to integrate with Spinnaker for planning, applying and destroying Terraform plans
[TerraSpin Docs](https://docs.opsmx.com/codelabs/terraform-spinnaker) 

## Developing Terraspin
Need to run terraSpin locally for development? Here's what you need to setup and run:

```
# Environment setup & Bulding application
we are using maven as build tool for buliding source code so maven 3 and uper version is required in your machine to build this source code.

Clone this repository 
git clone https://github.com/OpsMx/terraform-stage.git

Once cloning is done go inside spinterra-core directory run below command to build application 
cmd- mvn clean install  
After buliding maven will put jar in target folder of spinterra-core directory

# Running application
To run microservice dial below command 
cmd- java -jar TerraSpin.jar 
```
