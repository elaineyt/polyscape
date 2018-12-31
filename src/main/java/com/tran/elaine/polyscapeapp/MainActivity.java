package com.tran.elaine.polyscapeapp;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Button;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnTouchListener, OnTaskCompleted, GetShapeListCompleted {

    // these matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;

    // private TextView txt;
    private ImageView imageView, imageViewFG;
    private Bitmap bitmap;
    private static final int PICK_IMAGE = 100;
    private static final int PICK_BG_IMAGE = 200;
    private static final int PICK_FILTER = 300;
    Uri imageUri;

    private int angle = 0, rotate_x, rotate_y;
    float trans_x, trans_y, diff_x, diff_y, scale_xy = 1f;
    int alpha = 255;
    int value = 0;

    private boolean filterForeground = false;
    Switch aSwitch;
    TextView fgtv;
    TextView bgtv;

    LinearLayout filterGallery;
    SeekBar sb;
    LinearLayout shapegallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fgtv = (TextView) findViewById(R.id.foregroundText);
        bgtv = (TextView) findViewById(R.id.backgroundText);

        aSwitch = (Switch) findViewById(R.id.switchImage);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    filterForeground = true;
                } else {
                    filterForeground = false;
                }
            }
        });
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        imageView = (ImageView) findViewById(R.id.imageView);
        imageViewFG = (ImageView) findViewById(R.id.imageViewFG);

        Button chooseButton = (Button) findViewById(R.id.chooseBG);
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickBGImage();
            }
        });

        Button changeButton = (Button) findViewById(R.id.chooseIM);
        changeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickImage();
            }
        });

        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToGallery();
            }
        });

        Button rotateButton = (Button) findViewById(R.id.rotate);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage();
            }
        });

        Button stampButton = (Button) findViewById(R.id.stamp);
        stampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stampImage();
            }
        });

        shapegallery = (LinearLayout) findViewById(R.id.shapegallery);
        Button filterButton = (Button) findViewById(R.id.filter);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (shapegallery.getVisibility() == View.VISIBLE)
                    shapegallery.setVisibility(View.GONE);
                else {
                    shapegallery.setVisibility(View.VISIBLE);
                    filterGallery.setVisibility(View.GONE);
                    sb.setVisibility(View.GONE);
                    aSwitch.setVisibility(View.GONE);
                    fgtv.setVisibility(View.GONE);
                    bgtv.setVisibility(View.GONE);
                }


            }
        });
        filterGallery = (LinearLayout) findViewById(R.id.filtergallery);
        Button adjustmentButton = (Button) findViewById(R.id.adjustments);
        adjustmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (filterGallery.getVisibility() == View.VISIBLE) {
                    filterGallery.setVisibility(View.GONE);
                    aSwitch.setVisibility(View.GONE);
                    fgtv.setVisibility(View.GONE);
                    bgtv.setVisibility(View.GONE);
                }
                else {
                    filterGallery.setVisibility(View.VISIBLE);
                    aSwitch.setVisibility(View.VISIBLE);
                    fgtv.setVisibility(View.VISIBLE);
                    bgtv.setVisibility(View.VISIBLE);
                    sb.setVisibility(View.GONE);
                    shapegallery.setVisibility(View.GONE);
                }

            }
        });

        Button grayscale = (Button) findViewById(R.id.grayscaleEffect);
        grayscale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterForeground) {
                    applyGreyscaleEffect();
                } else
                    applyGreyscaleEffectbg();
            }
        });

        Button brightness = (Button) findViewById(R.id.brightnessEffect);
        brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterForeground) {
                    applyBrightnessEffect();
                } else
                    applyBrightnessEffectbg();
            }
        });

        Button contrast = (Button) findViewById(R.id.contrastEffect);
        contrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterForeground) {
                    applyContrastEffect();
                } else
                    applyContrastEffectbg();
            }
        });

        Button hue = (Button) findViewById(R.id.hueEffect);
        hue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterForeground) {
                    applyHueFilter();
                } else
                    applyHueFilterbg();
            }
        });

        Button sepia = (Button) findViewById(R.id.sepiaEffect);
        sepia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterForeground) {
                    applySepiaEffect();
                } else
                    applySepiaEffectbg();
            }
        });

        Button invert = (Button) findViewById(R.id.invertEffect);
        invert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterForeground) {
                    applyInvertEffect();
                } else
                    applyInvertEffectbg();
            }
        });

        Button blur = (Button) findViewById(R.id.blurEffect);
        blur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterForeground) {
                    applyBlurEffect();
                } else
                    applyBlurEffectbg();
            }
        });

        Button decreaseDepth = (Button) findViewById(R.id.colorDepthEffect);
        decreaseDepth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterForeground) {
                    applyDecreaseColorDepthEffect();
                } else
                    applyDecreaseColorDepthEffectbg();
            }
        });

        sb = (SeekBar) findViewById(R.id.alphasb);
        Button transparency = (Button) findViewById(R.id.transparency);
        transparency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sb.getVisibility() == View.VISIBLE)
                    sb.setVisibility(View.GONE);
                else {
                    sb.setVisibility(View.VISIBLE);
                    shapegallery.setVisibility(View.GONE);
                    filterGallery.setVisibility(View.GONE);
                    aSwitch.setVisibility(View.GONE);
                    fgtv.setVisibility(View.GONE);
                    bgtv.setVisibility(View.GONE);
                }

            }
        });


        GetShapeList runner = new GetShapeList(MainActivity.this);
        runner.execute();


        imageViewFG.setOnTouchListener(this);


        // make both viewers the same size
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int w = imageView.getWidth();
                int h = imageView.getHeight();

                imageViewFG.getLayoutParams().width = w;
                imageViewFG.getLayoutParams().height = h;
                imageViewFG.requestLayout();

                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });

        SeekBar alphasb = (SeekBar) findViewById(R.id.alphasb);
        alphasb.setMax(255);
        alphasb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                alpha = 255 - progress;
                imageViewFG.setAlpha(alpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void stampImage() {
        float[] f = new float[9];
        imageViewFG.getImageMatrix().getValues(f);

        final int transX = Math.round(f[Matrix.MTRANS_X]);
        final int transY = Math.round(f[Matrix.MTRANS_Y]);

        Bitmap ivBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        Bitmap ivBitmapFG = ((BitmapDrawable)imageViewFG.getDrawable()).getBitmap();
        Bitmap ivBitmapNew = Bitmap.createBitmap(ivBitmap.getWidth(), ivBitmap.getHeight(), ivBitmap.getConfig());

        final int tx = transX * ivBitmap.getWidth() / imageView.getWidth();
        final int ty = transY * ivBitmap.getHeight() / imageView.getHeight();

        Canvas canvas = new Canvas(ivBitmapNew);
        canvas.drawBitmap(ivBitmap, 0, 0, null);

        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(alpha);

        Matrix matrix = new Matrix();

        // Look like the forground image has been scaled to fit intp the groundground imageviewer
        // without this adjustment, the stamped image is slight off the foreground image
        int scale_width = (int) ((BitmapDrawable)imageViewFG.getDrawable()).getIntrinsicWidth() * ivBitmap.getWidth() / imageView.getWidth();
        float scale_ratio = (float)  scale_width/ivBitmapFG.getWidth();

        matrix.preScale(scale_ratio * scale_xy, scale_ratio * scale_xy);
        matrix.postTranslate(0, 0);
        matrix.postRotate(angle);
        matrix.postTranslate(tx, ty);

        canvas.drawBitmap(ivBitmapFG, matrix, alphaPaint);
        imageView.setImageBitmap(ivBitmapNew);
    }

    private void saveToGallery() {

        Bitmap ivBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        try {
            String filename = getFilename();
            File image = new File(filename);
            if (image.createNewFile()) {
                FileOutputStream outputStream = new FileOutputStream(image);
                ivBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
            }

            Toast.makeText(getApplicationContext(), "Image Saved", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
        }
    }

    public static String getFilename() {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File mFolder = new File(storageDir.toString() + File.separator + "Polyscape");

        if (!mFolder.exists()) {
            mFolder.mkdir();
        }

        Long tsLong = System.currentTimeMillis() / 1000;
        String fileName = tsLong.toString();

        return mFolder.getAbsolutePath() + File.separator + fileName + ".jpg";
    }

    private void pickBGImage() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_BG_IMAGE);
    }

    private void pickImage() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void pickFilter() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_FILTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        trans_x = 0;
        trans_y = 0;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            imageUri = data.getData();

            if (requestCode == PICK_BG_IMAGE)
                imageView.setImageURI(imageUri);
            else if (requestCode == PICK_IMAGE)
                imageViewFG.setImageURI(imageUri);
            // else
            //   filterImage(/*imageUri*/);
        }

        // Make both viewers the same size after a new image is selected
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int w = imageView.getWidth();
                int h = imageView.getHeight();

                imageViewFG.getLayoutParams().width = w;
                imageViewFG.getLayoutParams().height = h;
                imageViewFG.requestLayout();

                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


    }

    private void filterImage(Bitmap imshpBitmapFilter) {
        // get the location and size of the foreground image
        float[] f = new float[9];
        imageViewFG.getImageMatrix().getValues(f);

        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];
        //Log.d("Test", "Org Size: " + String.valueOf(scaleX) + "," + String.valueOf(scaleY));

        final int transX = Math.round(f[Matrix.MTRANS_X]);
        final int transY = Math.round(f[Matrix.MTRANS_Y]);
        //Log.d("Test", "Trans X,Y: " + String.valueOf(transX) + "," + String.valueOf(transY));

        Bitmap imfgBitmap = ((BitmapDrawable) imageViewFG.getDrawable()).getBitmap();

        Bitmap imfgBitmapNew = Bitmap.createBitmap(imfgBitmap.getWidth(), imfgBitmap.getHeight(), imfgBitmap.getConfig());

        Canvas canvas = new Canvas(imfgBitmapNew);
        canvas.drawBitmap(imfgBitmap, 0, 0, null);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;


        //  Log.d("Test", "filterURI.getPath(): " + String.valueOf(filterURI.getPath()));

        // imshpBitmapFilter = null; // = BitmapFactory.decodeResource(getResources(), R.drawable.fgfilter1, options);
/*
        try {
            imshpBitmapFilter = MediaStore.Images.Media.getBitmap(getContentResolver(), filterURI);
        } catch (IOException ex) {

        }*/

/*
        URL url = null;
        try {
            url = new URL("http://elainetran.com/polyscape/shapes/fgfiltertriangle.png");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            imshpBitmapFilter = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
*/


        //Log.d("Test", "Foreground Image Width, Height: " + String.valueOf(imfgBitmapNew.getWidth()) + "," + String.valueOf(imfgBitmapNew.getHeight()));
        // Log.d("Test", "Foreground Image Width, Height: " + String.valueOf(imfgBitmapNew.get) + "," + String.valueOf(imfgBitmapNew.getHeight()));
        //Log.d("Test", "Shape Image Width, Height: " + String.valueOf(imshpBitmapFilter.getWidth()) + "," + String.valueOf(imshpBitmapFilter.getHeight()));

        int rx, ry, rw, rh;

        if (imfgBitmap.getWidth() < imfgBitmap.getHeight()) {
            rx = 0;
            ry = imfgBitmap.getHeight() / 2 - imfgBitmap.getWidth() / 2;
            rw = imfgBitmap.getWidth();
            rh = imfgBitmap.getWidth();
        } else {
            rx = imfgBitmap.getWidth() / 2 - imfgBitmap.getHeight() / 2;
            ry = 0;
            rw = imfgBitmap.getHeight();
            rh = imfgBitmap.getHeight();
        }

        Rect rectangle = new Rect(rx, ry, rx + rw, ry + rh);


        canvas.drawBitmap(imshpBitmapFilter, null, rectangle, null);

        Bitmap sideFiller = BitmapFactory.decodeResource(getResources(), R.drawable.fgfilterblue, options);

        if (imfgBitmap.getWidth() < imfgBitmap.getHeight()) {
            canvas.drawBitmap(sideFiller, null, new Rect(0, 0, imfgBitmap.getWidth(), (imfgBitmap.getHeight() - rh) / 2), null);
            canvas.drawBitmap(sideFiller, null, new Rect(0, (imfgBitmap.getHeight() - rh) / 2 + rh, imfgBitmap.getWidth(), imfgBitmap.getHeight()), null);
        } else {
            //Log.d("Test", "Left Filler (x, y, w, h): " + String.valueOf(0) + "," + String.valueOf(0) + "," + String.valueOf((imfgBitmap.getWidth()-rw)/2) + "," + String.valueOf(imfgBitmap.getHeight()));
            canvas.drawBitmap(sideFiller, null, new Rect(0, 0, (imfgBitmap.getWidth() - rw) / 2, imfgBitmap.getHeight()), null);
            canvas.drawBitmap(sideFiller, null, new Rect((imfgBitmap.getWidth() - rw) / 2 + rw, 0, imfgBitmap.getWidth(), imfgBitmap.getHeight()), null);
        }

        imfgBitmapNew = replaceIntervalColor(imfgBitmapNew, 0, 0, 0, 0, 255, 255, Color.TRANSPARENT);

        imageViewFG.setImageBitmap(imfgBitmapNew);
    }

    private void rotateImage() {

        angle = (angle + 15) % 360;

        final Drawable d = imageViewFG.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        matrix.reset();

        matrix.preScale(scale_xy, scale_xy);
        matrix.postTranslate(trans_x, trans_y);
        matrix.postRotate(angle, trans_x + origW*scale_xy/2, trans_y + origH*scale_xy/2);   // rotate aroound the center

        imageViewFG.setImageMatrix(matrix);

        if (angle == 0) {
        }
        else {
            float[] f = new float[9];
            imageViewFG.getImageMatrix().getValues(f);

            // after rotation, find the delta of the left-top corner from the point where the angle is zero
            // this point (angle=0) is needed to keep the rotated image in place while rotating
            diff_x  = Math.round(f[Matrix.MTRANS_X]) - trans_x;
            diff_y = Math.round(f[Matrix.MTRANS_Y]) - trans_y;
        }
    }

    private void screen_info_only()
    {
        Log.d("Test", "Background Viewer Width, Height: " + String.valueOf(imageView.getWidth()) + "," + String.valueOf(imageView.getHeight()));

        // This is the actual dimension of the background image
        Bitmap ivBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        Log.d("Test", "Background Image Width, Height: " + String.valueOf(ivBitmap.getWidth()) + "," + String.valueOf(ivBitmap.getHeight()));

        Log.d("Test", "Foreground Viewer Width, Height: " + String.valueOf(imageViewFG.getWidth()) + "," + String.valueOf(imageViewFG.getHeight()));

        // This is the actual dimension of the background image
        Bitmap ivfgBitmap = ((BitmapDrawable)imageViewFG.getDrawable()).getBitmap();
        Log.d("Test", "Foreground Image Width, Height: " + String.valueOf(ivfgBitmap.getWidth()) + "," + String.valueOf(ivfgBitmap.getHeight()));

        // The dimension of the background image shown on the screen (usaully scaled down by 1/3)
        Log.d("Test", "Foreground Image Intrinsic Width, Height: " + String.valueOf(((BitmapDrawable)imageViewFG.getDrawable()).getIntrinsicWidth()) + "," + String.valueOf(((BitmapDrawable)imageViewFG.getDrawable()).getIntrinsicHeight()));

        float[] f = new float[9];
        imageViewFG.getImageMatrix().getValues(f);
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];
        final int tw = (int) (scaleX * ((BitmapDrawable)imageViewFG.getDrawable()).getIntrinsicWidth() * ivBitmap.getWidth() / imageView.getWidth());
        final int th = (int) (scaleY * ((BitmapDrawable)imageViewFG.getDrawable()).getIntrinsicHeight() * ivBitmap.getHeight() / imageView.getHeight());

        // after zooming, this is the size shown on the screen
        Log.d("Test", "Foreground Scaled Image Width, Height: " + String.valueOf(tw) + "," + String.valueOf(th));

        // workaround, find to existing scale of a rotated image
        float scalex = f[Matrix.MSCALE_X];
        float skewy = f[Matrix.MSKEW_Y];
        float scale = (float) Math.sqrt(scalex * scalex + skewy * skewy);
        Log.d("Test", "Foreground scale value" + String.valueOf(scale));

        // calculate the degree of rotation based on the matrix
        float rAngle = Math.round(Math.atan2(f[Matrix.MSKEW_X], f[Matrix.MSCALE_X]) * (180 / Math.PI));
        Log.d("Test", "Foreground angle value: " + String.valueOf(rAngle));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public boolean onTouch(View v, MotionEvent event) {

        // handle touch events here
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (view.getWidth() / 2) * sx;
                        float yc = (view.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);

        float[] f = new float[9];
        matrix.getValues(f);
        if (angle == 0) {
            trans_x = Math.round(f[Matrix.MTRANS_X]);
            trans_y = Math.round(f[Matrix.MTRANS_Y]);
            scale_xy = f[0];
        }
        else {
            // calculate the left-top corner as if the angle is zero
            trans_x = Math.round(f[Matrix.MTRANS_X]) - diff_x;
            trans_y = Math.round(f[Matrix.MTRANS_Y]) - diff_y;

            // workaround, find to existing scale of a rotated image
            float scalex = f[Matrix.MSCALE_X];
            float skewy = f[Matrix.MSKEW_Y];
            scale_xy = (float) Math.sqrt(scalex * scalex + skewy * skewy);

        }
        return true;
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    public static Bitmap replaceIntervalColor(Bitmap bitmap, int redStart, int redEnd, int greenStart, int greenEnd, int blueStart, int blueEnd, int colorNew) {
        if (bitmap != null) {
            int picw = bitmap.getWidth();
            int pich = bitmap.getHeight();
            int[] pix = new int[picw * pich];
            bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);
            for (int y = 0; y < pich; y++) {
                for (int x = 0; x < picw; x++) {
                    int index = y * picw + x;
                    if (
                            ((Color.red(pix[index]) >= redStart) && (Color.red(pix[index]) <= redEnd)) &&
                                    ((Color.green(pix[index]) >= greenStart) && (Color.green(pix[index]) <= greenEnd)) &&
                                    ((Color.blue(pix[index]) >= blueStart) && (Color.blue(pix[index]) <= blueEnd))
                            ) {
                        pix[index] = colorNew;
                    }
                }
            }
            Bitmap bm = Bitmap.createBitmap(pix, picw, pich, Bitmap.Config.ARGB_8888);
            return bm;
        }
        return null;
    }

    public void OnTaskCompleted(Bitmap imshpBitmapFilter) {
       /* LinearLayout sg = (LinearLayout)findViewById(R.id.shapegallery);

        for(int i = 1; i < 20; i++) {
            ImageView iv = new ImageView(this);
            // iv.setBackgroundResource(imshpBitmapFilter);
            iv.setImageBitmap(imshpBitmapFilter);
            sg.addView(iv);
        }*/

        filterImage(imshpBitmapFilter);
    }

    public void GetShapeListCompleted(ArrayList<Bitmap> shapeList, ArrayList<String> nameList) {
        LinearLayout sg = (LinearLayout) findViewById(R.id.shapegallery);

        for (int i = 0; i < shapeList.size(); i++) {
            final String fileName = nameList.get(i);
            ImageView iv = new ImageView(this);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Test", fileName);
                    AsyncTaskRunner runner = new AsyncTaskRunner(MainActivity.this, fileName);
                    runner.execute();

                }
            });
            // iv.setBackgroundResource(imshpBitmapFilter);
            iv.setImageBitmap(shapeList.get(i));
            sg.addView(iv);
        }


    }

    public void applyGreyscaleEffect() {
        // constant factors
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;


        Bitmap bgBitmap = ((BitmapDrawable) imageViewFG.getDrawable()).getBitmap();

        // pixel information
        int A, R, G, B;
        int pixel;

        // get image size
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();

        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());
        int[] pixelData = new int[width * height];

        // scan through every single pixel
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get one pixel color
                pixel = bgBitmap.getPixel(x, y);
                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // take conversion up to one single value
                R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                // set new pixel color to output bitmap
                pixelData[y * (width) + x] = Color.argb(A, R, G, B);

            }
        }
        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageViewFG.setImageBitmap(temp);

    }

    public void applyGreyscaleEffectbg() {
        // constant factors
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;


        Bitmap bgBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        // pixel information
        int A, R, G, B;
        int pixel;

        // get image size
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();

        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());
        int[] pixelData = new int[width * height];

        // scan through every single pixel
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get one pixel color
                pixel = bgBitmap.getPixel(x, y);
                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // take conversion up to one single value
                R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                // set new pixel color to output bitmap
                pixelData[y * (width) + x] = Color.argb(A, R, G, B);

            }
        }
        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(temp);

    }

    public void applyBrightnessEffect() {
        Bitmap bgBitmap = ((BitmapDrawable) imageViewFG.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        int[] pixelData = new int[width * height];
        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = bgBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // increase/decrease each channel
                R += 50;
                if (R > 255) {
                    R = 255;
                } else if (R < 0) {
                    R = 0;
                }

                G += 50;
                if (G > 255) {
                    G = 255;
                } else if (G < 0) {
                    G = 0;
                }

                B += 50;
                if (B > 255) {
                    B = 255;
                } else if (B < 0) {
                    B = 0;
                }

                // apply new pixel color to output bitmap
                pixelData[y * (width) + x] = Color.argb(A, R, G, B);


            }
        }
        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageViewFG.setImageBitmap(temp);
    }

    public void applyBrightnessEffectbg() {
        Bitmap bgBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        int[] pixelData = new int[width * height];
        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = bgBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // increase/decrease each channel
                R += 50;
                if (R > 255) {
                    R = 255;
                } else if (R < 0) {
                    R = 0;
                }

                G += 50;
                if (G > 255) {
                    G = 255;
                } else if (G < 0) {
                    G = 0;
                }

                B += 50;
                if (B > 255) {
                    B = 255;
                } else if (B < 0) {
                    B = 0;
                }

                // apply new pixel color to output bitmap
                pixelData[y * (width) + x] = Color.argb(A, R, G, B);


            }
        }
        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(temp);
    }

    public void applyContrastEffect() {
        Bitmap bgBitmap = ((BitmapDrawable) imageViewFG.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());
        int[] pixelData = new int[width * height];

        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + 40) / 100, 2);

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = bgBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }

                G = Color.red(pixel);
                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }

                B = Color.red(pixel);
                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output bitmap
                pixelData[y * (width) + x] = Color.argb(A, R, G, B);

            }
        }

        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageViewFG.setImageBitmap(temp);
    }

    public void applyContrastEffectbg() {
        Bitmap bgBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());
        int[] pixelData = new int[width * height];

        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + 40) / 100, 2);

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = bgBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }

                G = Color.red(pixel);
                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }

                B = Color.red(pixel);
                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output bitmap
                pixelData[y * (width) + x] = Color.argb(A, R, G, B);

            }
        }

        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(temp);
    }

    public void applyHueFilter() {
        Bitmap bgBitmap = ((BitmapDrawable) imageViewFG.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());

        int[] pixels = new int[width * height];
        float[] HSV = new float[3];
        // get pixel array from source
        bgBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int index = 0;
        // iteration through pixels
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // convert to HSV
                Color.colorToHSV(pixels[index], HSV);
                // increase Saturation level
                HSV[0] *= 2;
                HSV[0] = (float) Math.max(0.0, Math.min(HSV[0], 360.0));
                // take color back
                pixels[index] |= Color.HSVToColor(HSV);
            }
        }
        // output bitmap

        temp.setPixels(pixels, 0, width, 0, 0, width, height);
        imageViewFG.setImageBitmap(temp);
    }

    public void applyHueFilterbg() {
        Bitmap bgBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());

        int[] pixels = new int[width * height];
        float[] HSV = new float[3];
        // get pixel array from source
        bgBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int index = 0;
        // iteration through pixels
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // convert to HSV
                Color.colorToHSV(pixels[index], HSV);
                // increase Saturation level
                HSV[0] *= 2;
                HSV[0] = (float) Math.max(0.0, Math.min(HSV[0], 360.0));
                // take color back
                pixels[index] |= Color.HSVToColor(HSV);
            }
        }
        // output bitmap

        temp.setPixels(pixels, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(temp);
    }

    public  void applySepiaEffect() {
        Bitmap bgBitmap = ((BitmapDrawable)imageViewFG.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());

        final double GS_RED = 0.3;
        final double GS_GREEN = 0.59;
        final double GS_BLUE = 0.11;

        int A, R, G, B;
        int pixel;
        int[] pixelData = new int[width * height];
        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = bgBitmap.getPixel(x, y);
                // get color on each channel
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // apply grayscale sample
                B = G = R = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);

                // apply intensity level for sepia-toning on each channel
                R += (10 * 1.5);
                if(R > 255) { R = 255; }

                G += (10 * 0.6);
                if(G > 255) { G = 255; }

                B += (10 * 0.12);
                if(B > 255) { B = 255; }

                pixelData[y * (width) + x] = Color.argb(A, R, G, B);
            }
        }

        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageViewFG.setImageBitmap(temp);

    }

    public  void applySepiaEffectbg() {
        Bitmap bgBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());

        final double GS_RED = 0.3;
        final double GS_GREEN = 0.59;
        final double GS_BLUE = 0.11;

        int A, R, G, B;
        int pixel;
        int[] pixelData = new int[width * height];
        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = bgBitmap.getPixel(x, y);
                // get color on each channel
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // apply grayscale sample
                B = G = R = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);

                // apply intensity level for sepia-toning on each channel
                R += (10 * 1.5);
                if(R > 255) { R = 255; }

                G += (10 * 0.6);
                if(G > 255) { G = 255; }

                B += (10 * 0.12);
                if(B > 255) { B = 255; }

                pixelData[y * (width) + x] = Color.argb(A, R, G, B);
            }
        }

        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(temp);

    }

    public void applyInvertEffect() {
        Bitmap bgBitmap = ((BitmapDrawable)imageViewFG.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());
        int A, R, G, B;
        int pixelColor;
        int[] pixelData = new int[width * height];


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // get one pixel
                pixelColor = bgBitmap.getPixel(x, y);
                // saving alpha channel
                A = Color.alpha(pixelColor);
                // inverting byte for each R/G/B channel
                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);
                // set newly-inverted pixel to output image
                pixelData[y * (width) + x] = Color.argb(A, R, G, B);
            }
        }

        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageViewFG.setImageBitmap(temp);
    }

    public void applyInvertEffectbg() {
        Bitmap bgBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());
        int A, R, G, B;
        int pixelColor;
        int[] pixelData = new int[width * height];


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // get one pixel
                pixelColor = bgBitmap.getPixel(x, y);
                // saving alpha channel
                A = Color.alpha(pixelColor);
                // inverting byte for each R/G/B channel
                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);
                // set newly-inverted pixel to output image
                pixelData[y * (width) + x] = Color.argb(A, R, G, B);
            }
        }

        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(temp);
    }

    public void applyBlurEffect() {

        Bitmap bgBitmap = ((BitmapDrawable)imageViewFG.getDrawable()).getBitmap();

        double[][] GaussianBlurConfig = new double[][] {
                { 1, 2, 1 },
                { 2, 4, 2 },
                { 1, 2, 1 }
        };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(GaussianBlurConfig);
        convMatrix.Factor = 16;
        convMatrix.Offset = 25;
        Bitmap temp = ConvolutionMatrix.computeConvolution3x3(bgBitmap, convMatrix);
        imageViewFG.setImageBitmap(temp);
    }

    public void applyBlurEffectbg() {

        Bitmap bgBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        double[][] GaussianBlurConfig = new double[][] {
                { 1, 2, 1 },
                { 2, 4, 2 },
                { 1, 2, 1 }
        };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(GaussianBlurConfig);
        convMatrix.Factor = 16;
        convMatrix.Offset = 25;
        Bitmap temp = ConvolutionMatrix.computeConvolution3x3(bgBitmap, convMatrix);
        imageView.setImageBitmap(temp);
    }

    public  void applyDecreaseColorDepthEffect() {
        Bitmap bgBitmap = ((BitmapDrawable)imageViewFG.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());
        int[] pixelData = new int[width * height];
        int A, R, G, B;
        int pixel;


        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = bgBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // round-off color offset
                R = ((R + (32 / 2)) - ((R + (32 / 2)) % 32) - 1);
                if(R < 0) { R = 0; }
                G = ((G + (32 / 2)) - ((G + (32 / 2)) % 32) - 1);
                if(G < 0) { G = 0; }
                B = ((B + (32 / 2)) - ((B + (32 / 2)) % 32) - 1);
                if(B < 0) { B = 0; }

                pixelData[y * (width) + x] = Color.argb(A, R, G, B);
            }
        }

        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageViewFG.setImageBitmap(temp);
    }

    public  void applyDecreaseColorDepthEffectbg() {
        Bitmap bgBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        int width = bgBitmap.getWidth();
        int height = bgBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(width, height, bgBitmap.getConfig());
        int[] pixelData = new int[width * height];
        int A, R, G, B;
        int pixel;


        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = bgBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // round-off color offset
                R = ((R + (32 / 2)) - ((R + (32 / 2)) % 32) - 1);
                if(R < 0) { R = 0; }
                G = ((G + (32 / 2)) - ((G + (32 / 2)) % 32) - 1);
                if(G < 0) { G = 0; }
                B = ((B + (32 / 2)) - ((B + (32 / 2)) % 32) - 1);
                if(B < 0) { B = 0; }

                pixelData[y * (width) + x] = Color.argb(A, R, G, B);
            }
        }

        temp.setPixels(pixelData, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(temp);
    }
}
