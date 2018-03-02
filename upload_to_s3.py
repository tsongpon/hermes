# Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
# except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS"
# BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under the License.
"""
A Bitbucket Builds template for deploying
an application to AWS Elastic Beanstalk
joshcb@amazon.com
v1.0.0
"""
from __future__ import print_function
import os
import sys
import boto3
from botocore.exceptions import ClientError

BASE_BUCKET_KEY = os.getenv('APPLICATION_NAME') + '/' + os.getenv('APPLICATION_NAME')

def upload_to_s3(artifact):
    """
    Uploads an artifact to Amazon S3
    """
    with open('version.txt', 'r') as myfile:
        version_number=myfile.read().replace('\n', '')
    try:
        bucket_key = BASE_BUCKET_KEY + '-' + version_number + '.zip'
        print("Upload hermes version : ", bucket_key)
        client = boto3.client('s3')
    except ClientError as err:
        print("Failed to create boto3 client.\n" + str(err))
        return False

    try:
        client.put_object(
            Body=open(artifact, 'rb'),
            Bucket=os.getenv('S3_BUCKET'),
            Key=bucket_key
        )
    except ClientError as err:
        print("Failed to upload artifact to S3.\n" + str(err))
        return False
    except IOError as err:
        print("Failed to access artifact.zip in this directory.\n" + str(err))
        return False

    return True

def main():
    " Your favorite wrapper's favorite wrapper "
    if not upload_to_s3('/tmp/artifact.zip'):
        sys.exit(1)


if __name__ == "__main__":
    main()