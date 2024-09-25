#!/usr/bin/env bash

mkdir backup

STACK_NAME=$STACK_NAME
ACCOUNT_TO=$ACCOUNT_TO
ACCOUNT_FROM=$ACCOUNT_FROM

aws-vault exec ${ACCOUNT_FROM} -- aws ssm get-parameters-by-path --with-decryption --path "/${STACK_NAME}/" | jq . > ./backup/params.json

###### Load TlS Certs #######
tlsCert=$(aws-vault exec ${ACCOUNT_FROM} -- aws acm export-certificate --certificate-arn $TLS_CERT_ACM --passphrase fileb://passPhrase.txt | jq -r '"\(.Certificate)"')

aws-vault exec ${ACCOUNT_FROM} -- aws acm export-certificate --certificate-arn $TLS_CERT_ACM --passphrase fileb://passPhrase.txt | jq -r '"\(.PrivateKey)"' > tlsPrivateKey.pem # pragma: allowlist secret
openssl rsa -in tlsPrivateKey.pem -out decryptedtlsPrivateKey.pem -passin file:passPhrase.txt # pragma: allowlist secret
tlsKey=`cat decryptedtlsPrivateKey.pem` # pragma: allowlist secret

tlsKey=$(echo "$tlsKey" |tr -d "\n\r")
tlsKey=$(echo "${tlsKey//-----BEGIN PRIVATE KEY-----/}") # pragma: allowlist secret
tlsKey=$(echo "${tlsKey//-----END PRIVATE KEY-----/}") # pragma: allowlist secret
tlsKey=$(echo "$tlsKey" | tr -d ' ')

tlsCert=$(echo "$tlsCert" |tr -d "\n\r")
tlsCert=$(echo "${tlsCert//-----BEGIN CERTIFICATE-----/}")
tlsCert=$(echo "${tlsCert//-----END CERTIFICATE-----/}")
tlsCert=$(echo "$tlsCert" | sed 's/ //g')

aws-vault exec ${ACCOUNT_TO} -- aws ssm put-parameter --overwrite --name "/${STACK_NAME}/DVA/HttpClient/tlsCert-03-07-2024" --type "String" --value $tlsCert 2>&1 > /dev/null
aws-vault exec ${ACCOUNT_TO} -- aws ssm put-parameter --overwrite --name "/${STACK_NAME}/DVA/HttpClient/tlsKey-03-07-2024" --type "SecureString" --value $tlsKey 2>&1 > /dev/null

#####################################

###### Load Signing Certs #######
signingCert=$(aws-vault exec ${ACCOUNT_FROM} -- aws acm export-certificate --certificate-arn $SIGNING_CERT_ACM --passphrase fileb://passPhrase.txt | jq -r '"\(.Certificate)"')

aws-vault exec ${ACCOUNT_FROM} -- aws acm export-certificate --certificate-arn $SIGNING_CERT_ACM --passphrase fileb://passPhrase.txt | jq -r '"\(.PrivateKey)"' > signingPrivateKey.pem # pragma: allowlist secret
openssl rsa -in signingPrivateKey.pem -out decryptedSigningPrivateKey.pem -passin file:passPhrase.txt # pragma: allowlist secret
signingPrivateKey=`cat decryptedSigningPrivateKey.pem` # pragma: allowlist secret

signingPrivateKey=$(echo "$signingPrivateKey" |tr -d "\n\r") # pragma: allowlist secret
signingPrivateKey=$(echo "${signingPrivateKey//-----BEGIN PRIVATE KEY-----/}") # pragma: allowlist secret
signingPrivateKey=$(echo "${signingPrivateKey//-----END PRIVATE KEY-----/}") # pragma: allowlist secret
signingPrivateKey=$(echo "$signingPrivateKey" | tr -d ' ') # pragma: allowlist secret

signingCert=$(echo "$signingCert" |tr -d "\n\r")
signingCert=$(echo "${signingCert//-----BEGIN CERTIFICATE-----/}")
signingCert=$(echo "${signingCert//-----END CERTIFICATE-----/}")
signingCert=$(echo "$signingCert" | sed 's/ //g')

aws-vault exec ${ACCOUNT_TO} -- aws ssm put-parameter --overwrite --name "/${STACK_NAME}/DVA/JWS/signingCertForDvaToVerify-03-07-2024" --type "String" --value $signingCert 2>&1 > /dev/null
aws-vault exec ${ACCOUNT_TO} -- aws ssm put-parameter --overwrite --name "/${STACK_NAME}/DVA/JWS/signingKeyForDrivingPermitToSign-03-07-2024" --type "SecureString" --value $signingPrivateKey 2>&1 > /dev/null # pragma: allowlist secret
#####################################

###### Load Encryption Certs #######
aws-vault exec ${ACCOUNT_FROM} -- aws acm export-certificate --certificate-arn $ENCRYPTION_CERT_ACM --passphrase fileb://passPhrase.txt | jq -r '"\(.PrivateKey)"' > encryptionPrivateKey.pem # pragma: allowlist secret
openssl rsa -in encryptionPrivateKey.pem -out decryptedEncryptionPrivateKey.pem -passin file:passPhrase.txt # pragma: allowlist secret
encryptionPrivateKey=`cat decryptedEncryptionPrivateKey.pem` # pragma: allowlist secret

encryptionPrivateKey=$(echo "$encryptionPrivateKey" |tr -d "\n\r") # pragma: allowlist secret
encryptionPrivateKey=$(echo "${encryptionPrivateKey//-----BEGIN PRIVATE KEY-----/}") # pragma: allowlist secret
encryptionPrivateKey=$(echo "${encryptionPrivateKey//-----END PRIVATE KEY-----/}") # pragma: allowlist secret
encryptionPrivateKey=$(echo "$encryptionPrivateKey" | tr -d ' ') # pragma: allowlist secret

aws-vault exec ${ACCOUNT_TO} -- aws ssm put-parameter --overwrite --name "/${STACK_NAME}/DVA/JWE/encryptionKeyForDrivingPermitToDecrypt-03-07-2024" --type "SecureString" --value $encryptionPrivateKey 2>&1 > /dev/null # pragma: allowlist secret
#####################################
