import os

# If you have multiple images open, you may need to adjust
img = gimp.image_list()[0]

savefn = gimp.pdb['file-png-save-defaults']
outpath = os.path.join(os.path.dirname(os.path.realpath(img.filename)), "../temp")

for lay in img.layers:
    # Even if your layer names contain spaces, not a problem
    outname = lay.name + ".png"
    savefn(img, lay, os.path.join(outpath, outname), outname)