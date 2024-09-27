#!/usr/bin/env bash

mkdir kmsEncryption
privateKey=$(aws-vault exec $ACCOUNT_FROM -- aws acm export-certificate --certificate-arn $ENCRYPTION_CERT_ACM --passphrase fileb://passPhrase.txt | jq -r '"\(.PrivateKey)"')
echo "$privateKey" > ./kmsEncryption/encrypted_key.pem
openssl rsa -in ./kmsEncryption/encrypted_key.pem -out ./kmsEncryption/decrypted_key.pem
openssl pkcs8 -topk8 -inform PEM -outform DER -in ./kmsEncryption/decrypted_key.pem -out ./kmsEncryption/PlaintextKeyMaterial.der -nocrypt
mv ./kmsEncryption/PlaintextKeyMaterial.der ./kmsEncryption/PlaintextKeyMaterial.bin


kmsEncryptionKeyId=$ENC_KEY_ID
echo "KMS Encryption key Id $kmsEncryptionKeyId"
importData=$(aws-vault exec $ACCOUNT_TO -- aws kms get-parameters-for-import --key-id $kmsEncryptionKeyId --wrapping-algorithm RSA_AES_KEY_WRAP_SHA_256 --wrapping-key-spec RSA_2048 | jq .)
publicKey=$(echo $importData | jq .PublicKey | tr -d '"')
importToken=$(echo $importData | jq .ImportToken | tr -d '"')

echo "$publicKey" > ./kmsEncryption/PublicKey.b64
echo "$importToken" > ./kmsEncryption/importtoken.b64

openssl enc -d -base64 -A -in ./kmsEncryption/PublicKey.b64 -out ./kmsEncryption/WrappingPublicKey.bin
openssl enc -d -base64 -A -in ./kmsEncryption/importtoken.b64 -out ./kmsEncryption/ImportToken.bin

openssl rand -out ./kmsEncryption/aes-key.bin 32

openssl enc -id-aes256-wrap-pad \
        -K "$(xxd -p < ./kmsEncryption/aes-key.bin | tr -d '\n')" \
        -iv A65959A6 \
        -in ./kmsEncryption/PlaintextKeyMaterial.bin\
        -out ./kmsEncryption/key-material-wrapped.bin

openssl pkeyutl \
    -encrypt \
    -in ./kmsEncryption/aes-key.bin \
    -out ./kmsEncryption/aes-key-wrapped.bin \
    -inkey ./kmsEncryption/WrappingPublicKey.bin \
    -keyform DER \
    -pubin \
    -pkeyopt rsa_padding_mode:oaep \
    -pkeyopt rsa_oaep_md:sha256 \
    -pkeyopt rsa_mgf1_md:sha256

cat ./kmsEncryption/aes-key-wrapped.bin ./kmsEncryption/key-material-wrapped.bin > ./kmsEncryption/EncryptedKeyMaterial.bin

aws-vault exec $ACCOUNT_TO -- aws kms import-key-material --key-id $kmsEncryptionKeyId \
    --encrypted-key-material fileb://./kmsEncryption/EncryptedKeyMaterial.bin \
    --import-token fileb://./kmsEncryption/ImportToken.bin \
    --expiration-model KEY_MATERIAL_DOES_NOT_EXPIRE
