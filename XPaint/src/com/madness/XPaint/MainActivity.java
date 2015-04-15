package com.madness.XPaint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Time;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity {

    DrawingView dv ;
    private Paint mPaint;

    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Paint> paints = new ArrayList<Paint>();

    private Canvas mCanvas;
    private Bitmap  mBitmap;

    //private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;

    private Bitmap bitmap;
    private int bitmapHeight = 0;
    private int bitmapWidth = 0;
    private int brushSize = 5;
    private int transCol = 255;
    private int brushColor = Color.WHITE;
    private int selectedColor = 0;

    private Spinner colorSpinner;
    private Spinner toolSpinner;

    private static int RESULT_LOAD_IMAGE = 1;
    private String urlImage;
    private Boolean changeBackground = false;

    private int sampleSize = 0;

    private int tool = 0;

    //tool shit
    private LayoutInflater inflater;
    private View convertView;

    private Bitmap screenBitmap;

    private ProgressDialog simpleWaitDialog;

    private int colR = 255;
    private int colG = 255;
    private int colB = 255;

    @Override
    public void onResume() {
        super.onResume();
    }

    public void paintSettings(){
        try{
            mPaint.reset();
        } catch(Exception e){}
            mPaint = new Paint();
            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(brushColor);
            mPaint.setAlpha(transCol);
        if (tool == 1){
        }
        if (tool == 0){
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(brushSize);
        }
    }

    public Point getScreenSize(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public int getScreenWidth(){
        return getScreenSize().x;
    }

    public int getScreenHeight(){
        return getScreenSize().y;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dv = new DrawingView(this);
        setContentView(dv);
        paintSettings();

        Intent intent = getIntent();
        String message = intent.getStringExtra(MenuActivity.type);

        dv.invalidate();

        switch(Integer.valueOf(message)){
            case 2131165196:
                try{
                    browse();
                }catch (Exception e){}
                break;
            case 2131165199:
                try{
                    takePicture();
                } catch (Exception e){}
                break;
        }
        Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_SHORT).show();
    }

    public class DrawingView extends View {

        //Get resolutions (not for new years eve)
        public Point getScreenSize(){
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size;
        }

        public int getScreenWidth(){
            return getScreenSize().x;
        }

        public int getScreenHeight(){
            return getScreenSize().y;
        }

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.MAGENTA);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
        }

        public Bitmap loadBackground(){
            if(bitmap == null){
                bitmap = openDocument();
            }
            if(changeBackground){
                bitmap = openDocument();
                changeBackground = false;
            }
            return bitmap;
        }

        public Bitmap openDocument(){
            FileInputStream in;
            BufferedInputStream buf;
            Bitmap mutableBitmap = null;
            //
            try
            {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = sampleSize;

                in = new FileInputStream(urlImage);
                buf = new BufferedInputStream(in);
                bitmap = BitmapFactory.decodeStream(buf, null, opt);
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, getScreenHeight() * bitmap.getWidth() / bitmap.getHeight(), getScreenHeight(), true);

                if (bitmap.getHeight() > getScreenHeight())
                    bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * getScreenHeight() / bitmap.getHeight(), getScreenHeight(), true);
                if (bitmap.getWidth() > getScreenWidth())
                    bitmap = Bitmap.createScaledBitmap(bitmap, getScreenWidth(), bitmap.getHeight() * getScreenWidth() / bitmap.getWidth(), true);

                if (in != null) {
                    in.close();
                }
                if (buf != null) {
                    buf.close();
                }
                mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }

            catch(OutOfMemoryError E){
                sampleSize = sampleSize + 2;
                System.out.println("sampleSize: " + sampleSize);
                openDocument();
                invalidate();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch(StackOverflowError e){
                e.printStackTrace();
            }

            clearScreen();

            bitmapHeight = getScreenHeight() / 2 - mutableBitmap.getHeight() / 2;
            bitmapWidth = getScreenWidth() / 2 - mutableBitmap.getWidth() / 2;
            return mutableBitmap;
        }

                @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            try{
                canvas.drawBitmap(loadBackground(), bitmapWidth, bitmapHeight, mBitmapPaint);
            }catch (Exception e){}
            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath, mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            //bladi bladi. dit moet anders kan je geen stipjes zetten:
            mPath.quadTo(x, y, x + 0.001f, y + 0.001f);
            mX = x;
            mY = y;
            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();

            paintSettings();

            paths.add(new Path(mPath));
            paints.add(new Paint(mPaint));

            mCanvas.drawPath(mPath,  mPaint);
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    @Override
    public void onBackPressed(){
        undoStroke();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getBrushSettings();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        //MenuInflater inflater2 = getMenuInflater();
        //inflater2.inflate(R.menu.brush_menu, menu);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                System.out.println(data.getData());
                OpenPicture op = new OpenPicture();
                op.execute(data);
            }
        }
    }

    public void takePicture() {
        try {
            Intent imageLoadIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(imageLoadIntent, 1);
        } catch (Exception e){}

    }

    public void browse() {
        try {
            Intent imageLoadIntent = new Intent();
            imageLoadIntent.setType("image/*");
            imageLoadIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(imageLoadIntent, "Select Picture"), 1);
        } catch (Exception e){}
    }

    public void undoStroke(){
        clearScreen();
        if(paths.size() > 0){
            for (int i = 0; i < (paths.size() - 1); i++){
                Path memPath = paths.get(i);
                Paint memPaint = paints.get(i);
                mCanvas.drawPath(memPath, memPaint);
            }
            paths.remove(paths.size() - 1);
            paints.remove(paints.size() - 1);
        }
    }

    public void clearScreen(){
        mCanvas.drawColor(Color.BLACK);
        if(bitmap != null){
            mCanvas.drawBitmap(bitmap, bitmapWidth, bitmapHeight, mBitmapPaint);
        }
        dv.invalidate();
    }

    public void resetArrayLists(){
        paths.clear();
        paints.clear();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage, String filename) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, filename, null);
        return Uri.parse(path);
    }

    //Die fucking shit van 01-01-1970 01:00 moet gefixed worden
    private void galleryAddPic(Bitmap picture, String filename) {
        Uri contentUri = getImageUri(this, picture, filename);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        sendBroadcast(mediaScanIntent);
        Time now = new Time();
        now.setToNow();
        System.out.println(now);
    }

    private class SavePicture extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... args) {
            Bitmap screenBitmap;
            View v1 = dv.getRootView();
            v1.setDrawingCacheEnabled(true);
            screenBitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            if(screenBitmap != null)
                galleryAddPic(screenBitmap, "XPaint" + (int)(System.currentTimeMillis()/1000) + ".jpg");
            return "succes";
        }

        @Override
        protected void onPreExecute() {
            simpleWaitDialog = ProgressDialog.show(MainActivity.this, "Please wait", "Saving image...");
        }

        @Override
        protected void onPostExecute(String string) {
            try {
                simpleWaitDialog.dismiss();
                simpleWaitDialog = null;
            } catch (Exception e) {}
            Toast.makeText(getApplicationContext(), "Image saved", Toast.LENGTH_SHORT).show();
        }
    }

    private class OpenPicture extends AsyncTask<Intent, String, String> {

        @Override
        protected String doInBackground(Intent ... args) {
            Uri currImageURI = args[0].getData();
            String result;

            Cursor cursor = getContentResolver().query(currImageURI, null, null, null, null);
            if (cursor == null) {
                result = currImageURI.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }

            urlImage = result;
            sampleSize = 0;
            changeBackground = true;
            return "succes";
        }
        @Override
        protected void onPostExecute(String string) {
            clearScreen();
            Toast.makeText(getApplicationContext(), "Image loaded", Toast.LENGTH_SHORT).show();
        }
    }

    public void getBrushSettings(){
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.brush_settings, null);

        final AlertDialog d = new AlertDialog.Builder(this)
                .setView(convertView)
                .setPositiveButton("Back", null)
                .show();

        final TextView brushSizeText = (TextView) convertView.findViewById(R.id.brushSizeTxt);
        SeekBar seekBarBrush = (SeekBar) convertView.findViewById(R.id.brushBar);

        final TextView transSizeText = (TextView) convertView.findViewById(R.id.transTxt);
        SeekBar seekBarTrans = (SeekBar) convertView.findViewById(R.id.transBar);


        SeekBar seekBarR = (SeekBar) convertView.findViewById(R.id.colorRBar);
        SeekBar seekBarG = (SeekBar) convertView.findViewById(R.id.colorGBar);
        SeekBar seekBarB = (SeekBar) convertView.findViewById(R.id.colorBBar);

        seekBarR.setProgress(colR);
        seekBarG.setProgress(colG);
        seekBarB.setProgress(colB);

        final TableLayout tableBg = (TableLayout) convertView.findViewById(R.id.tableBg);

        tableBg.setBackgroundColor(Color.rgb(colR, colG, colB));

        seekBarR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colR = seekBar.getProgress();
                tableBg.setBackgroundColor(Color.rgb(colR, colG, colB));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        seekBarG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colG = seekBar.getProgress();
                tableBg.setBackgroundColor(Color.rgb(colR, colG, colB));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        seekBarB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colB = seekBar.getProgress();
                tableBg.setBackgroundColor(Color.rgb(colR, colG, colB));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        toolSpinner = (Spinner) convertView.findViewById(R.id.brushSpinner);
        Resources res2 = getResources();
        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, res2.getStringArray(R.array.tools_array));
        toolSpinner.setAdapter(spinnerArrayAdapter2);
        toolSpinner.setSelection(tool);

        brushSizeText.setText(String.valueOf(brushSize));
        transSizeText.setText(String.valueOf((int)(transCol/2.55)));

        seekBarBrush.setProgress(brushSize);
        seekBarBrush.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brushSize = seekBar.getProgress();
                brushSizeText.setText(String.valueOf(brushSize));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        seekBarTrans.setProgress(transCol);
        seekBarTrans.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                transCol = seekBar.getProgress();
                transSizeText.setText(String.valueOf((int)(transCol/2.55)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                brushColor = Color.rgb(colR, colG, colB);

                tool = toolSpinner.getSelectedItemPosition();

                d.dismiss();
                paintSettings();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.newImage:
                //clearScreen();
                bitmap = null;
                urlImage = null;
                clearScreen();
                resetArrayLists();
                return true;
            case R.id.clear:
                clearScreen();
                resetArrayLists();
                return true;
            case R.id.open:
                try{
                    clearScreen();
                    resetArrayLists();
                    browse();
                }catch (Exception e){}
                return true;
            case R.id.save:

                /*File storageDir = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                        ),
                        "XPaint"
                );*/

                SavePicture sp = new SavePicture();
                sp.execute();

                return true;
            case R.id.camera:
                try{
                    clearScreen();
                    resetArrayLists();
                    takePicture();
                }catch (Exception e){}
                return true;
            case R.id.tools:
                getBrushSettings();


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}