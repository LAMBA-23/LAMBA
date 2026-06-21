$templatePath = Join-Path $PSScriptRoot "user-story.yml"
$content = Get-Content -Raw $templatePath

$expected = @(
    "To Do"
    "Ready"
    "In Progress"
    "Review"
    "Done"
)

$unexpected = @(
    "In Review"
    "Blocked"
)

$missing = $expected | Where-Object { $content -notmatch [regex]::Escape("- $_") }
$presentUnexpected = $unexpected | Where-Object { $content -match [regex]::Escape("- $_") }

if ($missing.Count -gt 0 -or $presentUnexpected.Count -gt 0) {
    if ($missing.Count -gt 0) {
        Write-Error ("Missing expected Work Status values: " + ($missing -join ", "))
    }

    if ($presentUnexpected.Count -gt 0) {
        Write-Error ("Found unsupported Work Status values: " + ($presentUnexpected -join ", "))
    }

    exit 1
}

Write-Output "User Story Work Status values match Process Requirements."
