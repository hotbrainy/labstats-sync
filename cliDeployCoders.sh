#codebucket=$(aws cloudformation describe-stacks --stack-name coreLambdaCode --query 'Stacks[0].Outputs[?OutputKey==`S3Bucket`].OutputValue' --output text)
CODERS_AWS_BUCKET="cf-templates-u6grlzjqw4qg-ap-southeast-2"
sam build -b ./build -t ./template.yaml
sam package --template-file build/template.yaml --output-template-file packaged.yaml --s3-bucket $CODERS_AWS_BUCKET
rm -fr ./build
sam deploy --template-file packaged.yaml --parameter-overrides Environment=C Application=lbst LabStatsAPIKey=$LABSTATS_API_KEY --capabilities CAPABILITY_IAM --stack-name LabStatSync
rm packaged.yaml