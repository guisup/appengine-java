// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appengine.api.images;

/**
 * A transform that will resize an image to fit within a bounding box.
 *
 */
final class Resize extends Transform {

  private static final long serialVersionUID = -889209644904728094L;

  private final int width;
  private final int height;
  private final boolean cropToFit;
  private final float cropOffsetX;
  private final float cropOffsetY;

  /**
   * Creates a transform that will resize an image to fit within a rectangle
   * with the given dimensions. If {@code cropToFit} is true, then the image is
   * cropped to fit, with the center specified by {@code cropOffsetX} and
   * {@code cropOffsetY}.
   * @param width width of the bounding box
   * @param height height of the bounding box
   * @param cropToFit whether the image should be cropped to fit
   * @param cropOffsetX the relative horizontal position of the center
   * @param cropOffsetY the relative vertical position of the center
   * @throws IllegalArgumentException If {@code width} or {@code height} are
   * negative or greater than {@code MAX_RESIZE_DIMENSIONS}, if both
   * {@code width} and {@code height} are 0 or if {@code cropToFit} is
   * set and {@code width} or {@code height} is 0 or {@code cropOffsetX} or
   * {@code cropOffsetY} is outside the range 0.0 to 1.0.
   */
  Resize(int width, int height, boolean cropToFit, float cropOffsetX, float cropOffsetY) {
    if (width > ImagesService.MAX_RESIZE_DIMENSIONS
        || height > ImagesService.MAX_RESIZE_DIMENSIONS) {
      throw new IllegalArgumentException("width and height must be <= "
                                         + ImagesService.MAX_RESIZE_DIMENSIONS);
    }
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("width and height must be >= 0");
    }
    if (width == 0 && height == 0) {
      throw new IllegalArgumentException("width and height must not both be == 0");
    }
    if (cropToFit) {
      if (width == 0 || height == 0) {
        throw new IllegalArgumentException(
            "neither of width and height can be == 0 with crop to fit enabled");
      }
      checkCropArgument(cropOffsetX);
      checkCropArgument(cropOffsetY);
    }
    this.width = width;
    this.height = height;
    this.cropToFit = cropToFit;
    this.cropOffsetX = cropOffsetX;
    this.cropOffsetY = cropOffsetY;
  }

  /** {@inheritDoc} */
  @Override
  void apply(ImagesServicePb.ImagesTransformRequest.Builder request) {
    request.addTransform(
        ImagesServicePb.Transform.newBuilder()
        .setWidth(width)
        .setHeight(height)
        .setCropToFit(cropToFit)
        .setCropOffsetX(cropOffsetX)
        .setCropOffsetY(cropOffsetY));
  }

  /**
   * Checks that a crop argument is in the valid range.
   * @param arg crop argument
   */
  private void checkCropArgument(float arg) {
    if (arg < 0.0) {
      throw new IllegalArgumentException("Crop offsets must be >= 0");
    }
    if (arg > 1.0) {
      throw new IllegalArgumentException("Crop offsets must be <= 1");
    }
  }
}
