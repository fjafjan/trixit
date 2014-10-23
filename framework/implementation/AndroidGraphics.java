package com.trixit.framework.implementation;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Log;

import com.trixit.framework.Graphics;
import com.trixit.framework.Image;

// TODO fix some bug with the scaling and another thing, search the thread for the errors.
// implement AndroidImage.

public class AndroidGraphics implements Graphics {
    AssetManager assets;
    Bitmap frameBuffer;
    Canvas canvas;
    Paint paint;
    Rect srcRect = new Rect();
    Rect dstRect = new Rect();

    // Creates our AndroidGraphics instance.
    public AndroidGraphics(AssetManager assets, Bitmap frameBuffer) {
        this.assets = assets;
        this.frameBuffer = frameBuffer;
        this.canvas = new Canvas(frameBuffer);
        this.paint = new Paint();
    }

    @Override
    // Creates an image object with a low, medium or high fidelity format.
    // The names of the formats is the number of bytes in each layer.
    public Image newImage(String fileName, ImageFormat format) {
        Config config = null;
        if (format == ImageFormat.RGB565)
            config = Config.RGB_565;
        else if (format == ImageFormat.ARGB4444)
            config = Config.ARGB_4444;
        else
            config = Config.ARGB_8888;

        Options options = new Options();
        options.inPreferredConfig = config;
       
       
        InputStream in = null;
        Bitmap bitmap = null;
        // We try to load the given image file. If it doesn't work we 
        // throw some kind of exception.
        try { 
            in = assets.open(fileName);
            bitmap = BitmapFactory.decodeStream(in, null, options);
            if (bitmap == null)
                throw new RuntimeException("Couldn't load bitmap from asset '"
                        + fileName + "'");
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load bitmap from asset '"
                    + fileName + "'");
        } finally {
        	// Try to close the file we are reading even if exceptions are thrown. 
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        if (bitmap.getConfig() == Config.RGB_565)
            format = ImageFormat.RGB565;
        else if (bitmap.getConfig() == Config.ARGB_4444)
            format = ImageFormat.ARGB4444;
        else
            format = ImageFormat.ARGB8888;
        	
        // We store the results as an instance of our Image class.
        return new AndroidImage(bitmap, format);
    }

    /// Well this evidently clears the screen, but rather unclear how it does so.
    @Override
    public void clearScreen(int color) {
        canvas.drawRGB((color & 0xff0000) >> 16, (color & 0xff00) >> 8,
                (color & 0xff));
    }

    // Drawing a line between two points. Why is this important? Unclear...
    @Override
    public void drawLine(int x, int y, int x2, int y2, int color) {
        paint.setColor(color);
        canvas.drawLine(x, y, x2, y2, paint);
    }

    // Again we have a simplistic drawing method that as well seems to be
    // overridden? Unclear why this is beneficial but assuming the 
    // implementation is decent and not used too much it won't hurt.
    @Override
    public void drawRect(int x, int y, int width, int height, int color) {
        paint.setColor(color);
        paint.setStyle(Style.FILL);
        canvas.drawRect(x, y, x + width - 1, y + height - 1, paint);
    }
   
    // Drawing with an alpha channel. 
    @Override
    public void drawARGB(int a, int r, int g, int b) {
        paint.setStyle(Style.FILL);
       canvas.drawARGB(a, r, g, b);
    }
   
    @Override
    public void drawString(String text, int x, int y, Paint paint){
        canvas.drawText(text, x, y, paint);
    }
   
    // Draws the image Image. Draws only a subset of the image specified by
    // the src coordinates. The remaining image is drawn onto the dst
    // coordinates. 
    public void drawImage(Image Image, int x, int y, int srcX, int srcY,
            int srcWidth, int srcHeight) {
        srcRect.left = srcX;
        srcRect.top = srcY;
        srcRect.right = srcX + srcWidth;
        srcRect.bottom = srcY + srcHeight;
       
       
        dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x + srcWidth;
        dstRect.bottom = y + srcHeight;

        canvas.drawBitmap(((AndroidImage) Image).bitmap, srcRect, dstRect,
                null);
    }
   
    // This is similar to the previous function only this one always
    // draws the complete image, which means you only need to specify 
    // the top left corner where the image will be drawn. 

    @Override
    public void drawImage(Image image, int x, int y) {
        canvas.drawBitmap(((AndroidImage)image).bitmap, x, y, null);
    }

    
    
    // This similarly to the previous function draws a portion of an image.
    // However this one also specifies a size of the resulting image
    // scaling the results with factors width/srcWidth, height/srcHeight. 
    public void drawScaledImage(Image Image, int x, int y, int width, int height, int srcX, int srcY, 
    		int srcWidth, int srcHeight){
       
    	srcRect.left = srcX;
        srcRect.top = srcY;
        srcRect.right = srcX + srcWidth;
        srcRect.bottom = srcY + srcHeight;
       
       
        dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x + width;
        dstRect.bottom = y + height;
       
   
       
        canvas.drawBitmap(((AndroidImage) Image).bitmap, srcRect, dstRect, null);
       
    }
   
    /// Draws the image at position x, y scaled linearly with 'scale'.
    public void drawScaledImage(Image image, int x, int y, double scale){
        dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x +  (int)(image.getWidth() * scale);
        dstRect.bottom = y + (int)(image.getHeight() * scale);
       
        canvas.drawBitmap(((AndroidImage) image).bitmap, null , dstRect, null);
    }
    
    ///Draws the image at position x, y, scaled linearly and rotated a fixed angle. 
    public void drawRotatedScaledImage(Image image, int x, int y, double scale, double angle){
    	dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x +  (int)(image.getWidth() * scale);
        dstRect.bottom = y + (int)(image.getHeight() * scale);
        float imageCenterX = (float)(x + (image.getWidth() * scale * 0.5));
        float imageCenterY = (float)(y + (image.getHeight() * scale * 0.5));
//        Log.w("Debuggin", "imageCenter is " + imageCenterX + " , " + imageCenterY);
//        Log.w("Debuggin", "normal xy are  " + (x+50) + " , " + (y+50));
        Log.w("Debuggin", "Angle is " + angle);

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate((float) angle, imageCenterX, imageCenterY);
        canvas.drawBitmap(((AndroidImage) image).bitmap, x, y,null);
        canvas.restore();
    	//canvas.drawBitmap(((AndroidImage) image).bitmap, null , dstRect, null);
    }
    
    
    @Override
    public int getWidth() {
        return frameBuffer.getWidth();
    }

    @Override
    public int getHeight() {
        return frameBuffer.getHeight();
    }
}
 
