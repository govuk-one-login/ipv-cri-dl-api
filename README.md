# Digital Identity Driving Permit Credential Issuer
Driving Permit Check Credential Issuer

## SDKMan
This project has an `.sdkmanrc` file

Install SDKMan via the instructions on `https://sdkman.io/install`

For auto-switching between JDK versions, edit your `~/.sdkman/etc/config` and set `sdkman_auto_env=true`

Then use sdkman to install Java JDK listed in this projects `.sdkmanrc`
e.g `sdk install java x.y.z-amzn`

Restart your terminal

## Gradle

Gradle 8 is used on this project

## Build

Build with `./gradlew`

By default, this also calls spotlessApply and runs unit tests

## Linting

Check with `./gradlew :spotlessCheck`

Apply with `./gradlew :spotlessApply`

## Coverage Reports

Generate with `./gradlew reports` placed in `build/reports`

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

## TestData Strategy

For testing purposes, this CRI has the ability to route users requests to either
a real 3rd Party UAT instance of the service OR route users requests to an internally
managed, stubbed version of the 3rd party service.

Routing for the above is dictated by the client ID sent to the CRI from IPVCore/stubs. For lower
environments there is an IPV core stub that is configured to for routing CRIs to 3rd party stubs and another
IPV core stub that is configured for routing CRIs to the 3rd party UAT environment.

For testing purposes if you wish to route to the stubbed version of the 3rd party then use the following
core stub URL - https://cri.core.stubs.account.gov.uk/
If you wish to route to the real 3rd partys UAT instance of the service use the following
core stub url - https://cri-3rdparty.core.stubs.account.gov.uk/

Additional details on these stubs can be found on this confluence page -
https://govukverify.atlassian.net/wiki/spaces/OJ/pages/3147333723/Stubs+for+testing+journeys