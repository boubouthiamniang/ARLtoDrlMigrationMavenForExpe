# Set the target directory
$targetDir = "Path"

# Set the groupId and artifactId
$groupId = "com.bl.drools.demo"
$artifactId = "workshop-demo"

# Navigate to the target directory
Set-Location -Path $targetDir

# Run the Maven archetype generate command
$mavenCommand = "mvn archetype:generate -B -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.1 -DgroupId=$groupId -DartifactId=$artifactId -DinteractiveMode=false -Dversion=1.0-SNAPSHOT -Dpackage=com.bl.drools.demo.project"
Invoke-Expression $mavenCommand
