#!/usr/bin/env bash
url=https://github.com/swagger-api/swagger-ui/archive/master.zip
mkdir tmp
cd tmp
downloaded_file=archive.zip
swagger_destination=$1
mkdir ${swagger_destination}
curl -L ${url} -o ${downloaded_file}
unarchived_folder=unarchived
mkdir ${unarchived_folder}
unzip ${downloaded_file} -d ${unarchived_folder} >/dev/null
cp -rf ${unarchived_folder}/swagger-ui-master/dist/ ../${swagger_destination}/
index_file=../${swagger_destination}/index.html
sed 's|https://petstore.swagger.io/v2/swagger.json|swagger.yaml|' ${index_file} >${index_file}.sed
mv ${index_file}.sed ${index_file}
cd ..
rm -rf tmp
