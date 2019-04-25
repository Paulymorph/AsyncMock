#!/usr/bin/env bash

swagger_destination=$1
index_file=${swagger_destination}/index.html

echo "Checking if $index_file exists"
if [[ -f "$index_file" ]]
then
	echo "Skipping swagger download as it is present."
else
    echo "Creating temporary folder"
    mkdir tmp

    downloaded_file=tmp/archive.zip
    swagger_url=https://github.com/swagger-api/swagger-ui/archive/master.zip
    echo "Downloading Swagger UI archive at $swagger_url to downloaded_file"
    curl -L ${swagger_url} -o ${downloaded_file}

    unarchived_folder=tmp/unarchived
    mkdir ${unarchived_folder}
    echo "Unzipping $downloaded_file to $unarchived_folder"
    unzip ${downloaded_file} -d ${unarchived_folder} >/dev/null

    copy_source=${unarchived_folder}/swagger-ui-master/dist/*
    copy_dest=${swagger_destination}/
    echo "Copying contents of $copy_source to $copy_dest"
    cp -rf ${copy_source} ${copy_dest}

    echo "Replacing petstore swagger on swagger.yaml"
    sed 's|https://petstore.swagger.io/v2/swagger.json|swagger.yaml|' ${index_file} >${index_file}.sed
    mv ${index_file}.sed ${index_file}

    echo "Cleaning temporary folder"
    rm -rf tmp
fi
