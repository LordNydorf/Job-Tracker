import os
from PIL import Image, ImageDraw

def render_launcher_icon(size, is_round=False):
    # Create RGBA canvas
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    s = size / 108.0

    # 1. Background Mask & Canvas
    bg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bg_draw = ImageDraw.Draw(bg)
    
    if is_round:
        bg_draw.ellipse([0, 0, size, size], fill=(9, 13, 22, 255))
    else:
        radius = int(22 * s)
        bg_draw.rounded_rectangle([0, 0, size, size], radius=radius, fill=(9, 13, 22, 255))

    # 2. Radial Indigo Glow
    glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    center = size / 2.0
    max_r = 44 * s
    for r in range(int(max_r), 0, -1):
        alpha = int(220 * (1.0 - (r / max_r) ** 1.5))
        glow_draw.ellipse([center - r, center - r, center + r, center + r], fill=(42, 27, 109, alpha))
    
    bg = Image.alpha_composite(bg, glow)
    draw = ImageDraw.Draw(bg)

    # 3. Outer Circular Accent Rings
    def draw_ring(radius, color, width=1.5):
        r = radius * s
        w = max(1, int(width * s))
        draw.ellipse([center - r, center - r, center + r, center + r], outline=color, width=w)

    draw_ring(38, (49, 46, 129, 140), 1.2)
    draw_ring(33, (56, 189, 248, 110), 1.8)
    draw_ring(28, (67, 56, 202, 90), 1.2)

    # 4. Briefcase Handle Arc
    handle_w = max(3, int(4.5 * s))
    draw.rounded_rectangle([42.5 * s, 29.5 * s, 65.5 * s, 43 * s], radius=int(6.5 * s), outline=(56, 189, 248, 255), width=handle_w)
    draw.rectangle([40 * s, 39 * s, 68 * s, 45 * s], fill=(9, 13, 22, 0))

    # 5. Briefcase Main Body Depth Layer
    draw.rounded_rectangle([27 * s, 43 * s, 81 * s, 79 * s], radius=int(8 * s), fill=(55, 48, 163, 255))

    # 6. Briefcase Main Body Gradient Face
    face = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    face_draw = ImageDraw.Draw(face)
    face_draw.rounded_rectangle([27 * s, 43 * s, 81 * s, 76 * s], radius=int(8 * s), fill=(79, 70, 229, 255))
    
    face_highlight = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    fh_draw = ImageDraw.Draw(face_highlight)
    fh_draw.rounded_rectangle([27 * s, 43 * s, 81 * s, 60 * s], radius=int(8 * s), fill=(99, 102, 241, 220))
    face = Image.alpha_composite(face, face_highlight)
    
    bg = Image.alpha_composite(bg, face)
    draw = ImageDraw.Draw(bg)

    # 7. Geometric Cut Flap Accent (#818CF8)
    flap_poly = [
        (27 * s, 47 * s),
        (54 * s, 64 * s),
        (81 * s, 47 * s),
        (81 * s, 53 * s),
        (54 * s, 70 * s),
        (27 * s, 53 * s)
    ]
    draw.polygon(flap_poly, fill=(129, 140, 248, 255))

    # 8. Dynamic Upward Growth Arrow
    arrow_w = max(3, int(4.0 * s))
    draw.line([(54 * s, 53 * s), (54 * s, 68 * s)], fill=(255, 255, 255, 255), width=arrow_w)
    draw.line([(46 * s, 60 * s), (54 * s, 51 * s)], fill=(255, 255, 255, 255), width=arrow_w)
    draw.line([(54 * s, 51 * s), (62 * s, 60 * s)], fill=(255, 255, 255, 255), width=arrow_w)
    draw.ellipse([54 * s - arrow_w/2, 51 * s - arrow_w/2, 54 * s + arrow_w/2, 51 * s + arrow_w/2], fill=(255, 255, 255, 255))

    # 9. Active Emerald Pulse Indicator Dot (#10B981)
    dot_center_x = 73 * s
    dot_center_y = 39 * s
    dot_r = 4.2 * s
    draw.ellipse([dot_center_x - dot_r*1.5, dot_center_y - dot_r*1.5, dot_center_x + dot_r*1.5, dot_center_y + dot_r*1.5], fill=(16, 185, 129, 80))
    draw.ellipse([dot_center_x - dot_r, dot_center_y - dot_r, dot_center_x + dot_r, dot_center_y + dot_r], fill=(16, 185, 129, 255))

    # Apply circular mask if round
    if is_round:
        mask = Image.new("L", (size, size), 0)
        mask_draw = ImageDraw.Draw(mask)
        mask_draw.ellipse([0, 0, size, size], fill=255)
        output = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        output.paste(bg, (0, 0), mask)
        return output
    else:
        mask = Image.new("L", (size, size), 0)
        mask_draw = ImageDraw.Draw(mask)
        mask_draw.rounded_rectangle([0, 0, size, size], radius=int(22 * s), fill=255)
        output = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        output.paste(bg, (0, 0), mask)
        return output

def main():
    base_res = "c:/Users/notth/Projects/New folder/androidApp/src/main/res"
    
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    for folder, size in densities.items():
        folder_path = os.path.join(base_res, folder)
        os.makedirs(folder_path, exist_ok=True)
        
        # Standard icon
        img = render_launcher_icon(size, is_round=False)
        img.save(os.path.join(folder_path, "ic_launcher.png"), "PNG")
        
        # Round icon
        round_img = render_launcher_icon(size, is_round=True)
        round_img.save(os.path.join(folder_path, "ic_launcher_round.png"), "PNG")
        print(f"Generated {folder}/ic_launcher.png ({size}x{size})")

    # High-resolution 512x512 and 1024x1024 master assets for docs & Play Store
    docs_dir = "c:/Users/notth/Projects/New folder/docs/images"
    os.makedirs(docs_dir, exist_ok=True)
    
    master_512 = render_launcher_icon(512, is_round=False)
    master_512.save(os.path.join(docs_dir, "icon_512.png"), "PNG")
    
    master_1024 = render_launcher_icon(1024, is_round=False)
    master_1024.save(os.path.join(docs_dir, "icon_1024.png"), "PNG")
    
    print("Generated master 512x512 and 1024x1024 icons in docs/images/")

if __name__ == "__main__":
    main()
