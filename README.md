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

## Installation ##

### Prerequisites ###

* Some version of **Git**
    * If you are on the Mac, you should already have the command line version of git installed.
    * For other OSs or for the GUI version, they can be downloaded [here](http://git-scm.com/downloads).
* **JDK 8**
    * [download it from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* **[Apache Maven](http://maven.apache.org/download.cgi) 3.3** or later

A few binaries needed by the code have to be installed on the OS.

_Note: This only works on Linux and has only been tested on Ubuntu.  There are a number of non-java binaries that come with this project and they have I have only tried compiling them for for Linux, specifically Ubuntu._
* [ImageMagick](https://www.imagemagick.org/script/binary-releases.php) needs to be installed on the system (used for converting images because JAVA cannot handle certain file types).
* The following binaries need to be compiled into the root of the project in the `<PROJECT_DIRECTORY>/lib/binary/linux` directory.
  * advpng ([source](https://github.com/amadvance/advancecomp/), [homepage](http://advancemame.sourceforge.net/doc-advpng.html))
  * gifsicle ([source](https://www.lcdf.org/gifsicle/gifsicle-1.88.tar.gz), [homepage](https://www.lcdf.org/gifsicle/))
  * jfifremove ([source](https://lyncd.com/files/imgopt/jfifremove.c))
  * jpegtran ([source](http://www.ijg.org/files/jpegsrc.v9b.tar.gz), [homepage](http://jpegclub.org/jpegtran/))
  * optipng ([source](http://prdownloads.sourceforge.net/optipng/optipng-0.7.5.tar.gz?download), [homepage](http://optipng.sourceforge.net/))
  * pngout ([source](http://www.jonof.id.au/kenutils), [homepage](http://advsys.net/ken/utils.htm))


### Additional Maven set up ###

Maven uses the JDK pointed to be the `JAVA_HOME` environment variable. Verify that Maven is using JDK 8, for example:
Maven 3.3.3+ is recommended.

```
$ mvn --version

Apache Maven 3.3.3 (r01de14724cdef164cd33c7c8c2fe155faf9602da; 2013-02-19 05:51:28-0800)
Maven home: /Users/<user>/maven/apache-maven-3.0.5
Java version: 1.8.0_25, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "10.10.1", arch: "x86_64", family: "mac"
```

## Usage ##

There are 2 ways that the library can be used:

Calling the main method from the commandline with a list of files or folders.

    java -jar ImageOptimization-1.2.jar -DbinariesDirectory=<PATH_TO_BINARIES_DIRECTORY> path/to/image.png path/to/folder/of/images/

The `<PATH_TO_BINARIES_DIRECTORY>` is the path where the binaries exist that are used to optimize the images. The binaries are currently in the [`lib/binary/linux/`](https://git.soma.salesforce.com/perfeng/ImageOptimization/tree/master/lib/binary/linux) directory

You can also call this code programmatically from existing JAVA code by using the API, `com.salesforce.perfeng.uiperf.imageoptimization.service.ImageOptimizationService.optimizeAllImages(FileTypeConversion, boolean, Collection<File>)`.

Example:

    final IImageOptimizationService<Void> service = new ImageOptimizationService.createInstance(<PATH_TO_BINARIES_DIRECTORY>);
    final List<OptimizationResult<Void>> list = service.optimizeAllImages(FileTypeConversion.NONE, false, new File("path/to/image.jpg"), File("path/to/image2.jpg"));
    System.out.println(list);

The main API is `ImageOptimizationService.optimizeAllImages`.
* The 1st argument indicates if / how the image should be converted. There are currently 3 types of conversion. `FileTypeConversion.NONE`: None of the images will be converted to a different files type; `FileTypeConversion.ALL`: There are no restrictions around which images will be converted to different images types as long as it results in a smaller file size (less bytes) and optimization is lossless; `FileTypeConversion.IE6SAFE`: The same as `ALL` except that it will not convert the image if it is a GIF with Alpha transparency. PNG files with transparency, when loaded in IE6, show the transparent parts as gray.
* The 2nd argument indicates if browser specific versions of the file should be generated in addition to the optimized version of the image.
* The 3rd argument is the collection of image files to optimize.

The function returns a list of `OptimizationResult` objects.

### How is the Optimization Actually Accomplished? ###

The heavy lifing is done by 6 different binary applications:
* advpng ([binary](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/binary/linux/advpng), [source](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/advancecomp-1.17.zip), [homepage](http://advancemame.sourceforge.net/doc-advpng.html))
* gifsicle ([binary](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/binary/linux/gifsicle), [source](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/gifsicle-1.67.zip), [homepage](http://www.lcdf.org/gifsicle/))
* jfifremove ([binary](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/binary/linux/jijifremove), [source](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/jpeg-9.zip))
* jpegtran ([binary](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/binary/linux/jpegtran), [source](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/jpeg-9.zip), [homepage](http://jpegclub.org/jpegtran/))
* optipng ([binary](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/binary/linux/optipng), [source](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/optipng-0.7.4.zip), [homepage](http://optipng.sourceforge.net/))
* pngout ([binary](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/binary/linux/pngout), [source](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/pngout-20130221-linux.zip), [homepage](http://advsys.net/ken/utils.htm))

The JAVA code calls out to these binaries and using the appropriate ones for the image format.  The code does this twice.  For some reason passing in an already optimized image will result in a few bytes reduction the second time it is optimized.

For converting the images we use 3 binaries:
* ImageMagick ([source](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/ImageMagick-6.8.7-10.zip), [homepage](http://www.imagemagick.org/))
* cwebp ([binary](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/binary/linux/advpng), [source](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/libwebp-0.3.1-linux-x86-32.zip), [homepage](https://developers.google.com/speed/webp/docs/cwebp))
* gif2webp ([binary](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/binary/linux/advpng), [source](https://github.com/forcedotcom/ImageOptimization/blob/master/lib/libwebp-0.3.1-linux-x86-32.zip), [homepage](https://developers.google.com/speed/webp/docs/gif2webp))

### Automated Validation ###

For each image generated, the code will perform a pixel by pixel comparison of the original and optimized image to take sure they are identical. It will indicate when there is an issue. This allows for a high level of confidence that the image has not visually changed.
The way it works is

1. It will take the original image, render it, and gather all of the pixels in an arry.
2. It does the same thing for the optimized image.
3. Iterate over each array and compare the RGBA values of the pixel at spot `i`. If the pixels are identical, it passes. If the pixels are different and the alpha channel on both pixels is 100% transparent then the color does not matter and the pixels are considered identical. Any other difference will be considered a failure.
