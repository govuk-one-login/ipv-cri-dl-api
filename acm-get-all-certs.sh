#!/usr/bin/env bash

mkdir certs
signingCert=$(aws-vault exec dl-dev -- aws acm export-certificate --certificate-arn $SIGNING_CERT_ACM --passphrase fileb://passPhrase.txt | jq -r '"\(.Certificate)"')
echo "$signingCert" > ./certs/signingCert.crt

encryptionCert=$(aws-vault exec dl-dev -- aws acm export-certificate --certificate-arn $ENCRYPTION_CERT_ACM --passphrase fileb://passPhrase.txt | jq -r '"\(.Certificate)"')
echo "$encryptionCert" > ./certs/encryptionCert.crt

tlsCert=$(aws-vault exec dl-dev -- aws acm export-certificate --certificate-arn $TLS_CERT_ACM --passphrase fileb://passPhrase.txt | jq -r '"\(.Certificate)"')
echo "$tlsCert" > ./certs/tlsCert.crt

rootCert=$(aws-vault exec dl-dev -- aws acm-pca get-certificate-authority-certificate --certificate-authority-arn $ROOT_CERT_ACM | jq -r '"\(.Certificate)"')
echo "$rootCert" > ./certs/rootCert.crt
