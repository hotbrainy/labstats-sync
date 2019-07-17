#!/bin/bash -e
BranchName=release
StackName=lbstCodeBuild${BranchName}
aws cloudformation deploy --template-file lbstInfra.yaml --capabilities CAPABILITY_IAM --parameter-overrides Application=lbst ParentVPCStack=icttestawsvpc BranchName=${BranchName} --stack-name ${StackName}
#ProjectName=$(aws cloudformation describe-stacks --stack-name ${StackName} --query 'Stacks[0].Outputs[?OutputKey==`ProjectName`].OutputValue' --output text)
#aws codebuild create-webhook \
#    --project-name ${ProjectName} \
#    --filter-groups "[[{\"type\":\"EVENT\",\"pattern\":\"PUSH\"},{\"type\":\"HEAD_REF\",\"pattern\":\"^refs/heads/${BranchName}$\",\"excludeMatchedPattern\":false}]]"
