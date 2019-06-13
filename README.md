# LabStats Sync

LabStats Sync contains two AWS Lambdas to collect and get Room Station data from the [LabStats API](https://sea-api.labstats.com/index.html). 

## Services

- [DynamoDB](https://aws.amazon.com/dynamodb/) - Stores RoomStation data. [Schema](https://sydneyuni.atlassian.net/wiki/spaces/SUMA/pages/803110938/Find+a+PC+Integration)

## Testing

LabStats is unit-tested and CI for 100% of its classes. LabStats Sync uses LocalStack to ensure the Lambda DynamoDB calls work, but currently is not integrated with Bitbucket Pipelines.

For testing AWS Lambda locally, you can install [SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html) to run your serverless locally.

```
sam local start-api
open http://127.0.0.1:3000/room-stations
```

For testing Cloudformation locally, run the script [cliDeployCoders.sh](https://bitbucket.org/sydneyuni/labstats-sync/raw/master/cliDeployCoders.sh).

## Deploying

LabStats Sync uses [Cloudformation](https://aws.amazon.com/cloudformation/) and [SAM](https://aws.amazon.com/serverless/sam/) to automatically deploy Lambda and our required Setup in AWS.

CD is enabled for the Coders2 AWS Account.