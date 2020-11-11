/*
 * Copyright 2018 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.silentquot.socialcomponents.main.post;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.silentquot.R;
import com.silentquot.socialcomponents.main.pickImageBase.PickImageActivity;
import com.silentquot.socialcomponents.utils.ColorFilterGenerator;

import java.io.ByteArrayOutputStream;

public abstract class BaseCreatePostActivity<V extends BaseCreatePostView, P extends BaseCreatePostPresenter<V>>
        extends PickImageActivity<V, P> implements BaseCreatePostView {

    protected ImageView imageView;
    protected ProgressBar progressBar;
    protected EditText titleEditText;
    protected TextView Quotes ,authorName;
    protected EditText descriptionEditText;
    protected  RelativeLayout imageContainer;
   protected LinearLayout quotesContainer;
    String QUOTES ="quotes" ;
    Button addOverLay;
    View overLay , vignetteView1, vignetteView2 , borderLayout;
    SeekBar seekbarOverLay , fontsizeeekBar , brightnessseekbar ,borderLayoutSeekBar  , adjustSaturationSeekBar ,adjustHueSeekBar,VignetteSeekBar , adjustExposureSeekBar ;
    Switch overlaySwitch,brightnessSwitch  , borderLayoutSwitch ,adjustSaturationSwitch , adjustHueSwitch , adjustExposureSwitch ,vignetterSwitch;

    final static float STEP = 200;
    float mRatio = 1.0f;
    int mBaseDist;
    float mBaseRatio;

    static  boolean vignetteSwitchStatus=false;
    static  boolean overlaySwitchStatue=false;
    static  boolean brightnessSwitchStatue=false;
    static  boolean SaturationSwitchStatue=false;
    static  boolean borderLayoutSwitchStatue=false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_create_post_activity);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initToolbar();

        onSelectImageClick(getImageView());

        titleEditText = findViewById(R.id.titleEditText);
        Quotes = findViewById(R.id.Quotes);
        authorName=findViewById(R.id.authorName);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        progressBar = findViewById(R.id.progressBar);
        quotesContainer=findViewById(R.id.quotesContainer);
        overLay=findViewById(R.id.overLay);
        vignetteView1=findViewById(R.id.VignetteView1);
        vignetteView2=findViewById(R.id.VignetteView2);
        borderLayout=findViewById(R.id.borderLayout);

        vignetteView1.setBackground(getDrawable(R.drawable.centerbggraditent));
        vignetteView2.setBackground(getDrawable(R.drawable.centerbggraditent1));

        vignetteView1.setVisibility(View.GONE);
        vignetteView2.setVisibility(View.GONE);
        borderLayout.setVisibility(View.GONE);

        overLay.setBackgroundColor(Color.BLACK);
        overLay.getBackground().setAlpha(25);




        //initSwitch
        initSwitch();
        initseekbar();



        imageContainer=findViewById(R.id.imageContainer);
        generateTv(quotesContainer);
        imageView = findViewById(R.id.imageView);
        titleEditText.setText(getIntent().getStringExtra(QUOTES));
      //  imageView.setOnClickListener(v -> onSelectImageClick(v));


        titleEditText.addTextChangedListener(quoteTextWatcher);
        descriptionEditText.addTextChangedListener(authorNameTextWatcher);
        titleEditText.setOnTouchListener((v, event) -> {

            if (titleEditText.hasFocus() && titleEditText.getError() != null) {
                titleEditText.setError(null);
                return true;
            }

            return false;
        });


    //    Quotes.setOnTouchListener(fontSizeChange);
    }

    public  void initseekbar()
    {
        seekbarOverLay = (SeekBar)findViewById(R.id.overLaySeekBar);
        fontsizeeekBar=findViewById(R.id.fontsizeeekBar);
        brightnessseekbar=findViewById(R.id.brightnessseeekbar);
        VignetteSeekBar=findViewById(R.id.VignetteSeekBar);
        borderLayoutSeekBar=findViewById(R.id.borderLayoutSeekBar);
        adjustSaturationSeekBar=findViewById(R.id.adjustSaturationSeekBar);


        fontsizeeekBar.setOnSeekBarChangeListener(fontsizeeekBarListner);
       brightnessseekbar.setOnSeekBarChangeListener(brightnessSeekBarListner);
        VignetteSeekBar.setOnSeekBarChangeListener(vignetteSeekBarListner);
       borderLayoutSeekBar.setOnSeekBarChangeListener(borderLayoutSeekBarListner);
       // adjustHueSeekBar.setOnSeekBarChangeListener(HueSeekBar);
        adjustSaturationSeekBar.setOnSeekBarChangeListener(SaturationSeekBar);


    }

    public void initSwitch()
    {
        overlaySwitch= (Switch) findViewById(R.id.overLaySwitch);
        brightnessSwitch= (Switch) findViewById(R.id.brightnessSwitch);
        vignetterSwitch= (Switch) findViewById(R.id.vignetterSwitch);
        adjustSaturationSwitch= (Switch) findViewById(R.id.adjustSaturationSwitch);
        borderLayoutSwitch=findViewById(R.id.borderLayoutSwitch);


        overlaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    overLay.setVisibility(View.VISIBLE);

                    overlaySwitchStatue=true;
                    seekbarOverLay.setOnSeekBarChangeListener(overLaySeekBarListner);
                } else {
                    overLay.setVisibility(View.GONE);
                    overlaySwitchStatue=false;
                }
            }
        });
        brightnessSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    brightnessSwitchStatue=true;
                    brightnessseekbar.setOnSeekBarChangeListener(brightnessSeekBarListner);
                    imageView.setColorFilter(ColorFilterGenerator.adjustBrightness(brightnessseekbar.getProgress()));

                } else {
                    imageView.setColorFilter(ColorFilterGenerator.adjustBrightness(0));
                    brightnessSwitchStatue=false;
                }
            }
        });

        borderLayoutSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    borderLayoutSwitchStatue=true;
                    borderLayout
                            .setVisibility(View.VISIBLE);
                    borderLayoutSeekBar.setOnSeekBarChangeListener(borderLayoutSeekBarListner);
                } else {
                    borderLayout.setVisibility(View.GONE);
                    brightnessSwitchStatue=false;
                }
            }
        });


        adjustSaturationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    SaturationSwitchStatue=true;
                    adjustSaturationSeekBar.setOnSeekBarChangeListener(SaturationSeekBar);
                    imageView.setColorFilter(ColorFilterGenerator.adjustSaturation(adjustSaturationSeekBar.getProgress()));

                } else {
                    imageView.setColorFilter(ColorFilterGenerator.adjustBrightness(0));
                    SaturationSwitchStatue=false;
                }
            }
        });

        vignetterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    vignetteView1.setVisibility(View.VISIBLE);
                    vignetteView2.setVisibility(View.VISIBLE);
                    vignetteView1.getBackground().setAlpha(25);
                    vignetteView2.getBackground().setAlpha(25);
                    vignetteSwitchStatus=true;
                    VignetteSeekBar.setOnSeekBarChangeListener(vignetteSeekBarListner);

                } else {
                    overLay.setVisibility(View.GONE);
                    vignetteView1.setVisibility(View.GONE);
                    vignetteView2.setVisibility(View.GONE);
                    vignetteSwitchStatus=false;
                }
            }
        });




    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected ProgressBar getProgressView() {
        return progressBar;
    }

    @Override
    protected ImageView getImageView() {
        return imageView;
    }

    @Override
    protected void onImagePikedAction() {
      //  loadImageToImageView(imageUri);
        startCropImageActivity();
    }

    @Override
    public void setDescriptionError(String error) {
        descriptionEditText.setError(error);
        descriptionEditText.requestFocus();
    }

    @Override
    public void setTitleError(String error) {
        titleEditText.setError(error);
        titleEditText.requestFocus();
    }

    @Override
    public String getTitleText() {
        return titleEditText.getText().toString();
    }

    @Override
    public String getDescriptionText() {
        return descriptionEditText.getText().toString();
    }

    @Override
    public void requestImageViewFocus() {
        imageView.requestFocus();
    }

    @Override
    public void onPostSavedSuccess() {
        setResult(RESULT_OK);
        Toast.makeText(this  , "PostUpdated" , Toast.LENGTH_SHORT).show();
        this.finish();
    }

    @Override
    public Uri getImageUri()
    {
        Bitmap bitmap =getBitmapFromView(imageContainer);
        Uri img=getImageUri(getApplicationContext() , bitmap);
        return img;
    }

    private void generateTv(LinearLayout tv) {

            int Xposition = 150;
            int Yposition = 500;
            Typeface tf = Typeface.createFromAsset(getAssets(), "font/Kalam-Bold.ttf");
            Quotes.setTypeface(tf);
            authorName.setTypeface(tf);
            authorName.setTextColor(Color.WHITE);
            Quotes.setText(getIntent().getStringExtra(QUOTES));
            tv.setX(Xposition);
           // authorName.setX(Xposition);
            tv.setY(Yposition);
          //  authorName.setY(Yposition);
            Quotes.setTextColor(Color.WHITE);
            Quotes.setTextSize(18);
            Quotes.setSingleLine(false);
            tv.setOnTouchListener(mOnTouchListenerTv2);



    }
    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        super.onActivityResult(requestCode, resultCode, data);
        handleCropImageResult(requestCode, resultCode, data);
    }

    public final View.OnTouchListener fontSizeChange = new View.OnTouchListener() {
        int prevX, prevY;

        public boolean onTouch(View v , MotionEvent event) {

            if (event.getPointerCount() == 2) {
                int action = event.getAction();
                int pureaction = action & MotionEvent.ACTION_MASK;
                if (pureaction == MotionEvent.ACTION_POINTER_DOWN) {
                    mBaseDist = getDistance(event);
                    mBaseRatio = mRatio;
                } else {
                    float delta = (getDistance(event) - mBaseDist) / STEP;
                    float multi = (float) Math.pow(2, delta);
                    mRatio = Math.min(1024.0f, Math.max(0.1f, mBaseRatio * multi));
                    Quotes.setTextSize(mRatio + 13);
                    int progress = (int) (mRatio+13);
                    fontsizeeekBar.setProgress(progress);
                }
            }
            return true;
        }

        int getDistance(MotionEvent event) {
            int dx = (int) (event.getX(0) - event.getX(1));
            int dy = (int) (event.getY(0) - event.getY(1));
            return (int) (Math.sqrt(dx * dx + dy * dy));
        }

    };


    private  TextWatcher authorNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            authorName.setText("-"+descriptionEditText.getText());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private TextWatcher quoteTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Quotes.setText(titleEditText.getText());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public  final  SeekBar.OnSeekBarChangeListener overLaySeekBarListner =new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if (overlaySwitchStatue) {
                overLay.setVisibility(View.VISIBLE);
                overLay.getBackground().setAlpha(progress * 2);
            }
        }
    };


    public  final SeekBar.OnSeekBarChangeListener fontsizeeekBarListner = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Quotes.setTextSize(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

  public  final SeekBar.OnSeekBarChangeListener  brightnessSeekBarListner = new SeekBar.OnSeekBarChangeListener(){
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          if (brightnessSwitchStatue)
          imageView.setColorFilter(ColorFilterGenerator.adjustBrightness(progress));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
  };

  public  final SeekBar.OnSeekBarChangeListener  vignetteSeekBarListner = new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

          if (vignetteSwitchStatus) {
              vignetteView1.getBackground().setAlpha(progress * 2);
              vignetteView2.getBackground()
                      .setAlpha(progress * 2);
          }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
  };


    public  final SeekBar.OnSeekBarChangeListener  borderLayoutSeekBarListner = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (borderLayoutSwitchStatue) {
                GradientDrawable myGrad = (GradientDrawable) borderLayout.getBackground();
                myGrad.setStroke(progress, Color.WHITE);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public  final SeekBar.OnSeekBarChangeListener  SaturationSeekBar = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (SaturationSwitchStatue) {
                imageView.setColorFilter(ColorFilterGenerator.adjustSaturation(progress));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public final View.OnTouchListener mOnTouchListenerTv2 = new View.OnTouchListener() {
        int prevX, prevY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final RelativeLayout.LayoutParams par = (RelativeLayout.LayoutParams) v.getLayoutParams();

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE: {
                    par.topMargin += (int) event.getRawY() - prevY;

                    prevY = (int) event.getRawY();
                    par.leftMargin += (int) event.getRawX() - prevX;

                    prevX = (int) event.getRawX();
                    v.setLayoutParams(par);
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    par.topMargin += (int) event.getRawY() - prevY;

                    par.leftMargin += (int) event.getRawX() - prevX;

                    v.setLayoutParams(par);

                    return true;
                }
                case MotionEvent.ACTION_DOWN: {
                    prevX = (int) event.getRawX();
                    prevY = (int) event.getRawY();
                    par.bottomMargin = -2 * v.getHeight();
                    par.rightMargin = -2 * v.getWidth();
                    v.setLayoutParams(par);
                    return true;
                }
            }
            return false;
        }
    };


    private Bitmap getBitmapFromView(View view)
    {
        Bitmap returnedBitmap=Bitmap.createBitmap(view.getWidth() , view.getHeight() ,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(returnedBitmap);
        Drawable bgDrawable=view.getBackground();
        if(bgDrawable!=null)
        {
            bgDrawable.draw(canvas);
        }else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

}
