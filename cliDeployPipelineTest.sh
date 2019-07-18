#!/bin/bash -e
BranchName=test
StackName=lbstCodeBuild${BranchName}
Application=lbst
ParentVPCStack=icttestawsvpc
Environment=P
aws cloudformation deploy --template-file lbstInfra.yaml --capabilities CAPABILITY_IAM --parameter-overrides Environment=${Environment} Application=${Application} ParentVPCStack=${ParentVPCStack} BranchName=${BranchName} --stack-name ${StackName}
#ProjectName=$(aws cloudformation describe-stacks --stack-name ${StackName} --query 'Stacks[0].Outputs[?OutputKey==`ProjectName`].OutputValue' --output text)
#aws codebuild create-webhook \
#    --project-name ${ProjectName} \
#    --filter-groups "[[{\"type\":\"EVENT\",\"pattern\":\"PUSH\"},{\"type\":\"HEAD_REF\",\"pattern\":\"^refs/heads/${BranchName}$\",\"excludeMatchedPattern\":false}]]"
