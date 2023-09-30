#!/bin/bash -e
### cliDeployScript CICD
###
##############################
if [ -z "$1" ]; then
    echo "BranchName (master, dev, test, etc)"
    read BranchName
else
    BranchName=$1
fi

### Read parameters from the json file
constants=$(cat cicd.param.${BranchName}.json | jq '.Parameters' | jq -r "to_entries|map(\"\(.key)=\(.value|tostring)\")|.[]")
for key in ${constants}; do
    eval ${key}
    variables=${variables}" "${key}
done

### Do Cloudformation stuff
StackName=${Application}CICD${BranchName}
### the variables parameter is being inserted in the string for aws cloudformation
codebucket=$(aws cloudformation describe-stacks --stack-name coreLambdaCode --query 'Stacks[0].Outputs[?OutputKey==`S3Bucket`].OutputValue' --output text)
### the variables parameter is being inserted in the string for aws cloudformation
#aws cloudformation deploy --template-file template.yaml --s3-bucket $codebucket --capabilities CAPABILITY_IAM --parameter-overrides ${variables} --stack-name ${StackName}
#cfn-lint -t cicd.yaml
sam build -b ./build -t ./cicd.yaml
sam package --template-file build/template.yaml --output-template-file packaged.yaml --s3-bucket $codebucket
sam deploy --template-file packaged.yaml --s3-bucket $codebucket --parameter-overrides ${variables} --capabilities CAPABILITY_IAM --stack-name ${StackName}