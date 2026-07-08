# di-ipv-cri-dl-test

This folder has been created as a central location for any work related to Driving Licence CRI testing.

## Build

Build with `./gradlew`

## Run tests

For first-time setup, run `./run-local-tests.sh` which will guide you through configuration and save your answers to `test-args.conf`.

To run tests directly: `./gradlew cucumber -P tags=@dl_CRI`

### Environment variables

|         Variable         | Required |                                       Description                                       |
|--------------------------|----------|-----------------------------------------------------------------------------------------|
| `ENVIRONMENT`            | Yes      | Target environment e.g. `dev`                                                           |
| `AWS_STACK_NAME`         | No       | CloudFormation stack name. If not set, PII log scanning is skipped                      |
| `ECS_LOG_GROUP_NAME`     | No       | ECS CloudWatch log group name for PII scanning. If not set, ECS log scanning is skipped |
| `BROWSER`                | Yes      | Browser to use e.g. `chrome-headless`                                                   |
| `LOCAL`                  | Yes      | Set to `yes` if using a local stub                                                      |
| `E2E`                    | Yes      | Set to `yes` to include E2E tests                                                       |
| `BACKEND`                | Yes      | Set to `yes` to include backend tests                                                   |
| `TAG`                    | Yes      | Cucumber tag to run e.g. `@test`                                                        |
| `CORE_STUB_URL`          | No       | Stub URL (without protocol)                                                             |
| `CORE_STUB_USERNAME`     | No       | Stub username                                                                           |
| `CORE_STUB_PASSWORD`     | No       | Stub password                                                                           |
| `ORCHESTRATOR_URL`       | No       | Required when `E2E=yes`                                                                 |
| `API_GATEWAY_ID_PRIVATE` | No       | Required when `BACKEND=yes`                                                             |
| `API_GATEWAY_ID_PUBLIC`  | No       | Required when `BACKEND=yes`                                                             |

Speak to a member of the test team for credential values. When running in the pipeline these will be taken from AWS.
