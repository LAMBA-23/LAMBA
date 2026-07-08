from PIL import Image
import os
src = r'C:\Users\lis_b\Desktop\SWP\LAMBA\app\src\main\res\drawable-nodpi\lamba_car_cropped.png'
dst = r'C:\Users\lis_b\Desktop\SWP\LAMBA\app\src\main\res\drawable\ic_launcher_foreground.png'
img = Image.open(src).convert('RGBA')
size = 1080
padding = 80
max_draw = size - padding * 2
w, h = img.size
scale = min(max_draw / w, max_draw / h)
new_w = int(w * scale)
new_h = int(h * scale)
resized = img.resize((new_w, new_h), Image.LANCZOS)
canvas = Image.new('RGBA', (size, size), (0, 0, 0, 0))
canvas.paste(resized, ((size - new_w) // 2, (size - new_h) // 2), resized)
os.makedirs(os.path.dirname(dst), exist_ok=True)
canvas.save(dst)
print('created', dst, 'size', canvas.size, 'image size', new_w, new_h)