#!/bin/sh

gradleConfigurationFile=app/build.gradle.kts
versionCodeField="versionCode"

# Getting the version code
grep -n $versionCodeField $gradleConfigurationFile | cut -d: -f2
string=$(grep -n $versionCodeField $gradleConfigurationFile | cut -d: -f2)

# Incrementing the version
versionCode=$(echo $string | cut -d= -f2 | sed "s/$versionCodeField//" | tr -d ' ')
newVersionCode=$(( versionCode + 1 ))

# Replacing the previous version
echo "Will replace previous version $versionCode by new version $newVersionCode in $gradleConfigurationFile"
sed -i'.bak' "s/\($versionCodeField \)\(.*\)/$versionCodeField = $newVersionCode/" $gradleConfigurationFile
rm ${gradleConfigurationFile}.bak # remove backup file