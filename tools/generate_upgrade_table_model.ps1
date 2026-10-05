$modelPath = Join-Path $PSScriptRoot '..\src\main\resources\assets\techgunsupgrade\models\custom\upgrade_table.json'
$modelPath = [System.IO.Path]::GetFullPath($modelPath)

function New-Cube {
    param(
        [string] $Name,
        [double[]] $From,
        [double[]] $To,
        [string] $Texture,
        $Rotation = $null,
        [string] $FrontTexture = $null,
        [switch] $NoShade
    )

    $faces = [ordered]@{}
    foreach ($face in @('north', 'east', 'south', 'west', 'up', 'down')) {
        # Build UVs from the size of the cuboid rather than its absolute model
        # coordinates.  The station reaches Y=24 and the lever reaches X=19.6;
        # vanilla's automatic UVs wrap outside 0..16 for such elements.
        switch ($face) {
            { $_ -in @('north', 'south') } { $uv = @(0, 0, ($To[0] - $From[0]), ($To[1] - $From[1])); break }
            { $_ -in @('east', 'west') } { $uv = @(0, 0, ($To[2] - $From[2]), ($To[1] - $From[1])); break }
            default { $uv = @(0, 0, ($To[0] - $From[0]), ($To[2] - $From[2])) }
        }

        $faceTexture = $Texture
        if ($face -eq 'south' -and -not [string]::IsNullOrEmpty($FrontTexture)) {
            $faceTexture = $FrontTexture
            # Screens, signs and dials are authored as complete front panels.
            $uv = @(0, 0, 16, 16)
        }
        $faces[$face] = [ordered]@{ texture = "#$faceTexture"; uv = $uv }
    }

    $element = [ordered]@{
        name = $Name
        from = $From
        to = $To
        faces = $faces
    }
    if ($null -ne $Rotation) { $element.rotation = $Rotation }
    if ($NoShade) { $element.shade = $false }
    return $element
}

function New-GlassPane {
    $faces = [ordered]@{
        north = [ordered]@{ texture = '#glass'; uv = @(0, 0, 16, 16) }
        south = [ordered]@{ texture = '#glass'; uv = @(0, 0, 16, 16) }
    }
    return [ordered]@{
        name = 'reinforced glass'
        from = @(2.7, 7.0, 15.30)
        to = @(13.3, 17.3, 15.42)
        shade = $false
        faces = $faces
    }
}

$elements = @(
    (New-Cube 'main base' @(0, 2, 0) @(16, 6, 16) 'dark'),
    (New-Cube 'base armor' @(1, 3, 14.8) @(15, 5.8, 16) 'steel'),
    (New-Cube 'diagnostic screen' @(4, 3.4, 15.7) @(11, 5.3, 16.15) -Texture 'dark' -FrontTexture 'screen' -NoShade),
    (New-Cube 'amber status lamp' @(12.2, 3.5, 15.72) @(14.2, 5.2, 16.16) -Texture 'dark' -FrontTexture 'red' -NoShade),

    (New-Cube 'front left foot' @(0.8, 0, 10.5) @(4.2, 2, 15.2) 'steel'),
    (New-Cube 'front right foot' @(11.8, 0, 10.5) @(15.2, 2, 15.2) 'steel'),
    (New-Cube 'rear left foot' @(0.8, 0, 0.8) @(4.2, 2, 5.5) 'steel'),
    (New-Cube 'rear right foot' @(11.8, 0, 0.8) @(15.2, 2, 5.5) 'steel'),
    (New-Cube 'front warning rail' @(2, 2.0, 15.72) @(14, 3.0, 16.18) -Texture 'dark' -FrontTexture 'hazard'),

    (New-Cube 'chamber back' @(1.2, 5.8, 0.4) @(14.8, 18.8, 2.0) 'dark'),
    (New-Cube 'left chamber pillar' @(0, 5.5, 1.0) @(3.0, 19.2, 16) 'steel'),
    (New-Cube 'right chamber pillar' @(13.0, 5.5, 1.0) @(16, 19.2, 16) 'steel'),
    (New-Cube 'chamber floor' @(2.2, 5.7, 1.5) @(13.8, 7.2, 16) 'dark'),
    (New-Cube 'chamber ceiling' @(2.2, 17.0, 1.5) @(13.8, 19.3, 16) 'steel'),

    (New-Cube 'upper housing' @(0, 19.0, 0) @(16, 22.2, 16) 'steel'),
    (New-Cube 'upper power module' @(2.2, 22.0, 2.2) @(10.2, 24.0, 13.8) 'dark'),
    (New-Cube 'upper coil window' @(3.0, 22.1, 13.7) @(9.3, 23.8, 16.0) -Texture 'dark' -FrontTexture 'glow' -NoShade),
    (New-Cube 'upgrade sign' @(2.4, 18.8, 15.45) @(10.1, 20.7, 16.15) -Texture 'dark' -FrontTexture 'sign' -NoShade),
    (New-Cube 'gauge panel' @(10.3, 19.0, 15.42) @(14.5, 21.2, 16.12) -Texture 'steel' -FrontTexture 'gauges'),

    (New-Cube 'left energy rail' @(2.35, 7.1, 14.6) @(3.20, 17.2, 15.48) -Texture 'dark' -FrontTexture 'glow' -NoShade),
    (New-Cube 'right energy rail' @(12.80, 7.1, 14.6) @(13.65, 17.2, 15.48) -Texture 'dark' -FrontTexture 'glow' -NoShade),
    (New-Cube 'top energy rail' @(3.1, 16.35, 14.6) @(12.9, 17.2, 15.48) -Texture 'dark' -FrontTexture 'glow' -NoShade),
    (New-GlassPane),

    (New-Cube 'left clamp foot' @(3.0, 7.0, 7.0) @(5.1, 8.0, 12.6) 'steel'),
    (New-Cube 'left clamp post' @(3.4, 7.8, 8.0) @(4.8, 12.0, 11.5) 'dark'),
    (New-Cube 'left clamp jaw' @(3.0, 10.8, 8.7) @(5.4, 12.2, 11.2) 'steel'),
    (New-Cube 'right clamp foot' @(10.9, 7.0, 7.0) @(13.0, 8.0, 12.6) 'steel'),
    (New-Cube 'right clamp post' @(11.2, 7.8, 8.0) @(12.6, 12.0, 11.5) 'dark'),
    (New-Cube 'right clamp jaw' @(10.6, 10.8, 8.7) @(13.0, 12.2, 11.2) 'steel'),

    (New-Cube 'right power cable' @(15.55, 3.0, 3.0) @(16.35, 16.0, 5.2) 'red'),
    (New-Cube 'lever housing' @(15.4, 5.5, 8.2) @(19.2, 10.8, 13.6) 'dark'),
    (New-Cube 'lever pivot' @(16.1, 7.0, 9.3) @(19.6, 10.3, 12.8) 'steel'),
    (New-Cube 'lever shaft' @(16.9, 8.7, 10.3) @(18.0, 18.0, 11.8) 'steel' ([ordered]@{ origin = @(17.45, 9.2, 11.05); axis = 'z'; angle = -22.5; rescale = $true })),
    (New-Cube 'lever handle' @(16.0, 16.0, 9.5) @(19.2, 19.2, 12.6) 'red' ([ordered]@{ origin = @(17.45, 9.2, 11.05); axis = 'z'; angle = -22.5; rescale = $true }))
)

$model = [ordered]@{
    credit = 'Techguns Upgrade Station industrial cabinet'
    parent = 'block/block'
    ambientocclusion = $true
    textures = [ordered]@{
        particle = 'techgunsupgrade:blocks/upgrade_table_steel'
        steel = 'techgunsupgrade:blocks/upgrade_table_steel'
        dark = 'techgunsupgrade:blocks/upgrade_table_dark'
        glow = 'techgunsupgrade:blocks/upgrade_table_glow'
        glass = 'techgunsupgrade:blocks/upgrade_table_glass'
        hazard = 'techgunsupgrade:blocks/upgrade_table_hazard'
        screen = 'techgunsupgrade:blocks/upgrade_table_screen'
        gauges = 'techgunsupgrade:blocks/upgrade_table_gauges'
        red = 'techgunsupgrade:blocks/upgrade_table_red'
        sign = 'techgunsupgrade:blocks/upgrade_table_sign'
    }
    display = [ordered]@{
        gui = [ordered]@{ rotation = @(25, 225, 0); translation = @(0, -2.5, 0); scale = @(0.48, 0.48, 0.48) }
        ground = [ordered]@{ translation = @(0, 2.0, 0); scale = @(0.35, 0.35, 0.35) }
        fixed = [ordered]@{ rotation = @(0, 180, 0); translation = @(0, -3.0, 0); scale = @(0.45, 0.45, 0.45) }
        thirdperson_righthand = [ordered]@{ rotation = @(75, 45, 0); translation = @(0, 1.5, 0); scale = @(0.28, 0.28, 0.28) }
        firstperson_righthand = [ordered]@{ rotation = @(0, 45, 0); translation = @(0, 0, 0); scale = @(0.32, 0.32, 0.32) }
    }
    elements = $elements
}

$json = $model | ConvertTo-Json -Depth 12
[System.IO.File]::WriteAllText($modelPath, $json, (New-Object System.Text.UTF8Encoding($false)))
Write-Output $modelPath
