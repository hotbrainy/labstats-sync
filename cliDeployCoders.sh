#codebucket=$(aws cloudformation describe-stacks --stack-name coreLambdaCode --query 'Stacks[0].Outputs[?OutputKey==`S3Bucket`].OutputValue' --output text)
codebucket="cf-templates-u6grlzjqw4qg-ap-southeast-2"
sam build -b ./build --use-container -t labstats.yaml
sam package --template-file build/template.yaml --output-template-file packaged.yaml --s3-bucket $codebucket
rm -fr ./build
sam deploy --template-file packaged.yaml --parameter-overrides Lifecycle=Development Environment=D Application=lbst --capabilities CAPABILITY_IAM --stack-name lbstSyncFunc