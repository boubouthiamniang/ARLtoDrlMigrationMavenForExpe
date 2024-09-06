$sourceDir = "Path"
$destinationDir = "Path" 
if (-not (Test-Path $destinationDir)) {
    New-Item -ItemType Directory -Path $destinationDir
}

# Copy all .java files from source to destination, maintaining the directory structure
Get-ChildItem -Path $sourceDir -Recurse -Filter *.java | ForEach-Object {
    #$relativePath = $_.FullName.Substring($sourceDir.Length)    #Twoline below commented because we do not want to keep path structure 
    #$destinationPath = Join-Path $destinationDir $relativePath  
    $destinationPath = Join-Path $destinationDir $_.Name
    
    # Create the directory structure in the destination
    $destinationDirPath = Split-Path $destinationPath
    if (-not (Test-Path $destinationDirPath)) {
        New-Item -ItemType Directory -Path $destinationDirPath -Force
    }
    
    # Copy the file
    Copy-Item -Path $_.FullName -Destination $destinationPath -Force
}

Write-Output "Java POJO classes copied successfully."
