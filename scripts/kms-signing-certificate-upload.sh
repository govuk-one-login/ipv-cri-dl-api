#!/usr/bin/env bash

mkdir kmsSigning
privateKey=$(aws-vault exec $ACCOUNT_FROM -- aws acm export-certificate --certificate-arn $SIGNING_CERT_ACM --passphrase fileb://passPhrase.txt | jq -r '"\(.PrivateKey)"')
echo "$privateKey" > ./kmsSigning/encrypted_key.pem
openssl rsa -in ./kmsSigning/encrypted_key.pem -out ./kmsSigning/decrypted_key.pem
openssl pkcs8 -topk8 -inform PEM -outform DER -in ./kmsSigning/decrypted_key.pem -out ./kmsSigning/PlaintextKeyMaterial.der -nocrypt
mv ./kmsSigning/PlaintextKeyMaterial.der ./kmsSigning/PlaintextKeyMaterial.bin


kmsSigningKeyId=$SIGNING_KEY_ID
echo "KMS Signing key Id $kmsSigningKeyId"
importData=$(aws-vault exec $ACCOUNT_TO -- aws kms get-parameters-for-import --key-id $kmsSigningKeyId --wrapping-algorithm RSA_AES_KEY_WRAP_SHA_256 --wrapping-key-spec RSA_2048 | jq .)
publicKey=$(echo $importData | jq .PublicKey | tr -d '"')
importToken=$(echo $importData | jq .ImportToken | tr -d '"')

echo "$publicKey" > ./kmsSigning/PublicKey.b64
echo "$importToken" > ./kmsSigning/importtoken.b64

openssl enc -d -base64 -A -in ./kmsSigning/PublicKey.b64 -out ./kmsSigning/WrappingPublicKey.bin
openssl enc -d -base64 -A -in ./kmsSigning/importtoken.b64 -out ./kmsSigning/ImportToken.bin

openssl rand -out ./kmsSigning/aes-key.bin 32

openssl enc -id-aes256-wrap-pad \
        -K "$(xxd -p < ./kmsSigning/aes-key.bin | tr -d '\n')" \
        -iv A65959A6 \
        -in ./kmsSigning/PlaintextKeyMaterial.bin\
        -out ./kmsSigning/key-material-wrapped.bin

openssl pkeyutl \
    -encrypt \
    -in ./kmsSigning/aes-key.bin \
    -out ./kmsSigning/aes-key-wrapped.bin \
    -inkey ./kmsSigning/WrappingPublicKey.bin \
    -keyform DER \
    -pubin \
    -pkeyopt rsa_padding_mode:oaep \
    -pkeyopt rsa_oaep_md:sha256 \
    -pkeyopt rsa_mgf1_md:sha256

cat ./kmsSigning/aes-key-wrapped.bin ./kmsSigning/key-material-wrapped.bin > ./kmsSigning/EncryptedKeyMaterial.bin

aws-vault exec $ACCOUNT_TO -- aws kms import-key-material --key-id $kmsSigningKeyId \
    --encrypted-key-material fileb://./kmsSigning/EncryptedKeyMaterial.bin \
    --import-token fileb://./kmsSigning/ImportToken.bin \
    --expiration-model KEY_MATERIAL_DOES_NOT_EXPIRE
