#!/usr/bin/env bash
set -e

CURRENT_PATH="${PWD}"

RED="\033[1;31m"
GREEN="\033[1;32m"
NOCOLOR="\033[0m"

stack_name="$1"
audit_event_name_prefix="$2"
cri_identifier="$3"

if [ -z "$stack_name" ]; then
  echo -e "😱 ${RED}stack name expected as first argument, e.g. ${GREEN}./deploy.sh my-di-ipv-cri-dl-api${NOCOLOR}"
  exit 1
fi

if [ -z "$audit_event_name_prefix" ]; then
  audit_event_name_prefix="/common-cri-parameters/DrivingPermitAuditEventNamePrefix"
fi

if [ -z "$cri_identifier" ]; then
  cri_identifier="/common-cri-parameters/DrivingPermitCriIdentifier"
fi

echo -e "👉 deploying di-ipv-cri-dl-api with:"
echo -e "\tstack name: ${GREEN}$stack_name${NOCOLOR}"
echo -e "\tAuditEventNamePrefix SSM key ${GREEN}$audit_event_name_prefix${NOCOLOR}"
echo -e "\tCriIdentifier SSM key ${GREEN}$cri_identifier${NOCOLOR}"

echo -e "🧹 Starting a clean build"
./gradlew clean
echo -e "🔎 Checking with cfn-lint"
cfn-lint infrastructure/lambda/template.yaml -f pretty
echo -e "🔎 Checking with sam validate --lint"
sam validate -t infrastructure/lambda/template.yaml --config-env dev --lint
echo -e "🧱 Building with sam build"
sam build -s "$CURRENT_PATH" -t infrastructure/lambda/template.yaml --config-env dev
echo -e "🚀 Deploying..."
sam validate -t infrastructure/lambda/template.yaml --config-env dev --lint
sam build -s "$CURRENT_PATH" -t infrastructure/lambda/template.yaml --config-env dev
sam deploy --stack-name "$stack_name" \
  --no-fail-on-empty-changeset \
  --no-confirm-changeset \
  --resolve-s3 \
  --region eu-west-2 \
  --capabilities CAPABILITY_IAM \
  --tags \
  cri:component=ipv-cri-dl-api \
  cri:stack-type=dev \
  cri:application=Lime \
  cri:deployment-source=manual \
  --parameter-overrides \
  CodeSigningEnabled=false \
  Environment=dev \
  AuditEventNamePrefix=$audit_event_name_prefix \
  CommonStackName=driving-permit-common-cri-api-local \
  CriIdentifier=$cri_identifier \
  CreateMockTxmaResourcesOverride=true \
  ParameterPrefix="dl-cri-api-v1" \
  DeploymentType="not-pipeline" \
  LambdaDeploymentPreference="AllAtOnce"
