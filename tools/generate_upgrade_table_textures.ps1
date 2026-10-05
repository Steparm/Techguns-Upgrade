Add-Type -AssemblyName System.Drawing

$outputDirectory = Join-Path $PSScriptRoot '..\src\main\resources\assets\techgunsupgrade\textures\blocks'
$outputDirectory = [System.IO.Path]::GetFullPath($outputDirectory)
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null

function New-Texture([int] $width, [int] $height, [System.Drawing.Color] $background) {
    $bitmap = New-Object System.Drawing.Bitmap($width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $graphics.Clear($background)
    return @($bitmap, $graphics)
}

function Fill-Rect($graphics, [System.Drawing.Color] $color, [int] $x, [int] $y, [int] $width, [int] $height) {
    $brush = New-Object System.Drawing.SolidBrush($color)
    $graphics.FillRectangle($brush, $x, $y, $width, $height)
    $brush.Dispose()
}

function Draw-Line($graphics, [System.Drawing.Color] $color, [int] $x1, [int] $y1, [int] $x2, [int] $y2) {
    $pen = New-Object System.Drawing.Pen($color)
    $graphics.DrawLine($pen, $x1, $y1, $x2, $y2)
    $pen.Dispose()
}

function Save-Texture($bitmap, $graphics, [string] $name) {
    $path = Join-Path $outputDirectory $name
    $graphics.Dispose()
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
    Write-Output $path
}

# Gunmetal plate with seams, scratches and corner bolts.
$parts = New-Texture 32 32 ([System.Drawing.Color]::FromArgb(255, 45, 49, 56))
$bitmap, $graphics = $parts
for ($y = 0; $y -lt 32; $y += 4) {
    for ($x = 0; $x -lt 32; $x += 4) {
        $shade = if ((($x + $y) / 4) % 2 -eq 0) { 4 } else { -3 }
        Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 45 + $shade, 49 + $shade, 56 + $shade)) $x $y 4 4
    }
}
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 24, 27, 33)) 0 0 32 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 76, 82, 91)) 0 2 32 1
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 25, 28, 34)) 0 15 32 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 67, 73, 82)) 0 17 32 1
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 27, 30, 36)) 15 0 2 32
foreach ($point in @(@(2, 2), @(28, 2), @(2, 28), @(28, 28))) {
    Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 17, 19, 24)) $point[0] $point[1] 2 2
    $bitmap.SetPixel($point[0], $point[1], [System.Drawing.Color]::FromArgb(255, 119, 124, 130))
}
foreach ($point in @(@(6, 7), @(23, 5), @(10, 21), @(26, 25), @(4, 26), @(20, 12))) {
    $bitmap.SetPixel($point[0], $point[1], [System.Drawing.Color]::FromArgb(255, 92, 96, 101))
    if ($point[0] -lt 31) { $bitmap.SetPixel($point[0] + 1, $point[1], [System.Drawing.Color]::FromArgb(255, 30, 33, 39)) }
}
Save-Texture $bitmap $graphics 'upgrade_table_steel.png'

# Recessed dark panels.
$parts = New-Texture 32 32 ([System.Drawing.Color]::FromArgb(255, 19, 23, 28))
$bitmap, $graphics = $parts
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 57, 63, 70)) 0 0 32 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 8, 11, 15)) 2 2 28 28
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 29, 34, 40)) 4 4 24 24
Draw-Line $graphics ([System.Drawing.Color]::FromArgb(255, 46, 52, 60)) 5 25 25 5
Draw-Line $graphics ([System.Drawing.Color]::FromArgb(255, 18, 22, 27)) 7 27 27 7
foreach ($point in @(@(4, 4), @(26, 4), @(4, 26), @(26, 26))) {
    Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 91, 96, 102)) $point[0] $point[1] 2 2
}
Save-Texture $bitmap $graphics 'upgrade_table_dark.png'

# Cyan energy strips. Forge 1.12 requires atlas sprites to be square unless
# they have animation metadata, so every static station texture is square.
$parts = New-Texture 32 32 ([System.Drawing.Color]::FromArgb(255, 6, 17, 23))
$bitmap, $graphics = $parts
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 8, 39, 50)) 3 0 26 32
for ($y = 1; $y -lt 32; $y += 5) {
    Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 22, 119, 144)) 5 $y 22 3
    Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 94, 232, 255)) 8 $y 16 1
}
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 2, 8, 12)) 0 0 4 32
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 2, 8, 12)) 28 0 4 32
Save-Texture $bitmap $graphics 'upgrade_table_glow.png'

# Cutout glass: transparent center with a cyan reinforced border.
$parts = New-Texture 32 32 ([System.Drawing.Color]::FromArgb(0, 0, 0, 0))
$bitmap, $graphics = $parts
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 15, 74, 88)) 0 0 32 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 15, 74, 88)) 0 30 32 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 15, 74, 88)) 0 0 2 32
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 15, 74, 88)) 30 0 2 32
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 82, 208, 228)) 2 2 28 1
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 82, 208, 228)) 2 2 1 28
for ($i = 5; $i -lt 28; $i += 8) {
    $bitmap.SetPixel($i, $i - 2, [System.Drawing.Color]::FromArgb(255, 45, 128, 145))
    $bitmap.SetPixel($i + 1, $i - 2, [System.Drawing.Color]::FromArgb(255, 45, 128, 145))
}
Save-Texture $bitmap $graphics 'upgrade_table_glass.png'

# Industrial warning stripe.
$parts = New-Texture 32 32 ([System.Drawing.Color]::FromArgb(255, 18, 18, 17))
$bitmap, $graphics = $parts
for ($offset = -32; $offset -lt 64; $offset += 14) {
    [System.Drawing.Point[]] $points = @(
        [System.Drawing.Point]::new($offset, 32),
        [System.Drawing.Point]::new(($offset + 8), 32),
        [System.Drawing.Point]::new(($offset + 30), 0),
        [System.Drawing.Point]::new(($offset + 22), 0)
    )
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 194, 137, 24))
    $graphics.FillPolygon($brush, $points)
    $brush.Dispose()
}
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 74, 79, 82)) 0 0 32 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 7, 8, 9)) 0 30 32 2
Save-Texture $bitmap $graphics 'upgrade_table_hazard.png'

# Blue diagnostic screen.
$parts = New-Texture 32 32 ([System.Drawing.Color]::FromArgb(255, 7, 15, 20))
$bitmap, $graphics = $parts
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 50, 58, 65)) 0 0 32 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 2, 29, 39)) 2 2 28 28
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 20, 128, 153)) 4 6 8 3
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 68, 214, 235)) 4 13 14 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 23, 105, 128)) 4 20 19 2
foreach ($x in @(21, 25, 29)) { Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 238, 132, 34)) $x 6 2 3 }
Save-Texture $bitmap $graphics 'upgrade_table_screen.png'

# Two analogue dials on a steel panel.
$parts = New-Texture 32 32 ([System.Drawing.Color]::FromArgb(255, 36, 40, 46))
$bitmap, $graphics = $parts
foreach ($centerX in @(9, 23)) {
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 199, 184, 145))
    $graphics.FillEllipse($brush, $centerX - 6, 10, 12, 12)
    $brush.Dispose()
    $pen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 20, 22, 25), 2)
    $graphics.DrawEllipse($pen, $centerX - 6, 10, 12, 12)
    $pen.Dispose()
    Draw-Line $graphics ([System.Drawing.Color]::FromArgb(255, 145, 37, 25)) $centerX 16 ($centerX + 3) 13
    Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 24, 24, 23)) ($centerX - 1) 15 2 2
}
Save-Texture $bitmap $graphics 'upgrade_table_gauges.png'

# Lever grip and red cable material.
$parts = New-Texture 16 16 ([System.Drawing.Color]::FromArgb(255, 88, 24, 19))
$bitmap, $graphics = $parts
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 151, 49, 34)) 2 1 12 14
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 207, 77, 50)) 4 2 8 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 93, 25, 21)) 3 12 10 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 45, 14, 13)) 0 0 2 16
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 45, 14, 13)) 14 0 2 16
Save-Texture $bitmap $graphics 'upgrade_table_red.png'

# Pixel-art UPGRADE sign. The glyph rows are four pixels tall to compensate
# for mapping this square atlas sprite onto a wide physical sign.
$parts = New-Texture 64 64 ([System.Drawing.Color]::FromArgb(255, 7, 9, 11))
$bitmap, $graphics = $parts
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 76, 82, 88)) 0 0 64 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 45, 49, 54)) 1 2 62 60
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 10, 12, 14)) 3 5 58 54
$glyphs = @{
    'U' = @('10001','10001','10001','10001','10001','10001','01110')
    'P' = @('11110','10001','10001','11110','10000','10000','10000')
    'G' = @('01110','10001','10000','10111','10001','10001','01110')
    'R' = @('11110','10001','10001','11110','10100','10010','10001')
    'A' = @('01110','10001','10001','11111','10001','10001','10001')
    'D' = @('11110','10001','10001','10001','10001','10001','11110')
    'E' = @('11111','10000','10000','11110','10000','10000','11111')
}
$cursorX = 7
foreach ($character in 'UPGRADE'.ToCharArray()) {
    $rows = $glyphs[[string]$character]
    for ($row = 0; $row -lt 7; $row++) {
        for ($column = 0; $column -lt 5; $column++) {
            if ($rows[$row][$column] -eq '1') {
                Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 244, 171, 39)) ($cursorX + $column) (18 + $row * 4) 1 4
                $bitmap.SetPixel($cursorX + $column, 18 + $row * 4, [System.Drawing.Color]::FromArgb(255, 255, 213, 92))
            }
        }
    }
    $cursorX += 7
}
Save-Texture $bitmap $graphics 'upgrade_table_sign.png'

# Dedicated inventory/hand icon. The full 34-part block model is taller and
# wider than a normal Minecraft block, and Forge 1.12 does not bake it reliably
# as an ItemBlock (it can fall back to the magenta missing-model cube). A small
# front-view sprite keeps the apparatus recognizable without clipping the hand
# or inventory slot.
$itemOutputDirectory = Join-Path $PSScriptRoot '..\src\main\resources\assets\techgunsupgrade\textures\items'
$itemOutputDirectory = [System.IO.Path]::GetFullPath($itemOutputDirectory)
New-Item -ItemType Directory -Path $itemOutputDirectory -Force | Out-Null

$parts = New-Texture 32 32 ([System.Drawing.Color]::FromArgb(0, 0, 0, 0))
$bitmap, $graphics = $parts

# Cabinet silhouette and top machinery.
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 10, 12, 15)) 5 4 21 26
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 55, 61, 69)) 6 5 19 24
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 22, 26, 32)) 8 1 14 4
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 66, 73, 82)) 9 1 12 1
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 9, 24, 31)) 11 2 8 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 55, 205, 229)) 12 2 6 1

# Amber UPGRADE nameplate.
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 7, 9, 11)) 7 5 17 4
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 226, 153, 30)) 9 6 13 1
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 105, 67, 15)) 10 7 11 1

# Cyan reinforced display chamber.
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 8, 12, 17)) 7 10 17 12
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 27, 153, 181)) 7 10 17 1
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 80, 229, 247)) 8 11 1 10
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 15, 52, 65)) 9 12 13 8
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 24, 100, 119)) 22 11 1 10

# Weapon silhouette held by two orange clamps.
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 88, 96, 104)) 10 15 10 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 134, 142, 148)) 12 14 6 1
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 35, 39, 44)) 19 15 3 1
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 39, 43, 48)) 12 17 2 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 221, 113, 31)) 9 14 1 4
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 221, 113, 31)) 22 14 1 4

# Lower console, hazard stripe, feet and red lever.
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 12, 18, 22)) 7 23 17 4
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 24, 151, 181)) 10 24 7 1
foreach ($x in @(7, 11, 15, 19)) {
    Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 211, 148, 28)) $x 27 3 1
}
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 19, 22, 27)) 7 29 5 2
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 19, 22, 27)) 20 29 5 2
Draw-Line $graphics ([System.Drawing.Color]::FromArgb(255, 88, 94, 101)) 25 14 28 8
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 154, 48, 35)) 27 6 3 5
Fill-Rect $graphics ([System.Drawing.Color]::FromArgb(255, 225, 84, 54)) 28 6 1 4

# Steel highlights and corner bolts.
Draw-Line $graphics ([System.Drawing.Color]::FromArgb(255, 91, 99, 108)) 6 5 6 28
Draw-Line $graphics ([System.Drawing.Color]::FromArgb(255, 20, 23, 28)) 25 5 25 28
foreach ($point in @(@(7, 6), @(23, 6), @(7, 28), @(23, 28))) {
    $bitmap.SetPixel($point[0], $point[1], [System.Drawing.Color]::FromArgb(255, 155, 159, 162))
}

$itemPath = Join-Path $itemOutputDirectory 'upgrade_table.png'
$graphics.Dispose()
$bitmap.Save($itemPath, [System.Drawing.Imaging.ImageFormat]::Png)
$bitmap.Dispose()
Write-Output $itemPath
