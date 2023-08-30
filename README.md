# Digital Identity Driving Permit Credential Issuer
Driving Permit Check Credential Issuer

## Checkout submodules
> The first time you check out or clone the repository, you will need to run the following commands:
 
`git submodule update --init --recursive`

> Subsequent times you will need to run the following commands:

`git submodule update --recursive`

### Updating submodules to the latest "main" branch
> You can also update the submodules to the latest "main" branch, but this is not done automatically 
> in case there have been changes made to the shared libraries you do not yet want to track

cd into each submodule (folders are `/common-lib` and `/common-lambdas`) and run the following commands:

`git checkout main && git pull`

## Build

Build with `./gradlew`

## Deploy

### Prerequisites

See onboarding guide for instructions on how to setup the following command line interfaces (CLI)
- aws cli
- aws-vault
- sam cli
- gds cli

### Deploy to DL dev account

Note deploy.sh has a prefix override set to use the existing dev pipeline parameters and should
not need parameters set unless new ones are being added.

`gds aws gds aws di-ipv-cri-dl-dev-admin -- ./deploy.sh di-ipv-cri-dl-myusernameORticket`

### Delete stack from DL dev account
> The stack name *must* be unique to you and created by you in the deploy stage above.
> Type `y`es when prompted to delete the stack and the folders in S3 bucket

The command to run is:

`gds aws di-ipv-cri-dl-dev-admin -- sam delete --config-env dev --stack-name di-ipv-cri-dl-myusernameORticket`

### Deploy to shared dev account

Note deploy.sh has a prefix override set to use the existing dl-dev pipeline parameters, the prefix will need changed 
to "none" and all parameters set in the parameter store.

`gds aws di-ipv-cri-dev -- ./deploy.sh di-ipv-cri-dl-myusernameORticket`

### Delete stack from shared dev account
> The stack name *must* be unique to you and created by you in the deploy stage above.
> Type `y`es when prompted to delete the stack and the folders in S3 bucket

The command to run is:

`gds aws ROLE -- sam delete --config-env dev --stack-name <unique-stack-name>`

### Parameter prefix

This allows a deploying stack to use parameters of another stack.
Created to enable pre-merge integration tests to use the parameters of the pipeline stack.

ParameterPrefix if set, this value is used in place of AWS::Stackname for parameter store paths.
- Default is "none", which will use AWS::StackName as the prefix.

Can also be used with the following limitations in development.
- Existing stack needs to have all the parameters needed for the stack with the prefix enabled.
- Existing stack parameters values if changed will trigger behaviour changes in the stack with the prefix enabled.
- Existing stack if deleted will cause errors in the deployed stack.

## Acceptance Test

In a terminal, change into the acceptance test folder and execute

`./run-local-tests`

Then follow the on-screen prompts.

### Note

To run API tests locally you need
- A manually deployed api stack with the code (main or branch) that is to be tested.
- A locally running core-stub with a cri id configured for `driving-licence-cri-dev`.

Acceptance tests
- build/staging test can be run against the environments.
- Using a locally running stub and front, with a manually deployed api stack configured to match the environments.

