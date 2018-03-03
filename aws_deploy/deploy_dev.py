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
from time import strftime, sleep
import boto3
from botocore.exceptions import ClientError

BASE_BUCKET_KEY = os.getenv('APPLICATION_NAME') + '/' + os.getenv('APPLICATION_NAME')

def deploy_new_version():
    """
    Deploy a new version to AWS Elastic Beanstalk
    """
    try:
        client = boto3.client('elasticbeanstalk')
    except ClientError as err:
        print("Failed to create boto3 client.\n" + str(err))
        return False

    try:
        with open('version.txt', 'r') as myfile:
            version_number=myfile.read().replace('\n', '')
        version_label = 'hermes-' + version_number
        print('Deploying new application version : ', version_label)

        response = client.update_environment(
            ApplicationName=os.getenv('APPLICATION_NAME'),
            EnvironmentName='hermes-dev',
            VersionLabel=version_label,
        )
    except ClientError as err:
        print("Failed to update environment.\n" + str(err))
        return False

    print(response)
    return True

def main():
    " Your favorite wrapper's favorite wrapper "
    sleep(5)
    if not deploy_new_version():
        sys.exit(1)


if __name__ == "__main__":
    main()