#!/usr/bin/env bash
set -e

stack_name="$1"

if [ -z "$stack_name" ]
then
echo -e "Stack name expected as first argument, e.g. ./deploy.sh my-driving-permit-api"
exit 1
fi

./gradlew

sam validate -t infrastructure/lambda/template.yaml --config-env dev

sam build -t infrastructure/lambda/template.yaml --config-env dev

sam deploy --stack-name $stack_name \
   --no-fail-on-empty-changeset \
   --no-confirm-changeset \
   --resolve-s3 \
   --region eu-west-2 \
   --capabilities CAPABILITY_IAM \
   --parameter-overrides Environment=dev
