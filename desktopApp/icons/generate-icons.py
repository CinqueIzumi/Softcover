#!/usr/bin/env python3
"""Generate the desktop app icons from the canonical 1024x1024 source.

The repo has no vector/SVG master, so the single source of truth is the iOS app icon
(iosApp/.../AppIcon-1024.png, 1024x1024, opaque square). This script derives every desktop
icon from it so the set can be regenerated deterministically if the source ever changes:

  desktopApp/src/main/resources/softcover.png  512x512 square  (runtime Window icon + Linux .deb icon)
  desktopApp/icons/softcover.icns              rounded+padded   (macOS .dmg / dock — native squircle look)
  desktopApp/icons/softcover.ico               multi-size square (Windows .msi / taskbar)

Windows and Linux use the flat square (native there); only macOS gets the rounded, padded,
softly-shadowed treatment so the dock/Finder icon looks native rather than a full-bleed tile.

Requirements (all preinstalled on the build machine — no new deps): Python 3 + Pillow, and on
macOS `iconutil` (ships with the OS) to pack the .iconset into a .icns. Run from anywhere:

    python3 desktopApp/icons/generate-icons.py
"""

import os
import shutil
import subprocess
import sys
import tempfile

from PIL import Image, ImageDraw, ImageFilter

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.normpath(os.path.join(SCRIPT_DIR, "..", ".."))

SOURCE = os.path.join(
    REPO_ROOT,
    "iosApp", "iosApp", "Assets.xcassets", "AppIcon.appiconset", "AppIcon-1024.png",
)
RESOURCES_DIR = os.path.join(REPO_ROOT, "desktopApp", "src", "main", "resources")
PNG_OUT = os.path.join(RESOURCES_DIR, "softcover.png")
ICNS_OUT = os.path.join(SCRIPT_DIR, "softcover.icns")
ICO_OUT = os.path.join(SCRIPT_DIR, "softcover.ico")

# macOS icon grid: content occupies ~80% of the canvas, leaving a margin for the dock shadow;
# corner radius ~22.47% of the content box (the Big Sur "squircle" proportion).
CANVAS = 1024
CONTENT_RATIO = 0.80
CORNER_RATIO = 0.2247
# Base sizes for the .iconset; each emits icon_NxN.png (N px) and icon_NxN@2x.png (2N px).
ICNS_BASE_SIZES = [16, 32, 128, 256, 512]
ICO_SIZES = [16, 32, 48, 64, 128, 256]


def load_source():
    if not os.path.isfile(SOURCE):
        sys.exit(f"Source icon not found: {SOURCE}")

    return Image.open(SOURCE).convert("RGBA")


def rounded_mask(size, radius):
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)

    return mask


def macos_master(src):
    """1024x1024 rounded, padded, softly-shadowed icon on a transparent canvas."""
    content = int(CANVAS * CONTENT_RATIO)
    margin = (CANVAS - content) // 2
    radius = int(content * CORNER_RATIO)

    tile = src.resize((content, content), Image.LANCZOS)
    tile.putalpha(rounded_mask(content, radius))

    canvas = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))

    # Subtle drop shadow, offset slightly down, to match the native dock treatment: a soft-alpha
    # black rounded shape, blurred.
    shadow_alpha = rounded_mask(content, radius).point(lambda a: int(a * 0.35))
    shadow_shape = Image.new("RGBA", (content, content), (0, 0, 0, 255))
    shadow_shape.putalpha(shadow_alpha)

    shadow = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    shadow.paste(shadow_shape, (margin, margin + int(CANVAS * 0.012)), shadow_shape)
    shadow = shadow.filter(ImageFilter.GaussianBlur(int(CANVAS * 0.012)))

    canvas = Image.alpha_composite(canvas, shadow)
    canvas.paste(tile, (margin, margin), tile)

    return canvas


def write_png(src):
    os.makedirs(RESOURCES_DIR, exist_ok=True)
    src.resize((512, 512), Image.LANCZOS).save(PNG_OUT, format="PNG")
    print(f"wrote {os.path.relpath(PNG_OUT, REPO_ROOT)}")


def write_icns(master):
    iconset = tempfile.mkdtemp(suffix=".iconset")
    try:
        for base in ICNS_BASE_SIZES:
            master.resize((base, base), Image.LANCZOS).save(
                os.path.join(iconset, f"icon_{base}x{base}.png"), format="PNG",
            )
            master.resize((base * 2, base * 2), Image.LANCZOS).save(
                os.path.join(iconset, f"icon_{base}x{base}@2x.png"), format="PNG",
            )

        if shutil.which("iconutil") is None:
            print("WARNING: iconutil not found (macOS only); skipping .icns", file=sys.stderr)
            return

        subprocess.run(
            ["iconutil", "-c", "icns", iconset, "-o", ICNS_OUT],
            check=True,
        )
        print(f"wrote {os.path.relpath(ICNS_OUT, REPO_ROOT)}")
    finally:
        shutil.rmtree(iconset, ignore_errors=True)


def write_ico(src):
    src.resize((256, 256), Image.LANCZOS).save(
        ICO_OUT, format="ICO", sizes=[(s, s) for s in ICO_SIZES],
    )
    print(f"wrote {os.path.relpath(ICO_OUT, REPO_ROOT)}")


def main():
    src = load_source()
    write_png(src)
    write_icns(macos_master(src))
    write_ico(src)


if __name__ == "__main__":
    main()
