import sys
import os 
from PIL import Image
from os import listdir
from os.path import isfile, join
dir_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), "temp")
onlyfiles = [f for f in listdir(dir_path) if isfile(join(dir_path, f)) and ".png" in f]
onlyfiles.sort()

for x in onlyfiles:
    print(x)


filename = onlyfiles[0]
filename = filename[:filename.index("_frame")]
images = [Image.open(os.path.join(dir_path, x)) for x in onlyfiles]
#images = Image.open(onlyfiles[0])
widths, heights = zip(*(i.size for i in images))

total_width = sum(widths)
max_height = max(heights)

new_im = Image.new('RGBA', (total_width, max_height))

x_offset = 0
for im in images:
  new_im.paste(im, (x_offset,0))
  x_offset += im.size[0]

if not os.path.exists("spritesheet"):
    os.mkdir("spritesheet")
    
out_png = 'spritesheet/'+filename+".png"
print(f"##Status: saving {filename} to {out_png}")
new_im.save(out_png)