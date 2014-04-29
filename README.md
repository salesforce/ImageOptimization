# ImageOptimization - Library for losslessly optimizing images #

Copyright (c) 2014, Salesforce.com. All rights reserved.

Created by <span itemscope="" itemtype="http://schema.org/Person">
	<a itemprop="url" rel="author" href="https://github.com/eperret"><span itemprop="name">Eric Perret</span></a>
</span>

## Summary ##

ImageOptimization is a JAVA batch program / service used to optimize images by reducing the size (less bytes) of images without changing the quality of the images. This process is called [lossless compression](http://en.wikipedia.org/wiki/Image_compression#Lossy_and_lossless_compression).

Apart from optimizing an image, it also supports a few other things
* Converting image types, GIFs to PNGs, if it will make the image smaller.
* Create a Chrome specific verison, [WebP](https://developers.google.com/speed/webp/?csw=1)
* Automated validation of images.

## Usage ##

<div>There are 2 ways that the library can be used:</div>
Calling the main method from the commandline with a list of files or folders.

    java -jar ImageOptimization-1.2.jar -DbinariesDirectory=<PATH_TO_BINARIES_DIRECTORY> path/to/image.png path/to/folder/of/images/

The `<PATH_TO_BINARIES_DIRECTORY>` is the path where the binaries exist that are used to optimize the images. The bi narys are currently in the [`lib/binary/linux/`](https://git.soma.salesforce.com/perfeng/ImageOptimization/tree/master/lib/binary/linux) directory

You can also call this code programmatically from existing JAVA code by using the API, `com.salesforce.perfeng.uiperf.imageoptimization.service.ImageOptimizationService.optimizeAllImages(FileTypeConversion, boolean, Collection<File>)`.

Example:

    final IImageOptimizationService<Void> service = new ImageOptimizationService.createInstance(<PATH_TO_BINARIES_DIRECTORY>);
    final List<OptimizationResult<Void>> list = service.optimizeAllImages(FileTypeConversion.NONE, false, new File("path/to/image.jpg"), File("path/to/image2.jpg"));
    System.out.println(list);

The main API is `ImageOptimizationService.optimizeAllImages`.
* The 1st agument indicates if / how the image should be converted. There are currently 3 types of conversion. `FileTypeConversion.NONE`: None of the images will be converted to a different files type; `FileTypeConversion.ALL`: There are no restrictions around which images will be converted to different images types as long as it results in a smaller file size (less bytes) and optimization is lossless; `FileTypeConversion.IE6SAFE`: The same as `ALL` except that it will not convert the image if it is a GIF with Alpha transparency. PNG files with transparency, when loaded in IE6, show the transparent parts as gray.
* The 2nd argument indicates if browser specific versions of the file should be generated in addition to the optimized version of the image.
* The 3rd argument is the collection of image files to optimize.

The function returns a list of `OptimizationResult` objects.

### Automated Validation ###

For each image generated the code will perform a pixel by pixel comparison of the original and optimized image to take sure they are identical. It will indicate when there is an issue. This allows for a high level of confidence that the image has not visually changed.
The way it works is

1. It will take the original image, render it, and gather all of the pixels in an arry.
2. It does the same thing for the optimized image.
3. Iterate over each array and compare the RGBA values of the pixel at spot `i`. If the pixels are identical then it passes. If the pixels are different and the alpha channel on both pixels is 100% transparent then the color does not matter and the pixels are considered identical. Any other difference will be connsidered a failure.
