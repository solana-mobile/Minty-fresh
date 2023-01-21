#!/bin/sh

newVersionName=$1
if [ -z "$newVersionName" ]
  then
    echo "No newVersionName argument supplied, it is required"
    exit 1
fi

gradleConfigurationFile=app/build.gradle.kts
versionNameField="versionName"

# Getting the version name
grep -n $versionNameField $gradleConfigurationFile | cut -d: -f2
string=$(grep $versionNameField $gradleConfigurationFile | awk -F[\'\'] '{print $2}' | tr -d \''"\')
previousVersionName=$(echo "$string" | cut -d= -f2 | tr -d \''"\')

# Replacing the previous version
echo "Will replace previous version $previousVersionName by new version $newVersionName in $gradleConfigurationFile"
sed -i'.bak' "s/\($versionNameField \)\(.*\)/$versionNameField = \"$newVersionName\"/" $gradleConfigurationFile
rm ${gradleConfigurationFile}.bak # remove backup file